package com.alfred.server.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.network.NetworkHandler;

/**
 * This class is the abstraction of the Alfred server connections. It maintains
 * a list of client connections and a list of connection handlers
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class Server {

    private static List<Socket> serverConnections = new ArrayList<Socket>();
    private static List<NetworkHandler> networkHandlers = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    
    public static List<Socket> getServerConnections() {
        return serverConnections;
    }
    
    /**
     * @param connection
     */
    public static void addServerConnection(Socket connection) {
        serverConnections.add(connection);
        log.info("New Connection added");
        // Notify Connection Handlers
        for(NetworkHandler handler : networkHandlers) {
            handler.onConnect(connection);
        }
    }
    
    
    /**
     * @param connection
     */
    public static void removeServerConnection(Socket connection) {
        serverConnections.remove(connection);
        log.info("Connection removed");
    }
    
    
    public static int getConnectionCount() {
        return serverConnections.size();
    }
    
    // Methods for adding and removing connection handlers
    
    public static void addNetworkHandler(NetworkHandler handler) {
        if(!networkHandlers.contains(handler)) {
            log.info("Adding server connection handler: " + handler.getClass());
            networkHandlers.add(handler);
        }
    }
    
    public static void removeNetworkHandler(NetworkHandler handler) {
        if(networkHandlers.contains(handler)) {
            log.info("Removing server connection handler: " + handler.getClass());
            networkHandlers.remove(handler);
        }
    }
    
    /**
     * Method for receiving a new message
     */
    public static void messageReceived(StateDeviceMessage msg) {
     // Notify Connection Handlers
        for(NetworkHandler handler : networkHandlers) {
            log.debug("Message Received: notifying handler " + handler);
            handler.onMessageReceived(msg);
        }
    }
}


