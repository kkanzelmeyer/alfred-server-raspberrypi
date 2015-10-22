package com.alfred.server.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread runs a "while" loop listening for incoming connections When a new
 * connection is initiated it starts a new ClientConnection thread to handle
 * message traffic with that connection
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class NewConnectionThread implements Runnable {

    private String hostAddress;
    private String hostPort;
    private ServerSocket serverSocket;
    private static final Logger log = LoggerFactory.getLogger(NewConnectionThread.class);

    public NewConnectionThread(String hostAddress, String hostPort) {
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
    }

    @Override
    public void run() {
        
        log.info("Starting thread to listen for incoming client connections");
        
        try {
            InetAddress host = InetAddress.getByName(hostAddress);
            serverSocket = new ServerSocket(Integer.valueOf(hostPort), 10, host);
            
            while (true) {
                Socket connection = serverSocket.accept();
                log.info("New connection received");
                ClientConnection clientConnection = new ClientConnection(
                        connection);
                new Thread(clientConnection).start();
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
