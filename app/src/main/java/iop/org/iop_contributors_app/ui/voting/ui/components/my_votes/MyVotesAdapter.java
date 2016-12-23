package iop.org.iop_contributors_app.ui.voting.ui.components.my_votes;

import android.content.Intent;
import android.view.View;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.propose.Proposal;
import iop.org.iop_contributors_app.furszy_sdk.android.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.ui.CreateProposalActivity;
import iop.org.iop_contributors_app.ui.ForumActivity;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.voting.VotingProposalActivity;
import iop.org.iop_contributors_app.ui.voting.VotingProposalsActivity;
import iop.org.iop_contributors_app.ui.voting.ui.components.proposals.VotingProposalsHolder;
import iop.org.iop_contributors_app.ui.voting.util.VoteWrapper;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static iop.org.iop_contributors_app.core.iop_sdk.blockchain.utils.CoinUtils.coinToString;
import static iop.org.iop_contributors_app.ui.ProposalSummaryActivity.INTENT_DATA_PROPOSAL;

/**
 * Created by mati on 17/11/16.
 */

public class MyVotesAdapter extends FermatAdapterImproved<VoteWrapper,MyVotesHolder> {

    private WalletModule module;

    public MyVotesAdapter(BaseActivity context, WalletModule module) {
        super(context);
        this.module = module;
    }

    @Override
    protected MyVotesHolder createHolder(View itemView, int type) {
        return new MyVotesHolder(itemView,type);
    }

    @Override
    protected int getCardViewResource(int type) {
        return R.layout.my_votes_row;
    }

    @Override
    protected void bindHolder(MyVotesHolder holder, VoteWrapper data, int position) {
        holder.txt_title.setText(data.getProposal().getTitle());
        holder.txt_forum_id.setText(String.valueOf(data.getProposal().getForumId()));
        holder.txt_sub_title.setText(data.getProposal().getSubTitle());
        holder.txt_categories.setText(data.getProposal().getCategory());
        holder.txt_body.setText(data.getProposal().getBody());
    }


}
