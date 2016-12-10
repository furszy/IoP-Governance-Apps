package iop.org.iop_contributors_app.wallet;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDataEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import iop.org.iop_contributors_app.services.BlockchainState;
import iop.org.iop_contributors_app.configurations.WalletPreferencesConfiguration;
import iop.org.iop_contributors_app.wallet.util.RegtestUtil;


/**
 * Created by mati on 12/11/16.
 */

public class BlockchainManager {

    private static final Logger LOG = LoggerFactory.getLogger(BlockchainManager.class);

    // system
    private ContextWrapper context;

    // wallet files..
    private WalletManager walletManager;
    private WalletPreferencesConfiguration conf;

    private BlockStore blockStore;
    private File blockChainFile;
    private BlockChain blockChain;
    @Nullable
    private PeerGroup peerGroup;



    public BlockchainManager(ContextWrapper contextWrapper,WalletManager walletManager, WalletPreferencesConfiguration conf) {
        this.walletManager = walletManager;
        this.conf = conf;
        this.context = contextWrapper;

    }

    public void start(){
        // todo: en vez de que el service este maneje el blockchain deberia crear una clase que lo haga..
        blockChainFile = new File(context.getDir("blockstore", Context.MODE_PRIVATE), WalletConstants.Files.BLOCKCHAIN_FILENAME);
        final boolean blockChainFileExists = blockChainFile.exists();

        if (!blockChainFileExists) {
            LOG.info("blockchain does not exist, resetting wallet");
            walletManager.getWallet().reset();
        }

        // Create the blockstore
        try {
            blockStore = new SPVBlockStore(WalletConstants.NETWORK_PARAMETERS, blockChainFile);
            blockStore.getChainHead(); // detect corruptions as early as possible

            final long earliestKeyCreationTime = walletManager.getWallet().getEarliestKeyCreationTime();

            // todo: esto es para cuando tengamos checkpoints
//			if (!blockChainFileExists && earliestKeyCreationTime > 0)
//			{
//				try
//				{
//					final Stopwatch watch = Stopwatch.createStarted();
//					final InputStream checkpointsInputStream = getAssets().open(Constants.Files.CHECKPOINTS_FILENAME);
//					CheckpointManager.checkpoint(Constants.NETWORK_PARAMETERS, checkpointsInputStream, blockStore, earliestKeyCreationTime);
//					watch.stop();
//					log.info("checkpoints loaded from '{}', took {}", Constants.Files.CHECKPOINTS_FILENAME, watch);
//				}
//				catch (final IOException x)
//				{
//					log.error("problem reading checkpoints, continuing without", x);
//				}
//			}

        }catch (final BlockStoreException x) {
            blockChainFile.delete();

            final String msg = "blockstore cannot be created";
            LOG.error(msg, x);
            throw new Error(msg, x);
        }

        // create the blockchain
        try {
            blockChain = new BlockChain(WalletConstants.NETWORK_PARAMETERS, walletManager.getWallet(), blockStore);
        }catch (final BlockStoreException x) {
            throw new Error("blockchain cannot be created", x);
        }

    }

    public void addDiscuonnectedEventListener(PeerDisconnectedEventListener listener){
        peerGroup.addDisconnectedEventListener(listener);
    }

    public void addConnectivityListener(PeerConnectedEventListener listener){
        peerGroup.addConnectedEventListener(listener);
    }

    public void removeDisconnectedEventListener(PeerDisconnectedEventListener listener){
        peerGroup.removeDisconnectedEventListener(listener);
    }

    public void removeConnectivityListener(PeerConnectedEventListener listener){
        peerGroup.removeConnectedEventListener(listener);
    }

    /**
     *
     * @param transactionHash
     */
    public ListenableFuture<Transaction> broadcastTransaction(byte[] transactionHash) {
        final Sha256Hash hash = Sha256Hash.wrap(transactionHash);
        final Transaction tx = walletManager.getWallet().getTransaction(hash);

        if (peerGroup != null) {
            LOG.info("broadcasting transaction " + tx.getHashAsString());
            TransactionBroadcast transactionBroadcast = peerGroup.broadcastTransaction(tx,1);
            return transactionBroadcast.future();
        } else {
            LOG.info("peergroup not available, not broadcasting transaction " + tx.getHashAsString());
            return null;
        }
    }

    public void destroy(boolean resetBlockchainOnShutdown) {
        if (peerGroup != null) {

//            peerGroup.removeDisconnectedEventListener(peerConnectivityListener);
//            peerGroup.removeConnectedEventListener(peerConnectivityListener);
            peerGroup.removeWallet(walletManager.getWallet());
            peerGroup.stopAsync();

            LOG.info("peergroup stopped");

        }


        try {
            blockStore.close();
        } catch (final BlockStoreException x) {
            throw new RuntimeException(x);
        }

        // save the wallet
        walletManager.saveWallet();

        if (resetBlockchainOnShutdown) {
            LOG.info("removing blockchain");
            blockChainFile.delete();
        }
    }

    public void check(Set<BlockchainState.Impediment> impediments, PowerManager.WakeLock wakeLock, PeerConnectedEventListener peerConnectivityListener,PeerDisconnectedEventListener peerDisconnectedEventListener ,PeerDataEventListener blockchainDownloadListener){

        final Wallet wallet = walletManager.getWallet();

        if (impediments.isEmpty() && peerGroup == null){

            LOG.debug("acquiring wakelock");
            wakeLock.acquire();

            // consistency check
            final int walletLastBlockSeenHeight = wallet.getLastBlockSeenHeight();
            final int bestChainHeight = blockChain.getBestChainHeight();
            if (walletLastBlockSeenHeight != -1 && walletLastBlockSeenHeight != bestChainHeight) {
                final String message = "wallet/blockchain out of sync: " + walletLastBlockSeenHeight + "/" + bestChainHeight;
                LOG.error(message);
//                CrashReporter.saveBackgroundTrace(new RuntimeException(message), application.packageInfo());
            }

            LOG.info("starting peergroup");
            peerGroup = new PeerGroup(WalletConstants.NETWORK_PARAMETERS, blockChain);
            peerGroup.setDownloadTxDependencies(0); // recursive implementation causes StackOverflowError
            peerGroup.addWallet(wallet);
            peerGroup.setUserAgent(WalletConstants.USER_AGENT, context.packageInfo().versionName);
            peerGroup.addConnectedEventListener(peerConnectivityListener);
            peerGroup.addDisconnectedEventListener(peerDisconnectedEventListener);

            // check memory, esto lo tengo qu ehacer mejor..
            final int maxConnectedPeers = 2;//context.isMemoryLow() ? 4 : 6 ;

            final String trustedPeerHost = null;//config.getTrustedPeerHost();
            final boolean hasTrustedPeer = trustedPeerHost != null;

            final boolean connectTrustedPeerOnly = false;//hasTrustedPeer && config.getTrustedPeerOnly();
            peerGroup.setMaxConnections(connectTrustedPeerOnly ? 1 : maxConnectedPeers);
            peerGroup.setConnectTimeoutMillis(WalletConstants.PEER_TIMEOUT_MS);
            peerGroup.setPeerDiscoveryTimeoutMillis(WalletConstants.PEER_DISCOVERY_TIMEOUT_MS);

//            peerGroup.addPeerDiscovery(new PeerDiscovery() {
//
//                private final PeerDiscovery normalPeerDiscovery = MultiplexingDiscovery.forServices(WalletConstants.NETWORK_PARAMETERS, 0);
//
//                @Override
//                public InetSocketAddress[] getPeers(final long services, final long timeoutValue, final TimeUnit timeoutUnit)
//                        throws PeerDiscoveryException
//                {
//                    final List<InetSocketAddress> peers = new LinkedList<InetSocketAddress>();
//
//                    boolean needsTrimPeersWorkaround = false;
//
//                    if (hasTrustedPeer)
//                    {
//                        LOG.info("trusted peer '" + trustedPeerHost + "'" + (connectTrustedPeerOnly ? " only" : ""));
//
//                        final InetSocketAddress addr = new InetSocketAddress(trustedPeerHost, WalletConstants.NETWORK_PARAMETERS.getPort());
//                        if (addr.getAddress() != null)
//                        {
//                            peers.add(addr);
//                            needsTrimPeersWorkaround = true;
//                        }
//                    }
//
//                    if (!connectTrustedPeerOnly)
//                        peers.addAll(Arrays.asList(normalPeerDiscovery.getPeers(services, timeoutValue, timeoutUnit)));
//
//                    // workaround because PeerGroup will shuffle peers
//                    if (needsTrimPeersWorkaround)
//                        while (peers.size() >= maxConnectedPeers)
//                            peers.remove(peers.size() - 1);
//
//                    return peers.toArray(new InetSocketAddress[0]);
//                }
//
//                @Override
//                public void shutdown()
//                {
//                    normalPeerDiscovery.shutdown();
//                }
//            });

            if (WalletConstants.NETWORK_PARAMETERS.equals(RegTestParams.get())) {
				for (PeerAddress peerAddress : RegtestUtil.getConnectedPeers(WalletConstants.NETWORK_PARAMETERS,conf.getNode())) {
					peerGroup.addAddress(peerAddress);
				}
			}else throw new RuntimeException("Pelotudo descomentá lo de arriba please");

            // start peergroup
            peerGroup.startAsync();
            peerGroup.startBlockChainDownload(blockchainDownloadListener);

        }else if (!impediments.isEmpty() && peerGroup != null) {
            LOG.info("stopping peergroup");
            peerGroup.removeDisconnectedEventListener(peerDisconnectedEventListener);
            peerGroup.removeConnectedEventListener(peerConnectivityListener);
            peerGroup.removeWallet(wallet);
            peerGroup.stopAsync();
            peerGroup = null;

            LOG.debug("releasing wakelock");
            wakeLock.release();
        }

        //todo: falta hacer el tema de la memoria, hoy en día si se queda sin memoria no dice nada..
        //todo: ver si conviene esto..
//        broadcastBlockchainState();

    }


    public BlockchainState getBlockchainState(Set<BlockchainState.Impediment> impediments)
    {
        final StoredBlock chainHead = blockChain.getChainHead();
        final Date bestChainDate = chainHead.getHeader().getTime();
        final int bestChainHeight = chainHead.getHeight();
        final boolean replaying = chainHead.getHeight() < conf.getBestChainHeightEver();

        return new BlockchainState(bestChainDate, bestChainHeight, replaying, impediments);
    }


    public List<Peer> getConnectedPeers() {
        if (peerGroup != null)
            return peerGroup.getConnectedPeers();
        else
            return null;
    }


    public List<StoredBlock> getRecentBlocks(final int maxBlocks) {

        final List<StoredBlock> blocks = new ArrayList<StoredBlock>(maxBlocks);

        try
        {
            StoredBlock block = blockChain.getChainHead();

            while (block != null)
            {
                blocks.add(block);

                if (blocks.size() >= maxBlocks)
                    break;

                block = block.getPrev(blockStore);
            }
        }
        catch (final BlockStoreException x)
        {
            // swallow
        }

        return blocks;
    }

    public int getChainHeadHeight() {
        return blockChain.getChainHead().getHeight();
    }

}
