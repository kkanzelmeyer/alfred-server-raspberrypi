package com.kanzelmeyer.alfred.plugins;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


public class InputPlugin {
    // get associated object
    private int _pin;
    private GpioPinListenerDigital _pinHandler = null;
    private GpioController _gpio = null;
    private GpioPinDigitalInput _input = null;
    
    public InputPlugin(int pin, GpioPinListenerDigital handler) {
        _pin = pin;
        _pinHandler = handler;
    }

    public void activate() {
        System.out.println("Adding observer for pin " + _pin);
        try {
            _gpio = GpioFactory.getInstance();
            _input = _gpio.provisionDigitalInputPin(
                        PinConverter.ModelB.fromInt(_pin),
                        "MyButton",
                        PinPullResistance.PULL_DOWN);
            _input.addListener(_pinHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
