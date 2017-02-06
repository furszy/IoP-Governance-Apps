package iop.org.voting_app.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import iop.org.furszy_lib.adapter.FermatListItemListeners;
import iop.org.furszy_lib.base.NavViewHelper;
import iop.org.furszy_lib.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.wallet.BackupDialog;
import iop.org.iop_contributors_app.ui.dialogs.wallet.RestoreDialogFragment2;
import iop.org.voting_app.SettingsActivity;
import iop.org.voting_app.ui.ForumActivity;
import iop.org.voting_app.ui.VotingMyVotesActivity;
import iop.org.voting_app.ui.VotingProposalsActivity;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.Vote;

import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_VOTE_FROZEN_FUNDS_UNLOCKED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_EXTRA_DATA_VOTE;
import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;

/**
 * Created by mati on 21/12/16.
 */

public abstract class VotingBaseActivity extends BaseActivity {

    private static final String TAG = "VotingBaseActivity";


    private final static int MENU_DRAWER_HOME = 0;
    private final static int MENU_DRAWER_FORUM = 1;
    private final static int MENU_DRAWER_CREATE_PROPOSAL = 2;
    private final static int MENU_DRAWER_PROPOSALS = 3;
    private final static int MENU_DRAWER_SETTINGS = 4;

    private FermatListItemListeners<NavMenuItem> listener = new FermatListItemListeners<NavMenuItem>() {
        @Override
        public void onItemClickListener(NavMenuItem data, int position) {
            int id = data.getId();
            Intent intent = null;
            switch (id){
//                    case MENU_DRAWER_HOME:
//                        intent = new Intent(BaseActivity.this, MainActivity.class);
//                        break;
                case MENU_DRAWER_FORUM:
                    intent = new Intent(VotingBaseActivity.this, ForumActivity.class);
                    break;
                case MENU_DRAWER_CREATE_PROPOSAL:
                    intent = new Intent(VotingBaseActivity.this, VotingProposalsActivity.class);
                    break;
                case MENU_DRAWER_PROPOSALS:
                    intent = new Intent(VotingBaseActivity.this, VotingMyVotesActivity.class);
                    break;
                case MENU_DRAWER_SETTINGS:
                    intent = new Intent(VotingBaseActivity.this, SettingsActivity.class);
                    break;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
            startActivity(intent);
        }

        @Override
        public void onLongItemClickListener(NavMenuItem data, int position) {

        }
    };

    protected void beforeCreate(Bundle savedInstanceState){
    }

    protected void onNavViewCreated(NavViewHelper navViewHelper){
        navViewHelper.setNavViewAdapter(new VotingNavViewAdapter(this,loadNavMenuItems()));
        navViewHelper.setHeaderViewBackground(R.drawable.banner);
        navViewHelper.setNavMenuListener(listener);
        navViewHelper.setNavViewBackgroundColor(Color.WHITE);
    }


    protected List<NavMenuItem> loadNavMenuItems() {
        List<NavMenuItem> items = new ArrayList<>();
//        items.put(new NavMenuItem(MENU_DRAWER_HOME,true,"Home",R.drawable.icon_home_on));
        items.add(new NavMenuItem(MENU_DRAWER_CREATE_PROPOSAL,false,"Home",R.drawable.ic_home_drawer,R.drawable.on_ic_home_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_FORUM,false,"Forum",R.drawable.ic_forum_drawer,R.drawable.on_ic_forum_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_PROPOSALS,true,"My Votes", R.drawable.ic_votes_drawer,R.drawable.on_ic_votes_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_SETTINGS,false,"Settings",R.drawable.ic_settings_drawer,R.drawable.on_ic_settings_drawer));
        return items;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {
//        container.setBackgroundColor(Color.WHITE);
        toolbar.setBackgroundColor(Color.parseColor("#171519"));
        container.setBackgroundColor(Color.WHITE);
        super.onCreateView(container, savedInstance);
    }

    protected int appColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(R.color.purple,null);
        }else
            return getResources().getColor(R.color.purple);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (hasOptionMenu()) {

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_backup) {
            BackupDialog backupDialog = BackupDialog.factory(this,module);
            backupDialog.show(getFragmentManager(), "backup_dialog");
            return true;
        } else if (i == R.id.action_restore) {
            RestoreDialogFragment2 restoreDialogFragment = RestoreDialogFragment2.newInstance(module);
            restoreDialogFragment.show(getFragmentManager(), "restore_dialog");
            return true;
        } else {// If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {

        if (data.containsKey(INTENT_BROADCAST_DATA_TYPE)){
            if (data.get(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_VOTE_FROZEN_FUNDS_UNLOCKED)){
                Vote vote = (Vote) data.getSerializable(INTENT_BROADCAST_EXTRA_DATA_VOTE);
                Proposal proposal = (Proposal) data.getSerializable(INTENT_EXTRA_PROPOSAL);
                notifyProposalAndFundsUnlocked(proposal,vote);
            }
        }
        return onVotingBroadcastReceive(data);
    }

    protected boolean onVotingBroadcastReceive(Bundle data){
        return false;
    }

    /**
     * se notifica que la propuesta terminó o fue cancelada y que los fondos freezados fueron desbloqueados
     * @param vote
     */
    private void notifyProposalAndFundsUnlocked(Proposal proposal, Vote vote){
        Log.i(TAG,"notifyProposalAndFundsUnlocked, for vote: "+vote.toString());
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic__launcher)
                        .setContentTitle("Proposal with id: "+proposal.getForumId()+" finished")
                        .setContentText("State: "+proposal.getState()+"\nFrozen votes unloked");

        notificationManager.notify(10,mBuilder.build());
    }
}
