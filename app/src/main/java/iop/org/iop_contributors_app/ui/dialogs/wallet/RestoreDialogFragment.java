package iop.org.iop_contributors_app.ui.dialogs.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Charsets;

import org.bitcoinj.wallet.Wallet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.dialogs.Crypto;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.ui.dialogs.FileAdapter;
import iop.org.iop_contributors_app.ui.dialogs.ShowPasswordCheckListener;
import iop.org.iop_contributors_app.ui.dialogs.WalletUtils;
import iop.org.iop_contributors_app.utils.Io;
import iop.org.iop_contributors_app.wallet.WalletConstants;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 28/11/16.
 */

public class RestoreDialogFragment extends DialogFragment {

    private static final String TAG = "RestoreDialogFragment";

    private static final int DIALOG_RESTORE_WALLET = 0;

    private WalletModule module;
    private Activity activity;


    public static RestoreDialogFragment factory(Activity activity) {
        RestoreDialogFragment restoreDialogFragment = new RestoreDialogFragment();
        restoreDialogFragment.setActivity(activity);
        return restoreDialogFragment;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Dialog onCreateDialog(Bundle bundle) {

        module = ApplicationController.getInstance().getWalletModule();

        final View view = LayoutInflater.from(activity).inflate(R.layout.restore_dialog, null);
        final TextView messageView = (TextView) view.findViewById(R.id.restore_wallet_dialog_message);
        final Spinner fileView = (Spinner) view.findViewById(R.id.import_keys_from_storage_file);
        final EditText passwordView = (EditText) view.findViewById(R.id.import_keys_from_storage_password);

        final DialogBuilder dialog = new DialogBuilder(activity);
        dialog.setTitle(R.string.import_keys_dialog_title);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.import_keys_dialog_button_import, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int which)
            {
                final File file = (File) fileView.getSelectedItem();
                final String password = passwordView.getText().toString().trim();
                passwordView.setText(null); // get rid of it asap

                if (WalletUtils.BACKUP_FILE_FILTER.accept(file))
                    module.restoreWalletFromProtobuf(file);
                else if (WalletUtils.KEYS_FILE_FILTER.accept(file))
                    module.restorePrivateKeysFromBase58(file);
                else if (Crypto.OPENSSL_FILE_FILTER.accept(file))
                    module.restoreWalletFromEncrypted(file, password);
            }
        });
        dialog.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int which)
            {
                passwordView.setText(null); // get rid of it asap
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(final DialogInterface dialog)
            {
                passwordView.setText(null); // get rid of it asap
            }
        });

        final FileAdapter adapter = new FileAdapter(activity)
        {
            @Override
            public View getDropDownView(final int position, View row, final ViewGroup parent)
            {
                final File file = getItem(position);
                final boolean isExternal = WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.equals(file.getParentFile());
                final boolean isEncrypted = Crypto.OPENSSL_FILE_FILTER.accept(file);

                if (row == null)
                    row = inflater.inflate(R.layout.restore_wallet_file_row, null);

                final TextView filenameView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_filename);
                filenameView.setText(file.getName());

                final TextView securityView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_security);
                final String encryptedStr = context.getString(isEncrypted ? R.string.import_keys_dialog_file_security_encrypted
                        : R.string.import_keys_dialog_file_security_unencrypted);
                final String storageStr = context.getString(isExternal ? R.string.import_keys_dialog_file_security_external
                        : R.string.import_keys_dialog_file_security_internal);
                securityView.setText(encryptedStr + ", " + storageStr);

                final TextView createdView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_created);
                createdView
                        .setText(context.getString(isExternal ? R.string.import_keys_dialog_file_created_manual
                                : R.string.import_keys_dialog_file_created_automatic, DateUtils.getRelativeTimeSpanString(context,
                                file.lastModified(), true)));

                return row;
            }
        };

        final String path;
        final String backupPath = WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.getAbsolutePath();
        final String storagePath = WalletConstants.Files.EXTERNAL_STORAGE_DIR.getAbsolutePath();
        if (backupPath.startsWith(storagePath))
            path = backupPath.substring(storagePath.length());
        else
            path = backupPath;
        messageView.setText(getString(R.string.import_keys_dialog_message, path));

        fileView.setAdapter(adapter);

        Dialog resultDialog = dialog.create();
//        prepareRestoreWalletDialog(resultDialog,view);
        return resultDialog;
    }


    private void prepareRestoreWalletDialog(final Dialog dialog, View root)
    {
        final AlertDialog alertDialog = (AlertDialog) dialog;

        final List<File> files = new LinkedList<File>();

        // external storage
        if (WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.exists() && WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.isDirectory())
            for (final File file : WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.listFiles())
                if (Crypto.OPENSSL_FILE_FILTER.accept(file))
                    files.add(file);

        // internal storage
        for (final String filename : activity.fileList())
            if (filename.startsWith(WalletConstants.Files.WALLET_KEY_BACKUP_PROTOBUF + '.'))
                files.add(new File(activity.getFilesDir(), filename));

        // sort
        Collections.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(final File lhs, final File rhs)
            {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        final View replaceWarningView = root.findViewById(R.id.restore_wallet_from_storage_dialog_replace_warning);
        final boolean hasCoins = module.getWalletManager().getWallet().getBalance(Wallet.BalanceType.ESTIMATED).signum() > 0;
        replaceWarningView.setVisibility(hasCoins ? View.VISIBLE : View.GONE);

        final Spinner fileView = (Spinner) root.findViewById(R.id.import_keys_from_storage_file);
        final FileAdapter adapter = (FileAdapter) fileView.getAdapter();
        adapter.setFiles(files);
        fileView.setEnabled(!adapter.isEmpty());

        final EditText passwordView = (EditText) root.findViewById(R.id.import_keys_from_storage_password);
        passwordView.setText(null);

        final ImportDialogButtonEnablerListener dialogButtonEnabler = new ImportDialogButtonEnablerListener(passwordView, alertDialog)
        {
            @Override
            protected boolean hasFile()
            {
                return fileView.getSelectedItem() != null;
            }

            @Override
            protected boolean needsPassword()
            {
                final File selectedFile = (File) fileView.getSelectedItem();
                return selectedFile != null ? Crypto.OPENSSL_FILE_FILTER.accept(selectedFile) : false;
            }
        };
        passwordView.addTextChangedListener(dialogButtonEnabler);
        fileView.setOnItemSelectedListener(dialogButtonEnabler);

        final CheckBox showView = (CheckBox) root.findViewById(R.id.import_keys_from_storage_show);
        showView.setOnCheckedChangeListener(new ShowPasswordCheckListener(passwordView));
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareRestoreWalletDialog(this.getDialog(),getDialog().findViewById(R.id.dialog_container));
    }


    private void restoreWalletFromEncrypted(final InputStream cipher, final String password)
    {
        try
        {
            final BufferedReader cipherIn = new BufferedReader(new InputStreamReader(cipher, Charsets.UTF_8));
            final StringBuilder cipherText = new StringBuilder();
            Io.copy(cipherIn, cipherText, WalletConstants.BACKUP_MAX_CHARS);
            cipherIn.close();

            final byte[] plainText = Crypto.decryptBytes(cipherText.toString(), password.toCharArray());
            final InputStream is = new ByteArrayInputStream(plainText);

            restoreWallet(WalletUtils.restoreWalletFromProtobufOrBase58(is, WalletConstants.NETWORK_PARAMETERS));

            Log.i(TAG,"successfully restored encrypted wallet from external source");
        }
        catch (final IOException x)
        {
            final DialogBuilder dialog = DialogBuilder.warn(activity, R.string.import_export_keys_dialog_failure_title);
            dialog.setMessage(getString(R.string.import_keys_dialog_failure, x.getMessage()));
            dialog.setPositiveButton(R.string.button_dismiss, finishListener).setOnCancelListener(finishListener);
            dialog.setNegativeButton(R.string.button_retry, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    activity.showDialog(DIALOG_RESTORE_WALLET);
                }
            });
            dialog.show();

            Log.d(TAG,"problem restoring wallet", x);
        }
    }

    private void restoreWallet(final Wallet wallet) throws IOException
    {

        module.getWalletManager().replaceWallet(wallet);

        //todo: esto es un recordatorio para que haga e backup
//        config.disarmBackupReminder();

        final DialogBuilder dialog = new DialogBuilder(activity);
        final StringBuilder message = new StringBuilder();
        message.append(getString(R.string.restore_wallet_dialog_success));
        message.append("\n\n");
        message.append(getString(R.string.restore_wallet_dialog_success_replay));
        dialog.setMessage(message);
        dialog.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int id)
            {
                module.getWalletManager().resetBlockchain();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener
    {
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            dialog.dismiss();
        }

        @Override
        public void onCancel(final DialogInterface dialog)
        {
            dialog.dismiss();
        }
    }

    private final FinishListener finishListener = new FinishListener();
}
