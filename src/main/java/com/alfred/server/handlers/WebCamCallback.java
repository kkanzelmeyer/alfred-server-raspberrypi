package com.alfred.server.handlers;

import java.io.File;

public interface WebCamCallback {
    public void onComplete(File image);
}
