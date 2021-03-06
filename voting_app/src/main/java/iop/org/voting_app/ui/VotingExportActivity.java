package iop.org.voting_app.ui;

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
import android.text.InputType;
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
import org.bitcoinj.utils.BtcFormat;
import org.bitcoinj.wallet.Wallet;
import org.iop.AppController;
import org.iop.WalletConstants;
import org.iop.WalletModule;
import org.iop.exceptions.CantSendTransactionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import iop.org.iop_contributors_app.ui.settings.IoPBalanceActivity;
import iop.org.voting_app.R;

import static org.iop.intents.constants.IntentsConstants.ACTION_NOTIFICATION;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;


/**
 * Created by mati on 05/12/16.
 * //todo: falta mejorar esto y armar una clase que sirva de conversor de tokens de una vez por todas..
 */

public class VotingExportActivity extends AppCompatActivity {

    private static final String TAG = "VotingExportActivity";

    private WalletModule module;
    protected LocalBroadcastManager localBroadcastManager;
    private IoPCode ioPCodeTouched;

    enum IoPCode{

        IoP,mIoP

    }

    private Toolbar toolbar;

    private TextView txt_balance;
    private TextView txt_voting_power_available;
    private TextView txt_voting_power_locked;
    private EditText edit_address;
    private EditText edit_amount;
    private Spinner spinner_coin;
    private Button btn_send;

    private boolean isAddressFine;
    private boolean isAmountFine;


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

        setTitle("Export");

        module = ((AppController)getApplication()).getModule();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        setContentView(R.layout.voting_export_main);

        setTitle("Balance");
        initToolbar();
        initEditTexts();
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

    private void initEditTexts() {
        edit_address = (EditText) findViewById(R.id.edit_address);
        edit_amount = (EditText) findViewById(R.id.edit_amount);

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

        ioPCodeTouched = IoPCode.IoP;

        spinner_coin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ioPCodeTouched = IoPCode.IoP;
                        edit_amount.setHint("0.00");
                        edit_amount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        break;
                    case 1:
                        ioPCodeTouched = IoPCode.mIoP;
                        edit_amount.setHint("0");
                        edit_amount.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private long parseAmount(String amount,IoPCode ioPCode) {
        long value = 0;
        Log.d(TAG,"value to parse: "+amount);
        if (ioPCode== IoPCode.IoP){
            if (amount.contains(".")) {
                double valueDouble = Double.parseDouble(amount);
                String str = new Double(valueDouble).toString();
                //
                str = str.substring(0, str.indexOf('.'));
                int parteEntera = Integer.valueOf(str);

                // decimal part
                str = str.substring(str.indexOf('.') + 1);
                int decimal = Integer.valueOf(str);

                value = Coin.valueOf(parteEntera, decimal).value;
            }else {
                value = Coin.valueOf(Integer.parseInt(amount),0).value;
            }
        }else {
            value = Coin.valueOf(Long.parseLong(amount)).value;
        }
        return value;
    }

    private void initSendBtn() {
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAddressFine) {

                    if (isAmountFine) {
                        final String address = edit_address.getText().toString();

                        final long amount =  parseAmount(edit_amount.getText().toString(),ioPCodeTouched);

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    module.sendTransactionFromAvailableBalance(address, amount);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showErrorDialog("Succed","Tokens exported from wallet\nRemember wait 10 minutes until coins are confirmed by the network");
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

        BtcFormat btcFormat = BtcFormat.getInstance(Locale.GERMANY);
        String balance = btcFormat.format(module.getBalance(),4,1).replace("BTC","IoP");
        txt_balance.setText(balance);
        String spendableValue = btcFormat.format(module.getAvailableBalance(),4,1).replace("BTC","IoP");
        txt_voting_power_available.setText(spendableValue);
        String lockedBalance = btcFormat.format(module.getLockedBalance(),4,1).replace("BTC","IoP");
        txt_voting_power_locked.setText(lockedBalance);
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
