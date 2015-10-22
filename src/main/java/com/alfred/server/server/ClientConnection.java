package com.alfred.server.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;

/**
 * This class is a thread that listens for message traffic on a given connection.
 * Each connected client will have it's own instance of this class so that the
 * server can easily communicate will all connected clients.
 * 
 * When a connection is successfully established the connection is registered
 * with the server and all NetworkHandlers are notified of the new connection.
 * 
 * Note - the run method loops and waits on the input stream from a socket. It
 * is expecting a StateDeviceMessage message type. When a message is received it
 * notifies the Server class using the static method "messageReceived"
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class ClientConnection implements Runnable {

    private Socket _socket;
    private static final Logger log = LoggerFactory.getLogger(ClientConnection.class);

    public ClientConnection(Socket socket) {
        _socket = socket;
    }

    @Override
    public void run() {
        
        log.info("Starting thread to listen for incoming client messages");
        if (_socket.isConnected()) {
            Server.addServerConnection(_socket);

            InputStream stream;
            StateDeviceMessage msg;

            while (true) {
                try {
                    if (_socket.isConnected()) {
                        stream = _socket.getInputStream();
                        msg = StateDeviceMessage.parseDelimitedFrom(stream);
                        log.info("Message Received");
                        if(msg == null) {
                            break;
                        }

                        // notify handlers
                        Server.messageReceived(msg);

                    } else {
                        log.info("Socket not connected");
                        break;
                    }
                } catch (IOException e) {
                    log.info("Lost Client connection : " + _socket.hashCode(), e);
                    break;
                }
            }
            Server.removeServerConnection(_socket);
            log.info("Dropping connection. " + Server.getConnectionCount() + " connections remaining.");
        } else {
            log.info("Socket not connected");
        }
    }

}
