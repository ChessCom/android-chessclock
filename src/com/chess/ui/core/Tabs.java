package com.chess.ui.core;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MobclixHelper;
import com.chess.utilities.Notifications;
import com.mobclix.android.sdk.Mobclix;
import com.mopub.mobileads.MoPubView;

@Deprecated
public class Tabs extends TabActivity implements OnClickListener {

	public MainApp mainApp;
	private Button upgradeBtn;
	private MoPubView moPubAdView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mobclix.onCreate(this);
		mainApp = (MainApp) getApplication();

		//get global Shared Preferences
		if (mainApp.getSharedData() == null) {
			mainApp.setSharedData(getSharedPreferences("sharedData", 0));
			mainApp.setSharedDataEditor(mainApp.getSharedData().edit());
		}

		setContentView(R.layout.tabs);

		upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		// integrate mopub, disable mobclix
		/*if (MobclixHelper.isShowAds(mainApp))
				{
				  if (MobclixHelper.getBannerAdviewWrapper(mainApp) == null || MobclixHelper.getBannerAdview(mainApp) == null)
				  {
					MobclixHelper.initializeBannerAdView(this, mainApp);
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

		// TODO check logic in HomeScreenActivity class
//		mainApp.setTabHost(getTabHost());
//		getTabHost().addTab(getTabHost().newTabSpec("tab1")
//				.setIndicator(getString(R.string.home), getResources().getDrawable(R.drawable.home))
//				.setContent(new Intent(this, Home.class)));
//		if (mainApp.guest) {
//			getTabHost().addTab(getTabHost().newTabSpec("tab2")
//					.setIndicator(getString(R.string.live), getResources().getDrawable(R.drawable.live))
//					.setContent(new Intent(this, Register.class).putExtra(AppConstants.LIVE_CHESS, true)));
//			getTabHost().addTab(getTabHost().newTabSpec("tab6")
//					.setIndicator(getString(R.string.online), getResources().getDrawable(R.drawable.online))
//					.setContent(new Intent(this, Register.class).putExtra(AppConstants.LIVE_CHESS, false)));
//		} else {
//			getTabHost().addTab(getTabHost().newTabSpec("tab2")
//					.setIndicator(getString(R.string.live), getResources().getDrawable(R.drawable.live))
//					.setContent(new Intent(this, Online.class).putExtra(AppConstants.LIVE_CHESS, true)));
//			getTabHost().addTab(getTabHost().newTabSpec("tab6")
//					.setIndicator(getString(R.string.online), getResources().getDrawable(R.drawable.online))
//					.setContent(new Intent(this, Online.class).putExtra(AppConstants.LIVE_CHESS, false)));
//		}
//
//		getTabHost().addTab(getTabHost().newTabSpec("tab3")
//				.setIndicator(getString(R.string.comp), getResources().getDrawable(R.drawable.computer))
//				.setContent(new Intent(this, Computer.class)));
//		getTabHost().addTab(getTabHost().newTabSpec("tab4")
//				.setIndicator(getString(R.string.tactics), getResources().getDrawable(R.drawable.tactics))
//				.setContent(new Intent(this, Game.class)
//						.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_TACTICS)
//						.putExtra(AppConstants.LIVE_CHESS, false)));
//		getTabHost().addTab(getTabHost().newTabSpec("tab5")
//				.setIndicator(getString(R.string.video), getResources().getDrawable(R.drawable.video))
//				.setContent(new Intent(this, Video.class)));

		TabWidget tw = getTabWidget();
		for (int i = 0; i < tw.getChildCount(); i++) {
			RelativeLayout relLayout = (RelativeLayout) tw.getChildAt(i);
			TextView tv = (TextView) relLayout.getChildAt(1);
			tv.setTextSize(12);
			tv.setTypeface(Typeface.DEFAULT_BOLD);
		}

		int tab = 0;
		try {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				if (extras.getBoolean(AppConstants.ENTER_FROM_NOTIFICATION)) {
					tab = 2;
					Notifications.resetCounter();
					if (mainApp.getSharedDataEditor() != null) {
						mainApp.getSharedDataEditor().putInt(AppConstants.ONLINE_GAME_LIST_TYPE, 1);
						mainApp.getSharedDataEditor().commit();
					}
				} else {
					tab = extras.getInt(AppConstants.TAB_INDEX, 0);
					if (tab != 0 && mainApp.getSharedDataEditor() != null) {
						mainApp.getSharedDataEditor().putInt(AppConstants.ONLINE_GAME_LIST_TYPE, 1);
						mainApp.getSharedDataEditor().commit();
					}
				}
			}
		} catch (Exception e) {
			tab = 0;
		}

		getTabHost().setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				//System.out.println("LCCLOG2: ONTABCHANGED");
				if (MobclixHelper.isShowAds(mainApp)) {
					if (tabId.equals("tab1") || tabId.equals("tab2")
							|| tabId.equals("tab3") || tabId.equals("tab5") || tabId.equals("tab6")) {
						//System.out.println("LCCLOG2: ONTABCHANGED 1");
						MobclixHelper.showBannerAd(upgradeBtn, Tabs.this, mainApp);
					} else if (tabId.equals("tab4")) {
						//System.out.println("LCCLOG2: ONTABCHANGED 2");
						MobclixHelper.hideBannerAd(mainApp, upgradeBtn);
					}
				}
			}
		});

		getTabHost().setCurrentTab(tab);
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
		if (MobclixHelper.isShowAds(mainApp)) {
			//System.out.println("LCCLOG2: TABS ONRESUME 1");
			final String currentTab = getTabHost().getCurrentTabTag();
			if (currentTab.equals("tab1") || currentTab.equals("tab2") || currentTab.equals("tab3") || currentTab.equals("tab5") || currentTab.equals("tab6")) {
				//System.out.println("LCCLOG2: TABS ONRESUME 2");
				// integrate mopub, disable mobclix
				//MobclixHelper.showBannerAd(upgradeBtn, this, mainApp);
			} else if (currentTab.equals("tab4")) {
				//System.out.println("LCCLOG2: TABS ONRESUME 3");
				MobclixHelper.hideBannerAd(mainApp, upgradeBtn);
			}
			//System.out.println("LCCLOG2: TABS ONRESUME 4");
		}
		//System.out.println("LCCLOG2: TABS ONRESUME 5");
		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (MobclixHelper.isShowAds(mainApp)) {
			MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
		}
		mainApp.setForceBannerAdOnFailedLoad(false);
		unregisterReceiver(lccLoggingInInfoReceiver);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		//System.out.println("LCCLOG2: TABS FOCUS 1");
		System.out.println("LCCLOG MOBCLIX: onWindowFocusChanged hasFocus=" + hasFocus + ", isForceBannerAdOnFailedLoad=" + mainApp.isForceBannerAdOnFailedLoad());
		if (MobclixHelper.isShowAds(mainApp) && hasFocus && mainApp.isForceBannerAdOnFailedLoad()) {
			//System.out.println("LCCLOG2: TABS FOCUS 2");
			System.out.println("LCCLOG MOBCLIX: onWindowFocusChanged SHOW");
			MobclixHelper.showBannerAd(upgradeBtn, this, mainApp);
			//System.out.println("LCCLOG2: TABS FOCUS 3");
		}
	}

	private BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			new Handler().post(new Runnable() {
				public void run() {
					if (getTabHost().getCurrentTabTag().equals("tab2") && mainApp.isLiveChess() && !intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR)) {
						if (MobclixHelper.isShowAds(mainApp) && mainApp.getLccHolder().isConnected() && !mainApp.getLccHolder().isConnectingInProgress()) {
//							MobclixHelper.showBannerAd( upgradeBtn, Tabs.this, mainApp);
						}
					}
				}
			});
		}
	};

	public void unregisterReceiver(BroadcastReceiver receiver) {
		try {
			super.unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// hack for Android's IllegalArgumentException: Receiver not registered
		}
	}


	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
					"http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") +
							"&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html?c=androidads")));

		}
	}
}
