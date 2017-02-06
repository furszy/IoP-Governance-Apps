package iop_sdk.profile_server.client;


import com.google.protobuf.ByteString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import iop_sdk.IoHandler;
import iop_sdk.global.ContextWrapper;
import iop_sdk.profile_server.CantConnectException;
import iop_sdk.profile_server.CantSendMessageException;
import iop_sdk.profile_server.ProfileServerConfigurations;
import iop_sdk.profile_server.Signer;
import iop_sdk.profile_server.SslContextFactory;
import iop_sdk.profile_server.protocol.IopProfileServer;
import iop_sdk.profile_server.protocol.MessageFactory;

/**
 * Created by mati on 08/11/16.
 *
 * This class is in charge of build the messages in a protocol server language.
 *
 */

public class ProfSerImp implements ProfileServer {

    private static final Logger logger = LoggerFactory.getLogger(ProfSerImp.class);

    private ProfSerConnectionManager profSerConnectionManager;

    private ProfileServerConfigurations configurations;


    public ProfSerImp(ContextWrapper context, ProfileServerConfigurations configurations, SslContextFactory sslContextFactory) throws Exception {
        this.configurations = configurations;
        profSerConnectionManager = new ProfSerConnectionManager(configurations.getHost(),sslContextFactory);
    }

    public void connect() throws IOException {
//        profSerConnectionManager.connect();
    }


    @Override
    public int ping(IopProfileServer.ServerRoleType portType) throws CantConnectException,CantSendMessageException {
        IopProfileServer.Message message = MessageFactory.buildPingRequestMessage(
                ByteString.copyFromUtf8("hi").toByteArray(),
                configurations.getProtocolVersion());
        profSerConnectionManager.write(portType,getPort(portType),message);
        return message.getId();
    }

    @Override
    public int listRolesRequest() throws CantConnectException,CantSendMessageException {
        IopProfileServer.Message message = MessageFactory.buildServerListRolesRequestMessage(configurations.getProtocolVersion());
        profSerConnectionManager.write(
                IopProfileServer.ServerRoleType.PRIMARY,
                configurations.getPrimaryPort(),
                message
        );
        return message.getId();
    }

    @Override
    public int homeNodeRequest(byte[] identityPk,String identityType) throws CantConnectException,CantSendMessageException {
        IopProfileServer.Message message = MessageFactory.buildHomeNodeRequestRequest(identityPk,identityType,System.currentTimeMillis(),null);
        profSerConnectionManager.write(
                IopProfileServer.ServerRoleType.CL_NON_CUSTOMER,
                configurations.getNonClPort(),
                message
        );
        return message.getId();
    }

    @Override
    public int startConversationNonCl(byte[] clientPk, byte[] challenge) throws CantConnectException,CantSendMessageException {
        logger.info("startConversationNonCl, clientPK bytes count: "+clientPk.length+", challenge bytes count: "+challenge.length);
        logger.info("clientPK: "+ Arrays.toString(clientPk));
        logger.info("challenge: "+ Arrays.toString(challenge));
        IopProfileServer.Message message = MessageFactory.buildStartConversationRequest(clientPk,challenge,configurations.getProtocolVersion());
        logger.info("startConversationNonCl message id: "+message.getId());
        profSerConnectionManager.write(
                IopProfileServer.ServerRoleType.CL_NON_CUSTOMER,
                configurations.getNonClPort(),
                message
        );
        return message.getId();
    }

    @Override
    public int startConversationNonCl(String clientPk, String challenge) throws CantConnectException,CantSendMessageException {
        return startConversationNonCl(ByteString.copyFromUtf8(clientPk).toByteArray(),ByteString.copyFromUtf8(challenge).toByteArray());

    }

    @Override
    public int startConversationCl(byte[] clientPk, byte[] challenge) throws CantConnectException,CantSendMessageException {
        IopProfileServer.Message message = MessageFactory.buildStartConversationRequest(clientPk,challenge,configurations.getProtocolVersion());
        logger.info("startConversationCl message id: "+message.getId());
        profSerConnectionManager.write(
                IopProfileServer.ServerRoleType.CL_CUSTOMER,
                configurations.getClPort(),
                message
        );
        return message.getId();
    }
    // metodo rápido..
    @Override
    public int startConversationCl(String clientPk, String challenge) throws CantConnectException,CantSendMessageException{
       return startConversationCl(ByteString.copyFromUtf8(clientPk).toByteArray(),ByteString.copyFromUtf8(challenge).toByteArray());
    }

    @Override
    public int checkIn(byte[] nodeChallenge,Signer signer) throws CantConnectException,CantSendMessageException {
        IopProfileServer.Message message = MessageFactory.buildCheckInRequest(nodeChallenge,signer);
        logger.info("checkIn message id: "+message.getId());
        profSerConnectionManager.write(
                IopProfileServer.ServerRoleType.CL_CUSTOMER,
                configurations.getClPort(),
                message
        );
        return message.getId();
    }

    /**
     * todo: ver si hace falta que se firme este mensaje
     *
     * @param version
     * @param name
     * @param img
     * @param latitude
     * @param longitude
     * @param extraData
     * @return
     */
    @Override
    public int updateProfileRequest(Signer signer,byte[] version, String name, byte[] img, int latitude, int longitude, String extraData) throws CantConnectException,CantSendMessageException {
        logger.info("updateProfileRequest, Profile version: "+Arrays.toString(version)+", name: "+name+", extra data: "+extraData);
        IopProfileServer.Message message = MessageFactory.buildUpdateProfileRequest(signer,version,name,img,latitude,longitude,extraData);
        profSerConnectionManager.write(
                IopProfileServer.ServerRoleType.CL_CUSTOMER,
                configurations.getClPort(),
                message
        );
        return message.getId();
    }

    @Override
    public int updateExtraData(Signer signer,String extraData) throws CantConnectException,CantSendMessageException{
        logger.info("UpdateExtraData, extra data: "+extraData);
        IopProfileServer.Message message = MessageFactory.buildUpdateProfileRequest(signer,null,null,null,0,0,extraData);
        profSerConnectionManager.write(
                IopProfileServer.ServerRoleType.CL_CUSTOMER,
                configurations.getClPort(),
                message
        );
        return message.getId();
    }


    @Override
    public void addHandler(IoHandler hanlder) {
        profSerConnectionManager.setHandler(hanlder);
    }

    @Override
    public void closePort(IopProfileServer.ServerRoleType portType) throws IOException {
        profSerConnectionManager.close(portType);
    }

    private int getPort(IopProfileServer.ServerRoleType portType){
        int port = 0;
        switch (portType){
            case CL_CUSTOMER:
                port = configurations.getClPort();
                break;
            case PRIMARY:
                port = configurations.getPrimaryPort();
                break;
            case CL_NON_CUSTOMER:
                port = configurations.getNonClPort();
                break;
        }
        return port;
    }


    /**
     * Ping tast to mantain alive the customer connection.
     */
    private class PingTask implements Runnable{

        IopProfileServer.ServerRoleType portType;

        public PingTask(IopProfileServer.ServerRoleType portType) {
            this.portType = portType;
        }

        @Override
        public void run() {

            try {
                ping(portType);
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("PING FAIL, ver si tengo que reconectar acá..");
            }

        }
    }
}
