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
 * Date: 30.12.12
 * Time: 22:04
 */
public class HomeGamesFragment extends CommonLogicFragment {

	private ListView listView;
	private List<NavigationMenuItem> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<NavigationMenuItem>();
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
		menuItems.add(new NavigationMenuItem("2 hours", "Player", R.drawable.img_profile_picture_stub));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView) view.findViewById(R.id.listView);
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivityFace().setBadgeValueForId(R.id.menu_games, 0);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		NewNavigationMenuAdapter adapter = new NewNavigationMenuAdapter(getActivity(), menuItems);

		listView.setAdapter(adapter);
	}

	private class NavigationMenuItem { // TODO remove
		public String playerName;
		public String timeLeft;
		public int playerImg;

		public NavigationMenuItem(String timeLeft, String playerName, int playerImg) {
			this.timeLeft = timeLeft;
			this.playerName = playerName;
			this.playerImg = playerImg;
		}
	}

	private class NewNavigationMenuAdapter extends ItemsAdapter<NavigationMenuItem> { // TODO  remove

		public NewNavigationMenuAdapter(Context context, List<NavigationMenuItem> menuItems) {
			super(context, menuItems);
		}

		@Override
		protected View createView(ViewGroup parent) {
			return LayoutInflater.from(getContext()).inflate(R.layout.new_games_list_item, null, false);
		}

		@Override
		protected void bindView(NavigationMenuItem item, int pos, View convertView) {
			ImageView icon = (ImageView) convertView.findViewById(R.id.playerImg);
			icon.setImageResource(item.playerImg);
			TextView timeLeftTxt = (TextView) convertView.findViewById(R.id.timeLeftTxt);
			timeLeftTxt.setText(item.timeLeft);
			TextView playerNameTxt = (TextView) convertView.findViewById(R.id.playerNameTxt);
			playerNameTxt.setText(item.playerName);
		}

		public Context getContext() {
			return context;
		}


	}
}
