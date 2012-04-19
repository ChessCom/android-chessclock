package com.mopub.mobileads;

import com.mopub.mobileads.MraidView.PlacementType;
import com.mopub.mobileads.MraidView.ViewState;

abstract class MraidProperty {
    private String sanitize(String str) {
        return (str != null) ? str.replaceAll("[^a-zA-Z0-9_,:\\s\\{\\}\\\'\\\"]", "") : "";
    }

    @Override
    public String toString() {
        return sanitize(toJsonPair());
    }

    public abstract String toJsonPair();
}

class MraidPlacementTypeProperty extends MraidProperty {
    private final PlacementType mPlacementType;
    
    MraidPlacementTypeProperty(PlacementType placementType) {
        mPlacementType = placementType;
    }
    
    public static MraidPlacementTypeProperty createWithType(
            PlacementType placementType) {
        return new MraidPlacementTypeProperty(placementType);
    }
    
    @Override
    public String toJsonPair() {
        return "placementType: '" + mPlacementType.toString().toLowerCase() + "'";
    }
}

class MraidScreenSizeProperty extends MraidProperty {
    private final int mScreenWidth;
    private final int mScreenHeight;
    
    MraidScreenSizeProperty(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }
    
    public static MraidScreenSizeProperty createWithSize(int width, int height) {
        return new MraidScreenSizeProperty(width, height);
    }
    
    @Override
    public String toJsonPair() {
        return "screenSize: { width: " + mScreenWidth + ", height: " + mScreenHeight + " }";
    }
}

class MraidStateProperty extends MraidProperty {
    private final ViewState mViewState;
    
    MraidStateProperty(ViewState viewState) {
        mViewState = viewState;
    }
    
    public static MraidStateProperty createWithViewState(ViewState viewState) {
        return new MraidStateProperty(viewState);
    }
    
    @Override
    public String toJsonPair() {
        return "state: '" + mViewState.toString().toLowerCase() + "'";
    }
}

class MraidViewableProperty extends MraidProperty {
    private final boolean mViewable;
    
    MraidViewableProperty(boolean viewable) {
        mViewable = viewable;
    }
    
    public static MraidViewableProperty createWithViewable(boolean viewable) {
        return new MraidViewableProperty(viewable);
    }
    
    @Override
    public String toJsonPair() {
        return "viewable: " + (mViewable ? "true" : "false");
    }
}