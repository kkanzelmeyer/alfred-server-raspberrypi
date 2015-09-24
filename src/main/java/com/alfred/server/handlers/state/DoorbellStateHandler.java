package com.alfred.server.handlers.state;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.handlers.StateDeviceHandler;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Builder;
import com.alfred.server.handlers.hardware.WebCamCallback;
import com.alfred.server.handlers.hardware.WebCameraThread;
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
        // TODO take picture
        WebCameraThread webCamThread = new WebCameraThread(new TakePictureCallback(device));
        new Thread(webCamThread).start();
        // Start building message
        messageBuilder.setId(device.getId())
                      .setState(device.getState());
    }

    @Override
    public void onRemoveDevice(StateDevice device) {
        log.info("Device removed");
        log.info(device.toString());
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
         * This method finishes building the message that was started in the parent
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
            
            // build message
            StateDeviceMessage deviceMessage = messageBuilder.build();
            
            // Send message to each client
            log.info("Sending message");
            log.info(deviceMessage.toString());
            for(Socket socket : Server.getServerConnections()) {
                try {
                    deviceMessage.writeTo(socket.getOutputStream());
                } catch (IOException e) {
                    log.error("Writing to socket failed", e);
                }
            }
            
        }
        
    }

}
