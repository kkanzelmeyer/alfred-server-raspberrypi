package com.alfred.server.plugins;

import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.handlers.StateDeviceHandler;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.common.network.NetworkHandler;
import com.alfred.server.server.Server;
import com.alfred.server.utils.PinConverter;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


/**
 * Raspberry Pi Doorbell Plugin (no Webcam)
 * <p>
 * <i>If you would like to use a webcam with the doorbell device please use the
 * RPDoorbellWebcamPlugin</i>
 * <p>
 * This class handles the server behavior for a doorbell device. The general
 * behavior is that the sensor near the door (button, motion, etc) can set the
 * device state to "active". When the device is set to active it sends a message
 * to all connected clients. It will also start a reset timer that resets the
 * device in "inactive" after two minutes.
 * <p>
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
 * @author Kevin Kanzelmeyer
 *
 */
public class RPDoorbellPlugin implements DevicePlugin {

    private int pin;
    private String myDeviceId;
    private GpioPinListenerDigital pinHandler = null;
    private DoorbellStateHandler stateHandler = null;
    private DoorbellNetworkHandler networkHandler = null;
    
    final private static Logger log = LoggerFactory.getLogger(RPDoorbellPlugin.class);
    
    /**
     * Constructor order is pin, deviceId.
     * 
     * @param pin
     *            The pin on the Raspberry Pi to which the sensor device is
     *            connected
     * @param deviceId
     *            The ID of the device that is associated with this plugin
     *            instance
     */
    public RPDoorbellPlugin(int pin, String deviceId) {
        this.pin = pin;
        this.myDeviceId = deviceId;
    }

    @Override
    public void activate() {
        // Raspberry pi handler
        log.info("Adding plugin for pin " + pin);
        pinHandler = new DoorbellSensorHandler();
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
    public class DoorbellSensorHandler implements GpioPinListenerDigital {
        
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
         * This method sends a message to connected clients when a state update
         * is received. If the state is being updated to Active a reset timer is
         * started that resets the state to Inactive after two minutes
         * (parameter)
         */
        @Override
        public void onUpdateDevice(StateDevice device) {
            // filter message based on this plugin's device id
            if(device.getId().equals(myDeviceId)) {
                log.info("Device updated" + device.toString());
                
                // if the device is being set to Active start a timer
                // to reset the state after a specified interval
                if (device.getState() == State.ACTIVE) {
                    timer = new Timer();
                    startResetTimer(2, device);
                }

                // build and send message
                StateDeviceMessage msg = 
                        StateDeviceMessage.newBuilder()
                        .setId(device.getId())
                        .setType(Type.DOORBELL)
                        .setName(device.getName())
                        .setState(device.getState())
                        .build();
                Server.sendMessage(msg);
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
         * @param minutes delay in minutes for the reset timer
         * @param device reference to the state device
         */
        public void startResetTimer(int minutes, StateDevice device) {
            resetTask = new DoorbellResetTask(device);
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
         * Class to reset the doorbell state to inactive. This timer task is
         * scheduled by the parent class
         * 
         * @author Kevin Kanzelmeyer
         *
         */
        public class DoorbellResetTask extends TimerTask {

            private StateDevice device = null;

            /**
             * Method to add a state device for the reset task
             * @param stateDevice A valid StateDevice that will be updated by the reset task
             */
            public DoorbellResetTask(StateDevice stateDevice) {
                device = stateDevice;
            }

            /**
             * Method to check if a device has been set
             * 
             * @return True if the device has been set, false otherwise
             */
            public boolean hasDevice() {
                return (device != null);
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
     * Network handler for the doorbell device. This handler's responsibility is to
     * update the state device manager with the new state received in a message
     * 
     * @author Kevin Kanzelmeyer
     *
     */
    public class DoorbellNetworkHandler implements NetworkHandler {

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
