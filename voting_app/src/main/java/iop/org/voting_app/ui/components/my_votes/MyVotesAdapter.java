package iop.org.voting_app.ui.components.my_votes;

import android.view.View;

import org.iop.WalletModule;

import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.furszy_lib.adapter.FermatListItemListeners;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.utils.ForumUtils;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.VoteWrapper;

/**
 * Created by mati on 17/11/16.
 */

public class MyVotesAdapter extends FermatAdapterImproved<VoteWrapper,MyVotesHolder> {

    private WalletModule module;
    private FermatListItemListeners<VoteWrapper> onEventListeners;

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
    protected void bindHolder(MyVotesHolder holder, final VoteWrapper data, final int position) {
        String title = data.getProposal().getTitle();
        if (title.length()>29){
            title = title.substring(0,30)+"...";
        }
        holder.txt_title.setText(title);
        holder.txt_forum_id.setText(String.valueOf(data.getProposal().getForumId()));
        holder.txt_sub_title.setText(data.getProposal().getSubTitle());
        holder.txt_categories.setText(data.getProposal().getCategory());
        holder.txt_body.setText(data.getProposal().getBody());
        holder.txt_read_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEventListeners.onItemClickListener(data,position);
            }
        });
        holder.txt_forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForumUtils.goToFoum(v.getContext(),module,data.getProposal());
            }
        });
        if (!data.getProposal().isActive()){
            holder.view_proposal_state.setBackgroundResource(R.drawable.gradientecards_rojo);
        }

        holder.txt_state.setText(data.getProposal().getState().toString());
    }

    public void setFermatListEventListener(FermatListItemListeners<VoteWrapper> onEventListeners) {
        this.onEventListeners=onEventListeners;
    }


}
