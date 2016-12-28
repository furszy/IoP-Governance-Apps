package iop.org.iop_contributors_app.ui.dialogs.wallet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.Charsets;
import org.bitcoinj.wallet.Wallet;
import org.iop.WalletConstants;
import org.iop.WalletModule;
import org.iop.exceptions.CantRestoreEncryptedWallet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import iop.org.furszy_lib.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.dialogs.FileAdapter;
import iop.org.iop_contributors_app.ui.dialogs.ShowPasswordCheckListener;
import iop_sdk.crypto.Crypto;
import iop_sdk.wallet.utils.WalletUtils;

/**
 * Created by mati on 06/12/16.
 */

public class RestoreDialogFragment2 extends DialogFragment {

    private WalletModule module;

    private EditText passwordView;
    private View root;

    public static RestoreDialogFragment2 newInstance(WalletModule module) {
        RestoreDialogFragment2 restoreDialogFragment2 = new RestoreDialogFragment2();
        restoreDialogFragment2.setModule(module);
        return restoreDialogFragment2;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        root = LayoutInflater.from(getActivity()).inflate(R.layout.restore_dialog, null);
        final TextView messageView = (TextView) root.findViewById(R.id.restore_wallet_dialog_message);
        final Spinner fileView = (Spinner) root.findViewById(R.id.import_keys_from_storage_file);
        passwordView = (EditText) root.findViewById(R.id.import_keys_from_storage_password);

        final DialogBuilder dialog = new DialogBuilder(getActivity());
        dialog.setTitle(R.string.import_keys_dialog_title);
        dialog.setView(root);
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
                else if (KEYS_FILE_FILTER.accept(file))
                    module.restorePrivateKeysFromBase58(file);
                else if (Crypto.OPENSSL_FILE_FILTER.accept(file)) {
                    try {
                        module.restoreWalletFromEncrypted(file, password);
                    } catch (CantRestoreEncryptedWallet x) {
                        final DialogBuilder warnDialog = DialogBuilder.warn(getActivity(), R.string.import_export_keys_dialog_failure_title);
                        warnDialog.setMessage(getActivity().getString(R.string.import_keys_dialog_failure, x.getMessage()));
                        warnDialog.setPositiveButton(R.string.button_dismiss, null);
                        warnDialog.setNegativeButton(R.string.button_retry, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int id) {
                                Toast.makeText(getActivity(),"Acá tengo que resetear el proceso",Toast.LENGTH_LONG).show();
                                //showDialog(DIALOG_RESTORE_WALLET);
                            }
                        });
                        warnDialog.show();
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getActivity(),"Error, please check logs",Toast.LENGTH_LONG).show();
                    }
                }
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

        final FileAdapter adapter = new FileAdapter(getActivity())
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


        return dialog.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        prepareRestoreWalletDialog(this.getDialog());
    }

    private void prepareRestoreWalletDialog(final Dialog dialog)
    {
        final AlertDialog alertDialog = (AlertDialog) dialog;

        final List<File> files = new LinkedList<File>();

        // external storage
        if (WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.exists() && WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.isDirectory())
            for (final File file : WalletConstants.Files.EXTERNAL_WALLET_BACKUP_DIR.listFiles())
                if (Crypto.OPENSSL_FILE_FILTER.accept(file))
                    files.add(file);

        // internal storage
        for (final String filename : getActivity().fileList())
            if (filename.startsWith(WalletConstants.Files.WALLET_KEY_BACKUP_PROTOBUF + '.'))
                files.add(new File(getActivity().getFilesDir(), filename));

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

        final ImportDialogButtonEnablerListener dialogButtonEnabler = new ImportDialogButtonEnablerListener(passwordView, alertDialog) {
            @Override
            protected boolean hasFile()
            {
                return fileView.getSelectedItem() != null;
            }

            @Override
            protected boolean needsPassword() {
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
    public void onCancel(DialogInterface dialog) {
        passwordView.setText(null); // get rid of it asap
    }

    public static final FileFilter KEYS_FILE_FILTER = new FileFilter() {


        @Override
        public boolean accept(final File file)
        {
            BufferedReader reader = null;

            try
            {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
                WalletUtils.readKeys(reader, WalletConstants.NETWORK_PARAMETERS,WalletConstants.BACKUP_MAX_CHARS);

                return true;
            }
            catch (final IOException x)
            {
                return false;
            }
            finally
            {
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (final IOException x)
                    {
                        // swallow
                    }
                }
            }
        }
    };

    public void setModule(WalletModule module) {
        this.module = module;
    }
}
