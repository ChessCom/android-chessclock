package com.mopub.mobileads;

import com.mopub.mobileads.MraidView.ExpansionStyle;
import com.mopub.mobileads.MraidView.NativeCloseButtonStyle;
import com.mopub.mobileads.MraidView.PlacementType;
import com.mopub.mobileads.MraidView.ViewState;

import android.view.View;

public class MraidActivity extends BaseActivity {    
    private MraidView mAdView;
    
    @Override
    public View getAdView() {
        mAdView = new MraidView(this, ExpansionStyle.DISABLED, NativeCloseButtonStyle.AD_CONTROLLED,
                PlacementType.INTERSTITIAL);
        
        mAdView.setOnReadyListener(new MraidView.OnReadyListener() {
           public void onReady(MraidView view) {
               showInterstitialCloseButton();
           }
        });
        
        mAdView.setOnCloseButtonStateChange(new MraidView.OnCloseButtonStateChangeListener() {
            public void onCloseButtonStateChange(MraidView view, boolean enabled) {
                if (enabled) showInterstitialCloseButton();
                else hideInterstitialCloseButton();
            }
        });
        
        mAdView.setOnCloseListener(new MraidView.OnCloseListener() {
            public void onClose(MraidView view, ViewState newViewState) {
                finish();
            }
        });
        
        String source = getIntent().getStringExtra("com.mopub.mobileads.Source");
        mAdView.loadHtmlData(source);
        
        return mAdView;
    }
    
    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }
}
