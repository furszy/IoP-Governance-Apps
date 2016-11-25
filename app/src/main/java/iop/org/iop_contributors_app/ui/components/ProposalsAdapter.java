package iop.org.iop_contributors_app.ui.components;

import android.content.Context;
import android.view.View;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.Proposal;
import iop.org.iop_contributors_app.ui.components.sdk.FermatAdapterImproved;

/**
 * Created by mati on 17/11/16.
 */

public class ProposalsAdapter extends FermatAdapterImproved<Proposal,ProposalsHolder> {


    public ProposalsAdapter(Context context) {
        super(context);
    }

    @Override
    protected ProposalsHolder createHolder(View itemView, int type) {
        return new ProposalsHolder(itemView,type);
    }

    @Override
    protected int getCardViewResource(int type) {
        return R.layout.proposal_row;
    }

    @Override
    protected void bindHolder(ProposalsHolder holder, Proposal data, int position) {



    }
}
