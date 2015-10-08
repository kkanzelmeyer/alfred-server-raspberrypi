package com.alfred.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;

/**
 * Unit test for server app
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
        
        // update the state device
        StateDeviceManager.updateStateDevice(doorbell.getId(), State.ACTIVE);
        
        // test state
        StateDevice doorbellClone = StateDeviceManager.getDevice("doorbell1");
        assertEquals("Doorbell state is active", doorbellClone.getState(), State.ACTIVE);
    }
    

}
