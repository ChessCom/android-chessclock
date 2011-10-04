package com.chess.activities.tabs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.chess.R;
import com.chess.activities.Preferences;
import com.chess.activities.Singin;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MobclixAdViewListenerImpl;
import com.mobclix.android.sdk.MobclixFullScreenAdView;
import com.mobclix.android.sdk.MobclixFullScreenAdViewListener;
import com.mobclix.android.sdk.MobclixMMABannerXLAdView;

public class Home extends CoreActivity {

	private MobclixMMABannerXLAdView adview;
	private TextView removeAds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(App.guest)
			setContentView(R.layout.home_guest);
		else
			setContentView(R.layout.home);

	removeAds = (TextView) findViewById(R.id.removeAds);
    removeAds.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
          "http://www." + LccHolder.HOST + "/login.html?als=" + App.sharedData.getString("user_token", "") +
          "&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html?c=androidads")));
      }
    });
    adview = (MobclixMMABannerXLAdView) findViewById(R.id.adview);
    adview.addMobclixAdViewListener(new MobclixAdViewListenerImpl());

    findViewById(R.id.live).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        App.mTabHost.setCurrentTab(1);
			}
		});
		findViewById(R.id.online).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				App.mTabHost.setCurrentTab(2);
			}
		});
		findViewById(R.id.computer).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				App.mTabHost.setCurrentTab(3);
			}
		});
		findViewById(R.id.tactics).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				App.mTabHost.setCurrentTab(4);
			}
		});
		findViewById(R.id.video).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				App.mTabHost.setCurrentTab(5);
			}
		});

		findViewById(R.id.settings).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Home.this, Preferences.class));
			}
		});

		findViewById(R.id.site).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.chess.com")));
			}
		});
    findViewById(R.id.logout).setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if(!App.guest)
        {
          if(App.isLiveChess()/* && lccHolder.isConnected()*/)
          {
            lccHolder.logout();
          }
          App.SDeditor.putString("password", "");
          App.SDeditor.putString("user_token", "");
          App.SDeditor.commit();
        }
        startActivity(new Intent(Home.this, Singin.class));
        finish();
      }
    });

	}

	@Override
	public void LoadNext(int code) {

	}

	@Override
	public void LoadPrev(int code) {
		finish();
	}

	@Override
	public void Update(int code) {
	}

  @Override
  protected void onResume() {
      super.onResume();
      new Handler().post(new Runnable() {
          public void run() {
              showAds(adview);
              showFullscreenAd();
              if (isShowAds()) {
                  showRemoveAds(adview, removeAds);
              }
          }
      });
  }

  private void showFullscreenAd()
  {
    if(!App.sharedData.getBoolean("com.chess.showedFullscreenAd", false) && isShowAds())
    {
		//MobclixFullScreenAdView fsAdView = new MobclixFullScreenAdView(this);
/*		fsAdView.addMobclixAdViewListener(new MobclixFullScreenAdViewListener() {
			
			@Override
			public String query() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void onPresentAd(MobclixFullScreenAdView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinishLoad(MobclixFullScreenAdView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailedLoad(MobclixFullScreenAdView adView, int errorCode) {
				System.out.println("!!!!!!!! AD ERROR: " + errorCode);				
			}
			
			@Override
			public void onDismissAd(MobclixFullScreenAdView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String keywords() {
				// TODO Auto-generated method stub
				return null;
			}
		});*/
		//fsAdView.requestAndDisplayAd();

		//MoPubInterstitial interstitial = new MoPubInterstitial(this, "agltb3B1Yi1pbmNyDQsSBFNpdGUYioOrAgw");
		/*MoPubInterstitial interstitial = new MoPubInterstitial(this, "agltb3B1Yi1pbmNyDAsSBFNpdGUYsckMDA"); // test
		interstitial.showAd();
		App.SDeditor.putBoolean("com.chess.showedFullscreenAd", true);
		App.SDeditor.commit();*/
    }
  }
}
