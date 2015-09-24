package com.alfred.server.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread runs a while loop listening for incoming connections
 * When a new connection is initiated it starts a new thread to handle
 * socket traffic with that connection and adds the socket instance to
 * the list of server connections
 * @author kevin
 *
 */
public class NewConnectionThread implements Runnable {

    private String _hostAddress;
    private String _hostPort;
    private ServerSocket _serverSocket;
    private static final Logger log = LoggerFactory.getLogger(NewConnectionThread.class);

    public NewConnectionThread(String hostAddress, String hostPort) {
        _hostAddress = hostAddress;
        _hostPort = hostPort;
    }

    @Override
    public void run() {
        log.info("Starting thread to listen for incoming client connections");
        try {
            InetAddress host = InetAddress.getByName(_hostAddress);
            _serverSocket = new ServerSocket(Integer.valueOf(_hostPort), 10,
                    host);
            while (true) {
                Socket connection = _serverSocket.accept();
                log.info("New connection received");
                Server.addServerConnection(connection);
                ClientConnection serverConnection = new ClientConnection(
                        connection);
                new Thread(serverConnection).start();
            }
        } catch (UnknownHostException e) {
            log.error("Uknown host exception", e);
        } catch (NumberFormatException e) {
            log.error("Number format exception", e);
        } catch (IOException e) {
            log.error("Socket connection failed", e);
        }

    }

}
