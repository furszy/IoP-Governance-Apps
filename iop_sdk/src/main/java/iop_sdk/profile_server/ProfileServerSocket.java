package iop_sdk.profile_server;


import com.google.protobuf.InvalidProtocolBufferException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.net.SocketFactory;

import iop_sdk.IoHandler;
import iop_sdk.profile_server.protocol.IopHomeNodeProto3;

/**
 * Created by mati on 08/11/16.
 */

public class ProfileServerSocket implements IoSession<IopHomeNodeProto3.Message>{

    private static final Logger logger = LoggerFactory.getLogger(ProfileServerSocket.class);

    private int port;
    private String host;

    private PortType portType;

    private SocketFactory socketFactory;

    private Socket socket;

    private IoHandler<IopHomeNodeProto3.Message> handler;


    public ProfileServerSocket(SocketFactory socketFactory, String host, int port,PortType portType) throws Exception {
        this.socketFactory = socketFactory;
        if (port<=0) throw new IllegalArgumentException(portType+" port is 0");
        this.port = port;
        this.host = host;
        this.portType = portType;
    }

    public void connect() throws IOException {
        this.socket = socketFactory.createSocket(host,port);
    }

    public void setHandler(IoHandler<IopHomeNodeProto3.Message> handler) {
        this.handler = handler;
    }

    public void write(IopHomeNodeProto3.Message message) throws CantSendMessageException{
        try {
            int messageSize = message.toByteArray().length;
            IopHomeNodeProto3.MessageWithHeader messageWithHeaderBuilder = IopHomeNodeProto3.MessageWithHeader.newBuilder()
                    .setHeader(messageSize+computeProtocolOverhead(messageSize))
                    .setBody(message)
                    .build();
            byte[] messageToSend = messageWithHeaderBuilder.toByteArray();
            logger.info("Message lenght to send: "+messageToSend.length+", Message lenght in the header: "+messageWithHeaderBuilder.getHeader());
            socket.getOutputStream().write(messageToSend);
            socket.getOutputStream().flush();
            handler.messageSent(this,message);
            read();
        }catch (Exception e){
            throw new CantSendMessageException(e);
        }
    }

    private int computeProtocolOverhead(int lenght){
        if (lenght<0) throw new IllegalArgumentException("lenght < 0");
        int overhead = 0;
        if (lenght<=127){
            // 1 byte overhead + 1 byte type
            overhead = 2;
        }else if (lenght<=16383){
            // 2 byte  overhead + 1 byte type
            overhead = 3;
        } else{
            // 3 byte overhead + 1 byte type
            overhead = 4;
        }
        return overhead;
    }

    private void read(){
        int count;
        byte[] buffer = new byte[8192];
        try {
            // read reply
            count = socket.getInputStream().read(buffer);
            logger.info("Reciving data..");
            IopHomeNodeProto3.MessageWithHeader message1 = null;
            ByteBuffer byteBufferToRead = ByteBuffer.allocate(count);
            byteBufferToRead.put(buffer,0,count);
            message1 = IopHomeNodeProto3.MessageWithHeader.parseFrom(byteBufferToRead.array());
            handler.messageReceived(this,message1.getBody());
        } catch (InvalidProtocolBufferException e) {
//                throw new InvalidProtocolViolation("Invalid message",e);
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public PortType getPortType() {
        return portType;
    }

    @Override
    public void closeNow() throws IOException {
        logger.info("Closing socket port: "+portType);
        socket.close();
    }

    @Override
    public boolean isActive() {
        return socket.isConnected();
    }

    @Override
    public boolean isConnected() {
        return socket.isConnected();
    }

    @Override
    public Socket getChannel() {
        return socket;
    }

    @Override
    public boolean isReadSuspended() {
        return false;
    }

    @Override
    public boolean isWriteSuspended() {
        return false;
    }
}