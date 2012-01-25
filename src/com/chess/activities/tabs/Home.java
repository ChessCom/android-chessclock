package com.chess.activities.tabs;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chess.R;
import com.chess.activities.Preferences;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MobclixHelper;
import com.chess.views.BackgroundChessDrawable;
import com.chess.views.HomeListItem;
import com.mobclix.android.sdk.MobclixFullScreenAdView;
import com.mobclix.android.sdk.MobclixFullScreenAdViewListener;

public class Home extends CoreActivity implements View.OnClickListener, OnItemClickListener {

	private ListView listView;
	private LayoutInflater inflater;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_new);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		inflater = LayoutInflater.from(this);

		listView = (ListView) findViewById(R.id.listView);

		List<HomeListItem> itemList = new ArrayList<HomeListItem>();
		String[] homeItems = getResources().getStringArray(R.array.home_items);
		itemList.add(setHomeItem(homeItems[0], R.drawable.home_live));
		itemList.add(setHomeItem(homeItems[1], R.drawable.home_players));
		itemList.add(setHomeItem(homeItems[2], R.drawable.home_computer));
		itemList.add(setHomeItem(homeItems[3], R.drawable.home_tactics));
		itemList.add(setHomeItem(homeItems[4], R.drawable.home_video));

		listView.setAdapter(new HomeListAdapter(itemList));
		listView.setOnItemClickListener(this);

//
//		findViewById(R.id.logout).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (!mainApp.guest) {
//					if (mainApp.isLiveChess()/* && lccHolder.isConnected() */) {
//						lccHolder.logout();
//					}
//					mainApp.getSharedDataEditor().putString("password", "");
//					mainApp.getSharedDataEditor().putString("user_token", "");
//					mainApp.getSharedDataEditor().commit();
//				}
//				startActivity(new Intent(Home.this, Singin.class));
//				finish();
//			}
//		});		

		findViewById(R.id.settings).setOnClickListener(this);
		findViewById(R.id.upgrade).setOnClickListener(this);
		findViewById(R.id.help).setOnClickListener(this);
	}

	private HomeListItem setHomeItem(String text, int imageId) {
		HomeListItem homeItem = new HomeListItem();
		homeItem.setImage(getResources().getDrawable(imageId));
		homeItem.setText(text);
		return homeItem;
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
		if (MobclixHelper.isShowAds(mainApp)) {
			showFullscreenAd();
		}
		super.onResume();
	}

	private void showFullscreenAd() {
		if (!mainApp.getSharedData().getBoolean("com.chess.showedFullscreenAd", false) && MobclixHelper.isShowAds(mainApp)) {
			MobclixFullScreenAdView fsAdView = new MobclixFullScreenAdView(this);
			fsAdView.addMobclixAdViewListener(new MobclixFullScreenAdViewListener() {

				@Override
				public String query() {
					return null;
				}

				@Override
				public void onPresentAd(MobclixFullScreenAdView arg0) {
					System.out.println("mobclix fullscreen onPresentAd");

				}

				@Override
				public void onFinishLoad(MobclixFullScreenAdView arg0) {
					System.out.println("mobclix fullscreen onFinishLoad");

				}

				@Override
				public void onFailedLoad(MobclixFullScreenAdView adView, int errorCode) {
					System.out.println("mobclix fullscreen onFailedLoad errorCode=" + errorCode);
				}

				@Override
				public void onDismissAd(MobclixFullScreenAdView arg0) {
					System.out.println("mobclix fullscreen onDismissAd");
				}

				@Override
				public String keywords() {
					return null;
				}
			});
			fsAdView.requestAndDisplayAd();

			// MoPubInterstitial interstitial = new MoPubInterstitial(this,
// "agltb3B1Yi1pbmNyDQsSBFNpdGUYioOrAgw");
			/*
			 * MoPubInterstitial interstitial = new MoPubInterstitial(this,
			 * "agltb3B1Yi1pbmNyDAsSBFNpdGUYsckMDA"); // test
			 * interstitial.showAd();
			 */
			mainApp.getSharedDataEditor().putBoolean("com.chess.showedFullscreenAd", true);
			mainApp.getSharedDataEditor().commit();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.settings) {
			startActivity(new Intent(Home.this, Preferences.class));
		} else if (v.getId() == R.id.upgrade) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als="
					+ mainApp.getSharedData().getString("user_token", "") + "&goto=http%3A%2F%2Fwww." + LccHolder.HOST
					+ "%2Fmembership.html?c=androidads")));
		} else if (v.getId() == R.id.help) {
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
		mainApp.getTabHost().setCurrentTab(position + 1);
	}

	private class HomeListAdapter extends BaseAdapter {

		private final List<HomeListItem> itemList;
		private final Resources res;

		public HomeListAdapter(List<HomeListItem> itemList) {
			this.itemList = itemList;
			res = getResources();
		}

		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public Object getItem(int position) {
			return itemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.default_list_item, parent, false);

				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			setItemBackground(convertView, position);
			holder.image.setImageDrawable(itemList.get(position).getImage());
			holder.text.setText(itemList.get(position).getText());
			return convertView;
		}

		private void setItemBackground(View convertView, int position) {
			if (getCount() == 1) {
				convertView.setBackgroundResource(R.drawable.rounded_button_one_selector);
			} else {
				if (position == 0) {
					convertView.setBackgroundResource(R.drawable.rounded_button_top_selector);
				} else if (position == getCount() - 1) {
					convertView.setBackgroundResource(R.drawable.rounded_button_bot_selector);
				} else {
					convertView.setBackgroundResource(R.drawable.rounded_button_mid_selector);
				}
			}
		}

		private class ViewHolder {
			ImageView image;
			TextView text;
		}
	}
}