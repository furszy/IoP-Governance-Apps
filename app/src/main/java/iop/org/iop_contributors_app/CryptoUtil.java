package iop.org.iop_contributors_app;

import java.math.BigInteger;
import org.apache.commons.codec.binary.Hex;


/**
 * Created by mati on 09/11/16.
 */

public class CryptoUtil {


    public static byte[] hexStringToByte(String s){
        if (s==null) return null;
        return new BigInteger(s,16).toByteArray();
    }


}
