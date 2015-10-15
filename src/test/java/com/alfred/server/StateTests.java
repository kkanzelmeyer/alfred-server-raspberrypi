package com.alfred.server;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.State;
import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;
import com.alfred.server.server.Server;
import com.alfred.server.utils.Config;

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
    
    /**
     * Email test
     */
//    @Test
//    public void sendEmail() {
//        Server.loadProperties();
//        VisitorEmail email = new VisitorEmail();
//        email.setDate(String.valueOf(System.currentTimeMillis()));
//        email.setSubject("Visitor at the Front Door");
//        email.setImagePath("/home/kevin/Downloads/visitor1444667076.jpg");
//        Server.sendEmail(email);
//    }

//    @Test
//    public void loadDevicesTest() {
//        Config.loadDevices("cfg/devices.json");
//    }
//    
    /**
     * Properties test
     */
    @Test
    public void propertiesTest() {
        Server.loadProperties();
        String[] emails = Server.getProperty(Server.EMAIL_CLIENTS).split(",");
        for(String email : emails) {
            System.out.println(email);
        }
    }

}
