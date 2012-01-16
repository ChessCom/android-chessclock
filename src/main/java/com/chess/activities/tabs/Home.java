package com.chess.activities.tabs;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.chess.R;
import com.chess.activities.Preferences;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MobclixHelper;
import com.chess.views.HomeListItem;
import com.mobclix.android.sdk.MobclixFullScreenAdView;
import com.mobclix.android.sdk.MobclixFullScreenAdViewListener;

import java.util.ArrayList;
import java.util.List;

public class Home extends CoreActivity implements View.OnClickListener, OnItemClickListener {

	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.home_new);
		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		List<HomeListItem> itemList = new ArrayList<HomeListItem>();
		String[] homeItems = getResources().getStringArray(R.array.home_items);
		itemList.add(setHomeItem(homeItems[0], R.drawable.home_live));
		itemList.add(setHomeItem(homeItems[1], R.drawable.home_players));
		itemList.add(setHomeItem(homeItems[2], R.drawable.home_computer));
		itemList.add(setHomeItem(homeItems[3], R.drawable.home_tactics));
		itemList.add(setHomeItem(homeItems[4], R.drawable.home_video));
//		itemList.add("Online");
//		itemList.add("Computer");
//		itemList.add("Trainer");
//		itemList.add("Video");

		listView.setAdapter(new HomeListAdapter(itemList));
		listView.setOnItemClickListener(this);
//		if (App.guest)
//			setContentView(R.layout.home_guest);
//		else
//			setContentView(R.layout.home);

//
//		findViewById(R.id.logout).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (!App.guest) {
//					if (App.isLiveChess()/* && lccHolder.isConnected() */) {
//						lccHolder.logout();
//					}
//					App.SDeditor.putString("password", "");
//					App.SDeditor.putString("user_token", "");
//					App.SDeditor.commit();
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
		if (MobclixHelper.isShowAds(App)) {
			showFullscreenAd();
		}
		super.onResume();
	}

	private void showFullscreenAd() {
		if (!App.sharedData.getBoolean("com.chess.showedFullscreenAd", false) && MobclixHelper.isShowAds(App)) {
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
			App.SDeditor.putBoolean("com.chess.showedFullscreenAd", true);
			App.SDeditor.commit();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.settings) {
			startActivity(new Intent(Home.this, Preferences.class));
		} else if (v.getId() == R.id.upgrade) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als="
					+ App.sharedData.getString("user_token", "") + "&goto=http%3A%2F%2Fwww." + LccHolder.HOST
					+ "%2Fmembership.html?c=androidads")));
		} else if (v.getId() == R.id.help) {

		}
	}

	//	itemList.add("Live");
//	itemList.add("Online");
//	itemList.add("Computer");
//	itemList.add("Trainer");
//	itemList.add("Video");
	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
		App.mTabHost.setCurrentTab(position + 1);

//		findViewById(R.id.live).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				App.mTabHost.setCurrentTab(1);
//			}
//		});
//		findViewById(R.id.online).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				App.mTabHost.setCurrentTab(2);
//			}
//		});
//		findViewById(R.id.computer).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				App.mTabHost.setCurrentTab(3);
//			}
//		});
//		findViewById(R.id.tactics).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				App.mTabHost.setCurrentTab(4);
//			}
//		});
//		findViewById(R.id.video).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				App.mTabHost.setCurrentTab(5);
//			}
//		});
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
				convertView = Home.this.getLayoutInflater().inflate(R.layout.default_list_item, null, false);

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
				convertView.setBackgroundDrawable(res.getDrawable(R.drawable.rounded_button_one_selector));
			} else {
				if (position == 0) {
					convertView.setBackgroundDrawable(res.getDrawable(R.drawable.rounded_button_top_selector));
				} else if (position == getCount() - 1) {
					convertView.setBackgroundDrawable(res.getDrawable(R.drawable.rounded_button_bot_selector));
				} else {
					convertView.setBackgroundDrawable(res.getDrawable(R.drawable.rounded_button_mid_selector));
				}
			}
		}

		private class ViewHolder {
			ImageView image;
			TextView text;
		}
	}
}