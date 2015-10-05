package com.alfred.server.handlers;

import java.awt.Dimension;
import java.awt.image.RenderedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;

public class WebCameraThread implements Runnable {
    
    private RenderedImage image;
    private WebCamCallback _handler;
    
    public RenderedImage getImage() {
        return image;
    }
    
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
     * Take Picture method uses com.github.sarxos.webcam library
     * to take a picture using the default webcam (video0). Custom
     * dimensions can be defined in the myResolution array. When the
     * webcam has completed taking a picture the image is saved to
     * the specified directory
     */
    public void takePicture() {
        // Custom resolution
           Dimension[] myResolution = 
                   new Dimension[] { 
                       new Dimension(720, 405), 
                       new Dimension(1280, 720) 
                   };
           Webcam webcam = Webcam.getDefault();
           webcam.setCustomViewSizes(myResolution);
           webcam.setViewSize(myResolution[0]);
           webcam.open();
           image = webcam.getImage();
           webcam.close();
           
       }
}
