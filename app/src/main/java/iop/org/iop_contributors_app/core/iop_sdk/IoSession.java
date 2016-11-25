package iop.org.iop_contributors_app.core.iop_sdk;


import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

import iop.org.iop_contributors_app.profile_server.PortType;

/**
 * Created by mati on 03/11/16.
 */

public interface IoSession<M> {

    void write(M message) throws Exception;

    PortType getPortType();

    void closeNow() throws IOException;

    boolean isActive();

    boolean isConnected();

    Socket getChannel();

    boolean isReadSuspended();

    boolean isWriteSuspended();


}
