package iop.org.iop_contributors_app.ui.voting;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.ui.CreateProposalActivity;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.components.ProposalsAdapter;
import iop.org.iop_contributors_app.ui.settings.fragments.SettingsFragment;
import iop.org.iop_contributors_app.ui.voting.ui.components.VotingProposalsAdapter;
import iop.org.iop_contributors_app.ui.voting.ui.dialogs.VoteDialog;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 17/11/16.
 */

public class VotingProposalsActivity extends BaseActivity {


    private WalletModule module;

    private View root;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private VotingProposalsAdapter adapter;

    private View container_empty_screen;

    private List<Proposal> proposals;


    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        root = getLayoutInflater().inflate(R.layout.proposals_voting_main,container);

        module = ApplicationController.getInstance().getWalletModule();

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

    }

    /**
     * todo: esto no va así, va con el show del dialog...
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
            proposals = module.getProposals();
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
        container_empty_screen.setVisibility(View.VISIBLE);
    }

    private void hideEmptyScreen(){
        container_empty_screen.setVisibility(View.INVISIBLE);
    }


}