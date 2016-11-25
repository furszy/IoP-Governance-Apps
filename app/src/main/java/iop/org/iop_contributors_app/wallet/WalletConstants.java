package iop.org.iop_contributors_app.wallet;

import android.text.format.DateUtils;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;

import iop.org.iop_contributors_app.ApplicationController;

/**
 * Created by mati on 11/11/16.
 */

public class WalletConstants {

    public static final NetworkParameters NETWORK_PARAMETERS = RegTestParams.get();

    public static final boolean TEST = true;

    /** Bitcoinj global context. */
    public static final Context CONTEXT = new Context(NETWORK_PARAMETERS);

    /** User-agent to use for network access. */
    public static final String USER_AGENT = ApplicationController.getInstance().getPackageName()+"_AGENT";

    public static final class Files{

        private static final String FILENAME_NETWORK_SUFFIX = NETWORK_PARAMETERS.getId();

        /** Filename of the wallet. */
        public static final String WALLET_FILENAME_PROTOBUF = "wallet-protobuf" + FILENAME_NETWORK_SUFFIX;
        /** How often the wallet is autosaved. */
        public static final long WALLET_AUTOSAVE_DELAY_MS = 5 * DateUtils.SECOND_IN_MILLIS;
        /** Filename of the automatic wallet backup. */
        public static final String WALLET_KEY_BACKUP_PROTOBUF = "key-backup-protobuf" + FILENAME_NETWORK_SUFFIX;
        /** Filename of the block store for storing the chain. */
        public static final String BLOCKCHAIN_FILENAME = "blockchain" + FILENAME_NETWORK_SUFFIX;

        public static final String BIP39_WORDLIST_FILENAME = "bip39-wordlist.txt";


    }

    // blockchain service

    public static final int PEER_DISCOVERY_TIMEOUT_MS = 10 * (int) DateUtils.SECOND_IN_MILLIS;
    public static final int PEER_TIMEOUT_MS = 15 * (int) DateUtils.SECOND_IN_MILLIS;

    // Notifications ids
    public static final int NOTIFICATION_ID_CONNECTED = 0;
    public static final int NOTIFICATION_ID_COINS_RECEIVED = 1;

    // Memoria minima (si la app posee menos de esto conecto menos peers)
    public static final int MEMORY_CLASS_LOWEND = 48;
    //todo: ver que es esto
    public static final long BLOCKCHAIN_STATE_BROADCAST_THROTTLE_MS = DateUtils.SECOND_IN_MILLIS;




}
