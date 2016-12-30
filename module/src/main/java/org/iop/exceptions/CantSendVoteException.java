package org.iop.exceptions;

/**
 * Created by mati on 21/12/16.
 */
public class CantSendVoteException extends Throwable {
    public CantSendVoteException(String s, Exception e) {
        super(s,e);
    }
}
