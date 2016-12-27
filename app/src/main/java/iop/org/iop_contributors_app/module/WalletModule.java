package iop.org.iop_contributors_app.module;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;

import iop_sdk.blockchain.NotConnectedPeersException;
import iop_sdk.governance.propose.ProposalTransactionRequest;
import iop_sdk.wallet.VoteProposalRequest;
import iop.org.iop_contributors_app.intents.constants.IntentsConstants;
import iop.org.iop_contributors_app.services.BlockchainService;
import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop.org.iop_contributors_app.services.ServicesCodes;
import iop.org.iop_contributors_app.configurations.WalletPreferencesConfiguration;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.ui.voting.db.VotesDaoImp;
import iop.org.iop_contributors_app.ui.voting.util.VoteWrapper;
import iop_sdk.wallet.BlockchainManager;
import iop.org.iop_contributors_app.module.exceptions.CantRestoreEncryptedWallet;
import iop.org.iop_contributors_app.module.exceptions.CantSendTransactionException;
import iop.org.iop_contributors_app.module.exceptions.CantSendVoteException;
import iop.org.iop_contributors_app.module.exceptions.InvalidAddressException;
import iop.org.iop_contributors_app.module.exceptions.InvalidProposalException;
import org.iop.WalletConstants;
import iop_sdk.wallet.WalletManager;
import iop.org.iop_contributors_app.module.db.CantGetProposalException;
import iop.org.iop_contributors_app.module.db.CantSaveProposalException;
import iop.org.iop_contributors_app.module.db.CantSaveProposalExistException;
import iop.org.iop_contributors_app.module.db.CantUpdateProposalException;
import iop.org.iop_contributors_app.module.db.ProposalsDao;
import iop.org.iop_contributors_app.module.exceptions.CantSendProposalException;
import iop_sdk.wallet.exceptions.InsuficientBalanceException;
import iop.org.iop_sdk_android.core.profile_server.Profile;
import iop.org.iop_sdk_android.core.wrappers.PackageInfoAndroid;
import iop_sdk.blockchain.explorer.TransactionFinder;
import iop_sdk.crypto.CryptoBytes;
import iop_sdk.forum.CantCreateTopicException;
import iop_sdk.forum.CantUpdatePostException;
import iop_sdk.forum.ForumClient;
import iop_sdk.forum.ForumClientDiscourseImp;
import iop_sdk.forum.ForumConfigurations;
import iop_sdk.forum.ForumProfile;
import iop_sdk.forum.InvalidUserParametersException;
import iop_sdk.forum.wrapper.CantGetProposalsFromServer;
import iop_sdk.forum.wrapper.ServerWrapper;
import iop_sdk.global.ContextWrapper;
import iop_sdk.global.IntentWrapper;
import iop_sdk.global.exceptions.ConnectionRefusedException;
import iop_sdk.global.exceptions.NotValidParametersException;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.propose.ProposalTransactionBuilder;
import iop_sdk.governance.vote.Vote;;

import static org.iop.WalletConstants.SHOW_BLOCKCHAIN_OFF_DIALOG;

/**
 * Created by mati on 12/11/16.
 */

public class WalletModule implements ContextWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(WalletModule.class);

    private ApplicationController context;

    private WalletManager walletManager;
    private BlockchainManager blockchainManager;
    /** Class in charge of search and update special transactions */
    private TransactionFinder transactionFinder;

    /** Profile server profile */
    private Profile profile;

    private WalletPreferencesConfiguration configuration;

    private ForumClient forumClient;
    private ServerWrapper serverWrapper;
    private ForumConfigurations forumConfigurations;
    private String forumUrl;

    private long lockedBalance;

    /** Lock */
    private Object lock = new Object();

    //todo: leer lo que puse adnetro..
    private ProposalsDao proposalsDao;

    private VotesDaoImp votesDaoImp;



    public WalletModule(ApplicationController context, WalletPreferencesConfiguration configuration, ForumConfigurations forumConfigurations) {
        this.context = context;
        this.configuration = configuration;
        this.forumConfigurations = forumConfigurations;
        proposalsDao = new ProposalsDao(context);
        votesDaoImp = new VotesDaoImp(context);
        // locked outputs
        lockedBalance = proposalsDao.getTotalLockedBalance();
        forumConfigurations.getWrapperUrl();
        serverWrapper = new ServerWrapper(forumConfigurations.getWrapperUrl());
    }

    public void start(){
        // init
        walletManager = new WalletManager(this,configuration,new WalletManagerListenerImp(this));
        blockchainManager = new BlockchainManager(this,walletManager,configuration);
        forumClient = new ForumClientDiscourseImp(forumConfigurations,serverWrapper);

    }


    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return context.openFileOutput(name,mode);
    }

    @Override
    public FileOutputStream openFileOutputPrivateMode(String name) throws FileNotFoundException {
        return context.openFileOutput(name, android.content.Context.MODE_PRIVATE);
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return context.openFileInput(name);
    }

    @Override
    public File getFileStreamPath(String name) {
        return context.getFileStreamPath(name);
    }

    @Override
    public File getDir(String name, int mode) {
        return context.getDir(name,mode);
    }

    @Override
    public File getDirPrivateMode(String name) {
        return context.getDir(name,android.content.Context.MODE_PRIVATE);
    }

    /**
     *
     * @param service
     * @param command
     * @param args
     */
    @Override
    public void startService(int service, String command, Object... args) {

        Class<? extends Service> serviceClass = switchServices(service);
        //todo: despues podria cachear los intents
        Intent intent = new Intent(command,null,context,serviceClass);
        context.startService(intent);

    }

    @Override
    public void toast(String text) {
        Toast.makeText(context,text,Toast.LENGTH_LONG).show();
    }

    @Override
    public PackageInfoAndroid packageInfo() {
        return new PackageInfoAndroid(context.packageInfo());
    }

    @Override
    public boolean isMemoryLow() {
        return context.isMemoryLow();
    }

    @Override
    public InputStream openAssestsStream(String name) throws IOException {
        return context.getAssets().open(name);
    }

    @Override
    public String getPackageName() {
        return context.packageInfo().packageName;
    }

    @Override
    public void sendLocalBroadcast(IntentWrapper broadcast) {
        final Intent intent = new Intent();
        if (broadcast.getAction()!=null){
            intent.setAction(broadcast.getAction());
        }
        if (broadcast.getPackageName()!=null) {
            broadcast.setPackage(context.getPackageName());
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void showDialog(String id) {
        showDialog(id,null);
    }
    @Override
    public void showDialog(String id, String dialogText){
        if (id.equals(WalletConstants.SHOW_RESTORE_SUCCED_DIALOG)){
            showRestoreSuccedDialog();
        }else if (id.equals(SHOW_BLOCKCHAIN_OFF_DIALOG)){
            showBlockchainOff(dialogText);
        }
    }

    private void showRestoreSuccedDialog(){
        String message = context.getString(R.string.restore_wallet_dialog_success) +
                "\n\n" +
                context.getString(R.string.restore_wallet_dialog_success_replay);

        Intent intent = new Intent(BaseActivity.ACTION_NOTIFICATION);
        intent.putExtra(IntentsConstants.INTENT_BROADCAST_TYPE,IntentsConstants.INTENT_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE,IntentsConstants.RESTORE_SUCCED_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_EXTRA_MESSAGE, message);
        context.sendLocalBroadcast(intent);
    }

    private void showBlockchainOff(String dialogText){
        Intent intent = new Intent(BaseActivity.ACTION_NOTIFICATION);
        intent.putExtra(IntentsConstants.INTENT_BROADCAST_TYPE,IntentsConstants.INTENT_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE,IntentsConstants.COMMON_ERROR_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_EXTRA_MESSAGE, dialogText);
        context.sendLocalBroadcast(intent);
    }

    @Override
    public String[] fileList() {
        return context.fileList();
    }

    /**
     * Hardcoded method.
     */
    @Override
    public void stopBlockchainService() {
        startService(ServicesCodes.BLOCKCHAIN_SERVICE, BlockchainService.ACTION_RESET_BLOCKCHAIN);
    }

    private Class<? extends Service> switchServices(int service) {
        Class<? extends Service> clazz = null;
        switch (service){
            case ServicesCodes.BLOCKCHAIN_SERVICE:
                clazz = BlockchainServiceImpl.class;
                break;
//            case ServicesCodes.PROFILE_SERVER_SERVICE:
//                clazz = ProfileServerService.class;
//                break;
            default:
                throw new RuntimeException("Service unknown");
        }
        return clazz;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }

//    public boolean existIdentity(){
//        return context.getProfileServerManager().isIdentityCreated();
//    }

//    public void updateProfile(byte[] profileVersion, String username, byte[] profImgData) throws Exception {
//        profile.setName(username);
//        profile.setVersion(profileVersion);
//        profile.setImg(profImgData);
//
//        if (context.getProfileServerManager().isIdentityCreated()) {
//            // update the profile server
//            context.getProfileServerManager().updateProfileRequest(profile, profileVersion, username, profImgData, 0, 0, null);
//        }else {
//            context.getProfileServerManager().registerReqeust(profile,username,profImgData,0,0,null);
//        }
//    }


    public BlockchainManager getBlockchainManager() {
        return blockchainManager;
    }

    public WalletManager getWalletManager() {
        return walletManager;
    }


    public boolean sendProposal(Proposal proposal) throws CantSendProposalException, InsuficientBalanceException, CantSaveProposalException, InvalidProposalException, NotConnectedPeersException {

        LOG.info("SendProposal, title: "+proposal.getTitle());
        // lock to not to spend the same UTXO twice for error.
        synchronized (lock) {
            try {
                // check if the proposal is the same
                forumClient.getAndCheckValid(proposal);

                // save proposal to send
                try {
                    proposalsDao.saveIfChange(proposal);
                } catch (CantGetProposalException e) {
                    throw new CantSendProposalException("Proposal title changes or not exist", e);
                } catch (CantUpdateProposalException e) {
                    throw new CantSendProposalException("Cant update proposal", e);
                } catch (Exception e) {
                    throw new CantSendProposalException("Cant save proposal, db problem", e);
                }

                try {
                    ProposalTransactionRequest proposalTransactionRequest = new ProposalTransactionRequest(blockchainManager, walletManager, proposalsDao);
                    proposalTransactionRequest.forProposal(proposal);
                    proposalTransactionRequest.broadcast();

                    proposal = proposalTransactionRequest.getUpdatedProposal();
                    // lock contract output
                    boolean resp = proposalsDao.lockOutput(proposal.getForumId(), proposalTransactionRequest.getLockedOutputHashHex(), proposalTransactionRequest.getLockedOutputPosition());
                    LOG.info("proposal locked "+resp);
                    // mark proposal sent and put state in "voting"
                    resp = proposalsDao.markSentBroadcastedProposal(proposal.getForumId());
                    LOG.info("proposal mark sent "+resp);
                    // locked balance
                    lockedBalance += proposalTransactionRequest.getLockedBalance();
                    LOG.info("locked balance acumulated: "+lockedBalance);

                    LOG.info("sendProposal finished");

                    return true;

                } catch (InsuficientBalanceException e) {
                    LOG.error("Insuficient funds",e);
                    throw e;
                }catch (NotConnectedPeersException e) {
                    LOG.error("Not connected peers",e);
                    throw e;
                }
            }catch (CantSendProposalException e){
                throw e;
            } catch (NotValidParametersException e) {
                throw new InvalidProposalException("Proposal is not the same as in the forum, "+e.getMessage());
            }
        }
    }

    public boolean sendProposal(Proposal proposal,byte[] transactionHashDest) throws InsuficientBalanceException, InsufficientMoneyException, NotConnectedPeersException {

        ProposalTransactionRequest proposalTransactionRequest = new ProposalTransactionRequest(blockchainManager, walletManager, proposalsDao);
        proposalTransactionRequest.forProposal(proposal);
        proposalTransactionRequest.broadcast();


        byte[] tranHash = proposalTransactionRequest.getTransaction().getHash().getBytes();
        System.arraycopy(tranHash,0,transactionHashDest,0,tranHash.length);

        proposal = proposalTransactionRequest.getUpdatedProposal();
        // lock contract output
        boolean resp = proposalsDao.lockOutput(proposal.getForumId(), proposalTransactionRequest.getLockedOutputHashHex(), proposalTransactionRequest.getLockedOutputPosition());
        LOG.info("proposal locked "+resp);
        // mark proposal sent
        resp = proposalsDao.markSentBroadcastedProposal(proposal.getForumId());
        LOG.info("proposal mark sent "+resp);
        // locked balance
        lockedBalance += proposalTransactionRequest.getLockedBalance();
        LOG.info("locked balance acumulated: "+lockedBalance);

        LOG.info("sendProposal finished");

        return true;

    }

    public boolean sendVote(Vote vote) throws InsuficientBalanceException, NotConnectedPeersException, CantSendVoteException {

        try {
            // save vote
            long voteId = votesDaoImp.addVote(vote);
            // send vote
            VoteProposalRequest proposalVoteRequest = new VoteProposalRequest(blockchainManager, walletManager, votesDaoImp);
            proposalVoteRequest.forVote(vote);
            proposalVoteRequest.broadcast();

            vote = proposalVoteRequest.getUpdatedVote();
            // lock contract output
            boolean resp = votesDaoImp.lockOutput(voteId,vote.getLockedOutputHex(),vote.getLockedOutputIndex());
            LOG.info("vote locked "+resp);
            // locked balance
            lockedBalance += proposalVoteRequest.getLockedBalance();
            LOG.info("locked balance acumulated: "+lockedBalance);

            LOG.info("sendVote finished");

            return true;

        } catch (InsuficientBalanceException e){
            throw e;
        } catch (NotConnectedPeersException e){
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw new CantSendVoteException("Uknown error, please send log",e);
        }
    }

    public void sendTransaction(String address, long amount) throws InsufficientMoneyException, CantSendTransactionException {
        try {
            Context.propagate(WalletConstants.CONTEXT);
            Transaction tx = walletManager.createAndLockTransaction(address,amount);
            blockchainManager.broadcastTransaction(tx.getHash().getBytes()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Wallet.DustySendRequested e){
            throw new CantSendTransactionException("Dusty send transaction",e);
        }
    }

    public void cancelProposalOnBLockchain(Proposal proposal) {
        LOG.info("cancelProposalOnBLockchain");
    }

    public List<Proposal> getProposals() {
        return proposalsDao.listProposals();
    }

    public String getReceiveAddress() {
        String address = configuration.getReceiveAddress();
        if (address==null){
            address = walletManager.getWallet().freshReceiveAddress().toBase58();
            configuration.saveReceiveAddress(address);
        }
        LOG.info("Fresh new address: "+address);
        return address;
    }

    public CharSequence getAvailableBalanceStr() {
        long balance = walletManager.getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).value-lockedBalance;
        return coinToString(balance);
    }
    public long getAvailableBalance() {
        return walletManager.getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).value-lockedBalance;
    }



    public String getLockedBalance() {
        return coinToString(lockedBalance);
    }

    private String coinToString(long amount){
        String value = Coin.valueOf(amount).toPlainString();
        if (value.length()==4 || value.length()==3) return value;
        int pointIndex = value.indexOf('.');
        if (pointIndex!=-1){
            if (value.length()>pointIndex+2)
                value = value.substring(0,pointIndex+3);
            else
                value = value.substring(0,pointIndex+2);
        }
        return value;
    }

    public boolean isWalletEncrypted() {
        return walletManager.getWallet().isEncrypted();
    }

    public void backupWallet(File file, String password) throws IOException {
        walletManager.backupWallet(file,password);
    }

    public void restoreWalletFromProtobuf(File file) {
        try {
            walletManager.restoreWalletFromProtobuf(file);
        }catch (final IOException x)
        {
            final DialogBuilder dialog = DialogBuilder.warn(context, R.string.import_export_keys_dialog_failure_title);
            dialog.setMessage(context.getString(R.string.import_keys_dialog_failure, x.getMessage()));
            dialog.setPositiveButton(R.string.button_dismiss, null);
            dialog.setNegativeButton(R.string.button_retry, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    //showDialog(DIALOG_RESTORE_WALLET);
                }
            });
            dialog.show();

            LOG.info("problem restoring wallet", x);
        }
    }

    public void restorePrivateKeysFromBase58(File file) {
        try{
            walletManager.restorePrivateKeysFromBase58(file);
        }catch (final IOException x) {
            final DialogBuilder dialog = DialogBuilder.warn(context, R.string.import_export_keys_dialog_failure_title);
            dialog.setMessage(context.getString(R.string.import_keys_dialog_failure, x.getMessage()));
            dialog.setPositiveButton(R.string.button_dismiss, null);
            dialog.setNegativeButton(R.string.button_retry, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    //showDialog(DIALOG_RESTORE_WALLET);
                }
            });
            dialog.show();

            LOG.info("problem restoring private keys", x);
        }
    }

    public void restoreWalletFromEncrypted(final File file, final String password) throws CantRestoreEncryptedWallet {
        try {
            walletManager.restoreWalletFromEncrypted(file,password);
        }
        catch (final IOException x) {

            x.printStackTrace();

            LOG.info("problem restoring wallet", x);

            throw new CantRestoreEncryptedWallet(x);
        }
    }

    public int createForumProposal(Proposal proposal) throws CantCreateTopicException, CantSaveProposalException, CantSaveProposalExistException {
        LOG.info("createForumProposal");
        int forumId = forumClient.createTopic(proposal.getTitle(),proposal.getCategory(),proposal.toForumBody());
        if (forumId>0) {
            proposal.setForumId(forumId);
            proposal.setState(Proposal.ProposalState.FORUM);
            proposalsDao.saveProposal(proposal);
        }
        return forumId;
    }

    public boolean editForumProposal(Proposal proposal) throws CantUpdateProposalException, CantUpdatePostException {
        boolean resp = forumClient.updatePost(proposal.getTitle(),proposal.getForumId(),proposal.getCategory(),proposal.toForumBody());
        if (resp) {
            proposalsDao.updateProposal(proposal);
        }
        return resp;
    }

    public Proposal getProposal(int forumId) throws CantGetProposalException {
        LOG.info("### getProposal");
        return proposalsDao.findProposal(forumId);
    }


    public boolean isProposalMine(int forumId){
        return proposalsDao.isProposalMine(forumId);
    }



    /**
     * Forum methods
     */


    public boolean isForumRegistered() {
        return forumClient.isRegistered();
    }

    public ForumProfile getForumProfile(){
        return forumClient.getForumProfile();
    }

    public boolean registerForumUser(String username, String password, String email) throws InvalidUserParametersException {
        return forumClient.registerUser(username,password,email);
    }

    public boolean connectToForum(String username,String password) throws InvalidUserParametersException, ConnectionRefusedException {
        return forumClient.connect(username,password);
    }


    public void cleanEverything(){
        forumConfigurations.remove();
        configuration.remove();
        forumClient.clean();
        cleanProposalDb();
        profile = null;

    }

    public void setNewNode(String newNode) {
        this.configuration.saveNode(newNode);
    }

    public String getForumUrl() {
        if (forumUrl==null){
            forumUrl = forumConfigurations.getUrl();
        }
        return forumUrl;
    }

    public boolean requestCoins(String address) throws InvalidAddressException {
        try {
            Address.fromBase58(WalletConstants.NETWORK_PARAMETERS, address);
        }catch (Exception e){
            throw new InvalidAddressException("Invalid address");
        }
        return serverWrapper.requestCoins(address);
    }

    public void cleanProposalDb() {
        proposalsDao.clean();
    }

    public void setWrapperHost(String wrapperHost) {
        forumConfigurations.setWrapperUrl(wrapperHost);
        serverWrapper.setWrapperUrl(wrapperHost);
    }

    public void updateUser(String name, String password, String email, byte[] profImgData) {
        LOG.info("Saving image");
        forumConfigurations.setUserImg(profImgData);
    }

    public File getUserImageFile() {
        return forumConfigurations.getUserImgFile();
    }


    public TransactionFinder getAndCreateFinder(PeerGroup peerGroup){
        this.transactionFinder = new TransactionFinder(WalletConstants.CONTEXT,peerGroup);
        return transactionFinder;
    }

    /**
     * Request tx hashes from node
     * @param chainHeadHeight
     */
    public ServerWrapper.RequestProposalsResponse requestProposals(int chainHeadHeight) {
        try {
            // request tx hashes from node
            ServerWrapper.RequestProposalsResponse requestProposalsResponse = serverWrapper.getVotingProposals(0);

            return requestProposalsResponse;
        } catch (CantGetProposalsFromServer e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public List<Proposal> getVotingProposals() throws Exception {
        List<Proposal> proposals = new ArrayList<>();
        proposals = proposalsDao.listProposals();
//        for (Transaction transaction : transactionFinder.getWatchedTransactions()) {
//            // new proposal
//            Proposal proposal = null;
//
//            List<TransactionOutput> outputs = transaction.getOutputs();
//            // empiezo en 2 porque el 0 es el de lockeo y el 1 es el de changeAddress
//            for (int i = 2; i < outputs.size(); i++) {
//                TransactionOutput transactionOutput = outputs.get(i);
//                try {
//                    proposal = ProposalTransactionBuilder.decodeContract(transactionOutput);
//                    break;
//                }catch (Exception e){
//                    e.printStackTrace();
//                    continue;
//                }
//            }
//
//            proposal.setMine(false);
//
//            // forum
//            Proposal forumProposal = forumClient.getProposalFromWrapper(proposal.getForumId());
//
//            if (forumProposal!=null) {
//                // set parameters
//                forumProposal.setForumId(proposal.getForumId());
//                forumProposal.setStartBlock(proposal.getStartBlock());
//                forumProposal.setEndBlock(proposal.getEndBlock());
//                forumProposal.setBlockReward(proposal.getBlockReward());
//                forumProposal.setBlockchainHash(proposal.getBlockchainHash());
//
//                //check hash
////            forumProposal.checkHash();
//
//                proposals.add(forumProposal);
//            }else {
//                LOG.error("Forum proposal bad decode");
//            }
//        }

        return proposals;
    }

    /**
     * New tx proposal arrive, this method check the proposal and save it in the database
     * @param tx
     */
    public void txProposalArrive(Transaction tx) {
        Proposal proposal = null;
        List<TransactionOutput> outputs = tx.getOutputs();
        try {
            // empiezo en 2 porque el 0 es el de lockeo y el 1 es el de changeAddress
            for (int i = 2; i < outputs.size(); i++) {
                TransactionOutput transactionOutput = outputs.get(i);
                try {
                    proposal = ProposalTransactionBuilder.decodeContract(transactionOutput);
                    LOG.info("Decoded proposal from blockchain: " + proposal.toStringBlockchain());
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }

            proposal.setMine(false);

            // forum
            Proposal forumProposal = forumClient.getProposalFromWrapper(proposal.getForumId());

            if (forumProposal != null) {
                LOG.info("forumProposal arrive: "+forumProposal);
                // set parameters
                forumProposal.setForumId(proposal.getForumId());
                forumProposal.setStartBlock(proposal.getStartBlock());
                forumProposal.setEndBlock(proposal.getEndBlock());
                forumProposal.setBlockReward(proposal.getBlockReward());
                forumProposal.setBlockchainHash(proposal.getBlockchainHash());

                proposalsDao.saveProposal(forumProposal);
            } else {
                LOG.error("txProposalArrive error", tx, "proposal decoded form blokcchain: " + proposal, "Proposal obtained from the forum: " + forumProposal);
            }
        }catch (CantSaveProposalExistException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public boolean checkIfVoteExist(Vote vote) {
        return votesDaoImp.exist(vote);
    }

    public boolean isProposalTransactionMine(Transaction transaction) {
       return ProposalTransactionBuilder.isProposal(transaction);
    }

    public List<VoteWrapper> listMyVotes() {
        List<VoteWrapper> wrappers = new ArrayList<>();
        List<Vote> myVotes = votesDaoImp.listVotes();
        Map<String,Vote> voteByProposalHash = new HashMap<>();
        List<String> transactionHashes = new ArrayList<>();
        for (Vote myVote : myVotes) {
            voteByProposalHash.put(myVote.getGenesisHashHex(),null);
            transactionHashes.add(myVote.getGenesisHashHex());
        }
        List<Proposal> proposals = proposalsDao.listProposals(transactionHashes);
        for (Proposal proposal : proposals) {
            // todo: esto es totalmente mejorable si le pongo un hex en vez del byte array que tiene.
            wrappers.add(new VoteWrapper(
                   voteByProposalHash.get(CryptoBytes.toHexString(proposal.getBlockchainHash())),
                    proposal

            ));
        }

        return wrappers;
    }
}
