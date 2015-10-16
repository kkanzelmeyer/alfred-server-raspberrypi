package com.alfred.server.server;

import com.alfred.server.utils.Config;

public class ServerApp {

    public static void main(String[] args) {
        
        /* -------------------------------------------------------------------
         *  DISPLAY STARTUP MESSAGE
         * -------------------------------------------------------------------*/
        Server.printGreeting();

        /* -------------------------------------------------------------------
         *  LOAD CONFIGURATIONS
         * -------------------------------------------------------------------*/
        Server.loadProperties("cfg/config.properties");
        Config.loadDevices("cfg/devices.json");

//        /* -------------------------------------------------------------------
//         *  CREATE DEVICES
//         * -------------------------------------------------------------------*/
//        // create new device(s)
//        // TODO move this to server loadproperties method
//        StateDevice doorbell = new StateDevice.Builder()
//                .setId("doorbell1")
//                .setName("Front Door")
//                .setType(Type.DOORBELL)
//                .setState(State.INACTIVE)
//                .build();
//        // Add device(s) to device manager
//        StateDeviceManager.addStateDevice(doorbell);
//
//        /* -------------------------------------------------------------------
//         *  ACTIVATE PLUGINS
//         * -------------------------------------------------------------------*/
//        new ServerConnectionPlugin().activate();
//        new RPDoorbellPluginWebcam(13, doorbell.getId()).activate();

        /* -------------------------------------------------------------------
         *  START SERVER
         * -------------------------------------------------------------------*/
        Thread server = 
                new Thread(new NewConnectionThread(Server.getProperty(Server.HOST_ADDRESS),
                                                   Server.getProperty(Server.HOST_PORT)));
        server.start();

        // keep alive
        while (true) {

        }
    }

}
