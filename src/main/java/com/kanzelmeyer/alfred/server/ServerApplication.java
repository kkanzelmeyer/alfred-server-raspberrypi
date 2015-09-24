package com.kanzelmeyer.alfred.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.kanzelmeyer.alfred.handlers.hardware.DoorbellHandler;
import com.kanzelmeyer.alfred.plugins.InputPlugin;



public class ServerApplication {
    static Class<ServerApplication> CLASS = ServerApplication.class;
    private static final Logger log = LoggerFactory.getLogger(CLASS);

    public static void main(String[] args) {
        log.info("Starting Alfred Server");
        
        // Create devices, add to device manager
        StateDevice doorbell = 
                new StateDevice.Builder()
                .setId("doorbell1")
                .setName("Front Door")
                .setState(State.INACTIVE)
                .build();
        
        // garage door
        StateDevice garageDoor = 
                new StateDevice.Builder()
                .setId("garagedoor1")
                .setName("Main Garage Door")
                .setState(State.CLOSED)
                .build();
        
        // initialize plugins
        new InputPlugin(13, new DoorbellHandler(doorbell)).activate();
        
        // initialize state handlers
        
        
        // start connection thread
        new Thread(new NewConnectionThread("127.0.0.1", "4321")).start();
    }

}
