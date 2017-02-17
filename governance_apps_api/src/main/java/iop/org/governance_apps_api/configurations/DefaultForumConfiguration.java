package iop.org.governance_apps_api.configurations;

import android.content.SharedPreferences;

import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumConfigurations;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;
import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.DiscouseApiConstants;

/**
 * Created by mati on 22/11/16.
 */

public class DefaultForumConfiguration extends Configurations implements ForumConfigurations {


    public static final String PREFS_NAME = "MyPrefsFile";

    public static final String PREFS_USER_IS_REGISTERED_IN_FORUM = "isRegInForum";
    public static final String PREFS_USERNAME = "forumUsername";
    public static final String PREFS_PASSWORD = "forumPassword";
    public static final String PREFS_EMAIL = "forumEmail";
    public static final String PREFS_API_KEY = "apiKey";
    public static final String PREFS_URL = "forumUrl";

    public DefaultForumConfiguration(SharedPreferences prefs) {
        super(prefs);
    }

    @Override
    public void setIsRegistered(boolean isRegistered) {
        save(PREFS_USER_IS_REGISTERED_IN_FORUM,isRegistered);
    }

    @Override
    public boolean isRegistered() {
        return getBoolean(PREFS_USER_IS_REGISTERED_IN_FORUM,false);
    }

    @Override
    public void setForumUser(String name, String password, String mail) {
        save(PREFS_USERNAME,name);
        save(PREFS_PASSWORD,password);
        if (mail!=null)save(PREFS_EMAIL,mail);
    }

    @Override
    public void setApiKey(String apiKey) {
        save(PREFS_API_KEY,apiKey);
    }

    @Override
    public void setUrl(String url) {
        save(PREFS_URL,url);
    }

    @Override
    public ForumProfile getForumUser() {
        String username = getString(PREFS_USERNAME,null);
        if (username==null) return null;
        return new ForumProfile(
                username,
                getString(PREFS_PASSWORD,null),
                getString(PREFS_EMAIL,null)
        );
    }

    @Override
    public String getApiKey() {
        return getString(PREFS_API_KEY,null);
    }

    @Override
    public String getUrl() {
        return getString(PREFS_URL, DiscouseApiConstants.FORUM_URL);
    }
}
