package iop.org.iop_contributors_app.ui.transactions;

import android.view.View;

import org.iop.WalletModule;

import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.furszy_lib.adapter.FermatListItemListeners;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.utils.ForumUtils;
import iop_sdk.governance.vote.VoteWrapper;

/**
 * Created by mati on 17/11/16.
 */

public class TransactionAdapter extends FermatAdapterImproved<TransactionWrapper,TransactionHolder> {

    private WalletModule module;

    public TransactionAdapter(BaseActivity context, WalletModule module) {
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
    protected void bindHolder(TransactionHolder holder, final TransactionWrapper data, final int position) {

        holder.txt_tx_hash.setText("Transaction hash: "+data.getTransaction().getHash().toString());
        holder.txt_value.setText("Value: "+data.getTransaction().getValueSentToMe(module.getWalletManager().getWallet()));
        holder.txt_blocks_left.setText("Block left until it be available: "+blockLeftAmount(data.getTransaction().getConfidence().getDepthInBlocks()));


    }

    private int blockLeftAmount(int depthInBlocks){
        return 101-depthInBlocks;
    }



}
