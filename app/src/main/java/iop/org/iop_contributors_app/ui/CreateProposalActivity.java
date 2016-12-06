package iop.org.iop_contributors_app.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.forum.CantCreateTopicException;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.services.BlockchainService;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.ui.validators.CreateProposalActivityValidator;
import iop.org.iop_contributors_app.ui.validators.ValidationException;
import iop.org.iop_contributors_app.utils.Cache;
import iop.org.iop_contributors_app.wallet.db.CantGetProposalException;
import iop.org.iop_contributors_app.wallet.db.CantSaveProposalExistException;
import iop.org.iop_contributors_app.wallet.db.CantUpdateProposalException;

/**
 * Created by mati on 17/11/16.
 */

public class CreateProposalActivity extends BaseActivity {


    private static final String TAG = "CreateProposalActivity";

    public static final String ACTION_RECEIVE_EXCEPTION = "com.your.package.ACTION_RECEIVE_EXCEPTION";

    public static final String INTENT_DIALOG = "intent_dialog";

    // dialogs
    public static final String INTENT_EXTRA_MESSAGE_DIALOG = "extraDialogMessage";

    public static final int UNKNOWN_ERROR_DIALOG = 0;
    public static final int INSUFICIENTS_FUNDS_DIALOG = 1;
    public static final int CANT_SAVE_PROPOSAL_DIALOG = 2;
    public static final int COMMON_ERROR_DIALOG = 3;

    // action edit
    public static final String ACTION_EDIT_PROPOSAL = "actionEditProp";
    public static final String INTENT_DATA_FORUM_ID = "propForumId";
    public static final String INTENT_DATA_FORUM_TITLE = "propForumTitle";

    private boolean isEditing;
    private int forumId;
    private String forumTitle;
    private Proposal proposal;

    private AtomicBoolean lock = new AtomicBoolean(false);

    private CreateProposalActivityValidator validator = new CreateProposalActivityValidator();


    private LocalBroadcastManager localBroadcastManager;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (intent.getIntExtra(INTENT_DIALOG,0)){
                        case UNKNOWN_ERROR_DIALOG:
                            showCantSendProposalDialog();
                            break;
                        case INSUFICIENTS_FUNDS_DIALOG:
                            showInsuficientFundsException();
                            break;
                        case CANT_SAVE_PROPOSAL_DIALOG:
                            showErrorDialog("Error", intent.getStringExtra(INTENT_EXTRA_MESSAGE_DIALOG));
                            break;
                        case COMMON_ERROR_DIALOG:
                            showErrorDialog("Error", intent.getStringExtra(INTENT_EXTRA_MESSAGE_DIALOG));
                            break;
                        default:
                            Log.e(TAG,"BroadcastReceiver fail");
                            break;
                    }
                }
            });

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
    private Button btn_publish_proposal;

    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.proposal_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_complete_proposal){
            completeWithTestData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void completeWithTestData() {
        proposal = new Proposal();
        proposal.setMine(true);
        proposal.addBeneficiary("uhk9N8Wpw6HWjitFgfmvdLbgX6voUkDYAb", 80000000);
        forumTitle = proposal.getTitle();
        loadProposal();
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        if (getIntent().getAction()!=null) {
            if (getIntent().getAction().equals(ACTION_EDIT_PROPOSAL)) {
                isEditing = true;
                forumId = getIntent().getIntExtra(INTENT_DATA_FORUM_ID, -1);
                forumTitle = getIntent().getStringExtra(INTENT_DATA_FORUM_TITLE);
            }
        }

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
        btn_publish_proposal = (Button) root.findViewById(R.id.btn_publish_proposal);

        root.findViewById(R.id.img_help_my_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_beneficiary_address_1.setText(module.getNewAddress());
            }
        });

        btn_create_proposal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!lock.getAndSet(true)) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            String toastToShow = null;
                            try {
                                //todo: usar el buildProposal()
                                Proposal proposal;
                                if (!isEditing) {
//                                proposal = new Proposal();
//                                proposal.addBeneficiary("uhk9N8Wpw6HWjitFgfmvdLbgX6voUkDYAb", 80000000);
                                    proposal = buildProposal();
                                } else {
                                    proposal = buildProposal();
                                    //todo: esto está así porque el foro falla..
                                    if (proposal != null)
                                        proposal.setForumId(forumId);
                                }

                                if (proposal != null) {
                                    if (!isEditing) {
                                        if (module.createForumProposal(proposal)) {
                                            toastToShow = "Proposal created!";
                                        } else {
                                            toastToShow = "Proposal fail!";
                                        }
                                    } else {
                                        if (module.editForumProposal(proposal)) {
                                            toastToShow = "Edit succed";
                                        } else {
                                            toastToShow = "Edit fail";
                                        }

                                    }
                                } else
                                    Log.e(TAG, "proposal null, see logs");
                            } catch (final CantCreateTopicException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        buildFailDialog(getErrorsFromJson(e.getMessage()));
                                    }
                                });

                            } catch (final CantUpdateProposalException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        buildFailDialog(getErrorsFromJson(e.getMessage()));
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (CantSaveProposalExistException e) {
                                showErrorDialog("Error", "Proposal title already exist");
                            }

                            final String finalToastToShow = toastToShow;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CreateProposalActivity.this, finalToastToShow, Toast.LENGTH_SHORT).show();
                                }
                            });
                            // unlock
                            lock.set(false);
                        }
                    });

                }else Log.e(TAG,"Tocó el boton dos veces seguidas..");
            }
        });

        if (isEditing) {

            btn_create_proposal.setText("EDIT");

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        proposal = module.getProposal(forumId);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    loadProposal();
                                }catch (Exception e){
                                    Toast.makeText(CreateProposalActivity.this,"Load proposal fail",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } catch (CantGetProposalException e) {
                        e.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

    }



    @Override
    protected boolean onBroadcastReceive(String action, Bundle data) {
        if (action.equals(ACTION_PROPOSAL_BROADCASTED)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CreateProposalActivity.this,"Proposal broadcasted!, publishing in the forum..",Toast.LENGTH_SHORT).show();
                }
            });
        }
        return false;
    }

    private void loadProposal(){
        edit_title.setText(forumTitle);
        edit_subtitle.setText(proposal.getSubTitle());
        edit_category.setText(proposal.getCategory());
        edit_body.setText(proposal.getBody());
        edit_start_block.setText(String.valueOf(proposal.getStartBlock()));
        edit_end_block.setText(String.valueOf(proposal.getEndBlock()));
        edit_block_reward.setText(String.valueOf(proposal.getBlockReward()));
        for (Map.Entry<String, Long> beneficiary : proposal.getBeneficiaries().entrySet()) {
            edit_beneficiary_address_1.setText(beneficiary.getKey());
            edit_beneficiary_value_1.setText(String.valueOf(beneficiary.getValue()));
        }

        btn_publish_proposal.setVisibility(View.VISIBLE);


        View.OnClickListener onClickListener;
        if (proposal.isSent()){
            btn_create_proposal.setVisibility(View.GONE);
            btn_publish_proposal.setText("UNPUBLISH");
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"unpublishing..",Toast.LENGTH_LONG).show();
                    module.cancelProposalOnBLockchain(proposal);
                }
            };
        }else {
            btn_publish_proposal.setText("PUBLISH");
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(BlockchainService.INTENT_EXTRA_PROPOSAL,proposal);
                    sendWorkToBlockchainService(BlockchainService.ACTION_BROADCAST_PROPOSAL_TRANSACTION,bundle);
                }
            };
        }

        btn_publish_proposal.setOnClickListener(onClickListener);

    }




    /**
     * todo: falta hacer las validaciones
     * @return
     */
    private Proposal buildProposal(){
        Proposal proposal = new Proposal();
        String title = edit_title.getText().toString();
        String subtitle = edit_subtitle.getText().toString();
        String category = edit_category.getText().toString();
        String body = edit_body.getText().toString();
        int startBlock = Integer.parseInt(edit_start_block.getText().toString());
        int endBlock = Integer.parseInt(edit_end_block.getText().toString());
        long blockReward = Long.parseLong(edit_block_reward.getText().toString());
        Map<String,Long> beneficiaries = new HashMap<>();
        String addressBen1 = edit_beneficiary_address_1.getText().toString();
        long value = Long.parseLong(edit_beneficiary_value_1.getText().toString());
        beneficiaries.put(addressBen1,value);
        //todo: faltan los beneficiarios y las validaciones..
        try {
            proposal.setTitle(validator.validateTitle(title));
            proposal.setSubTitle(validator.validateSubTitle(subtitle));
            proposal.setCategory(validator.validatCategory(category));
            proposal.setBody(validator.validateBody(body));
            proposal.setStartBlock(validator.validateStartBlock(startBlock));
            proposal.setEndBlock(validator.validateEndBlock(endBlock));
            proposal.setBlockReward(validator.validateBlockReward(blockReward));
            if (validator.validateBeneficiary(addressBen1, value)) {
                proposal.addBeneficiary(addressBen1,value);
            }
            validator.validateBeneficiaries(proposal.getBeneficiaries(),proposal.getBlockReward());
        } catch (ValidationException e) {
            buildFailDialog(e.getMessage());
            proposal = null;
        }
        return proposal;

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

    private void showErrorDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle((title!=null)?title:"Upss");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void buildFailDialog(String message) {
        DialogBuilder dialogBuilder = new DialogBuilder(this);
        dialogBuilder.setTitle("Error");
        dialogBuilder.setMessage(message);
        dialogBuilder.show();
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
