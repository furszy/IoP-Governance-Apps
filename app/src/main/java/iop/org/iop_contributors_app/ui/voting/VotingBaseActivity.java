package iop.org.iop_contributors_app.ui.voting;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.furszy_sdk.android.adapter.FermatListItemListeners;
import iop.org.iop_contributors_app.furszy_sdk.android.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.ui.CreateProposalActivity;
import iop.org.iop_contributors_app.ui.ForumActivity;
import iop.org.iop_contributors_app.ui.ProposalsActivity;
import iop.org.iop_contributors_app.ui.SettingsActivity;
import iop.org.iop_contributors_app.ui.base.BaseActivity;

/**
 * Created by mati on 21/12/16.
 */

public abstract class VotingBaseActivity extends BaseActivity {


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
                    intent = new Intent(VotingBaseActivity.this, CreateProposalActivity.class);
                    break;
                case MENU_DRAWER_PROPOSALS:
                    intent = new Intent(VotingBaseActivity.this, ProposalsActivity.class);
                    break;
                case MENU_DRAWER_SETTINGS:
                    intent = new Intent(VotingBaseActivity.this, SettingsActivity.class);
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

    protected void onNavViewCreated(){
        setNavViewHeaderBackground(R.drawable.img_test);
        setNavMenuListener(listener);
    }


    protected List<NavMenuItem> loadNavMenuItems() {
        List<NavMenuItem> items = new ArrayList<>();
//        items.add(new NavMenuItem(MENU_DRAWER_HOME,true,"Home",R.drawable.icon_home_on));
        items.add(new NavMenuItem(MENU_DRAWER_CREATE_PROPOSAL,false,"Vote",R.drawable.icon_createcontributioncontract_off_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_FORUM,false,"Forum",R.drawable.icon_forum_off));
        items.add(new NavMenuItem(MENU_DRAWER_PROPOSALS,true,"My Votes", R.drawable.icon_mycontracts_off_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_SETTINGS,false,"Settings",R.drawable.icon_settings_off));
        return items;
    }


}
