package iop.org.iop_contributors_app.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.iop.WalletModule;

import iop.org.iop_contributors_app.ui.base.BaseActivity;
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





}
