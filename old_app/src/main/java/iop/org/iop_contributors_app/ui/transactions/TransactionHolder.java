package iop.org.iop_contributors_app.ui.transactions;

import android.view.View;
import android.widget.TextView;

import iop.org.furszy_lib.adapter.FermatViewHolder;
import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 23/12/16.
 */

public class TransactionHolder extends FermatViewHolder {

    TextView txt_tx_hash;
    TextView txt_value;
    TextView txt_blocks_left;



    public TransactionHolder(View itemView, int holderType) {
        super(itemView, holderType);

        txt_tx_hash = (TextView) itemView.findViewById(R.id.txt_tx_hash);
        txt_value = (TextView) itemView.findViewById(R.id.txt_value);
        txt_blocks_left = (TextView) itemView.findViewById(R.id.txt_blocks_left);

    }
}