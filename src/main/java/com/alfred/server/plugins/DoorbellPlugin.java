package com.alfred.server.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
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

    private int _pin;
    private StateDevice _device;
    private GpioPinListenerDigital _pinHandler = null;
    private GpioController _gpio = null;
    private GpioPinDigitalInput _input = null;
    final private static Logger log = LoggerFactory.getLogger(DoorbellPlugin.class);
    
    public DoorbellPlugin(int pin, StateDevice device) {
        _pin = pin;
        _device = device;
    }

    public void activate() {
        log.info("Adding plugin for pin " + _pin);
        _pinHandler = new DoorbellHandler(_device);
        try {
            _gpio = GpioFactory.getInstance();
            _input = _gpio.provisionDigitalInputPin(
                        PinConverter.ModelB.fromInt(_pin),
                        "Input",
                        PinPullResistance.PULL_DOWN);
            _input.addListener(_pinHandler);
        } catch (Exception e) {
            log.error("Exception caught", e);
        }
    }

public class DoorbellHandler implements GpioPinListenerDigital {
        
        private StateDevice _device;

        public DoorbellHandler(StateDevice device) {
            _device = device;
            System.out.println("Adding GPIO handler for " + device.getName());
        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            if (event.getState() == PinState.HIGH) {
                log.info(_device.getName() + " event detected : " + event.getState());
                StateDevice deviceClone = StateDeviceManager.getDevice(_device.getId());
                if (deviceClone.getState() != State.ACTIVE) {
                    deviceClone.setState(State.ACTIVE);
                } else {
                    // TODO for testing only - remove setting to inactive for product installation
                    deviceClone.setState(State.INACTIVE);
                }
                StateDeviceManager.updateStateDevice(deviceClone);
            } 
        }
    }
}
