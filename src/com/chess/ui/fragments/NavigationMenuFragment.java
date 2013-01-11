package com.chess.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.adapters.ItemsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 11:04
 */
public class NavigationMenuFragment extends CommonLogicFragment {
	private ListView listView;
	private List<NavigationMenuItem> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<NavigationMenuItem>();
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_upgrade), R.drawable.ic_nav_upgrade));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_home), R.drawable.ic_nav_home));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_play), R.drawable.ic_nav_play));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_tactics), R.drawable.ic_nav_tactics));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_videos), R.drawable.ic_nav_videos));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_articles), R.drawable.ic_nav_articles));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_stats), R.drawable.ic_nav_stats));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_friends), R.drawable.ic_nav_friends));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_messages), R.drawable.ic_nav_messages));
		menuItems.add(new NavigationMenuItem(getString(R.string.nav_settings), R.drawable.ic_nav_settings));

	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_navigation_menu_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listView = (ListView) view.findViewById(R.id.listView);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		NewNavigationMenuAdapter adapter = new NewNavigationMenuAdapter(getActivity(), menuItems);

		listView.setAdapter(adapter);
	}


	private class NavigationMenuItem {
		public String tag;
		public int iconRes;

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
			return LayoutInflater.from(getContext()).inflate(R.layout.new_navigation_menu_item, null, false);
		}

		@Override
		protected void bindView(NavigationMenuItem item, int pos, View convertView) {
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(item.iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(item.tag);
		}

		public Context getContext() {
			return context;
		}
	}
}
