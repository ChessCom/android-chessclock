package com.chess.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
public class MainMenuFragment extends CommonLogicFragment {
	private ListView listView;
	private List<MenuItem> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<MenuItem>();
		menuItems.add(new MenuItem(getString(R.string.menuLive), R.drawable.dashboard_play_live_selector));
		menuItems.add(new MenuItem(getString(R.string.menuOnline), R.drawable.dashboard_play_online_selector));
		menuItems.add(new MenuItem(getString(R.string.menuComp), R.drawable.dashboard_play_comp_selector));
		menuItems.add(new MenuItem(getString(R.string.menuTactics), R.drawable.dashboard_tactics_selector));
		menuItems.add(new MenuItem(getString(R.string.menuLive), R.drawable.dashboard_video_selector));
		menuItems.add(new MenuItem(getString(R.string.settings), R.drawable.dashboard_settings_selector));
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_main_menu_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listView = (ListView) view.findViewById(R.id.listView);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity(), menuItems);

		listView.setAdapter(adapter);
	}



	private class MenuItem {
		public String tag;
		public int iconRes;
		public MenuItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	public class SampleAdapter extends ItemsAdapter<MenuItem> {

		public SampleAdapter(Context context, List<MenuItem> menuItems) {
			super(context, menuItems);
		}

		@Override
		protected View createView(ViewGroup parent) {
			return LayoutInflater.from(getContext()).inflate(R.layout.slide_menu_row, null);
		}

		@Override
		protected void bindView(MenuItem item, int pos, View convertView) {
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(item.iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(item.tag);
		}
	}
}
