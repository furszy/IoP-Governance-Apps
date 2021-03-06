package iop.org.voting_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import iop.org.furszy_lib.utils.AnimationUtils;
import iop.org.voting_app.R;
import iop.org.voting_app.base.VotingBaseActivity;
import iop.org.voting_app.ui.components.proposals.VoteClickListener;
import iop.org.voting_app.ui.components.proposals.VotingProposalsAdapter;
import iop.org.voting_app.ui.dialogs.VoteDialog;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.Vote;
import iop_sdk.governance.vote.VoteWrapper;

import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_PROPOSAL_TRANSACTION_ARRIVED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;

/**
 * Created by mati on 17/11/16.
 */

public class VotingProposalsActivity extends VotingBaseActivity implements VoteClickListener {

    private static final String TAG = "VotingProposalsActivity";

    private View root;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private VotingProposalsAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private View container_empty_screen;

    private List<Proposal> proposals;

    private boolean allProposals;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.voting_proposals_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_show_all){
            allProposals = true;
            load();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        setTitle("Home");

        super.onCreateView(container,savedInstance);

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
        adapter = new VotingProposalsAdapter(this,module,null,this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // activate refresh
                swipeRefreshLayout.setRefreshing(true);
                // load proposals
                load();
            }
        });

    }


    void onItemsLoadComplete(boolean result) {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        if(swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(false);

        if (proposals.isEmpty()){
            showEmptyScreen();
        }
    }

    /**
     * @param data
     */
    public void showVoteDialog(Proposal data) {
        VoteDialog voteDialog = VoteDialog.newInstance(data);
        voteDialog.show(getFragmentManager(),"insuficientFundsDialog");
    }

    @Override
    protected boolean onVotingBroadcastReceive(Bundle data) {
        if (data.getString(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_PROPOSAL_TRANSACTION_ARRIVED)){
            Proposal proposal = (Proposal) data.get(INTENT_EXTRA_PROPOSAL);
            if (proposals==null || proposals.isEmpty()) {
                if (adapter!=null) {
                    if (proposal!=null) {
                        if (!proposal.getTitle().equals("")) {
                            proposals.add(proposal);
                            adapter.changeDataSet(proposals);
                            hideEmptyScreen();
                        } else {
                            Log.e(TAG, "Proposal showed in this screen with null title.. this is a big issue, proposal: " + proposal.toString());
                        }
                    }
                }
            }else {
                if (!proposals.contains(proposal)) {
                    adapter.addItem(proposal);
                }
            }
            return true;
        }

        return false;
    }

    @Override
    protected void onResume() {
        executor.execute(loadProposals);
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void load(){
        // Refresh items
        executor.submit(loadProposals);
    }

    Runnable loadProposals = new Runnable() {
        @Override
        public void run() {
            try {
                proposals = (allProposals)? module.getProposals() : module.getActiveLoadedProposals();
                Log.d(TAG,"Proposals loaded: "+proposals.size());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (proposals!=null && !proposals.isEmpty()) {
                            hideEmptyScreen();
                            adapter.changeDataSet(proposals);
                        } else
                            showEmptyScreen();
                        onItemsLoadComplete(true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                onItemsLoadComplete(false);
            }
        }
    };


    private void showEmptyScreen(){
        if (container_empty_screen!=null)
            AnimationUtils.fadeInView(container_empty_screen,300);
    }

    private void hideEmptyScreen() {
        if (container_empty_screen!=null)
            AnimationUtils.fadeOutView(container_empty_screen,300);
    }


    @Override
    public void goVote(Proposal data, int pos) {
        Vote vote = null;
        if ((vote = module.getVote(data.getGenesisTxHash()))!=null){
            Intent intent = new Intent(this,VotingVoteSummary.class);
            intent.putExtra(VotingVoteSummary.INTENT_VOTE_WRAPPER,new VoteWrapper(vote,data));
            intent.putExtra(VotingVoteSummary.INTENT_CHANGE_VOTE,true);
            startActivity(intent);
        }else {
            Intent intent = new Intent(this, VotingProposalActivity.class);
            intent.putExtra(INTENT_EXTRA_PROPOSAL, data);
            startActivity(intent);
        }
    }
}
