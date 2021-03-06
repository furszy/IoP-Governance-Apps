package iop.org.voting_app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.settings.fragments.DevSettingsFragment;

/**
 * Created by mati on 09/12/16.
 */

public class DevActivity extends BaseActivity {


    private Fragment devSettingsFragment;


    @Override
    protected boolean hasDrawer() {
        return false;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        setTitle("Settings");


        setTheme(R.style.PreferenceScreen1);

        devSettingsFragment = DevSettingsFragment.newInstance(module);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(container.getId(),devSettingsFragment)
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
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
