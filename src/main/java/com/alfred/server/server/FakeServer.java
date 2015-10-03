package com.alfred.server.server;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.server.handlers.DoorbellStateHandler;
import com.alfred.server.plugins.DoorbellPlugin;

public class FakeServer {

    public static void main(String[] args) {
        // Create devices
        StateDevice doorbell = new StateDevice.Builder().setId("doorbell1").setName("Front Door").setType(Type.DOORBELL)
                .setState(State.INACTIVE).build();

        // Add state handlers for devices
        StateDeviceManager.addDeviceHandler(doorbell.getId(), new DoorbellStateHandler());

        // Add device(s) to device manager
        StateDeviceManager.updateStateDevice(doorbell);
        
        // Add plugin for Raspberry pi
        new DoorbellPlugin(13, doorbell).activate();

        Thread server = new Thread(new NewConnectionThread("127.0.0.1", "4321"));
        server.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread client = new Thread(new FakeConnection("127.0.0.1", "4321"));
        client.start();

        while(true) {
            
        }
    }

}
