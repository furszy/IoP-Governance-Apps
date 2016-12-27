package iop.org.iop_contributors_app.ui.voting.ui.components.my_votes;

import android.view.View;

import iop.org.iop_contributors_app.R;
import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.voting.util.VoteWrapper;
import iop.org.iop_contributors_app.module.WalletModule;

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
