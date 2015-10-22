package com.alfred.server.utils;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Simple Pin conversion class that converts between pin numbers on the Pi 
 * to pin numbering convention used in the Pi4J library
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class PinConverter {
    
    /**
     * Raspberry Pi Model B and B+ pin converstion tool
     * 
     * @author Kevin Kanzelmeyer
     *
     */
    public static class ModelB {
        public static Pin fromInt(Integer pin) {
            switch(pin) {
            case 3:  return (Pin) RaspiPin.GPIO_08;
            case 12: return (Pin) RaspiPin.GPIO_01;
            case 13: return (Pin) RaspiPin.GPIO_02;
            default: return null;
            }
        }
    }
}
