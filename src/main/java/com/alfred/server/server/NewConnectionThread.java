package com.alfred.server.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Thread that listens for new incoming connections
 * @author Admin
 *
 */
public class NewConnectionThread implements Runnable {

    private String _hostAddress;
    private String _hostPort;
    private ServerSocket _serverSocket;

    public NewConnectionThread(String hostAddress, String hostPort) {
        _hostAddress = hostAddress;
        _hostPort = hostPort;
    }

    @Override
    public void run() {
        System.out.println("Starting thread to listen for incoming client connections");
        try {
            InetAddress host = InetAddress.getByName(_hostAddress);
            _serverSocket = new ServerSocket(Integer.valueOf(_hostPort), 10,
                    host);
            while (true) {
                Socket connection = _serverSocket.accept();
                System.out.println("Received a new connection");
                Server.addServerConnection(connection);
                // TODO add connection to server list of connections
                ClientConnection serverConnection = new ClientConnection(
                        connection);
                new Thread(serverConnection).start();
                // listen for new connections
                // create new thread for incoming connections
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
