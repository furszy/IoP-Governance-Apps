package iop.org.governance_apps_api.configurations;

import android.content.SharedPreferences;

import iop.org.iop_contributors_app.HardCodedConstans;

/**
 * Created by mati on 11/11/16.
 */
public class WalletPreferencesConfiguration extends Configurations{

    public static final String PREFS_NAME = "wallet_preferences";

    public static final String PREFS_KEY_CONNECTIVITY_NOTIFICATION = "connectivity_notification";

    public static final String PREFS_KEY_NODE = "node";

    private static final String PREFS_KEY_BEST_CHAIN_HEIGHT_EVER = "best_chain_height_ever";

    public WalletPreferencesConfiguration(SharedPreferences prefs) {
        super(prefs);
    }


    public boolean getConnectivityNotificationEnabled() {
        return prefs.getBoolean(PREFS_KEY_CONNECTIVITY_NOTIFICATION, false);
    }

    public int getBestChainHeightEver()
    {
        return prefs.getInt(PREFS_KEY_BEST_CHAIN_HEIGHT_EVER, 0);
    }

    public void maybeIncrementBestChainHeightEver(final int bestChainHeightEver)
    {
        if (bestChainHeightEver > getBestChainHeightEver())
            prefs.edit().putInt(PREFS_KEY_BEST_CHAIN_HEIGHT_EVER, bestChainHeightEver).apply();
    }

    public void saveNode(String host){
        save(PREFS_KEY_NODE,host);
    }

    public String getNode(){
        return getString(PREFS_KEY_NODE, HardCodedConstans.HOST);
    }
}
