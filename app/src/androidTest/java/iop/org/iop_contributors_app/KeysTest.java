package iop.org.iop_contributors_app;

import android.support.test.runner.AndroidJUnit4;

import org.bitcoinj.core.ECKey;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

import iop.org.iop_contributors_app.core.iop_sdk.crypto.KeyEd25519;

/**
 * Created by mati on 09/11/16.
 */
@RunWith(AndroidJUnit4.class)
public class KeysTest {


    @Test
    public void convertPubKeyToHexAndViceversa(){
        ECKey ecKey = new ECKey();
        String pubKeyHex = ecKey.getPublicKeyAsHex();
        byte[] pubKeyBytes = new BigInteger(pubKeyHex,16).toByteArray();;
        assert Arrays.equals(ecKey.getPubKey(),pubKeyBytes);
    }



    @Test
    public void checkSignatureTest() throws Exception {
        // generate the keys
        KeyEd25519 keyEd25519 = KeyEd25519.generateKeys();
        // get the public key of the user to send it
        byte[] publicKey = keyEd25519.getPublicKey();
        // create the message
        String message = "test message";
        // message signature
        byte[] signature = null;
        try {
            signature = keyEd25519.sign(message,keyEd25519.getExpandedPrivateKey());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //check the ownership of the message from the shared publick key
        try {
            assert KeyEd25519.verify(signature,message,publicKey);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
