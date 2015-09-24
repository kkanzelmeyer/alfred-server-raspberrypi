package com.kanzelmeyer.alfred.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDeviceManager;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


/**
 * This is a generic input plugin for a pin on the raspberry pi. The constructor
 * sets a pin number and a handler for the plugin.
 * 
 * Since the digital input behavior on the hardware is basically the same, independent
 * of what type of hardware is connected, the input plugin should be about the same for
 * each device. The behavior (i.e. what happens when the input changes) should be defined
 * in a handler.
 * 
 * The hardware input is defined to use the raspberry pi's internal pull down resistor 
 * 
 * @author kevin
 *
 */
public class InputPlugin {

    private int _pin;
    private GpioPinListenerDigital _pinHandler = null;
    private GpioController _gpio = null;
    private GpioPinDigitalInput _input = null;
    final private static Logger log = LoggerFactory.getLogger(InputPlugin.class);
    
    public InputPlugin(int pin, GpioPinListenerDigital handler) {
        _pin = pin;
        _pinHandler = handler;
    }

    public void activate() {
        log.info("Adding observer for pin " + _pin);
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
}
