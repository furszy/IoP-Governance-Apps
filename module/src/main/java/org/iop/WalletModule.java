package org.iop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.utils.BtcFormat;
import org.bitcoinj.wallet.Wallet;
import org.iop.db.CantGetProposalException;
import org.iop.db.CantSaveProposalException;
import org.iop.db.CantSaveProposalExistException;
import org.iop.db.CantUpdateProposalException;
import org.iop.db.ProposalsDao;
import org.iop.exceptions.CantRestoreEncryptedWallet;
import org.iop.exceptions.CantSendProposalException;
import org.iop.exceptions.CantSendTransactionException;
import org.iop.exceptions.CantSendVoteException;
import org.iop.exceptions.InvalidAddressException;
import org.iop.exceptions.InvalidProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import iop.org.iop_sdk_android.core.profile_server.Profile;
import iop.org.iop_sdk_android.core.wrappers.IntentWrapperAndroid;
import iop_sdk.blockchain.NotConnectedPeersException;
import iop_sdk.blockchain.explorer.TransactionFinder;
import iop_sdk.blockchain.explorer.TransactionStorage;
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
import iop_sdk.governance.propose.CantCompleteProposalException;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.propose.ProposalTransactionBuilder;
import iop_sdk.governance.propose.ProposalTransactionRequest;
import iop_sdk.governance.vote.Vote;
import iop_sdk.governance.vote.VoteWrapper;
import iop_sdk.governance.vote.VotesDao;
import iop_sdk.wallet.BlockchainManager;
import iop_sdk.wallet.VoteProposalRequest;
import iop_sdk.wallet.WalletManager;
import iop_sdk.wallet.WalletPreferenceConfigurations;
import iop_sdk.wallet.exceptions.InsuficientBalanceException;

;import static iop_sdk.governance.propose.Proposal.ProposalState.CANCELED_BY_OWNER;
import static iop_sdk.governance.propose.Proposal.ProposalState.EXECUTED;
import static iop_sdk.governance.propose.Proposal.ProposalState.EXECUTION_CANCELLED;
import static iop_sdk.governance.propose.Proposal.ProposalState.FORUM;
import static iop_sdk.governance.propose.Proposal.ProposalState.SUBMITTED;
import static org.iop.intents.constants.IntentsConstants.ACTION_NOTIFICATION;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_PROPOSAL_FROZEN_FUNDS_UNLOCKED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_VOTE_FROZEN_FUNDS_UNLOCKED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_EXTRA_DATA_VOTE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_DATA;
import static org.iop.intents.constants.IntentsConstants.INTENT_EXTRA_PROPOSAL;
import static org.iop.intents.constants.IntentsConstants.INTENT_NOTIFICATION;

/**
 * Created by mati on 12/11/16.
 *
 * todo: recuperar los 1000 IoPs bloqueados..
 */

public class WalletModule {

    private static final Logger LOG = LoggerFactory.getLogger(WalletModule.class);

    private AppController context;


    private WalletManager walletManager;
    private BlockchainManager blockchainManager;
    /** Class in charge of search and update special transactions */
    private TransactionFinder transactionFinder;

    /** Profile server profile */
    private Profile profile;

    private WalletPreferenceConfigurations configuration;

    private ForumClient forumClient;
    private ServerWrapper serverWrapper;
    private ForumConfigurations forumConfigurations;
    private String forumUrl;

    private long lockedBalance;

    /** Lock */
    private Object lock = new Object();

    //todo: leer lo que puse adnetro..
    private ProposalsDao proposalsDao;

    private VotesDao votesDaoImp;

    // esto no va a quedar acá..
    private TransactionStorage transactionStorage;


    public WalletModule(android.content.Context context, WalletPreferenceConfigurations configuration, ForumConfigurations forumConfigurations, VotesDao votesDao) {
        this.context = (AppController) context;
        this.configuration = configuration;
        this.forumConfigurations = forumConfigurations;
        proposalsDao = new ProposalsDao(context);
        votesDaoImp = votesDao;
        // locked outputs
        lockedBalance = (this.context.isVotingApp())? votesDao.getTotalLockedBalance():proposalsDao.getTotalLockedBalance();
        forumConfigurations.getWrapperUrl();
        serverWrapper = new ServerWrapper(forumConfigurations.getWrapperUrl());
    }

    public WalletModule(android.content.Context context, WalletPreferenceConfigurations configuration, ForumConfigurations forumConfigurations) {
        this(context,configuration,forumConfigurations,null);
    }

    public void start(){
        // init
        walletManager = new WalletManager(context,configuration,new WalletManagerListenerImp(this));
        blockchainManager = new BlockchainManager(context,walletManager,configuration);
        forumClient = new ForumClientDiscourseImp(forumConfigurations,serverWrapper);

    }



    public String[] fileList() {
        return context.fileList();
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


    public Proposal sendProposal(Proposal proposal) throws CantSendProposalException, InsuficientBalanceException, CantSaveProposalException, InvalidProposalException, NotConnectedPeersException, CantCompleteProposalException {

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
                    // lock contract output and update genesis hash
                    boolean resp = proposalsDao.lockOutput(proposal.getForumId(), proposalTransactionRequest.getLockedOutputHashHex(), proposalTransactionRequest.getLockedOutputPosition());
                    LOG.info("proposal locked "+resp);
                    // mark proposal sent and put state in "voting"
                    proposal.setSent(true);
                    resp = proposalsDao.markSentBroadcastedProposal(proposal.getForumId());
                    LOG.info("proposal mark sent "+resp);
                    // locked balance
                    addLockedBalance(proposalTransactionRequest.getLockedBalance());
                    LOG.info("locked balance acumulated: "+lockedBalance);

                    LOG.info("sendProposal finished");

                    return proposal;

                } catch (InsuficientBalanceException e) {
                    LOG.error("Insuficient funds",e);
                    throw e;
                }catch (NotConnectedPeersException e) {
                    LOG.error("Not connected peers",e);
                    throw e;
                } catch (CantCompleteProposalException e) {
                    LOG.error("CantCompleteProposalException",e);
                    throw e;
                } catch (CantSendTransactionException e) {
                    LOG.error("CantSendTransactionException",e);
                    throw new CantSendProposalException(e.getMessage());
                }
            }catch (CantSendProposalException e){
                throw e;
            } catch (NotValidParametersException e) {
                throw new InvalidProposalException("Proposal is not the same as in the forum, "+e.getMessage());
            }
        }
    }

    private void rollbackTx(){

    }

    public boolean sendProposal(Proposal proposal,byte[] transactionHashDest) throws InsuficientBalanceException, InsufficientMoneyException, NotConnectedPeersException, CantCompleteProposalException, CantSendTransactionException {

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
        addLockedBalance(proposalTransactionRequest.getLockedBalance());
        LOG.info("locked balance acumulated: "+lockedBalance);

        LOG.info("sendProposal finished");

        return true;

    }

    private boolean cancelVote(Vote vote){
        LOG.info("cancelVote request: "+vote.toString());
        try {
            Transaction tx = walletManager.changeAddressOfTx(vote.getLockedOutputHex(),0);
            ListenableFuture<Transaction> future = blockchainManager.broadcastTransaction(tx.getHash().getBytes());
            future.get(1, TimeUnit.MINUTES);

            votesDaoImp.addUpdateIfExistVote(vote);

            LOG.info("cancelVote succed: "+vote.toString());

            return true;

        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Proposal cancelProposalContract(Proposal proposal) throws CantCancelProsalException{
        LOG.info("cancelProposalContract : "+proposal.toString());
        try {
            if (proposal.getState()==CANCELED_BY_OWNER && proposal.isSent()) throw new CantCancelProsalException("Proposal is sent but not confirmed in the blockchain,\nplease wait until is accepted");
            Transaction tx = walletManager.changeAddressOfTx(proposal.getGenesisTxHash(),0);
            ListenableFuture<Transaction> future = blockchainManager.broadcastTransaction(tx.getHash().getBytes());
            future.get(1, TimeUnit.MINUTES);

            proposalsDao.updateProposalByForumId(proposal);

            proposal.setState(CANCELED_BY_OWNER);

            LOG.info("cancelProposalContract succed: "+proposal.toString());

            return proposal;

        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            throw new CantCancelProsalException("Insufficient money");
        } catch (Exception e){
            e.printStackTrace();
            throw new CantCancelProsalException(e.getMessage());
        }
    }

    public boolean sendVote(Vote vote) throws InsuficientBalanceException, NotConnectedPeersException, CantSendVoteException, iop_sdk.wallet.CantSendVoteException {

        try {

            Vote temp = null;
            if ((temp = votesDaoImp.getVote(vote.getGenesisHashHex()))!=null){


                if (vote.getVotingPower()==0 && vote.getVote() == Vote.VoteType.NEUTRAL){
                    return cancelVote(vote);
                }else
                    if (vote.getVotingPower()>0 && vote.getVote()== Vote.VoteType.NEUTRAL)throw new CantSendVoteException("Voting power is greater than 0 in a NEUTRAL vote");

            }



            // save vote
            votesDaoImp.addUpdateIfExistVote(vote);
            // send vote
            VoteProposalRequest proposalVoteRequest = new VoteProposalRequest(blockchainManager, walletManager, votesDaoImp);
            proposalVoteRequest.forVote(vote);
            proposalVoteRequest.broadcast();

            vote = proposalVoteRequest.getUpdatedVote();
            // lock contract output
            boolean resp = votesDaoImp.lockOutput(vote.getGenesisHashHex(),vote.getLockedOutputHex(),vote.getLockedOutputIndex());
            LOG.info("vote locked "+resp);
            // resto el voto anteriorque reutilicé de lo loqueado
            if (temp!=null) {
                minusLockedValue(temp.getVotingPower());
            }
            // locked balance
            addLockedBalance(proposalVoteRequest.getLockedBalance());
            LOG.info("locked balance acumulated: "+lockedBalance);

            LOG.info("sendVote finished");

            return true;

        } catch (InsuficientBalanceException e){
            rollbackVote(vote);
            throw e;
        } catch (NotConnectedPeersException e){
            rollbackVote(vote);
            throw e;
        } catch (iop_sdk.wallet.CantSendVoteException e){
            rollbackVote(vote);
            throw e;
        } catch (Exception e){
            rollbackVote(vote);
            e.printStackTrace();
            throw new CantSendVoteException("Uknown error, please send log",e);
        }
    }

    private void rollbackVote(Vote vote){
        // deberia chequear si fué comiteado en la wallet antes de hacer esto..
        votesDaoImp.removeIfExist(vote);
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


    public List<Proposal> getProposals() {
        return proposalsDao.listProposals();
    }

    public List<Proposal> getMyProposals() {
        return proposalsDao.listMyProposals();
    }

    public List<Proposal> getActiveProposals() {
        return proposalsDao.listProposalsActive(EXECUTED.getId()|EXECUTION_CANCELLED.getId());
    }

    public List<Proposal> getActiveLoadedProposals() {
        return proposalsDao.listProposalsActiveWithTitle(EXECUTED.getId()|EXECUTION_CANCELLED.getId());
    }

    public List<Proposal> getActiveProposalsInBlockchain() {
        return proposalsDao.listProposalsActiveInBlockchain(EXECUTED.getId()|EXECUTION_CANCELLED.getId()|FORUM.getId());
    }

    public String getReceiveAddress() {
        String address = null;//configuration.getReceiveAddress();
        if (address==null){
            address = walletManager.getWallet().freshReceiveAddress().toBase58();
//            configuration.saveReceiveAddress(address);
        }
        LOG.info("Fresh new address: "+address);
        return address;
    }

    public CharSequence getAvailableBalanceStr() {
        long balance = walletManager.getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).value-lockedBalance;
        return BtcFormat.getCoinInstance().format(balance);
    }

    public CharSequence getUnnavailableBalanceStr() {
        long balance = walletManager.getWallet().getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE).value-walletManager.getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).value;
        return BtcFormat.getCoinInstance().format(balance);
    }

    public long getAvailableBalance() {
        return walletManager.getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).value-lockedBalance;
    }



    public String getLockedBalance() {
        return BtcFormat.getCoinInstance().format(lockedBalance);
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
        }catch (final IOException x) {

            context.showDialog(WalletConstants.SHOW_IMPORT_EXPORT_KEYS_DIALOG_FAILURE,x.getMessage());

            LOG.info("problem restoring wallet", x);
        }
    }

    public void restorePrivateKeysFromBase58(File file) {
        try{
            walletManager.restorePrivateKeysFromBase58(file);
        }catch (final IOException x) {

            context.showDialog(WalletConstants.SHOW_IMPORT_EXPORT_KEYS_DIALOG_FAILURE,x.getMessage());

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
        proposal.setCategory("Voting system");
        int forumId = forumClient.createTopic(proposal.getTitle(),proposal.getCategory(),proposal.toForumBody());
        if (forumId>0) {
            proposal.setForumId(forumId);
            proposal.setState(Proposal.ProposalState.FORUM);
            proposal.setMine(true);
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
        boolean ret = forumClient.connect(username,password);
        if (context.isVotingApp()) {
            if (ret) {
                // notify that the user is connected
                LOG.info("checking uncheked proposals");
                checkUncheckedProposals();
            }
        }
        return ret;
    }

    /**
     * Chequeo las propuestas que llegaron de la blockchain pero como no estaba logueado el user no fueron validadas con el foro
     */
    private void checkUncheckedProposals(){
        for (Proposal proposal : proposalsDao.listUncheckedProposals()) {
            try {
                proposalArrive(proposal);
            } catch (CantSaveProposalException e) {
                e.printStackTrace();
            } catch (CantSaveProposalExistException e) {
                e.printStackTrace();
            } catch (CantGetProposalException e) {
                e.printStackTrace();
            }
        }
    }

    public void cleanEverything(){
        forumConfigurations.remove();
        configuration.remove();
        forumClient.clean();
        cleanProposalDb();
        cleanVotesDb();
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

    public void cleanVotesDb(){
        if (votesDaoImp!=null){
            votesDaoImp.clean();
        }
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
        this.transactionFinder = new TransactionFinder(WalletConstants.CONTEXT,peerGroup,transactionStorage);
        return transactionFinder;
    }

    /**
     * Request tx hashes from node
     * @param chainHeadHeight
     */
    public ServerWrapper.RequestProposalsResponse requestProposals(int chainHeadHeight)  {
        try {
            // request tx hashes from node
            ServerWrapper.RequestProposalsResponse requestProposalsResponse = serverWrapper.getVotingProposalsNew(0);

            return requestProposalsResponse;
        } catch (CantGetProposalsFromServer e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * request full tx proposals
     * @param chainHeadHeight
     * @return
     */
    public ServerWrapper.RequestProposalsResponse requestProposalsFullTx(int chainHeadHeight) {
        try {
            // request tx hashes from node
            ServerWrapper.RequestProposalsResponse requestProposalsResponse = serverWrapper.getVotingProposalsNew(0);

            return requestProposalsResponse;
        } catch (CantGetProposalsFromServer e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Request proposal by his genesis hash.
     * @param list
     * @return
     */
    public ServerWrapper.RequestProposalsResponse requestProposalsFullTx(List<Proposal> list) {
        try {
            LOG.info("requestProposalsFullTx hashes -> "+ Arrays.toString(list.toArray()));
            List<String> hashes = new ArrayList<>();
            for (Proposal proposal : list) {
                hashes.add(proposal.getGenesisTxHash());
            }
            // request tx hashes from node
            ServerWrapper.RequestProposalsResponse requestProposalsResponse = serverWrapper.getVotingProposalsNew(hashes);

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
        return proposals;
    }

    /**
     * New tx proposal arrive, this method check the proposal and save it in the database
     * @param tx
     */
    public Proposal txProposalArrive(Transaction tx) {
        Proposal proposal = null;
        List<TransactionOutput> outputs = tx.getOutputs();
        try {
            // empiezo en 2 porque el 0 es el de lockeo y el 1 es el de changeAddress
            TransactionOutput transactionOutput = outputs.get(2);
            try {
                proposal = ProposalTransactionBuilder.decodeContract(transactionOutput);
                LOG.info("Decoded proposal from blockchain: " + proposal.toStringBlockchain());
            } catch (Exception e) {
                e.printStackTrace();
            }

            proposal = proposalArrive(proposal);

        }catch (CantSaveProposalExistException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return proposal;
    }

    public boolean proposalAcceptedInBlockchain(Proposal proposal) throws CantUpdateProposalException, JsonProcessingException, CantGetProposalException {
        if (proposal==null) throw new IllegalArgumentException("Proposal null");
        boolean ret = false;
        proposal = proposalsDao.findProposal(proposal.getForumId());
        if (proposal!=null && proposal.getState()==FORUM) {
            proposal.setState(SUBMITTED);
            proposalsDao.updateProposalStateByForumId(proposal);
            ret = true;
        }
        return ret;
    }

    public Proposal proposalArrive(Proposal proposal) throws CantSaveProposalException, CantSaveProposalExistException, CantGetProposalException {

        // check if the proposal exist and is not in a final state
        Proposal proposalDb = proposalsDao.findProposal(proposal.getForumId());
        if (proposalDb!=null){
            if (!proposalDb.isActive())
                return null;
            if (proposalDb.isMine()){
                proposal.setMine(true);
                proposal.setSent(true);
            }
        }

        // si el usuario del foro no existe tengo que grabar esto para hacerlo más adelante..
        if (forumClient.getForumProfile()!=null) {
            // forum
            Proposal forumProposal = forumClient.getProposalFromWrapper(proposal.getForumId());
            //todo: acá deberia chequear con el hash del foro..
            if (forumProposal != null) {
                LOG.info("forumProposal arrive: " + forumProposal);
                // set parameters
                forumProposal.setForumId(proposal.getForumId());
                forumProposal.setStartBlock(proposal.getStartBlock());
                forumProposal.setEndBlock(proposal.getEndBlock());
                forumProposal.setBlockReward(proposal.getBlockReward());
                forumProposal.setGenesisTxHash(proposal.getGenesisTxHash());
                forumProposal.setState(proposal.getState());
                forumProposal.setVoteNo(proposal.getVoteNo());
                forumProposal.setVoteYes(proposal.getVoteYes());
                forumProposal.setGenesisTxHash(proposal.getGenesisTxHash());
                forumProposal.setMine(proposal.isMine());
                forumProposal.setSent(proposal.isSent());
                proposalsDao.saveProposal(forumProposal);

                // Unlock freeze outputs if the proposal is finished
                if(context.isVotingApp()){
                    if (forumProposal.getState() == EXECUTED || forumProposal.getState() == EXECUTION_CANCELLED){
                        // unlock funds
                        if (votesDaoImp.unlockOutput(proposal.getGenesisTxHash())){

                            Vote vote = votesDaoImp.getVote(proposal.getGenesisTxHash());
                            // update locked balance
                            minusLockedValue(vote.getVotingPower());
                            // notify unlocked funds
                            IntentWrapper intentWrapper = new IntentWrapperAndroid(ACTION_NOTIFICATION);
                            intentWrapper.put(INTENT_BROADCAST_TYPE,INTENT_DATA+INTENT_NOTIFICATION);
                            intentWrapper.put(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_VOTE_FROZEN_FUNDS_UNLOCKED);
                            intentWrapper.put(INTENT_BROADCAST_EXTRA_DATA_VOTE,vote);
                            intentWrapper.put(INTENT_EXTRA_PROPOSAL,proposal);
                            context.sendLocalBroadcast(intentWrapper);
                        }

                    }
                }else {
                    if (forumProposal.getState() == EXECUTED || forumProposal.getState() == EXECUTION_CANCELLED){
                        // No tengo que desloquear los fondos aquí ya que se lokean por el estado de la propuesta
                        // así que solamente tengo que notificar al usuario que sus fondos fueron desbloqueados
                        minusLockedValue(ProposalTransactionBuilder.FREEZE_VALUE.getValue());

                        IntentWrapper intentWrapper = new IntentWrapperAndroid(ACTION_NOTIFICATION);
                        intentWrapper.put(INTENT_BROADCAST_TYPE,INTENT_DATA+INTENT_NOTIFICATION);
                        intentWrapper.put(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_PROPOSAL_FROZEN_FUNDS_UNLOCKED);
                        intentWrapper.put(INTENT_EXTRA_PROPOSAL,proposal);
                        context.sendLocalBroadcast(intentWrapper);


                    }


                }


            } else {
                LOG.error("txProposalArrive error", proposal, "proposal decoded form blokcchain: " + proposal, "Proposal obtained from the forum: " + forumProposal);
            }
            return forumProposal;
        }else {
            LOG.info("ProposalArrive, profile not exist, saving to put it later");
            proposalsDao.saveProposal(proposal);
            return proposal;
        }
    }


    public boolean checkIfVoteExist(Vote vote) {
        return votesDaoImp.exist(vote);
    }

    public boolean isProposalTransaction(Transaction transaction) {
       return ProposalTransactionBuilder.isProposal(transaction);
    }

    public Proposal decodeProposalTransaction(Transaction transaction) {
        return ProposalTransactionBuilder.getProposal(transaction);
    }

    public List<VoteWrapper> listMyVotes() {
        List<VoteWrapper> wrappers = new ArrayList<>();
        List<Vote> myVotes = votesDaoImp.listVotes();
        Map<String,Vote> voteByProposalHash = new HashMap<>();
        List<String> transactionHashes = new ArrayList<>();
        for (Vote myVote : myVotes) {
            voteByProposalHash.put(myVote.getGenesisHashHex(),myVote);
            transactionHashes.add(myVote.getGenesisHashHex());
        }
        List<Proposal> proposals = proposalsDao.listProposals(transactionHashes);
        for (Proposal proposal : proposals) {
            // todo: esto es totalmente mejorable si le pongo un hex en vez del byte array que tiene.
            String proposalGenesisHash = proposal.getGenesisTxHash();
            Vote vote = voteByProposalHash.get(proposalGenesisHash);
            if (vote!=null) {
                wrappers.add(new VoteWrapper(
                        vote,
                        proposal

                ));
            }
        }

        return wrappers;
    }

    public ContextWrapper getAppController() {
        return context;
    }

    public void setTransactionStorage(TransactionStorage transactionStorage) {
        this.transactionStorage = transactionStorage;
    }


    public Vote getVote(String genesisTxHash) {
        return votesDaoImp.getVote(genesisTxHash);
    }

    private void addLockedBalance(Long value){
        lockedBalance+=value;
    }

    private void minusLockedValue(Long value){
        lockedBalance-=value;
    }


    public boolean proposalBeneficiaryAddressExist(String addressBen) {
        return proposalsDao.beneficiaryAddressExist(addressBen);
    }

    public void saveUnknownProposals(List<Proposal> remainingProposals) {
        LOG.info("saveUnknownProposals, List: "+Arrays.toString(remainingProposals.toArray()));
        for (Proposal remainingProposal : remainingProposals) {
            proposalsDao.updateProposalStateByForumId(remainingProposal.getForumId(), Proposal.ProposalState.UNKNOWN);
        }
    }
}
