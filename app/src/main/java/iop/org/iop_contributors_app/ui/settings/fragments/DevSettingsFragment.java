package iop.org.iop_contributors_app.ui.settings.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.blockchain.OpReturnOutputTransaction;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.core.iop_sdk.governance.ProposalTransactionRequest;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.ui.settings.IoPBalanceActivity;
import iop.org.iop_contributors_app.utils.Cache;
import iop.org.iop_contributors_app.wallet.InvalidProposalException;
import iop.org.iop_contributors_app.wallet.WalletConstants;
import iop.org.iop_contributors_app.wallet.WalletModule;
import iop.org.iop_contributors_app.wallet.db.CantSaveProposalException;
import iop.org.iop_contributors_app.wallet.exceptions.CantSendProposalException;
import iop.org.iop_contributors_app.wallet.exceptions.InsuficientBalanceException;

import static android.graphics.Color.WHITE;
import static iop.org.iop_contributors_app.core.iop_sdk.utils.ArraysUtils.numericTypeToByteArray;
import static iop.org.iop_contributors_app.utils.mine.QrUtils.encodeAsBitmap;
import static iop.org.iop_contributors_app.utils.mine.SizeUtils.convertDpToPx;

/**
 * Created by mati on 09/12/16.
 */

public class DevSettingsFragment extends PreferenceFragment {

    private View root;
    private WalletModule module;
    private ExecutorService executorService;

    private byte[] transactionHash = new byte[32];
    private boolean proposalSended;


    public final static DevSettingsFragment newInstance(WalletModule module) {
        DevSettingsFragment fragment = new DevSettingsFragment();
        fragment.setModule(module);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        addPreferencesFromResource(R.xml.dev_settings);

        root = super.onCreateView(inflater, container, savedInstanceState);
        root.setPadding(0,16,0,0);
        root.setBackgroundColor(Color.parseColor("#1A1A1A"));

        ListView list = (ListView) root.findViewById(android.R.id.list);
//        list.setDivider(getResources().getDrawable(R.drawable.settings_divider,null)); // or some other color int
        list.setDivider(new ColorDrawable(Color.WHITE));
        list.setDividerHeight((int) 2);

        setHasOptionsMenu(false);

        final Preference pref = getPreferenceManager().findPreference("id_node_host");
        pref.setSummary("192.168.0.111");

        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                module.setNewNode(newValue.toString());
                Toast.makeText(getActivity(),"Node changed to: "+newValue.toString()+", now touch reset and close the process please",Toast.LENGTH_LONG).show();
                return true;
            }
        });

        executorService = Executors.newSingleThreadExecutor();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals("id_restart_blockchain")){
            module.getWalletManager().resetBlockchain();
            Toast.makeText(getActivity(),"Reseting blockchain, please close (swipe open apps manager) and restart the app",Toast.LENGTH_LONG).show();
        }else if(preference.getKey().equals("id_broadcast_transaction")){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Context.propagate(WalletConstants.CONTEXT);
                    Proposal proposal = new Proposal();
                    proposal.addBeneficiary(module.getNewAddress(),proposal.getBlockReward());
                    try {
                        module.sendProposal(proposal,transactionHash);
                        proposalSended = true;
                    } catch (InsuficientBalanceException e) {
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    } catch (Exception e){
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else if (preference.getKey().equals("id_broadcasting_yes")){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (proposalSended) {
                        sendProposalVoting(transactionHash, true);
                    }else
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),"Tenes que enviar primero el contrato..",Toast.LENGTH_LONG).show();
                            }
                        });
                }
            });


        }else if (preference.getKey().equals("id_broadcasting_no")){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (proposalSended) {
                        sendProposalVoting(transactionHash, false);
                    }else
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),"Tenes que enviar primero el contrato..",Toast.LENGTH_LONG).show();
                            }
                        });

                }
            });
        } else if (preference.getKey().equals("id_request_coins")){

            showQrDialog(getActivity());

        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void sendProposalVoting(byte[] proposalHash,boolean vote){

        Wallet wallet = module.getWalletManager().getWallet();

        try {
            OpReturnOutputTransaction.Builder builder = new OpReturnOutputTransaction.Builder(RegTestParams.get());
            byte[] prevData = new byte[36];

            int tag = 0x564f54;
            int voting = (vote)?1:0;

            byte[] hash = proposalHash;

            numericTypeToByteArray(prevData, tag, 0, 3);
            numericTypeToByteArray(prevData, voting, 3, 1);
            System.arraycopy(hash, 0, prevData, 4, 32);
            builder.addData(prevData);

            OpReturnOutputTransaction opReturnOutputTransaction = builder.build2();


            Transaction tran = new Transaction(WalletConstants.NETWORK_PARAMETERS);

            // refund
            TransactionOutput freeze = new TransactionOutput(WalletConstants.NETWORK_PARAMETERS,tran,Coin.valueOf(50000000),wallet.freshReceiveAddress());

            // lock
            tran.addOutput(freeze);
            // op return
            tran.addOutput(opReturnOutputTransaction);

            SendRequest sendRequest = SendRequest.forTx(tran);

            sendRequest.signInputs = true;
            sendRequest.shuffleOutputs = false;
            sendRequest.changeAddress = wallet.freshReceiveAddress();

            // complete transaction
            wallet.completeTx(sendRequest);

            wallet.commitTx(sendRequest.tx);

            module.getBlockchainManager().broadcastTransaction(sendRequest.tx.getHash().getBytes()).get();


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"Voting broadcasteado pelado!",Toast.LENGTH_LONG).show();
                }
            });

        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
        } catch (Exception e){
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private class MyCoinSelector implements org.bitcoinj.wallet.CoinSelector {
        @Override
        public CoinSelection select(Coin coin, List<TransactionOutput> list) {
            return new CoinSelection(coin,new ArrayList<TransactionOutput>());
        }
    }

    private void showQrDialog(Activity activity){

        try {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.qr_dialog);


            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.txt_share);

            ImageView image = (ImageView) dialog.findViewById(R.id.img_qr);

            String address = Cache.getCacheAddress();
            if (address==null) {
                address = module.getNewAddress();
                Cache.setCacheAddress(address);
            }
            // qr
            Bitmap qrBitmap = Cache.getQrBigBitmapCache();
            if (qrBitmap == null) {
                Resources r = getResources();
                int px = convertDpToPx(getResources(),175);
                qrBitmap = encodeAsBitmap(address, px, px, Color.parseColor("#1A1A1A"), WHITE );
                Cache.setQrBigBitmapCache(qrBitmap);
            }
            image.setImageBitmap(qrBitmap);

            // cache address
            TextView txt_qr = (TextView)dialog.findViewById(R.id.txt_qr);
            txt_qr.setText(address);
//            txt_qr.setOnLongClickListener();

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textToClipboard(getActivity(),Cache.getCacheAddress());
                    Toast.makeText(getActivity(),"Copied",Toast.LENGTH_LONG).show();
                }
            };

            dialog.findViewById(R.id.txt_copy).setOnClickListener(clickListener);

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareText(getActivity(),"Qr",Cache.getCacheAddress());
                    dialog.dismiss();
                }
            });



            dialog.show();

        } catch (WriterException e) {
            e.printStackTrace();
        }catch ( Exception e){
            e.printStackTrace();
        }
    }
    private void textToClipboard(Activity activity,String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }


    private void shareText(Activity activity,String title,String text){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, title));
    }


    public void setModule(WalletModule module) {
        this.module = module;
    }
}
