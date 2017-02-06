package iop.org.iop_contributors_app.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import org.iop.WalletModule;

import iop.org.furszy_lib.dialogs.SimpleTwoButtonsDialog;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.base.SimpleTextDialog;
import iop.org.iop_contributors_app.ui.dialogs.wallet.InsuficientFundsDialog;

/**
 * Created by mati on 21/12/16.
 */

public class SimpleDialogs {

    public static void showErrorDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle((title!=null)?title:"Upss");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static AlertDialog buildDialog(Context context, String title, String message,DialogInterface.OnClickListener onOkClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle((title!=null)?title:"Upss");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",onOkClickListener);
        return alertDialog;
    }


    public static void showInsuficientFundsException(BaseActivity baseActivity, WalletModule module){
        InsuficientFundsDialog insuficientFundsDialog = InsuficientFundsDialog.newInstance(module);
        insuficientFundsDialog.show(baseActivity.getFragmentManager(),"insuficientFundsDialog");
    }



    public static SimpleTextDialog buildSimpleDialogForContributors(Context context, String title, String body){
        final SimpleTextDialog dialog = SimpleTextDialog.newInstance();
        dialog.setTitle(title);
        dialog.setBody(body);
        dialog.setOkBtnBackgroundColor(context.getResources().getColor(R.color.text_blue));
        dialog.setOkBtnTextColor(Color.WHITE);
        return dialog;
    }

    public static SimpleTwoButtonsDialog buildSimpleTwoBtnsDialogForContributors(Context context, String title, String body, SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener simpleTwoBtnsDialogListener){
        final SimpleTwoButtonsDialog dialog = SimpleTwoButtonsDialog.newInstance(context);
        dialog.setTitle(title);
        dialog.setTitleColor(Color.BLACK);
        dialog.setBody(body);
        dialog.setBodyColor(Color.BLACK);
        dialog.setListener(simpleTwoBtnsDialogListener);
        dialog.setContainerBtnsBackgroundColor(context.getResources().getColor(R.color.text_blue));
        dialog.setBtnsTextColor(Color.WHITE);
        return dialog;
    }

    public static SimpleTextDialog buildSimpleDialogForVoting(Context context, String title, String body){
        final SimpleTextDialog dialog = SimpleTextDialog.newInstance();
        dialog.setTitle(title);
        dialog.setBody(body);
        dialog.setOkBtnBackgroundColor(context.getResources().getColor(R.color.purple));
        dialog.setOkBtnTextColor(Color.WHITE);
        return dialog;
    }

    public static SimpleTwoButtonsDialog buildSimpleTwoBtnsDialogForVoting(Context context, String title, String body, SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener simpleTwoBtnsDialogListener){
        final SimpleTwoButtonsDialog dialog = SimpleTwoButtonsDialog.newInstance(context);
        dialog.setTitle(title);
        dialog.setBody(body);
        dialog.setListener(simpleTwoBtnsDialogListener);
        dialog.setContainerBtnsBackgroundColor(context.getResources().getColor(R.color.purple));
        dialog.setBtnsTextColor(Color.WHITE);
        return dialog;
    }



}
