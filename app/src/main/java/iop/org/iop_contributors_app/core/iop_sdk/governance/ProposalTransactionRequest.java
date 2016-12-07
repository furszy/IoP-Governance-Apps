package iop.org.iop_contributors_app.core.iop_sdk.governance;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import iop.org.iop_contributors_app.wallet.BlockchainManager;
import iop.org.iop_contributors_app.wallet.WalletConstants;
import iop.org.iop_contributors_app.wallet.WalletManager;
import iop.org.iop_contributors_app.wallet.exceptions.InsuficientBalanceException;

/**
 * Created by mati on 05/12/16.
 */

public class ProposalTransactionRequest {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalTransactionRequest.class.getName());

    private BlockchainManager blockchainManager;
    private WalletManager walletManager;
    private ProposalsContractDao proposalsDao;

    private byte[] lockedOutputHash;
    private int lockedOutputPosition;
    private long lockedBalance;

    private SendRequest sendRequest;

    public ProposalTransactionRequest(BlockchainManager blockchainManager, WalletManager walletManager, ProposalsContractDao proposalsDao) {
        this.blockchainManager = blockchainManager;
        this.walletManager = walletManager;
        this.proposalsDao = proposalsDao;
    }

    public void forProposal(Proposal proposal) throws InsuficientBalanceException, InsufficientMoneyException {

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
            if (proposalsDao.isLockedOutput(transactionOutPoint.getHash().getBytes(), transactionOutPoint.getIndex())) {
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
        proposal.setLockedOutputHash(parentTransactionHash);
        proposal.setLockedOutputIndex(lockedOutputPosition);
        lockedOutputHash = parentTransactionHash;
        lockedOutputPosition = 0;


        // lock balance
        lockedBalance += transactionOutputToLock.getValue().value;

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
                proposal.hash()
        );

        // beneficiaries outputs
        for (Map.Entry<String, Long> beneficiary : proposal.getBeneficiaries().entrySet()) {
            LOG.info("beneficiary address: "+beneficiary.getKey());
            proposalTransactionBuilder.addBeneficiary(
                    Address.fromBase58(WalletConstants.NETWORK_PARAMETERS, beneficiary.getKey()),
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


        LOG.info("inputs value: " + tran.getInputSum().toFriendlyString() + ", outputs value: " + tran.getOutputSum().toFriendlyString() + ", fee: " + tran.getFee().toFriendlyString());
        LOG.info("total en el aire: " + tran.getInputSum().minus(tran.getOutputSum().minus(tran.getFee())).toFriendlyString());

    }

    public void broadcast() throws InsufficientMoneyException {
        Wallet wallet = walletManager.getWallet();
        try {

            wallet.completeTx(sendRequest);

            wallet.commitTx(sendRequest.tx);

            blockchainManager.broadcastTransaction(sendRequest.tx.getHash().getBytes()).get();

            LOG.info("TRANSACCION BROADCASTEADA EXITOSAMENTE!");

        } catch (InsufficientMoneyException e) {
            LOG.info("fondos disponibles: " + wallet.getBalance());
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }



    private class MyCoinSelector implements org.bitcoinj.wallet.CoinSelector {
        @Override
        public CoinSelection select(Coin coin, List<TransactionOutput> list) {
            return new CoinSelection(coin,new ArrayList<TransactionOutput>());
        }
    }

    public byte[] getLockedOutputHash() {
        return lockedOutputHash;
    }

    public int getLockedOutputPosition() {
        return lockedOutputPosition;
    }
}
