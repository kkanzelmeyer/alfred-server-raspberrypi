package com.alfred.server.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static List<Socket> serverConnections = new ArrayList<Socket>();
    
    
    public static List<Socket> getServerConnections() {
        return serverConnections;
    }
    
    public static void addServerConnection(Socket connection) {
        serverConnections.add(connection);
    }
    
    public static void removeServerConnection(Socket connection) {
        serverConnections.remove(connection);
    }
}
