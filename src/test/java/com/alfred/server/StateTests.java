package com.alfred.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.server.handlers.DoorbellStateHandler;
import com.alfred.server.server.NewConnectionThread;

/**
 * Unit test for simple App.
 */
public class StateTests{

    /**
     * Test to ensure handlers are functioning properly
     */
    @Test
    public void handlerTest() {
        // Create devices
        StateDevice doorbell = 
                new StateDevice.Builder()
                .setId("doorbell1")
                .setName("Front Door")
                .setType(Type.DOORBELL)
                .setState(State.INACTIVE)
                .build();
        
        // Add device(s) to device manager
        StateDeviceManager.updateStateDevice(doorbell);
        
        // Add state handlers for devices
        StateDeviceManager.addDeviceHandler(new DoorbellStateHandler());
        
        // update the state device
        doorbell.setState(State.ACTIVE);
        StateDeviceManager.updateStateDevice(doorbell);
        
        // test state
        StateDevice doorbellClone = StateDeviceManager.getDevice("doorbell1");
        assertEquals("Doorbell state is active", doorbellClone.getState(), State.ACTIVE);
    }
    

}
