package iop.org.iop_contributors_app.ui;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.furszy_sdk.android.adapter.FermatListItemListeners;
import iop.org.iop_contributors_app.furszy_sdk.android.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.ui.base.BaseActivity;

/**
 * Created by mati on 21/12/16.
 */

public abstract class ContributorBaseActivity extends BaseActivity {


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
    protected void onNavViewCreated() {
        super.onNavViewCreated();
        setNavMenuListener(listener);
    }

    protected List<NavMenuItem> loadNavMenuItems() {
        List<NavMenuItem> items = new ArrayList<>();
//        items.add(new NavMenuItem(MENU_DRAWER_HOME,true,"Home",R.drawable.icon_home_on));
        items.add(new NavMenuItem(MENU_DRAWER_PROPOSALS,true,"Proposals", R.drawable.icon_mycontracts_off_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_FORUM,false,"Forum",R.drawable.icon_forum_off));
        items.add(new NavMenuItem(MENU_DRAWER_CREATE_PROPOSAL,false,"Create Proposal",R.drawable.icon_createcontributioncontract_off_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_SETTINGS,false,"Settings",R.drawable.icon_settings_off));
        return items;
    }


}
