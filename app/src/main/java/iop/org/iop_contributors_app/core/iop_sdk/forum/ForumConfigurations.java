package iop.org.iop_contributors_app.core.iop_sdk.forum;

/**
 * Created by mati on 22/11/16.
 */
public interface ForumConfigurations {


    void setIsRegistered(boolean isRegistered);

    boolean isRegistered();

    void setForumUser(String name,String password,String mail);

    ForumProfile getForumUser();


}
