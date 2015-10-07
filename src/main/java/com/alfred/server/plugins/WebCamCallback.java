package com.alfred.server.plugins;

import java.awt.image.RenderedImage;

public interface WebCamCallback {
    public void onComplete(RenderedImage image);
}
