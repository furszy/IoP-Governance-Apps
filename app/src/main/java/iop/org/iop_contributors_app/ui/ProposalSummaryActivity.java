package iop.org.iop_contributors_app.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.governance.propose.Proposal;
import iop.org.iop_contributors_app.furszy_sdk.android.mine.SizeUtils;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.BroadcastContractDialog;
import iop.org.iop_contributors_app.ui.dialogs.CancelLister;
import iop.org.iop_contributors_app.ui.dialogs.SimpleDialogs;
import iop.org.iop_contributors_app.ui.dialogs.wallet.InsuficientFundsDialog;
import iop.org.iop_contributors_app.wallet.db.CantGetProposalException;

import static iop.org.iop_contributors_app.core.iop_sdk.blockchain.utils.CoinUtils.coinToString;
import static iop.org.iop_contributors_app.core.iop_sdk.utils.TextUtils.transformToHtmlWithColor;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.CANT_SAVE_PROPOSAL_DIALOG;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.COMMON_ERROR_DIALOG;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INSUFICIENTS_FUNDS_DIALOG;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TRANSACTION_SUCCED;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_DIALOG;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INVALID_PROPOSAL_DIALOG;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.UNKNOWN_ERROR_DIALOG;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.INTENT_DATA_FORUM_ID;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.INTENT_DATA_FORUM_TITLE;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.INTENT_EXTRA_MESSAGE_DIALOG;
import static iop.org.iop_contributors_app.ui.dialogs.SimpleDialogs.showErrorDialog;

/**
 * Created by mati on 16/12/16.
 */

public class ProposalSummaryActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ProposalSummaryActivity";

    public static final String ACTION_PROPOSAL = "action_proposal";
    public static final String INTENT_DATA_PROPOSAL = "proposal";
    public static final String ACTION_SUMMARY_PROPOSAL = "summary_proposal";

    private Proposal proposal;

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
    private TextView txt_forum;
    private LinearLayout containerBeneficiaries;

    private Button btn_broadcast_proposal;
//    private ImageButton btn_edit;
    private View container_arrow;
    // loading ui
    private View container_send;
    private ProgressBar progressBar;
    private ImageView img_done;
    private TextView txt_done;

    /** broadcast flag */
    private AtomicBoolean lockBroadcast = new AtomicBoolean(false);

//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, final Intent intent) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
//
//        }
//    };

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {
        super.onCreateView(container, savedInstance);


        if (getIntent().getExtras().containsKey(INTENT_DATA_PROPOSAL)) {
            proposal = (Proposal) getIntent().getSerializableExtra(INTENT_DATA_PROPOSAL);
        }else if (getIntent().getAction().equals(ACTION_SUMMARY_PROPOSAL)){
            int forumId = getIntent().getIntExtra(INTENT_DATA_FORUM_ID, -1);
            String forumTitle = getIntent().getStringExtra(INTENT_DATA_FORUM_TITLE);
            forumTitle = forumTitle.replace("-"," ");
            Log.i(TAG,"editing mode, title: "+forumTitle+", id: "+forumId);
            try {
                proposal = module.getProposal(forumId);
            } catch (CantGetProposalException e) {
                e.printStackTrace();
            }
        }


        root = getLayoutInflater().inflate(R.layout.proposal_summary_main,container);

        txt_title = (TextView) root.findViewById(R.id.txt_title);
        txt_forum_id = (TextView) root.findViewById(R.id.txt_forum_id);
        txt_sub_title = (TextView) root.findViewById(R.id.txt_sub_title);
        txt_categories = (TextView) root.findViewById(R.id.txt_categories);
        txt_body = (TextView) root.findViewById(R.id.txt_body);
        txt_start_block = (TextView) root.findViewById(R.id.txt_start_block);
        txt_end_block = (TextView) root.findViewById(R.id.txt_end_block);
        txt_total_amount = (TextView) root.findViewById(R.id.txt_total_amount);
        txt_forum = (TextView) root.findViewById(R.id.txt_forum);
        containerBeneficiaries = (LinearLayout) root.findViewById(R.id.beneficiaries_container);

        btn_broadcast_proposal = (Button) root.findViewById(R.id.btn_broadcast_proposal);
//        btn_edit = (ImageButton) root.findViewById(R.id.btn_edit);
        container_arrow = root.findViewById(R.id.container_arrow);

        container_send = root.findViewById(R.id.container_send);
        img_done = (ImageView) root.findViewById(R.id.img_done);
        txt_done = (TextView) root.findViewById(R.id.txt_done);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);


        container_arrow.setAnimation(AnimationUtils.loadAnimation(this, iop.org.furszy_lib.R.anim.float_anim));

        txt_forum.setOnClickListener(this);
        btn_broadcast_proposal.setOnClickListener(this);

        loadProposal();

        if (proposal.isSent()){
            proposalSent();
        }else {
            btn_broadcast_proposal.setText("Broadcast");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//        IntentFilter intent = new IntentFilter(ACTION_RECEIVE_EXCEPTION);
//        localBroadcastManager.registerReceiver(receiver,intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        try {
//            localBroadcastManager.unregisterReceiver(receiver);
//        } catch (Exception e) {
//            // nothing
//        }
    }

    private void loadProposal(){
        txt_title.setText(proposal.getTitle());
        txt_forum_id.setText(String.valueOf(proposal.getForumId()));
        txt_sub_title.setText(proposal.getSubTitle());
        txt_categories.setText(proposal.getCategory());
        txt_body.setText(proposal.getBody());
        txt_start_block.setText(Html.fromHtml(transformToHtmlWithColor("Start block: ","#cccccc")+transformToHtmlWithColor(String.valueOf(proposal.getStartBlock()),"#ffffff")));
        txt_end_block.setText(Html.fromHtml(transformToHtmlWithColor("End block: ","#cccccc")+transformToHtmlWithColor(String.valueOf(proposal.getEndBlock()),"#ffffff")));
        loadBeneficiaries(proposal.getBeneficiaries());
        txt_total_amount.setText("Total: "+coinToString(proposal.getBlockReward()*proposal.getEndBlock())+" IoPs");

    }

    private void loadBeneficiaries(Map<String,Long> beneficiaries){
        for (Map.Entry<String, Long> beneficiary : beneficiaries.entrySet()) {
            TextView textView = new TextView(this);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(SizeUtils.convertDpToPx(getResources(),5));
            String first = coinToString(beneficiary.getValue())+" IoPs "+ "<font color='#EE0000'> -> </font>"+" "+beneficiary.getKey();
            textView.setText(Html.fromHtml(first));
            containerBeneficiaries.addView(textView);
        }
    }



    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        if (data.containsKey(INTENT_BROADCAST_DATA_TYPE)) {
            if (data.getString(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_TRANSACTION_SUCCED)) {
                lockBroadcast.set(false);
                showDoneLoading();
                Toast.makeText(this, "Proposal broadcasted!, publishing in the forum..", Toast.LENGTH_SHORT).show();
            }
        } else if(data.getString(INTENTE_BROADCAST_DIALOG_TYPE).equals(INTENT_DIALOG)){
            switch (data.getInt(INTENTE_BROADCAST_DIALOG_TYPE,0)){
                case UNKNOWN_ERROR_DIALOG:
                    showCantSendProposalDialog();
                    break;
                case INSUFICIENTS_FUNDS_DIALOG:
                    SimpleDialogs.showInsuficientFundsException(this,module);
                    break;
                case CANT_SAVE_PROPOSAL_DIALOG:
                    showErrorDialog(this,"Error", data.getString(INTENT_EXTRA_MESSAGE_DIALOG));
                    break;
                case INVALID_PROPOSAL_DIALOG:
                    loadProposal();
                    showErrorDialog(this,"Error",data.getString(INTENT_EXTRA_MESSAGE_DIALOG));
                    break;
                case COMMON_ERROR_DIALOG:
                    showErrorDialog(this,"Error", data.getString(INTENT_EXTRA_MESSAGE_DIALOG));
                    break;
                default:
                    Log.e(TAG,"BroadcastReceiver fail");
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
        if (id == R.id.btn_broadcast_proposal){
            if (!proposal.isSent()) {
                if (lockBroadcast.compareAndSet(false, true)) {

                    showBroadcastDialog();

                } else
                    Log.e(TAG, "Toco dos veces el broadcast..");
            }else {
                Toast.makeText(v.getContext(),"Method not implemented yet\nThanks for use the app!!",Toast.LENGTH_LONG).show();
            }
        } else if(id == R.id.txt_forum){
            Log.i(TAG,"redirecting to forum");
            redirectToForum(proposal);
        }

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
                proposalSent();
            }
        });
    }

    private void hideDoneLoading(){
        container_send.setVisibility(View.INVISIBLE);
    }

    private void proposalSent() {
        btn_broadcast_proposal.setText("Cancel");
    }


    private void redirectToForum(Proposal proposal) {
        Intent intent1 = new Intent(this,ForumActivity.class);
        String url = module.getForumUrl()+"/t/"+proposal.getTitle().toLowerCase().replace(" ","-")+"/"+proposal.getForumId();
        intent1.putExtra(ForumActivity.INTENT_URL,url);
        startActivity(intent1);
    }

    private void showBroadcastDialog() {
        // loading
        preparateLoading("Proposal broadcasted!", R.drawable.icon_done);
        BroadcastContractDialog.newinstance(module,proposal).setCancelListener(new CancelLister() {
            @Override
            public void cancel() {
                hideDoneLoading();
            }
        }).show(getSupportFragmentManager(),"broadcastContractDialog");
    }

    private void showCantSendProposalDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Upss");
        alertDialog.setMessage("Something in your proposal is wrong, please try again");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static String getErrorsFromJson(String json){
        StringBuilder formatedStr = new StringBuilder();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = (JSONArray) jsonObject.get("errors");
            for (int i=0;i<jsonArray.length();i++){
                formatedStr.append(jsonArray.get(i));
                if (jsonArray.length()-1!=i)
                    formatedStr.append("\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String result = formatedStr.toString();
        return result.equals("")?json:formatedStr.toString();
    }



}
