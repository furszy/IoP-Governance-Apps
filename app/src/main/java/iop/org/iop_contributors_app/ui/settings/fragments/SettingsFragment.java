package iop.org.iop_contributors_app.ui.settings.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.OnboardingWithCenterAnimationActivity;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.ui.dialogs.ReportIssueDialogBuilder;
import iop.org.iop_contributors_app.ui.settings.DevActivity;
import iop.org.iop_contributors_app.ui.settings.IoPBalanceActivity;
import iop.org.iop_contributors_app.utils.CrashReporter;
import iop.org.iop_contributors_app.wallet.WalletConstants;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static android.widget.Toast.*;

/**
 * Created by mati on 22/11/16.
 */

public class SettingsFragment extends PreferenceFragment {

    private WalletModule module;
    private View root;

    public static SettingsFragment newInstance(WalletModule module) {
        SettingsFragment fragment = new SettingsFragment();
        fragment.setModule(module);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        addPreferencesFromResource(R.xml.settings);

        root = super.onCreateView(inflater, container, savedInstanceState);
        root.setPadding(0,16,0,0);
        root.setBackgroundColor(Color.parseColor("#1A1A1A"));

        ListView list = (ListView) root.findViewById(android.R.id.list);
//        list.setDivider(getResources().getDrawable(R.drawable.settings_divider,null)); // or some other color int
        list.setDivider(new ColorDrawable(Color.WHITE));
        list.setDividerHeight((int) 2);


//        ListView list = (ListView) root.findViewById(android.R.id.list);
//        list.setDivider(null);

        setHasOptionsMenu(false);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals(getString(R.string.id_balance))){
            startActivity(new Intent(getActivity(), IoPBalanceActivity.class));
        }else if (preference.getKey().equals("id_report")){
            handleReportIssue();
        } else if(preference.getKey().equals("id_dev")){
            startActivity(new Intent(getActivity(), DevActivity.class));
        } else if (preference.getKey().equals("id_profile")){
            final DialogBuilder dialogBuilder = new DialogBuilder(getActivity());
            dialogBuilder.setMessage("You are going to remove everything in the app\nAre you sure?");
            dialogBuilder.setTitle("Remove User");
            dialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    module.cleanEverything();
                    startActivity(new Intent(getActivity(), OnboardingWithCenterAnimationActivity.class));
                }
            });
            dialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialogBuilder.show();

        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }



    private void handleReportIssue()
    {
        final ReportIssueDialogBuilder dialog = new ReportIssueDialogBuilder(getActivity(), R.string.report_issue_dialog_title_issue,
                R.string.report_issue_dialog_message_issue)
        {
            @Override
            protected CharSequence subject()
            {
                return WalletConstants.REPORT_SUBJECT_ISSUE + " " + ApplicationController.getInstance().packageInfo().versionName;
            }

            @Override
            protected CharSequence collectApplicationInfo() throws IOException
            {
                final StringBuilder applicationInfo = new StringBuilder();
                CrashReporter.appendApplicationInfo(applicationInfo, ApplicationController.getInstance());
                return applicationInfo;
            }

            @Override
            protected CharSequence collectStackTrace()
            {
                return null;
            }

            @Override
            protected CharSequence collectDeviceInfo() throws IOException
            {
                final StringBuilder deviceInfo = new StringBuilder();
                CrashReporter.appendDeviceInfo(deviceInfo, getActivity());
                return deviceInfo;
            }

            @Override
            protected CharSequence collectWalletDump()
            {
                return ApplicationController.getInstance().getWalletModule().getWalletManager().getWallet().toString(false, true, true, null);
            }
        };

        dialog.show();
    }

    public void setModule(WalletModule module) {
        this.module = module;
    }
}
