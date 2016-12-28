package iop.org.iop_contributors_app.ui.dialogs.wallet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

public class ImportDialogButtonEnablerListener implements TextWatcher, AdapterView.OnItemSelectedListener
{
	private final TextView passwordView;
	private final AlertDialog dialog;

	public ImportDialogButtonEnablerListener(final TextView passwordView, final AlertDialog dialog)
	{
		this.passwordView = passwordView;
		this.dialog = dialog;

		handle();
	}

	@Override
	public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id)
	{
		handle();
	}

	@Override
	public void onNothingSelected(final AdapterView<?> parent)
	{
		handle();
	}

	@Override
	public void afterTextChanged(final Editable s)
	{
		handle();
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after)
	{
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count)
	{
	}

	public void handle()
	{
		final boolean needsPassword = needsPassword();
		final boolean hasPassword = !passwordView.getText().toString().trim().isEmpty();
		final boolean hasFile = hasFile();

		final Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		button.setEnabled(hasFile && (!needsPassword || hasPassword));
	}

	protected boolean hasFile()
	{
		return true;
	}

	protected boolean needsPassword()
	{
		return true;
	}
}
