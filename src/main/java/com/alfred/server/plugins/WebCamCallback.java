package com.alfred.server.plugins;

import java.awt.image.RenderedImage;

public interface WebCamCallback {
    
    /**
     * Called when the webcam has completed taking a picture
     * 
     * @param image The RenderedImage taken by the webcam
     */
    public void onComplete(RenderedImage image);
}
