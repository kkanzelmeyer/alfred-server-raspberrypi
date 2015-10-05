package com.alfred.server.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.server.handlers.DoorbellStateHandler;
import com.alfred.server.utils.PinConverter;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


/**
 * This is an input plugin for a device monitored by Alfred. The constructor
 * sets a pin number and a handler for the plugin.
 * 
 * 
 * Inputs are configured to use the raspberry pi's internal pull down resistor 
 * 
 * @author kevin
 *
 */
public class DoorbellPlugin {

    private int pin;
    private StateDevice device;
    private GpioPinListenerDigital pinHandler = null;
    private DoorbellStateHandler stateHandler = null;
    
    final private static Logger log = LoggerFactory.getLogger(DoorbellPlugin.class);
    
    public DoorbellPlugin(int pin, StateDevice device) {
        this.pin = pin;
        this.device = device;
    }

    /**
     * Call this method to activate the plugin
     */
    public void activate() {
        // Raspberry pi handler
        log.info("Adding plugin for pin " + pin);
        pinHandler = new DoorbellHandler(device);
        try {
            GpioController gpio = GpioFactory.getInstance();
            GpioPinDigitalInput input = gpio.provisionDigitalInputPin(
                        PinConverter.ModelB.fromInt(pin),
                        "Input",
                        PinPullResistance.PULL_DOWN);
            input.addListener(pinHandler);
        } catch (Exception e) {
            log.error("Exception caught", e);
        }
        
        // State handler
        if(stateHandler == null) {
            stateHandler = new DoorbellStateHandler();
            StateDeviceManager.addDeviceHandler(device.getId(), stateHandler);
        }
    }

/**
 * This class handles input changes from the Raspberry Pi GPIO pins
 * @author kevin
 *
 */
private class DoorbellHandler implements GpioPinListenerDigital {
        
        private StateDevice _device;

        public DoorbellHandler(StateDevice device) {
            _device = device;
            System.out.println("Adding GPIO handler for " + device.getName());
        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            if (event.getState() == PinState.HIGH) {
                log.info(_device.getName() + " event detected : " + event.getState());
                StateDevice deviceClone = new StateDevice(StateDeviceManager.getDevice(_device.getId()));
                State newState;
                if (deviceClone.getState() != State.ACTIVE) {
                    newState = State.ACTIVE;
                } else {
                    // TODO for testing only - remove setting to inactive for product installation
                    newState = State.INACTIVE;
                }
                deviceClone.setState(newState);
                StateDeviceManager.updateStateDevice(deviceClone);
            } 
        }
    }
}
