package com.mopub.mobileads;

import java.util.Map;

abstract class MraidCommand {
    protected Map<String, String> mParams;
    protected MraidView mView;
    
    MraidCommand(Map<String, String> params, MraidView view) {
        mParams = params;
        mView = view;
    }
    
    abstract void execute();
    
    protected int getIntFromParamsForKey(String key) {
        String s = mParams.get(key);
        if (s == null) return -1;
        else {
            try {
                return Integer.parseInt(s, 10);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }
    
    protected String getStringFromParamsForKey(String key) {
        return mParams.get(key);
    }
    
    protected float getFloatFromParamsForKey(String key) {
        String s = mParams.get(key);
        if (s == null) return 0.0f;
        else {
            try {
                return Float.parseFloat(key);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        }
    }
    
    protected boolean getBooleanFromParamsForKey(String key) {
        return "true".equals(mParams.get(key));
    }
}

class MraidCommandClose extends MraidCommand {
    MraidCommandClose(Map<String, String> params, MraidView view) {
        super(params, view);
    }

    void execute() {
        mView.getDisplayController().close();
    }
}

class MraidCommandExpand extends MraidCommand {
    MraidCommandExpand(Map<String, String> params, MraidView view) {
        super(params, view);
    }

    void execute() {
        int width = getIntFromParamsForKey("w");
        int height = getIntFromParamsForKey("h");
        String url = getStringFromParamsForKey("url");
        boolean shouldUseCustomClose = getBooleanFromParamsForKey("shouldUseCustomClose");
        boolean shouldLockOrientation = getBooleanFromParamsForKey("lockOrientation");
        
        if (width <= 0) width = mView.getDisplayController().mScreenWidth;
        if (height <= 0) height = mView.getDisplayController().mScreenHeight;
        
        mView.getDisplayController().expand(url, width, height, shouldUseCustomClose,
                shouldLockOrientation);
    }
}

class MraidCommandUseCustomClose extends MraidCommand {
    MraidCommandUseCustomClose(Map<String, String> params, MraidView view) {
        super(params, view);
    }

    void execute() {
        boolean shouldUseCustomClose = getBooleanFromParamsForKey("shouldUseCustomClose");
        mView.getDisplayController().useCustomClose(shouldUseCustomClose);
    }
}

class MraidCommandOpen extends MraidCommand {
    MraidCommandOpen(Map<String, String> params, MraidView view) {
        super(params, view);
    }

    void execute() {
        String url = getStringFromParamsForKey("url");
        mView.getBrowserController().open(url);
    }
}
