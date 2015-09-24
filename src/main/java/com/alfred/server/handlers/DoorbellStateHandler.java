package com.alfred.server.handlers;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.handlers.StateDeviceHandler;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Builder;
import com.alfred.server.server.Server;
import com.google.protobuf.ByteString;

/**
 * Server state handler for a doorbell device
 * @author kevin
 *
 */
public class DoorbellStateHandler implements StateDeviceHandler {

    private static final Logger log = LoggerFactory.getLogger(DoorbellStateHandler.class);
    private Builder messageBuilder = StateDeviceMessage.newBuilder();

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
        log.info("Device updated");
        messageBuilder.setId(device.getId())
                      .setState(device.getState());
        sendMessage();
        // if the state is being set to Active, take a picture
        // and let the callback finish sending the message
        
        // TODO uncomment this section 
//        if(device.getState() == State.ACTIVE) {
//            WebCameraThread webCamThread = new WebCameraThread(new TakePictureCallback(device));
//            new Thread(webCamThread).start();
//        } else {
//            // if the state is not being set to active, just send the 
//            // state update message
//            sendMessage();
//        }
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
        if(Server.getServerConnections().size() > 0) {
            for(Socket socket : Server.getServerConnections()) {
                if(socket.isConnected()) {
                    try {
                        log.info("Sending message");
                        log.info("\n" + deviceMessage.toString());
                        deviceMessage.writeTo(socket.getOutputStream());
                    } catch (IOException e) {
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

        private StateDevice device;

        public TakePictureCallback(StateDevice device) {
            this.device = device;
        }

        /**
         * This method adds the image to the message that was started in the parent
         * class onDeviceUpdate method. After the message is built it is sent
         * to each client connected to the server
         */
        @Override
        public void onComplete(File image) {
            log.info("Device Update: " + device.toString());
            // TODO add image to message
            try {
                byte[] imageBytes = FileUtils.readFileToByteArray(image);
                messageBuilder.setData(ByteString.copyFrom(imageBytes));
            } catch (IOException e1) {
                log.error("Unable to read image file" + image.getAbsolutePath(), e1);
            }
            sendMessage();
        }
    }
}
