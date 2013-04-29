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
import com.chess.backend.statics.StaticData;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.sign_in.WelcomeFragment;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.04.13
 * Time: 20:43
 */
public class SettingsFragment extends LiveBaseFragment implements AdapterView.OnItemClickListener {

	private ListView listView;
	private List<SettingsMenuItem> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<SettingsMenuItem>();
		menuItems.add(new SettingsMenuItem(R.string.profile, R.string.glyph_profile));
		menuItems.add(new SettingsMenuItem(R.string.board_and_pieces, R.string.glyph_board));
		menuItems.add(new SettingsMenuItem(R.string.daily_chess, R.string.glyph_daily_game));
		menuItems.add(new SettingsMenuItem(R.string.live_chess, R.string.glyph_live_standard));
		menuItems.add(new SettingsMenuItem(R.string.tactics, R.string.glyph_tactics_game));
		menuItems.add(new SettingsMenuItem(R.string.lessons, R.string.glyph_lessons));
		menuItems.add(new SettingsMenuItem(R.string.theme, R.string.glyph_theme));
		menuItems.add(new SettingsMenuItem(R.string.profile, R.string.glyph_info));
		menuItems.add(new SettingsMenuItem(R.string.privacy, R.string.glyph_settings));
		menuItems.add(new SettingsMenuItem(R.string.blocking, R.string.glyph_blocking));
		menuItems.add(new SettingsMenuItem(R.string.tracking, R.string.glyph_tracking));
		menuItems.add(new SettingsMenuItem(R.string.sharing, R.string.glyph_share));
		menuItems.add(new SettingsMenuItem(R.string.alerts_and_emails, R.string.glyph_email_dark));
		menuItems.add(new SettingsMenuItem(R.string.password, R.string.glyph_password));
		menuItems.add(new SettingsMenuItem(R.string.account_history, R.string.glyph_history));
		menuItems.add(new SettingsMenuItem(R.string.logout, R.string.glyph_close));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_frame, container, false);
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
		NewNavigationMenuAdapter adapter = new NewNavigationMenuAdapter(getActivity(), menuItems);

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
			case R.string.glyph_profile:
				getActivityFace().openFragment(new SettingsProfileFragment());
				break;
			case R.string.glyph_board:
				getActivityFace().openFragment(new SettingsBoardFragment());
				break;
			case R.string.glyph_close:
				if (isLCSBound) {
					liveService.logout();
				}

				// un-register from GCM
				unRegisterGcmService();

//				Facebook facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
//				SessionStore.restore(facebook, getActivity());
//				facebook.logoutMe(this, new LogoutRequestListener());

				preferencesEditor.putString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY);
				preferencesEditor.putString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);
				preferencesEditor.commit();

				AppUtils.cancelNotifications(getActivity());
				getActivityFace().clearFragmentStack();
				getActivityFace().switchFragment(new WelcomeFragment());

				break;

			default: break;
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

	private class NewNavigationMenuAdapter extends ItemsAdapter<SettingsMenuItem> {

		public NewNavigationMenuAdapter(Context context, List<SettingsMenuItem> menuItems) {
			super(context, menuItems);
		}

		@Override
		protected View createView(ViewGroup parent) {
			return inflater.inflate(R.layout.new_settings_menu_item, parent, false);
		}

		@Override
		protected void bindView(SettingsMenuItem item, int pos, View convertView) {
			TextView icon = (TextView) convertView.findViewById(R.id.iconTxt);
			icon.setText(item.iconRes);

			TextView title = (TextView) convertView.findViewById(R.id.rowTitleTxt);
			title.setText(item.nameId);
		}

		public Context getContext() {
			return context;
		}
	}
}
