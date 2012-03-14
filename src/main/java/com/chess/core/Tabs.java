package com.chess.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.*;
import com.chess.R;
import com.chess.activities.Game;
import com.chess.activities.Register;
import com.chess.activities.tabs.Computer;
import com.chess.activities.tabs.Home;
import com.chess.activities.tabs.Online;
import com.chess.activities.tabs.Video;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MobclixHelper;
import com.mobclix.android.sdk.Mobclix;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.mopub.mobileads.MoPubView;

public class Tabs extends TabActivity {

	public MainApp App;
	private TextView removeAds;
	private MoPubView moPubAdView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mobclix.onCreate(this);
        App = (MainApp)getApplication();

        //get global Shared Preferences
        if (App.sharedData == null)
        {
          App.sharedData = getSharedPreferences("sharedData", 0);
          App.SDeditor = App.sharedData.edit();
        }

        setContentView(R.layout.tabs);

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

		// integrate mopub, disable mobclix
        /*if (MobclixHelper.isShowAds(App))
        {
          if (MobclixHelper.getBannerAdviewWrapper(App) == null || MobclixHelper.getBannerAdview(App) == null)
          {
            MobclixHelper.initializeBannerAdView(this, App);
          }
        }*/
		/*
		//
		Hashtable<String, String> map = new Hashtable<String, String>();
		//map.put("income", "50000");
		MMAdView adView = new MMAdView(this, "77013", MMAdView.BANNER_AD_BOTTOM, 30);
		//MMAdView adView = new MMAdView(this, "28911", MMAdView.BANNER_AD_BOTTOM, 30);
		adView.setId(MMAdViewSDK.DEFAULT_VIEWID);
		FrameLayout adFrameLayout = (FrameLayout)findViewById(R.id.millennial_wrapper);
		//LinearLayout adFrameLayout = (LinearLayout)findViewById(R.id.adview_wrapper);
		adFrameLayout.setVisibility(View.VISIBLE);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		adFrameLayout.addView(adView, lp);
		//final LinearLayout bannerAdviewWrapper = (LinearLayout) findViewById(R.id.wrapper);*/

		/*moPubAdView = (MoPubView) findViewById(R.id.mopub_adview);
		moPubAdView.setAdUnitId("agltb3B1Yi1pbmNyDQsSBFNpdGUYlvOBEww");
		moPubAdView.loadAd();*/
		//

	    App.mTabHost = getTabHost();
	    App.mTabHost.addTab(App.mTabHost.newTabSpec("tab1")
	    		.setIndicator("Home", getResources().getDrawable(R.drawable.home))
	    		.setContent(new Intent(this, Home.class)));
	    if(App.guest)
      {
	    	App.mTabHost.addTab(App.mTabHost.newTabSpec("tab2")
          .setIndicator("Live", getResources().getDrawable(R.drawable.live))
          .setContent(new Intent(this, Register.class).putExtra("liveChess", true)));
        App.mTabHost.addTab(App.mTabHost.newTabSpec("tab6")
          .setIndicator("Online", getResources().getDrawable(R.drawable.online))
          .setContent(new Intent(this, Register.class).putExtra("liveChess", false)));
      }
	    else
      {
        App.mTabHost.addTab(App.mTabHost.newTabSpec("tab2")
          .setIndicator("Live", getResources().getDrawable(R.drawable.live))
          .setContent(new Intent(this, Online.class).putExtra("liveChess", true)));
        App.mTabHost.addTab(App.mTabHost.newTabSpec("tab6")
          .setIndicator("Online", getResources().getDrawable(R.drawable.online))
          .setContent(new Intent(this, Online.class).putExtra("liveChess", false)));
      }

	    App.mTabHost.addTab(App.mTabHost.newTabSpec("tab3")
	    		.setIndicator("Comp", getResources().getDrawable(R.drawable.computer))
	    		.setContent(new Intent(this, Computer.class)));
	    App.mTabHost.addTab(App.mTabHost.newTabSpec("tab4")
	    		.setIndicator("Tactics", getResources().getDrawable(R.drawable.tactics))
	    		.setContent(new Intent(this, Game.class).putExtra("mode", 6).putExtra("liveChess", false)));
	    App.mTabHost.addTab(App.mTabHost.newTabSpec("tab5")
	    		.setIndicator("Video", getResources().getDrawable(R.drawable.video))
	    		.setContent(new Intent(this, Video.class)));

	    TabWidget tw = getTabWidget();
	    for (int i=0; i<tw.getChildCount(); i++) {
	        RelativeLayout relLayout = (RelativeLayout)tw.getChildAt(i);
	        TextView tv = (TextView)relLayout.getChildAt(1);
	        tv.setTextSize(12);
	        tv.setTypeface(Typeface.DEFAULT_BOLD);
	    }

	    int tab = 0;
	    try{
		    Bundle extras = getIntent().getExtras();
		    if(extras != null){
          if (extras.getBoolean("fromnotif"))
          {
            tab = 2;
            //Notifications.resetCounter();
            if(App.SDeditor != null){
              App.SDeditor.putInt("gamestype", 1);
              App.SDeditor.commit();
            }
          }
          else
          {
            tab = extras.getInt("tab", 0);
            if(tab != 0 && App.SDeditor != null){
              App.SDeditor.putInt("gamestype", 1);
              App.SDeditor.commit();
            }
          }
		    }
	    } catch (Exception e) {
			tab = 0;
		}

	    getTabHost().setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId)
			{
				//System.out.println("LCCLOG2: ONTABCHANGED");
				if (MobclixHelper.isShowAds(App))
				{
					if(tabId.equals("tab1") || tabId.equals("tab2") || tabId.equals("tab3") || tabId.equals("tab5") || tabId.equals("tab6"))
					{
						//System.out.println("LCCLOG2: ONTABCHANGED 1");
						MobclixHelper.showBannerAd(removeAds, Tabs.this, App);
					}
					else if (tabId.equals("tab4"))
					{
						//System.out.println("LCCLOG2: ONTABCHANGED 2");
						MobclixHelper.hideBannerAd(App, removeAds);
					}
					//System.out.println("LCCLOG2: ONTABCHANGED 3");
				}
				//System.out.println("LCCLOG2: ONTABCHANGED 4");
			}
		});

	    App.mTabHost.setCurrentTab(tab);
    }

	protected void onDestroy() {
		if (moPubAdView != null) {
			moPubAdView.destroy();
		}
		super.onDestroy();
	}

    @Override
    protected void onResume() {
		super.onResume();
		//System.out.println("LCCLOG2: TABS ONRESUME");
      if (MobclixHelper.isShowAds(App))
      {
		  //System.out.println("LCCLOG2: TABS ONRESUME 1");
    	  final String currentTab = getTabHost().getCurrentTabTag();
    	  if(currentTab.equals("tab1") || currentTab.equals("tab2") || currentTab.equals("tab3") || currentTab.equals("tab5") || currentTab.equals("tab6"))
			{
				//System.out.println("LCCLOG2: TABS ONRESUME 2");
				// integrate mopub, disable mobclix
				//MobclixHelper.showBannerAd(removeAds, this, App);
			}
			else if (currentTab.equals("tab4"))
			{
				//System.out.println("LCCLOG2: TABS ONRESUME 3");
				MobclixHelper.hideBannerAd(App, removeAds);
			}
		  //System.out.println("LCCLOG2: TABS ONRESUME 4");
      }
	  //System.out.println("LCCLOG2: TABS ONRESUME 5");
	  registerReceiver(lccLoggingInInfoReceiver, new IntentFilter("com.chess.lcc.android-logging-in-info"));
    }

    @Override
    protected void onPause()
	{
	  super.onPause();
      if (MobclixHelper.isShowAds(App))
	  {
        MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(App), App);
      }
      App.setForceBannerAdOnFailedLoad(false);
      unregisterReceiver(lccLoggingInInfoReceiver);
    }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
	  //System.out.println("LCCLOG2: TABS FOCUS 1");
    System.out.println("LCCLOG MOBCLIX: onWindowFocusChanged hasFocus=" + hasFocus + ", isForceBannerAdOnFailedLoad=" + App.isForceBannerAdOnFailedLoad());
    if (MobclixHelper.isShowAds(App) && hasFocus && App.isForceBannerAdOnFailedLoad())
    {
		//System.out.println("LCCLOG2: TABS FOCUS 2");
	  System.out.println("LCCLOG MOBCLIX: onWindowFocusChanged SHOW");
      MobclixHelper.showBannerAd(removeAds, this, App);
		//System.out.println("LCCLOG2: TABS FOCUS 3");
    }
  }

  private BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			new Handler().post(new Runnable() {
				public void run() {
					if (getTabHost().getCurrentTabTag().equals("tab2") && App.isLiveChess() && !intent.getExtras().getBoolean("enable"))
					{
						if (MobclixHelper.isShowAds(App) && App.getLccHolder().isConnected() && !App.getLccHolder().isConnectingInProgress())
						{
							//MobclixHelper.showBannerAd(removeAds, Tabs.this, App);
			            }
					}
				}
			});
		}
	};

	public void unregisterReceiver(BroadcastReceiver receiver)
	{
		try {
			super.unregisterReceiver(receiver);
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			// hack for Android's IllegalArgumentException: Receiver not registered
		}
	}
}
