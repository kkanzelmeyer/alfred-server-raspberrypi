package com.alfred.server.plugins;

import java.awt.Dimension;
import java.awt.image.RenderedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;

/**
 * Thread to handle taking a picture with a Webcam connected to the Pi. When the
 * webcam is finished capturing an image it calls the onComplete method for the
 * registered WebCamCallback with a reference to the captured image
 * 
 * @author Kevin Kanzelmeyer
 *
 */
public class WebCameraThread implements Runnable {
    
    private RenderedImage image;
    private WebCamCallback _handler;
    
    public RenderedImage getImage() {
        return image;
    }
    
    /**
     * Constructor takes a WebcamCallback as a handler so that the callback can
     * be notified when an image has been captured
     * 
     * @param handler
     *            A WebCamCallback handler
     */
    public WebCameraThread(WebCamCallback handler) {
        _handler = handler;
        Webcam.setDriver(new V4l4jDriver()); // this is important
    }

    @Override
    public void run() {
        // Take a picture
        takePicture();

        // notify handler
        _handler.onComplete(getImage());
    }
    
    
    /**
     * Take Picture method uses com.github.sarxos.webcam library to take a
     * picture using the default webcam (video0). Custom dimensions can be
     * defined in the myResolution array. When the webcam has completed taking a
     * picture the image is saved to the specified directory
     */
    public void takePicture() {
           // Custom resolution
           Dimension[] myResolution = 
                   new Dimension[] { 
                       new Dimension(640, 360), 
                       new Dimension(1280, 720) 
                   };
           // TODO make webcam device a SAP
           Webcam webcam = Webcam.getDefault();
           webcam.setCustomViewSizes(myResolution);
           webcam.setViewSize(myResolution[0]);
           webcam.open();
           image = webcam.getImage();
           webcam.close();
       }
}
