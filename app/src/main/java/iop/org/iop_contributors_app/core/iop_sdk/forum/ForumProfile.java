package iop.org.iop_contributors_app.core.iop_sdk.forum;

/**
 * Created by mati on 23/11/16.
 */

public class ForumProfile {

    private String username;
    private String password;
    private String email;

    public ForumProfile(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public ForumProfile(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
