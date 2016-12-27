package iop_sdk.governance.propose;

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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import iop_sdk.blockchain.NotConnectedPeersException;
import iop_sdk.wallet.WalletPreferenceConfigurations;
import iop_sdk.wallet.exceptions.InsuficientBalanceException;
import iop_sdk.wallet.BlockchainManager;
import iop_sdk.wallet.WalletManager;

;

/**
 * Created by mati on 05/12/16.
 */

public class ProposalTransactionRequest {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalTransactionRequest.class.getName());

    private BlockchainManager blockchainManager;
    private WalletManager walletManager;
    private ProposalsContractDao proposalsDao;
    private WalletPreferenceConfigurations conf;

    private Proposal proposal;
    private String lockedOutputHashHex;
    private int lockedOutputPosition = 0;
    private long lockedBalance;

    private SendRequest sendRequest;

    public ProposalTransactionRequest(BlockchainManager blockchainManager, WalletManager walletManager, ProposalsContractDao proposalsDao) {
        this.blockchainManager = blockchainManager;
        this.walletManager = walletManager;
        this.proposalsDao = proposalsDao;
        this.conf = walletManager.getConfigurations();
    }

    public void forProposal(Proposal proposal) throws InsuficientBalanceException {

        this.proposal = proposal;

        org.bitcoinj.core.Context.propagate(conf.getWalletContext());

        ProposalTransactionBuilder proposalTransactionBuilder = new ProposalTransactionBuilder(
                conf.getNetworkParams(),
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
            if (proposalsDao.isLockedOutput(transactionOutPoint.getHash().toString(), transactionOutPoint.getIndex())) {
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
            throw new InsuficientBalanceException("Inputs not satisfied contract value");

        // add inputs..
        proposalTransactionBuilder.addInputs(unspentTransactions);

        // lock address output
        Address lockAddress = wallet.freshReceiveAddress();
        TransactionOutput transactionOutputToLock = proposalTransactionBuilder.addLockedAddressOutput(lockAddress);

        // lock balance
        lockedBalance += transactionOutputToLock.getValue().value;

        // refund transaction, tengo el fee agregado al totalOutputsValue
        Coin flyingCoins = totalInputsValue.minus(totalOuputsValue);
        // le resto el fee
        flyingCoins = flyingCoins.minus(Coin.valueOf(proposal.getExtraFeeValue())).minus(conf.getWalletContext().getFeePerKb());
        proposalTransactionBuilder.addRefundOutput(flyingCoins, wallet.freshReceiveAddress());


        // contract
        proposalTransactionBuilder.addContract(
                proposal.getStartBlock(),
                proposal.getEndBlock(),
                proposal.getBlockReward(),
                proposal.hash(),
                proposal.getForumId()
        );

        // beneficiaries outputs
        for (Map.Entry<String, Long> beneficiary : proposal.getBeneficiaries().entrySet()) {
            LOG.info("beneficiary address: "+beneficiary.getKey());
            proposalTransactionBuilder.addBeneficiary(
                    Address.fromBase58(conf.getNetworkParams(), beneficiary.getKey()),
                    Coin.valueOf(beneficiary.getValue())
            );

        }
        // build the transaction..
        Transaction tran = proposalTransactionBuilder.build();

        LOG.info("Transaction fee: " + tran.getFee());

        sendRequest = SendRequest.forTx(tran);

        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false;
        sendRequest.coinSelector = new MyCoinSelector();

        // complete transaction
        try {
            wallet.completeTx(sendRequest);
        } catch (InsufficientMoneyException e) {
            LOG.error("Insuficient money exception",e);
            throw new InsuficientBalanceException("Insuficient money exception",e);
        } catch (Wallet.DustySendRequested e) {
            e.printStackTrace();
            LOG.error("DustySendRequest: "+sendRequest.tx+", wallet: "+wallet);
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
            future.get(1,TimeUnit.MINUTES);

            // now that the transaction is complete lock the output
            // lock address
            String parentTransactionHashHex = sendRequest.tx.getHash().toString();
            LOG.info("Locking transaction with 1000 IoPs: position: "+0+", parent hash: "+parentTransactionHashHex);
            proposal.setLockedOutputHashHex(parentTransactionHashHex);
            proposal.setLockedOutputIndex(lockedOutputPosition);
            lockedOutputHashHex = parentTransactionHashHex;
            lockedOutputPosition = 0;

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

    public Proposal getUpdatedProposal() {
        return proposal;
    }

    public long getLockedBalance() {
        return lockedBalance;
    }

    public Transaction getTransaction() {
        return sendRequest.tx;
    }


    private class MyCoinSelector implements org.bitcoinj.wallet.CoinSelector {
        @Override
        public CoinSelection select(Coin coin, List<TransactionOutput> list) {
            return new CoinSelection(coin,new ArrayList<TransactionOutput>());
        }
    }

    public String getLockedOutputHashHex() {
        return lockedOutputHashHex;
    }

    public int getLockedOutputPosition() {
        return lockedOutputPosition;
    }
}
