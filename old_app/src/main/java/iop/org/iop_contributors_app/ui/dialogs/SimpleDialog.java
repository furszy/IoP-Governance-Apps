package iop.org.iop_contributors_app.ui.dialogs;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 26/01/17.
 */

public class SimpleDialog extends DialogFragment {

    private DialogListener cancelListener;
    private boolean actionCompleted;

    private String title;
    private String body;

    public static SimpleDialog newInstance(String title, String body) {

        SimpleDialog fragment = new SimpleDialog();
        fragment.setTitle(title);
        fragment.setBody(body);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.simple_dialog,null);

        TextView txt_title = (TextView) root.findViewById(R.id.txt_title);
        TextView txt_body = (TextView) root.findViewById(R.id.txt_body);

        txt_title.setText(title);
        txt_body.setText(body);

        TextView btn_ok = (TextView) root.findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCompleted = true;
                dismiss();
            }
        });

        return root;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (cancelListener!=null)
            cancelListener.cancel(actionCompleted);
    }

    public void setListener(DialogListener listener) {
        this.cancelListener = listener;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
