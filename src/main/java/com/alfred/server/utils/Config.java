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
import com.alfred.server.plugins.DevicePlugin;
import com.alfred.server.plugins.PluginFactory;

public class Config {
    
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    /**
     * This static method creates the devices and plugins for Alfred to manage.
     * The devices are expected in JSON format as an array of devices. An
     * example can be found in the cfg directory at the root of the project
     * 
     * @param path
     */
    public static void loadDevices(String path) {
        JSONArray deviceArray = parseDeviceFile(path);
        for(int i = 0; i < deviceArray.length(); i++) {
            JSONObject obj = deviceArray.getJSONObject(i);
                        
            // create a state device from the json object
            JSONObject jsonStateDevice = obj.getJSONObject("statedevice");
            log.info("\nJSON Creating State Device from JSON:" 
                     + jsonStateDevice.toString());
            StateDevice device = null;
            try {
                device = new StateDevice(jsonStateDevice);
            } catch (Exception e) {
                log.error("Couldn't create state device from json object", e);
            }
            
            // add device to the state device manager
            StateDeviceManager.addStateDevice(device);
            
            // Create and activate plugins
            log.info("Creating plugin for device " + device.getId());
            DevicePlugin plugin = PluginFactory.getPlugin(obj);
            plugin.activate();
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
