package org.iop.configurations;

import android.content.SharedPreferences;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.iop.WalletConstants;

import iop_sdk.global.HardCodedConstans;
import iop_sdk.wallet.WalletPreferenceConfigurations;

import static org.iop.WalletConstants.CONTEXT;
import static org.iop.WalletConstants.Files.BIP39_WORDLIST_FILENAME;
import static org.iop.WalletConstants.Files.WALLET_FILENAME_PROTOBUF;
import static org.iop.WalletConstants.NETWORK_PARAMETERS;

/**
 * Created by mati on 11/11/16.
 */
public class WalletPreferencesConfiguration extends Configurations implements WalletPreferenceConfigurations {

    public static final String PREFS_NAME = "wallet_preferences";

    public static final String PREFS_KEY_CONNECTIVITY_NOTIFICATION = "connectivity_notification";

    public static final String PREFS_KEY_NODE = "node";

    public static final String PREFS_KEY_RECEIVE_ADDRESS = "receive_address";

    private static final String PREFS_KEY_BEST_CHAIN_HEIGHT_EVER = "best_chain_height_ever";

    public WalletPreferencesConfiguration(SharedPreferences prefs) {
        super(prefs);
    }


    public boolean getConnectivityNotificationEnabled() {
        return prefs.getBoolean(PREFS_KEY_CONNECTIVITY_NOTIFICATION, false);
    }

    public int getBestChainHeightEver() {
        return prefs.getInt(PREFS_KEY_BEST_CHAIN_HEIGHT_EVER, 0);
    }

    public int maybeIncrementBestChainHeightEver(final int bestChainHeightEver) {
        int height = getBestChainHeightEver();
        if (bestChainHeightEver > height) {
            prefs.edit().putInt(PREFS_KEY_BEST_CHAIN_HEIGHT_EVER, bestChainHeightEver).apply();
            return bestChainHeightEver;
        }
        return -1;
    }

    public void saveNode(String host){
        save(PREFS_KEY_NODE,host);
    }

    public String getNode(){
        return getString(PREFS_KEY_NODE, HardCodedConstans.HOST);
    }

    public void saveReceiveAddress(String address){
        save(PREFS_KEY_RECEIVE_ADDRESS,address);
    }

    public String getReceiveAddress(){
        return getString(PREFS_KEY_RECEIVE_ADDRESS,null);
    }


    /****** PREFERENCE CONSTANTS   ******/

    @Override
    public boolean isTest() {
        return WalletConstants.TEST;
    }

    @Override
    public Context getWalletContext() {
        return CONTEXT;
    }

    @Override
    public NetworkParameters getNetworkParams() {
        return NETWORK_PARAMETERS;
    }

    @Override
    public String getMnemonicFilename() {
        return BIP39_WORDLIST_FILENAME;
    }

    @Override
    public String getWalletProtobufFilename() {
        return WALLET_FILENAME_PROTOBUF;
    }

    @Override
    public String getKeyBackupProtobuf() {
        return WalletConstants.Files.WALLET_KEY_BACKUP_PROTOBUF;
    }

    @Override
    public long getBackupMaxChars() {
        return WalletConstants.BACKUP_MAX_CHARS;
    }

    @Override
    public long getWalletAutosaveDelayMs() {
        return WalletConstants.Files.WALLET_AUTOSAVE_DELAY_MS;
    }

    @Override
    public String getBlockchainFilename() {
        return WalletConstants.Files.BLOCKCHAIN_FILENAME;
    }

    @Override
    public int getPeerTimeoutMs() {
        return WalletConstants.PEER_TIMEOUT_MS;
    }

    @Override
    public long getPeerDiscoveryTimeoutMs() {
        return WalletConstants.PEER_DISCOVERY_TIMEOUT_MS;
    }


    /****** END PREFERENCE CONSTANTS   ******/

}
