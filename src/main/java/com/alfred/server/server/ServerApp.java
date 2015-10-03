package com.alfred.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.server.handlers.DoorbellStateHandler;
import com.alfred.server.handlers.NewConnectionHandler;
import com.alfred.server.plugins.DoorbellPlugin;

public class ServerApp {

    private static final Logger log = LoggerFactory.getLogger(ServerApp.class);

    public static void main(String[] args) {
        log.info("\n-----------------------------------------------------------"
                + "\n             Alfred Home Server"
                + "\n-----------------------------------------------------------" + "\n");
        
        log.info("Starting Alfred Server");

        // create new device
        StateDevice doorbell = new StateDevice.Builder().setId("doorbell1").setName("Front Door").setType(Type.DOORBELL)
                .setState(State.INACTIVE).build();

        // Add state handlers for device(s)
        StateDeviceManager.addDeviceHandler(doorbell.getId(), new DoorbellStateHandler());

        // Add device(s) to device manager
        StateDeviceManager.updateStateDevice(doorbell);

        // Add plugin for Raspberry pi
        new DoorbellPlugin(13, doorbell).activate();

        // Add handler to send the current state devices to new connections
        Server.addConnectionHandler(new NewConnectionHandler());

        // start server thread
        Thread server = new Thread(new NewConnectionThread("192.168.1.25", "56"));
        server.start();

        // Add a fake client connection
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // Thread client = new Thread(new FakeConnection("127.0.0.1", "4321"));
        // client.start();

        while (true) {

        }
    }

}
