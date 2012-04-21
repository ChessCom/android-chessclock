package com.mopub.mobileads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.FrameLayout;
import com.chess.R;

public class HTML5AdView extends AdView {
    
    private FrameLayout mCustomViewContainer;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private Bitmap mDefaultVideoPoster;
    private View mVideoProgressView;
    
    static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER = 
        new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
    
    public HTML5AdView(Context context, MoPubView view) {
        super(context, view);
        
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion > 7) {
            setWebChromeClient(new HTML5WebChromeClient());
        }
        
        mCustomViewContainer = new FrameLayout(context);
        mCustomViewContainer.setVisibility(GONE);
        mCustomViewContainer.setLayoutParams(COVER_SCREEN_GRAVITY_CENTER);
    }

    private class HTML5WebChromeClient extends WebChromeClient implements OnCompletionListener, 
            OnErrorListener {

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            
            HTML5AdView.this.setVisibility(View.GONE);
            
            // If a custom view already exists, don't show another one.
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            
            mCustomViewContainer.addView(view, COVER_SCREEN_GRAVITY_CENTER);
            mCustomView = view;
            mCustomViewCallback = callback;
            
            // Display the custom view in the MoPubView's hierarchy.
            mMoPubView.addView(mCustomViewContainer);
            mCustomViewContainer.setVisibility(View.VISIBLE);
            mCustomViewContainer.bringToFront();
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView == null) return;

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);
            
            // Remove the custom view from its container.
            mCustomViewContainer.removeView(mCustomView);
            mCustomView = null;
            mCustomViewContainer.setVisibility(View.GONE);
            mCustomViewCallback.onCustomViewHidden();
            
            // Stop displaying the custom view container and unhide the ad view.
            mMoPubView.removeView(mCustomViewContainer);
            HTML5AdView.this.setVisibility(View.VISIBLE);
        }
        
        @Override
        public Bitmap getDefaultVideoPoster() { 
            if (mDefaultVideoPoster == null) {
                mDefaultVideoPoster = BitmapFactory.decodeResource(
                        getResources(), R.drawable.default_video_poster);
            }
            return mDefaultVideoPoster;
        }
        
        @Override
        public View getVideoLoadingProgressView() {
            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                mVideoProgressView = inflater.inflate(R.layout.video_loading_progress, null);
            }
            return mVideoProgressView;
        }
        
        @Override
        public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
            Log.d("MoPub", "Video errored!");
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mp.stop();
            mCustomViewCallback.onCustomViewHidden();
            Log.d("MoPub", "Video completed!");
        }
    }
}
