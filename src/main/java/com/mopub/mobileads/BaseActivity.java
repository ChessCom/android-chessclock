package com.mopub.mobileads;

import android.app.Activity;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public abstract class BaseActivity extends Activity {
    private static final float CLOSE_BUTTON_SIZE_DP = 50.0f;
    private static final float CLOSE_BUTTON_PADDING_DP = 8.0f;
    
    private ImageView mCloseButton;
    private RelativeLayout mLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        mLayout = new RelativeLayout(this);
        final RelativeLayout.LayoutParams adViewLayout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        adViewLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayout.addView(getAdView(), adViewLayout);
        setContentView(mLayout);
        
        showInterstitialCloseButton();
    }
    
    public abstract View getAdView();
    
    protected void showInterstitialCloseButton() {
        if (mCloseButton == null) {
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[] {-android.R.attr.state_pressed},
                    getResources().getDrawable(R.drawable.close_button_normal));
            states.addState(new int[] {android.R.attr.state_pressed},
                    getResources().getDrawable(R.drawable.close_button_pressed));
            mCloseButton = new ImageButton(this);
            mCloseButton.setImageDrawable(states);
            mCloseButton.setBackgroundDrawable(null);
            mCloseButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        }
        
        final float scale = getResources().getDisplayMetrics().density;
        int buttonSize = (int) (CLOSE_BUTTON_SIZE_DP * scale + 0.5f);
        int buttonPadding = (int) (CLOSE_BUTTON_PADDING_DP * scale + 0.5f);
        RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams(
                buttonSize, buttonSize);
        buttonLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonLayout.setMargins(buttonPadding, 0, buttonPadding, 0);
        mLayout.removeView(mCloseButton);
        mLayout.addView(mCloseButton, buttonLayout);
    }
    
    protected void hideInterstitialCloseButton() {
        mLayout.removeView(mCloseButton);
    }
    
    @Override
    protected void onDestroy() {
        mLayout.removeAllViews();
        super.onDestroy();
    }
}
