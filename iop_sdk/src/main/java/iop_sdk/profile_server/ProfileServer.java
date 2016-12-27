package iop_sdk.profile_server;

import java.io.IOException;

import iop_sdk.IoHandler;


/**
 * Created by mati on 08/11/16.
 */

//
// Home Node Request and Check-in - Identity registration and first login full sequence
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
// A) Provided that the node's clNonCustomer port is different from clCustomer port, the sequence is as follows:
//   1) Identity connects to the node on its primary port and gets information about its roles to ports mapping.
//   2) Identity connects to the clNonCustomer port and sends a home node request.
//   3) Identity connects to the clCustomer port and performs a check-in.
//
// B) If clNonCustomer port is equal to clCustomer port, the sequence is as follows:
//   1) Identity connects to the node on its primary port and gets information about its roles to ports mapping.
//   2) Identity connects to the clNonCustomer+clCustomer port and sends a home node request.
// 3) Identity performs a check-in over the connection from 2).

public interface ProfileServer {


    int ping(PortType portType) throws Exception;

    int listRolesRequest() throws Exception;

    int homeNodeRequest(byte[] identityPk, String identityType) throws Exception;

    int startConversationNonCl(byte[] clientPk, byte[] challenge) throws Exception;
    int startConversationNonCl(String clientPk, String challenge) throws Exception;

    int startConversationCl(byte[] clientPk, byte[] challenge) throws Exception;
    int startConversationCl(String clientPk, String challenge) throws Exception;

    int checkIn(byte[] signedChallenge, Signer signer) throws Exception;

    int updateProfileRequest(Signer signer, byte[] version, String name, byte[] img, int latitude, int longitude, String extraData) throws Exception;

    int updateExtraData(Signer signer, String extraData) throws Exception;

    void addHandler(IoHandler hanlder);

    void closePort(PortType portType) throws IOException;

}
