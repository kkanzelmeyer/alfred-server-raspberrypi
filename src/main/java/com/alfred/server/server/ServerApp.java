package com.alfred.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.server.handlers.ServerNetworkHandler;
import com.alfred.server.plugins.DoorbellPlugin;

public class ServerApp {

    private static final Logger log = LoggerFactory.getLogger(ServerApp.class);

    public static void main(String[] args) {
        log.info("\n-----------------------------------------------------------"
                + "\n             Alfred Home Server"
                + "\n-----------------------------------------------------------" + "\n");
        
        log.info("Starting Alfred Server");

        // create new device
        StateDevice doorbell = new StateDevice.Builder()
                .setId("doorbell1")
                .setName("Front Door")
                .setType(Type.DOORBELL)
                .setState(State.INACTIVE).build();

        // Add device(s) to device manager
        StateDeviceManager.addStateDevice(doorbell);

        // Add and activate device plugins
        new DoorbellPlugin(13, doorbell).activate();

        // Add network handler for server traffic
        Server.addConnectionHandler(new ServerNetworkHandler());

        // start server thread
        Thread server = new Thread(new NewConnectionThread("192.168.1.25", "56"));
        server.start();

        // keep alive
        while (true) {

        }
    }

}
