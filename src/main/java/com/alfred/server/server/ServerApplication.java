package com.alfred.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.server.handlers.hardware.DoorbellHandler;
import com.alfred.server.handlers.state.DoorbellStateHandler;
import com.alfred.server.plugins.InputPlugin;



public class ServerApplication {
    static Class<ServerApplication> CLASS = ServerApplication.class;
    private static final Logger log = LoggerFactory.getLogger(CLASS);

    public static void main(String[] args) {
        log.info("Starting Alfred Server");
        
        // Create devices
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
        
        // Add devices to device manager
        StateDeviceManager.updateStateDevice(doorbell);
        StateDeviceManager.updateStateDevice(doorbell);
        
        // Add state handlers for devices
        StateDeviceManager.addDeviceHandler(doorbell.getId(), new DoorbellStateHandler());
        
        // initialize hardware plugins
        new InputPlugin(13, new DoorbellHandler(doorbell)).activate();
        
        
        
        // start connection thread
        new Thread(new NewConnectionThread("127.0.0.1", "4321")).start();
    }

}
