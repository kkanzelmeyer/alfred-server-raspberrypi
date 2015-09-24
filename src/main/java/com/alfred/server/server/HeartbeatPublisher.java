package com.alfred.server.server;

import java.util.Timer;


public class HeartbeatPublisher {

    private Timer _timer;
    private int _interval;
    
    public HeartbeatPublisher(int interval) {
        _interval = interval;
    }
    
    public void start() {
        _timer = new Timer("Message Timer");
        _timer.schedule(new HeartbeatTask(), 0, _interval * 1000);
    }
    
    public void stop() {
        _timer.cancel();
    }
}
