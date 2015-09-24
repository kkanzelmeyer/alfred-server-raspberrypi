package com.alfred.server.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


/**
 * Thread that listens for socket traffic on a given connection
 * @author Admin
 *
 */
public class ClientConnection implements Runnable {

    private Socket _socket;

    public ClientConnection(Socket socket) {
        _socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Starting thread to listen for incoming client messages");
        if (_socket.isConnected()) {
            // get incoming message
            InputStream stream;
            while (true) {
                try {
                    if (_socket.isConnected()) {
                        stream = _socket.getInputStream();
                        byte[] data = new byte[100];
                        int count = stream.read(data);
                        System.out.println("Message Received: "
                                + new String(data, 1, count));
                        // convert byte array to message
                        // handle message
                    }
                } catch (IOException e) {
                    System.out.println("Lost Client connection : " + _socket.hashCode());
                    Server.removeServerConnection(_socket);
                    break;
                }
            }
        }

    }

}
