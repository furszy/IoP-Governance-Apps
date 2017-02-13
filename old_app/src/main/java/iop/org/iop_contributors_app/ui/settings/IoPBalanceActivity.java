package iop.org.iop_contributors_app.ui.settings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.wallet.Wallet;
import org.iop.AppController;
import org.iop.WalletConstants;
import org.iop.WalletModule;
import org.iop.exceptions.CantSendTransactionException;

import java.util.ArrayList;
import java.util.List;

import iop.org.iop_contributors_app.R;

import static org.iop.intents.constants.IntentsConstants.ACTION_NOTIFICATION;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;

/**
 * Created by mati on 05/12/16.
 * //todo: falta mejorar esto y armar una clase que sirva de conversor de tokens de una vez por todas..
 */

public class IoPBalanceActivity extends AppCompatActivity {

    private static final String TAG = "IoPBalanceActivity";

    private WalletModule module;
    protected LocalBroadcastManager localBroadcastManager;

    private Toolbar toolbar;

    private TextView txt_balance;
    private TextView txt_voting_power_available;
    private TextView txt_voting_power_locked;
    private EditText edit_address;
    private EditText edit_amount;
    private Spinner spinner_coin;
    private Button btn_send;
    private TextView txt_danger;
    private TextView txt_danger_expl;

    private boolean isAddressFine;
    private boolean isAmountFine;

    private boolean isDonation;
    private String donationAddress;

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle.containsKey(INTENT_BROADCAST_DATA_TYPE)) {
                String dataType = bundle.getString(INTENT_BROADCAST_DATA_TYPE);

                if (dataType.equals(INTENT_BROADCAST_DATA_ON_COIN_RECEIVED)) {
                    loadBalances();
                }

            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        module = ((AppController)getApplication()).getModule();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        Intent intent = getIntent();
        if (intent!=null && intent.getAction()!=null && intent.getAction().equals("donate")){
            isDonation = true;
        }

        setContentView(R.layout.iop_balance_main);

        String title;
        String explanation;

        if (isDonation){
            setTitle("Donation");
            donationAddress = WalletConstants.DONATION_ADDRESS;
            title="Contribute";
            explanation="Thanks for support the development of IoP :) ";
        }else {
            setTitle("Export");
            title="Danger!";
            explanation="If you send your funds out\\nyou will not be able to contribute";
        }

        initText(title,explanation);
        initToolbar();
        initEditTexts(donationAddress);
        initSpinner();
        initSendBtn();
    }



    private void initToolbar() {
        toolbar  = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void initText(String title,String explanation){
        txt_danger = (TextView) findViewById(R.id.txt_danger);
        txt_danger_expl = (TextView) findViewById(R.id.txt_danger_expl);

        txt_danger.setText(title);
        txt_danger_expl.setText(explanation);
    }

    private void initEditTexts(String donationAddres) {
        edit_address = (EditText) findViewById(R.id.edit_address);
        edit_amount = (EditText) findViewById(R.id.edit_amount);

        if (donationAddres!=null){
            edit_address.setText(donationAddres);
            isAddressFine=true;
        }

        edit_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("") && s.toString().length()>20) {
                    try {
                        Address.fromBase58(WalletConstants.NETWORK_PARAMETERS, s.toString());
                        isAddressFine = true;
                    }catch (Exception e){
                        // nothing
                    }
                }else {
                    isAddressFine=false;
                }

            }
        });

        edit_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0){
                    isAmountFine = true;
                }else {
                    isAmountFine=false;
                }
            }
        });

    }


    private void initSpinner(){
        spinner_coin = (Spinner) findViewById(R.id.spinner_coin);
        List<String> list = new ArrayList<String>();
        list.add("IoP");
        list.add("mIoP");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_coin.setAdapter(dataAdapter);

        spinner_coin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:

                        break;
                    case 1:

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initSendBtn() {
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAddressFine) {

                    if (isAmountFine) {
                        final String address = edit_address.getText().toString();
                        final long amount = Long.parseLong(edit_amount.getText().toString());

                        final long realAmount = Coin.valueOf((int) amount,0).getValue();


                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    module.sendTransactionFromAvailableBalance(address, realAmount);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showErrorDialog("Succed","Tokens exported from wallet");
                                            loadBalances();
                                        }
                                    });
                                } catch (InsufficientMoneyException e) {
                                    Log.e(TAG,e.getMessage());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showErrorDialog("Error", "Insuficient balance");
                                        }
                                    });
                                }catch (final CantSendTransactionException e) {
                                    Log.e(TAG,e.getMessage());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showErrorDialog("Error", e.getMessage());
                                        }
                                    });
                                } catch (final Exception e) {
                                    e.printStackTrace();
//                                    Log.e(TAG,e.getMessage());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showErrorDialog("Error", e.getMessage());
                                        }
                                    });
                                }
                            }
                        });
                        thread.start();
                    }else {
                        showErrorDialog("Error","Invalid amount");
                    }
                }else {
                    showErrorDialog("Error","Invalid address");
                }

            }
        });
        if (isDonation){
            btn_send.setText("Donate!");
        }
    }

    @Override
    protected void onStop() {
        localBroadcastManager.unregisterReceiver(notificationReceiver);
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBalances();
        localBroadcastManager.registerReceiver(notificationReceiver,new IntentFilter(ACTION_NOTIFICATION));
    }

    private void loadBalances() {
        txt_balance = (TextView) findViewById(R.id.txt_balance);
        txt_voting_power_available = (TextView) findViewById(R.id.txt_voting_power_available);
        txt_voting_power_locked = (TextView) findViewById(R.id.txt_voting_power_locked);

        txt_balance.setText(module.getWalletManager().getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString());
        txt_voting_power_available.setText("Available: "+module.getAvailableBalanceStr()+" IoPs");
        txt_voting_power_locked.setText("Locked: "+module.getLockedBalanceStr()+ " IoPs");
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


}
