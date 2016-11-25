package iop.org.iop_contributors_app.configurations;

import android.content.SharedPreferences;

import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumConfigurations;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;

/**
 * Created by mati on 22/11/16.
 */

public class DefaultForumConfiguration extends Configurations implements ForumConfigurations {


    public static final String PREFS_NAME = "MyPrefsFile";

    public static final String PREFS_USER_IS_REGISTERED_IN_FORUM = "isRegInForum";
    public static final String PREFS_USERNAME = "forumUsername";
    public static final String PREFS_PASSWORD = "forumPassword";
    public static final String PREFS_EMAIL = "forumEmail";

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
        save(PREFS_EMAIL,mail);
    }

    @Override
    public ForumProfile getForumUser() {
        return new ForumProfile(
                getString(PREFS_USERNAME,null),
                getString(PREFS_PASSWORD,null),
                getString(PREFS_EMAIL,null)
        );
    }
}
