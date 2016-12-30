package org.iop;

import java.math.BigInteger;


/**
 * Created by mati on 09/11/16.
 */

public class CryptoUtil {


    public static byte[] hexStringToByte(String s){
        if (s==null) return null;
        return new BigInteger(s,16).toByteArray();
    }


}
