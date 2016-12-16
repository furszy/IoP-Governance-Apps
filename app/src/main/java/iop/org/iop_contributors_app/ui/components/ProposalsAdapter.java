package iop.org.iop_contributors_app.ui.components;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.ui.CreateProposalActivity;
import iop.org.iop_contributors_app.ui.ForumActivity;
import iop.org.iop_contributors_app.furszy_sdk.android.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 17/11/16.
 */

public class ProposalsAdapter extends FermatAdapterImproved<Proposal,ProposalsHolder> {

    WalletModule module;

    public ProposalsAdapter(Context context,WalletModule module) {
        super(context);
        this.module = module;
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
    protected void bindHolder(ProposalsHolder holder, final Proposal data, int position) {

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
        holder.txt_total_amount.setText("Reward "+data.getBlockReward()+" IoPs");
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

        holder.txt_state.setText(data.getState().toString().toLowerCase());
    }


}
