package iop.org.iop_contributors_app.wallet;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.Profile;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ServerWrapper;
import iop.org.iop_contributors_app.core.iop_sdk.forum.CantCreateTopicException;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumClient;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumClientDiscourseImp;
import iop.org.iop_contributors_app.core.iop_sdk.forum.InvalidUserParametersException;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumConfigurations;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;
import iop.org.iop_contributors_app.core.iop_sdk.governance.ProposalTransactionRequest;
import iop.org.iop_contributors_app.intents.constants.IntentsConstants;
import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop.org.iop_contributors_app.services.ServicesCodes;
import iop.org.iop_contributors_app.configurations.WalletPreferencesConfiguration;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.wallet.db.CantGetProposalException;
import iop.org.iop_contributors_app.wallet.db.CantSaveProposalException;
import iop.org.iop_contributors_app.wallet.db.CantSaveProposalExistException;
import iop.org.iop_contributors_app.wallet.db.CantUpdateProposalException;
import iop.org.iop_contributors_app.wallet.db.ProposalsDao;
import iop.org.iop_contributors_app.wallet.exceptions.CantSendProposalException;
import iop.org.iop_contributors_app.wallet.exceptions.InsuficientBalanceException;

/**
 * Created by mati on 12/11/16.
 */

public class WalletModule implements ContextWrapper{

    private static final Logger LOG = LoggerFactory.getLogger(WalletModule.class);

    private ApplicationController context;

    private WalletManager walletManager;
    private BlockchainManager blockchainManager;

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


    public WalletModule(ApplicationController context, WalletPreferencesConfiguration configuration, ForumConfigurations forumConfigurations) {
        this.context = context;
        this.configuration = configuration;
        this.forumConfigurations = forumConfigurations;
        proposalsDao = new ProposalsDao(context);
        // locked outputs
        lockedBalance = proposalsDao.getTotalLockedBalance();
        serverWrapper = new ServerWrapper();
    }

    public void start(){
        // init
        walletManager = new WalletManager(this,configuration);
        blockchainManager = new BlockchainManager(this,walletManager,configuration);
        forumClient = new ForumClientDiscourseImp(forumConfigurations,serverWrapper);

    }



    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return context.openFileOutput(name,mode);
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
    public PackageInfo packageInfo() {
        return context.packageInfo();
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
    public void sendLocalBroadcast(Intent broadcast) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    @Override
    public void showDialog(String id) {
        final StringBuilder message = new StringBuilder();
        message.append(context.getString(R.string.restore_wallet_dialog_success));
        message.append("\n\n");
        message.append(context.getString(R.string.restore_wallet_dialog_success_replay));

        Intent intent = new Intent(BaseActivity.ACTION_NOTIFICATION);
        intent.putExtra(IntentsConstants.INTENT_BROADCAST_TYPE,IntentsConstants.INTENT_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE,IntentsConstants.RESTORE_SUCCED_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_EXTRA_MESSAGE,message.toString());
        context.sendLocalBroadcast(intent);
    }

    @Override
    public String[] fileList() {
        return context.fileList();
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


    public boolean sendProposal(Proposal proposal) throws CantSendProposalException, InsuficientBalanceException, CantSaveProposalException, InvalidProposalException {

        LOG.info("SendProposal, title: "+proposal.getTitle());
        // lock to not to spend the same UTXO twice for error.
        synchronized (lock) {
            try {
                // check if the proposal is the same
                if(forumClient.getAndCheckValid(proposal)) {

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
                        // mark proposal sent
                        resp = proposalsDao.markSentProposal(proposal.getForumId());
                        LOG.info("proposal mark sent "+resp);
                        // locked balance
                        lockedBalance += proposalTransactionRequest.getLockedBalance();
                        LOG.info("locked balance acumulated: "+lockedBalance);

                        LOG.info("sendProposal finished");

                        return true;

                    } catch (InsufficientMoneyException e) {
                        e.printStackTrace();
                        LOG.info("fondos disponibles: " + walletManager.getWallet().getBalance());
                    }
                }else {
                    throw new InvalidProposalException("Proposal is not the same as in the forum");
                }

            } catch (InsuficientBalanceException e) {
                throw e;
            }catch (CantSendProposalException e){
                throw e;
            }
        }
        return false;
    }

    public boolean sendProposal(Proposal proposal,byte[] transactionHashDest) throws InsuficientBalanceException, InsufficientMoneyException {

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
        resp = proposalsDao.markSentProposal(proposal.getForumId());
        LOG.info("proposal mark sent "+resp);
        // locked balance
        lockedBalance += proposalTransactionRequest.getLockedBalance();
        LOG.info("locked balance acumulated: "+lockedBalance);

        LOG.info("sendProposal finished");

        return true;

    }

    public void sendTransaction(String address, long amount) throws InsufficientMoneyException {
        try {
            Transaction tx = walletManager.createAndLockTransaction(address,amount);
            blockchainManager.broadcastTransaction(tx.getHash().getBytes()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void cancelProposalOnBLockchain(Proposal proposal) {
        LOG.info("cancelProposalOnBLockchain");
    }

    public List<Proposal> getProposals() {
        return proposalsDao.listProposals();
    }

    public String getNewAddress() {
        String address = walletManager.getWallet().freshReceiveAddress().toBase58();;
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
        int pointIndex = value.indexOf('.');
        if (pointIndex!=-1){
            if (value.length()>=pointIndex+2)
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
            proposalsDao.saveProposal(proposal);
        }
        return forumId;
    }

    public boolean editForumProposal(Proposal proposal) throws CantUpdateProposalException {
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

    public boolean connectToForum(String username,String password) throws InvalidUserParametersException {
        return forumClient.connect(username,password);
    }


    public void cleanEverything(){
        forumConfigurations.remove();
        configuration.remove();

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
}
