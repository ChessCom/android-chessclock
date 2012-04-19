/*
 * Copyright (c) 2011, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public abstract class BaseAdapter {
    
    protected boolean mInvalidated;
    protected MoPubView mMoPubView;
    protected String mJsonParams;
    
    public abstract void loadAd();
    
    private static final HashMap<String, String> sAdapterMap;
    static {
        sAdapterMap = new HashMap<String, String>();
        sAdapterMap.put("admob_native", "com.mopub.mobileads.GoogleAdMobAdapter");
        sAdapterMap.put("millennial_native", "com.mopub.mobileads.MillennialAdapter");
        sAdapterMap.put("mraid", "com.mopub.mobileads.MraidAdapter");
    }
    
    public void init(MoPubView view, String jsonParams) {
        mMoPubView = view;
        mJsonParams = jsonParams;
        mInvalidated = false;
    }
    
    public void invalidate() {
        mMoPubView = null;
        mInvalidated = true;
    }
    
    public boolean isInvalidated() {
        return mInvalidated;
    }
    
    public static BaseAdapter getAdapterForType(String type) {
        if (type == null) return null;
        
        Class<?> adapterClass = classForAdapterType(type);
        if (adapterClass == null) return null;
    
        try {
            Constructor<?> constructor = adapterClass.getConstructor();
            BaseAdapter nativeAdapter = (BaseAdapter) constructor.newInstance();
            return nativeAdapter;
        } catch (Exception e) {
            Log.d("MoPub", "Couldn't create native adapter for type: " + type);
            return null;
        }
    }
    
    private static String classStringForAdapterType(String type) {
        return sAdapterMap.get(type);
    }
    
    private static Class<?> classForAdapterType(String type) {
        String className = classStringForAdapterType(type);
        if (className == null) {
            Log.d("MoPub", "Couldn't find a handler for this ad type: " + type + "."
                    + " MoPub for Android does not support it at this time.");
            return null;
        }
    
        try {
            return (Class<?>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.d("MoPub", "Couldn't find " + className + " class."
                    + " Make sure the project includes the adapter library for " + className
                    + " from the extras folder");
            return null;
        }
    }
}
