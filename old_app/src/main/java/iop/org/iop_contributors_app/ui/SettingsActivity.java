package iop.org.iop_contributors_app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.settings.fragments.SettingsFragment;

/**
 * Created by mati on 22/11/16.
 */

public class SettingsActivity  extends ContributorBaseActivity {


    private Fragment settingsFragment;

    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        setTitle("Settings");

        setTheme(R.style.PreferenceScreen1);

        settingsFragment = SettingsFragment.newInstance(application,module);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(container.getId(),settingsFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected boolean hasOptionMenu(){
        return false;
    }

    @Override
    protected boolean onContributorsBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    @Override
//    protected void onActionDrawerClicked() {
//        finish();
//        super.onActionDrawerClicked();
//    }
}
