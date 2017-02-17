package iop.org.governance_apps_api.wallet.exceptions;

/**
 * Created by mati on 17/11/16.
 */

public class InsuficientBalanceException extends Exception {

    public InsuficientBalanceException(String message) {
        super(message);
    }
}
