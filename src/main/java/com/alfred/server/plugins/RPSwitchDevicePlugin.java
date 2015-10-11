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
/**
 * Raspberry Pi Plugin for a switch device. This plugin has four primary
 * components:
 * 
 * <ul>
 * <li><b>Sensor:</b> The sensor is placed so that it can detect if the device
 * is on or off</li>
 * <li><b>Relay:</b> The relay or MOSFET is the switching device controlled by the Pi
 * that switches current to the device</li>
 * <li><b>Network Handler:</b> The network handler handles messages
 * sent to the Alfred server. This handler is responsible updating the state in
 * the device manager and for switching the device on and off based on the state
 * in the message</li>
 * <li><b>State Handler:</b> The state handler is responsible for notifying all
 * clients when the state changes</li>
 * </ul>
 * 
 * @author kanzelmeyer
 *
 */
public class RPSwitchDevicePlugin implements DevicePlugin {

    private int pin;
    private String myDeviceId;
    private GpioPinDigitalInput sensor = null;
    private GpioPinDigitalOutput relay = null;
    private SwitchDeviceNetworkHandler networkHandler = null;
    private SwitchDeviceStateHandler stateHandler = null;
    
    // Logger
    final private static Logger log = LoggerFactory.getLogger(RPDoorbellWebcamPlugin.class);
    
    @Override
    public void activate() {
        // Raspberry Pi pin provisioning
        log.info("Adding plugin for pin " + pin);
        try {
            // Create digital listener for sensor
            GpioController gpio = GpioFactory.getInstance();
            sensor = gpio.provisionDigitalInputPin(PinConverter.ModelB.fromInt(pin), "Sensor",
                    PinPullResistance.PULL_DOWN);
            sensor.addListener(new SwitchSensorHandler());

            // create digital output for the relay switch
            relay = gpio.provisionDigitalOutputPin(PinConverter.ModelB.fromInt(pin),
                    "Relay",
                    PinState.LOW);
        } catch (Exception e) {
            log.error("Exception caught", e);
        }
        
        // Network Handler
        if(networkHandler == null) {
            networkHandler = new SwitchDeviceNetworkHandler();
            StateDeviceManager.addDeviceHandler(stateHandler);
        }
        
        // State Handler
        if(stateHandler == null) {
            stateHandler = new SwitchDeviceStateHandler();
            Server.addNetworkHandler(networkHandler);
        }
    }

    @Override
    public void deactivate() { }

    /**
     * Class to handler network traffic for the device. For a device which
     * can be operated by a wall switch it's important that the message
     * handler be responsible for toggling the device's swith in Alfred. 
     * 
     * @author kanzelmeyer
     *
     */
    private class SwitchDeviceNetworkHandler implements NetworkHandler {

        @Override
        public void onConnect(Socket connection) { }

        @Override
        public void onMessageReceived(StateDeviceMessage msg) {
            // update the state device
            if (msg.getId().equals(myDeviceId)) {
                log.info("Message Received" + msg.toString());
                StateDevice device = new StateDevice(msg);
                StateDeviceManager.updateStateDevice(device);
                
                // Set the device switch based on the message command
                if(msg.getState() == State.ON) {
                    relay.setState(PinState.HIGH);
                } else {
                    relay.setState(PinState.LOW);
                }
            }
        }
    }
    
    /**
     * State handler for a simple state device. In a switch device where the
     * device can be turned on and off with a wall switch, the state handler
     * only needs to send messages to clients to keep their states updated
     * 
     * @author kanzelmeyer
     *
     */
    private class SwitchDeviceStateHandler implements StateDeviceHandler {

        @Override
        public void onAddDevice(StateDevice device) {
            if (device.getId().equals(myDeviceId)) {
                log.info("Device added" + device.toString());
            }
        }

        @Override
        public void onUpdateDevice(StateDevice device) {
            if (device.getId().equals(myDeviceId)) {
                log.info("Switching " + device.getId() + " " + device.getState());
                StateDeviceMessage msg = StateDeviceMessage.newBuilder()
                        .setId(device.getId())
                        .setType(device.getType())
                        .setName(device.getName())
                        .setState(device.getState())
                        .build();
                Server.sendMessage(msg);
            }
        }

        @Override
        public void onRemoveDevice(StateDevice device) { }
    }

    
    /**
     * Sensor handler for a simple state device. The sensor should be able
     * to detect if a device is turned on or off
     * 
     * @author kanzelmeyer
     *
     */
    private class SwitchSensorHandler implements GpioPinListenerDigital {

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            State newState;
            if (event.getState() == PinState.HIGH) {
                newState = State.ON;
            } else {
                newState = State.OFF;
            }

            // update the state device
            StateDeviceManager.updateStateDevice(myDeviceId, newState);
        }
    }
}
