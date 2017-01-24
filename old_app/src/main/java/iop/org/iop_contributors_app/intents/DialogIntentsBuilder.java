package iop.org.iop_contributors_app.intents;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import org.iop.intents.constants.IntentsConstants;

import java.util.concurrent.TimeUnit;

import iop.org.furszy_lib.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop_sdk.wallet.WalletManager;

/**
 * Created by mati on 08/12/16.
 */

public class DialogIntentsBuilder {


    public static Dialog buildSuccedRestoreDialog(final Context context, final WalletManager walletManager, Intent intent){

        String message = intent.getStringExtra(IntentsConstants.INTENTE_EXTRA_MESSAGE);

        final DialogBuilder dialog = new DialogBuilder(context);
        dialog.setMessage(message);
        dialog.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                walletManager.resetBlockchain();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent blockchainServiceIntent = new Intent(context, BlockchainServiceImpl.class);
                        context.startService(blockchainServiceIntent);
                    }
                }, TimeUnit.SECONDS.toMillis(10));
                dialog.dismiss();
            }
        });
        return dialog.create();
    }


}
