package iop.org.iop_contributors_app.ui.components;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewTreeObserver;

import org.iop.WalletModule;

import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.CreateProposalActivity;
import iop.org.iop_contributors_app.ui.ForumActivity;
import iop.org.iop_contributors_app.ui.ProposalSummaryActivity;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.utils.StringUtils;

import static iop_sdk.blockchain.utils.CoinUtils.coinToString;
import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;

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
    protected void bindHolder(final ProposalsHolder holder, final Proposal data, int position) {

        holder.txt_title.setText(data.getTitle());
        holder.txt_forum_id.setText(String.valueOf(data.getForumId()));
        holder.txt_sub_title.setText(data.getSubTitle());
        holder.txt_categories.setText(data.getCategory());
        holder.txt_body.setText(data.getBody());
        holder.btn_read_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(context,ProposalSummaryActivity.class);
                intent1.setAction(CreateProposalActivity.ACTION_EDIT_PROPOSAL);
                intent1.putExtra(INTENT_EXTRA_PROPOSAL,data);
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

        holder.txt_state.setText(data.getState().toString().toLowerCase());

        final long voteNo = data.getVoteNo();
        final long voteYes = data.getVoteYes();
        long totalValue = voteYes + voteNo;
        int voteYesPorcen = 0;
        int voteNoPorcen = 0;
        if (totalValue!=0) {
            // total -> 100%
            voteYesPorcen = (int) ((voteYes * 100) / totalValue);
            voteNoPorcen = (int) ((voteNo * 100) / totalValue);
        }

        holder.progressYes.setProgress(voteYesPorcen);
        holder.progressNo.setProgress(voteNoPorcen);
        holder.txt_vote_yes.setText(StringUtils.numberToNumberWithDots(data.getVoteYes()));
        holder.txt_vote_no.setText(StringUtils.numberToNumberWithDots((data.getVoteNo())));

        holder.progress_yes_container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (voteYes>voteNo){
                    holder.txt_vote_no.getLayoutParams().width = holder.txt_vote_yes.getLayoutParams().width;
                    holder.progress_no_container.getLayoutParams().width = holder.progress_yes_container.getLayoutParams().width;
                }
            }
        });

    }


}
