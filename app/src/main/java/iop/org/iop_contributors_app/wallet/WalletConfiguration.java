package iop.org.iop_contributors_app.wallet;

import org.bitcoinj.core.NetworkParameters;

/**
 * Created by mati on 08/11/16.
 */

public class WalletConfiguration {

    private NetworkParameters networkParameters;


    public WalletConfiguration(NetworkParameters networkParameters) {
        this.networkParameters = networkParameters;
    }

}
