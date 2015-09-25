package com.alfred.server.handlers;

import java.net.Socket;

public interface ServerConnectionHandler {
    
    public void onAddConnection(Socket connection);
    
    public void onRemoveConnection(Socket connection);
}
