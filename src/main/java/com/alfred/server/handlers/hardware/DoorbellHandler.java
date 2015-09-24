package com.alfred.server.handlers.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


/**
 * This handler handles the behavior on the Alfred server for a doorbell detection
 * event. The doorbell device state is set to active when the raspberry pi pin
 * reads high, and remains active until it is set to inactive on a client
 * @author kevin
 *
 */
public class DoorbellHandler implements GpioPinListenerDigital {
    
    private StateDevice _device;
    private static final Logger log = LoggerFactory.getLogger(DoorbellHandler.class);

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
