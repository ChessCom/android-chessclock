package com.chess.ui.fragments.live;

import android.animation.LayoutTransition;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.widgets.RelLayout;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.friends.ChallengeFriendFragment;
import com.chess.ui.fragments.friends.FriendsFragment;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.09.13
 * Time: 6:54
 */
public class LiveNewGameFragment extends CommonLogicFragment {

	private LiveGameConfig.Builder liveGameConfigBuilder;
	private int positionMode;
	private List<View> timeOptionsGroup;
	private HashMap<Integer, Button> timeButtonsModeMap;
	private boolean timeOptionsVisible;
	private Button liveTimeSelectBtn;
	private String[] newGameButtonsArray;
	private View inviteFriendView1;
	private View inviteFriendView2;
	private TextView friendUserName1Txt;
	private TextView friendUserName2Txt;
	private TextView friendRealName1Txt;
	private TextView friendRealName2Txt;
	private String firstFriendUserName;
	private String secondFriendUserName;
	private RelLayout liveOptionsView;
	private boolean liveFullOptionsVisible;
	private TextView liveExpandIconTxt;
	private LiveGameOptionsFragment liveGameOptionsFragment;

	public LiveNewGameFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, CENTER_MODE);
		setArguments(bundle);
	}

//	public static LiveNewGameFragment createInstance(int mode) {
//		LiveNewGameFragment fragment = new LiveNewGameFragment();
//		Bundle bundle = new Bundle();
//		bundle.putInt(MODE, mode);
//		fragment.setArguments(bundle);
//		return fragment;
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			positionMode = getArguments().getInt(MODE);
		} else {
			positionMode = savedInstanceState.getInt(MODE);
		}

		liveGameConfigBuilder = new LiveGameConfig.Builder();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_live_home_new_game_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		RelativeLayout liveHomeOptionsFrame = (RelativeLayout) view.findViewById(R.id.liveHomeOptionsFrame);
		liveExpandIconTxt = (TextView) view.findViewById(R.id.liveExpandIconTxt);
		liveOptionsView = (RelLayout) view.findViewById(R.id.liveOptionsView);
		if (positionMode == CENTER_MODE) {
			inflater.inflate(R.layout.new_home_live_options_view, liveHomeOptionsFrame, true);
			liveExpandIconTxt.setText(R.string.ic_right);
		} else {
			inflater.inflate(R.layout.new_right_live_options_view, liveHomeOptionsFrame, true);
			View liveHeaderView = view.findViewById(R.id.liveHeaderView);
//			View friendsHeaderView = view.findViewById(R.id.friendsHeaderView);
//			ButtonDrawableBuilder.setBackgroundToView(friendsHeaderView, R.style.ListItem_Header_2_Dark);
			ButtonDrawableBuilder.setBackgroundToView(liveHeaderView, R.style.ListItem_Header_Dark);
		}

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		loadRecentOpponents();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
	}

	private void loadRecentOpponents() {  // TODO load from live archive games
		Cursor cursor = DbDataManager.getRecentOpponentsCursor(getActivity(), getUsername());// TODO load avatars
		if (cursor != null && cursor.moveToFirst()) {
			if (cursor.getCount() >= 2) {
				inviteFriendView1.setVisibility(View.VISIBLE);
				inviteFriendView1.setOnClickListener(this);
				inviteFriendView2.setVisibility(View.VISIBLE);
				inviteFriendView2.setOnClickListener(this);

				firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
				if (firstFriendUserName.equals(getUsername())) {
					firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
				}
				friendUserName1Txt.setText(firstFriendUserName);

				cursor.moveToNext();

				secondFriendUserName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
				if (secondFriendUserName.equals(getUsername())) {
					secondFriendUserName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
				}
				friendUserName2Txt.setText(secondFriendUserName);
			} else if (cursor.getCount() == 1) {
				inviteFriendView1.setVisibility(View.VISIBLE);
				inviteFriendView1.setOnClickListener(this);

				firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
				if (firstFriendUserName.equals(getUsername())) {
					firstFriendUserName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
				}
				friendUserName1Txt.setText(firstFriendUserName);
			}

			cursor.close();
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveTimeSelectBtn) {
			toggleTimeOptionsView();
		} else if (view.getId() == R.id.liveHeaderView) {
			if (positionMode == RIGHT_MENU_MODE) {
				liveFullOptionsVisible = !liveFullOptionsVisible;
				liveOptionsView.setVisibility(liveFullOptionsVisible ? View.GONE : View.VISIBLE);
				if (liveFullOptionsVisible) {
					liveExpandIconTxt.setText(R.string.ic_up);
					if (liveGameOptionsFragment == null) {
						liveGameOptionsFragment = new LiveGameOptionsFragment();
					}

					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.replace(R.id.liveOptionsFrame, liveGameOptionsFragment).commit();
				} else {
					liveExpandIconTxt.setText(R.string.ic_down);
					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.remove(liveGameOptionsFragment).commit();
				}
			} else {
				getActivityFace().changeRightFragment(LiveGameOptionsFragment.createInstance(CENTER_MODE));
				getActivityFace().toggleRightMenu();
			}

		} else if (view.getId() == R.id.livePlayBtn) {
			createLiveChallenge();
			if (positionMode == RIGHT_MENU_MODE) {
				getActivityFace().toggleRightMenu();
			}
		} else if (view.getId() == R.id.inviteFriendView1) {
			liveGameConfigBuilder.setOpponentName(firstFriendUserName);
			createLiveChallenge();
		} else if (view.getId() == R.id.inviteFriendView2) {
			liveGameConfigBuilder.setOpponentName(secondFriendUserName);
			createLiveChallenge();
		} else if (view.getId() == R.id.friendsHeaderView) {
			getActivityFace().openFragment(new FriendsFragment());
		} else if (view.getId() == R.id.playFriendView) {
			ChallengeFriendFragment challengeFriendFragment;
			if (positionMode == CENTER_MODE) {
				challengeFriendFragment = ChallengeFriendFragment.createInstance(CENTER_MODE);
				getActivityFace().toggleRightMenu();
			} else {
				challengeFriendFragment = new ChallengeFriendFragment();
			}
			getActivityFace().changeRightFragment(challengeFriendFragment);
		} else {
			handleLiveModeClicks(view);
		}
	}

	private void handleLiveModeClicks(View view) {
		int id = view.getId();
		boolean liveModeButton = false;
		for (Button button : timeButtonsModeMap.values()) {
			if (id == button.getId()) {
				liveModeButton = true;
				break;
			}
		}

		if (liveModeButton) {
			for (Map.Entry<Integer, Button> buttonEntry : timeButtonsModeMap.entrySet()) {
				Button button = buttonEntry.getValue();
				button.setSelected(false);
				if (id == button.getId()) {
					setDefaultTimeMode(view, buttonEntry.getKey());
				}
			}
		}
	}

	private void setDefaultTimeMode(View view, int mode) {
		view.setSelected(true);
		liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[mode]));
		liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[mode]);
		getAppData().setDefaultLiveMode(mode);
	}

	private void toggleTimeOptionsView() {
		timeOptionsVisible = !timeOptionsVisible;
		for (View view : timeOptionsGroup) {
			view.setVisibility(timeOptionsVisible ? View.VISIBLE : View.GONE);
		}

		int selectedLiveTimeMode = getAppData().getDefaultLiveMode();
		for (Map.Entry<Integer, Button> buttonEntry : timeButtonsModeMap.entrySet()) {
			Button button = buttonEntry.getValue();
			button.setVisibility(timeOptionsVisible ? View.VISIBLE : View.GONE);
			if (timeOptionsVisible) {
				if (selectedLiveTimeMode == buttonEntry.getKey()) {
					button.setSelected(true);
				}
			}
		}
	}

	private void createLiveChallenge() {
//		fragmentByTag = (BasePopupsFragment) findFragmentByTag(GameLiveFragment.class.getSimpleName());
//		if (fragmentByTag == null) {
//			fragmentByTag = new LiveGameWaitFragment();
//		}

		getActivityFace().openFragment(LiveGameWaitFragment.createInstance(liveGameConfigBuilder.build()));
	}

	private void widgetsInit(View view) {
		int darkBtnColor = getResources().getColor(R.color.stats_label_grey);

		inviteFriendView1 = view.findViewById(R.id.inviteFriendView1);
		inviteFriendView2 = view.findViewById(R.id.inviteFriendView2);
		friendUserName1Txt = (TextView) view.findViewById(R.id.friendUserName1Txt);
		friendRealName1Txt = (TextView) view.findViewById(R.id.friendRealName1Txt);
		friendUserName2Txt = (TextView) view.findViewById(R.id.friendUserName2Txt);
		friendRealName2Txt = (TextView) view.findViewById(R.id.friendRealName2Txt);

		if (positionMode == CENTER_MODE) { // we use white background and dark titles for centered mode
			int darkTextColor = getResources().getColor(R.color.new_subtitle_dark_grey);

			View livePlayScrollView = view.findViewById(R.id.livePlayScrollView);
			livePlayScrollView.setBackgroundResource(R.color.white);

			TextView liveChessHeaderTxt = (TextView) view.findViewById(R.id.liveChessHeaderTxt);
			Button liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
			TextView challengeFriendTxt = (TextView) view.findViewById(R.id.challengeFriendTxt);
			TextView friendsHeaderTxt = (TextView) view.findViewById(R.id.friendsHeaderTxt);

			liveChessHeaderTxt.setTextColor(darkTextColor);
			liveTimeSelectBtn.setTextColor(darkBtnColor);
			friendsHeaderTxt.setTextColor(darkTextColor);
			friendUserName1Txt.setTextColor(darkTextColor);
			friendRealName1Txt.setTextColor(darkTextColor);
			friendUserName2Txt.setTextColor(darkTextColor);
			friendRealName2Txt.setTextColor(darkTextColor);
			challengeFriendTxt.setTextColor(darkTextColor);
		}

		liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
		liveTimeSelectBtn.setOnClickListener(this);
		view.findViewById(R.id.livePlayBtn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.liveHeaderView).setOnClickListener(this);
		view.findViewById(R.id.friendsHeaderView).setOnClickListener(this);

		{ // live options
			if (JELLY_BEAN_PLUS_API) {
				ViewGroup liveOptionsView = (ViewGroup) view.findViewById(R.id.homePlayLinLay);
				LayoutTransition layoutTransition = liveOptionsView.getLayoutTransition();
				layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
			}

			timeOptionsGroup = new ArrayList<View>();
			timeOptionsGroup.add(view.findViewById(R.id.liveLabelStandardTxt));
			timeOptionsGroup.add(view.findViewById(R.id.liveLabelBlitzTxt));
			timeOptionsGroup.add(view.findViewById(R.id.liveLabelBulletTxt));

			timeButtonsModeMap = new HashMap<Integer, Button>();
			timeButtonsModeMap.put(0, (Button) view.findViewById(R.id.standard1SelectBtn));
			timeButtonsModeMap.put(1, (Button) view.findViewById(R.id.blitz1SelectBtn));
			timeButtonsModeMap.put(2, (Button) view.findViewById(R.id.blitz2SelectBtn));
			timeButtonsModeMap.put(3, (Button) view.findViewById(R.id.bullet1SelectBtn));
			timeButtonsModeMap.put(4, (Button) view.findViewById(R.id.standard2SelectBtn));
			timeButtonsModeMap.put(5, (Button) view.findViewById(R.id.blitz3SelectBtn));
			timeButtonsModeMap.put(6, (Button) view.findViewById(R.id.blitz4SelectBtn));
			timeButtonsModeMap.put(7, (Button) view.findViewById(R.id.bullet2SelectBtn));

			int mode = getAppData().getDefaultLiveMode();
			darkBtnColor = getResources().getColor(R.color.text_controls_icons_white);
			// set texts to buttons
			newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
			for (Map.Entry<Integer, Button> buttonEntry : timeButtonsModeMap.entrySet()) {
				int key = buttonEntry.getKey();
				buttonEntry.getValue().setText(getLiveModeButtonLabel(newGameButtonsArray[key]));
				buttonEntry.getValue().setOnClickListener(this);

				if (positionMode == CENTER_MODE) {
					buttonEntry.getValue().setTextColor(darkBtnColor);
				}

				if (key == mode) {
					setDefaultTimeMode(buttonEntry.getValue(), buttonEntry.getKey());
				}
			}
		}
	}

	private String getLiveModeButtonLabel(String label) {
		if (label.contains(Symbol.SLASH)) { // "5 | 2"
			return label;
		} else { // "10 min"
			return getString(R.string.min_arg, label);
		}
	}
}
