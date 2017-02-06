package iop.org.furszy_lib.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import iop.org.furszy_lib.R;

import static android.view.View.GONE;

/**
 * Created by mati on 26/01/17.
 */

public class SimpleTwoButtonsDialog extends Dialog implements View.OnClickListener {

    private SimpleTwoBtnsDialogListener listener;

    private String title;
    private String body;

    private int titleColor;
    private int bodyColor;
    private int containerBtnsBackgroundColor;
    private int leftBtnTextColor;
    private int rightBtnTextColor;

    private int imgAlertRes;

    public SimpleTwoButtonsDialog(Context context) {
        super(context);
    }

    public static SimpleTwoButtonsDialog newInstance(Context context) {
        SimpleTwoButtonsDialog fragment = new SimpleTwoButtonsDialog(context);
        return fragment;
    }

    public interface SimpleTwoBtnsDialogListener{

        void onRightBtnClicked(SimpleTwoButtonsDialog dialog);

        void onLeftBtnClicked(SimpleTwoButtonsDialog dialog);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.simple_two_btns_dialog);

        View title_container = findViewById(R.id.title_container);
        TextView txt_title = (TextView) findViewById(R.id.txt_title);
        TextView txt_body = (TextView) findViewById(R.id.txt_body);
        ImageView imgAlert = (ImageView) findViewById(R.id.img_alert);
        View btn_container = findViewById(R.id.btn_container);
        TextView btn_left = (TextView) findViewById(R.id.btn_left);
        TextView btn_right = (TextView) findViewById(R.id.btn_right);

        initTitle(title_container,txt_title);
        initBody(txt_body);
        initImgAlert(imgAlert);
        initBtns(btn_container,btn_left,btn_right);

//        setOnDismissListener(this);

        super.onCreate(savedInstanceState);
    }




    private void initTitle(View title_container, TextView txt_title){
        if (title!=null){
            title_container.setVisibility(View.VISIBLE);
            txt_title.setText(title);
            if (titleColor!=0){
                txt_title.setTextColor(titleColor);
            }
        }else {
            title_container.setVisibility(GONE);
        }
    }

    private void initBody(TextView txt_body){
        if (body!=null){
            txt_body.setText(body);
            if (bodyColor!=0){
                txt_body.setTextColor(bodyColor);
            }
        }
    }

    private void initImgAlert(ImageView img_alert){
        if (imgAlertRes!=0){
            img_alert.setImageResource(imgAlertRes);
        }
    }

    private void initBtns(View btn_container, TextView btn_left, TextView btn_right){
        if (containerBtnsBackgroundColor!=0){
            btn_container.setBackgroundColor(containerBtnsBackgroundColor);
        }
        if (leftBtnTextColor!=0){
            btn_left.setTextColor(leftBtnTextColor);
        }

        if (rightBtnTextColor!=0){
            btn_right.setTextColor(rightBtnTextColor);
        }

        btn_left.setOnClickListener(this);
        btn_right.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (listener!=null) {

            if (id == R.id.btn_left) {
                listener.onLeftBtnClicked(this);
            } else if (id == R.id.btn_right) {
                listener.onRightBtnClicked(this);
            }

        }
    }

    public void setListener(SimpleTwoBtnsDialogListener listener) {
        this.listener = listener;
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

    public void setContainerBtnsBackgroundColor(int containerBtnsBackgroundColor) {
        this.containerBtnsBackgroundColor = containerBtnsBackgroundColor;
    }

    public void setLeftBtnTextColor(int leftBtnTextColor) {
        this.leftBtnTextColor = leftBtnTextColor;
    }

    public void setRightBtnTextColor(int rightBtnTextColor) {
        this.rightBtnTextColor = rightBtnTextColor;
    }

    public void setBtnsTextColor(int color){
        setLeftBtnTextColor(color);
        setRightBtnTextColor(color);
    }
}
