package iop.org.iop_contributors_app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.ViewGroup;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;

/**
 * Created by mati on 22/11/16.
 */

public class SettingsActivity  extends BaseActivity {


    private Fragment settingsFragment;

    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {
        super.onCreateView(container, savedInstance);
            settingsFragment = new SettingsFragment();
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
    protected boolean onBroadcastReceive(String action, Bundle data) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
