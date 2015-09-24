package com.kanzelmeyer.alfred.handlers.hardware;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.fswebcam.FsWebcamDriver;

public class WebCamera {
    
    static {
        Webcam.setDriver(new FsWebcamDriver()); // this is important
    }
    
    private File image;
    
    public File getImage() {
        return image;
    }

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
