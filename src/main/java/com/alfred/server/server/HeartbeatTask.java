package com.kanzelmeyer.alfred.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.TimerTask;

public class HeartbeatTask extends TimerTask {

    @Override
    public void run() {
        List<Socket> connections = Server.getServerConnections();
        if (connections != null && connections.size() > 0) {
            // HeartbeatMessage msg = new HeartBeatMessage();
            for (Socket socket : connections) {
                if (socket != null) {
                    String message = "thump THUMP!";
                    OutputStream outToServer;
                    try {
                        outToServer = socket.getOutputStream();
                        DataOutputStream out = new DataOutputStream(outToServer);
                        out.writeUTF(message);
                        System.out.println("Sent message: " + message);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}
