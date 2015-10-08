package com.alfred.server.plugins;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.handlers.StateDeviceHandler;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.network.NetworkHandler;
import com.alfred.server.server.Server;
import com.alfred.server.utils.PinConverter;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class GarageDoorPlugin implements DevicePlugin {

    private int pin;
    private String myDeviceId;
    private GpioPinListenerDigital sensorHandler = null;
    private GpioPinDigitalOutput button = null;
    private GarageDoorStateHandler stateHandler = null;
    private GarageDoorNetworkHandler networkHandler = null;

    // Logger
    final private static Logger log = LoggerFactory.getLogger(DoorbellPlugin.class);

    @Override
    public void activate() {
        // Raspberry Pi interfaces
        log.info("Adding plugin for pin " + pin);
        sensorHandler = new GarageDoorSensorHandler();
        try {
            // Create digital listener for garage door sensor
            GpioController gpio = GpioFactory.getInstance();
            GpioPinDigitalInput input = gpio.provisionDigitalInputPin(PinConverter.ModelB.fromInt(pin), "Sensor",
                    PinPullResistance.PULL_DOWN);
            input.addListener(sensorHandler);

            // create digital output for garage door button
            button = gpio.provisionDigitalOutputPin(PinConverter.ModelB.fromInt(pin), // PIN
                                                                                      // NUMBER
                    "Button", // PIN FRIENDLY NAME (optional)
                    PinState.LOW); // PIN STARTUP STATE (optional)
        } catch (Exception e) {
            log.error("Exception caught", e);
        }

        // State Handler
        if (stateHandler == null) {
            stateHandler = new GarageDoorStateHandler();
            StateDeviceManager.addDeviceHandler(stateHandler);
        }

        // Network Handler
        if (networkHandler == null) {
            networkHandler = new GarageDoorNetworkHandler();
            Server.addNetworkHandler(networkHandler);
        }
    }

    @Override
    public void deactivate() {
        // TODO Auto-generated method stub

    }

    /**
     * Raspberry Pi hardware handler for the garage door sensor switch
     * 
     * @author kkanzelmeyer
     *
     */
    private class GarageDoorSensorHandler implements GpioPinListenerDigital {

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            // input has a pull down resistor, so it is low when the switch is
            // open
            State newState;
            if (event.getState() == PinState.HIGH) {
                newState = State.CLOSED;
            } else {
                newState = State.OPEN;
            }

            // update the state device
            StateDeviceManager.updateStateDevice(myDeviceId, newState);
        }

    }

    /**
     * State handler for a garage door device
     * 
     * @author kanzelmeyer
     *
     */
    private class GarageDoorStateHandler implements StateDeviceHandler {

        @Override
        public void onAddDevice(StateDevice device) {
            if (device.getId().equals(myDeviceId)) {
                log.info("Device added" + device.toString());
            }
        }

        @Override
        public void onUpdateDevice(StateDevice device) {
            if (device.getId().equals(myDeviceId)) {
                // if device is set to closing or open, trigger the button
                // TODO add "closing" state
                if (device.getState() == State.OPEN) {
                    log.info("Triggering garage door button");
                    button.pulse(200);
                }
                sendMessage(device);
            }
        }

        @Override
        public void onRemoveDevice(StateDevice device) {
        }

        /**
         * Helper method to send a state update message to all connected clients
         * 
         * @param device
         */
        private void sendMessage(StateDevice device) {
            // create a message to send
            StateDeviceMessage msg = StateDeviceMessage.newBuilder()
                    .setId(device.getId())
                    .setType(device.getType())
                    .setName(device.getName())
                    .setState(device.getState())
                    .build();

            // Send message to each client
            if (Server.getConnectionCount() > 0) {
                for (Socket socket : Server.getServerConnections()) {
                    if (socket.isConnected()) {
                        try {
                            log.info("Sending message");
                            msg.writeDelimitedTo(socket.getOutputStream());
                        } catch (Exception e) {
                            Server.removeServerConnection(socket);
                            log.error("Writing to socket failed", e);
                        }
                    }
                }
            }
        }

    }

    private class GarageDoorNetworkHandler implements NetworkHandler {

        @Override
        public void onConnect(Socket connection) {}

        @Override
        public void onMessageReceived(StateDeviceMessage msg) {
            if (msg.getId().equals(myDeviceId)) {
                log.info("Message Received" + msg.toString());
                StateDevice device = new StateDevice(msg);
                StateDeviceManager.updateStateDevice(device);
            }
        }

    }
}
