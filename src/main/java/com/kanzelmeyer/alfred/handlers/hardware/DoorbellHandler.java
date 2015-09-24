package com.kanzelmeyer.alfred.handlers.hardware;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


public class DoorbellHandler implements GpioPinListenerDigital{
    private StateDevice _device;
    
    public DoorbellHandler(StateDevice device) {
        _device = device;
        System.out.println("Adding GPIO handler for " + device.getName());
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        // TODO Auto-generated method stub
        if (event.getState() == PinState.HIGH) {
            System.out.println(_device.getName() + " event detected : " + event.getState());
            if (_device.getState() != State.ACTIVE) {
                _device.setState(State.ACTIVE);
            } else {
                _device.setState(State.INACTIVE);
            }
        } 
    }
}
