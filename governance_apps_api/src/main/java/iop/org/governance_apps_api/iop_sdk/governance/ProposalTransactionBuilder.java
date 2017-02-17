package iop.org.governance_apps_api.iop_sdk.governance;

import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import iop.org.iop_contributors_app.core.iop_sdk.blockchain.OpReturnOutputTransaction;

import static iop.org.iop_contributors_app.core.iop_sdk.utils.ArraysUtils.numericTypeToByteArray;

/**
 * Created by mati on 11/11/16.
 */
public  class ProposalTransactionBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalTransactionBuilder.class);

    public static final Coin FREEZE_VALUE = Coin.valueOf(1000,0);

    // position
    private static final int CONTRACT_TAG_POSITION = 0;
    private static final int CONTRACT_VERSION_POSITION = 2;
    private static final int CONTRACT_START_HEIGHT_POSITION = 4;
    private static final int CONTRACT_END_HEIGHT_POSITION = 7;
    private static final int CONTRACT_REWARD_POSITION = 9;
    private static final int CONTRACT_HASH_POSITION = 12;
    // size
    private static final int CONTRACT_SIZE = 44;
    private static final int CONTRACT_TAG_SIZE = 2;
    private static final int CONTRACT_VERSION_SIZE = 2;
    private static final int CONTRACT_START_HEIGHT_SIZE = 3;
    private static final int CONTRACT_END_HEIGHT_SIZE = 2;
    private static final int CONTRACT_REWARD_SIZE = 3;
    private static final int CONTRACT_HASH_SIZE = 32;

    /** tag */
    private static short tag = 0x4343;
    /**  */
    private static short version = 0x0100;

    private NetworkParameters networkParameters;

    private Transaction proposalTransaction = null;

    private int blockStartHeight;

    private int endHeight;
    /** in IoPtoshis.. */
    private long blockReward;

    private byte[] proposalHash;

    private int bestChainHeight;

    private List<TransactionOutput> inputs;

    private List<TransactionOutput> prevOpOutputs;

    private List<TransactionOutput> postOpOutputs;

    private OpReturnOutputTransaction contractTransaction;

    /** height+1000+startBlock */
    private int blockchainStartBlock;
    /** blockchainStartBlock+endHeight  */
    private int blockchainEndBlock;
    /** blockchainEndBlock-blockchainStartBlock */
    private int totalBlocks;
    /**    */
    private long totalReward;

    // inputs total amount
    private Coin totalCoins;



    public ProposalTransactionBuilder(NetworkParameters networkParameters, int bestChainHeight) {

        this.networkParameters = networkParameters;
        this.bestChainHeight = bestChainHeight;

        this.proposalTransaction = new Transaction(networkParameters);

        this.totalCoins = Coin.ZERO;

        inputs = new ArrayList<>();
        prevOpOutputs = new ArrayList<>();
        postOpOutputs = new ArrayList<>();
    }

    private void checkValid() {
        if (bestChainHeight>(blockStartHeight+1000)){
            throw new IllegalArgumentException("blockStartHeight must be 1000 blocks away from the bestChainHeight");
        }
    }

    /**
     * Add inputs
     *
     * @param unspentTransactions
     * @return
     */
    public ProposalTransactionBuilder addInputs(List<TransactionOutput> unspentTransactions) {
        // ahora que tengo los inputs los agrego
        for (TransactionOutput unspentTransaction : unspentTransactions) {
            inputs.add(unspentTransaction);
        }
        return this;
    }


    /* Coins to lock, voting power
    *
    * @param address
    * @return transaction hash
    */
    public ProposalTransactionBuilder addLockedAddressOutput(Address address, byte[] lockedOutputHash){
        totalCoins=totalCoins.minus(FREEZE_VALUE);
        TransactionOutput transactionOutput = new TransactionOutput(networkParameters,proposalTransaction, FREEZE_VALUE,address);
        prevOpOutputs.add(transactionOutput);
        if (lockedOutputHash!=null)System.arraycopy(transactionOutput.getHash().getBytes(),0,lockedOutputHash,0,lockedOutputHash.length);
        return this;
    }

    public TransactionOutput addLockedAddressOutput(Address address){
        totalCoins=totalCoins.minus(FREEZE_VALUE);
        TransactionOutput transactionOutput = new TransactionOutput(networkParameters,proposalTransaction, FREEZE_VALUE,address);
        prevOpOutputs.add(transactionOutput);
        return transactionOutput;
    }

    /**
     *
     *
     * @param refundCoins
     * @param address
     */
    public ProposalTransactionBuilder addRefundOutput(Coin refundCoins, Address address){
        totalCoins=totalCoins.minus(refundCoins);
        prevOpOutputs.add(new TransactionOutput(networkParameters,proposalTransaction,refundCoins,address));
        return this;
    }

    public ProposalTransactionBuilder addContract(int blockStartHeight, int endHeight, long blockReward, byte[] proposalHash){

        if (proposalHash.length!=32) throw new IllegalArgumentException("hash is not from SHA256");

        this.blockStartHeight = blockStartHeight;
        this.endHeight = endHeight;
        this.blockReward = blockReward;
        this.proposalHash = proposalHash;


        // data
        blockchainStartBlock = bestChainHeight+1000+blockStartHeight;
        blockchainEndBlock = blockchainStartBlock+endHeight;
        totalBlocks = blockchainEndBlock-blockchainStartBlock;
        totalReward = blockReward*totalBlocks;


        try {

            // data
            byte[] prevData = new byte[CONTRACT_SIZE];
            numericTypeToByteArray(prevData,tag,CONTRACT_TAG_POSITION,CONTRACT_TAG_SIZE);
            numericTypeToByteArray(prevData,version,CONTRACT_VERSION_POSITION,CONTRACT_VERSION_SIZE);
            numericTypeToByteArray(prevData,blockStartHeight,CONTRACT_START_HEIGHT_POSITION,CONTRACT_START_HEIGHT_SIZE);
            numericTypeToByteArray(prevData,endHeight,CONTRACT_END_HEIGHT_POSITION,CONTRACT_END_HEIGHT_SIZE);
            numericTypeToByteArray(prevData,blockReward,CONTRACT_REWARD_POSITION,CONTRACT_REWARD_SIZE);
            System.arraycopy(proposalHash,0,prevData,CONTRACT_HASH_POSITION,CONTRACT_HASH_SIZE);

            OpReturnOutputTransaction opReturnOutputTransaction = new OpReturnOutputTransaction.Builder(networkParameters)
                    .setParentTransaction(proposalTransaction)
                    .addData(prevData)
                    .build2();

            LOG.info("OP_RETURN TRANSACTION created, data: "+opReturnOutputTransaction.toString());
            contractTransaction = opReturnOutputTransaction;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Add a beneficiary
     *
     * @param address
     * @param coinPerBlock
     */
    public ProposalTransactionBuilder addBeneficiary(Address address, Coin coinPerBlock){
        totalCoins= totalCoins.minus(coinPerBlock);
        postOpOutputs.add(new TransactionOutput(networkParameters,proposalTransaction,coinPerBlock,address));
        return this;
    }

    public Transaction build(){

        // add inputs
        for (TransactionOutput input : inputs) {
            proposalTransaction.addInput(input);
        }

        // first add the prev contract outputs (like lock coins, refund)
        for (TransactionOutput prevOpOutput : prevOpOutputs) {
            proposalTransaction.addOutput(prevOpOutput);
        }

        // contract output
        proposalTransaction.addOutput(contractTransaction);


        // beneficiaries outputs
        long totalOutputReward = 0;
        for (TransactionOutput postOpOutput : postOpOutputs) {
            proposalTransaction.addOutput(postOpOutput);
            totalOutputReward+=postOpOutput.getValue().getValue();
        }

        if (blockReward<totalOutputReward){
            throw new IllegalArgumentException("total reward in the beneficiaries outputs is different that the total in the contract.");
        }

        return proposalTransaction;
    }


    /**
     * Metodo para decodificar el valor del op_retun
     *
     * @param  data -> OP_RETUTN data
     * @return
     */
    public static Proposal decodeContract(byte[] data) throws DecoderException, UnsupportedEncodingException {

        if (data.length!=CONTRACT_SIZE) throw new IllegalArgumentException("data has not the right size: "+data.length);

        Proposal proposal = new Proposal();

        short newTag = getShortData(data,CONTRACT_TAG_POSITION,CONTRACT_TAG_SIZE);
        if (tag != newTag ) throw new IllegalArgumentException("data tag is not the right one, tag: "+newTag) ;

        proposal.setVersion(getShortData(data,CONTRACT_VERSION_POSITION,CONTRACT_VERSION_SIZE));
        proposal.setStartBlock(getIntData(data,CONTRACT_START_HEIGHT_POSITION,CONTRACT_START_HEIGHT_SIZE));
        proposal.setEndBlock(getIntData(data,CONTRACT_END_HEIGHT_POSITION,CONTRACT_END_HEIGHT_SIZE));
        proposal.setBlockReward(getLongData(data,CONTRACT_REWARD_POSITION,CONTRACT_REWARD_SIZE));

        if (proposal.checkHash(getByteArray(data,CONTRACT_HASH_POSITION,CONTRACT_HASH_SIZE))) throw new IllegalArgumentException("Hash don't match");

        return proposal;
    }

    private static byte[] getByteArray(byte[] data, int init, int lenght) {
        byte[] retDat = new byte[init+lenght];
        System.arraycopy(data,init,retDat,0,init+lenght);
        return retDat;
    }


    private static int getIntData(byte[] data, int init, int lenght){
        byte[] retDat = new byte[init+lenght];
        System.arraycopy(data,init,retDat,0,init+lenght);
        String versionStr = org.libsodium.jni.encoders.Hex.HEX.encode(retDat);
        return new BigInteger(versionStr,16).intValue();
    }

    private static short getShortData(byte[] data, int init, int lenght){
        byte[] retDat = new byte[init+lenght];
        System.arraycopy(data,init,retDat,0,init+lenght);
        String versionStr = org.libsodium.jni.encoders.Hex.HEX.encode(retDat);
        return new BigInteger(versionStr,16).shortValue();
    }

    private static long getLongData(byte[] data, int init, int lenght){
        byte[] retDat = new byte[init+lenght];
        System.arraycopy(data,init,retDat,0,init+lenght);
        String versionStr = org.libsodium.jni.encoders.Hex.HEX.encode(retDat);
        return new BigInteger(versionStr,16).longValue();
    }

}
