package iop.org.voting_app.ui;

import android.os.Bundle;
import android.util.Log;
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
import android.widget.Toast;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar;
import iop.org.iop_contributors_app.ui.dialogs.SimpleDialogs;
import iop.org.voting_app.base.VotingBaseActivity;
import iop.org.voting_app.ui.dialogs.BroadcastVoteDialog;
import iop.org.voting_app.ui.dialogs.CancelLister;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.propose.ProposalUtil;
import iop_sdk.governance.utils.IoPCalculator;
import iop_sdk.governance.vote.Vote;
import iop_sdk.governance.vote.VoteWrapper;

import static iop.org.iop_contributors_app.services.BlockchainService.INTENT_EXTRA_PROPOSAL_VOTE;
import static iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar.Position.CENTER;
import static iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar.Position.LEFT;
import static iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar.Position.RIGHT;
import static iop_sdk.blockchain.utils.CoinUtils.coinToString;
import static iop_sdk.utils.StringUtils.numberToNumberWithDots;
import static org.iop.intents.constants.IntentsConstants.COMMON_ERROR_DIALOG;
import static org.iop.intents.constants.IntentsConstants.INSUFICIENTS_FUNDS_DIALOG;
import static org.iop.intents.constants.IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENTE_EXTRA_MESSAGE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_VOTE_TRANSACTION_SUCCED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_DIALOG;
import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;
import static org.iop.intents.constants.IntentsConstants.INVALID_PROPOSAL_DIALOG;
import static org.iop.intents.constants.IntentsConstants.UNKNOWN_ERROR_DIALOG;

/**
 * Created by mati on 23/12/16.
 */

public class VotingVoteSummary extends VotingBaseActivity implements View.OnClickListener {

    private static final String TAG = "VotingVoteSummary";

    public static final String INTENT_VOTE_WRAPPER = "vote_wrapper";

    private VoteWrapper voteWrapper;

    // UI
    private View root;

    private TextView txt_title;
    private TextView txt_forum_id;
    private TextView txt_sub_title;
    private TextView txt_categories;
    private TextView txt_body;
    private TextView txt_end_date;
    private TextView txt_total_amount;
    private TextView txt_go_cancel;
    private TextView txt_go_vote;
    private View card_bottom_border;

    private ProgressBar progressYes;
    private TextView txt_vote_yes;
    private ProgressBar progressNo;
    private TextView txt_vote_no;

    private TextView txt_vote_result;

    // loading ui
    private View container_send;
    private ProgressBar progressBar;
    private ImageView img_done;
    private TextView txt_done;
    private View container_back;

    /** change vote */
    private int votingAmount;

    private View container_change_vote;
    private SwitchSeekBar seek_bar_switch;
    /** no, neutral, yes */
    private Vote.VoteType voteType = Vote.VoteType.NEUTRAL;

    private Button btn_minus_voting;
    private Button btn_plus_voting;
    private Button btn_plus_x2_voting;
    private Button btn_plus_div_2_voting;
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

            if (voteWrapper.getProposal().isActive()) {
                if (!isChangeVoteExpanded) {
                    expand(container_change_vote, card_bottom_border);
                } else {
                    collapse(container_change_vote, card_bottom_border);
                }

                isChangeVoteExpanded = !isChangeVoteExpanded;
            }
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
        txt_end_date = (TextView) root.findViewById(R.id.txt_end_date);
        txt_total_amount = (TextView) root.findViewById(R.id.txt_total_amount);
        txt_go_cancel = (TextView) root.findViewById(R.id.txt_go_cancel);
        txt_go_vote = (TextView) root.findViewById(R.id.txt_go_vote);
        card_bottom_border = root.findViewById(R.id.card_bottom_border);

        container_change_vote = root.findViewById(R.id.container_change_vote);
        seek_bar_switch = (SwitchSeekBar) root.findViewById(R.id.seek_bar_switch);

        txt_vote_quantity = (EditText) root.findViewById(R.id.txt_vote_quantity);
        btn_minus_voting = (Button) root.findViewById(R.id.btn_minus_voting);
        btn_plus_voting = (Button) root.findViewById(R.id.btn_plus_voting);
        btn_plus_x2_voting = (Button) root.findViewById(R.id.btn_plus_x2_voting);
        btn_plus_div_2_voting = (Button) root.findViewById(R.id.btn_plus_div_2_voting);

        container_send = root.findViewById(R.id.container_send);
        img_done = (ImageView) root.findViewById(R.id.img_done);
        txt_done = (TextView) root.findViewById(R.id.txt_done);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        progressYes = (ProgressBar) root.findViewById(R.id.progress_yes);
        progressNo = (ProgressBar) root.findViewById(R.id.progress_no);

        txt_vote_yes = (TextView) root.findViewById(R.id.txt_vote_yes);
        txt_vote_no = (TextView) root.findViewById(R.id.txt_vote_no);

        txt_vote_result = (TextView) root.findViewById(R.id.txt_vote_result);

        container_back = root.findViewById(R.id.container_back);

        btn_minus_voting.setOnClickListener(this);
        btn_plus_voting.setOnClickListener(this);
        btn_plus_x2_voting.setOnClickListener(this);
        btn_plus_div_2_voting.setOnClickListener(this);
        txt_go_cancel.setOnClickListener(this);
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
        double minutes = ProposalUtil.getEstimatedTimeToContractExecution(voteWrapper.getProposal());
        double hours = minutes/60;
        hours = round(hours,2);
        txt_end_date.setText(String.valueOf(hours)+" hours");
        txt_total_amount.setText("Reward "+coinToString(voteWrapper.getProposal().getBlockReward())+" IoPs");

        long voteNo = voteWrapper.getProposal().getVoteNo();
        long voteYes = voteWrapper.getProposal().getVoteYes();

        long totalValue = voteYes + voteNo;
        int voteYesPorcen = 0;
        int voteNoPorcen = 0;
        if (totalValue!=0) {
            // total -> 100%
            voteYesPorcen = (int) ((voteYes * 100) / totalValue);
            voteNoPorcen = (int) ((voteNo * 100) / totalValue);
        }

        progressNo.setProgress(voteNoPorcen);
        progressYes.setProgress(voteYesPorcen);
        txt_vote_yes.setText(numberToNumberWithDots(voteYes));
        txt_vote_no.setText(numberToNumberWithDots(voteNo));

        if (totalValue>0){
            txt_vote_result.setText((voteYes>voteNo)?"Yes is winning":"No is winning");
        }else {
            txt_vote_result.setText("No votes yet");
        }

        seek_bar_switch.setPosition(getPositionByVoteType(voteWrapper.getVote().getVote()));

        txt_vote_quantity.setText(String.valueOf(voteWrapper.getVote().getVotingPower()));

        if (!voteWrapper.getProposal().isActive()){
            card_bottom_border.setBackgroundResource(R.drawable.gradientecards_rojo);
        }
    }

    private SwitchSeekBar.Position getPositionByVoteType(Vote.VoteType voteType){
        switch (voteType){
            case NEUTRAL:
                return CENTER;
            case NO:
                return LEFT;
            case YES:
                return RIGHT;
            default:
                throw new IllegalArgumentException("Something really bad happen");
        }
    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
        else if (id == R.id.btn_plus_x2_voting){
            if (voteType!= Vote.VoteType.NEUTRAL) {
                votingAmount = votingAmount*2;
                updateVotesAmount();
            }
        }
        else if (id == R.id.btn_plus_div_2_voting){
            if (voteType!= Vote.VoteType.NEUTRAL) {
                votingAmount = votingAmount/2;
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

        votingAmount = Integer.parseInt(txt_vote_quantity.getText().toString());

        long amountIoPToshis = votingAmount;




        if (Transaction.MIN_NONDUST_OUTPUT.isGreaterThan(Coin.valueOf(amountIoPToshis))){
            SimpleDialogs.showErrorDialog(this,"Error", "Votes not allowed, min votes value: "+Transaction.MIN_NONDUST_OUTPUT.getValue());
            hideDoneLoading();
            return;
        }


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
        container_back.setVisibility(View.VISIBLE);
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
        container_back.setVisibility(View.INVISIBLE);
    }


    @Override
    protected boolean onVotingBroadcastReceive(Bundle bundle) {
        if (bundle.containsKey(INTENT_BROADCAST_DATA_TYPE)){
            if (bundle.getString(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_VOTE_TRANSACTION_SUCCED)) {
                Vote vote = (Vote) bundle.getSerializable(INTENT_EXTRA_PROPOSAL_VOTE);
                if (Arrays.equals(vote.getGenesisHash(),this.voteWrapper.getVote().getGenesisHash())) {
                    showDoneLoading();
                }
            }
        }else if(bundle.getString(INTENT_BROADCAST_TYPE).equals(INTENT_DIALOG)){
            switch (bundle.getInt(INTENTE_BROADCAST_DIALOG_TYPE,0)){
                case UNKNOWN_ERROR_DIALOG:
                    String textToShow = "Uknown error, please send log";
                    if (bundle.containsKey(INTENTE_EXTRA_MESSAGE)) {
                        textToShow = bundle.getString(INTENTE_EXTRA_MESSAGE);
                    }
                    SimpleDialogs.showErrorDialog(this, "Upss",textToShow );
                    break;
                case INSUFICIENTS_FUNDS_DIALOG:
                    SimpleDialogs.showInsuficientFundsException(this,module);
                    break;
                case COMMON_ERROR_DIALOG:
                    SimpleDialogs.showErrorDialog(this,"Error", bundle.getString(INTENTE_EXTRA_MESSAGE));
                    break;
                default:
                    Log.e(TAG,"BroadcastReceiver fail");
                    break;
            }
            hideDoneLoading();
        } else if (bundle.containsKey(INTENT_EXTRA_PROPOSAL)) {
            Proposal proposal = (Proposal) bundle.getSerializable(INTENT_EXTRA_PROPOSAL);
            voteWrapper.getProposal().setState(proposal.getState());
        }

        return false;
    }

}
