package com.alfred.server.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;

public class Server {

    private static List<Socket> serverConnections = new ArrayList<Socket>();
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    
    
    public static List<Socket> getServerConnections() {
        return serverConnections;
    }
    
    public static void addServerConnection(Socket connection) {
        serverConnections.add(connection);
        log.info("New Connection added");
        // TODO Send complete data model
        HashMap<String, StateDevice> deviceList = StateDeviceManager.getAllDevices();
        for(int i = 0; i < deviceList.size(); i++) {
            sendDevice();
        }
    }
    
    public static void removeServerConnection(Socket connection) {
        serverConnections.remove(connection);
        log.info("Connection removed");
    }
    
    private static void sendDevice() {
        
    }
}
