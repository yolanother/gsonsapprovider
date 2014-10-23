package com.doubtech.gear.gsonsapprovider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

public abstract class GsonSapProvider extends SAAgent {
    public static final long serialVersionUID = 3L;
    public static final String VERSION = "v0.0.3";

    public static boolean DEBUG = false;

    private final String TAG;
    public static final int CHANNEL = 104;

    HashMap<String, Class<?>> mClassRegistry = new HashMap<>();
    HashMap<String, String> mReverseClassRegistry = new HashMap<>();
    HashMap<String, JsonSapProviderConnection> mConnections = new HashMap<>();

    private final IBinder mBinder = new GsonSapProviderBinder();

    public GsonSapProvider(String tag) {
        super(tag, JsonSapProviderConnection.class);
        TAG = tag;
    }

    @Override
    protected void onServiceConnectionResponse(SASocket saSocket, int result) {
        JsonSapProviderConnection connection = (JsonSapProviderConnection) saSocket;
        switch (result) {
        case CONNECTION_SUCCESS:
            connection.setPeerId(connection.getConnectedPeerAgent().getPeerId());
            mConnections.put(
                    connection.getPeerId(),
                    connection);
            onDeviceConnected(
                    connection.getPeerId(),
                    connection);
            break;
        case CONNECTION_ALREADY_EXIST:
            connection.setPeerId(connection.getConnectedPeerAgent().getPeerId());
            mConnections.put(
                    connection.getPeerId(),
                    connection);
            break;
        }
    }

    @Override
    protected void onFindPeerAgentResponse(SAPeerAgent saPeerAgent, int result) {

    }

    /**
     * @author Aaron Jackson
     *
     */
    public class GsonSapProviderBinder extends Binder {
        public GsonSapProvider getService() {
            return GsonSapProvider.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    protected void registerTypeAdapter(String name, Class<?> classType) {
        mClassRegistry.put(name, classType);
        mReverseClassRegistry.put(classType.getName(), name);
    }

    protected void registerGenericRequest(String name) {
        mClassRegistry.put(name, GenericGsonSapRequest.class);
    }

    Class<?> getRegisteredClass(String name) {
        return mClassRegistry.get(name);
    }

    /**
     * onReceiveRaw is called before json data is parsed so client app can process raw data if
     * needed without the extra overhead of parsing data that may not be json data.
     * If there is any remaining unprocessed data that data will be passed off to the json parser.
     * @param data
     * @return Returns the number of bytes processed if data was handled.
     */
    protected int onReceiveRaw(byte[] data) {
        return 0;
    }

    /**
     * Called when data has successfully been associated with a type in the class registry.
     * @param type
     * @param data
     */
    abstract protected void onReceive(JsonSapProviderConnection.Requester requester, Object data);

    /**
     * Called when a generic request is received or an unknown type was received.
     * @param requester
     * @param request
     */
    abstract protected void onReceiveRequest(JsonSapProviderConnection.Requester requester, GenericGsonSapRequest request);

    private static class SapData {
        final String type;
        final Object data;

        public SapData(String type, Object data) {
            this.type = type;
            this.data = data;
        }
    }

    private class JsonSapDeserializer implements JsonDeserializer<SapData> {
        @Override
        public SapData deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException
        {
            String jstype = je.getAsJsonObject().get("type").getAsString();
            if (null != jstype) {
                Class<?> registeredClass = getRegisteredClass(jstype);
                if (null == registeredClass) {
                    try {
                        registeredClass = Class.forName(jstype);
                    } catch (ClassNotFoundException e) {
                        // Type doesn't point to a class.
                    }
                }

                JsonElement content = je.getAsJsonObject().get("data");

                if (null != registeredClass && !GenericGsonSapRequest.class.isAssignableFrom(registeredClass)) {
                    return new SapData(
                            jstype,
                            new Gson().fromJson(content, registeredClass));
                } else {
                    return new SapData(jstype, new GenericGsonSapRequest(jstype, content));
                }
            }
            return null;
        }
    }

    /**
     * Converts a result code from an attempt to establish a connection with a peer to a string
     * @param result
     * @return
     */
    public static String statusToString(int result) {
        switch (result) {
        case CONNECTION_ALREADY_EXIST:
            return "connection already exists";
        case CONNECTION_FAILURE_DEVICE_UNREACHABLE:
            return "connection failure device unreachable";
        case CONNECTION_FAILURE_INVALID_PEERAGENT:
            return "connection failure invalid peer agent";
        case CONNECTION_FAILURE_NETWORK:
            return "connection failure network";
        case CONNECTION_FAILURE_PEERAGENT_NO_RESPONSE:
            return "connection failure peer agent no response";
        case CONNECTION_FAILURE_PEERAGENT_REJECTED:
            return "connection failure peer agent rejected";
        case CONNECTION_FAILURE_SERVICE_LIMIT_REACHED:
            return "connection failure service limit reached";
        case ERROR_CONNECTION_INVALID_PARAM:
            return "error connection invalid param";
        case ERROR_FATAL:
            return "error fatal";
        case ERROR_SDK_NOT_INITIALIZED:
            return "error sdk not initialized";
        case ERROR_SERVICE_CLASS_NAME_WRONG_IN_XML:
            return "error service class name wrong in xml";
        case ERROR_WRONG_CONSTRUCTOR_PARAM:
            return "error wrong constructor param";
        }
        return "unknown status (" + result + ")";
    }

    /**
     * Converts an error code to a string value
     * @param errorCode A connection lost error code
     * @return
     */
    public static String errorToString(int errorCode) {
        switch (errorCode) {
        case SASocket.CONNECTION_LOST_PEER_DISCONNECTED:
            return "CONNECTION_LOST_PEER_DISCONNECTED (" + errorCode +")";
        case SASocket.CONNECTION_LOST_UNKNOWN_REASON:
            return "CONNECTION_LOST_UNKNOWN_REASON (" + errorCode +")";
        case SASocket.CONNECTION_LOST_DEVICE_DETACHED:
            return "CONNECTION_LOST_DEVICE_DETACHED (" + errorCode +")";
        case SASocket.CONNECTION_LOST_RETRANSMISSION_FAILED:
            return "CONNECTION_LOST_RETRANSMISSION_FAILED (" + errorCode +")";
        case SASocket.ERROR_FATAL:
            return "ERROR_FATAL (" + errorCode +")";
        case SASocket.ERROR_CONNECTION_CLOSED:
            return "ERROR_CONNECTION_CLOSED (" + errorCode +")";
        }
        return "UNKNOWN (" + errorCode +")";
    }

    /**
     * Called when a new connection is established with a device.
     * @param peerId
     * @param connection
     */
    protected void onDeviceConnected(String peerId, JsonSapProviderConnection connection) {
        // Implement if needed.
    }

    /**
     * Called when a connection with a device has been lost.
     * @param peerId
     * @param connection
     * @param errorCode
     */
    protected void onConnectionLost(String peerId, JsonSapProviderConnection connection, int errorCode) {
        // Implement if needed

    }

    private String getRegisteredTypeName(Object data) throws IOException {
        String type = mReverseClassRegistry.get(data.getClass().getName());
        if (null == type) {
            if (DEBUG) {
                Log.w(TAG, data.getClass().getName() + " has not been added to registry. Using class name as registered type name.");
            }
            return data.getClass().getName();
        }
        return type;
    }

    /**
     * Send data to all connected accessories.
     * NOTE: The type will be determined via type registration. If the object is not registered in the registry an exception will be thrown.
     * @param data The data to be converted to JSON via GSON
     * @throws IOException
     */
    public void send(Object data) throws IOException {
        send(getRegisteredTypeName(data), data);
    }

    /**
     * Send data to all connected accessories
     * @param type The name of the message being sent
     * @param data The data to be converted to JSON via GSON
     * @throws IOException
     */
    public void send(String type, Object data) throws IOException {
        for (JsonSapProviderConnection conn : mConnections.values()) {
            if (conn.isConnected()) {
                conn.send(type, data);
            } else {
                Log.w(TAG, "Accessory not connected: " + conn.getConnectedPeerAgent().getDeviceName());
            }
        }
    }

    /**
     * Send data directly to a given accessory identified by its peer id
     * @param peerId The id of the peer to send to
     * @param type The name of the message being sent
     * @param data The data to be converted to JSON via GSON
     * @throws IOException
     */
    public void send(String type, Object data, String peerId) throws IOException {
        JsonSapProviderConnection conn = mConnections.get(peerId);
        if (null == conn) throw new IOException("Accessory not found.");
        if (!conn.isConnected()) throw new IOException("Accessory not connected");
        conn.send(type, data);
    }

    /**
     * Send data directly to a given accessory identified by its peer id
     * @param peerId The id of the peer to send to
     * @param type The name of the message being sent
     * @param data The data to be converted to JSON via GSON
     * @throws IOException
     */
    public void send(Object data, String peerId) throws IOException {
        JsonSapProviderConnection conn = mConnections.get(peerId);
        if (null == conn) throw new IOException("Accessory not found.");
        if (!conn.isConnected()) throw new IOException("Accessory not connected");
        conn.send(getRegisteredTypeName(data), data);
    }

    /**
     * Gets the connection object associated with a given peer id.
     * @param peerId The peer id to find.
     * @return Returns null if no peer id is found.
     */
    public JsonSapProviderConnection getConnection(String peerId) {
        return mConnections.get(peerId);
    }

    /**
     * Connection object that handles connections with an individual peer application
     * Created by Aaron Jackson on 10/13/14.
     */
    public class JsonSapProviderConnection extends
            SASocket {
        /**
         * Created by Aaron Jackson on 10/13/14.
         */
        public class Requester {
            private String mType;
            private SAPeerAgent mAgent;

            public Requester(String type) {
                mType = type;
                mAgent = getConnectedPeerAgent();
            }

            public String getRequestType() {
                return mType;
            }

            public SAPeerAgent getPeerAgent() {
                return mAgent;
            }

            public String getDeviceName() {
                return mAgent.getDeviceName();
            }

            public void reply(String type, Object data) throws IOException {
                send(type, data);
            }

            public void reply(Object data) throws IOException {
                send(getRegisteredTypeName(data), data);
            }
        }

        private String TAG;
        private String mPeerId;

        /**
         *
         */
        public JsonSapProviderConnection() {
            super(JsonSapProviderConnection.class.getName());
        }

        public String getPeerId() {
            return mPeerId;
        }

        public void setPeerId(String peerId) {
            mPeerId = peerId;
        }

        /**
         *
         * @param channelId
         * @param data
         * @return
         */
        @Override
        public void onReceive(int channelId, byte[] data) {
            int processed = onReceiveRaw(data);
            if (processed == data.length) return;

            try {
                String sData = new String(data, processed, data.length - processed);
                if (null != sData && sData.length() > 0) {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(SapData.class, new JsonSapDeserializer())
                            .create();
                    SapData parsedData = gson.fromJson(sData, SapData.class);
                    if (null != parsedData) {
                        if (parsedData.data instanceof GenericGsonSapRequest) {
                            onReceiveRequest(new Requester(parsedData.type), (GenericGsonSapRequest) parsedData.data);
                        } else {
                            GsonSapProvider.this.onReceive(new Requester(parsedData.type), parsedData.data);
                        }
                    }
                }
            } catch (JsonSyntaxException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        /**
         *
         * @param channelId
         * @param errorString
         * @param error
         */
        @Override
        public void onError(int channelId, String errorString, int error) {
            Log.e(TAG, errorString + " (" + error + ")");
        }

        /**
         *
         * @param errorCode
         */
        @Override
        public void onServiceConnectionLost(int errorCode) {
            final String peerId = getPeerId();
            mConnections.remove(peerId);
            onConnectionLost(peerId, this, errorCode);
        }

        void send(String type, Object data) throws IOException {
            send(CHANNEL, new Gson().toJson(new SapData(type, data)).getBytes());
        }
    }
}
