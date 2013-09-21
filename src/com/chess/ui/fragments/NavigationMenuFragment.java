package com.chess.ui.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.statics.AppConstants;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.fragments.articles.ArticlesFragment;
import com.chess.ui.fragments.comp.GameCompFragment;
import com.chess.ui.fragments.daily.DailyTabsFragment;
import com.chess.ui.fragments.forums.ForumCategoriesFragment;
import com.chess.ui.fragments.friends.FriendsFragment;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.lessons.LessonsFragment;
import com.chess.ui.fragments.live.GameLiveFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.fragments.messages.MessagesInboxFragment;
import com.chess.ui.fragments.settings.SettingsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.fragments.tactics.GameTacticsFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.fragments.videos.VideosFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 11:04
 */
public class NavigationMenuFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final int HOME_POS = 0;

	public static final int[] SELECTED_STATE = new int[]{android.R.attr.state_enabled, android.R.attr.state_checked};
	public static final int[] ENABLED_STATE = new int[]{android.R.attr.state_enabled};

	private ListView listView;
	private List<NavigationMenuItem> menuItems;
	private NewNavigationMenuAdapter adapter;
	private int imageSize;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		imageSize = (int) (getResources().getDimension(R.dimen.nav_item_image_size) / getResources().getDisplayMetrics().density);

		menuItems = new ArrayList<NavigationMenuItem>();
		menuItems.add(new NavigationMenuItem(getString(R.string.home), R.drawable.ic_nav_home));
		menuItems.add(new NavigationMenuItem(getString(R.string.upgrade), R.drawable.ic_nav_upgrade_shine));
		menuItems.add(new NavigationMenuItem(getString(R.string.play_daily), R.drawable.ic_nav_play_daily));
		menuItems.add(new NavigationMenuItem(getString(R.string.play_live), R.drawable.ic_nav_play_live));
		menuItems.add(new NavigationMenuItem(getString(R.string.tactics), R.drawable.ic_nav_tactics));
		menuItems.add(new NavigationMenuItem(getString(R.string.lessons), R.drawable.ic_nav_lessons));
		menuItems.add(new NavigationMenuItem(getString(R.string.videos), R.drawable.ic_nav_videos));
		menuItems.add(new NavigationMenuItem(getString(R.string.articles), R.drawable.ic_nav_articles));
		menuItems.add(new NavigationMenuItem(getString(R.string.forums), R.drawable.ic_nav_forums));
		menuItems.add(new NavigationMenuItem(getString(R.string.stats), R.drawable.ic_nav_stats));
		menuItems.add(new NavigationMenuItem(getString(R.string.friends), R.drawable.ic_nav_friends));
		menuItems.add(new NavigationMenuItem(getString(R.string.messages), R.drawable.ic_nav_messages));
		menuItems.add(new NavigationMenuItem(getString(R.string.settings), R.drawable.ic_nav_settings));
		menuItems.add(new NavigationMenuItem(getString(R.string.vs_computer), R.drawable.ic_nav_vs_comp));

		menuItems.get(0).selected = true;
		adapter = new NewNavigationMenuAdapter(getActivity(), menuItems);
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
		listView.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		for (NavigationMenuItem menuItem : menuItems) {
			menuItem.selected = false;
		}

		menuItems.get(position).selected = true;
		NavigationMenuItem menuItem = (NavigationMenuItem) listView.getItemAtPosition(position);
		menuItem.selected = true;
		((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();

		BasePopupsFragment fragmentByTag = null;
		// TODO adjust switch/closeBoard when the same fragment opened
		switch (menuItem.iconRes) {
			case R.drawable.ic_nav_home:
				getActivityFace().clearFragmentStack();
				getActivityFace().switchFragment(new HomeTabsFragment());
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getActivityFace().toggleLeftMenu();
					}
				}, SIDE_MENU_DELAY);
				return;
			case R.drawable.ic_nav_upgrade_shine:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(UpgradeFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new UpgradeFragment();
				}
				break;
			case R.drawable.ic_nav_play_daily:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(DailyTabsFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new DailyTabsFragment();
				}
				break;
			case R.drawable.ic_nav_play_live:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(GameLiveFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = LiveGameWaitFragment.createInstance(getAppData().getLiveGameConfig());
				}
				break;
			case R.drawable.ic_nav_tactics:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(GameTacticsFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new GameTacticsFragment();
				}
				break;
			case R.drawable.ic_nav_lessons:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(LessonsFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new LessonsFragment();
				}
				break;
			case R.drawable.ic_nav_videos:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(VideosFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new VideosFragment();
				}
				break;
			case R.drawable.ic_nav_articles:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(ArticlesFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new ArticlesFragment();
				}
				break;
			case R.drawable.ic_nav_friends:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(FriendsFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new FriendsFragment();
				}
				break;
			case R.drawable.ic_nav_messages:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(MessagesInboxFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new MessagesInboxFragment();
				}
				break;
			case R.drawable.ic_nav_stats:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(StatsGameFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new StatsGameFragment();
				}
				break;
			case R.drawable.ic_nav_settings:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(SettingsFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new SettingsFragment();
				}
				break;
			case R.drawable.ic_nav_forums:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(ForumCategoriesFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new ForumCategoriesFragment();
				}
				break;
			case R.drawable.ic_nav_vs_comp:
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(GameCompFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = prepareGameCompFragmentInstance();
				}
				break;
		}
		if (fragmentByTag != null) {
			getActivityFace().openFragment(fragmentByTag);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					getActivityFace().toggleLeftMenu();
				}
			}, SIDE_MENU_DELAY);
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

	private class NewNavigationMenuAdapter extends ItemsAdapter<NavigationMenuItem> {

		private final String userAvatarUrl;
		private final String themeName;

		public NewNavigationMenuAdapter(Context context, List<NavigationMenuItem> menuItems) {
			super(context, menuItems);
			userAvatarUrl = getAppData().getUserAvatar();
			themeName = getAppData().getThemeName();
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_navigation_menu_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.icon = (ProgressImageView) view.findViewById(R.id.iconImg);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);

			if (themeName.equals(AppConstants.LIGHT_THEME_NAME)) {
				holder.title.setTextColor(resources.getColor(R.color.transparent_button_border_top));
			}
			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(NavigationMenuItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();
			if (pos == HOME_POS) {
				imageLoader.download(userAvatarUrl, holder.icon, imageSize);
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
		}

		public Context getContext() {
			return context;
		}

		public class ViewHolder {
			ProgressImageView icon;
			//			ImageView icon;
			TextView title;
		}
	}
}
