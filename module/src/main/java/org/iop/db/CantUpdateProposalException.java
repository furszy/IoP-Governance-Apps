package org.iop.db;

/**
 * Created by mati on 01/12/16.
 */
public class CantUpdateProposalException extends Exception {
    public CantUpdateProposalException(Exception e) {
        super(e);
    }

    public CantUpdateProposalException(String message) {
        super(message);
    }
}
