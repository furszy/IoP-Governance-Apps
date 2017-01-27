package org.iop;

/**
 * Created by mati on 27/01/17.
 */
public class CantCancelVoteException extends Exception {

    public CantCancelVoteException(String message) {
        super(message);
    }

    public CantCancelVoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
