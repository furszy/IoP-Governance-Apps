package iop.org.iop_contributors_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";


    private View create_contract;
    private View show_contracts;

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        super.beforeCreate(savedInstanceState);
    }


    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(final ViewGroup container, Bundle savedInstance) {

        final View view = getLayoutInflater().inflate(R.layout.home_content,container);

        show_contracts = view.findViewById(R.id.show_contract);
        create_contract = view.findViewById(R.id.create_contract);


        show_contracts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ProposalsActivity.class));
            }
        });

        create_contract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CreateProposalActivity.class));
            }
        });

//        Button button = (Button) view.findViewById(R.id.btn_send);
//        button.setText("send proposal");
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try{
//                            Proposal proposal = new Proposal();
//                            proposal.addBeneficiary("uhk9N8Wpw6HWjitFgfmvdLbgX6voUkDYAb",8000000);
//                            Bundle bundle = new Bundle();
//                            bundle.putSerializable(BlockchainService.INTENT_EXTRA_PROPOSAL,proposal);
//                            sendWorkToBlockchainService(BlockchainService.ACTION_BROADCAST_PROPOSAL_TRANSACTION,bundle);
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                }).init();
//
//            }
//        });
//
//        button.setBackgroundColor(Color.BLUE);
//
//        Button button1 = (Button) view.findViewById(R.id.btn_fresh_address);
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG,"Fresh address: "+module.getWalletManager().getWallet().freshReceiveAddress());
//                Log.d(TAG,"balance: "+module.getWalletManager().getWallet().getBalance().toFriendlyString());
//            }
//        });
//
//
//
//        findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
//            }
//        });



//        if (module.getProfile()==null){
//            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//            alertDialog.setTitle("Welcome!");
//            alertDialog.setMessage("You need to create a Profile to init contributing");
//            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
//                            startActivity(intent);
//                            dialog.dismiss();
//                        }
//                    });
//            alertDialog.show();
//        }

    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}
