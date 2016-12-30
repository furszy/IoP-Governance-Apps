package iop.org.voting_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import iop.org.furszy_lib.adapter.FermatListItemListeners;
import iop.org.iop_contributors_app.R;
import iop.org.voting_app.base.VotingBaseActivity;
import iop.org.voting_app.ui.components.my_votes.MyVotesAdapter;
import iop.org.voting_app.ui.dialogs.VoteDialog;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.VoteWrapper;

/**
 * Created by mati on 17/11/16.
 */

public class VotingMyVotesActivity extends VotingBaseActivity implements FermatListItemListeners<VoteWrapper> {



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
        // todo: adapter
        adapter = new MyVotesAdapter(this,module);
        recyclerView.setAdapter(adapter);
        adapter.setFermatListEventListener(this);

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


    @Override
    public void onItemClickListener(VoteWrapper data, int position) {
//        Intent intent = new Intent(this,VotingVoteSummary.class);
//        startActivity(intent);

        Intent intent = new Intent(this,VotingVoteSummary.class);
        intent.putExtra(VotingVoteSummary.INTENT_VOTE_WRAPPER,data);
        String transitionName = getString(R.string.transition_card);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        recyclerView.getChildAt(position),//albumCoverImageView,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning to
                );
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public void onLongItemClickListener(VoteWrapper data, int position) {

    }
}
