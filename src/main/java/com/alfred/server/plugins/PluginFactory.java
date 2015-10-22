package com.alfred.server.plugins;

import org.json.JSONObject;

import com.alfred.common.messages.StateDeviceProtos.StateDeviceMessage.Type;

/**
 * A simple factory class with a method to find a plugin
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class PluginFactory {
    
    /**
     * A static method for returning a plugin for a given JSON object
     * 
     * @param obj JSON Object representation of a valid state device
     * @return plugin the appropriate plugin for the given device
     */
    public static DevicePlugin getPlugin(JSONObject obj) {
        
        DevicePlugin plugin = null;
        
        // get relevant info from the json object
        int sensorPin = obj.has("sensorpin") ? obj.getInt("sensorpin") : 0;
        int switchPin = obj.has("switchpin") ? obj.getInt("switchpin") : 0;
        String deviceId = obj.getString("id");
        String typeString = obj.getString("type").toUpperCase();
        Type type = Type.valueOf(typeString);
        
        // determine the appropriate plugin for the device
        switch(type) {
            case DOORBELL : 
                if(obj.has("hasWebcam") && obj.getBoolean("hasWebcam")) {
                    plugin = new RPDoorbellPluginWebcam(sensorPin, deviceId);
                } else {
                    plugin = new RPDoorbellPlugin(sensorPin, deviceId);
                }
                break;
            case GARAGEDOOR :
                plugin = new RPGarageDoorPlugin(sensorPin, switchPin, deviceId);
                break;
            default : 
                plugin = new RPSwitchDevicePlugin(sensorPin, switchPin, deviceId);
                break;
        }
        
        return plugin;
    }

}
