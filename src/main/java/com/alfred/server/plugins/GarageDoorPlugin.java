package com.alfred.server.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.handlers.StateDeviceHandler;
import com.alfred.server.utils.PinConverter;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class GarageDoorPlugin implements DevicePlugin{
    
    private int pin;
    private StateDevice device;
    private GpioPinListenerDigital switchHandler = null;
    private GpioPinDigitalOutput button = null;
    private GarageDoorStateHandler stateHandler = null;
    
    // Logger
    final private static Logger log = LoggerFactory.getLogger(DoorbellPlugin.class);

    @Override
    public void activate() {
        // Raspberry Pi interfaces
        log.info("Adding plugin for pin " + pin);
        switchHandler = new GarageDoorSensorHandler(device);
        try {
            // Create digital listener for garage door sensor
            GpioController gpio = GpioFactory.getInstance();
            GpioPinDigitalInput input = gpio.provisionDigitalInputPin(
                        PinConverter.ModelB.fromInt(pin),
                        "Input",
                        PinPullResistance.PULL_DOWN);
            input.addListener(switchHandler);
            
            // create digital output for garage door button
            button = gpio.provisionDigitalOutputPin(
                    PinConverter.ModelB.fromInt(pin),   // PIN NUMBER
                    "Button",           // PIN FRIENDLY NAME (optional)
                    PinState.LOW);      // PIN STARTUP STATE (optional)
        } catch (Exception e) {
            log.error("Exception caught", e);
        }
        
        // State Handler
        if(stateHandler == null) {
            stateHandler = new GarageDoorStateHandler();
        }
        
    }
    
    
    private class GarageDoorSensorHandler implements GpioPinListenerDigital{
        
        private StateDevice device;
        
        public GarageDoorSensorHandler(StateDevice stateDevice) {
            device = stateDevice;
        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
            /*
             * input has a pull down, so it is low when the switch is open
             */
            // input is low, garage door is open
            // input is high, garage door is closed
            
        }
        
    }
    
    private class GarageDoorStateHandler implements StateDeviceHandler {

        @Override
        public void onAddDevice(StateDevice device) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onUpdateDevice(StateDevice device) {
            // if device is set to closing or open, trigger the button
            
        }

        @Override
        public void onRemoveDevice(StateDevice device) {
            // TODO Auto-generated method stub
            
        }
        
    }
}
