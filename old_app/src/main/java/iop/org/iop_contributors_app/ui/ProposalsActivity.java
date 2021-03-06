package iop.org.iop_contributors_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import iop.org.furszy_lib.utils.AnimationUtils;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.components.ProposalsAdapter;
import iop_sdk.governance.propose.Proposal;

import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;

/**
 * Created by mati on 17/11/16.
 */

public class ProposalsActivity extends ContributorBaseActivity {

    private View root;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProposalsAdapter adapter;

    private View container_empty_screen;

    private List<Proposal> proposals;


    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        setTitle("My Proposals");

        root = getLayoutInflater().inflate(R.layout.proposals_main,container);

        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_proposals);
        container_empty_screen = root.findViewById(R.id.container_empty_screen);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new ProposalsAdapter(this,module);
        recyclerView.setAdapter(adapter);


        container_empty_screen.findViewById(R.id.show_contract).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(),ForumActivity.class));
            }
        });

        container_empty_screen.findViewById(R.id.create_contract).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(),CreateProposalActivity.class));
            }
        });


    }

    @Override
    protected boolean onContributorsBroadcastReceive(Bundle data) {
        if (data.containsKey(INTENT_EXTRA_PROPOSAL)){
            Proposal proposal = (Proposal) data.getSerializable(INTENT_EXTRA_PROPOSAL);
            Proposal myProp;
            for (int i=0;i<adapter.getItemCount();i++){
                if ((myProp = adapter.getItem(i)).getForumId()==proposal.getForumId()){
                    myProp.setState(proposal.getState());
                    myProp.setVoteYes(proposal.getVoteYes());
                    myProp.setVoteNo(proposal.getVoteNo());
                    adapter.notifyItemChanged(i);
                }
            }
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

    Runnable loadProposals = new Runnable() {
        @Override
        public void run() {
            proposals = module.getMyProposals();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!proposals.isEmpty()) {
                        hideEmptyScreen();
                        adapter.changeDataSet(proposals);
                    } else
                        showEmptyScreen();
                }
            });
        }
    };


    private void showEmptyScreen(){
        if (container_empty_screen!=null)
            AnimationUtils.fadeInView(container_empty_screen,300);
    }

    private void hideEmptyScreen(){
        if (container_empty_screen!=null)
            AnimationUtils.fadeOutView(container_empty_screen,300);
    }
}
