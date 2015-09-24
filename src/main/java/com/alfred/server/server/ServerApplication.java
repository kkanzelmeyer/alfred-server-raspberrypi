package com.alfred.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.server.handlers.DoorbellStateHandler;
import com.alfred.server.plugins.DoorbellPlugin;


public class ServerApplication {
    
    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        log.info("\n-----------------------------------------------------------"
               + "\n             Alfred Home Server"
               + "\n-----------------------------------------------------------"
               + "\n");
        log.info("Starting Alfred Server");
        
        // Create devices
        StateDevice doorbell = 
                new StateDevice.Builder()
                .setId("doorbell1")
                .setName("Front Door")
                .setState(State.INACTIVE)
                .build();
        
        // Add device(s) to device manager
        StateDeviceManager.updateStateDevice(doorbell);
        
        // Add state handlers for devices
        StateDeviceManager.addDeviceHandler(doorbell.getId(), new DoorbellStateHandler());
        
        // initialize hardware plugins
        new DoorbellPlugin(13, doorbell).activate();
        
        
        
        // start connection thread
        new Thread(new NewConnectionThread("127.0.0.1", "4321")).start();
    }

}
