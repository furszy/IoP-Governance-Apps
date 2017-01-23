package iop.org.iop_contributors_app.ui.transactions;

import android.content.Context;
import android.view.View;

import org.bitcoinj.core.Transaction;
import org.iop.WalletModule;

import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 17/11/16.
 */

public class TransactionAdapter extends FermatAdapterImproved<Transaction,TransactionHolder> {

    private WalletModule module;

    public TransactionAdapter(Context context, WalletModule module) {
        super(context);
        this.module = module;
    }

    @Override
    protected TransactionHolder createHolder(View itemView, int type) {
        return new TransactionHolder(itemView,type);
    }

    @Override
    protected int getCardViewResource(int type) {
        return R.layout.unnavailable_transaction_row;
    }

    @Override
    protected void bindHolder(TransactionHolder holder, final Transaction data, final int position) {

        holder.txt_tx_hash.setText("Transaction hash: "+data.getHash().toString());
        holder.txt_value.setText("Value: "+data.getValueSentToMe(module.getWalletManager().getWallet()));
        holder.txt_blocks_left.setText("Block left until it be available: "+blockLeftAmount(data.getConfidence().getDepthInBlocks()));


    }

    private int blockLeftAmount(int depthInBlocks){
        return 101-depthInBlocks;
    }



}
