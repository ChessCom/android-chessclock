package com.mopub.mobileads.factories;

import com.mopub.mobileads.util.vast.VastManager;

public class VastManagerFactory {
    protected static VastManagerFactory instance = new VastManagerFactory();

    public static VastManager create() {
        return instance.internalCreate();
    }

    public VastManager internalCreate() {
        return new VastManager();
    }

    @Deprecated // for testing
    public static void setInstance(VastManagerFactory factory) {
        instance = factory;
    }
}
