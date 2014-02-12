package com.chess.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.statics.AppConstants;
import com.chess.statics.IntentConstants;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.fragments.articles.ArticlesFragment;
import com.chess.ui.fragments.articles.ArticlesFragmentTablet;
import com.chess.ui.fragments.comp.GameCompFragment;
import com.chess.ui.fragments.daily.DailyHomeFragment;
import com.chess.ui.fragments.daily.DailyHomeFragmentTablet;
import com.chess.ui.fragments.forums.ForumCategoriesFragment;
import com.chess.ui.fragments.forums.ForumCategoriesFragmentTablet;
import com.chess.ui.fragments.friends.FriendsFragment;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.lessons.LessonsFragment;
import com.chess.ui.fragments.lessons.LessonsFragmentTablet;
import com.chess.ui.fragments.live.LiveHomeFragment;
import com.chess.ui.fragments.live.LiveHomeFragmentTablet;
import com.chess.ui.fragments.messages.MessagesFragmentTablet;
import com.chess.ui.fragments.messages.MessagesInboxFragment;
import com.chess.ui.fragments.profiles.ProfileBaseFragmentTablet;
import com.chess.ui.fragments.settings.SettingsFragment;
import com.chess.ui.fragments.settings.SettingsFragmentTablet;
import com.chess.ui.fragments.stats.StatsBasicFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.fragments.tactics.GameTacticsFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragmentTablet;
import com.chess.ui.fragments.videos.VideosFragment;
import com.chess.ui.fragments.videos.VideosFragmentTablet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 11:04
 */
public class LeftNavigationFragment extends LiveBaseFragment implements AdapterView.OnItemClickListener {

	private static final int HOME_POS = 0;

	public static final int[] SELECTED_TITLE_STATE = new int[]{android.R.attr.state_selected};
	public static final int[] SELECTED_STATE = new int[]{android.R.attr.state_enabled, android.R.attr.state_checked};
	public static final int[] ENABLED_STATE = new int[]{android.R.attr.state_enabled};

	private ListView listView;
	private List<NavigationMenuItem> menuItems;
	private NavigationMenuAdapter navigationAdapter;
	private int imageSize;
	private IntentFilter fontsUpdateFilter;
	private FontsUpdateReceiver fontsUpdateReceiver;
	private boolean updateFonts;
	private int previousPosition;
	private SparseArray<String> selectedPositionsMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		imageSize = getResources().getDimensionPixelSize(R.dimen.nav_item_image_size);

		selectedPositionsMap = new SparseArray<String>();

		menuItems = new ArrayList<NavigationMenuItem>();
		menuItems.add(new NavigationMenuItem(getString(R.string.home), R.drawable.ic_nav_home));
		if (isNeedToUpgrade()) {
			menuItems.add(new NavigationMenuItem(getString(R.string.upgrade), R.drawable.ic_nav_upgrade_shine));
		}
		menuItems.add(new NavigationMenuItem(getString(R.string.play_live), R.drawable.ic_nav_play_live));
		menuItems.add(new NavigationMenuItem(getString(R.string.daily_chess), R.drawable.ic_nav_play_daily));
		menuItems.add(new NavigationMenuItem(getString(R.string.computer), R.drawable.ic_nav_vs_comp));
//		menuItems.add(new NavigationMenuItem("ThemesManager", R.drawable.ic_nav_badge));
		menuItems.add(new NavigationMenuItem(getString(R.string.tactics), R.drawable.ic_nav_tactics));
		menuItems.add(new NavigationMenuItem(getString(R.string.lessons), R.drawable.ic_nav_lessons));
		menuItems.add(new NavigationMenuItem(getString(R.string.videos), R.drawable.ic_nav_videos));
		menuItems.add(new NavigationMenuItem(getString(R.string.articles), R.drawable.ic_nav_articles));
		menuItems.add(new NavigationMenuItem(getString(R.string.forums), R.drawable.ic_nav_forums));
		menuItems.add(new NavigationMenuItem(getString(R.string.friends), R.drawable.ic_nav_friends));
		menuItems.add(new NavigationMenuItem(getString(R.string.stats), R.drawable.ic_nav_stats));
		menuItems.add(new NavigationMenuItem(getString(R.string.messages), R.drawable.ic_nav_messages));
		if (isNeedToUpgradePremium() && !isNeedToUpgrade()) {
			menuItems.add(new NavigationMenuItem(getString(R.string.upgrade), R.drawable.ic_nav_upgrade_shine));
		}
		menuItems.add(new NavigationMenuItem(getString(R.string.settings), R.drawable.ic_nav_settings));

		menuItems.get(0).selected = true;
		getImageFetcher().setLoadingImage(R.drawable.empty);
		navigationAdapter = new NavigationMenuAdapter(getActivity(), menuItems, getImageFetcher());
		fontsUpdateFilter = new IntentFilter(IntentConstants.BACKGROUND_LOADED);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setAdapter(navigationAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();

		updateSelectedMenuItem(previousPosition);

		fontsUpdateReceiver = new FontsUpdateReceiver();
		registerReceiver(fontsUpdateReceiver, fontsUpdateFilter);
	}

	@Override
	public void onPause() {
		super.onPause();

		unRegisterMyReceiver(fontsUpdateReceiver);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// Comment out until invent correct logic of collapsing fragments by category.
		// For example Articles -> ArticlesCategories -> ArticlesDetails.
		// Tapping on Articles should open home articles fragment(ArticlesFragment)

//		if (position == previousPosition) { // don't add the same fragment
//			getActivityFace().toggleLeftMenu();
//			return;
//		}

		previousPosition = position;

		// update selected state flag
		NavigationMenuItem menuItem = updateSelectedMenuItem(position);

		BasePopupsFragment fragmentByTag = null;
		
		switch (menuItem.iconRes) {
			case R.drawable.ic_nav_home:
				backToHomeFragment();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getActivityFace().toggleLeftMenu();
					}
				}, SIDE_MENU_DELAY);
				return;
			case R.drawable.ic_nav_upgrade_shine:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(UpgradeFragment.class.getSimpleName());
				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(UpgradeFragmentTablet.class.getSimpleName());
				}

				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new UpgradeFragment();
					} else {
						fragmentByTag = new UpgradeFragmentTablet();
					}
				}
				break;
//			case R.drawable.ic_nav_badge: // use to update themes resources // TODO rename to another icon usage if needed
//				fragmentByTag = (BasePopupsFragment) findFragmentByTag(ThemeManagerFragment.class.getSimpleName());
//				if (fragmentByTag == null) {
//					fragmentByTag = new ThemeManagerFragment();
//				}
//				break;
			case R.drawable.ic_nav_play_daily:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(DailyHomeFragment.class.getSimpleName());
				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(DailyHomeFragmentTablet.class.getSimpleName());
				}
				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new DailyHomeFragment();
					} else {
						fragmentByTag = new DailyHomeFragmentTablet();
					}
				}
				break;
			case R.drawable.ic_nav_play_live:

				fragmentByTag = ((LiveBaseActivity) getActivity()).getLiveHomeFragment();
				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new LiveHomeFragment();
					} else {
						fragmentByTag = new LiveHomeFragmentTablet();
					}
				}
				break;
			case R.drawable.ic_nav_vs_comp:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(GameCompFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = prepareGameCompFragmentInstance();
				}
				break;
			case R.drawable.ic_nav_tactics:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(GameTacticsFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new GameTacticsFragment();
				}
				break;
			case R.drawable.ic_nav_lessons:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(LessonsFragment.class.getSimpleName());
				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(LessonsFragmentTablet.class.getSimpleName());
				}

				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new LessonsFragment();
					} else {
						fragmentByTag = new LessonsFragmentTablet();
					}
				}
				break;
			case R.drawable.ic_nav_videos:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(VideosFragment.class.getSimpleName());
				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(VideosFragmentTablet.class.getSimpleName());
				}

				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new VideosFragment();
					} else {
						fragmentByTag = new VideosFragmentTablet();
					}
				}
				break;
			case R.drawable.ic_nav_articles:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(ArticlesFragment.class.getSimpleName());
				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(ArticlesFragmentTablet.class.getSimpleName());
				}
				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new ArticlesFragment();
					} else {
						fragmentByTag = new ArticlesFragmentTablet();
					}
				}
				break;
			case R.drawable.ic_nav_friends:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(FriendsFragment.class.getSimpleName());
				}

				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new FriendsFragment();
					} else {
						fragmentByTag = new ProfileBaseFragmentTablet();
					}
				}
				break;
			case R.drawable.ic_nav_messages:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(MessagesInboxFragment.class.getSimpleName());
				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(MessagesFragmentTablet.class.getSimpleName());
				}
				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new MessagesInboxFragment();
					} else {
						fragmentByTag = new MessagesFragmentTablet();
					}
				}
				break;
			case R.drawable.ic_nav_stats:
				if (!isTablet) {
					if (isNeedToUpgrade()) {
						fragmentByTag = (BasePopupsFragment) findFragmentByTag(StatsBasicFragment.class.getSimpleName());
					} else {
						fragmentByTag = (BasePopupsFragment) findFragmentByTag(StatsGameFragment.class.getSimpleName());
					}
				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(ProfileBaseFragmentTablet.class.getSimpleName());
				}

				if (fragmentByTag == null) {
					if (!isTablet) {
						if (isNeedToUpgrade()) {
							fragmentByTag = new StatsBasicFragment();
						} else {
							fragmentByTag = new StatsGameFragment();
						}
					} else {
						fragmentByTag = ProfileBaseFragmentTablet.createInstance(ProfileBaseFragmentTablet.STATS_MODE, getUsername());
					}
				} else {
					if (!isTablet) {
						if (!isNeedToUpgrade()) {
							((StatsGameFragment) fragmentByTag).updateUsername(getUsername());
						}
					} else {
						fragmentByTag = ProfileBaseFragmentTablet.createInstance(ProfileBaseFragmentTablet.STATS_MODE, getUsername());
					}
				}
				break;
			case R.drawable.ic_nav_forums:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(ForumCategoriesFragment.class.getSimpleName());

				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(ForumCategoriesFragmentTablet.class.getSimpleName());
				}
				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new ForumCategoriesFragment();
					} else {
						fragmentByTag = new ForumCategoriesFragmentTablet();
					}

				}
				break;
			case R.drawable.ic_nav_settings:
				if (!isTablet) {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(SettingsFragment.class.getSimpleName());
				} else {
					fragmentByTag = (BasePopupsFragment) findFragmentByTag(SettingsFragmentTablet.class.getSimpleName());
				}
				if (fragmentByTag == null) {
					if (!isTablet) {
						fragmentByTag = new SettingsFragment();
					} else {
						fragmentByTag = new SettingsFragmentTablet();
					}
				}
				break;
		}
		if (fragmentByTag != null) {
			// save fragmentName and position to proper highlight row when navigating back
			selectedPositionsMap.put(position, fragmentByTag.getClass().getSimpleName());
			
			getActivityFace().openFragment(fragmentByTag);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					getActivityFace().toggleLeftMenu();
				}
			}, SIDE_MENU_DELAY);
		}
	}

	private NavigationMenuItem updateSelectedMenuItem(int position) {
		for (NavigationMenuItem menuItem : menuItems) {
			menuItem.selected = false;
		}

		menuItems.get(position).selected = true;
		NavigationMenuItem menuItem = (NavigationMenuItem) listView.getItemAtPosition(position);
		menuItem.selected = true;
		navigationAdapter.notifyDataSetChanged();
		return menuItem;
	}

	public void onOpened() {
		Fragment fragment = getActivityFace().getCurrentActiveFragment();
		if (fragment != null) {
			String fragmentName = fragment.getClass().getSimpleName();
			int positionToSelect = previousPosition;
			for (int i=0; i< selectedPositionsMap.size(); i++) {
				String selectedFragmentName = selectedPositionsMap.valueAt(i);
				if (fragmentName.equals(selectedFragmentName) ){
					positionToSelect = selectedPositionsMap.keyAt(i);
				}
			}

			if (positionToSelect != previousPosition) {
				previousPosition = positionToSelect;
				updateSelectedMenuItem(previousPosition);
			}
		}

	}

	private GameCompFragment prepareGameCompFragmentInstance() {
		int compGameMode = getAppData().getCompGameMode();
		if (compGameMode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) { // replace this fast speed fun
			compGameMode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE;
			getAppData().setCompGameMode(compGameMode);
		}
		CompGameConfig.Builder builder = new CompGameConfig.Builder()
				.setMode(compGameMode)
				.setStrength(getAppData().getCompLevel());

		return GameCompFragment.createInstance(builder.build());
	}

	private class NavigationMenuItem {
		public String tag;
		public int iconRes;
		public boolean selected;

		public NavigationMenuItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	private class NavigationMenuAdapter extends ItemsAdapter<NavigationMenuItem> {

		private final String userAvatarUrl;
		private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

		public NavigationMenuAdapter(Context context, List<NavigationMenuItem> menuItems, SmartImageFetcher imageFetcher) {
			super(context, menuItems, imageFetcher);
			userAvatarUrl = getAppData().getUserAvatar();
			imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_navigation_menu_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.icon = (ProgressImageView) view.findViewById(R.id.iconImg);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);

			holder.title.setTextColor(themeFontColorStateList);
			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(NavigationMenuItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();
			if (pos == HOME_POS) {

				if (!imageDataMap.containsKey(userAvatarUrl)) {
					imageDataMap.put(userAvatarUrl, new SmartImageFetcher.Data(userAvatarUrl, imageSize));
				}

				imageFetcher.loadImage(imageDataMap.get(userAvatarUrl), holder.icon.getImageView());
			} else {
				holder.icon.setImageDrawable(resources.getDrawable(item.iconRes));
			}

			holder.title.setText(item.tag);


			Drawable background = view.getBackground();
			if (item.selected) {
				background.mutate().setState(SELECTED_STATE);
			} else {
				background.mutate().setState(ENABLED_STATE);
			}

			if (updateFonts) {
				holder.title.setTextColor(themeFontColorStateList); // need to update all views as they are not replacing each other in big list
			}
		}

		public Context getContext() {
			return context;
		}

		public class ViewHolder {
			ProgressImageView icon;
			TextView title;
		}
	}

	private class FontsUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			navigationAdapter.notifyDataSetChanged();
			updateFonts = true;
		}
	}
}
