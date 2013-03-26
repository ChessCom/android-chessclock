package com.chess.backend.share.facebook;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

/**
 * Skeleton base class for RequestListeners, providing default error 
 * handling. Applications should handle these error conditions.
 *
 */
public abstract class BaseDialogListener implements DialogListener {

    @Override
	public void onFacebookError(FacebookError e) {
        e.printStackTrace();
    }

    @Override
	public void onError(DialogError e) {
        e.printStackTrace();        
    }

    @Override
	public void onCancel() {
    }
    
}
