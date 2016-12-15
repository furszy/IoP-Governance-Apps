package iop.org.iop_contributors_app.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.furszy_sdk.android.mine.AnimationUtils;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.components.ProposalsAdapter;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 17/11/16.
 */

public class ProposalsActivity extends BaseActivity {


    private WalletModule module;

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

        root = getLayoutInflater().inflate(R.layout.proposals_main,container);

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
        AnimationUtils.fadeInView(container_empty_screen,300);
    }

    private void hideEmptyScreen(){
        AnimationUtils.fadeOutView(container_empty_screen,300);
    }
}
