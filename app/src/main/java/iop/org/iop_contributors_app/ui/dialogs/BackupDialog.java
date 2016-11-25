package iop.org.iop_contributors_app.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 22/11/16.
 */

public class BackupDialog extends DialogFragment{

    private Activity activity;

    public static BackupDialog factory(Activity activity) {
        BackupDialog backupDialog = new BackupDialog();
        backupDialog.setActivity(activity);
        return backupDialog;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View view = LayoutInflater.from(activity).inflate(R.layout.backup_wallet_dialog, null);


        final DialogBuilder builder = new DialogBuilder(activity);
//        builder.setTitle(R.string.export_keys_dialog_title);
        builder.setView(view);
        builder.setPositiveButton("ok", null); // dummy, just to make it show
        builder.setNegativeButton("cancel", null);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        return dialog;
    }
}
