package com.alfred.server.handlers;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;

/**
 * This class handles sending the complete data model to a new client
 * when the client has connected to the server
 * 
 * @author kevin
 *
 */
public class NewConnectionHandler implements ServerConnectionHandler {

    private static final Logger log = LoggerFactory.getLogger(NewConnectionHandler.class);

    @Override
    public void onAddConnection(Socket connection) {
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
                    log.info("Finished writing");
                } catch (IOException e) {
                    log.error("Writing to socket failed", e);
                }
            }
        } else {
            log.info("No devices found");
        }
    }

    @Override
    public void onRemoveConnection(Socket connection) {
        // TODO Auto-generated method stub
        
    }

}
