package iop.org.iop_contributors_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import iop.org.furszy_lib.adapter.FermatListItemListeners;
import iop.org.furszy_lib.base.NavViewHelper;
import iop.org.furszy_lib.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.components.NavViewAdapter;
import iop.org.iop_contributors_app.ui.dialogs.wallet.BackupDialog;
import iop.org.iop_contributors_app.ui.dialogs.wallet.RestoreDialogFragment2;
import iop.org.iop_sdk_android.core.wrappers.IntentWrapperAndroid;
import iop_sdk.global.IntentWrapper;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.Vote;

import static org.iop.intents.constants.IntentsConstants.ACTION_NOTIFICATION;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_PROPOSAL_FROZEN_FUNDS_UNLOCKED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_VOTE_FROZEN_FUNDS_UNLOCKED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_EXTRA_DATA_VOTE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_DATA;
import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;
import static org.iop.intents.constants.IntentsConstants.INTENT_NOTIFICATION;

/**
 * Created by mati on 21/12/16.
 */

public abstract class ContributorBaseActivity extends BaseActivity {

    private static final String TAG = "ContributorBaseActivity";

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
                    intent = new Intent(ContributorBaseActivity.this, ForumActivity.class);
                    break;
                case MENU_DRAWER_CREATE_PROPOSAL:
                    intent = new Intent(ContributorBaseActivity.this, CreateProposalActivity.class);
                    break;
                case MENU_DRAWER_PROPOSALS:
                    intent = new Intent(ContributorBaseActivity.this, ProposalsActivity.class);
                    break;
                case MENU_DRAWER_SETTINGS:
                    intent = new Intent(ContributorBaseActivity.this, SettingsActivity.class);
                    break;
            }
            startActivity(intent);
        }

        @Override
        public void onLongItemClickListener(NavMenuItem data, int position) {

        }
    };

    protected void beforeCreate(Bundle savedInstanceState){
    }

    @Override
    protected void onNavViewCreated(NavViewHelper navViewHelper) {
        navViewHelper.setNavViewAdapter(new NavViewAdapter(this,loadNavMenuItems()));
        navViewHelper.setNavMenuListener(listener);
    }

    protected List<NavMenuItem> loadNavMenuItems() {
        List<NavMenuItem> items = new ArrayList<>();
//        items.put(new NavMenuItem(MENU_DRAWER_HOME,true,"Home",R.drawable.icon_home_on));
        items.add(new NavMenuItem(MENU_DRAWER_PROPOSALS,true,"Proposals", R.drawable.icon_mycontracts_off_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_FORUM,false,"Forum",R.drawable.icon_forum_off));
        items.add(new NavMenuItem(MENU_DRAWER_CREATE_PROPOSAL,false,"Create Proposal",R.drawable.icon_createcontributioncontract_off_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_SETTINGS,false,"Settings",R.drawable.icon_settings_off));
        return items;
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
            if (data.get(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_PROPOSAL_FROZEN_FUNDS_UNLOCKED)){
                Proposal proposal = (Proposal) data.getSerializable(INTENT_EXTRA_PROPOSAL);
                notifyProposalAndFundsUnlocked(proposal);
            }
        }
        return onContributorsBroadcastReceive(data);
    }

    protected boolean onContributorsBroadcastReceive(Bundle data) {
        return false;
    }

    /**
     * se notifica que la propuesta terminó o fue cancelada y que los fondos freezados fueron desbloqueados
     * @param
     */
    private void notifyProposalAndFundsUnlocked(Proposal proposal){
        Log.i(TAG,"notifyProposalAndFundsUnlocked, for proposal: "+proposal.toString());
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic__launcher)
                        .setContentTitle("Proposal finished: "+proposal.getTitle())
                        .setContentText("State: "+proposal.getState()+"\nYour frozen power are unloked");

        notificationManager.notify(10,mBuilder.build());
    }
}
