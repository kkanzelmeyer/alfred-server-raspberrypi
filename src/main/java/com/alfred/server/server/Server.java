package com.alfred.server.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.server.handlers.ServerConnectionHandler;

/**
 * This class is the abstraction of the Alfred server connections. It maintains
 * a list of client connections and a list of connection handlers
 * 
 * @author kevin
 *
 */
public class Server {

    private static List<Socket> serverConnections = new ArrayList<Socket>();
    private static List<ServerConnectionHandler> connectionHandlers = new ArrayList<ServerConnectionHandler>();
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    
    public static List<Socket> getServerConnections() {
        return serverConnections;
    }
    
    public static void addServerConnection(Socket connection) {
        serverConnections.add(connection);
        log.info("New Connection added");
        // Notify Connection Handlers
        for(ServerConnectionHandler handler : connectionHandlers) {
            handler.onAddConnection(connection);
        }
    }
    
    public static void removeServerConnection(Socket connection) {
        serverConnections.remove(connection);
        log.info("Connection removed");
    }
    
    // Methods for adding and removing connection handlers
    
    public static void addConnectionHandler(ServerConnectionHandler handler) {
        if(!connectionHandlers.contains(handler)) {
            log.info("Adding server connection handler: " + handler.getClass());
            connectionHandlers.add(handler);
        }
    }
    
    public static void removeConnectionHandler(ServerConnectionHandler handler) {
        if(connectionHandlers.contains(handler)) {
            log.info("Removing server connection handler: " + handler.getClass());
            connectionHandlers.remove(handler);
        }
    }
}


