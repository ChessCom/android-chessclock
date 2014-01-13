package com.chess.ui.fragments.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.AppConstants;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.widgets.RoboButton;
import com.chess.widgets.RoboTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.04.13
 * Time: 20:43
 */
public class SettingsFragment extends LiveBaseFragment implements AdapterView.OnItemClickListener {

	protected ListView listView;
	protected List<SettingsMenuItem> menuItems;
	protected SettingsMenuAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<SettingsMenuItem>();
		if (StaticData.USE_SWITCH_API) {
			menuItems.add(new SettingsMenuItem(R.string.app_name, R.string.ic_key));
		}
		menuItems.add(new SettingsMenuItem(R.string.profile, R.string.ic_profile));
		menuItems.add(new SettingsMenuItem(R.string.theme, R.string.ic_theme));
		menuItems.add(new SettingsMenuItem(R.string.general, R.string.ic_board));
		menuItems.add(new SettingsMenuItem(R.string.daily_chess, R.string.ic_daily_game));
		menuItems.add(new SettingsMenuItem(R.string.live_chess, R.string.ic_live_standard));
		menuItems.add(new SettingsMenuItem(R.string.tactics, R.string.ic_help));
//		menuItems.add(new SettingsMenuItem(R.string.lessons, R.string.ic_lessons));
//		menuItems.add(new SettingsMenuItem(R.string.privacy, R.string.ic_settings));
//		menuItems.add(new SettingsMenuItem(R.string.blocking, R.string.ic_blocking));
//		menuItems.add(new SettingsMenuItem(R.string.tracking, R.string.ic_challenge_friend));
//		menuItems.add(new SettingsMenuItem(R.string.sharing, R.string.ic_share));
//		menuItems.add(new SettingsMenuItem(R.string.alerts_and_emails, R.string.ic_email_dark));
		menuItems.add(new SettingsMenuItem(R.string.password, R.string.ic_password));
//		menuItems.add(new SettingsMenuItem(R.string.account_history, R.string.ic_history));
		menuItems.add(new SettingsMenuItem(R.string.report_problem, R.string.ic_ticket));
		menuItems.add(new SettingsMenuItem(R.string.language, R.string.ic_home));
		menuItems.add(new SettingsMenuItem(R.string.logout, R.string.ic_close));

		adapter = new SettingsMenuAdapter(getActivity(), menuItems);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.settings);

		if (!isTablet) {
			RoboTextView versionCodeTxt = (RoboTextView) view.findViewById(R.id.versionCodeTxt);
			versionCodeTxt.setText("v" + deviceInfo.APP_VERSION_NAME);
			versionCodeTxt.setTextColor(themeFontColorStateList.getDefaultColor());
		} else {
			RoboButton resumeLessonBtn = (RoboButton) view.findViewById(R.id.resumeLessonBtn); // used only for beta
			resumeLessonBtn.setDrawableStyle(R.style.ListItem);
			resumeLessonBtn.setText("v" + deviceInfo.APP_VERSION_NAME);
			resumeLessonBtn.setTextColor(themeFontColorStateList.getDefaultColor());
			resumeLessonBtn.setVisibility(View.VISIBLE);
		}

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
		((BaseAdapter) parent.getAdapter()).notifyDataSetChanged();

		// TODO adjust switch/closeBoard when the same fragment opened
		switch (menuItem.iconRes) {
			case R.string.ic_key:
				if (StaticData.USE_SWITCH_API) {
					getActivityFace().openFragment(new SettingsApiFragment());
				}
				break;
			case R.string.ic_profile:
				getActivityFace().openFragment(new SettingsProfileFragment());
				break;
			case R.string.ic_theme:
				getActivityFace().openFragment(new SettingsThemeFragment());
				break;
			case R.string.ic_board:
				getActivityFace().openFragment(new SettingsGeneralFragment());
				break;
			case R.string.ic_daily_game:
				getActivityFace().openFragment(new SettingsDailyChessFragment());
				break;
			case R.string.ic_live_standard:
				getActivityFace().openFragment(new SettingsLiveChessFragment());
				break;
			case R.string.ic_help:
				getActivityFace().openFragment(new SettingsTacticsFragment());
				break;
			case R.string.ic_password:
				getActivityFace().openFragment(new SettingsPasswordFragment());
				break;
			case R.string.ic_home:
				getActivityFace().openFragment(new SettingsLanguageFragment());
				break;
			case R.string.ic_ticket:
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.setType(AppConstants.MIME_TYPE_MESSAGE_RFC822);
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstants.FEEDBACK_EMAIL});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, AppConstants.FEEDBACK_SUBJECT);
				emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackBodyCompose(getUsername()));
				startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
				break;
			case R.string.ic_close:
				logoutFromLive();
				performLogout();

				break;
		}
	}

	protected class SettingsMenuItem {
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
