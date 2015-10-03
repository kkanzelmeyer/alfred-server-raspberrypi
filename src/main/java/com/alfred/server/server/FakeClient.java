package com.alfred.server.server;

public class FakeClient {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Thread client = new Thread(new FakeConnection("192.168.1.25", "56"));
        client.start();
    }

}
