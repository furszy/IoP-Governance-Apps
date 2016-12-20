package iop.org.iop_contributors_app.ui.voting.ui.components;

import android.content.Intent;
import android.view.View;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.ui.CreateProposalActivity;
import iop.org.iop_contributors_app.ui.ForumActivity;
import iop.org.iop_contributors_app.furszy_sdk.android.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.ui.voting.VotingProposalsActivity;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static iop.org.iop_contributors_app.core.iop_sdk.blockchain.utils.CoinUtils.coinToString;

/**
 * Created by mati on 17/11/16.
 */

public class VotingProposalsAdapter extends FermatAdapterImproved<Proposal,VotingProposalsHolder> {

    private WalletModule module;

    public VotingProposalsAdapter(VotingProposalsActivity context, WalletModule module) {
        super(context);
        this.module = module;
    }

    @Override
    protected VotingProposalsHolder createHolder(View itemView, int type) {
        return new VotingProposalsHolder(itemView,type);
    }

    @Override
    protected int getCardViewResource(int type) {
        return R.layout.voting_proposal_row;
    }

    @Override
    protected void bindHolder(VotingProposalsHolder holder, final Proposal data, int position) {

        holder.txt_title.setText(data.getTitle());
        holder.txt_forum_id.setText(String.valueOf(data.getForumId()));
        holder.txt_sub_title.setText(data.getSubTitle());
        holder.txt_categories.setText(data.getCategory());
        holder.txt_body.setText(data.getBody());
        holder.btn_read_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String forumTitle = data.getTitle();
                int forumId = data.getForumId();
                Intent intent1 = new Intent(context,CreateProposalActivity.class);
                intent1.setAction(CreateProposalActivity.ACTION_EDIT_PROPOSAL);
                intent1.putExtra(CreateProposalActivity.INTENT_DATA_FORUM_ID,Integer.valueOf(forumId));
                intent1.putExtra(CreateProposalActivity.INTENT_DATA_FORUM_TITLE,forumTitle);
                context.startActivity(intent1);
            }
        });
        holder.txt_start_block.setText(String.valueOf(data.getStartBlock()));
        holder.txt_end_block.setText(String.valueOf(data.getEndBlock()));
        holder.txt_total_amount.setText("Reward "+coinToString(data.getBlockReward())+" IoPs");
        holder.txt_go_forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // posts http://fermat.community/t/propuesta-numero-4/19
                Intent intent1 = new Intent(context,ForumActivity.class);
                String url = module.getForumUrl()+"/t/"+data.getTitle().replace(" ","-")+"/"+data.getForumId();
                intent1.putExtra(ForumActivity.INTENT_URL,url);
                context.startActivity(intent1);

            }
        });
        holder.txt_go_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((VotingProposalsActivity)context).showVoteDialog(data);
            }
        });
    }


}
