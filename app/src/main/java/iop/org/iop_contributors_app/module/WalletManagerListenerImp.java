package iop.org.iop_contributors_app.module;

import org.iop.WalletConstants;

import iop_sdk.wallet.WalletManagerListener;

/**
 * Created by mati on 26/12/16.
 */

public class WalletManagerListenerImp implements WalletManagerListener {

    private WalletModule walletModule;

    public WalletManagerListenerImp(WalletModule walletModule) {
        this.walletModule = walletModule;
    }

    @Override
    public void onWalletRestored() {
        walletModule.showDialog(WalletConstants.SHOW_RESTORE_SUCCED_DIALOG);
    }
}
