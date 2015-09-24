package com.alfred.server.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private static List<Socket> serverConnections = new ArrayList<Socket>();
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    
    
    public static List<Socket> getServerConnections() {
        return serverConnections;
    }
    
    public static void addServerConnection(Socket connection) {
        serverConnections.add(connection);
        log.info("New Connection added");
    }
    
    public static void removeServerConnection(Socket connection) {
        serverConnections.remove(connection);
        log.info("Connection removed");
    }
}
