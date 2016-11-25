package iop.org.iop_contributors_app.wallet.exceptions;

import iop.org.iop_contributors_app.wallet.db.CantSaveProposalException;

/**
 * Created by mati on 17/11/16.
 */
public class CantSendProposalException extends Exception {

    public CantSendProposalException(String s, Exception e) {
        super(s,e);
    }

    public CantSendProposalException(String s) {
        super(s);
    }
}
