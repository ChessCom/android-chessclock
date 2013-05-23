package com.chess.ui.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.articles.ArticlesFragment;
import com.chess.ui.fragments.daily.DailyTabsFragment;
import com.chess.ui.fragments.friends.FriendsFragment;
import com.chess.ui.fragments.game.GameTacticsFragment;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.fragments.live.GameLiveFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.fragments.settings.SettingsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
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

	private static final int UPGRADE_POS = 1;
	public static final int[] SELECTED_STATE = new int[]{android.R.attr.state_enabled, android.R.attr.state_checked};
	public static final int[] ENABLED_STATE = new int[]{android.R.attr.state_enabled};

	private ListView listView;
	private List<NavigationMenuItem> menuItems;
	private NewNavigationMenuAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<NavigationMenuItem>();
		menuItems.add(new NavigationMenuItem(getString(R.string.home), R.drawable.ic_nav_home));
		menuItems.add(new NavigationMenuItem(getString(R.string.upgrade), R.drawable.ic_nav_upgrade_shine));
		menuItems.add(new NavigationMenuItem(getString(R.string.play_daily), R.drawable.ic_nav_play_daily));
		menuItems.add(new NavigationMenuItem(getString(R.string.play_live), R.drawable.ic_nav_play_live));
		menuItems.add(new NavigationMenuItem(getString(R.string.tactics), R.drawable.ic_nav_tactics));
		menuItems.add(new NavigationMenuItem(getString(R.string.lessons), R.drawable.ic_nav_lessons));
		menuItems.add(new NavigationMenuItem(getString(R.string.videos), R.drawable.ic_nav_videos));
		menuItems.add(new NavigationMenuItem(getString(R.string.articles), R.drawable.ic_nav_articles));
		menuItems.add(new NavigationMenuItem(getString(R.string.stats), R.drawable.ic_nav_stats));
		menuItems.add(new NavigationMenuItem(getString(R.string.friends), R.drawable.ic_nav_friends));
		menuItems.add(new NavigationMenuItem(getString(R.string.messages), R.drawable.ic_nav_messages));
		menuItems.add(new NavigationMenuItem(getString(R.string.settings), R.drawable.ic_nav_settings));

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
	public void onResume() {
		super.onResume();
		logTest("left menu resumed");
	}

	@Override
	public void onPause() {
		super.onPause();

		logTest("left menu paused!");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		for (NavigationMenuItem menuItem : menuItems) {
			menuItem.selected = false;
		}

		menuItems.get(position).selected = true;
		NavigationMenuItem menuItem = (NavigationMenuItem) listView.getItemAtPosition(position);
		menuItem.selected = true;
		((BaseAdapter)parent.getAdapter()).notifyDataSetChanged();

		// TODO adjust switch/closeBoard when the same fragment opened
		switch (menuItem.iconRes) {
			case R.drawable.ic_nav_home: {
				getActivityFace().clearFragmentStack();
//				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(HomeTabsFragment.class.getSimpleName());
//				if(fragmentByTag == null) {
//					fragmentByTag = new HomeTabsFragment();
//				}
//				getActivityFace().switchFragment(fragmentByTag);
				getActivityFace().switchFragment(new HomeTabsFragment());
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_upgrade_shine:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(UpgradeFragment.class.getSimpleName());
				if(fragmentByTag == null) {
					fragmentByTag = new UpgradeFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_play_daily:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(DailyTabsFragment.class.getSimpleName());
				if(fragmentByTag == null) {
					fragmentByTag = new DailyTabsFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_play_live:{
				BasePopupsFragment liveFragment = (BasePopupsFragment) findFragmentByTag(GameLiveFragment.class.getSimpleName());
				if (liveFragment == null) {
					liveFragment = new LiveGameWaitFragment();
				}
				getActivityFace().openFragment(liveFragment);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_tactics:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(GameTacticsFragment.class.getSimpleName());
				if (fragmentByTag == null) {
					fragmentByTag = new GameTacticsFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_lessons:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(LessonsFragment.class.getSimpleName());
				if(fragmentByTag == null) {
					fragmentByTag = new LessonsFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_videos:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(VideosFragment.class.getSimpleName());
				if(fragmentByTag == null) {
					fragmentByTag = new VideosFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_articles:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(ArticlesFragment.class.getSimpleName());
				if(fragmentByTag == null) {
					fragmentByTag = new ArticlesFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_friends:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(FriendsFragment.class.getSimpleName());
				if(fragmentByTag == null) {
					fragmentByTag = new FriendsFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_stats:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(StatsGameFragment.class.getSimpleName());
				if(fragmentByTag == null) {
					fragmentByTag = new StatsGameFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
			case R.drawable.ic_nav_settings:{
				BasePopupsFragment fragmentByTag = (BasePopupsFragment) findFragmentByTag(SettingsFragment.class.getSimpleName());
				if(fragmentByTag == null) {
					fragmentByTag = new SettingsFragment();
				}
				getActivityFace().openFragment(fragmentByTag);
				getActivityFace().toggleLeftMenu();
				break;}
		}
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

		public NewNavigationMenuAdapter(Context context, List<NavigationMenuItem> menuItems) {
			super(context, menuItems);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_navigation_menu_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.icon = (ImageView) view.findViewById(R.id.iconImg);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);
			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(NavigationMenuItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.icon.setImageResource(item.iconRes);
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
			ImageView icon;
			TextView title;
		}
	}
}
