package iop.org.iop_contributors_app.ui.dialogs;

import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * @author Andreas Schildbach
 */
public final class ShowPasswordCheckListener implements CompoundButton.OnCheckedChangeListener
{
	private EditText[] passwordViews;

	public ShowPasswordCheckListener(final EditText... passwordViews)
	{
		this.passwordViews = passwordViews;
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
	{
		final TransformationMethod transformationMethod = isChecked ? null : PasswordTransformationMethod.getInstance();

		for (final EditText passwordView : passwordViews)
			passwordView.setTransformationMethod(transformationMethod);
	}
}
