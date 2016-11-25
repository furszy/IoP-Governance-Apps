package iop.org.iop_contributors_app.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.Proposal;
import iop.org.iop_contributors_app.services.BlockchainService;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;

/**
 * Created by mati on 17/11/16.
 */

public class CreateProposalActivity extends BaseActivity {


    private static final String TAG = "CreateProposalActivity";

    public static final String ACTION_RECEIVE_EXCEPTION = "com.your.package.ACTION_RECEIVE_EXCEPTION";

    public static final String INTENT_DIALOG = "intent_dialog";

    public static final int UNKNOWN_ERROR_DIALOG = 0;
    public static final int INSUFICIENTS_FUNDS_DIALOG = 1;


    private LocalBroadcastManager localBroadcastManager;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                switch (intent.getIntExtra(INTENT_DIALOG,0)){
                    case UNKNOWN_ERROR_DIALOG:
                        showInsuficientFundsException();
                        break;
                    case INSUFICIENTS_FUNDS_DIALOG:
                        showCantSendProposalDialog();
                        break;
                    default:
                        Log.e(TAG,"BroadcastReceiver fail");
                        break;
                }
        }
    };


    // UI
    private View root;

    private EditText edit_title;
    private EditText edit_subtitle;
    private EditText edit_category;
    private EditText edit_body;
    private EditText edit_start_block;
    private EditText edit_end_block;
    private EditText edit_block_reward;
    private EditText edit_beneficiary_address_1;
    private EditText edit_beneficiary_value_1;
    private Button btn_create_proposal;



    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        root = getLayoutInflater().inflate(R.layout.create_proposal_main,container);

        edit_title = (EditText) root.findViewById(R.id.edit_title);
        edit_subtitle = (EditText) root.findViewById(R.id.edit_subtitle);
        edit_category = (EditText) root.findViewById(R.id.edit_category);
        edit_body = (EditText) root.findViewById(R.id.edit_body);
        edit_start_block = (EditText) root.findViewById(R.id.edit_start_block);
        edit_end_block = (EditText) root.findViewById(R.id.edit_end_block);
        edit_block_reward = (EditText) root.findViewById(R.id.edit_block_reward);
        edit_beneficiary_address_1 = (EditText) root.findViewById(R.id.edit_beneficiary_1_address);
        edit_beneficiary_value_1 = (EditText) root.findViewById(R.id.edit_beneficiary_1_value);
        btn_create_proposal = (Button) root.findViewById(R.id.btn_create_proposal);

        btn_create_proposal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            //todo: usar el buildProposal()
                            Proposal proposal = new Proposal();
                            proposal.addBeneficiary("uhk9N8Wpw6HWjitFgfmvdLbgX6voUkDYAb",80000000);
                            proposal.setTitle(buildXmlTitle(proposal.getTitle()));
                            proposal.setBody(buildXmlBody(proposal.getBody()));
                            try {
                                if (module.startDiscussion(proposal)) {
                                    CreateProposalActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            buildDialogSucced();
                                        }
                                    });
                                } else {
                                    CreateProposalActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            buildFailDialog("Fail for some reason");
                                        }
                                    });

                                }
                            }catch (final Exception e){
                                e.printStackTrace();
                                CreateProposalActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        buildFailDialog(e.getMessage());
                                    }
                                });

                            }

//                            Bundle bundle = new Bundle();
//                            bundle.putSerializable(BlockchainService.INTENT_EXTRA_PROPOSAL,proposal);
//                            sendWorkToBlockchainService(BlockchainService.ACTION_BROADCAST_PROPOSAL_TRANSACTION,bundle);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }


    private void buildFailDialog(String message) {
        DialogBuilder dialogBuilder = new DialogBuilder(this);
        dialogBuilder.setTitle("Error");
        dialogBuilder.setMessage(message);
        dialogBuilder.show();
    }

    private void buildDialogSucced(){
        DialogBuilder dialogBuilder = new DialogBuilder(this);
        dialogBuilder.setTitle("Great!");
        dialogBuilder.setMessage("Register completed, please check the email to verify the account");
        dialogBuilder.singleDismissButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(CreateProposalActivity.this,MainActivity.class));
            }
        });

        dialogBuilder.show();
    }

    private Proposal buildProposal(){
        Proposal proposal = new Proposal();

        String title = edit_title.getText().toString();
        String subtitle = edit_subtitle.getText().toString();
        String category = edit_category.getText().toString();
        String body = edit_body.getText().toString();
        int startBlock = Integer.parseInt(edit_start_block.getText().toString());
        int endBlock = Integer.parseInt(edit_end_block.getText().toString());
        long blockReward = Long.parseLong(edit_block_reward.getText().toString());
        //todo: faltan los delirables y las validaciones..


        proposal.setTitle(title);
        proposal.setSubTitle(subtitle);
        proposal.setCategory(category);
        proposal.setBody(body);
        proposal.setStartBlock(startBlock);
        proposal.setEndBlock(endBlock);
        proposal.setBlockReward(blockReward);


        return proposal;

    }

    private String buildXmlTitle(String title){
        return concatenateStrings("<cotract_title>",title,"</contract_title>");
    }

    private String buildXmlBody(String body){
        return concatenateStrings("<cotract_body>",body,"</contract_body>");
    }



    private String concatenateStrings(String tagInit,String text,String tagEnd){
        return tagInit+text+tagEnd;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intent = new IntentFilter(ACTION_RECEIVE_EXCEPTION);
        localBroadcastManager.registerReceiver(receiver,intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            // nothing
        }
    }

    private void showInsuficientFundsException(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Upss");
        alertDialog.setMessage("Insuficient funds, please check your available balance");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

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
}
