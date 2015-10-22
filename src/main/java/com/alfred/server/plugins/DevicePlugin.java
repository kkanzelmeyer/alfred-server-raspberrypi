package com.alfred.server.plugins;

public interface DevicePlugin {
    
    /**
     * Activate method should handle all the plugin property initializations.
     * The activate method is called by the Config.loadDevices() method
     */
    public void activate();
    
    /**
     * Deactivate should handle resetting the plugin properties
     */
    public void deactivate();

}
