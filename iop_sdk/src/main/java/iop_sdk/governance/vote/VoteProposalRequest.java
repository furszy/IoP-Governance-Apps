package iop_sdk.governance.vote;

import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import iop_sdk.blockchain.NotConnectedPeersException;
import iop_sdk.wallet.BlockchainManager;
import iop_sdk.wallet.CantSendVoteException;
import iop_sdk.wallet.WalletManager;
import iop_sdk.wallet.WalletPreferenceConfigurations;
import iop_sdk.wallet.exceptions.InsuficientBalanceException;

import static iop_sdk.wallet.utils.WalletUtils.sumValue;

/**
 * Created by mati on 21/12/16.
 * //todo: el fee tiene que ser el 1% de la cantidad de votos. -> 0.5 votes -> 0.005 fee
 */

public class VoteProposalRequest {

    private static final Logger LOG = LoggerFactory.getLogger(VoteProposalRequest.class);


    private BlockchainManager blockchainManager;
    private WalletManager walletManager;
    private VotesDao votesDaoImp;
    private WalletPreferenceConfigurations conf;

    private SendRequest sendRequest;

    private Vote vote;
    private long lockedBalance;


    public VoteProposalRequest(BlockchainManager blockchainManager, WalletManager walletManager, VotesDao votesDaoImp) {
        this.blockchainManager = blockchainManager;
        this.walletManager = walletManager;
        this.votesDaoImp = votesDaoImp;
        this.conf = walletManager.getConfigurations();
    }

    public void forVote(Vote vote) throws InsuficientBalanceException,CantSendVoteException {

        if (vote.getVote() == Vote.VoteType.NEUTRAL) throw new IllegalArgumentException("Voto tipo neutral");

        this.vote = vote;

        org.bitcoinj.core.Context.propagate(conf.getWalletContext());

        VoteTransactionBuilder voteTransactionBuilder = new VoteTransactionBuilder(conf.getNetworkParams());

        Wallet wallet = walletManager.getWallet();


        Coin freezeOutputVotingPowerValue = Coin.valueOf(vote.getVotingPower());
        Coin feeForVoting = Coin.valueOf(vote.getVotingPower()/100);
        Coin totalOuputsValue = feeForVoting.plus(freezeOutputVotingPowerValue);

        List<TransactionOutput> unspentTransactions = new ArrayList<>();
        Coin totalInputsValue = Coin.ZERO;
        // check if the vote is already used, if the wallet have the genesisTx of the vote we reuse the freeze output as input of the new vote.
        if (vote.getLockedOutputHex()!=null){
            LOG.info("Reusing the previous vote tx, adding the frozen output as input of the tx");
            Map<Sha256Hash, Transaction> pool = wallet.getTransactionPool(WalletTransaction.Pool.UNSPENT);
            TransactionOutput prevFrozenOutput = pool.get(Sha256Hash.wrap(vote.getLockedOutputHex())).getOutput(0);
            unspentTransactions.add(prevFrozenOutput);
        }
        // fill the tx with valid inputs
        if (!sumValue(unspentTransactions).isGreaterThan(totalOuputsValue) && !totalOuputsValue.isNegative() && totalOuputsValue.getValue()!=0) {
            unspentTransactions = walletManager.getInputsForAmount(totalOuputsValue);
        }
        // inputs value
        totalInputsValue = sumValue(unspentTransactions);
        // put inputs..
        voteTransactionBuilder.addInputs(unspentTransactions);
        // first check if the value is non dust
        if (Transaction.MIN_NONDUST_OUTPUT.isGreaterThan(Coin.valueOf(vote.getVotingPower()))){
            throw new CantSendVoteException("Vote value is to small to be included, min value: "+Transaction.MIN_NONDUST_OUTPUT.toFriendlyString());
        }
        // freeze address -> voting power
        Address lockAddress = wallet.freshReceiveAddress();
        TransactionOutput transactionOutputToLock = voteTransactionBuilder.addLockedAddressOutput(lockAddress,vote.getVotingPower());
        //update locked balance
        lockedBalance+=Coin.valueOf(vote.getVotingPower()).getValue();
        // op return output
        voteTransactionBuilder.addContract(vote.isYesVote(),vote.getGenesisHash());
        // refunds output
        // le resto el fee
        Coin flyingCoins = totalInputsValue.minus(totalOuputsValue).minus(feeForVoting).minus(conf.getWalletContext().getFeePerKb());
        voteTransactionBuilder.addRefundOutput(flyingCoins, wallet.freshReceiveAddress());

        // build the transaction..
        Transaction tran = voteTransactionBuilder.build();

        LOG.info("Transaction fee: " + tran.getFee());

        sendRequest = SendRequest.forTx(tran);

        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false;
        sendRequest.coinSelector = new MyCoinSelector();

        StringBuilder outputsValue = new StringBuilder();
        int i=0;
        for (TransactionOutput transactionOutput : sendRequest.tx.getOutputs()) {
            outputsValue.append("Output "+i+" value "+transactionOutput.getValue().getValue());
            outputsValue.append("\n");
            i++;
        }

        LOG.info(" *Outpus value:\n "+outputsValue.toString());
        LOG.info("Total outputs sum: "+sendRequest.tx.getOutputSum());
        LOG.info("Total inputs sum sum: "+sendRequest.tx.getInputSum());

        // complete transaction
        try {
            wallet.completeTx(sendRequest);
        } catch (InsufficientMoneyException e) {
            LOG.error("Insuficient money exception",e);
            throw new InsuficientBalanceException("Insuficient money exception",e);
        }

        LOG.info("inputs value: " + tran.getInputSum().toFriendlyString() + ", outputs value: " + tran.getOutputSum().toFriendlyString() + ", fee: " + tran.getFee().toFriendlyString());
        LOG.info("total en el aire: " + tran.getInputSum().minus(tran.getOutputSum().minus(tran.getFee())).toFriendlyString());
    }


    public void broadcast() throws NotConnectedPeersException {
        Wallet wallet = walletManager.getWallet();
        try {

            // check if we have at least one peer connected
            if(blockchainManager.getConnectedPeers().isEmpty()) throw new NotConnectedPeersException();

            wallet.commitTx(sendRequest.tx);

            ListenableFuture<Transaction> future = blockchainManager.broadcastTransaction(sendRequest.tx.getHash().getBytes());
            future.get(1, TimeUnit.MINUTES);

            // now that the transaction is complete lock the output
            // lock address
            String parentTransactionHashHex = sendRequest.tx.getHash().toString();
            LOG.info("Locking transaction with 1000 IoPs: position: "+0+", parent hash: "+parentTransactionHashHex);
            vote.setLockedOutputHashHex(parentTransactionHashHex);
            vote.setLockedOutputIndex(0);

            LOG.info("TRANSACCION BROADCASTEADA EXITOSAMENTE, hash: "+sendRequest.tx.getHash().toString());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new NotConnectedPeersException(e);
        }
    }

    public long getLockedBalance() {
        return lockedBalance;
    }

    public Vote getUpdatedVote() {
        return vote;
    }

    private class MyCoinSelector implements org.bitcoinj.wallet.CoinSelector {
        @Override
        public CoinSelection select(Coin coin, List<TransactionOutput> list) {
            return new CoinSelection(coin,new ArrayList<TransactionOutput>());
        }
    }

}
