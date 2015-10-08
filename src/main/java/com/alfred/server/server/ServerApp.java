package com.alfred.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.server.plugins.DoorbellPlugin;
import com.alfred.server.plugins.ServerConnectionPlugin;

public class ServerApp {

    private static final Logger log = LoggerFactory.getLogger(ServerApp.class);

    public static void main(String[] args) {
        log.info("\n-----------------------------------------------------------"
                + "\n             Alfred Home Server"
                + "\n-----------------------------------------------------------" + "\n");

        log.info("Starting Alfred Server");

        // create new device(s)
        StateDevice doorbell = new StateDevice.Builder()
                .setId("doorbell1")
                .setName("Front Door")
                .setType(Type.DOORBELL)
                .setState(State.INACTIVE).build();

        // Add device(s) to device manager
        StateDeviceManager.addStateDevice(doorbell);

        // Create plugins
        ServerConnectionPlugin serverConnectionPlugin = new ServerConnectionPlugin();
        DoorbellPlugin frontDoorPlugin = new DoorbellPlugin(13, doorbell.getId());
        
        // Activate plugins
        serverConnectionPlugin.activate();
        frontDoorPlugin.activate();

        // start server thread
        Thread server = new Thread(new NewConnectionThread("192.168.1.25", "56"));
        server.start();

        // keep alive
        while (true) {

        }
    }

}
