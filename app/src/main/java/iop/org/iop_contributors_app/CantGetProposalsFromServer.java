package iop.org.iop_contributors_app;

import org.apache.http.conn.HttpHostConnectException;

/**
 * Created by mati on 23/12/16.
 */
public class CantGetProposalsFromServer extends Throwable {
    public CantGetProposalsFromServer(String s, Exception e) {
        super(s,e);
    }

    public CantGetProposalsFromServer(String s) {
        super(s);
    }
}
