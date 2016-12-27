package iop.org.iop_contributors_app.ui.voting;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.voting.base.VotingBaseActivity;
import iop.org.iop_contributors_app.ui.voting.util.VoteWrapper;

import static iop_sdk.blockchain.utils.CoinUtils.coinToString;

/**
 * Created by mati on 23/12/16.
 */

public class VotingVoteSummary extends VotingBaseActivity {

    private static final String LOG = "VotingVoteSummary";

    public static final String INTENT_VOTE_WRAPPER = "vote_wrapper";
    
    private VoteWrapper voteWrapper;

    // UI
    private View root;
    private View container_change_vote;

    private TextView txt_title;
    private TextView txt_forum_id;
    private TextView txt_sub_title;
    private TextView txt_categories;
    private TextView txt_body;
    TextView txt_start_block;
    TextView txt_end_block;
    TextView txt_total_amount;
    TextView txt_go_forum;
    TextView txt_go_vote;

    @Override
    protected void onCreateView(final ViewGroup container, Bundle savedInstance) {
        super.onCreateView(container, savedInstance);

        voteWrapper = (VoteWrapper) getIntent().getExtras().getSerializable(INTENT_VOTE_WRAPPER);

        root = getLayoutInflater().inflate(R.layout.vote_summary_main,container);

        txt_title = (TextView) root.findViewById(R.id.txt_title);
        txt_forum_id = (TextView) root.findViewById(R.id.txt_forum_id);
        txt_sub_title = (TextView) root.findViewById(R.id.txt_sub_title);
        txt_categories = (TextView) root.findViewById(R.id.txt_categories);
        txt_body = (TextView) root.findViewById(R.id.txt_body);
        txt_start_block = (TextView) root.findViewById(R.id.txt_start_block);
        txt_end_block = (TextView) root.findViewById(R.id.txt_end_block);
        txt_total_amount = (TextView) root.findViewById(R.id.txt_total_amount);
        txt_go_forum = (TextView) root.findViewById(R.id.txt_go_forum);
        txt_go_vote = (TextView) root.findViewById(R.id.txt_go_vote);

        container_change_vote = root.findViewById(R.id.container_change_vote);

        root.findViewById(R.id.txt_change_vote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (container_change_vote.getVisibility()!=View.VISIBLE) {
//                    container_change_vote.setVisibility(View.VISIBLE);
                    expand(container_change_vote);
                }else {
                    collapse(container_change_vote);
                }
            }
        });
        
        loadVote();

    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private void loadVote() {
        txt_title.setText(voteWrapper.getProposal().getTitle());
        txt_forum_id.setText(String.valueOf(voteWrapper.getProposal().getForumId()));
        txt_sub_title.setText(voteWrapper.getProposal().getSubTitle());
        txt_categories.setText(voteWrapper.getProposal().getCategory());
        txt_body.setText(voteWrapper.getProposal().getBody());
        txt_start_block.setText(String.valueOf(voteWrapper.getProposal().getStartBlock()));
        txt_end_block.setText(String.valueOf(voteWrapper.getProposal().getEndBlock()));
        txt_total_amount.setText("Reward "+coinToString(voteWrapper.getProposal().getBlockReward())+" IoPs");

    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected boolean hasDrawer() {
        return true;
    }
}
