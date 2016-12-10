package iop.org.iop_contributors_app;

import junit.framework.Assert;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.params.RegTestParams;
import org.junit.Test;
import org.libsodium.jni.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

import iop.org.iop_contributors_app.core.iop_sdk.blockchain.OpReturnOutputTransaction;

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
        // start height
        Assert.assertEquals(startHeight,getData(data,4,7));
        // end block
        Assert.assertEquals(endBlock,getData(data,7,9));
        // block reward
        Assert.assertEquals(blockReward,getData(data,9,12));

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
        // start height
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
