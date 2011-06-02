package com.mopub.mobileads;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import android.util.Log;

public abstract class BaseInterstitialAdapter {

    protected MoPubInterstitial mInterstitial;
    
    private static final HashMap<String, String> sInterstitialAdapterMap;
    static {
        sInterstitialAdapterMap = new HashMap<String, String>();
        sInterstitialAdapterMap.put("admob_full", "com.mopub.mobileads.GoogleAdMobInterstitialAdapter");
        sInterstitialAdapterMap.put("millennial_full", "com.mopub.mobileads.MillennialInterstitialAdapter");
    }
    
    public abstract void loadInterstitial();
    public abstract void showInterstitial();
    public abstract void invalidate();
    
    protected BaseInterstitialAdapter(MoPubInterstitial interstitial) {
        mInterstitial = interstitial;
    }
    
    public static BaseInterstitialAdapter getAdapterForType(MoPubInterstitial interstitial, 
                                                            String type, 
                                                            HashMap<String, String> params) {
        if (type == null) {
            return null;
        }
        
        Class<?> adapterClass = classForAdapterType(type, params);
        if (adapterClass == null) {
            return null;
        }
        
        Class<?>[] parameterTypes = new Class[2];
        parameterTypes[0] = MoPubInterstitial.class;
        parameterTypes[1] = String.class;
        
        Object[] args = new Object[2];
        args[0] = interstitial;
        args[1] = params.get("X-Nativeparams");
        
        try {
            Constructor<?> constructor = adapterClass.getConstructor(parameterTypes);
            BaseInterstitialAdapter nativeAdapter = 
                    (BaseInterstitialAdapter) constructor.newInstance(args);
            return nativeAdapter;
        } catch (Exception e) {
            Log.d("MoPub", "Couldn't create native interstitial adapter for type: "+type);
            return null;
        }
    }
        
    protected static String classStringForAdapterType(String type, HashMap<String, String> params) {
        String fullAdType = params.get("X-Fulladtype");
        
        if (type.equals("interstitial") && fullAdType != null) {
            return sInterstitialAdapterMap.get(fullAdType);
        }
        return null;
    }
        
    protected static Class<?> classForAdapterType(String type, HashMap<String, String> params) {
        String className = classStringForAdapterType(type, params);
        if (className == null) {
            Log.d("MoPub", "Couldn't find a handler for this ad type: "+type+"."
            + " MoPub for Android does not support it at this time.");
            return null;
        }
        
        try {
            return (Class<?>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.d("MoPub", "Couldn't find "+className+ "class."
            + " Make sure the project includes the adapter library for "+className
            + " from the extras folder");
            return null;
        }
    }
}
