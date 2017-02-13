package iop.org.iop_contributors_app;

import junit.framework.Assert;

import org.apache.commons.codec.DecoderException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.RegTestParams;
import org.junit.Test;
import org.libsodium.jni.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

import iop_sdk.blockchain.OpReturnOutputTransaction;
import iop_sdk.crypto.CryptoBytes;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.propose.ProposalTransactionBuilder;

/**
 * Created by mati on 18/11/16.
 */

public class ProposalTransactionTest {

    /** tag */
    private static short tag = 0x4343;
    /**  */
    private static short version = 256;

    // 1500
    private int startHeight = 1500;
    // 1000
    private short endBlock = 0x03e8;
    // 2
    private int blockReward = 20000;





    // El problema era el endian, rodri lo puso en big endian (es como se lee), little endian es al reves.

    @Test
    public void createOpContract(){

        OpReturnOutputTransaction.Builder builder = new OpReturnOutputTransaction.Builder(RegTestParams.get());
        byte[] prevData = new byte[12];
        numericTypeToByteArray(prevData,tag,0,2);
        numericTypeToByteArray(prevData,version,2,2);
        numericTypeToByteArray(prevData,startHeight,4,3);
        numericTypeToByteArray(prevData,endBlock,7,2);
        numericTypeToByteArray(prevData,blockReward,9,3);
        builder.addData(prevData);

        OpReturnOutputTransaction opReturnOutputTransaction = builder.build2();

        byte[] data = opReturnOutputTransaction.getData();

        // chequeo el tag

        Assert.assertEquals(tag,getData(data,0,2));
        // chequeo la version
        Assert.assertEquals(version,getData(data,2,4));
        // init height
        Assert.assertEquals(startHeight,getData(data,4,7));
        // end block
        Assert.assertEquals(endBlock,getData(data,7,9));
        // block reward
        Assert.assertEquals(blockReward,getData(data,9,12));

    }

    // position
    private static final int CONTRACT_TAG_POSITION = 0;
    private static final int CONTRACT_VERSION_POSITION = 2;
    private static final int CONTRACT_START_HEIGHT_POSITION = 4;
    private static final int CONTRACT_END_HEIGHT_POSITION = 7;
    private static final int CONTRACT_REWARD_POSITION = 9;
    private static final int CONTRACT_HASH_POSITION = 12;
    private static final int CONTRACT_FORUM_ID_POSITION = 44;
    // size
    private static final int CONTRACT_SIZE = 46;
    private static final int CONTRACT_TAG_SIZE = 2;
    private static final int CONTRACT_VERSION_SIZE = 2;
    private static final int CONTRACT_START_HEIGHT_SIZE = 3;
    private static final int CONTRACT_END_HEIGHT_SIZE = 2;
    private static final int CONTRACT_REWARD_SIZE = 3;
    private static final int CONTRACT_HASH_SIZE = 32;
    private static final int CONTRACT_FORUM_ID_SIZE = 2;

    @Test
    public void createContract2(){

        int blockStartHeight = 20;
        int endHeight = 60;
        long blockReward = 10000000;
        byte[] proposalHash = CryptoBytes.fromHexToBytes("0080a9f7727726783617077919407ceec77865f5ae67d908b87ab0b42ef55fc9");
        int forumId = 110;

        // data
        byte[] prevData = new byte[CONTRACT_SIZE];
        numericTypeToByteArray(prevData,tag,CONTRACT_TAG_POSITION,CONTRACT_TAG_SIZE);
        numericTypeToByteArray(prevData,version,CONTRACT_VERSION_POSITION,CONTRACT_VERSION_SIZE);
        numericTypeToByteArray(prevData,blockStartHeight,CONTRACT_START_HEIGHT_POSITION,CONTRACT_START_HEIGHT_SIZE);
        numericTypeToByteArray(prevData,endHeight,CONTRACT_END_HEIGHT_POSITION,CONTRACT_END_HEIGHT_SIZE);
        numericTypeToByteArray(prevData,blockReward,CONTRACT_REWARD_POSITION,CONTRACT_REWARD_SIZE);
        System.arraycopy(proposalHash,0,prevData,CONTRACT_HASH_POSITION,CONTRACT_HASH_SIZE);
        numericTypeToByteArray(prevData,forumId,CONTRACT_FORUM_ID_POSITION,CONTRACT_FORUM_ID_SIZE);

        Transaction transaction = new Transaction(RegTestParams.get());

        OpReturnOutputTransaction opReturnOutputTransaction = new OpReturnOutputTransaction.Builder(RegTestParams.get())
                .setParentTransaction(transaction)
                .addData(prevData)
                .build2();

        System.out.println(getData2Hex(opReturnOutputTransaction.getData(),CONTRACT_REWARD_POSITION,CONTRACT_REWARD_SIZE));

        Assert.assertEquals(blockReward,getData2(prevData,CONTRACT_REWARD_POSITION,CONTRACT_REWARD_SIZE));
        // block reward
        Assert.assertEquals(blockReward,getData2(opReturnOutputTransaction.getData(),CONTRACT_REWARD_POSITION,CONTRACT_REWARD_SIZE));




    }


    @Test
    public void decodeContract(){

        String contractHash = "3e50cb73af308e0c4861c57b209554a30a4ccf261f65b237a3f92c44dc9f2b14";


        byte[] bytes = org.spongycastle.util.encoders.Hex.decode(contractHash);

        try {
            Proposal proposal = ProposalTransactionBuilder.decodeContract(bytes);
            System.out.println(proposal);
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    byte[] toBytes(int i)
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }
    /**
     * Field
     Description
     Size
     Voting tag
     A tag indicating that this transaction is a IoP Voting transaction. It is always 0x564f54.
     3 bytes
     Voting Power
     The voter decision. 1 = Yes, 0 = NO
     1 byte
     Genesis Transaction
     Sha256 hash of the transaction that originated the contract
     32 bytes
     */

    @Test
    public void createOpContractVoting(){

        OpReturnOutputTransaction.Builder builder = new OpReturnOutputTransaction.Builder(RegTestParams.get());
        byte[] prevData = new byte[36];

        int tag = 0x564f54;
        int voting = 1;

        byte[] hash = new byte[32];
        for (int i = 0; i < hash.length; i++) {
            hash[i] = (byte) i;
        }

        numericTypeToByteArray(prevData,tag,0,3);
        numericTypeToByteArray(prevData,voting,3,1);
        System.arraycopy(hash,0,prevData,4,32);
        builder.addData(prevData);

        OpReturnOutputTransaction opReturnOutputTransaction = builder.build2();

        byte[] data = opReturnOutputTransaction.getData();

        // chequeo el tag

        Assert.assertEquals(tag,getData(data,0,3));
        // chequeo la version
        Assert.assertEquals(voting,getData(data,3,4));
        // init height
        byte[] res = new byte[32];
        System.arraycopy(data,4,res,0,32);
        boolean result = Arrays.equals(hash,res);
        Assert.assertEquals(true,result);
    }

    private int getData(byte[] data, int init, int end){
        byte[] retDat = new byte[end-init];
        System.arraycopy(data,init,retDat,0,end-init);
        String versionStr = Hex.HEX.encode(retDat);
        return new BigInteger(versionStr,16).intValue();
    }

    private int getData2(byte[] data, int init, int end){
        byte[] retDat = new byte[end];
        System.arraycopy(data,init,retDat,0,end);
        String versionStr = Hex.HEX.encode(retDat);
        return new BigInteger(versionStr,16).intValue();
    }

    private String getData2Hex(byte[] data, int init, int end){
        byte[] retDat = new byte[end];
        System.arraycopy(data,init,retDat,0,end);
        String versionStr = Hex.HEX.encode(retDat);
        return versionStr;
    }

    private void numericTypeToByteArray(byte[] src,int data,int posStart,int lenght){
        int pos = posStart+lenght-1;
        for (int i = 0; i < lenght; i++) {
            src[pos] = (byte) ((data >> 8 * i) & 0xff);
            pos--;
        }
    }

    private void numericTypeToByteArray(byte[] src,short data,int posStart,int lenght){
        int pos = posStart+lenght-1;
        for (int i = 0; i < lenght; i++) {
            src[pos] = (byte) ((data >> 8 * i) & 0xff);
            pos--;
        }
    }

    private void numericTypeToByteArray(byte[] src,long data,int posStart,int lenght){
        int pos = posStart+lenght-1;
        for (int i = 0; i < lenght; i++) {
            src[pos] = (byte) ((data >> 8 * i) & 0xff);
            pos--;
        }
    }



}
