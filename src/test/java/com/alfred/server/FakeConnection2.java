package com.alfred.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;

public class FakeConnection2 implements Runnable {

    private String _hostAddress = null;
    private String _hostPort = null;
    private Socket _socket = null;
    private static final Logger log = LoggerFactory.getLogger(FakeConnection2.class);

    public FakeConnection2(String hostAddress, String hostPort) {
        _hostAddress = hostAddress;
        _hostPort = hostPort;
    }

    public Socket getConnection() {
        return _socket;
    }

    @Override
    public void run() {
        try {
            InetAddress host = InetAddress.getByName(_hostAddress);
            _socket = new Socket(host, Integer.valueOf(_hostPort));
            log.info("Connection to the server successful");
            while (true) {
                // get input from the socket
                if(_socket.isConnected()) {
                    StateDeviceMessage msg = StateDeviceMessage.parseDelimitedFrom(_socket.getInputStream());
                    log.info("Message Received from Server");
//                    log.info("\n" + msg.toString());
                    // try to recreate image file from message
                    if(msg.hasData()) {
                        String imagePath = "/home/pi/Alfred/img/";
                        String imageName = System.currentTimeMillis() + "recreated.jpg";
                        File image = new File(imagePath + imageName);
                        try {
                            FileUtils.writeByteArrayToFile(image, msg.getData().toByteArray());
                            log.info("Saving image " + imagePath);
                        } catch (IOException e) {
                            log.error("Can't write to file", e);
                        }
                    }
                } else {
                    log.info("Socket not connected");
                    break;
                }
            }
        } catch (UnknownHostException e) {
            log.error("Uknown host", e);
        } catch (NumberFormatException e) {
            log.error("Number format exception", e);
        } catch (IOException e) {
            log.error("Lost connection or unable to connect", e);
        }
    }

    public void shutdown() {
        
    }

}
