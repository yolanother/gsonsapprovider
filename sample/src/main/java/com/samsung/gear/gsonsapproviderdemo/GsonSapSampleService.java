package com.samsung.gear.gsonsapproviderdemo;

import java.io.IOException;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.samsung.gear.gsonsapproviderservice.GenericGsonSapRequest;
import com.samsung.gear.gsonsapproviderservice.GsonSapProvider;
import com.samsung.gear.gsonsapproviderservice.GsonSapProvider.JsonSapProviderConnection.Requester;
import com.samsung.gear.gsonsapproviderservice.annotations.Channel;
import com.samsung.gear.gsonsapproviderservice.annotations.PreventUnregistered;
import com.samsung.gear.gsonsapproviderdemo.data.HelloMessage;
import com.samsung.gear.gsonsapproviderdemo.data.UnregisteredMessage;

@Channel(104)
@PreventUnregistered
public class GsonSapSampleService extends GsonSapProvider {
    public static final String TAG = GsonSapSampleService.class.getSimpleName();

    private static final String REQ_INIT_HELLO = "req-initialize";

    public GsonSapSampleService() {
        super(TAG);

        registerTypeAdapter(REQ_INIT_HELLO, HelloMessage.class);
    }

    @Override
    protected void onReceive(Requester requester, Object data) {
        if (data instanceof HelloMessage) {
            HelloMessage message = (HelloMessage) data;
            Toast.makeText(this, message.message, Toast.LENGTH_LONG).show();
            try {
                requester.reply(new HelloMessage("Hello from " + Build.DEVICE));

                // This message will only be sent if there is no PreventUnregistered annotation
                requester.reply(new UnregisteredMessage("Again, hello from " + Build.DEVICE));
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        send
    }

    @Override
    protected void onReceiveRequest(Requester requester, GenericGsonSapRequest request) {
        // Unidentified messages will appear here as well as any requests
        // registered as GenericGsonSapRequests
    }

    @Override
    protected void onDeviceConnected(String peerId, JsonSapProviderConnection connection) {
        String device = connection.getConnectedPeerAgent().getAccessory().getName();
        try {
            send(new HelloMessage("I see you have connected " + device), peerId);
            send(new HelloMessage(device + " has connected."));
            send(new UnregisteredMessage("This message isn't registered"));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onConnectionLost(String peerId,
            JsonSapProviderConnection connection, int errorCode) {
        Log.d(TAG, errorToString(errorCode));
    }
}
