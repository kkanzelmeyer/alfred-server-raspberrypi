package com.kanzelmeyer.alfred.handlers.state;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.handlers.StateDeviceHandler;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.messages.StateDeviceProtos.StateListMessage;
import com.kanzelmeyer.alfred.handlers.hardware.WebCamera;
import com.kanzelmeyer.alfred.server.Server;

/**
 * Server state handler for a doorbell device
 * @author kevin
 *
 */
public class DoorbellStateHandler implements StateDeviceHandler {

    private static final Logger log = LoggerFactory.getLogger(DoorbellStateHandler.class);
    @Override
    public void onAddDevice(StateDevice device) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onUpdateDevice(StateDevice device) {
        // TODO send message to each server connection
        // TODO take picture
        WebCamera camera = new WebCamera();
        camera.takePicture();
        
        log.info("Device Update: " + device.toString());
        StateDeviceMessage deviceMessage = 
                StateDeviceMessage.newBuilder()
                .setId(device.getId())
                .setState(device.getState())
                .build();
        
        log.info("Sending message");
        log.info(deviceMessage.toString());
        for(Socket socket : Server.getServerConnections()) {
            try {
                deviceMessage.writeTo(socket.getOutputStream());
            } catch (IOException e) {
                log.error("Writing to socket failed", e);
            }
        }
    }

    @Override
    public void onRemoveDevice(StateDevice device) {
        // TODO Auto-generated method stub
        
    }

}
