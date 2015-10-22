package com.alfred.server.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfred.common.datamodel.StateDevice;
import com.alfred.common.datamodel.StateDeviceManager;
import com.alfred.server.plugins.DevicePlugin;
import com.alfred.server.plugins.PluginFactory;
import com.alfred.server.plugins.ServerConnectionPlugin;
import com.alfred.server.server.Server;

/**
 * Configuration utility class that handles configuration initialization
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class Config {
    
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    
    // convenient constants for getting property keys
    public static final String EMAIL_USERNAME = "mail.username";
    public static final String EMAIL_TOKEN    = "mail.token";
    public static final String HOST_ADDRESS   = "alfred.hostaddress";
    public static final String HOST_PORT      = "alfred.hostport";
    public static final String IMAGE_PATH     = "alfred.imagepath";
    public static final String EMAIL_CLIENTS  = "alfred.emailclients";


    /**
     * Method to load properties from the configuration file. Your configuration
     * file should be in {project root}/cfg. The configuration property keys
     * should match the string values in the above server constants. An example
     * and notes can be found in the cfg directory
     * 
     * @param path
     *            the path to the configuration file
     */
    public static void loadProperties(String path) {
        if(Server.getProperties() == null) {
            Properties properties = new Properties();
            try {
                InputStream input = new FileInputStream(path);
                properties.load(input);
                Server.loadProperties(properties);

                // register email clients
                String[] emails = Server.getProperty(EMAIL_CLIENTS).split(",");
                for(String email : emails) {
                    InternetAddress emailAddress = new InternetAddress(email);
                    emailAddress.validate();
                    Server.addEmailClient(emailAddress.getAddress());
                }
            } catch(IOException e) {
                log.error("IO Exception:" , e);
            } catch (AddressException e) {
                log.error("Address Exception:" , e);
            }
        }
    }
    
    
    /**
     * This static method initializes the devices and plugins for Alfred to manage.
     * The devices are expected in JSON format as an array of devices. An
     * example can be found in the cfg directory at the root of the project
     * 
     * @param path
     *            The path to the json configuration file
     */
    public static void initialize(String path) {
        
        JSONArray deviceArray = parseDeviceFile(path);
        
        for(int i = 0; i < deviceArray.length(); i++) {
            JSONObject obj = deviceArray.getJSONObject(i);

            // create a state device from the json object
            JSONObject jsonStateDevice = obj.getJSONObject("statedevice");
            loadDevice(jsonStateDevice);

            // Create and activate plugins
            log.info("Creating plugin for device " + jsonStateDevice.getString("id"));
            DevicePlugin plugin = PluginFactory.getPlugin(obj);
            plugin.activate();
        }

        // additional plugin(s)
        new ServerConnectionPlugin().activate();
    }
    
    
    /**
     * This device loader method creates a StateDevice instance from the input
     * JSON Object and registers the device with the StateDeviceManager
     * 
     * @param obj
     *            JSON representation of a State Device
     */
    public static void loadDevice(JSONObject obj) {
        log.info("\nJSON Creating State Device from JSON:" + obj.toString());
        StateDevice device = null;
        try {
            device = new StateDevice(obj);
        } catch (Exception e) {
            log.error("Couldn't create state device from json object", e);
        }
        
        // add device to the state device manager
        StateDeviceManager.addStateDevice(device);
    }
    
    /**
     * Utility to parse the JSON device file and convert it to a JSON array
     * 
     * @param path The path to the file containing the JSON array
     * @return A JSON array instance of the content contained in the input path file
     */
    public static JSONArray parseDeviceFile(String path) {
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
