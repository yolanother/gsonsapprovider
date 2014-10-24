package com.samsung.gear.gsonsapproviderdemo.data;

public class HelloMessage {
    public long timestamp;
    public String message;

    public HelloMessage(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
