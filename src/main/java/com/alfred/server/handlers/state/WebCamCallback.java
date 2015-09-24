package com.alfred.server.handlers.state;

import java.io.File;

public interface WebCamCallback {
    public void onComplete(File image);
}
