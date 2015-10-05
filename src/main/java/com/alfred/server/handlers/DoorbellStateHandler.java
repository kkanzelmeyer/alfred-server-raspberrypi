package com.alfred.server.handlers;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.handlers.StateDeviceHandler;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Builder;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.server.server.Server;
import com.google.protobuf.ByteString;

/**
 * Server state handler for a doorbell device
 * @author kevin
 *
 */
public class DoorbellStateHandler implements StateDeviceHandler {

    private static final Logger log = LoggerFactory.getLogger(DoorbellStateHandler.class);
    private Builder messageBuilder;

    @Override
    public void onAddDevice(StateDevice device) {
        log.info("Device added");
        log.info(device.toString());
    }

    /**
     * This method starts a thread to capture an image with the webcam. It also
     * starts building the message to send clients. The message is completed and 
     * sent in the TakePictureCallback
     */
    @Override
    public void onUpdateDevice(StateDevice device) {
        // Start building message
        log.info("Device updated" + device.toString());
        messageBuilder = StateDeviceMessage.newBuilder();
        messageBuilder.setId(device.getId())
                      .setType(Type.DOORBELL)
                      .setName(device.getName())
                      .setState(device.getState());
        // if the state is being set to Active, take a picture
        // and let the callback finish sending the message
        
        if(device.getState() == State.ACTIVE) {
            WebCameraThread webCamThread = new WebCameraThread(new TakePictureCallback());
            new Thread(webCamThread).start();
        } else {
            // if the state is not being set to active, just send the 
            // state update message
            sendMessage();
        }
    }

    @Override
    public void onRemoveDevice(StateDevice device) {
        log.info("Device removed");
        log.info(device.toString());
    }

    /**
     * Builds the message that is constructed in the message builder, then
     * sends the message to each client connected to the server
     */
    private void sendMessage() {
        // build message
        StateDeviceMessage deviceMessage = messageBuilder.build();

        // Send message to each client
        if(Server.getConnectionCount() > 0) {
            for(Socket socket : Server.getServerConnections()) {
                if(socket.isConnected()) {
                    try {
                        log.info("Sending message");
                        deviceMessage.writeDelimitedTo(socket.getOutputStream());
                    } catch (Exception e) {
                        Server.removeServerConnection(socket);
                        log.error("Writing to socket failed", e);
                    }
                }
            }
        }
    }

    /**
     * This method acts as a callback to the webcam thread. The onComplete
     * method is called by the webcam thread after the picture has been taken.
     * 
     * @author Admin
     *
     */
    private class TakePictureCallback implements WebCamCallback {

        /**
         * This method adds the image to the message that was started in the parent
         * class onDeviceUpdate method. After the message is built it is sent
         * to each client connected to the server
         */
        @Override
        public void onComplete(RenderedImage image) {
            log.info("Finished taking picture. Adding to message");
            // Send the message
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();
                messageBuilder.setData(ByteString.copyFrom(imageBytes));
                sendMessage();
            } catch (IOException e1) {
                log.error("Unable to read image file" + image, e1);
            }

            // Save the image to a file
            try {
                log.info("Saving image file on server");
                String filepath = "/home/pi/Alfred/img/";
                String filename = "visitor" + System.currentTimeMillis() / 1000L + ".jpg";
                File outputfile = new File(filepath + filename);
                ImageIO.write(image, "jpg", outputfile);
                log.info("Finished saving");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
