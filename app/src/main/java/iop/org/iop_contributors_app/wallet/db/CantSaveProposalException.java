package iop.org.iop_contributors_app.wallet.db;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Created by mati on 17/11/16.
 */
public class CantSaveProposalException extends Exception {

    public CantSaveProposalException(Exception e) {
        super(e);
    }

    public CantSaveProposalException(String message) {
        super(message);
    }
}
