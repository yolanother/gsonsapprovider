package com.doubtech.gear.gsonsapprovider;

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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public abstract class GsonSapProvider extends SAAgent {
    private final String TAG;

    public static final int CHANNEL = 104;

    HashMap<String, Class<?>> mClassRegistry = new HashMap<>();

    private final IBinder mBinder = new LocalBinder();

    public GsonSapProvider(String tag) {
        super(tag, JsonSapProviderConnection.class);
        TAG = tag;
    }

    @Override
    protected void onServiceConnectionResponse(SASocket saSocket, int result) {
        
    }

    @Override
    protected void onFindPeerAgentResponse(SAPeerAgent saPeerAgent, int result) {

    }

    /**
     * @author Aaron Jackson
     *
     */
    public class LocalBinder extends Binder {
        public GsonSapProvider getService() {
            return GsonSapProvider.this;
        }
    }

    /**
     *
     * @param intent
     * @return IBinder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    protected void registerTypeAdapter(String name, Class<?> classType) {
        mClassRegistry.put(name, classType);
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
     * @param data
     * @return Returns true if data was handled and does not need to go to the json parser.
     */
    protected boolean onReceiveRaw(byte[] data) {
        // Implement if needed.
        return false;
    }

    /**
     * Called when data has successfully been associated with a type in the class registry.
     * @param type
     * @param data
     */
    abstract protected void onReceive(JsonSapProviderConnection.Requester requester, Object data);

    abstract protected void onReceivedRequest(JsonSapProviderConnection.Requester requester, GenericGsonSapRequest request);

    private static class SapData {
        final String type;
        final Object data;

        public SapData(String type, Object data) {
            this.type = type;
            this.data = data;
        }
    }

    private class JsonSapDeserializer implements JsonDeserializer<SapData> {
        public JsonSapDeserializer() {
        }

        @Override
        public SapData deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException
        {
            String jstype = je.getAsJsonObject().get("type").getAsString();
            if(null != jstype) {
                Class<?> registeredClass = getRegisteredClass(jstype);
                Log.w("JsonSapProvider", "Type not in registry: " + jstype);
                if (null != registeredClass) {
                    if(GenericGsonSapRequest.class.isAssignableFrom(registeredClass)) {
                        return new SapData(jstype, new GenericGsonSapRequest(jstype, je.getAsJsonObject().get("data")));
                    } else {
                        JsonElement content = je.getAsJsonObject().get("data");
                        return new SapData(
                                jstype,
                                new Gson().fromJson(content, registeredClass));
                    }
                }
            }
            return null;
        }
    }

    public class JsonSapProviderConnection extends
            SASocket {
        /**
         * Created by Aaron Jackson on 10/13/14.
         */
        public class Requester {
            private String mType;

            public Requester(String type) {
                mType = type;
            }

            public String getRequestType() {
                return mType;
            }

            public void reply(String type, Object data) throws IOException {
                send(CHANNEL, new Gson().toJson(new SapData(type, data)).getBytes());
            }
        }

        private int mConnectionId;
        private String TAG;

        /**
         *
         */
        public JsonSapProviderConnection() {
            super(JsonSapProviderConnection.class.getName());
        }

        public void setService(GsonSapProvider service) {
            TAG = GsonSapProvider.this.TAG + "::Connection";
        }

        /**
         *
         * @param channelId
         * @param data
         * @return
         */
        @Override
        public void onReceive(int channelId, byte[] data) {
            if(onReceiveRaw(data)) return;

            try {
                String sData = new String(data);
                if (null != sData && sData.length() > 0) {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(SapData.class, new JsonSapDeserializer())
                            .create();
                    SapData parsedData = gson.fromJson(sData, SapData.class);
                    if (null != parsedData) {
                        if(parsedData.data instanceof GenericGsonSapRequest) {
                            onReceivedRequest(new Requester(parsedData.type), (GenericGsonSapRequest) parsedData.data);
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
            Log.e(TAG, "Connection is not alive ERROR: " + errorString + "  "
                    + error);
        }

        /**
         *
         * @param errorCode
         */
        @Override
        public void onServiceConnectionLost(int errorCode) {
            Log.e(TAG, "onServiceConectionLost  for peer = "
                    + mConnectionId + "error code =" + errorCode);
        }
    }
}
