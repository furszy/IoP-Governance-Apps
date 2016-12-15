package iop.org.iop_contributors_app;

import org.apache.http.conn.HttpHostConnectException;

/**
 * Created by mati on 15/12/16.
 */
public class ConnectionRefusedException extends Throwable {
    public ConnectionRefusedException(String s, Exception e) {
        super(s,e);
    }
}
