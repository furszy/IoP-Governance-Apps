package iop.org.iop_contributors_app.ui.voting;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.propose.Proposal;
import iop.org.iop_contributors_app.ui.voting.base.VotingBaseActivity;
import iop.org.iop_contributors_app.ui.voting.ui.components.my_votes.MyVotesAdapter;
import iop.org.iop_contributors_app.ui.voting.ui.dialogs.VoteDialog;
import iop.org.iop_contributors_app.ui.voting.util.VoteWrapper;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 17/11/16.
 */

public class VotingMyVotesActivity extends VotingBaseActivity {


    private WalletModule module;

    private View root;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MyVotesAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private View container_empty_screen;

    private List<VoteWrapper> votes;


    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        root = getLayoutInflater().inflate(R.layout.proposals_voting_main,container);

        module = ApplicationController.getInstance().getWalletModule();

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
        // todo: adapter
        adapter = new MyVotesAdapter(this,module);
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
                executor.submit(loadMyVotes);
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
        if (votes==null){
            executor.execute(loadMyVotes);
        }
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    Runnable loadMyVotes = new Runnable() {
        @Override
        public void run() {
            try {
                votes = module.listMyVotes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (votes!=null && !votes.isEmpty()) {
                            hideEmptyScreen();
                            adapter.changeDataSet(votes);
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
