package iop.org.iop_contributors_app.intents;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.intents.constants.IntentsConstants;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.wallet.WalletManager;

/**
 * Created by mati on 08/12/16.
 */

public class DialogIntentsBuilder {


    public static Dialog buildSuccedRestoreDialog(Context context, final WalletManager walletManager, Intent intent){

        String message = intent.getStringExtra(IntentsConstants.INTENTE_EXTRA_MESSAGE);

        final DialogBuilder dialog = new DialogBuilder(context);
        dialog.setMessage(message);
        dialog.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int id)
            {
                walletManager.resetBlockchain();
                dialog.dismiss();
            }
        });
        return dialog.create();
    }

}
