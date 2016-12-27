package iop_sdk.wallet;

import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.DefaultCoinSelector;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import iop_sdk.blockchain.NotConnectedPeersException;
import iop_sdk.governance.vote.VotesDao;
import iop_sdk.wallet.exceptions.InsuficientBalanceException;
import iop_sdk.governance.vote.Vote;
import iop_sdk.governance.vote.VoteTransactionBuilder;

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

    public void forVote(Vote vote) throws InsuficientBalanceException {

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
        boolean inputsSatisfiedContractValue = false;
        for (TransactionOutput transactionOutput : wallet.getUnspents()) {
            //
            TransactionOutPoint transactionOutPoint = transactionOutput.getOutPointFor();
            if (votesDaoImp.isLockedOutput(transactionOutPoint.getHash().toString(), transactionOutPoint.getIndex())) {
                continue;
            }
            if (DefaultCoinSelector.isSelectable(transactionOutput.getParentTransaction())) {
                LOG.info("adding non locked transaction to spend as an input: postion:" + transactionOutPoint.getIndex() + ", parent hash: " + transactionOutPoint.toString());
                totalInputsValue = totalInputsValue.add(transactionOutput.getValue());
                unspentTransactions.add(transactionOutput);
                if (totalInputsValue.isGreaterThan(totalOuputsValue)) {
                    inputsSatisfiedContractValue = true;
                    break;
                }
            }
        }

        if (!inputsSatisfiedContractValue)
            throw new InsuficientBalanceException("Inputs not satisfied vote value");


        // add inputs..
        voteTransactionBuilder.addInputs(unspentTransactions);
        // freeze address -> voting power
        Address lockAddress = wallet.freshReceiveAddress();
        TransactionOutput transactionOutputToLock = voteTransactionBuilder.addLockedAddressOutput(lockAddress,vote.getVotingPower());
        //update locked balance
        lockedBalance+=transactionOutputToLock.getValue().getValue();
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
