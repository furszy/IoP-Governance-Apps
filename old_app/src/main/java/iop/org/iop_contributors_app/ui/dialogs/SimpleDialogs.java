package iop.org.iop_contributors_app.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.iop.AppController;
import org.iop.WalletConstants;
import org.iop.WalletModule;

import java.io.IOException;

import iop.org.furszy_lib.dialogs.SimpleTwoButtonsDialog;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.base.SimpleTextDialog;
import iop.org.iop_contributors_app.ui.dialogs.wallet.InsuficientFundsDialog;
import iop.org.iop_contributors_app.utils.Cache;
import iop.org.iop_contributors_app.utils.CrashReporter;

import static android.graphics.Color.WHITE;
import static iop.org.furszy_lib.utils.AndroidUtils.shareText;
import static iop.org.furszy_lib.utils.AndroidUtils.textToClipboard;
import static iop.org.furszy_lib.utils.QrUtils.encodeAsBitmap;
import static iop.org.furszy_lib.utils.SizeUtils.convertDpToPx;

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


    public static ReportIssueDialogBuilder buildReportIssueDialogVoting(final Context context, final AppController appController, final WalletModule module,int titleRes,int messageRes){
        final ReportIssueDialogBuilder dialog = new ReportIssueDialogBuilder(
                context,
                "iop.org.voting_app.myfileprovider",
                titleRes,
                messageRes)
        {
            @Override
            protected CharSequence subject() {
                return WalletConstants.REPORT_SUBJECT_ISSUE + " " + module.getAppController().packageInfoWrapper().getVersionName();
            }

            @Override
            protected CharSequence collectApplicationInfo() throws IOException
            {
                final StringBuilder applicationInfo = new StringBuilder();
                CrashReporter.appendApplicationInfo(applicationInfo, appController);
                return applicationInfo;
            }

            @Override
            protected CharSequence collectStackTrace()
            {
                return null;
            }

            @Override
            protected CharSequence collectDeviceInfo() throws IOException
            {
                final StringBuilder deviceInfo = new StringBuilder();
                CrashReporter.appendDeviceInfo(deviceInfo, context);
                return deviceInfo;
            }

            @Override
            protected CharSequence collectWalletDump()
            {
                return module.getWalletManager().getWallet().toString(false, true, true, null);
            }
        };
        return dialog;
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


    public static void showQrDialog(final Activity activity, WalletModule module){

        try {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.qr_dialog);


            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.txt_share);

            dialog.findViewById(R.id.txt_exit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            ImageView image = (ImageView) dialog.findViewById(R.id.img_qr);

            final String address = module.getReceiveAddress();
            // qr
            Bitmap qrBitmap = Cache.getQrBigBitmapCache();
            if (qrBitmap == null) {
                Resources r = activity.getResources();
                int px = convertDpToPx(r,175);
                qrBitmap = encodeAsBitmap(address, px, px, Color.parseColor("#1A1A1A"), WHITE );
                Cache.setQrBigBitmapCache(qrBitmap);
            }
            image.setImageBitmap(qrBitmap);

            // cache address
            TextView txt_qr = (TextView)dialog.findViewById(R.id.txt_qr);
            txt_qr.setText(address);
//            txt_qr.setOnLongClickListener();

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textToClipboard(v.getContext(),address);
                    Toast.makeText(activity,"Copied",Toast.LENGTH_LONG).show();
                }
            };

            dialog.findViewById(R.id.txt_copy).setOnClickListener(clickListener);

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareText(activity,"Qr",address);
                    dialog.dismiss();
                }
            });



            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
