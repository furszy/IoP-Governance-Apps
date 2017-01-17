package iop.org.iop_contributors_app.ui.transactions;

import org.bitcoinj.core.Transaction;

/**
 * Created by mati on 17/01/17.
 */
public class TransactionWrapper {

    private org.bitcoinj.core.Transaction transaction;

    public TransactionWrapper(Transaction transaction){
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
