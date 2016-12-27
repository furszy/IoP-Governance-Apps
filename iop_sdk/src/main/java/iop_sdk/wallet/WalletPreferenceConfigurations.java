package iop_sdk.wallet;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;

/**
 * Created by mati on 25/12/16.
 */
public interface WalletPreferenceConfigurations {

    boolean getConnectivityNotificationEnabled();

    int getBestChainHeightEver();

    void maybeIncrementBestChainHeightEver(final int bestChainHeightEver);

    void saveNode(String host);

    String getNode();

    void saveReceiveAddress(String address);

    String getReceiveAddress();


    /****** PREFERENCE CONSTANTS   ******/

    String getMnemonicFilename();

    String getWalletProtobufFilename();

    NetworkParameters getNetworkParams();

    Context getWalletContext();

    String getKeyBackupProtobuf();

    long getBackupMaxChars();

    boolean isTest();

    long getWalletAutosaveDelayMs();

    String getBlockchainFilename();

    int getPeerTimeoutMs();

    long getPeerDiscoveryTimeoutMs();


    /****** END PREFERENCE CONSTANTS   ******/


}