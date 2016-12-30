package org.iop.db;

/**
 * Created by mati on 03/12/16.
 */
public class CantGetProposalException extends Exception {

    public CantGetProposalException() {
    }

    public CantGetProposalException(String message) {
        super(message);
    }

    public CantGetProposalException(String message, Exception e) {
        super(message,e);
    }
}
