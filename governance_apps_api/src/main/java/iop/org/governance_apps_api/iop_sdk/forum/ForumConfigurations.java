package iop.org.governance_apps_api.iop_sdk.forum;

/**
 * Created by mati on 22/11/16.
 */
public interface ForumConfigurations {


    void setIsRegistered(boolean isRegistered);

    boolean isRegistered();

    void setForumUser(String name, String password, String mail);

    void setApiKey(String apiKey);

    void setUrl(String url);

    ForumProfile getForumUser();

    String getApiKey();

    void remove();

    String getUrl();
}
