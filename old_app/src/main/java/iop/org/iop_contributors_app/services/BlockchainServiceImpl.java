package iop.org.iop_contributors_app.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.listeners.AbstractPeerDataEventListener;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDataEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.utils.BtcFormat;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.iop.AppController;
import org.iop.WalletConstants;
import org.iop.WalletModule;
import org.iop.configurations.WalletPreferencesConfiguration;
import org.iop.db.CantSaveProposalException;
import org.iop.db.CantSaveProposalExistException;
import org.iop.db.CantUpdateProposalException;
import org.iop.exceptions.CantSendProposalException;
import org.iop.exceptions.CantSendVoteException;
import org.iop.exceptions.InvalidProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import iop.org.iop_contributors_app.R;
import iop_sdk.blockchain.NotConnectedPeersException;
import iop_sdk.blockchain.explorer.TransactionFinder;
import iop_sdk.blockchain.explorer.TransactionFinderListener;
import iop_sdk.forum.wrapper.ServerWrapper;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.Vote;
import iop_sdk.wallet.BlockchainManager;
import iop_sdk.wallet.BlockchainManagerListener;
import iop_sdk.wallet.exceptions.InsuficientBalanceException;
import iop_sdk.wallet.utils.BlockchainState;

import static org.iop.WalletConstants.BLOCKCHAIN_STATE_BROADCAST_THROTTLE_MS;
import static org.iop.WalletConstants.CONTEXT;
import static org.iop.WalletConstants.SHOW_BLOCKCHAIN_OFF_DIALOG;
import static org.iop.intents.constants.IntentsConstants.ACTION_NOTIFICATION;
import static org.iop.intents.constants.IntentsConstants.CANT_SAVE_PROPOSAL_DIALOG;
import static org.iop.intents.constants.IntentsConstants.COMMON_ERROR_DIALOG;
import static org.iop.intents.constants.IntentsConstants.INSUFICIENTS_FUNDS_DIALOG;
import static org.iop.intents.constants.IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENTE_EXTRA_MESSAGE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED_IS_TRANSACTION_MINE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_PROPOSAL_TRANSACTION_ARRIVED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_PROPOSAL_TRANSACTION_SUCCED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_VOTE_TRANSACTION_SUCCED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_DATA;
import static org.iop.intents.constants.IntentsConstants.INTENT_DIALOG;
import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;
import static org.iop.intents.constants.IntentsConstants.INTENT_NOTIFICATION;
import static org.iop.intents.constants.IntentsConstants.INVALID_PROPOSAL_DIALOG;
import static org.iop.intents.constants.IntentsConstants.UNKNOWN_ERROR_DIALOG;

/**
 * Created by mati on 11/11/16.
 */

public class BlockchainServiceImpl extends Service implements BlockchainService{

    private static final Logger LOG = LoggerFactory.getLogger(BlockchainServiceImpl.class);
    private static final String TAG = "BlockchainServiceImpl";

    private AppController application;
    /** Root Module */
    private WalletModule walletModule;
    /** Blockchain module manager */
    private BlockchainManager blockchainManager;
    /** Configurations */
    private WalletPreferencesConfiguration conf;
    /** Special transaction explorer */
    private TransactionFinder transactionFinder;

    private final Handler handler = new Handler();
    private final Handler delayHandler = new Handler();
    // Esto es para dejar prendido el telefono
    private PowerManager.WakeLock wakeLock;

    private ExecutorService executorService;
    // listeners
    private PeerConnectivityListener peerConnectivityListener;

    private boolean resetBlockchainOnShutdown = false;

    private NotificationManager nm;
    private final Set<BlockchainState.Impediment> impediments = EnumSet.noneOf(BlockchainState.Impediment.class);

    private long serviceCreatedAt;

    private LocalBroadcastManager localBroadcast;

    // Notifications
    private int notificationCount;
    private Coin notificationAccumulatedAmount = Coin.ZERO;


    private final class PeerConnectivityListener
            implements PeerConnectedEventListener, PeerDisconnectedEventListener, SharedPreferences.OnSharedPreferenceChangeListener {
        private int peerCount;
        private AtomicBoolean stopped = new AtomicBoolean(false);

        public PeerConnectivityListener() {
            conf.registerOnSharedPreferenceChangeListener(this);
        }

        public void stop() {
            stopped.set(true);

            conf.unregisterOnSharedPreferenceChangeListener(this);

            nm.cancel(WalletConstants.NOTIFICATION_ID_CONNECTED);
        }

        @Override
        public void onPeerConnected(final Peer peer, final int peerCount) {
            LOG.info("######### Peer connected!! ######");
            android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic__launcher)
                            .setContentTitle("Peer connected!")
                            .setContentText("OnPeerConnected: "+peer.getAddress().toString());

            nm.notify(2,mBuilder.build());

            this.peerCount = peerCount;
            changed(peerCount);
        }

        @Override
        public void onPeerDisconnected(final Peer peer, final int peerCount) {
            this.peerCount = peerCount;
            changed(peerCount);
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            if (WalletPreferencesConfiguration.PREFS_KEY_CONNECTIVITY_NOTIFICATION.equals(key))
                changed(peerCount);
        }

        private void changed(final int numPeers) {
            if (stopped.get())
                return;

            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    final boolean connectivityNotificationEnabled = conf.getConnectivityNotificationEnabled();

                    if (!connectivityNotificationEnabled || numPeers == 0) {
                        nm.cancel(WalletConstants.NOTIFICATION_ID_CONNECTED);
                    }
                    else {
//                        final Notification.ProposalTransactionBuilder notification = new Notification.ProposalTransactionBuilder(BlockchainServiceImpl.this);
//                        notification.setSmallIcon(R.drawable.stat_sys_peers, numPeers > 4 ? 4 : numPeers);
//                        notification.setContentTitle(getString(R.string.app_name));
//                        notification.setContentText(getString(R.string.notification_peers_connected_msg, numPeers));
//                        notification.setContentIntent(PendingIntent.getActivity(BlockchainServiceImpl.this, 0, new Intent(BlockchainServiceImpl.this,
//                                WalletActivity.class), 0));
//                        notification.setWhen(System.currentTimeMillis());
//                        notification.setOngoing(true);
//                        nm.notify(Constants.NOTIFICATION_ID_CONNECTED, notification.getNotification());
                    }

                    // send broadcast
//                    broadcastPeerState(numPeers);
                }
            });
        }
    }

    private final PeerDataEventListener blockchainDownloadListener = new AbstractPeerDataEventListener() {

        private final AtomicLong lastMessageTime = new AtomicLong(0);

        @Override
        public void onBlocksDownloaded(final Peer peer, final Block block, final FilteredBlock filteredBlock, final int blocksLeft)
        {
            LOG.info("############# on Blockcs downloaded ###########");
            LOG.info("Peer: "+peer+", Block: "+block+", left: "+blocksLeft);
            LOG.info("############# on Blockcs downloaded end ###########");


            delayHandler.removeCallbacksAndMessages(null);



            final long now = System.currentTimeMillis();
            if (now - lastMessageTime.get() > BLOCKCHAIN_STATE_BROADCAST_THROTTLE_MS)
                delayHandler.post(new RunnableBlockChecker(block));
            else
                delayHandler.postDelayed(new RunnableBlockChecker(block), BLOCKCHAIN_STATE_BROADCAST_THROTTLE_MS);
        }

//        private final Runnable runnable = new Runnable() {
//            @Override
//            public void run()
//            {
//                lastMessageTime.set(System.currentTimeMillis());
//
//
//                int bestBlockHeight = conf.maybeIncrementBestChainHeightEver(blockchainManager.getChainHeadHeight());
//
//                if (bestBlockHeight!=-1 && !transactionFinder.getLastBestChainHash().equals(block.getHash().toString())){
//                    List<String> txHashes = null;
//                    ServerWrapper.RequestProposalsResponse requestProposalsResponse= null;
//
//                    if (application.isVotingApp()) {
//                        // obtain proposal contracts to filter
//                        requestProposalsResponse = walletModule.requestProposals(blockchainManager.getChainHeadHeight());
//                        if (requestProposalsResponse != null) {
//                            txHashes = requestProposalsResponse.getTxHashes();
//
//                        }
//                    }
//                }
//                // todo: ver esto, lo broadcastea para que todos sepan el estado de la blockchain
////                broadcastBlockchainState();
//            }
//        };


        final class RunnableBlockChecker implements Runnable{

            private Block block;

            public RunnableBlockChecker(Block block) {
                this.block = block;
            }

            @Override
            public void run() {
                lastMessageTime.set(System.currentTimeMillis());


                final int bestBlockHeight = conf.maybeIncrementBestChainHeightEver(blockchainManager.getChainHeadHeight());

                if (application.isVotingApp()) {

                    LOG.info("executing download voting");

                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //bestBlockHeight != -1 &&
                                if (!transactionFinder.getLastBestChainHash().equals(block.getHash().toString())) {
                                    LOG.info("executing download-1");
                                    List<Proposal> proposals = null;
                                    ServerWrapper.RequestProposalsResponse requestProposalsResponse = null;

                                    // obtain proposal contracts to filter
                                    requestProposalsResponse = walletModule.requestProposalsFullTx(blockchainManager.getChainHeadHeight());
                                    if (requestProposalsResponse != null) {
                                        proposalsArrive(requestProposalsResponse.getProposals());
                                    }

                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }


                    });


                }else {

                    LOG.info("executing download active proposals (para actualizarlas)");
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // update own contract proposal
                                List<Proposal> list = walletModule.getActiveProposalsInBlockchain();
                                if (!list.isEmpty()) {
                                    LOG.info("Active proposals: "+Arrays.toString(list.toArray()));
                                    ServerWrapper.RequestProposalsResponse requestProposalsResponse = walletModule.requestProposalsFullTx(list);
                                    if (requestProposalsResponse != null) {
                                        proposalsArrive(requestProposalsResponse.getProposals());
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });

                }

                // todo: ver esto, lo broadcastea para que todos sepan el estado de la blockchain
//                broadcastBlockchainState();
            }
        }
        private void proposalsArrive(List<Proposal> proposals){
            for (Proposal proposal : proposals) {
                try {
                    Proposal temp = proposal;
                    proposal = walletModule.proposalArrive(proposal);
                    if (proposal!=null) {
                        LOG.info("Proposal arrive!");
                        // notify state
                        broadcastProposalArrived(proposal);
                    }else {
                        LOG.info("Proposal arrive return null, state: "+temp.getState());
                    }
                } catch (CantSaveProposalException e) {
                    LOG.info(e.getMessage());
                } catch (CantSaveProposalExistException e) {
                    // nothing
                    LOG.info("proposal exist! " + proposal.getGenesisTxHash());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    };



    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
            {
                final NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                final boolean hasConnectivity = networkInfo.isConnected();
                LOG.info("network is {}, state {}/{}", hasConnectivity ? "up" : "down", networkInfo.getState(), networkInfo.getDetailedState());

                if (hasConnectivity)
                    impediments.remove(BlockchainState.Impediment.NETWORK);
                else
                    impediments.add(BlockchainState.Impediment.NETWORK);
//                check();
            }
            else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action))
            {
                LOG.info("device storage low");

                impediments.add(BlockchainState.Impediment.STORAGE);
//                check();
            }
            else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action))
            {
                LOG.info("device storage ok");

                impediments.remove(BlockchainState.Impediment.STORAGE);
//                check();
            }
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    check();
                }
            });
        }
    };

    private WalletCoinsReceivedEventListener coinReceiverListener = new WalletCoinsReceivedEventListener() {

        android.support.v4.app.NotificationCompat.Builder mBuilder;
        PendingIntent deleteIntent;

        @Override
        public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {

            //todo: acá falta una validación para saber si la transaccion es mia.

            boolean isTransactionMine = walletModule.isProposalTransaction(transaction);
            int depthInBlocks = transaction.getConfidence().getDepthInBlocks();

            Intent intent = new Intent(ACTION_NOTIFICATION);
            intent.putExtra(INTENT_BROADCAST_TYPE, INTENT_DATA + INTENT_NOTIFICATION);
            intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_ON_COIN_RECEIVED);
            intent.putExtra(INTENT_BROADCAST_DATA_ON_COIN_RECEIVED_IS_TRANSACTION_MINE,isTransactionMine);

            localBroadcast.sendBroadcast(intent);

            //final Address address = WalletUtils.getWalletAddressOfReceived(WalletConstants.NETWORK_PARAMETERS,transaction, wallet);
            final Coin amount = transaction.getValue(wallet);
            final TransactionConfidence.ConfidenceType confidenceType = transaction.getConfidence().getConfidenceType();

            notificationCount++;
            notificationAccumulatedAmount = notificationAccumulatedAmount.add(amount);

            if (!isTransactionMine ){//&& depthInBlocks>1) {


                Intent resultIntent = new Intent(getApplicationContext(),BlockchainServiceImpl.this.getClass());
                resultIntent.setAction(ACTION_CANCEL_COINS_RECEIVED);
                deleteIntent = PendingIntent.getService(BlockchainServiceImpl.this, 0, resultIntent,PendingIntent.FLAG_CANCEL_CURRENT);


                 mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic__launcher)
                                .setContentTitle("IoPs received!")
                                .setContentText("Transaction received for a value of " + BtcFormat.getInstance().format(notificationAccumulatedAmount.getValue()))
                                .setAutoCancel(false)
                                .setDeleteIntent(deleteIntent);

                nm.notify(1, mBuilder.build());
            } else {
                android.support.v4.app.NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic__launcher)
                                .setContentTitle("IoPs received!")
                                .setContentText("Transaction received for a value of " + amount.toFriendlyString())
                                .setSubText("This transaction is not confirmed yet, will be confirmed in the next 10 minutes")
                                .setDeleteIntent(deleteIntent);

                nm.notify(5, mBuilder.build());
            }
        }
    };

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    private TransactionFinderListener transactionFinderListener = new TransactionFinderListener() {
        @Override
        public void onReceiveTransaction(final Transaction tx) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {

                    try {
                        org.bitcoinj.core.Context.propagate(CONTEXT);

                        broadcastProposalArrived(
                            // check and save proposal in db
                                walletModule.txProposalArrive(tx)

                        );
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
//            Intent intent = new Intent(ACTION_NOTIFICATION);
//            intent.putExtra(INTENT_BROADCAST_TYPE,INTENT_DATA);
//            intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_ON_COIN_RECEIVED);
//            localBroadcast.sendBroadcast(intent);
        }
    };

    public class LocalBinder extends Binder {
        public BlockchainService getService()
        {
            return BlockchainServiceImpl.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LOG.debug(".onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        LOG.debug(".onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        serviceCreatedAt = System.currentTimeMillis();
        LOG.debug(".onCreate()");
        super.onCreate();

        // wake lock
        final String lockName = getPackageName() + " blockchain sync";
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, lockName);

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        localBroadcast = LocalBroadcastManager.getInstance(this);

        executorService = Executors.newFixedThreadPool(3);

        application = (AppController) getApplication();

        walletModule = application.getModule();
        blockchainManager = walletModule.getBlockchainManager();
        conf = (WalletPreferencesConfiguration) walletModule.getWalletManager().getConfigurations();

        //todo: esto no me gusta nada

        peerConnectivityListener = new PeerConnectivityListener();

//        broadcastPeerState(0);
        blockchainManager.init();


        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        registerReceiver(connectivityReceiver, intentFilter); // implicitly init PeerGroup

        // coins received
        walletModule.getWalletManager().getWallet().addCoinsReceivedEventListener(coinReceiverListener);
        walletModule.getWalletManager().getWallet().addTransactionConfidenceEventListener(new TransactionConfidenceEventListener() {
            @Override
            public void onTransactionConfidenceChanged(Wallet wallet, Transaction transaction) {
                try {
                    org.bitcoinj.core.Context.propagate(WalletConstants.CONTEXT);
                    if (transaction.getConfidence().getDepthInBlocks() > 1) {
                        Proposal proposal = null;
                        if ((proposal = walletModule.decodeProposalTransaction(transaction)) != null) {
                            try {
                                if (walletModule.proposalAcceptedInBlockchain(proposal)){
                                    proposal.setState(Proposal.ProposalState.SUBMITTED);
                                    broadcastProposalArrived(proposal);
                                }
                            } catch (CantUpdateProposalException e) {
                                e.printStackTrace();
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

//        wallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletEventListener);
//        wallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletEventListener);
//        wallet.addChangeEventListener(Threading.SAME_THREAD, walletEventListener);

//        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

    }


    /**
     * Create temporary file to save waiting transactions
     */
    private void createTempFileForTransactions(){
        try {
            File outputDir = getCacheDir(); // context being the Activity pointer
            File outputFile = new File(outputDir + "transactionsTemp");
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            LOG.info("service init command: " + intent
                    + (intent.hasExtra(Intent.EXTRA_ALARM_COUNT) ? " (alarm count: " + intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, 0) + ")" : ""));

            final String action = intent.getAction();

            if (BlockchainService.ACTION_BROADCAST_PROPOSAL_TRANSACTION.equals(action)) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Proposal proposal = (Proposal) intent.getSerializableExtra(INTENT_EXTRA_PROPOSAL);
                            if (walletModule.sendProposal(proposal)){
                                broadcastProposalSuced(proposal.getTitle());
                            }
                        }catch (InsuficientBalanceException e){
                            e.printStackTrace();
                            showDialogException(INSUFICIENTS_FUNDS_DIALOG,null);
                        }catch (CantSendProposalException e) {
                            e.printStackTrace();
                            showDialogException(UNKNOWN_ERROR_DIALOG, e.getMessage());
                        }catch (CantSaveProposalException e){
                            e.printStackTrace();
                            showDialogException(CANT_SAVE_PROPOSAL_DIALOG,e.getMessage());
                        } catch (InvalidProposalException e) {
                            e.printStackTrace();
                            showDialogException(INVALID_PROPOSAL_DIALOG, e.getMessage()+".\n\nEdit in the forum first if you want any change.");
                        } catch (NotConnectedPeersException e) {
                            e.printStackTrace();
                            showDialogException(COMMON_ERROR_DIALOG,"Not connected peers, please try again later");
                            check();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });


            } else if(ACTION_BROADCAST_VOTE_PROPOSAL_TRANSACTION.equals(action)){
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Obtengo el voto del intent
                            Vote vote = (Vote) intent.getSerializableExtra(INTENT_EXTRA_PROPOSAL_VOTE);
                            if (walletModule.sendVote(vote)) {
                                broadcastVoteSucced(vote);
                            }
                        } catch (InsuficientBalanceException e) {
                            showDialogException(INSUFICIENTS_FUNDS_DIALOG,null);
                        } catch (NotConnectedPeersException e) {
                            showDialogException(COMMON_ERROR_DIALOG,"Not connected peers, please try again later");
                        } catch (CantSendVoteException e) {
                            showDialogException(UNKNOWN_ERROR_DIALOG, e.getMessage());
                        } catch (iop_sdk.wallet.CantSendVoteException e) {
                            showDialogException(COMMON_ERROR_DIALOG,e.getMessage());
                        }

                    }
                });

            } else if (BlockchainService.ACTION_CANCEL_COINS_RECEIVED.equals(action)) {
                notificationCount = 0;
                notificationAccumulatedAmount = Coin.ZERO;
//                notificationAddresses.clear();

                nm.cancel(WalletConstants.NOTIFICATION_ID_COINS_RECEIVED);
            } else if (BlockchainService.ACTION_RESET_BLOCKCHAIN.equals(action)) {
                LOG.info("will remove blockchain on service shutdown");

                resetBlockchainOnShutdown = true;
                stopSelf();
            } else if (BlockchainService.ACTION_BROADCAST_TRANSACTION.equals(action)) {
                // Acá se broadcastea la transacción a la blockchain
                blockchainManager.broadcastTransaction(intent.getByteArrayExtra(BlockchainService.ACTION_BROADCAST_TRANSACTION_HASH));
            }
        } else {
            LOG.warn("service restart, although it was started as non-sticky");
        }
        return START_NOT_STICKY;
    }


    private void showDialogException(int dialogType, String message){
        Intent intent = new Intent(ACTION_NOTIFICATION);
        intent.putExtra(INTENT_BROADCAST_TYPE,INTENT_DIALOG);
        intent.putExtra(INTENTE_BROADCAST_DIALOG_TYPE,dialogType);
//        intent.putExtra(INTENT_DIALOG,dialogType);public static final String ACTION_RECEIVE_EXCEPTION = CreateProposalActivity.class.getName() + "_receive_exception";
        intent.putExtra(INTENTE_EXTRA_MESSAGE,message);
        localBroadcast.sendBroadcast(intent);
    }

    private void broadcastProposalSuced(String title){
        Intent intent = new Intent(ACTION_NOTIFICATION);
        intent.putExtra("title",title);
        intent.putExtra(INTENT_BROADCAST_TYPE,INTENT_DATA+INTENT_NOTIFICATION);
        intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_PROPOSAL_TRANSACTION_SUCCED);
        localBroadcast.sendBroadcast(intent);
    }

    private void broadcastVoteSucced(Vote vote){
        Intent intent = new Intent(ACTION_NOTIFICATION);
//        intent.putExtra(I,title);
        intent.putExtra(INTENT_BROADCAST_TYPE,INTENT_DATA+INTENT_NOTIFICATION);
        intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_VOTE_TRANSACTION_SUCCED);
        intent.putExtra(INTENT_EXTRA_PROPOSAL_VOTE,vote);
        localBroadcast.sendBroadcast(intent);
    }

    private void broadcastProposalArrived(Proposal proposal) {
        Intent intent = new Intent(ACTION_NOTIFICATION);
//        intent.putExtra(I,title);
        intent.putExtra(INTENT_BROADCAST_TYPE,INTENT_DATA+INTENT_NOTIFICATION);
        intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_PROPOSAL_TRANSACTION_ARRIVED);
        intent.putExtra(INTENT_EXTRA_PROPOSAL,proposal);
        localBroadcast.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        try {

            LOG.debug(".onDestroy()");

            org.bitcoinj.core.Context.propagate(WalletConstants.CONTEXT);

            // esto es para encender el servicio de la blockchain cada cierto tiempo, verlo despues en el otro proyecto..
//            WalletApplication.scheduleStartBlockchainService(this);

//            unregisterReceiver(tickReceiver);

//            application.getWallet().removeChangeEventListener(walletEventListener);
//            application.getWallet().removeCoinsSentEventListener(walletEventListener);
//            application.getWallet().removeCoinsReceivedEventListener(walletEventListener);


            // coins received
            walletModule.getWalletManager().getWallet().removeCoinsReceivedEventListener(coinReceiverListener);

            unregisterReceiver(connectivityReceiver);

            // remove listeners
            blockchainManager.removeDisconnectedEventListener(peerConnectivityListener);
            blockchainManager.removeConnectivityListener(peerConnectivityListener);
            // destroy the blockchain
            blockchainManager.destroy(resetBlockchainOnShutdown);

            peerConnectivityListener.stop();

            delayHandler.removeCallbacksAndMessages(null);

            if (wakeLock.isHeld()) {
                LOG.debug("wakelock still held, releasing");
                wakeLock.release();
            }

            super.onDestroy();

            LOG.info("service was up for " + ((System.currentTimeMillis() - serviceCreatedAt) / 1000 / 60) + " minutes");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private AtomicBoolean isChecking = new AtomicBoolean(false);

    /**
     * Check and download the blockchain if it needed
     */
    private void check(){

        org.bitcoinj.core.Context.propagate(WalletConstants.CONTEXT);

        try {

            if (!isChecking.getAndSet(true)) {

                List<String> txHashes = null;
                ServerWrapper.RequestProposalsResponse requestProposalsResponse= null;

                if (application.isVotingApp()) {
                    // obtain proposal contracts to filter
                    requestProposalsResponse = walletModule.requestProposals(blockchainManager.getChainHeadHeight());
                    if (requestProposalsResponse != null) {
                        txHashes = requestProposalsResponse.getTxHashesNew();
                    }
                }

                final List<String> finalTxHashes = txHashes;
                final ServerWrapper.RequestProposalsResponse finalRequestProposalsResponse = requestProposalsResponse;
                blockchainManager.addBlockchainManagerListener(new BlockchainManagerListener() {
                    @Override
                    public void peerGroupInitialized(PeerGroup peerGroup) {
                        if (application.isVotingApp()) {
                            // new thing
                            transactionFinder = walletModule.getAndCreateFinder(peerGroup);
                            transactionFinder.addTransactionFinderListener(transactionFinderListener);
                            if (finalRequestProposalsResponse!=null) {
                                transactionFinder.setLastBestChainHash(finalRequestProposalsResponse.getBestChainHash());
                                for (String txHash : finalTxHashes) {
                                    transactionFinder.addTx(txHash);
                                    //todo: falta agregar el output de lockeo en el finder como hice abajo..
                                }
//                        transactionFinder.addTx("068af403e4f0935c419fb71ab625fb8364d7565a436c35a997ba6a75c9883b2b");
//                        transactionFinder.addWatchedOutpoint(Sha256Hash.wrap("068af403e4f0935c419fb71ab625fb8364d7565a436c35a997ba6a75c9883b2b"),0,36);
                                transactionFinder.startDownload();
                            }
                        }
                    }

                    @Override
                    public void onBlockchainOff(Set<BlockchainState.Impediment> impediments) {
                        String dialogText = "Blockchain impediment: ";
                        int i = 0;
                        for (BlockchainState.Impediment impediment : impediments) {
                            dialogText += impediment.toString();
                            if (i != 0) dialogText += " , ";
                            i++;
                        }
                        walletModule.getAppController().showDialog(SHOW_BLOCKCHAIN_OFF_DIALOG, dialogText);
                    }

                    @Override
                    public void checkStart() {
                        LOG.debug("acquiring wakelock");
                        wakeLock.acquire();
                    }

                    @Override
                    public void checkEnd() {
                        LOG.debug("releasing wakelock");
                        wakeLock.release();
                    }
                });

                blockchainManager.check(
                        impediments,
                        peerConnectivityListener,
                        peerConnectivityListener,
                        blockchainDownloadListener);

                //todo: ver si conviene esto..
//        broadcastBlockchainState();

                isChecking.set(false);
            } else {
                LOG.error("algo malo pasa");
                Log.e(TAG, "algo malo pasa..");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onTrimMemory(final int level) {
        LOG.info("onTrimMemory({}) called", level);

        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            LOG.warn("low memory detected, stopping service");
            stopSelf();
        }
    }

    @Override
    public BlockchainState getBlockchainState() {
        return blockchainManager.getBlockchainState(impediments);
    }

    @Override
    public List<Peer> getConnectedPeers() {
        return blockchainManager.getConnectedPeers();
    }

    @Override
    public List<StoredBlock> getRecentBlocks(final int maxBlocks) {
        return blockchainManager.getRecentBlocks(maxBlocks);
    }


}
