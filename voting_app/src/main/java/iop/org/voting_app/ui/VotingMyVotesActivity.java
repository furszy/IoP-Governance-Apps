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
import iop.org.furszy_lib.utils.AnimationUtils;
import iop.org.voting_app.R;
import iop.org.voting_app.base.VotingBaseActivity;
import iop.org.voting_app.ui.components.my_votes.MyVotesAdapter;
import iop.org.voting_app.ui.components.my_votes.MyVotesHolder;
import iop.org.voting_app.ui.dialogs.VoteDialog;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.VoteWrapper;

import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;

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

        setTitle("My Votes");

        super.onCreateView(container,savedInstance);

        root = getLayoutInflater().inflate(R.layout.my_votes_main,container);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_proposals);
        container_empty_screen = root.findViewById(R.id.container_empty_screen);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyVotesAdapter(this,module);
        recyclerView.setAdapter(adapter);

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
    protected boolean onVotingBroadcastReceive(Bundle data) {
        if (data.containsKey(INTENT_EXTRA_PROPOSAL)) {
            Proposal proposal = (Proposal) data.getSerializable(INTENT_EXTRA_PROPOSAL);
            Proposal temp;
            for (int i=0;i<adapter.getItemCount();i++){
                if ((temp=adapter.getItem(i).getProposal()).getForumId()==proposal.getForumId()){
                    temp.setState(proposal.getState());
                    adapter.notifyItemChanged(i);
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        adapter.setFermatListEventListener(this);
        executor.execute(loadMyVotes);
        super.onResume();
    }


    @Override
    protected void onStop() {
        adapter.clear();
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
        AnimationUtils.fadeInView(container_empty_screen,300);
    }

    private void hideEmptyScreen(){
        AnimationUtils.fadeOutView(container_empty_screen,300);
    }


    @Override
    public void onItemClickListener(VoteWrapper data, int position) {
//        Intent intent = new Intent(this,VotingVoteSummary.class);
//        startActivity(intent);

        Intent intent = new Intent(this,VotingVoteSummary.class);
        intent.putExtra(VotingVoteSummary.INTENT_VOTE_WRAPPER,data);
        String transitionName = getString(R.string.transition_card);
        MyVotesHolder holder = (MyVotesHolder) recyclerView.findViewHolderForAdapterPosition(position);
//        ViewCompat.setTransitionName(view, transitionName);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        holder.card_view,//albumCoverImageView,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning to
                );
        ActivityCompat.startActivity(this, intent, options.toBundle());

    }

    @Override
    public void onLongItemClickListener(VoteWrapper data, int position) {

    }
}
