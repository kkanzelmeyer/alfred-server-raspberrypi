package com.alfred.server.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;

public class Server {

    private static List<Socket> serverConnections = new ArrayList<Socket>();
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    
    
    public static List<Socket> getServerConnections() {
        return serverConnections;
    }
    
    public static void addServerConnection(Socket connection) {
        serverConnections.add(connection);
        log.info("New Connection added");
        // Send complete data model
        HashMap<String, StateDevice> deviceList = StateDeviceManager.getAllDevices();
        if(deviceList.size() > 0) {
            for(String id : deviceList.keySet()) {
                StateDevice device = deviceList.get(id);
                sendDevice(device);
            }
        } else {
            log.info("No devices found");
        }
    }
    
    public static void removeServerConnection(Socket connection) {
        serverConnections.remove(connection);
        log.info("Connection removed");
    }
    
    private static void sendDevice(StateDevice device) {
        if(serverConnections.size() > 0) {
            for(Socket connection : serverConnections) {
                StateDeviceMessage msg = StateDeviceMessage.newBuilder()
                        .setId(device.getId())
                        .setName(device.getName())
                        .setType(device.getType())
                        .setState(device.getState())
                        .build();
                try {
                    log.info("Sending device");
                    log.info("\n" + msg.toString());
                    msg.writeDelimitedTo(connection.getOutputStream());
                    log.info("Finished writing");
                } catch (IOException e) {
                    log.error("Writing to socket failed", e);
                }
            }
        }
    }
}
