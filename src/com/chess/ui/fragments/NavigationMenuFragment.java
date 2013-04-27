package com.chess.ui.fragments;

import android.content.Context;
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
import com.chess.ui.fragments.settings.SettingsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.fragments.videos.VideosFragment;
import com.slidingmenu.lib.SlidingMenu;

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

	private ListView listView;
	private List<NavigationMenuItem> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<NavigationMenuItem>();
		menuItems.add(new NavigationMenuItem(getString(R.string.home), R.drawable.ic_nav_home));
		menuItems.add(new NavigationMenuItem(getString(R.string.upgrade), R.drawable.ic_nav_upgrade));
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_navigation_menu_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		NewNavigationMenuAdapter adapter = new NewNavigationMenuAdapter(getActivity(), menuItems);

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
		((BaseAdapter)parent.getAdapter()).notifyDataSetChanged();

		// TODO adjust switch/closeBoard when the same fragment opened
		switch (menuItem.iconRes) {
			case R.drawable.ic_nav_home:
				getActivityFace().switchFragment(new HomeTabsFragment()); // TODO clear stack
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_upgrade:
				getActivityFace().openFragment(new UpgradeFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_play_daily:
				getActivityFace().openFragment(new DailyTabsFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_play_live:
				getActivityFace().openFragment(new GameLiveFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_tactics:
				getActivityFace().openFragment(new GameTacticsFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_lessons:
				getActivityFace().openFragment(new LessonsFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_videos:
				getActivityFace().openFragment(new VideosFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_articles:
				getActivityFace().openFragment(new ArticlesFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_friends:
				getActivityFace().openFragment(new FriendsFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_stats:
				getActivityFace().openFragment(StatsGameFragment.newInstance(0));
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
			case R.drawable.ic_nav_settings:
				getActivityFace().openFragment(new SettingsFragment());
				getActivityFace().toggleMenu(SlidingMenu.LEFT);
				break;
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
			return inflater.inflate(R.layout.new_navigation_menu_item, parent, false);
		}

		@Override
		protected void bindView(NavigationMenuItem item, int pos, View convertView) {
			ImageView icon = (ImageView) convertView.findViewById(R.id.iconImg);
			icon.setImageResource(item.iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.rowTitleTxt);
			title.setText(item.tag);

			if (pos == UPGRADE_POS){
				if (item.selected)
					convertView.setBackgroundResource(R.drawable.upgrade_menu_item_back_selected);
				else {
					convertView.setBackgroundResource(R.drawable.upgrade_menu_item_back_selector);
				}
			} else {
				icon.setBackgroundDrawable(null);
				if (item.selected) {
					convertView.setBackgroundResource(R.drawable.nav_menu_item_selected);
				} else {
					convertView.setBackgroundResource(R.drawable.nav_menu_item_selector);
				}
			}

		}

		public Context getContext() {
			return context;
		}
	}
}
