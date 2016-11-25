package iop.org.iop_contributors_app.profile_server;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.Arrays;

import iop.org.iop_contributors_app.configurations.ProfileServerConfigurations;
import iop.org.iop_contributors_app.Signer;
import iop.org.iop_contributors_app.core.iop_sdk.IoHandler;
import iop.org.iop_contributors_app.profile_server.protocol.IopHomeNodeProto3;
import iop.org.iop_contributors_app.profile_server.protocol.MessageFactory;

/**
 * Created by mati on 08/11/16.
 */

public class ProfileServerImp implements ProfileServer {

    private static final String TAG = "ProfileServerImp";

    private ProfileServerManager profileServerManager;

    private ProfileServerConfigurations configurations;


    public ProfileServerImp(Context context, ProfileServerConfigurations configurations) throws Exception {
        this.configurations = configurations;
        profileServerManager = new ProfileServerManager(context,configurations.getHost());
    }

    public void connect() throws IOException {
//        profileServerManager.connect();
    }


    @Override
    public int ping(PortType portType) throws CantConnectException,CantSendMessageException {
        IopHomeNodeProto3.Message message = MessageFactory.buildPingRequestMessage(
                ByteString.copyFromUtf8("hi").toByteArray(),
                configurations.getProtocolVersion());
        profileServerManager.write(portType,getPort(portType),message);
        return message.getId();
    }

    @Override
    public int listRolesRequest() throws CantConnectException,CantSendMessageException {
        IopHomeNodeProto3.Message message = MessageFactory.buildServerListRolesRequestMessage(configurations.getProtocolVersion());
        profileServerManager.write(
                PortType.PRIMARY,
                configurations.getPrimaryPort(),
                message
        );
        return message.getId();
    }

    @Override
    public int homeNodeRequest(byte[] identityPk,String identityType) throws CantConnectException,CantSendMessageException {
        IopHomeNodeProto3.Message message = MessageFactory.buildHomeNodeRequestRequest(identityPk,identityType,System.currentTimeMillis(),null);
        profileServerManager.write(
                PortType.NON_CUSTOMER,
                configurations.getNonClPort(),
                message
        );
        return message.getId();
    }

    @Override
    public int startConversationNonCl(byte[] clientPk, byte[] challenge) throws CantConnectException,CantSendMessageException {
        Log.d(TAG,"startConversationNonCl, clientPK bytes count: "+clientPk.length+", challenge bytes count: "+challenge.length);
        Log.d(TAG,"clientPK: "+ Arrays.toString(clientPk));
        Log.d(TAG,"challenge: "+ Arrays.toString(challenge));
        IopHomeNodeProto3.Message message = MessageFactory.buildStartConversationRequest(clientPk,challenge,configurations.getProtocolVersion());
        Log.d(TAG,"startConversationNonCl message id: "+message.getId());
        profileServerManager.write(
                PortType.NON_CUSTOMER,
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
        IopHomeNodeProto3.Message message = MessageFactory.buildStartConversationRequest(clientPk,challenge,configurations.getProtocolVersion());
        Log.d(TAG,"startConversationCl message id: "+message.getId());
        profileServerManager.write(
                PortType.CUSTOMER,
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
        IopHomeNodeProto3.Message message = MessageFactory.buildCheckInRequest(nodeChallenge,signer);
        Log.d(TAG,"checkIn message id: "+message.getId());
        profileServerManager.write(
                PortType.CUSTOMER,
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
        Log.d(TAG,"updateProfileRequest, Profile version: "+Arrays.toString(version)+", name: "+name+", extra data: "+extraData);
        IopHomeNodeProto3.Message message = MessageFactory.buildUpdateProfileRequest(signer,version,name,img,latitude,longitude,extraData);
        profileServerManager.write(
                PortType.CUSTOMER,
                configurations.getClPort(),
                message
        );
        return message.getId();
    }

    @Override
    public int updateExtraData(Signer signer,String extraData) throws CantConnectException,CantSendMessageException{
        Log.d(TAG,"UpdateExtraData, extra data: "+extraData);
        IopHomeNodeProto3.Message message = MessageFactory.buildUpdateProfileRequest(signer,null,null,null,0,0,extraData);
        profileServerManager.write(
                PortType.CUSTOMER,
                configurations.getClPort(),
                message
        );
        return message.getId();
    }


    @Override
    public void addHandler(IoHandler hanlder) {
        profileServerManager.setHandler(hanlder);
    }

    @Override
    public void closePort(PortType portType) throws IOException {
        profileServerManager.close(portType);
    }

    private int getPort(PortType portType){
        int port = 0;
        switch (portType){
            case CUSTOMER:
                port = configurations.getClPort();
                break;
            case PRIMARY:
                port = configurations.getPrimaryPort();
                break;
            case NON_CUSTOMER:
                port = configurations.getNonClPort();
                break;
        }
        return port;
    }


    /**
     * Ping tast to mantain alive the customer connection.
     */
    private class PingTask implements Runnable{

        PortType portType;

        public PingTask(PortType portType) {
            this.portType = portType;
        }

        @Override
        public void run() {

            try {
                ping(portType);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"PING FAIL, ver si tengo que reconectar acá..");
            }

        }
    }
}
