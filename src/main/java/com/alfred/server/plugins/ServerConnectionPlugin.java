package com.alfred.server.plugins;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.network.NetworkHandler;
import com.alfred.server.server.Server;

public class ServerConnectionPlugin implements DevicePlugin {
    
    private NewConnectionHandler newConnectionHandler = null;

    private static final Logger log = LoggerFactory.getLogger(ServerConnectionPlugin.class);

    public void activate() {
        if(newConnectionHandler == null) {
            log.info("Starting new connection handler");
            newConnectionHandler = new NewConnectionHandler();
            Server.addConnectionHandler(newConnectionHandler);
        }
    }
    

    public void deactivate() {
        if(newConnectionHandler != null) {
            log.info("Removing new connection handler");
            Server.removeConnectionHandler(newConnectionHandler);
            newConnectionHandler = null;
        }
    }

    /**
     * The purpose of this class is to send all state devices to newly connected clients
     * 
     * @author Kevin Kanzelmeyer
     *
     */
    private class NewConnectionHandler implements NetworkHandler {
        
        @Override
        public void onConnect(Socket connection) {
            // Send complete data model
            HashMap<String, StateDevice> deviceList = StateDeviceManager.getAllDevices();
            if (deviceList.size() > 0) {
                for (String id : deviceList.keySet()) {
                    StateDevice device = deviceList.get(id);
                    StateDeviceMessage msg = StateDeviceMessage.newBuilder().setId(device.getId()).setName(device.getName())
                            .setType(device.getType()).setState(device.getState()).build();
                    try {
                        log.info("Sending device");
                        log.info("\n" + msg.toString());
                        msg.writeDelimitedTo(connection.getOutputStream());
                    } catch (IOException e) {
                        log.error("Writing to socket failed", e);
                    }
                }
            } else {
                log.info("No devices found");
            }
        }

        @Override
        public void onMessageReceived(StateDeviceMessage msg) {
//            StateDevice device = new StateDevice(msg);
//            StateDeviceManager.updateStateDevice(device);
        }

    }
}
