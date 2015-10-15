package com.alfred.server.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.server.plugins.RPDoorbellPlugin;
import com.alfred.server.plugins.RPDoorbellPluginWebcam;
import com.alfred.server.plugins.RPGarageDoorPlugin;
import com.alfred.server.plugins.RPSwitchDevicePlugin;

public class Config {
    
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    public static void loadDevices(String path) {
        JSONArray deviceArray = parseDeviceFile(path);
        for(int i = 0; i < deviceArray.length(); i++) {
            JSONObject obj = deviceArray.getJSONObject(i);
            log.info("\nJSON Object:" + obj.toString());
                        
            // create a state device from the json object
            JSONObject jsonStateDevice = obj.getJSONObject("statedevice");
            log.info("\nJSON Object:" + jsonStateDevice.toString());
            StateDevice device = null;
            try {
                device = new StateDevice(jsonStateDevice);
            } catch (Exception e) {
                log.error("Couldn't create state device from json object", e);
            }
            log.info("\nDevice:" + device.toString());
            
            // Create and activate plugins
            if(device != null) {
                int sensorPin = obj.has("sensorpin") ? obj.getInt("sensorpin") : 0;
                int switchPin = obj.has("switchpin") ? obj.getInt("switchpin") : 0;
                String deviceId = device.getId();
                switch(device.getType()) {
                    case DOORBELL : 
                        if(obj.has("hasWebcam") && obj.getBoolean("hasWebcam")) {
                            new RPDoorbellPluginWebcam(sensorPin, deviceId).activate();
                        } else {
                            new RPDoorbellPlugin(sensorPin, deviceId).activate();
                        }
                        break;
                    case GARAGEDOOR :
                        new RPGarageDoorPlugin(sensorPin, switchPin, deviceId).activate();
                        break;
                    default : 
                        new RPSwitchDevicePlugin(sensorPin, switchPin, deviceId).activate();
                        break;
                }
            }
            
            // add device to the state device manager
            StateDeviceManager.addStateDevice(device);
        }
    }
    
    /**
     * Utility to parse the JSON device file and convert it to a JSON Array
     * @return
     */
    private static JSONArray parseDeviceFile(String path) {
        JSONArray devices = null;
        try {
            FileInputStream in = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();
            if (sb.toString() != "") {
                devices = new JSONArray(sb.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return devices;
    }
}
