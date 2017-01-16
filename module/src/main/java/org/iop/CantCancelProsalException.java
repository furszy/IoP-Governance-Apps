package org.iop;

/**
 * Created by mati on 15/01/17.
 */
public class CantCancelProsalException extends Exception {

    public CantCancelProsalException(String message) {
        super(message);
    }

    public CantCancelProsalException(String message, Throwable cause) {
        super(message, cause);
    }
}
