package iop_sdk.profile_server;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import iop_sdk.IoHandler;
import iop_sdk.global.ContextWrapper;
import iop_sdk.profile_server.protocol.IopHomeNodeProto3;


/**
 * Created by mati on 07/11/16.
 */

public class ProfileServerManager {

    private static final String TAG = "ProfileServerManager";

    private SSLContext sslContext;

    private String host;

    private Map<PortType,ProfileServerSocket> serverSockets;
    
    private ContextWrapper context;

    private IoHandler<IopHomeNodeProto3.Message> handler;

    /** seconds */
    private static final long connectionTimeout = 30;

    public ProfileServerManager(ContextWrapper context,String host,SslContextFactory sslContextFactory) throws Exception {
        this.context = context;
        this.host = host;
        serverSockets = new HashMap<>();
        initContext(sslContextFactory);
    }

    public void setHandler(IoHandler<IopHomeNodeProto3.Message> handler) {
        this.handler = handler;
    }

    private void initContext(SslContextFactory sslContextFactory) throws Exception {
        if (sslContextFactory==null) throw new IllegalArgumentException("ssl context factory null");
        this.sslContext = SslContextFactory.buildContext();
    }

    public boolean connectToSecurePort(final PortType portType, final int port) throws CantConnectException {
        boolean isActive = false;
        if (!serverSockets.containsKey(portType)){
            isActive = syncAddServer(portType,port);
        }else {
            isActive = serverSockets.get(portType).isActive();
        }
        return isActive;
    }

    public boolean connectToUnSecurePort(PortType portType,int port) throws CantConnectException {
        boolean isActive = false;
        if (!serverSockets.containsKey(portType)) {
            isActive = syncAddServer(portType,port);
        }else {
            isActive = serverSockets.get(portType).isActive();
        }
        return isActive;
    }

    /**
     * Connect to the server with a timeout
     *
     * @param portType
     * @param port
     * @return
     */
    private boolean syncAddServer(final PortType portType, final int port) throws CantConnectException{
        boolean isActive = false;
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    try {
                        if (portType==PortType.PRIMARY) {
                            addServerSocket(SocketFactory.getDefault(), portType, host, port).connect();
                        }else
                            addServerSocket(sslContext.getSocketFactory(), portType, host, port).connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }
            });
            try {
                isActive = future.get(connectionTimeout, TimeUnit.SECONDS);
            }catch (TimeoutException exception){
                throw new CantConnectException("Timeout exception",exception);
            }
            executorService.shutdownNow();
        }catch (Exception e){
            e.printStackTrace();
        }
        return isActive;
    }

    private ProfileServerSocket addServerSocket(SocketFactory socketFactory,PortType portType,String host,int port) throws Exception {
        ProfileServerSocket profileServerSocket = new ProfileServerSocket(
                socketFactory,
                host,
                port,
                portType
        );
        profileServerSocket.setHandler(handler);
        serverSockets.put(
                portType,
                profileServerSocket

        );
        return profileServerSocket;
    }

    /**
     *  Send a message
     *
     * @param portType
     * @param port
     * @param message
     * @throws Exception
     */
    public void write(PortType portType,int port,IopHomeNodeProto3.Message message) throws CantConnectException,CantSendMessageException{

        try {
            boolean result = connectToPort(portType,port);
            if (!result) throw new Exception("Something happen with the connection");
            ProfileServerSocket profileServerSocket = serverSockets.get(portType);
            profileServerSocket.write(message);

            // Launch message sent event
            handler.messageSent(profileServerSocket,message);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean connectToPort(PortType portType,int port) throws CantConnectException {
        boolean isConnected = false;
        switch (portType){
            case CUSTOMER:
                isConnected = connectToSecurePort(portType,port);
                break;
            case NON_CUSTOMER:
                isConnected = connectToSecurePort(portType,port);
                break;
            case PRIMARY:
                isConnected = connectToUnSecurePort(portType,port);
                break;
        }
        return isConnected;
    }


    public void close(PortType portType) throws IOException {
        this.serverSockets.remove(portType).closeNow();
    }
}
