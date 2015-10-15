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
import com.alfred.server.email.VisitorEmail;
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
 * Raspberry Pi Doorbell Plugin with Webcam option
 * 
 * This class handles the server behavior for a doorbell device. The general
 * behavior is that the sensor near the door (button, motion, etc) can set the
 * device state to "active". When the device is set to active it requests a
 * picture from the connected webcam and sends a message to all connected
 * clients. It will also start a reset timer that resets the device in
 * "inactive" after two minutes.
 * 
 * The plugin has three primary components:
 * 
 * <ul>
 * <li><b>Sensor:</b> This is the hardware that notifies the system when a
 * visitor is present. It can be a button, motion sensor, proximity sensor, etc.
 * </li>
 * <li><b>State Handler:</b> The state handler is responsible for getting a
 * picture from the webcam when the device is set to Active, and for sending
 * state changes to all connected clients</li>
 * <li><b>Network Handler:</b> The network handler receives messages and updates
 * the state accordingly</li>
 * </ul>
 * 
 * @author kevin
 *
 */
public class RPDoorbellPluginWebcam implements DevicePlugin {

    private int pin;
    private String myDeviceId;
    private GpioPinDigitalInput sensor = null;
    private DoorbellStateHandler stateHandler = null;
    private DoorbellNetworkHandler networkHandler = null;
    
    final private static Logger log = LoggerFactory.getLogger(RPDoorbellPluginWebcam.class);
    
    public RPDoorbellPluginWebcam(int pin, String deviceId) {
        this.pin = pin;
        this.myDeviceId = deviceId;
    }

    /**
     * Call this method to activate the plugin
     */
    @Override
    public void activate() {
        // Raspberry pi handler
        log.info("Adding plugin for pin " + pin);
        GpioController gpio = GpioFactory.getInstance();
        sensor = gpio.provisionDigitalInputPin(
                    PinConverter.ModelB.fromInt(pin),
                    "Input",
                    PinPullResistance.PULL_DOWN);
        sensor.addListener(new DoorbellSensorHandler());
        
        // State handler
        if(stateHandler == null) {
            stateHandler = new DoorbellStateHandler();
            StateDeviceManager.addDeviceHandler(stateHandler);
        }
        
        // Network handler
        if(networkHandler == null) {
            networkHandler = new DoorbellNetworkHandler();
            Server.addNetworkHandler(networkHandler);
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
            Server.removeNetworkHandler(networkHandler);
            networkHandler = null;
        }
    }

    /**
     * This class handles input changes from the Raspberry Pi GPIO pins
     * @author kevin
     *
     */
    private class DoorbellSensorHandler implements GpioPinListenerDigital {
        
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            if (event.getState() == PinState.HIGH) {
                log.info(myDeviceId + " event detected : " + event.getState());
                StateDevice deviceClone = StateDeviceManager.getDevice(myDeviceId);
                State newState;
                if (deviceClone.getState() != State.ACTIVE) {
                    newState = State.ACTIVE;
                } else {
                    // TODO for testing only - remove setting to inactive for product installation
                    newState = State.INACTIVE;
                }
                
                // update the state
                StateDeviceManager.updateStateDevice(myDeviceId, newState);
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

        
        @Override
        public void onAddDevice(StateDevice device) {
            // filter message based on this plugin's device id
            if(device.getId().equals(myDeviceId)) {
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
            if(device.getId().equals(myDeviceId)) {
                // Start building message
                log.info("Device updated" + device.toString());
                log.info("Updated device on thread " + Thread.currentThread().getId());
                messageBuilder = StateDeviceMessage.newBuilder();
                messageBuilder.setId(device.getId())
                              .setType(Type.DOORBELL)
                              .setName(device.getName())
                              .setState(device
                              .getState());
                
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
                    StateDeviceMessage msg = messageBuilder.build();
                    Server.sendMessage(msg);
                    
                }
            }
        }

        @Override
        public void onRemoveDevice(StateDevice device) {
            // filter message based on this plugin's device id
            if(device.getId().equals(myDeviceId)) {
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
            resetTask = new DoorbellResetTask();
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
         * This class is a callback to the webcam thread. The onComplete method is
         * called by the webcam thread after the picture has been taken.
         * 
         * @author Kevin Kanzelmeyer
         *
         */
        private class TakePictureCallback implements WebCamCallback {

            /**
             * This method adds the image to the message that was started in the
             * parent class onDeviceUpdate method. After the message is built it
             * is sent to each client, then the image is saved as a file, then
             * an email is sent to email clients
             */
            @Override
            public void onComplete(RenderedImage image) {
                log.info("Finished taking picture. Adding to message");
                // Send the message
                StateDeviceMessage msg = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ImageIO.write(image, "jpg", baos);
                    byte[] imageBytes = baos.toByteArray();
                    messageBuilder.setData(ByteString.copyFrom(imageBytes));
                    msg = messageBuilder.build();
                    Server.sendMessage(msg);
                } catch (IOException e1) {
                    log.error("Unable to read image file" + image, e1);
                }

                // Save the image to a file
                log.info("Saving image file on server");
                String filepath = Server.getProperty(Server.IMAGE_PATH);
                String date = String.valueOf(System.currentTimeMillis());
                String filename = "visitor" + date + ".jpg";
                try {
                    File outputfile = new File(filepath + filename);
                    ImageIO.write(image, "jpg", outputfile);
                    log.info("Finished saving");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                // send email
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                log.info("Sending email to email clients");
                log.info("Creating email on thread " + Thread.currentThread().getId());
                VisitorEmail email = new VisitorEmail();
                email.setDate(date);
                email.setImagePath(filepath + filename);
                email.setSubject("Visitor at the " + msg.getName());
                Server.sendEmail(email);
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

    /**
     * The doorbell network handler is responsible for updating the state device
     * manager with the new state received from the message
     * 
     * @author kanzelmeyer
     *
     */
    private class DoorbellNetworkHandler implements NetworkHandler {

        @Override
        public void onConnect(Socket connection) { }

        @Override
        public void onMessageReceived(StateDeviceMessage msg) {
            if(msg != null && msg.getId().equals(myDeviceId)) {
                log.info("Doorbell update received: " + msg.toString());
                StateDevice device = new StateDevice(msg);
                StateDeviceManager.updateStateDevice(device);
            }
        }
    }
}
