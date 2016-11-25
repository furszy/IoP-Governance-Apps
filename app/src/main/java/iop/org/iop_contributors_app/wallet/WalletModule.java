package iop.org.iop_contributors_app.wallet;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.widget.Toast;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.SendRequest;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.Profile;
import iop.org.iop_contributors_app.core.Proposal;
import iop.org.iop_contributors_app.core.iop_sdk.blockchain.contribution_contract.ProposalTransactionBuilder;
import iop.org.iop_contributors_app.core.iop_sdk.forum.FlarumClient;
import iop.org.iop_contributors_app.core.iop_sdk.forum.FlarumClientInvalidDataException;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumConfigurations;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;
import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop.org.iop_contributors_app.services.ProfileServerService;
import iop.org.iop_contributors_app.services.ServicesCodes;
import iop.org.iop_contributors_app.configurations.WalletPreferencesConfiguration;
import iop.org.iop_contributors_app.wallet.db.CantSaveProposalException;
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
    /** Forum profile */
    private ForumProfile forumProfile;

    private WalletPreferencesConfiguration configuration;

    private FlarumClient flarumClient;
    private ForumConfigurations forumConfigurations;

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
        // pre init
        forumProfile = forumConfigurations.getForumUser();
        // locked outputs
        lockedBalance = proposalsDao.getTotalLockedBalance();
    }

    public void start(){
        // init
        walletManager = new WalletManager(this,configuration);
        blockchainManager = new BlockchainManager(this,walletManager,configuration);
        flarumClient = new FlarumClient(forumConfigurations);

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

    private Class<? extends Service> switchServices(int service) {
        Class<? extends Service> clazz = null;
        switch (service){
            case ServicesCodes.BLOCKCHAIN_SERVICE:
                clazz = BlockchainServiceImpl.class;
                break;
            case ServicesCodes.PROFILE_SERVER_SERVICE:
                clazz = ProfileServerService.class;
                break;
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

    public boolean existIdentity(){
        return context.getProfileServerManager().isIdentityCreated();
    }

    public void updateProfile(byte[] profileVersion, String username, byte[] profImgData) throws Exception {
        profile.setName(username);
        profile.setVersion(profileVersion);
        profile.setImg(profImgData);

        if (context.getProfileServerManager().isIdentityCreated()) {

            // update the profile server
            context.getProfileServerManager().updateProfileRequest(profile, profileVersion, username, profImgData, 0, 0, null);
        }else {
            context.getProfileServerManager().registerReqeust(profile,username,profImgData,0,0,null);
        }
    }


    public BlockchainManager getBlockchainManager() {
        return blockchainManager;
    }

    public WalletManager getWalletManager() {
        return walletManager;
    }

    private long id=0;

    public void sendProposal(Proposal proposal) throws CantSendProposalException, InsuficientBalanceException, CantSaveProposalException {

        // lock to not to spend the same UTXO twice for error.

        synchronized (lock) {

            try {

//                if (proposalsDao.exist(proposal.getIoPIP())) throw new CantSendProposalException("Proposal already exist");

                // lazy lazy test
                id++;
                proposal.setId(id);

                // save proposal to send
                try {
                    if (!proposalsDao.saveProposal(proposal))
                        throw new CantSaveProposalException("database error, please check the log");
                } catch (CantSaveProposalException e) {
                    throw new CantSendProposalException("database fail", e);
                } catch (Exception e) {
                    throw new CantSaveProposalException("database error, please check the log");
                }

                org.bitcoinj.core.Context.propagate(WalletConstants.CONTEXT);

                ProposalTransactionBuilder proposalTransactionBuilder = new ProposalTransactionBuilder(
                        WalletConstants.NETWORK_PARAMETERS,
                        blockchainManager.getChainHeadHeight()
                );

                Wallet wallet = walletManager.getWallet();

                Coin totalOuputsValue = Coin.ZERO;
                for (Long aLong : proposal.getBeneficiaries().values()) {
                    totalOuputsValue = totalOuputsValue.add(Coin.valueOf(aLong));
                }

                // locked coins 1000 IoPs
                totalOuputsValue = totalOuputsValue.add(Coin.valueOf(1000, 0));

                List<TransactionOutput> unspentTransactions = new ArrayList<>();
                Coin totalInputsValue = Coin.ZERO;
                boolean inputsSatisfiedContractValue = false;
                for (TransactionOutput transactionOutput : wallet.getUnspents()) {
                    //
                    TransactionOutPoint transactionOutPoint = transactionOutput.getOutPointFor();
                    if (proposalsDao.isLockedOutput(transactionOutPoint.getHash().getBytes(),transactionOutPoint.getIndex())) {
                        continue;
                    }
                    totalInputsValue = totalInputsValue.add(transactionOutput.getValue());
                    unspentTransactions.add(transactionOutput);
                    if (totalInputsValue.isGreaterThan(totalOuputsValue)) {
                        inputsSatisfiedContractValue = true;
                        break;
                    }
                }

                if (!inputsSatisfiedContractValue)
                    throw new InsuficientBalanceException("Inputs not satisfied contract value");

                // add inputs..
                proposalTransactionBuilder.addInputs(unspentTransactions);

                // lock address output
                Address lockAddress = wallet.freshReceiveAddress();
                TransactionOutput transactionOutputToLock = proposalTransactionBuilder.addLockedAddressOutput(lockAddress);
                // lock address
                byte[] parentTransactionHash = transactionOutputToLock.getParentTransactionHash().getBytes();
                proposalsDao.lockOutput(proposal.getId(),parentTransactionHash,0);
                proposal.setLockedOutputHash(parentTransactionHash);
                proposal.setLockedOutputIndex(0);
                // lock balance
                lockedBalance+=transactionOutputToLock.getValue().value;

                // refund transaction, tengo el fee agregado al totalOutputsValue
                Coin flyingCoins = totalInputsValue.minus(totalOuputsValue);
                // le resto el fee
                flyingCoins = flyingCoins.minus(Coin.valueOf(proposal.getExtraFeeValue())).minus(WalletConstants.CONTEXT.getFeePerKb());
                proposalTransactionBuilder.addRefundOutput(flyingCoins, wallet.freshReceiveAddress());


                // contract
                proposalTransactionBuilder.addContract(
                        proposal.getStartBlock(),
                        proposal.getEndBlock(),
                        proposal.getBlockReward(),
                        proposal.getOwnerPubKey(),
                        proposal.hash()
                );

                // beneficiaries outputs
                for (Map.Entry<String, Long> beneficiary : proposal.getBeneficiaries().entrySet()) {
                    proposalTransactionBuilder.addBeneficiary(
                            Address.fromBase58(WalletConstants.NETWORK_PARAMETERS, beneficiary.getKey()),
                            Coin.valueOf(beneficiary.getValue())
                    );

                }
                // build the transaction..
                Transaction tran = proposalTransactionBuilder.build();

                LOG.info("Transaction fee: " + tran.getFee());

                SendRequest sendRequest = SendRequest.forTx(tran);

                sendRequest.signInputs = true;
                sendRequest.shuffleOutputs = false;
                sendRequest.coinSelector = new MyCoinSelector();


                LOG.info("inputs value: " + tran.getInputSum().toFriendlyString() + ", outputs value: " + tran.getOutputSum().toFriendlyString() + ", fee: " + tran.getFee().toFriendlyString());
                LOG.info("total en el aire: " + tran.getInputSum().minus(tran.getOutputSum().minus(tran.getFee())).toFriendlyString());

                try {
//                    wallet.completeTx(sendRequest);
//
//                    wallet.commitTx(sendRequest.tx);
//
//                    blockchainManager.broadcastTransaction(sendRequest.tx.getHash().getBytes()).get();;

                    LOG.info("TRANSACCION BROADCASTEADA EXITOSAMENTE!");

                    // server update data
                    context.getProfileServerManager().updateExtraData(profile,proposal.buildExtraData());

                } catch (InsufficientMoneyException e) {
                    e.printStackTrace();
                    LOG.info("fondos disponibles: " + wallet.getBalance());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }catch (CantSaveProposalException e){
                throw e;
            }catch (InsuficientBalanceException e) {
                throw e;
            }catch (CantSendProposalException e){
                throw e;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public List<Proposal> getProposals() {
        return proposalsDao.listProposals();
    }

    public String getNewAddress() {
        return walletManager.getWallet().freshReceiveAddress().toBase58();
    }

    public long getAvailableBalance() {
        return walletManager.getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).value-lockedBalance;
    }

    public long getLockedBalance() {
        return lockedBalance;
    }

    private class MyCoinSelector implements org.bitcoinj.wallet.CoinSelector {
        @Override
        public CoinSelection select(Coin coin, List<TransactionOutput> list) {
            return new CoinSelection(coin,new ArrayList<TransactionOutput>());
        }
    }


    /**
     * Forum methods
     */


    public boolean isForumRegistered() {
        return flarumClient.isRegistered();
    }

    public ForumProfile getForumProfile(){
        return forumProfile;
    }

    public boolean registerForumUser(String username, String password, String email) throws FlarumClientInvalidDataException {
        forumProfile = new ForumProfile(username,password,email);
        boolean response = flarumClient.registerUser(username,password,email);
        if (response){
            forumConfigurations.setIsRegistered(true);
            forumConfigurations.setForumUser(username,password,email);
        }
        return response;
    }

    public boolean connectToForum(String username,String password) throws FlarumClientInvalidDataException{
        boolean response = flarumClient.connect(username,password);
        if (response) {
            forumConfigurations.setIsRegistered(true);
            forumConfigurations.setForumUser(username,password,null);
        }
        return response;
    }

    public boolean startDiscussion(Proposal proposal) throws FlarumClientInvalidDataException {
        boolean response =  flarumClient.createDiscussion(proposal.getTitle(),proposal.getBody());
        return response;
    }

    public boolean connectToForum() throws FlarumClientInvalidDataException {
        boolean response = flarumClient.connect(forumProfile.getUsername(),forumProfile.getPassword());
        return response;
    }

    public String getForumToken() {
        return flarumClient.getToken();
    }

}
