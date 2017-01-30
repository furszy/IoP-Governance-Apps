package iop.org.iop_contributors_app.ui.base;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.dialogs.DialogListener;

/**
 * Created by mati on 26/01/17.
 */

public class SimpleTextDialog extends DialogFragment {

    private DialogListener cancelListener;
    private boolean actionCompleted;

    private String title;
    private String body;

    private int titleColor;
    private int bodyColor;
    private int okBtnBackgroundColor;
    private int okBtnTextColor;

    private int imgAlertRes;

    public static SimpleTextDialog newInstance() {
        SimpleTextDialog fragment = new SimpleTextDialog();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.simple_dialog,null);

        TextView txt_title = (TextView) root.findViewById(R.id.txt_title);
        TextView txt_body = (TextView) root.findViewById(R.id.txt_body);
        ImageView imgAlert = (ImageView) root.findViewById(R.id.img_alert);
        TextView btn_ok = (TextView) root.findViewById(R.id.btn_ok);

        initTitle(txt_title);
        initBody(txt_body);
        initImgAlert(imgAlert);
        initOkBtn(btn_ok);

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

    private void initTitle(TextView txt_title){
        if (title!=null){
            txt_title.setText(title);
            if (titleColor>0){
                txt_title.setTextColor(titleColor);
            }
        }
    }

    private void initBody(TextView txt_body){
        if (body!=null){
            txt_body.setText(body);
            if (bodyColor>0){
                txt_body.setTextColor(bodyColor);
            }
        }
    }

    private void initImgAlert(ImageView img_alert){
        if (imgAlertRes >0){
            img_alert.setImageResource(imgAlertRes);
        }
    }

    private void initOkBtn(TextView btn_ok){
        if (okBtnBackgroundColor>0){
            btn_ok.setBackgroundColor(okBtnBackgroundColor);
        }
        if (okBtnTextColor>0){
            btn_ok.setTextColor(okBtnTextColor);
        }
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

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    public void setBodyColor(int bodyColor) {
        this.bodyColor = bodyColor;
    }

    public void setImgAlertRes(int imgAlertRes) {
        this.imgAlertRes = imgAlertRes;
    }

    public void setOkBtnBackgroundColor(int okBtnBackgroundColor) {
        this.okBtnBackgroundColor = okBtnBackgroundColor;
    }

    public void setOkBtnTextColor(int okBtnTextColor) {
        this.okBtnTextColor = okBtnTextColor;
    }
}
