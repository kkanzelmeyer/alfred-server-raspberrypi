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

public class RPSwitchDevicePlugin implements DevicePlugin {

    private int pin;
    private String myDeviceId;
    private GpioPinDigitalInput sensor = null;
    private GpioPinDigitalOutput button = null;
    private SwitchDeviceNetworkHandler networkHandler = null;
    private SwitchDeviceStateHandler stateHandler = null;
    
    // Logger
    final private static Logger log = LoggerFactory.getLogger(RPDoorbellPlugin.class);
    
    @Override
    public void activate() {
        // Raspberry Pi pin provisioning
        log.info("Adding plugin for pin " + pin);
        try {
            // Create digital listener for garage door sensor
            GpioController gpio = GpioFactory.getInstance();
            sensor = gpio.provisionDigitalInputPin(PinConverter.ModelB.fromInt(pin), "Sensor",
                    PinPullResistance.PULL_DOWN);
            sensor.addListener(new SwitchSensorHandler());

            // create digital output for garage door button
            button = gpio.provisionDigitalOutputPin(PinConverter.ModelB.fromInt(pin),
                    "Button",
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
    public void deactivate() {
        // TODO Auto-generated method stub

    }

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
                    button.setState(PinState.HIGH);
                } else {
                    button.setState(PinState.LOW);
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
        public void onRemoveDevice(StateDevice device) {
            // TODO Auto-generated method stub
            
        }
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
