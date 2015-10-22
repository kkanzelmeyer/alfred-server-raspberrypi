package com.alfred.server;

import com.alfred.server.server.NewConnectionThread;
import com.alfred.server.server.Server;
import com.alfred.server.utils.Config;

/**
 * Server Application main class. This class will start the Alfred Server application
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class ServerApp {

    /**
     * Main method that loads the configuration files and starts the main server thread
     * 
     * @param args none
     */
    public static void main(String[] args) {
        
        /* -------------------------------------------------------------------
         *  DISPLAY STARTUP MESSAGE
         * -------------------------------------------------------------------*/
        Server.printGreeting();

        /* -------------------------------------------------------------------
         *  LOAD CONFIGURATIONS
         * -------------------------------------------------------------------*/
        Config.loadProperties("cfg/config.properties");
        Config.initialize("cfg/devices.json");

        /* -------------------------------------------------------------------
         *  START SERVER
         * -------------------------------------------------------------------*/
        Thread server = 
                new Thread(new NewConnectionThread(Server.getProperty(Config.HOST_ADDRESS),
                                                   Server.getProperty(Config.HOST_PORT)));
        server.start();

        // keep alive
        while (true) {

        }
    }

}
