package iop.org.iop_contributors_app;

/**
 * Created by mati on 12/11/16.
 */

public interface Signer {


    byte[] sign(byte[] message);

    boolean verify(byte[] signature,byte[] message);

}
