package com.alfred.server.handlers;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.network.NetworkHandler;

/**
 * This class handles sending the complete data model to a new client
 * when the client has connected to the server
 * 
 * @author kevin
 *
 */
public class ServerNetworkHandler implements NetworkHandler {

    private static final Logger log = LoggerFactory.getLogger(ServerNetworkHandler.class);

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
        StateDevice device = new StateDevice(msg);
        StateDeviceManager.updateStateDevice(device);
        
    }

}
