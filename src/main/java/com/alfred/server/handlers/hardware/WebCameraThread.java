package com.alfred.server.handlers.hardware;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.fswebcam.FsWebcamDriver;

public class WebCameraThread implements Runnable {
    
    static {
        Webcam.setDriver(new FsWebcamDriver()); // this is important
    }
    
    private File image;
    private WebCamCallback _handler;
    
    public File getImage() {
        return image;
    }
    
    public WebCameraThread(WebCamCallback handler) {
        _handler = handler;
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
           String filepath = "/home/pi/Alfred/img";
           String filename = "visitor" + System.currentTimeMillis() / 1000L + ".jpg";
           try {
               image = new File(filepath + filename);
               ImageIO.write(webcam.getImage(), "JPG", image);
           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
           webcam.close();
       }
}
