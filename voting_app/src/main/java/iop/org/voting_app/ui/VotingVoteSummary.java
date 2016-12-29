package iop.org.voting_app.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar;
import iop.org.voting_app.base.VotingBaseActivity;
import iop.org.voting_app.ui.dialogs.BroadcastVoteDialog;
import iop.org.voting_app.ui.dialogs.CancelLister;
import iop_sdk.governance.utils.IoPCalculator;
import iop_sdk.governance.vote.Vote;
import iop_sdk.governance.vote.VoteWrapper;

import static iop_sdk.blockchain.utils.CoinUtils.coinToString;

/**
 * Created by mati on 23/12/16.
 */

public class VotingVoteSummary extends VotingBaseActivity implements View.OnClickListener {

    private static final String LOG = "VotingVoteSummary";

    public static final String INTENT_VOTE_WRAPPER = "vote_wrapper";
    
    private VoteWrapper voteWrapper;

    // UI
    private View root;

    private TextView txt_title;
    private TextView txt_forum_id;
    private TextView txt_sub_title;
    private TextView txt_categories;
    private TextView txt_body;
    private TextView txt_start_block;
    private TextView txt_end_block;
    private TextView txt_total_amount;
    private TextView txt_go_forum;
    private TextView txt_go_vote;
    private View card_bottom_border;

    // loading ui
    private View container_send;
    private ProgressBar progressBar;
    private ImageView img_done;
    private TextView txt_done;

    /** change vote */
    private int votingAmount;

    private View container_change_vote;
    private SwitchSeekBar seek_bar_switch;
    /** no, neutral, yes */
    private Vote.VoteType voteType = Vote.VoteType.NEUTRAL;

    private Button btn_minus_voting;
    private Button btn_plus_voting;
    private EditText txt_vote_quantity;

    private boolean isChangeVoteExpanded;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.summary_voting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.id_change_vote){

            if (!isChangeVoteExpanded){
                expand(container_change_vote,card_bottom_border);
            }else {
                collapse(container_change_vote,card_bottom_border);
            }

            isChangeVoteExpanded = !isChangeVoteExpanded;

            return true;
        }

        return false;
    }

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
        card_bottom_border = root.findViewById(R.id.card_bottom_border);

        container_change_vote = root.findViewById(R.id.container_change_vote);
        seek_bar_switch = (SwitchSeekBar) root.findViewById(R.id.seek_bar_switch);

        txt_vote_quantity = (EditText) root.findViewById(R.id.txt_vote_quantity);
        btn_minus_voting = (Button) root.findViewById(R.id.btn_minus_voting);
        btn_plus_voting = (Button) root.findViewById(R.id.btn_plus_voting);

        btn_minus_voting.setOnClickListener(this);
        btn_plus_voting.setOnClickListener(this);
        txt_go_forum.setOnClickListener(this);
        txt_go_vote.setOnClickListener(this);

//        root.findViewById(R.id.txt_change_vote).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (container_change_vote.getVisibility()!=View.VISIBLE) {
////                    container_change_vote.setVisibility(View.VISIBLE);
//                    expand(container_change_vote);
//                }else {
//                    collapse(container_change_vote);
//                }
//            }
//        });

        seek_bar_switch.addSwitchListener(new SwitchSeekBar.SwitchListener() {
            @Override
            public void handleLeft() {
                voteType = Vote.VoteType.NO;
                updateVotesAmount();
            }

            @Override
            public void handleRight() {
                voteType = Vote.VoteType.YES;
                updateVotesAmount();
            }

            @Override
            public void handleCenter() {
                voteType = Vote.VoteType.NEUTRAL;
            }
        });
        
        loadVote();

    }

    public static void collapse(final View v, final View... views) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                    if (views!=null){
                        for (View view : views) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
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

    public static void expand(final View v, final View... views) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);

        if (views!=null){
            for (View view : views) {
                view.setVisibility(View.GONE);
            }
        }

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

    private void updateVotesAmount(){
        if (voteType== Vote.VoteType.YES)
            txt_vote_quantity.setText(String.valueOf(votingAmount));
        else if (voteType== Vote.VoteType.NO){
            txt_vote_quantity.setText(String.valueOf(votingAmount*5));
        }
        else if (voteType == Vote.VoteType.NEUTRAL){
            txt_vote_quantity.setText("0");
        }
    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_minus_voting){
            if (votingAmount!=0 && voteType!= Vote.VoteType.NEUTRAL){
                votingAmount--;
                updateVotesAmount();
            }
        }
        else if (id == R.id.btn_plus_voting){
            if (voteType!= Vote.VoteType.NEUTRAL) {
                votingAmount++;
                updateVotesAmount();
            }
        }
        else if (id == R.id.txt_go_cancel){
            collapse(container_change_vote,card_bottom_border);
        }
        else if (id == R.id.txt_go_vote){
            showBroadcastDialog();
        }
    }

    private void showBroadcastDialog() {
        // loading
        preparateLoading("Vote sent!", R.drawable.icon_done);

        long amountIoPToshis = IoPCalculator.iopToIopToshis(votingAmount);

        Vote vote = voteWrapper.getVote();
        vote.setVoteType(voteType);
        vote.setAmount(amountIoPToshis);

        BroadcastVoteDialog.newinstance(module,vote).setCancelListener(new CancelLister() {
            @Override
            public void cancel() {
                hideDoneLoading();
            }

            @Override
            public void onDismiss(boolean isActionOk) {
                if(!isActionOk){
                    hideDoneLoading();
                }
            }
        }).show(getSupportFragmentManager(),"broadcastContractDialog");

    }

    private void preparateLoading(String textDone, int resImgDone){
        container_send.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        txt_done.setText(textDone);
        img_done.setImageResource(resImgDone);
        txt_done.setVisibility(View.INVISIBLE);
        img_done.setVisibility(View.INVISIBLE);
    }

    private void showDoneLoading(){
        progressBar.setVisibility(View.INVISIBLE);
        txt_done.setVisibility(View.VISIBLE);
        img_done.setVisibility(View.VISIBLE);
        container_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDoneLoading();
            }
        });
    }

    private void hideDoneLoading(){
        container_send.setVisibility(View.INVISIBLE);
    }
}
