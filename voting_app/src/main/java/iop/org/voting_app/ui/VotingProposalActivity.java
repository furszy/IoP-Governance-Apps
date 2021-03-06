package iop.org.voting_app.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.iop.AppController;
import org.iop.db.CantGetProposalException;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import iop.org.voting_app.R;
import iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar;
import iop.org.iop_contributors_app.ui.dialogs.ReportIssueDialogBuilder;
import iop.org.iop_contributors_app.ui.dialogs.SimpleDialogs;
import iop.org.voting_app.base.VotingBaseActivity;
import iop.org.voting_app.ui.dialogs.BroadcastVoteDialog;
import iop.org.voting_app.ui.dialogs.CancelLister;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.Vote;

import static iop.org.iop_contributors_app.services.BlockchainService.INTENT_EXTRA_PROPOSAL_VOTE;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.INTENT_DATA_FORUM_ID;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.INTENT_DATA_FORUM_TITLE;
import static iop.org.iop_contributors_app.ui.ProposalSummaryActivity.ACTION_SUMMARY_PROPOSAL;
import static iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar.Position.CENTER;
import static iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar.Position.LEFT;
import static iop.org.iop_contributors_app.ui.components.switch_seek_bar.SwitchSeekBar.Position.RIGHT;
import static iop.org.iop_contributors_app.ui.dialogs.SimpleDialogs.buildReportIssueDialogVoting;
import static iop_sdk.blockchain.utils.CoinUtils.coinToString;
import static iop_sdk.governance.propose.Proposal.ProposalState.IN_EXECUTION;
import static iop_sdk.governance.propose.Proposal.ProposalState.QUEUED_FOR_EXECUTION;
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
 * Created by mati on 20/12/16.
 */

public class VotingProposalActivity extends VotingBaseActivity implements View.OnClickListener {


    private static final String TAG = "VotingProposalActivity";

    private Proposal proposal;
    private long votingAmount;
    /**
     * Vote
     */
    private Vote vote;

    private View root;

    TextView txt_title;
    TextView txt_forum_id;
    TextView txt_sub_title;
    TextView txt_categories;
    TextView txt_body;
    TextView txt_start_block;
    TextView txt_end_block;
    TextView txt_total_amount;
    TextView txt_go_forum;
    TextView txt_go_vote;

    private SwitchSeekBar switchSeekBar;
    /**
     * no, neutral, yes
     */
    private Vote.VoteType voteType = Vote.VoteType.NEUTRAL;

    private Button btn_minus_voting;
    private Button btn_plus_voting;
    private EditText edit_vote_quantity;
    private Button btn_plus_x2_voting;
    private Button btn_plus_div_2_voting;

    // loading ui
    private View container_send;
    private ProgressBar progressBar;
    private ImageView img_done;
    private TextView txt_done;

    private AtomicBoolean lockBroadcast = new AtomicBoolean(false);

    private Handler handler = new Handler();


    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {
        super.onCreateView(container, savedInstance);

        setTitle("Vote");

        if (getIntent().getExtras().containsKey(INTENT_EXTRA_PROPOSAL)) {
            proposal = (Proposal) getIntent().getSerializableExtra(INTENT_EXTRA_PROPOSAL);
        } else if (getIntent().getAction().equals(ACTION_SUMMARY_PROPOSAL)) {
            int forumId = getIntent().getIntExtra(INTENT_DATA_FORUM_ID, -1);
            String forumTitle = getIntent().getStringExtra(INTENT_DATA_FORUM_TITLE);
            forumTitle = forumTitle.replace("-", " ");
            Log.i(TAG, "editing mode, title: " + forumTitle + ", id: " + forumId);
            try {
                proposal = module.getProposal(forumId);
            } catch (CantGetProposalException e) {
                e.printStackTrace();
            }
        }

        root = getLayoutInflater().inflate(R.layout.vote_proposal_main, container);

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

        switchSeekBar = (SwitchSeekBar) root.findViewById(R.id.seek_bar_switch);

        edit_vote_quantity = (EditText) root.findViewById(R.id.edit_vote_quantity);
        btn_minus_voting = (Button) root.findViewById(R.id.btn_minus_voting);
        btn_plus_voting = (Button) root.findViewById(R.id.btn_plus_voting);
        btn_plus_x2_voting = (Button) root.findViewById(R.id.btn_plus_x2_voting);
        btn_plus_div_2_voting = (Button) root.findViewById(R.id.btn_plus_div_2_voting);

        btn_minus_voting.setOnClickListener(this);
        btn_plus_voting.setOnClickListener(this);
        btn_plus_x2_voting.setOnClickListener(this);
        btn_plus_div_2_voting.setOnClickListener(this);
        txt_go_forum.setOnClickListener(this);
        txt_go_vote.setOnClickListener(this);

        container_send = root.findViewById(R.id.container_send);
        img_done = (ImageView) root.findViewById(R.id.img_done);
        txt_done = (TextView) root.findViewById(R.id.txt_done);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        switchSeekBar.addSwitchListener(new SwitchSeekBar.SwitchListener() {
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

        loadProposal();
    }

    private void loadProposal() {
        txt_title.setText(proposal.getTitle());
        txt_forum_id.setText(String.valueOf(proposal.getForumId()));
        txt_sub_title.setText(proposal.getSubTitle());
        txt_categories.setText(proposal.getCategory());
        txt_body.setText(proposal.getBody());
        txt_start_block.setText(String.valueOf(proposal.getStartBlock()));
        txt_end_block.setText(String.valueOf(proposal.getEndBlock()));
        txt_total_amount.setText("Reward " + coinToString(proposal.getBlockReward()) + " IoPs");

        vote = module.getVote(proposal.getGenesisTxHash());
        if (vote != null) {
            voteType = vote.getVote();
            switchSeekBar.setPosition(getPositionByVoteType(vote.getVote()));
            edit_vote_quantity.setText(String.valueOf(vote.getVotingPower()));
        }
    }

    private SwitchSeekBar.Position getPositionByVoteType(Vote.VoteType voteType) {
        switch (voteType) {
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

    @Override
    protected boolean onVotingBroadcastReceive(Bundle bundle) {

        if (bundle.containsKey(INTENT_BROADCAST_DATA_TYPE)) {
            if (bundle.getString(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_VOTE_TRANSACTION_SUCCED)) {
                Vote vote = (Vote) bundle.getSerializable(INTENT_EXTRA_PROPOSAL_VOTE);
                if (Arrays.equals(vote.getGenesisHash(), this.vote.getGenesisHash())) {
                    showDoneLoading();
                    lockBroadcast.set(false);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(VotingProposalActivity.this, VotingMyVotesActivity.class));
                            finish();
                        }
                    }, TimeUnit.SECONDS.toMillis(3));
                }
            }
        } else if (bundle.getString(INTENT_BROADCAST_TYPE).equals(INTENT_DIALOG)) {
            switch (bundle.getInt(INTENTE_BROADCAST_DIALOG_TYPE, 0)) {
                case UNKNOWN_ERROR_DIALOG:
                    String textToShow = "Uknown error, please send log";
                    if (bundle.containsKey(INTENTE_EXTRA_MESSAGE)) {
                        textToShow = bundle.getString(INTENTE_EXTRA_MESSAGE);
                    }
                    SimpleDialogs.showErrorDialog(this, "Upss", textToShow);

                    ReportIssueDialogBuilder reportIssueDialog = buildReportIssueDialogVoting(this, (AppController) getApplication(),module,R.string.upss,R.string.upss_message);
                    reportIssueDialog.show();

                    break;
                case INSUFICIENTS_FUNDS_DIALOG:
                    SimpleDialogs.showInsuficientFundsException(this, module);
                    break;
                case INVALID_PROPOSAL_DIALOG:
                    loadProposal();
                    SimpleDialogs.showErrorDialog(this, "Error", bundle.getString(INTENTE_EXTRA_MESSAGE));
                    break;
                case COMMON_ERROR_DIALOG:
                    SimpleDialogs.showErrorDialog(this, "Error", bundle.getString(INTENTE_EXTRA_MESSAGE));
                    break;
                default:
                    Log.e(TAG, "BroadcastReceiver fail");
                    break;
            }
            hideDoneLoading();
            lockBroadcast.set(false);
        }

        return false;
    }

    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_minus_voting) {
            if (votingAmount != 0 && voteType != Vote.VoteType.NEUTRAL) {
                updateVotingPower();
                votingAmount--;
                updateVotesAmount();
            }
        } else if (id == R.id.btn_plus_voting) {
            if (voteType != Vote.VoteType.NEUTRAL) {
                updateVotingPower();
                votingAmount++;
                updateVotesAmount();
            }
        } else if (id == R.id.btn_plus_x2_voting) {
            if (voteType != Vote.VoteType.NEUTRAL) {
                updateVotingPower();
                votingAmount = votingAmount * 2;
                updateVotesAmount();
            }
        } else if (id == R.id.btn_plus_div_2_voting) {
            if (voteType != Vote.VoteType.NEUTRAL) {
                updateVotingPower();
                votingAmount = votingAmount / 2;
                updateVotesAmount();
            }
        } else if (id == R.id.txt_go_forum) {
            onBackPressed();
        } else if (id == R.id.txt_go_vote) {
            handleSend();
        }
    }

    private void handleSend() {
        if (lockBroadcast.compareAndSet(false, true)) {
            showBroadcastDialog();
        } else
            Log.e(TAG, "Tocó dos veces..");
    }

    private void updateVotesAmount() {
        if (voteType == Vote.VoteType.YES)
            edit_vote_quantity.setText(String.valueOf(votingAmount));
        else if (voteType == Vote.VoteType.NO) {
            edit_vote_quantity.setText(String.valueOf(votingAmount));
        } else if (voteType == Vote.VoteType.NEUTRAL) {
            edit_vote_quantity.setText("0");
        }
    }

    private void updateVotingPower(){
        votingAmount = Long.parseLong(edit_vote_quantity.getText().toString());
    }

    private void showBroadcastDialog() {
        // loading
        preparateLoading("Vote sent!", R.drawable.icon_done);

        updateVotingPower();

        long amountIoPToshis = votingAmount; //IoPCalculator.iopToIopToshis(votingAmount);

        vote = new Vote(proposal.getGenesisTxHash(), voteType, amountIoPToshis);

        if (!proposal.isActive()) {
            final AlertDialog alertDialog = SimpleDialogs.buildDialog(this, "Update", "Proposal is not more active, state: " + proposal.getState().toString(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    onBackPressed();
                }
            });
            alertDialog.show();
            unlockAndHideLoading();
            return;
        }


        if (Transaction.MIN_NONDUST_OUTPUT.isGreaterThan(Coin.valueOf(amountIoPToshis))) {
            SimpleDialogs.showErrorDialog(this, "Error", "Votes not allowed, min votes value: " + Transaction.MIN_NONDUST_OUTPUT.getValue());
            unlockAndHideLoading();
            return;
        }

        // check if the proposal is in a valid state
        if (voteType == Vote.VoteType.YES && (proposal.getState() == QUEUED_FOR_EXECUTION || proposal.getState() == IN_EXECUTION)) {
            Toast.makeText(this, "No more positive votes are allowed\nProposal is already in execution", Toast.LENGTH_LONG).show();
            unlockAndHideLoading();
            return;
        }

        if (checkVote(vote)) {
            BroadcastVoteDialog.newinstance(module, vote).setCancelListener(new CancelLister() {
                @Override
                public void cancel() {
                    hideDoneLoading();
                    lockBroadcast.set(false);
                }

                @Override
                public void onDismiss(boolean isActionOk) {
                    if (!isActionOk) {
                        hideDoneLoading();
                        lockBroadcast.set(false);
                    }
                }
            }).show(getSupportFragmentManager(), "broadcastContractDialog");
        } else {
            Toast.makeText(this, "Vote exist, app works great!\nFurszy :)", Toast.LENGTH_LONG).show();
            hideDoneLoading();
            lockBroadcast.set(false);
        }
    }

    private void unlockAndHideLoading() {
        lockBroadcast.set(false);
        hideDoneLoading();
    }

    private boolean checkVote(Vote vote) {
        return !module.checkIfVoteExist(vote);
    }

    private void preparateLoading(String textDone, int resImgDone) {
        container_send.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        txt_done.setText(textDone);
        img_done.setImageResource(resImgDone);
        txt_done.setVisibility(View.INVISIBLE);
        img_done.setVisibility(View.INVISIBLE);
    }

    private void showDoneLoading() {
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

    private void hideDoneLoading() {
        container_send.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
