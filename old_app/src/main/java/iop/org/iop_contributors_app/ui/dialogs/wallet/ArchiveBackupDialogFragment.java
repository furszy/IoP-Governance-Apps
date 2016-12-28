package iop.org.iop_contributors_app.ui.dialogs.wallet;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.Toast;

import org.iop.WalletConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import iop.org.furszy_lib.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.R;

public class ArchiveBackupDialogFragment extends DialogFragment
{
	private static final String FRAGMENT_TAG = ArchiveBackupDialogFragment.class.getName();

	private static final String KEY_FILE = "file";

	public static void show(Context context,final FragmentManager fm, final File backupFile)
	{
		final DialogFragment newFragment = instance(backupFile,context);
		newFragment.show(fm, FRAGMENT_TAG);
	}

	private static ArchiveBackupDialogFragment instance(final File backupFile, Context context) {
		final ArchiveBackupDialogFragment fragment = new ArchiveBackupDialogFragment();
		fragment.setContext(context);
		final Bundle args = new Bundle();
		args.putSerializable(KEY_FILE, backupFile);
		fragment.setArguments(args);

		return fragment;
	}

	private Context activity;

	private static final Logger log = LoggerFactory.getLogger(ArchiveBackupDialogFragment.class);

	@Override
	public void onAttach(final Context activity)
	{
		super.onAttach(activity);

		this.activity = activity;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Bundle args = getArguments();
		final File backupFile = (File) args.getSerializable(KEY_FILE);

		final String path;
		final String backupPath = backupFile.getAbsolutePath();
		final String storagePath = WalletConstants.Files.EXTERNAL_STORAGE_DIR.getAbsolutePath();
		if (backupPath.startsWith(storagePath))
			path = backupPath.substring(storagePath.length());
		else
			path = backupPath;

		final DialogBuilder dialog = new DialogBuilder(activity);
		dialog.setMessage(Html.fromHtml(getString(R.string.export_keys_dialog_success, path)));
		dialog.setPositiveButton(WholeStringBuilder.bold(getString(R.string.export_keys_dialog_button_archive)),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(final DialogInterface dialog, final int which)
					{
						archiveWalletBackup(backupFile);
					}
				});
		dialog.setNegativeButton(R.string.button_dismiss, null);

		return dialog.create();
	}

	private void archiveWalletBackup(final File backupFile)
	{
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_keys_dialog_mail_subject));
//		intent.putExtra(Intent.EXTRA_TEXT,
//				getString(R.string.export_keys_dialog_mail_text) + "\n\n" + String.format(Constants.WEBMARKET_APP_URL, activity.getPackageName())
//						+ "\n\n" + Constants.SOURCE_URL + '\n');
		intent.setType(WalletConstants.MIMETYPE_WALLET_BACKUP);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(backupFile));

		try
		{
			startActivity(Intent.createChooser(intent, getString(R.string.export_keys_dialog_mail_intent_chooser)));
			log.info("invoked chooser for archiving wallet backup");
		}
		catch (final Exception x)
		{
			//todo: falta esto
			Toast.makeText(activity,R.string.export_keys_dialog_mail_intent_failed,Toast.LENGTH_LONG).show();
			log.error("archiving wallet backup failed", x);
		}
	}

	public void setContext(Context context) {
		this.activity = context;
	}

	public static class WholeStringBuilder extends SpannableStringBuilder
	{
		public static CharSequence bold(final CharSequence text)
		{
			return new WholeStringBuilder(text, new StyleSpan(Typeface.BOLD));
		}

		public WholeStringBuilder(final CharSequence text, final Object span)
		{
			super(text);

			setSpan(span, 0, text.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
}