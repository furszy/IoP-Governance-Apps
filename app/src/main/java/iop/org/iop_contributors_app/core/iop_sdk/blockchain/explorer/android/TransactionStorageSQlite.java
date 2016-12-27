package iop.org.iop_contributors_app.core.iop_sdk.blockchain.explorer.android;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iop_sdk.blockchain.explorer.TransactionStorage;

import static iop_sdk.blockchain.explorer.TxUtils.serializeData;


/**
 * Created by mati on 18/12/16.
 */

public class TransactionStorageSQlite implements TransactionStorage {

    List<Transaction> txs = new ArrayList<>();

    /** Outputs que estoy vigilando que no han sido consumidos (esto va en una db) */
    Map<byte[],Boolean> notSpentwatchedOutputs = new HashMap<>();

    public TransactionStorageSQlite() {
    }

    /** metodo solo de pueba.. */
    public byte[] saveWatchedOutput(Sha256Hash hash, int index, int size){
        byte[] data = serializeData(hash, index,size);
        notSpentwatchedOutputs.put(data,false);
        return data;
    }

    @Override
    public void saveTx(Transaction tx) {
        txs.add(tx);
    }

    @Override
    public void markOutputSpend(Transaction tx, int index, int lenght){
        notSpentwatchedOutputs.put(serializeData(tx.getHash(),index,lenght),true);
    }

    @Override
    public List<Transaction> getTransactions() {
        return txs;
    }
}
