package iop.org.iop_contributors_app.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 05/12/16.
 */

public class IoPBalanceActivity extends AppCompatActivity {

    private static final String LOG = "IoPBalanceActivity";

    private WalletModule module;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        module = ApplicationController.getInstance().getWalletModule();

        setContentView(R.layout.iop_balance_main);

        setTitle("Balance");
        initToolbar();
        initEditTexts();
        initSpinner();
        initSendBtn();
        loadBalances();
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
                if (s.length()==32){
                    isAddressFine=true;
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

                if (isAddressFine && isAmountFine) {

                    String address = edit_address.getText().toString();
                    long amount = Long.parseLong(edit_amount.getText().toString());

                    try {
                        module.sendTransaction(address, amount);
                    } catch (InsufficientMoneyException e) {
                        showErrorDialog("Error", "Insuficient balance");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    showErrorDialog("Error","Cant process data, please check your inputs");
                }

            }
        });
    }


    private void loadBalances() {
        txt_balance = (TextView) findViewById(R.id.txt_balance);
        txt_voting_power_available = (TextView) findViewById(R.id.txt_voting_power_available);
        txt_voting_power_locked = (TextView) findViewById(R.id.txt_voting_power_locked);

        txt_balance.setText(module.getWalletManager().getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString());
        txt_voting_power_available.setText("Available: "+module.getAvailableBalanceStr()+" IoPs");
        txt_voting_power_locked.setText("Locked: "+module.getLockedBalance()+ " IoPs");
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
