package com.samsung.gear.gsonsapproviderdemo.data;

public class UnregisteredMessage {
    long timestamp;
    String message;

    public UnregisteredMessage(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
