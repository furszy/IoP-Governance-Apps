package iop.org.iop_contributors_app.core.iop_sdk.blockchain.explorer;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;

import java.util.List;

/**
 * Created by mati on 18/12/16.
 */
public interface TransactionStorage {


    void saveTx(Transaction tx);

    byte[] saveWatchedOutput(Sha256Hash hash, int index, int size);

    void markOutputSpend(Transaction tx, int index, int lenght);

    List<Transaction> getTransactions();
}
