package com.chess.core;

import com.chess.R;
import com.chess.activities.Game;
import com.chess.activities.Register;
import com.chess.activities.tabs.Computer;
import com.chess.activities.tabs.Home;
import com.chess.activities.tabs.Online;
import com.chess.activities.tabs.Video;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MobclixHelper;
import com.chess.utilities.Notifications;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class Tabs extends TabActivity {

	public MainApp App;
	private TextView removeAds;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        
        if (MobclixHelper.isShowAds(App))
        {
          if (MobclixHelper.getBannerAdviewWrapper(App) == null || MobclixHelper.getBannerAdview(App) == null)
          {
            MobclixHelper.initializeBannerAdView(this, App);
          }
        }

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
            Notifications.resetCounter();
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
			public void onTabChanged(String tabId) {
				if(tabId.equals("tab1") || tabId.equals("tab2") || tabId.equals("tab6")) {
					
				}
			}
		});
	    
	    App.mTabHost.setCurrentTab(tab);
    }
	
    @Override
    protected void onResume() {
      if (MobclixHelper.isShowAds(App))
      {
        MobclixHelper.showBannerAd(MobclixHelper.getBannerAdviewWrapper(App), removeAds, this, App);
      }
      super.onResume();
    }

    @Override
    protected void onPause() {
      if (MobclixHelper.isShowAds(App)) {
        MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(App), App);
      }
      super.onPause();
    }
}
