package com.chess.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.TacticsDataHolder;
import com.chess.backend.entity.new_api.LoginItem;
import com.chess.backend.entity.new_api.RegisterItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.ui.fragments.WelcomeFourFragment;
import com.chess.ui.fragments.WelcomeOneFragment;
import com.chess.ui.fragments.WelcomeThreeFragment;
import com.chess.ui.fragments.WelcomeTwoFragment;
import com.chess.ui.views.drawables.LogoBackgroundDrawable;
import com.slidingmenu.lib.SlidingMenu;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends CommonLogicActivity {

	private static final long SPLASH_DELAY = 1000;
	private static final int PAGE_CNT = 4;
	private WelcomePagerAdapter mainPageAdapter;
	private RadioGroup homePageRadioGroup;
	private LayoutInflater inflater;
	private ViewPager viewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_screen);

		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

		LogoBackgroundDrawable logoBackgroundDrawable = new LogoBackgroundDrawable(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			findViewById(R.id.mainView).setBackground(logoBackgroundDrawable);
		} else {
			findViewById(R.id.mainView).setBackgroundDrawable(logoBackgroundDrawable);
		}

		inflater = LayoutInflater.from(this);

		viewPager = (ViewPager) findViewById(R.id.viewPager);
		mainPageAdapter = new WelcomePagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mainPageAdapter);
		viewPager.setOnPageChangeListener(pageChangeListener);

		homePageRadioGroup = (RadioGroup) findViewById(R.id.pagerIndicatorGroup);
		for (int i = 0; i < PAGE_CNT; ++i)
			inflater.inflate(R.layout.new_page_indicator_view, homePageRadioGroup, true);

		((RadioButton) homePageRadioGroup.getChildAt(0)).setChecked(true);

	}

	@Override
	protected void onResume() {
		super.onResume();

//		if (AppData.getUserToken(this).equals(StaticData.SYMBOL_EMPTY)) {
//			goToLoginScreen();
//		}
//
//		else { // validate credentials
//			LoadItem loadItem = new LoadItem();
//
//			loadItem.setLoadPath(RestHelper.CMD_LOGIN);
//			loadItem.setRequestMethod(RestHelper.POST);
//			loadItem.addRequestParams(RestHelper.P_USER_NAME_OR_MAIL, AppData.getUserName(this));
//			loadItem.addRequestParams(RestHelper.P_PASSWORD, AppData.getPassword(this));
//
//			new RequestJsonTask<LoginItem>(new LoginUpdateListenerNew()).executeTask(loadItem);
//		}
	}

	private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			((RadioButton) homePageRadioGroup.getChildAt(position)).setChecked(true);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}
	};

	private class WelcomePagerAdapter extends FragmentPagerAdapter {

		private List<Fragment> fragments;

		public WelcomePagerAdapter(FragmentManager fm) {
			super(fm);
			fragments = new ArrayList<Fragment>();
			fragments.add(new WelcomeOneFragment());
			fragments.add(new WelcomeTwoFragment());
			fragments.add(new WelcomeThreeFragment());
			fragments.add(new WelcomeFourFragment());
		}

		@Override
		public Fragment getItem(int i) {
			return fragments.get(i);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
	}

	private class LoginUpdateListenerNew extends AbstractUpdateListener<LoginItem> {
		public LoginUpdateListenerNew() {
			super(getContext(), LoginItem.class);
		}

//		@Override
//		public void showProgress(boolean show) {
//			if (show){
//				showPopupHardProgressDialog(R.string.signing_in_);
//			} else {
//				if(isPaused)
//					return;
//
//				dismissProgressDialog();
//			}
//		}

		@Override
		public void updateData(LoginItem returnedObj) {
			processLogin(returnedObj.getData());
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				// get server code
				int serverCode = RestHelper.decodeServerCode(resultCode);

				String serverMessage = ServerErrorCode.getUserFriendlyMessage(getContext(), serverCode); // TODO restore
				showToast(serverMessage);
			} else {
				goToLoginScreen();
			}
		}
	}

	protected void processLogin(RegisterItem.Data returnedObj) {
		try {
			preferencesEditor.putString(AppConstants.USER_TOKEN, URLEncoder.encode(returnedObj.getLogin_token(), HTTP.UTF_8));
		} catch (UnsupportedEncodingException ignored) {
			preferencesEditor.putString(AppConstants.USER_TOKEN, returnedObj.getLogin_token());
		}
		preferencesEditor.commit();

		AppData.setGuest(this, false);
		AppData.setLiveChessMode(this, false);
		DataHolder.reset();
		TacticsDataHolder.reset();

		registerGcmService();

		goToLoginScreen();
	}

	private void goToLoginScreen(){
		startActivity(new Intent(getContext(), NewLoginActivity.class));
	}
}
