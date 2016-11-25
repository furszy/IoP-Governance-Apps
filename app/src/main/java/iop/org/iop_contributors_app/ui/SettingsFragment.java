package iop.org.iop_contributors_app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.dialogs.BackupDialog;

import static android.widget.Toast.*;

/**
 * Created by mati on 22/11/16.
 */

public class SettingsFragment extends PreferenceFragment {

    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.settings);

        root = super.onCreateView(inflater, container, savedInstanceState);
        root.setPadding(0,16,0,0);
        root.setBackgroundColor(Color.parseColor("#1A1A1A"));

        setHasOptionsMenu(false);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals(getString(R.string.preference_backup_key))){
            Toast.makeText(getActivity(),"nada",LENGTH_LONG).show();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
