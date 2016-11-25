package iop.org.iop_contributors_app.profile_server;

import iop.org.iop_contributors_app.Signer;

/**
 * Created by mati on 22/11/16.
 */

public interface ModuleProfileServer {


    int registerReqeust(Signer signer, String name, byte[] img, int latitude, int longitude, String extraData) throws Exception;

    int updateProfileRequest(Signer signer, byte[] version, String name, byte[] img, int latitude, int longitude, String extraData) throws Exception;

    int updateExtraData(Signer signer, String extraData) throws Exception;

    boolean isIdentityCreated();
}
