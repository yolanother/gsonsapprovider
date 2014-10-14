package com.doubtech.gear.gsonsapprovider;

import com.google.gson.JsonElement;

/**
 * Created by Aaron Jackson on 10/13/14.
 */
public class GenericGsonSapRequest {
    private final String mType;
    private final JsonElement mData;

    public GenericGsonSapRequest(String type, JsonElement data) {
        mType = type;
        mData = data;
    }

    public String getRequestType() {
        return mType;
    }

    public boolean has(String name) {
        return null == mData ? false : null != mData.getAsJsonObject().get(name);
    }

    public String getString(String name, String defaultVal) {
        if(null != mData) {
            JsonElement element = mData.getAsJsonObject().get(name);
            if(null != element) {
                return element.getAsString();
            }
        }
        return defaultVal;
    }

    public boolean getBoolean(String name, boolean defaultVal) {
        if(null != mData) {
            JsonElement element = mData.getAsJsonObject().get(name);
            if(null != element) {
                return element.getAsBoolean();
            }
        }
        return defaultVal;
    }

    public int getInt(String name, int defaultVal) {
        if(null != mData) {
            JsonElement element = mData.getAsJsonObject().get(name);
            if(null != element) {
                return element.getAsInt();
            }
        }
        return defaultVal;
    }

    public long getLong(String name, long defaultVal) {
        if(null != mData) {
            JsonElement element = mData.getAsJsonObject().get(name);
            if(null != element) {
                return element.getAsLong();
            }
        }
        return defaultVal;
    }

    public float getFloat(String name, float defaultVal) {
        if(null != mData) {
            JsonElement element = mData.getAsJsonObject().get(name);
            if(null != element) {
                return element.getAsFloat();
            }
        }
        return defaultVal;
    }
}
