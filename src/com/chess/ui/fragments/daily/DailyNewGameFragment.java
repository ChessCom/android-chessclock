package com.chess.ui.fragments.daily;

import android.animation.LayoutTransition;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.DailySeekItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.friends.ChallengeFriendFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.04.13
 * Time: 12:29
 */
public class DailyNewGameFragment extends CommonLogicFragment {

	private static final String ERROR_TAG = "error popup";

	private View inviteFriendView1;
	private View inviteFriendView2;
	private TextView friendUserName1Txt;
	private TextView friendUserName2Txt;
	private TextView friendRealName1Txt;
	private TextView friendRealName2Txt;
	private String firstFriendUserName;
	private String secondFriendUserName;

	private DailyGameConfig.Builder gameConfigBuilder;
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private int positionMode;
	private List<View> timeOptionsGroup;
	private HashMap<Integer, Button> timeButtonsModeMap;
	private boolean timeOptionsVisible;
	private int[] newGameButtonsArray;
	private Button dailyTimeSelectBtn;

	public DailyNewGameFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, CENTER_MODE);
		setArguments(bundle);
	}

	public static DailyNewGameFragment createInstance(int mode) {
		DailyNewGameFragment fragment = new DailyNewGameFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			positionMode = getArguments().getInt(MODE);
		} else {
			positionMode = savedInstanceState.getInt(MODE);
		}

		gameConfigBuilder = new DailyGameConfig.Builder();
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_daily_new_game_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.daily);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		RelativeLayout dailyHomeOptionsFrame = (RelativeLayout) view.findViewById(R.id.dailyHomeOptionsFrame);
		if (positionMode == CENTER_MODE) {
			inflater.inflate(R.layout.new_daily_home_time_options_view, dailyHomeOptionsFrame, true);
//			liveExpandIconTxt.setText(R.string.ic_right);
		} else {
			inflater.inflate(R.layout.new_daily_home_time_options_view, dailyHomeOptionsFrame, true);
//			View liveHeaderView = view.findViewById(R.id.liveHeaderView);
//			View friendsHeaderView = view.findViewById(R.id.friendsHeaderView);
//			ButtonDrawableBuilder.setBackgroundToView(friendsHeaderView, R.style.ListItem_Header_2_Dark);
//			ButtonDrawableBuilder.setBackgroundToView(liveHeaderView, R.style.ListItem_Header_Dark);
		}

		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.dailyHeaderView).setOnClickListener(this);

		widgetsInit(view);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (view.getId() == R.id.dailyTimeSelectBtn) {
			toggleTimeOptionsView();
		} else if (id == R.id.dailyPlayBtn) {
			createDailyChallenge();

		} else if (id == R.id.playFriendView) {
			getActivityFace().changeRightFragment(new ChallengeFriendFragment());
			getActivityFace().toggleRightMenu();
		} else if (id == R.id.dailyHeaderView) {
			getActivityFace().changeRightFragment(new DailyGameOptionsFragment());
			getActivityFace().toggleRightMenu();
		} else {
			handleTimeModeClicks(view);
		}
	}

	private void loadRecentOpponents() {
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
		}
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = gameConfigBuilder.build();

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}
	}


	private void widgetsInit(View view) {
		int darkBtnColor = getResources().getColor(R.color.stats_label_grey);

		inviteFriendView1 = view.findViewById(R.id.inviteFriendView1);
		inviteFriendView2 = view.findViewById(R.id.inviteFriendView2);
		friendUserName1Txt = (TextView) view.findViewById(R.id.friendUserName1Txt);
		friendRealName1Txt = (TextView) view.findViewById(R.id.friendRealName1Txt);
		friendUserName2Txt = (TextView) view.findViewById(R.id.friendUserName2Txt);
		friendRealName2Txt = (TextView) view.findViewById(R.id.friendRealName2Txt);

		int darkTextColor = getResources().getColor(R.color.new_subtitle_dark_grey);
		View dailyPlayScrollView = view.findViewById(R.id.dailyPlayScrollView);
		dailyPlayScrollView.setBackgroundResource(R.color.white);

		dailyTimeSelectBtn = (Button) view.findViewById(R.id.dailyTimeSelectBtn);
		dailyTimeSelectBtn.setOnClickListener(this);
		TextView dailyChessHeaderTxt = (TextView) view.findViewById(R.id.dailyChessHeaderTxt);
		TextView friendsHeaderTxt = (TextView) view.findViewById(R.id.friendsHeaderTxt);

		dailyTimeSelectBtn.setTextColor(darkBtnColor);
		dailyChessHeaderTxt.setTextColor(darkTextColor);
		friendUserName1Txt.setTextColor(darkTextColor);
		friendRealName1Txt.setTextColor(darkTextColor);
		friendUserName2Txt.setTextColor(darkTextColor);
		friendRealName2Txt.setTextColor(darkTextColor);
		friendsHeaderTxt.setTextColor(darkTextColor);

		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.dailyHeaderView).setOnClickListener(this);

		{ // time options
			if (JELLY_BEAN_PLUS_API) {
				ViewGroup dailyNewOptionsView = (ViewGroup) view.findViewById(R.id.dailyNewOptionsView);
				LayoutTransition layoutTransition = dailyNewOptionsView.getLayoutTransition();
				layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
			}

			timeOptionsGroup = new ArrayList<View>();

			timeButtonsModeMap = new HashMap<Integer, Button>();
			timeButtonsModeMap.put(0, (Button) view.findViewById(R.id.time1SelectBtn));
			timeButtonsModeMap.put(1, (Button) view.findViewById(R.id.time2SelectBtn));
			timeButtonsModeMap.put(2, (Button) view.findViewById(R.id.time3SelectBtn));
			timeButtonsModeMap.put(3, (Button) view.findViewById(R.id.time4SelectBtn));
			timeButtonsModeMap.put(4, (Button) view.findViewById(R.id.time5SelectBtn));
			timeButtonsModeMap.put(5, (Button) view.findViewById(R.id.time6SelectBtn));


			int mode = getAppData().getDefaultDailyMode();
			darkBtnColor = getResources().getColor(R.color.text_controls_icons_white);
			// set texts to buttons
			newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
			for (Map.Entry<Integer, Button> buttonEntry : timeButtonsModeMap.entrySet()) {
				int key = buttonEntry.getKey();
				buttonEntry.getValue().setText(getDaysString(newGameButtonsArray[key]));
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

	private void handleTimeModeClicks(View view) {
		int id = view.getId();
		boolean timeModeButton = false;
		for (Button button : timeButtonsModeMap.values()) {
			if (id == button.getId()) {
				timeModeButton = true;
				break;
			}
		}

		if (timeModeButton) {
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
		dailyTimeSelectBtn.setText(getDaysString(newGameButtonsArray[mode]));
		gameConfigBuilder.setDaysPerMove(newGameButtonsArray[mode]);
		getAppData().setDefaultDailyMode(mode);
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
}
