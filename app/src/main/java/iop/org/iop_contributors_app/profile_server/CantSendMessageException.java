package iop.org.iop_contributors_app.profile_server;

/**
 * Created by mati on 20/11/16.
 */
public class CantSendMessageException extends Exception {
    public CantSendMessageException(Exception e) {
        super(e);
    }
}
