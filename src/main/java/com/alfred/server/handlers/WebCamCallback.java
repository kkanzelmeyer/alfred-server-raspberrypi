package com.alfred.server.handlers;

import java.awt.image.RenderedImage;

public interface WebCamCallback {
    public void onComplete(RenderedImage image);
}
