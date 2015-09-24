package com.alfred.server.handlers.hardware;

import java.io.File;

public interface WebCamCallback {
    public void onComplete(File image);
}
