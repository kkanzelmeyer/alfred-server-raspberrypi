package com.alfred.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;

public class FakeConnection implements Runnable {

    private String _hostAddress = null;
    private String _hostPort = null;
    private Socket _socket = null;
    private static final Logger log = LoggerFactory.getLogger(FakeConnection.class);

    public FakeConnection(String hostAddress, String hostPort) {
        _hostAddress = hostAddress;
        _hostPort = hostPort;
    }

    public Socket getConnection() {
        return _socket;
    }

    @Override
    public void run() {
        try {
            InetAddress host = InetAddress.getByName(_hostAddress);
            _socket = new Socket(host, Integer.valueOf(_hostPort));
            log.info("Connection to the server successful");
            while (true) {
                // get input from the socket
                if(_socket.isConnected()) {
                    StateDeviceMessage msg = StateDeviceMessage.parseDelimitedFrom(_socket.getInputStream());
                    log.info("Message Received from Server");
                    log.info("\n" + msg.toString());
                } else {
                    log.info("Socket not connected");
                    break;
                }
            }
        } catch (UnknownHostException e) {
            log.error("Uknown host", e);
        } catch (NumberFormatException e) {
            log.error("Number format exception", e);
        } catch (IOException e) {
            log.error("Lost connection or unable to connect", e);
        }
    }

    public void shutdown() {
        
    }

}
