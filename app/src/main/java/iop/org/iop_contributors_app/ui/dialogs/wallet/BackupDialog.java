package iop.org.iop_contributors_app.ui.dialogs.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.ui.dialogs.Iso8601Format;
import iop.org.iop_contributors_app.ui.dialogs.ShowPasswordCheckListener;
import iop.org.iop_contributors_app.wallet.WalletConstants;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by mati on 22/11/16.
 */

public class BackupDialog extends DialogFragment{

    private static final Logger LOG = LoggerFactory.getLogger(BackupDialog.class);

    private Activity activity;
    private Button positiveButton;
    private EditText passwordView;
    private EditText passwordAgainView;
    private TextView passwordStrengthView;
    private View passwordMismatchView;
    private CheckBox showView;

    private WalletModule module;

    public static BackupDialog factory(Activity activity) {
        BackupDialog backupDialog = new BackupDialog();
        backupDialog.setActivity(activity);
        return backupDialog;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }



    private final TextWatcher textWatcher = new TextWatcher()
    {
        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count)
        {
            passwordMismatchView.setVisibility(View.INVISIBLE);
            updateView();
        }

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after)
        {
        }

        @Override
        public void afterTextChanged(final Editable s)
        {
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        module = ApplicationController.getInstance().getWalletModule();

        final View view = LayoutInflater.from(activity).inflate(R.layout.backup_wallet_dialog, null);

        passwordView = (EditText) view.findViewById(R.id.backup_wallet_dialog_password);
        passwordView.setText(null);

        passwordAgainView = (EditText) view.findViewById(R.id.backup_wallet_dialog_password_again);
        passwordAgainView.setText(null);

        passwordStrengthView = (TextView) view.findViewById(R.id.backup_wallet_dialog_password_strength);

        passwordMismatchView = view.findViewById(R.id.backup_wallet_dialog_password_mismatch);

        showView = (CheckBox) view.findViewById(R.id.backup_wallet_dialog_show);

        final TextView warningView = (TextView) view.findViewById(R.id.backup_wallet_dialog_warning_encrypted);
        warningView.setVisibility(module.isWalletEncrypted() ? View.VISIBLE : View.GONE);


        final DialogBuilder builder = new DialogBuilder(activity);
//        builder.setTitle(R.string.export_keys_dialog_title);
        builder.setView(view);
        builder.setPositiveButton("ok", null); // dummy, just to make it show
        builder.setNegativeButton("cancel", null);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface d) {
                positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTypeface(Typeface.DEFAULT_BOLD);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        handleGo();
                    }
                });

                passwordView.addTextChangedListener(textWatcher);
                passwordAgainView.addTextChangedListener(textWatcher);

                showView.setOnCheckedChangeListener(new ShowPasswordCheckListener(passwordView, passwordAgainView));

                //BackupWalletDialogFragment.this.dialog = dialog;
                updateView();
            }
        });

        return dialog;
    }

    @Override
    public void onDismiss(final DialogInterface dialog)
    {
//        this.dialog = null;

        passwordView.removeTextChangedListener(textWatcher);
        passwordAgainView.removeTextChangedListener(textWatcher);

        showView.setOnCheckedChangeListener(null);

        wipePasswords();

        super.onDismiss(dialog);
    }

    private void handleGo()
    {
        final String password = passwordView.getText().toString().trim();
        final String passwordAgain = passwordAgainView.getText().toString().trim();

        if (passwordAgain.equals(password))
        {
            passwordView.setText(null); // get rid of it asap
            passwordAgainView.setText(null);

            backupWallet(password);

            dismiss();
            //todo: esto es un recordatorio para que se acuerde de backupearla..
            //application.getConfiguration().disarmBackupReminder();
        }
        else
        {
            passwordMismatchView.setVisibility(View.VISIBLE);
        }
    }


    private void wipePasswords()
    {
        passwordView.setText(null);
    }

    private void updateView()
    {
//        if (dialog == null)
//            return;

        final int passwordLength = passwordView.getText().length();
        passwordStrengthView.setVisibility(passwordLength > 0 ? View.VISIBLE : View.INVISIBLE);
        if (passwordLength < 6)
        {
            passwordStrengthView.setText(R.string.encrypt_keys_dialog_password_strength_weak);
            passwordStrengthView.setTextColor(getResources().getColor(R.color.fg_password_strength_weak));
        }
        else if (passwordLength < 8)
        {
            passwordStrengthView.setText(R.string.encrypt_keys_dialog_password_strength_fair);
            passwordStrengthView.setTextColor(getResources().getColor(R.color.fg_password_strength_fair));
        }
        else if (passwordLength < 10)
        {
            passwordStrengthView.setText(R.string.encrypt_keys_dialog_password_strength_good);
            passwordStrengthView.setTextColor(getResources().getColor(R.color.fg_less_significant));
        }
        else
        {
            passwordStrengthView.setText(R.string.encrypt_keys_dialog_password_strength_strong);
            passwordStrengthView.setTextColor(getResources().getColor(R.color.fg_password_strength_strong));
        }

        final boolean hasPassword = !passwordView.getText().toString().trim().isEmpty();
        final boolean hasPasswordAgain = !passwordAgainView.getText().toString().trim().isEmpty();

        positiveButton.setEnabled(hasPassword && hasPasswordAgain);
    }

    private void backupWallet(final String password) {
        final File file = determineBackupFile();

        try {
            module.backupWallet(file,password);
            LOG.info("backed up wallet to: '" + file + "'");
            //todo: esto es el dialog de "succed"
            ArchiveBackupDialogFragment.show(activity,getFragmentManager(), file);
        }
        catch (final IOException x) {
            final DialogBuilder dialog = DialogBuilder.warn(activity, R.string.import_export_keys_dialog_failure_title);
            dialog.setMessage(getString(R.string.export_keys_dialog_failure, x.getMessage()));
            dialog.singleDismissButton(null);
            dialog.show();

            LOG.error("problem backing up wallet", x);
        }

    }

    private File determineBackupFile()
    {
        WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.mkdirs();
        checkState(WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.isDirectory(), "%s is not a directory", WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR);

        final DateFormat dateFormat = Iso8601Format.newDateFormat();
        dateFormat.setTimeZone(TimeZone.getDefault());

        for (int i = 0; true; i++)
        {
            final StringBuilder filename = new StringBuilder(WalletConstants.Files.EXTERNAL_WALLET_BACKUP);
            filename.append('-');
            filename.append(dateFormat.format(new Date()));
            if (i > 0)
                filename.append(" (").append(i).append(')');

            final File file = new File(WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR, filename.toString());
            if (!file.exists())
                return file;
        }
    }

}
