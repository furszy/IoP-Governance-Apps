package iop.org.voting_app.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import iop.org.iop_contributors_app.R;
import iop.org.voting_app.base.VotingBaseActivity;
import iop.org.voting_app.ui.components.proposals.VotingProposalsAdapter;
import iop.org.voting_app.ui.dialogs.VoteDialog;
import iop_sdk.governance.propose.Proposal;

/**
 * Created by mati on 17/11/16.
 */

public class VotingProposalsActivity extends VotingBaseActivity {

    private static final String TAG = "VotingProposalsActivity";

    private View root;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private VotingProposalsAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private View container_empty_screen;

    private List<Proposal> proposals;


    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        root = getLayoutInflater().inflate(R.layout.proposals_voting_main,container);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_proposals);
        container_empty_screen = root.findViewById(R.id.container_empty_screen);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new VotingProposalsAdapter(this,module);
        recyclerView.setAdapter(adapter);

        container_empty_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoteDialog(null);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                executor.submit(loadProposals);
            }
        });

    }


    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * @param data
     */
    public void showVoteDialog(Proposal data) {
        VoteDialog voteDialog = VoteDialog.newInstance(data);
        voteDialog.show(getFragmentManager(),"insuficientFundsDialog");
    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected void onResume() {
        if (proposals==null){
            executor.execute(loadProposals);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    Runnable loadProposals = new Runnable() {
        @Override
        public void run() {
            try {
                proposals = module.getVotingProposals();
                Log.d(TAG,"Proposals loaded: "+proposals.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (proposals!=null && !proposals.isEmpty()) {
                            hideEmptyScreen();
                            adapter.changeDataSet(proposals);
                        } else
                            showEmptyScreen();
                        onItemsLoadComplete();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private void showEmptyScreen(){
        container_empty_screen.setVisibility(View.VISIBLE);
    }

    private void hideEmptyScreen(){
        container_empty_screen.setVisibility(View.INVISIBLE);
    }


}
