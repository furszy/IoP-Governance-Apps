package iop.org.voting_app.ui.components.proposals;

import android.content.Intent;
import android.view.View;

import org.bitcoinj.utils.BtcFormat;
import org.iop.ForumHelper;
import org.iop.WalletModule;

import java.util.List;
import java.util.Locale;

import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.R;
import iop.org.voting_app.ui.ForumActivity;
import iop.org.voting_app.ui.VotingProposalsActivity;
import iop_sdk.governance.propose.Proposal;

import static iop_sdk.blockchain.utils.CoinUtils.coinToString;

/**
 * Created by mati on 17/11/16.
 */

public class VotingProposalsAdapter extends FermatAdapterImproved<Proposal,VotingProposalsHolder> {

    private WalletModule module;

    private VoteClickListener onVoteTouchedListener;

    public VotingProposalsAdapter(VotingProposalsActivity context, WalletModule module,List<Proposal> proposalList,VoteClickListener onVoteTouchedListener) {
        super(context,proposalList);
        this.module = module;
        this.onVoteTouchedListener = onVoteTouchedListener;
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
    protected void bindHolder(VotingProposalsHolder holder, final Proposal data, final int position) {

        holder.txt_title.setText(data.getTitle());
        holder.txt_forum_id.setText(String.valueOf(data.getForumId()));
        holder.txt_sub_title.setText(data.getSubTitle());
        holder.txt_categories.setText(data.getCategory());
        holder.txt_body.setText(data.getBody());
        holder.btn_read_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String forumTitle = data.getTitle();
//                int forumId = data.getForumId();
//                Intent intent1 = new Intent(context,CreateProposalActivity.class);
//                intent1.setAction(CreateProposalActivity.ACTION_EDIT_PROPOSAL);
//                intent1.putExtra(CreateProposalActivity.INTENT_DATA_FORUM_ID,Integer.valueOf(forumId));
//                intent1.putExtra(CreateProposalActivity.INTENT_DATA_FORUM_TITLE,forumTitle);
//                context.startActivity(intent1);
                //Toast.makeText(context,"Aqui deberia mostrar todo el texto si es que lo tiene o esconder el boton si no tiene..",Toast.LENGTH_LONG).show();
            }
        });
        holder.btn_read_more.setVisibility(View.INVISIBLE);
        holder.txt_start_block.setText(String.valueOf(data.getStartBlock()));
        holder.txt_end_block.setText(String.valueOf(data.getEndBlock()));
        holder.txt_total_amount.setText("Reward "+BtcFormat.getInstance(Locale.GERMAN).format(data.getBlockReward(),2).replace("BTC","IoP"));
        holder.txt_go_forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // posts http://fermat.community/t/propuesta-numero-4/19
                Intent intent1 = new Intent(context,ForumActivity.class);
                String url = ForumHelper.getForumUrl(module,data);
                intent1.putExtra(ForumActivity.INTENT_URL,url);
                context.startActivity(intent1);

            }
        });
        holder.txt_state.setText(data.getState().toString());
        if (!data.isActive()) {
            holder.txt_go_vote.setVisibility(View.GONE);
            holder.view_btns_divider.setVisibility(View.GONE);
            holder.view_proposal_state.setBackgroundResource(R.drawable.gradientecards_rojo);
        }else {
            holder.txt_go_vote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onVoteTouchedListener.goVote(data,position);
                }
            });
            holder.txt_go_vote.setVisibility(View.VISIBLE);
            holder.view_btns_divider.setVisibility(View.VISIBLE);
            holder.view_proposal_state.setBackgroundResource(R.drawable.gradientecards_verde);
        }
    }


}
