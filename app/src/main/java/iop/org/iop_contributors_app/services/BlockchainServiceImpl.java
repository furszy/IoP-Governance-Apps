package iop.org.iop_contributors_app.services;

import android.app.NotificationManager;
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

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.AbstractPeerDataEventListener;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDataEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.configurations.WalletPreferencesConfiguration;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.ui.CreateProposalActivity;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.wallet.BlockchainManager;
import iop.org.iop_contributors_app.wallet.InvalidProposalException;
import iop.org.iop_contributors_app.wallet.db.CantSaveProposalException;
import iop.org.iop_contributors_app.wallet.exceptions.CantSendProposalException;
import iop.org.iop_contributors_app.wallet.WalletConstants;
import iop.org.iop_contributors_app.wallet.WalletModule;
import iop.org.iop_contributors_app.wallet.exceptions.InsuficientBalanceException;

import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TRANSACTION_SUCCED;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_BROADCAST_TYPE;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_DATA;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_NOTIFICATION;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.CANT_SAVE_PROPOSAL_DIALOG;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.INSUFICIENTS_FUNDS_DIALOG;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.INVALID_PROPOSAL_DIALOG;
import static iop.org.iop_contributors_app.ui.CreateProposalActivity.UNKNOWN_ERROR_DIALOG;
import static iop.org.iop_contributors_app.ui.base.BaseActivity.ACTION_NOTIFICATION;
import static iop.org.iop_contributors_app.wallet.WalletConstants.BLOCKCHAIN_STATE_BROADCAST_THROTTLE_MS;

/**
 * Created by mati on 11/11/16.
 */

public class BlockchainServiceImpl extends Service implements BlockchainService{

    private static final Logger LOG = LoggerFactory.getLogger(BlockchainServiceImpl.class);
    private static final String TAG = "BlockchainServiceImpl";

    private ApplicationController application;

    private BlockchainManager blockchainManager;

    private WalletModule walletModule;

    private WalletPreferencesConfiguration conf;

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
                delayHandler.post(runnable);
            else
                delayHandler.postDelayed(runnable, BLOCKCHAIN_STATE_BROADCAST_THROTTLE_MS);
        }

        private final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                lastMessageTime.set(System.currentTimeMillis());


                conf.maybeIncrementBestChainHeightEver(blockchainManager.getChainHeadHeight());
                // todo: ver esto, lo broadcastea para que todos sepan el estado de la blockchain
//                broadcastBlockchainState();
            }
        };
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
                check();
            }
            else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action))
            {
                LOG.info("device storage low");

                impediments.add(BlockchainState.Impediment.STORAGE);
                check();
            }
            else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action))
            {
                LOG.info("device storage ok");

                impediments.remove(BlockchainState.Impediment.STORAGE);
                check();
            }
        }
    };

    private WalletCoinsReceivedEventListener coinReceiverListener = new WalletCoinsReceivedEventListener() {
        @Override
        public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {

            Intent intent = new Intent(ACTION_NOTIFICATION);

            android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("IoPs received!")
                            .setContentText("Transaction received for a value of "+coin.toFriendlyString());

            nm.notify(1,mBuilder.build());
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

        application = ApplicationController.getInstance();

        walletModule = application.getWalletModule();
        blockchainManager = application.getBlockchainManager();
        conf = application.getWalletConfigurations();

        //todo: esto no me gusta nada

        peerConnectivityListener = new PeerConnectivityListener();

//        broadcastPeerState(0);
        blockchainManager.start();


        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        registerReceiver(connectivityReceiver, intentFilter); // implicitly start PeerGroup


        // coins received
        walletModule.getWalletManager().getWallet().addCoinsReceivedEventListener(coinReceiverListener);

//        wallet.addCoinsReceivedEventListener(Threading.SAME_THREAD, walletEventListener);
//        wallet.addCoinsSentEventListener(Threading.SAME_THREAD, walletEventListener);
//        wallet.addChangeEventListener(Threading.SAME_THREAD, walletEventListener);

//        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

    }




    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        if (intent != null) {
            LOG.info("service start command: " + intent
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
                            showDialogException(INSUFICIENTS_FUNDS_DIALOG,null);
                        }catch (CantSendProposalException e) {
                            showDialogException(UNKNOWN_ERROR_DIALOG, e.getMessage());
                        }catch (CantSaveProposalException e){
                            showDialogException(CANT_SAVE_PROPOSAL_DIALOG,e.getMessage());
                        } catch (InvalidProposalException e) {
                            showDialogException(INVALID_PROPOSAL_DIALOG, e.getMessage()+".\n\nEdit in the forum first if you want any change.");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });


            } else if (BlockchainService.ACTION_CANCEL_COINS_RECEIVED.equals(action)) {
//                notificationCount = 0;
//                notificationAccumulatedAmount = Coin.ZERO;
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
        Intent intent = new Intent(CreateProposalActivity.ACTION_RECEIVE_EXCEPTION);
        intent.putExtra(CreateProposalActivity.INTENT_DIALOG,dialogType);
        intent.putExtra(CreateProposalActivity.INTENT_EXTRA_MESSAGE_DIALOG,message);
        Log.e(TAG,"insuficient funds exception");
        localBroadcast.sendBroadcast(intent);
    }

    private void broadcastProposalSuced(String title){
        Intent intent = new Intent(BaseActivity.ACTION_NOTIFICATION);
        intent.putExtra("title",title);
        intent.putExtra(INTENT_BROADCAST_TYPE,INTENT_DATA+INTENT_NOTIFICATION);
        intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_TRANSACTION_SUCCED);
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

        if (!isChecking.getAndSet(true)) {

            blockchainManager.check(
                    impediments,
                    wakeLock,
                    peerConnectivityListener,
                    peerConnectivityListener,
                    blockchainDownloadListener);

            //todo: ver si conviene esto..
//        broadcastBlockchainState();

            isChecking.set(false);
        }else {
            LOG.error("algo malo pasa");
            Log.e(TAG,"algo malo pasa..");
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
