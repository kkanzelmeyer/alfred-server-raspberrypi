package com.alfred.server.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;

/**
 * Thread that listens for socket traffic on a given connection.
 * Note - the run method loops and waits on the input stream from
 * a socket. It is expecting a StateDeviceMessage message type 
 * @author kevin
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
            // get incoming message
            InputStream stream;
            StateDeviceMessage msg;
            StateDevice device;
            while (true) {
                try {
                    if (_socket.isConnected()) {
                        stream = _socket.getInputStream();
                        log.info("Raw input : " + IOUtils.toString(stream, "UTF-8"));
                        msg = StateDeviceMessage.parseFrom(stream);
                        log.info("Message Received");
                        log.info(msg.toString());
                        device = new StateDevice(msg);
                        StateDeviceManager.updateStateDevice(device);
                    } else {
                        log.info("Socket not connected");
                        break;
                    }
                } catch (IOException e) {
                    log.info("Lost Client connection : " + _socket.hashCode(), e);
                    Server.removeServerConnection(_socket);
                    break;
                }
            }
        } else {
            log.info("Socket not connected");
        }

    }

}
