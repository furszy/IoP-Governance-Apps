package iop.org.voting_app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup;

import iop.org.iop_contributors_app.R;
import iop.org.voting_app.base.VotingBaseActivity;
import iop.org.voting_app.ui.fragments.SettingsFragment;

/**
 * Created by mati on 22/11/16.
 */

public class SettingsActivity  extends VotingBaseActivity {


    private Fragment settingsFragment;

    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

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
    protected boolean onVotingBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}