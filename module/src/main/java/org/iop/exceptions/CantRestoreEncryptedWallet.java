package org.iop.exceptions;

/**
 * Created by mati on 06/12/16.
 */
public class CantRestoreEncryptedWallet extends Exception {

    public CantRestoreEncryptedWallet(String message, Exception cause) {
        super(message, cause);
    }

    public CantRestoreEncryptedWallet(Exception x) {
        super(x);
    }
}
