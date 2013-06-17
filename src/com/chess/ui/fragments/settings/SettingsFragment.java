package com.chess.ui.fragments.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.welcome.WelcomeTabsFragment;
import com.chess.utilities.AppUtils;
import com.facebook.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.04.13
 * Time: 20:43
 */
public class SettingsFragment extends LiveBaseFragment implements AdapterView.OnItemClickListener {

	private static final long SWITCH_DELAY = 50;
	private ListView listView;
	private List<SettingsMenuItem> menuItems;
	private SettingsMenuAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<SettingsMenuItem>();
		menuItems.add(new SettingsMenuItem(R.string.profile, R.string.ic_profile));
		menuItems.add(new SettingsMenuItem(R.string.board_and_pieces, R.string.ic_board));
		menuItems.add(new SettingsMenuItem(R.string.daily_chess, R.string.ic_daily_game));
		menuItems.add(new SettingsMenuItem(R.string.live_chess, R.string.ic_live_standard));
		menuItems.add(new SettingsMenuItem(R.string.tactics, R.string.ic_help));
		menuItems.add(new SettingsMenuItem(R.string.lessons, R.string.ic_lessons));
		menuItems.add(new SettingsMenuItem(R.string.theme, R.string.ic_theme));
		menuItems.add(new SettingsMenuItem(R.string.privacy, R.string.ic_settings));
		menuItems.add(new SettingsMenuItem(R.string.blocking, R.string.ic_blocking));
		menuItems.add(new SettingsMenuItem(R.string.tracking, R.string.ic_challenge_friend));
		menuItems.add(new SettingsMenuItem(R.string.sharing, R.string.ic_share));
		menuItems.add(new SettingsMenuItem(R.string.alerts_and_emails, R.string.ic_email_dark));
		menuItems.add(new SettingsMenuItem(R.string.password, R.string.ic_password));
		menuItems.add(new SettingsMenuItem(R.string.account_history, R.string.ic_history));
		menuItems.add(new SettingsMenuItem(R.string.logout, R.string.ic_close));

		adapter = new SettingsMenuAdapter(getActivity(), menuItems);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.settings);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		listView.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		for (SettingsMenuItem menuItem : menuItems) {
			menuItem.selected = false;
		}

		menuItems.get(position).selected = true;
		SettingsMenuItem menuItem = (SettingsMenuItem) listView.getItemAtPosition(position);
		menuItem.selected = true;
		((BaseAdapter)parent.getAdapter()).notifyDataSetChanged();

		// TODO adjust switch/closeBoard when the same fragment opened
		switch (menuItem.iconRes) {
			case R.string.ic_profile:
				getActivityFace().openFragment(new SettingsProfileFragment());
				break;
			case R.string.ic_board:
				getActivityFace().openFragment(new SettingsBoardFragment());
				break;
			case R.string.ic_theme:
				getActivityFace().openFragment(new SettingsThemeFragment());
				break;
			case R.string.ic_close:
				logoutFromLive();

				// un-register from GCM
				unRegisterGcmService();

				// logout from facebook
				Session facebookSession = Session.getActiveSession();
				if (facebookSession != null) {
					facebookSession.closeAndClearTokenInformation();
					Session.setActiveSession(null);
				}

				preferencesEditor.putString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY);
				preferencesEditor.putString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);
				preferencesEditor.commit();

				AppUtils.cancelNotifications(getActivity());
				getActivityFace().clearFragmentStack();
				// make pause to wait while transactions complete
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						getActivityFace().switchFragment(new WelcomeTabsFragment());
					}
				}, SWITCH_DELAY);

				// clear theme
				AppData.setThemeBackId(getActivity(), R.drawable.img_theme_green_felt);
				getActivityFace().setMainBackground(R.drawable.img_theme_green_felt);

				// clear comp game
				ChessBoardComp.resetInstance();
				AppData.clearSavedCompGame(getActivity());

				break;
		}
	}

	private class SettingsMenuItem {
		public int nameId;
		public int iconRes;
		public boolean selected;

		public SettingsMenuItem(int nameId, int iconRes) {
			this.nameId = nameId;
			this.iconRes = iconRes;
		}
	}

	private class SettingsMenuAdapter extends ItemsAdapter<SettingsMenuItem> {

		public SettingsMenuAdapter(Context context, List<SettingsMenuItem> menuItems) {
			super(context, menuItems);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_settings_menu_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.icon = (TextView) view.findViewById(R.id.iconTxt);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);
			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(SettingsMenuItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.icon.setText(item.iconRes);
			holder.title.setText(item.nameId);
		}

		public Context getContext() {
			return context;
		}

		public class ViewHolder {
			TextView icon;
			TextView title;
		}
	}
}
