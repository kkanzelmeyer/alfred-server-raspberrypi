package com.alfred.server.plugins;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.handlers.StateDeviceHandler;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Builder;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.common.network.NetworkHandler;
import com.alfred.server.server.Server;
import com.alfred.server.utils.PinConverter;
import com.google.protobuf.ByteString;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


/**
 * Doorbell Plugin
 * 
 * This class handles the server behavior for a doorbell device. The general behavior is
 * that the sensor near the door (button, motion, etc) can set the device state to "active".
 * When the device is set to active it will send a message to all connected clients, and start
 * a reset timer that resets the device in "inactive" after two minutes. The plugin also listens
 * for incoming messages. If an incoming message sets the device state 
 * 
 * @author kevin
 *
 */
public class DoorbellPlugin implements DevicePlugin {

    private int pin;
    private StateDevice myDevice;
    private GpioPinListenerDigital pinHandler = null;
    private DoorbellStateHandler stateHandler = null;
    private DoorbellNetworkHandler networkHandler = null;
    
    final private static Logger log = LoggerFactory.getLogger(DoorbellPlugin.class);
    
    public DoorbellPlugin(int pin, StateDevice device) {
        this.pin = pin;
        this.myDevice = device;
    }

    /**
     * Call this method to activate the plugin
     */
    @Override
    public void activate() {
        // Raspberry pi handler
        log.info("Adding plugin for pin " + pin);
        pinHandler = new DoorbellSensorHandler(myDevice);
        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalInput input = gpio.provisionDigitalInputPin(
                    PinConverter.ModelB.fromInt(pin),
                    "Input",
                    PinPullResistance.PULL_DOWN);
        input.addListener(pinHandler);
        
        // State handler
        if(stateHandler == null) {
            stateHandler = new DoorbellStateHandler();
            StateDeviceManager.addDeviceHandler(stateHandler);
        }
        
        // Network handler
        if(networkHandler == null) {
            networkHandler = new DoorbellNetworkHandler();
            Server.addConnectionHandler(networkHandler);
        }
        
    }
    
    public void deactivate() {
     // State handler
        if(stateHandler != null) {
            StateDeviceManager.removeDeviceHandler(stateHandler);
            stateHandler = null;
        }
        
        // Network handler
        if(networkHandler != null) {
            Server.removeConnectionHandler(networkHandler);
            networkHandler = null;
        }
    }

    /**
     * This class handles input changes from the Raspberry Pi GPIO pins
     * @author kevin
     *
     */
    private class DoorbellSensorHandler implements GpioPinListenerDigital {
        
        private StateDevice _device;

        public DoorbellSensorHandler(StateDevice device) {
            _device = device;
            System.out.println("Adding GPIO handler for " + device.getName());
        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            if (event.getState() == PinState.HIGH) {
                log.info(_device.getName() + " event detected : " + event.getState());
                StateDevice deviceClone = StateDeviceManager.getDevice(_device.getId());
                State newState;
                if (deviceClone.getState() != State.ACTIVE) {
                    newState = State.ACTIVE;
                } else {
                    // TODO for testing only - remove setting to inactive for product installation
                    newState = State.INACTIVE;
                }
                
                // update the state
                StateDeviceManager.updateStateDevice(_device.getId(), newState);
            } 
        }
    }
    
    
    /**
     * The purpose of this class is to handle state changes to a doorbell device
     * 
     * @author Kevin Kanzelmeyer
     *
     */
    public class DoorbellStateHandler implements StateDeviceHandler {

        private Builder messageBuilder;
        private Timer timer = null;
        private DoorbellResetTask resetTask = null;

        public DoorbellStateHandler() {
            resetTask = new DoorbellResetTask();
        }

        
        @Override
        public void onAddDevice(StateDevice device) {
            // filter message based on this plugin's device id
            if(device.getId() == myDevice.getId()) {
                log.info("Device added");
                log.info(device.toString());
            }
        }

        /**
         * This method starts a thread to capture an image with the webcam. It also
         * starts building the message to send clients. The message is completed and
         * sent in the TakePictureCallback
         */
        @Override
        public void onUpdateDevice(StateDevice device) {
            // filter message based on this plugin's device id
            if(device.getId() == myDevice.getId()) {
                // Start building message
                log.info("Device updated" + device.toString());
                messageBuilder = StateDeviceMessage.newBuilder();
                messageBuilder.setId(device.getId()).setType(Type.DOORBELL).setName(device.getName())
                        .setState(device.getState());
                // if the state is being set to Active, take a picture
                // and let the callback finish sending the message
    
                if (device.getState() == State.ACTIVE) {
                    // Start a thread to take a picture from the webcam
                    WebCameraThread webCamThread = new WebCameraThread(new TakePictureCallback());
                    new Thread(webCamThread).start();
                    
                    // start a reset timer
                    timer = new Timer();
                    startResetTimer(2, device);
                } else {
                    // if the state is not being set to active, just send the
                    // state update message
                    sendMessage();
                    
                }
            }
        }

        @Override
        public void onRemoveDevice(StateDevice device) {
            // filter message based on this plugin's device id
            if(device.getId() == myDevice.getId()) {
                log.info("Device removed");
                log.info(device.toString());
            }
        }

        /**
         * This helper method schedules the reset doorbell task
         * 
         * @param minutes
         */
        public void startResetTimer(int minutes, StateDevice device) {
            resetTask.addDevice(device);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, minutes);
            Date endTime = calendar.getTime();
            log.info("Scheduling reset timer");
            if (!resetTask.hasDevice()) {
                log.error("Error - reset task does not have a registered device");
            } else {
                timer.schedule(resetTask, endTime);
            }
        }

        /**
         * Builds the message that is constructed in the message builder, then sends
         * the message to each client connected to the server
         */
        private void sendMessage() {
            // build message
            StateDeviceMessage deviceMessage = messageBuilder.build();

            // Send message to each client
            if (Server.getConnectionCount() > 0) {
                for (Socket socket : Server.getServerConnections()) {
                    if (socket.isConnected()) {
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
         * This class is a callback to the webcam thread. The onComplete method is
         * called by the webcam thread after the picture has been taken.
         * 
         * @author Kevin Kanzelmeyer
         *
         */
        private class TakePictureCallback implements WebCamCallback {

            /**
             * This method adds the image to the message that was started in the
             * parent class onDeviceUpdate method. After the message is built it is
             * sent to each client connected to the server
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

        /**
         * Class to reset the doorbell state to inactive. This timer task is
         * scheduled by the parent class
         * 
         * @author Kevin Kanzelmeyer
         *
         */
        private class DoorbellResetTask extends TimerTask {

            private StateDevice device = null;

            /**
             * Method to add a state device for the reset task
             * @param stateDevice
             */
            public void addDevice(StateDevice stateDevice) {
                device = stateDevice;
            }

            /**
             * Method to check if a device has been set
             * @return
             */
            public boolean hasDevice() {
                if (device != null)
                    return true;
                else
                    return false;
            }

            /**
             * Task to be executed when the timer scheduler calls it
             */
            @Override
            public void run() {
                log.info("Resetting " + device.getName());
                StateDeviceManager.updateStateDevice(device.getId(), State.INACTIVE);
            }
        }
    }

    
    private class DoorbellNetworkHandler implements NetworkHandler {

        public void onConnect(Socket connection) {
            // Do nothing
        }

        public void onMessageReceived(StateDeviceMessage msg) {
            if(msg != null && msg.getId() == myDevice.getId()) {
                log.info("Doorbell update received: " + msg.toString());
                StateDevice device = new StateDevice(msg);
                StateDeviceManager.updateStateDevice(device);
            }
        }
        
    }
    
}
