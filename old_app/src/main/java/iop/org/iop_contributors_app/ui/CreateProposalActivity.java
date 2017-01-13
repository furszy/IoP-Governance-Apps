package iop.org.iop_contributors_app.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.iop.db.CantGetProposalException;
import org.iop.db.CantSaveProposalExistException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import iop.org.furszy_lib.ChromeHelpPopup;
import iop.org.furszy_lib.utils.SizeUtils;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.dialogs.wallet.InsuficientFundsDialog;
import iop.org.iop_contributors_app.ui.validators.CreateProposalActivityValidator;
import iop.org.iop_contributors_app.ui.validators.CreateProposalWatcher;
import iop.org.iop_contributors_app.ui.validators.ValidationException;
import iop.org.iop_contributors_app.utils.AppUtils;
import iop.org.iop_contributors_app.utils.CrashReporter;
import iop_sdk.forum.CantCreateTopicException;
import iop_sdk.governance.propose.Beneficiary;
import iop_sdk.governance.propose.Proposal;

import static iop.org.iop_contributors_app.ui.ProposalSummaryActivity.ACTION_PROPOSAL;
import static iop.org.iop_contributors_app.ui.ProposalSummaryActivity.INTENT_DATA_PROPOSAL;
import static iop_sdk.governance.ProposalForum.FIELD_ADDRESS;
import static iop_sdk.governance.ProposalForum.FIELD_BLOCK_REWARD;
import static iop_sdk.governance.ProposalForum.FIELD_BODY;
import static iop_sdk.governance.ProposalForum.FIELD_CATEGORY;
import static iop_sdk.governance.ProposalForum.FIELD_END_BLOCK;
import static iop_sdk.governance.ProposalForum.FIELD_START_BLOCK;
import static iop_sdk.governance.ProposalForum.FIELD_SUBTITLE;
import static iop_sdk.governance.ProposalForum.FIELD_TITLE;
import static iop_sdk.governance.ProposalForum.FIELD_VALUE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TRANSACTION_SUCCED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;

/**
 * Created by mati on 17/11/16.
 */

public class CreateProposalActivity extends ContributorBaseActivity {


    private static final String TAG = "CreateProposalActivity";

//    public static final String ACTION_PROPOSAL_BROADCASTED = "propBroadcasted";

    // dialogs


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

    private Map<Integer,ChromeHelpPopup> popups;


    // UI
    private View root;

    private View container_send;
    private ProgressBar progressBar;
    private ImageView img_done;
    private TextView txt_done;

    private EditText edit_title;
    private EditText edit_subtitle;
    private EditText edit_category;
    private EditText edit_body;
    private EditText edit_start_block;
    private EditText edit_end_block;
    private EditText edit_block_reward;
//    private EditText edit_beneficiary_address_1;
//    private EditText edit_beneficiary_value_1;
    private TextView txt_add_beneficiary;
    private Button btn_create_proposal;

    private RecyclerView recycler_beneficiaries;
    private BeneficiariesAdapter beneficiariesAdapter;
    private List<Beneficiary> extraBeneficiaries = new ArrayList<>();
    private LinearLayout ben_container;

    private RelativeLayout beneficiaries_container;


    private Map<String,CreateProposalWatcher> watchers = new HashMap<>();



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
        proposal = Proposal.buildRandomProposal();
        proposal.setMine(true);
        String address = module.getReceiveAddress();
        Log.d(TAG,"fress address: "+address);
        proposal.addBeneficiary(address, 8000000);
        forumTitle = proposal.getTitle();
        loadProposal();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

    }

    @Override
    protected void onCreateView(final ViewGroup container, Bundle savedInstance) {

        if (getIntent().getAction()!=null) {
            if (getIntent().getAction().equals(ACTION_EDIT_PROPOSAL)) {
                isEditing = true;
                forumId = getIntent().getIntExtra(INTENT_DATA_FORUM_ID, -1);
                forumTitle = getIntent().getStringExtra(INTENT_DATA_FORUM_TITLE);
                forumTitle = forumTitle.replace("-"," ");
                Log.i(TAG,"editing mode, title: "+forumTitle+", id: "+forumId);
            }
        }

        root = getLayoutInflater().inflate(R.layout.create_proposal_main,container);

        container_send = root.findViewById(R.id.container_send);
        img_done = (ImageView) root.findViewById(R.id.img_done);
        txt_done = (TextView) root.findViewById(R.id.txt_done);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        edit_title = (EditText) root.findViewById(R.id.edit_title);
        edit_subtitle = (EditText) root.findViewById(R.id.edit_subtitle);
        edit_category = (EditText) root.findViewById(R.id.edit_category);
        edit_body = (EditText) root.findViewById(R.id.edit_body);
        edit_start_block = (EditText) root.findViewById(R.id.edit_start_block);
        edit_end_block = (EditText) root.findViewById(R.id.edit_end_block);
        edit_block_reward = (EditText) root.findViewById(R.id.edit_block_reward);
//        edit_beneficiary_address_1 = (EditText) root.findViewById(R.id.edit_beneficiary_1_address);
//        edit_beneficiary_value_1 = (EditText) root.findViewById(R.id.edit_beneficiary_1_value);
        txt_add_beneficiary = (TextView) root.findViewById(R.id.txt_add_beneficiary);
        btn_create_proposal = (Button) root.findViewById(R.id.btn_create_proposal);

        recycler_beneficiaries = (RecyclerView) root.findViewById(R.id.recycler_beneficiaries);
        ben_container = (LinearLayout) root.findViewById(R.id.ben_container);

        beneficiaries_container = (RelativeLayout) root.findViewById(R.id.beneficiaries_container) ;

        initHelpViews();

        initWatchers();

        root.findViewById(R.id.img_help_my_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!isEditing)
//                    edit_beneficiary_address_1.setText(module.getReceiveAddress());
            }
        });

        container_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container_send.setVisibility(View.GONE);
                isEditing = true;
                btn_create_proposal.setVisibility(View.GONE);
//                initBtnEdit();
            }
        });

        if (isEditing) {

            //disableEditTexts();
//            btn_create_proposal.setText("EDIT");
            btn_create_proposal.setVisibility(View.GONE);

//            initBtnEdit();

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
        }else {

            btn_create_proposal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!lock.getAndSet(true)) {

                        if (!isEditing)
                            preparateLoading("Proposal posted!",R.drawable.icon_done);

                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                String errorTitle = null;
                                String messageBody = null;

                                boolean result = false;
                                try {
                                    proposal = buildProposal();
                                    if (proposal != null) {

                                        int forumId = 0;
                                        if ((forumId = module.createForumProposal(proposal))>0) {
                                            messageBody = "Proposal created!";
                                            proposal.setForumId(forumId);
                                            result = true;
                                        } else {
                                            errorTitle = "Error";
                                            messageBody = "Uknown, proposal fail!\nplease send a report";
                                        }
                                    } else {
                                        Log.e(TAG, "proposal null, see logs");
                                        errorTitle = "Uknown error";
                                        messageBody =  "please send a report";
                                    }
                                } catch (final CantCreateTopicException e) {
                                    e.printStackTrace();
                                    errorTitle = "Error";
                                    messageBody = (e.getMessage()!=null && !e.getMessage().equals(""))?getErrorsFromJson(e.getMessage()):"CantCreateTopicException";
                                } catch (CantSaveProposalExistException e) {
                                    errorTitle = "Error";
                                    messageBody = "Proposal title already exist";
                                } catch (ValidationException e){
                                    errorTitle = "Validation error";
                                    messageBody = e.getMessage();
                                } catch(Exception e) {
                                    e.printStackTrace();
                                    // save error in report
                                    CrashReporter.saveBackgroundTrace(e, AppUtils.packageInfoFromContext(CreateProposalActivity.this));
                                    errorTitle = "Error";
                                    messageBody = (e.getMessage()!=null && !e.getMessage().equals(""))?getErrorsFromJson(e.getMessage()):"Exception, please send report";
                                }

                                final boolean finalResult = result;
                                final String finalMessageBody = messageBody;
                                final String finalErrorTitle = errorTitle;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (finalResult){
                                            showDoneLoading();
                                            //disableEditTexts();
                                            Toast.makeText(CreateProposalActivity.this, finalMessageBody, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CreateProposalActivity.this,ProposalSummaryActivity.class);
                                            intent.setAction(ACTION_PROPOSAL);
                                            intent.putExtra(INTENT_DATA_PROPOSAL,proposal);
                                            startActivity(intent);
                                            finish();
                                        }else {
                                            container_send.setVisibility(View.INVISIBLE);
                                            showErrorDialog(finalErrorTitle, finalMessageBody);
                                        }
                                    }
                                });
                                // unlock
                                lock.set(false);
                            }
                        });

                    }else Log.e(TAG,"Tocó el boton dos veces seguidas..");
                }
            });



            extraBeneficiaries.add(new Beneficiary());
            initBeneficiaryRecycler();


            txt_add_beneficiary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    int beneficiariesSize = extraBeneficiaries.size();

                    // check if the address and amount are loaded in the previous beneficiary
                    Beneficiary lastBeneficiary = beneficiariesAdapter.getItem(beneficiariesSize-1);
                    try {
                        validator.validateBeneficiary(lastBeneficiary.getAddress(), lastBeneficiary.getAmount());
                    } catch (ValidationException e) {
                        Toast.makeText(v.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                        return;
                    } catch (Exception e){
                        e.printStackTrace();
                        return;
                    }

//                        ben_container.getLayoutParams().height = recycler_beneficiaries.getLayoutParams().height+SizeUtils.convertDpToPx(getResources(),32);
//                        recycler_beneficiaries.getLayoutParams().height = recycler_beneficiaries.getLayoutParams().height+SizeUtils.convertDpToPx(getResources(),32);
                    Beneficiary beneficiary = new Beneficiary();
                    beneficiariesAdapter.addItem(beneficiary);
                    beneficiariesAdapter.notifyDataSetChanged();


                }
            });
        }

    }

    private void initBeneficiaryRecycler(){
        recycler_beneficiaries.setVisibility(View.VISIBLE);
        //recycler_beneficiaries.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_beneficiaries.setLayoutManager(layoutManager);
        VerticalSpaceItemDecoration dividerItemDecoration = new VerticalSpaceItemDecoration(SizeUtils.convertDpToPx(getResources(),12));
        recycler_beneficiaries.addItemDecoration(dividerItemDecoration);
        beneficiariesAdapter = new BeneficiariesAdapter(CreateProposalActivity.this,extraBeneficiaries,validator);
        recycler_beneficiaries.setAdapter(beneficiariesAdapter);
    }

//    private void initBtnEdit() {
//        btn_edit.setVisibility(View.VISIBLE);
//        btn_edit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    proposal = buildProposal();
//                    if (proposal != null)
//                        proposal.setForumId(forumId);
//                    redirectToForum(proposal);
//                } catch (ValidationException e) {
//                    showErrorDialog("Validation error", e.getMessage());
//                }
//            }
//        });
//    }

    private void initHelpViews() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();

                if (id == R.id.help_start_block){
                    ChromeHelpPopup chromeHelpPopup = loadPopup(v,getString(R.string.create_proposal_help_start_block));
                    chromeHelpPopup.setyMove(-10);
                    chromeHelpPopup.setAnimation(false);
                    chromeHelpPopup.setTextColor(ContextCompat.getColor(v.getContext(),R.color.create_proposal_help_text_color));
                    chromeHelpPopup.setTextBackgroundColor(ContextCompat.getColor(v.getContext(),R.color.create_proposal_help_text_background));
                    chromeHelpPopup.show(v);
                } else if (id == R.id.help_end_block){
                    ChromeHelpPopup chromeHelpPopup = loadPopup(v,getString(R.string.create_proposal_help_end_block));
                    chromeHelpPopup.setyMove(-10);
                    chromeHelpPopup.setAnimation(false);
                    chromeHelpPopup.setTextColor(ContextCompat.getColor(v.getContext(),R.color.create_proposal_help_text_color));
                    chromeHelpPopup.setTextBackgroundColor(ContextCompat.getColor(v.getContext(),R.color.create_proposal_help_text_background));
                    chromeHelpPopup.show(v);
                } else if (id == R.id.help_block_reward){
                    ChromeHelpPopup chromeHelpPopup = loadPopup(v,getString(R.string.create_proposal_help_block_reward));
                    chromeHelpPopup.setyMove(-10);
                    chromeHelpPopup.setAnimation(false);
                    chromeHelpPopup.setTextColor(ContextCompat.getColor(v.getContext(),R.color.create_proposal_help_text_color));
                    chromeHelpPopup.setTextBackgroundColor(ContextCompat.getColor(v.getContext(),R.color.create_proposal_help_text_background));
                    chromeHelpPopup.show(v);
                } else if (id == R.id.help_beneficiaries ){
                    ChromeHelpPopup chromeHelpPopup = loadPopup(v,getString(R.string.create_proposal_help_beneficiaries));
                    chromeHelpPopup.setyMove(-10);
                    chromeHelpPopup.setAnimation(false);
                    chromeHelpPopup.setTextColor(ContextCompat.getColor(v.getContext(),R.color.create_proposal_help_text_color));
                    chromeHelpPopup.setTextBackgroundColor(ContextCompat.getColor(v.getContext(),R.color.create_proposal_help_text_background));
                    chromeHelpPopup.show(v);
                }
            }
        };

        findViewById(R.id.help_start_block).setOnClickListener(onClickListener);
        findViewById(R.id.help_end_block).setOnClickListener(onClickListener);
        findViewById(R.id.help_block_reward).setOnClickListener(onClickListener);
        findViewById(R.id.help_beneficiaries).setOnClickListener(onClickListener);


//        Spinner spinner = (Spinner) findViewById(R.id.spinner_categories);
//        // Spinner click listener
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String item = parent.getItemAtPosition(position).toString();
////                edit_category.setText(item);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//
//        // Spinner Drop down elements
//        List<String> categories = new ArrayList <String>();
//        categories.put("Develop");
//        categories.put("Graphic design");
//        categories.put("Community");
//        categories.put("Public relationship");
//
//
//        // Creating adapter for spinner
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.create_proposal_spinner_item, categories);
//
//        // Drop down layout style - list view with radio button
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // attaching data adapter to spinner
//        spinner.setAdapter(dataAdapter);

    }


    private void initWatchers(){
        setWatcher(FIELD_TITLE,edit_title,null);
        setWatcher(FIELD_SUBTITLE,edit_subtitle,null);
        setWatcher(FIELD_CATEGORY,edit_category,null);
        setWatcher(FIELD_BODY,edit_body,null);
        setWatcher(FIELD_START_BLOCK,edit_start_block,null);
        setWatcher(FIELD_END_BLOCK,edit_end_block,null);
        setWatcher(FIELD_BLOCK_REWARD,edit_block_reward,null);
//        setWatcher(FIELD_ADDRESS,edit_beneficiary_address_1,null);
//        setWatcher(FIELD_VALUE,edit_beneficiary_value_1,null);
    }

    public void setWatcher(String id,EditText editText,View errorView){
        CreateProposalWatcher createProposalWatcher = new CreateProposalWatcher(id,validator,errorView);
        editText.addTextChangedListener(createProposalWatcher);
        watchers.put(id,createProposalWatcher);
    }

    private ChromeHelpPopup loadPopup(View view,String text) {
        if (popups==null)popups = new HashMap<>();
        ChromeHelpPopup chromeHelpPopup = null;
        if (popups.containsKey(view.getId())) {
            chromeHelpPopup = popups.get(view.getId());
        }else {
            chromeHelpPopup = new ChromeHelpPopup(this,text);
        }
        return chromeHelpPopup;
    }

    @Override
    protected boolean onContributorsBroadcastReceive(Bundle data) {
        if (data.getString(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_TRANSACTION_SUCCED)){
            showDoneLoading();
//            lockBroadcast.set(false);
            Toast.makeText(CreateProposalActivity.this,"Proposal broadcasted!, publishing in the forum..",Toast.LENGTH_SHORT).show();
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

        for (int i = 0; i < proposal.getBeneficiaries().size(); i++) {
            Beneficiary beneficiaryToLoad = proposal.getBeneficiaries().get(i);
            if (i==0){
                Beneficiary beneficiary = beneficiariesAdapter.getItem(i);
                beneficiary.setAddress(beneficiaryToLoad.getAddress());
                beneficiary.setAmount(beneficiaryToLoad.getAmount());
                extraBeneficiaries.get(i).setAddress(beneficiaryToLoad.getAddress());
                beneficiariesAdapter.notifyItemChanged(i);
            }else {
                beneficiariesAdapter.addItem(beneficiaryToLoad);
            }
        }



//        View.OnClickListener onClickListener = null;
//        if (proposal.isSent()){
//            btn_create_proposal.setVisibility(View.GONE);
//            btn_publish_proposal.setVisibility(View.VISIBLE);
//            btn_publish_proposal.setText("UNPUBLISH");
//            onClickListener = new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(v.getContext(),"unpublishing..",Toast.LENGTH_LONG).show();
//                    module.cancelProposalOnBLockchain(proposal);
//                }
//            };
//            btn_publish_proposal.setOnClickListener(onClickListener);
//        }else if (isEditing){
//            initBroadcastBtn();
//        }

    }

//    private void initBroadcastBtn(){
//        btn_publish_proposal.setVisibility(View.VISIBLE);
//        btn_publish_proposal.setText("BRODCAST");
//        View.OnClickListener onClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isEditing) {
//                    try{
//                        if(lockBroadcast.compareAndSet(false,true)) {
//                            // loading
//                            preparateLoading("Proposal broadcasted!",R.drawable.icon_done);
//
//                            Bundle bundle = new Bundle();
//                            Proposal proposalNew = buildProposal();
//                            proposalNew.setForumId(proposal.getForumId());
//                            bundle.putSerializable(BlockchainService.INTENT_EXTRA_PROPOSAL, proposalNew);
//                            sendWorkToBlockchainService(BlockchainService.ACTION_BROADCAST_PROPOSAL_TRANSACTION, bundle);
//                        }else
//                            Log.e(TAG,"Toco dos veces el broadcast..");
//                    } catch (ValidationException e) {
//                        showErrorDialog("Validation error",e.getMessage());
//                        lockBroadcast.set(false);
//                    }
//                }else
//                    Toast.makeText(v.getContext(),"You have to post before publish on blockchain",Toast.LENGTH_LONG).show();
//            }
//        };
//        btn_publish_proposal.setOnClickListener(onClickListener);
//    }


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
    }

    private void hideDoneLoading(){
        container_send.setVisibility(View.INVISIBLE);
    }



    /**
     * Disable edit
     */
    private void disableEditTexts() {
        edit_title.setEnabled(false);
        edit_subtitle.setEnabled(false);
        edit_category.setEnabled(false);
        edit_body.setEnabled(false);
        edit_start_block.setEnabled(false);
        edit_end_block.setEnabled(false);
        edit_block_reward.setEnabled(false);
//        edit_beneficiary_address_1.setEnabled(false);
//        edit_beneficiary_value_1.setEnabled(false);

    }


    /**
     * todo: falta hacer las validaciones
     * @return
     */
    private Proposal buildProposal() throws ValidationException{
        Proposal proposal = new Proposal();

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, CreateProposalWatcher> stringCreateProposalWatcherEntry : watchers.entrySet()) {
            boolean isValid = stringCreateProposalWatcherEntry.getValue().isValid();
            String errorText = stringCreateProposalWatcherEntry.getValue().getErrorToShow();
            if (!isValid) {
                switch (stringCreateProposalWatcherEntry.getKey()) {
                    case FIELD_TITLE:
                        stringBuilder.append("* "+errorText);
                        break;
                    case FIELD_SUBTITLE:
                        stringBuilder.append("* "+errorText);
                        break;
                    case FIELD_BODY:
                        stringBuilder.append("* "+errorText);
                        break;
                    case FIELD_CATEGORY:
                        stringBuilder.append("* "+errorText);
                        break;
                    case FIELD_START_BLOCK:
                        stringBuilder.append("* Start block is not valid");
                        break;
                    case FIELD_END_BLOCK:
                        stringBuilder.append("* End block is not valid");
                        break;
                    case FIELD_BLOCK_REWARD:
                        stringBuilder.append("* Block reward is not valid");
                        break;
                    case FIELD_ADDRESS:
                        stringBuilder.append("* Address is not valid");
                        break;
                    case FIELD_VALUE:
                        // nothing por ahora
                        break;
                }
                stringBuilder.append("\n");
            }
        }

        if (stringBuilder.length()>0){
            throw new ValidationException(stringBuilder.toString());
        }

        String title = edit_title.getText().toString();
        String subtitle = edit_subtitle.getText().toString();
        String category = edit_category.getText().toString();
        String body = edit_body.getText().toString();
        int startBlock = Integer.parseInt(edit_start_block.getText().toString());
        int endBlock = Integer.parseInt(edit_end_block.getText().toString());
        long blockReward = Long.parseLong(edit_block_reward.getText().toString());
//        String addressBen1 = edit_beneficiary_address_1.getText().toString();
//        long value = Long.parseLong(edit_beneficiary_value_1.getText().toString());

        //todo: faltan los beneficiarios y las validaciones..
        proposal.setTitle(validator.validateTitle(title));
        proposal.setSubTitle(validator.validateSubTitle(subtitle));
//        proposal.setCategory(validator.validatCategory(category));
        proposal.setBody(validator.validateBody(body));
        proposal.setStartBlock(validator.validateStartBlock(startBlock));
        proposal.setEndBlock(validator.validateEndBlock(endBlock));
        proposal.setBlockReward(validator.validateBlockReward(blockReward));
//        if (validator.validateBeneficiary(addressBen1, value)) {
//            proposal.addBeneficiary(addressBen1,value);
//        }

        for (int i = 0; i < extraBeneficiaries.size(); i++) {
            Beneficiary extraBeneficiary = beneficiariesAdapter.getItem(i);
            if (i>0){
                if (extraBeneficiary.getAddress()==null || extraBeneficiary.getAddress().equals("") && extraBeneficiary.getAmount()==0){
                    continue;
                }
            }
            if (validator.validateBeneficiary(extraBeneficiary.getAddress(),extraBeneficiary.getAmount())){
                proposal.addBeneficiary(extraBeneficiary.getAddress(),extraBeneficiary.getAmount());
            }
        }


        validator.validateBeneficiaries(proposal.getBeneficiaries(),proposal.getBlockReward());

        return proposal;
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

        if (popups!=null){
            popups.clear();
        }
    }

    private void redirectToForum(Proposal proposal) {
        Intent intent1 = new Intent(CreateProposalActivity.this,ForumActivity.class);
        String url = module.getForumUrl()+"/t/"+proposal.getTitle().toLowerCase().replace(" ","-")+"/"+proposal.getForumId();
        intent1.putExtra(ForumActivity.INTENT_URL,url);
        startActivity(intent1);
    }


    private void showInsuficientFundsException(){
        InsuficientFundsDialog insuficientFundsDialog = InsuficientFundsDialog.newInstance(module);
        insuficientFundsDialog.show(getFragmentManager(),"insuficientFundsDialog");

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
