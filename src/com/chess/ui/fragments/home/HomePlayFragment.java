package com.chess.ui.fragments.home;

import android.animation.LayoutTransition;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.widgets.RelLayout;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.DailySeekItem;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.comp.GameCompFragment;
import com.chess.ui.fragments.daily.DailyGamesOptionsFragment;
import com.chess.ui.fragments.friends.ChallengeFriendFragment;
import com.chess.ui.fragments.live.LiveGameOptionsFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 18:29
 */
public class HomePlayFragment extends CommonLogicFragment implements SlidingMenu.OnOpenedListener{
	private static final String ERROR_TAG = "error popup";

	private TextView liveRatingTxt;
	private TextView dailyRatingTxt;
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private DailyGameConfig.Builder dailyGameConfigBuilder;
	private LiveGameConfig.Builder liveGameConfigBuilder;
	private int positionMode;
	private List<View> liveOptionsGroup;
	private HashMap<Integer, Button> liveButtonsModeMap;
	private boolean liveOptionsVisible;
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
	private LinearLayout dailyGameQuickOptions;
	private RelLayout liveOptionsView;
	private boolean liveFullOptionsVisible;
	private boolean dailyFullOptionsVisible;
	private LiveGameOptionsFragment liveGameOptionsFragment;
	private DailyGamesOptionsFragment dailyGamesOptionsFragment;
	private TextView liveExpandIconTxt;
	private TextView dailyExpandIconTxt;

	public HomePlayFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, CENTER_MODE);
		setArguments(bundle);
	}

	public static HomePlayFragment createInstance(int mode) {
		HomePlayFragment fragment = new HomePlayFragment();
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

		dailyGameConfigBuilder = new DailyGameConfig.Builder();
		liveGameConfigBuilder = new LiveGameConfig.Builder();
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
		getActivityFace().addOnOpenMenuListener(this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_play_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		RelativeLayout liveHomeOptionsFrame = (RelativeLayout) view.findViewById(R.id.liveHomeOptionsFrame);
		liveExpandIconTxt = (TextView) view.findViewById(R.id.liveExpandIconTxt);
		dailyExpandIconTxt = (TextView) view.findViewById(R.id.dailyExpandIconTxt);
		liveOptionsView = (RelLayout) view.findViewById(R.id.liveOptionsView);
		dailyGameQuickOptions = (LinearLayout) view.findViewById(R.id.dailyGameQuickOptions);
		if (positionMode == CENTER_MODE) {
			inflater.inflate(R.layout.new_home_live_options_view, liveHomeOptionsFrame, true);
			liveExpandIconTxt.setText(R.string.ic_right);
			dailyExpandIconTxt.setText(R.string.ic_right);
		} else {
			inflater.inflate(R.layout.new_right_live_options_view, liveHomeOptionsFrame, true);
			View liveHeaderView = view.findViewById(R.id.liveHeaderView);
			View dailyHeaderView = view.findViewById(R.id.dailyHeaderView);
			View vsCompHeaderView = view.findViewById(R.id.vsCompHeaderView);
			ButtonDrawableBuilder.setBackgroundToView(liveHeaderView, R.style.ListItem_Header_Dark);
			ButtonDrawableBuilder.setBackgroundToView(dailyHeaderView, R.style.ListItem_Header_2_Dark);
			ButtonDrawableBuilder.setBackgroundToView(vsCompHeaderView, R.style.ListItem_Header_Dark);
		}

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		setRatings();
		loadRecentOpponents();
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivityFace().removeOnOpenMenuListener(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
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

			cursor.close();
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveTimeSelectBtn) {
			toggleLiveOptionsView();
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
		} else if (view.getId() == R.id.dailyHeaderView) {
			if (positionMode == RIGHT_MENU_MODE) {
				dailyFullOptionsVisible = !dailyFullOptionsVisible;
				dailyGameQuickOptions.setVisibility(dailyFullOptionsVisible ? View.GONE : View.VISIBLE);
				if (dailyFullOptionsVisible) {
					dailyExpandIconTxt.setText(R.string.ic_up);
					if (dailyGamesOptionsFragment == null) {
						dailyGamesOptionsFragment = new DailyGamesOptionsFragment();
					}

					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.replace(R.id.dailyOptionsFrame, dailyGamesOptionsFragment).commit();
				} else {
					dailyExpandIconTxt.setText(R.string.ic_down);
					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.remove(dailyGamesOptionsFragment).commit();
				}
			} else {
				getActivityFace().changeRightFragment(DailyGamesOptionsFragment.createInstance(CENTER_MODE));
				getActivityFace().toggleRightMenu();
			}

		} else if (view.getId() == R.id.dailyPlayBtn) {
			createDailyChallenge();
		} else if (view.getId() == R.id.inviteFriendView1) {
			dailyGameConfigBuilder.setOpponentName(firstFriendUserName);
			createDailyChallenge();
		} else if (view.getId() == R.id.inviteFriendView2) {
			dailyGameConfigBuilder.setOpponentName(secondFriendUserName);
			createDailyChallenge();
		} else if (view.getId() == R.id.playFriendView) {
			ChallengeFriendFragment challengeFriendFragment;
			if (positionMode == CENTER_MODE) {
				challengeFriendFragment = ChallengeFriendFragment.createInstance(CENTER_MODE);
				getActivityFace().toggleRightMenu();
			} else {
				challengeFriendFragment = new ChallengeFriendFragment();
			}
			getActivityFace().changeRightFragment(challengeFriendFragment);
		} else if (view.getId() == R.id.vsCompHeaderView) {
			CompGameConfig.Builder gameConfigBuilder = new CompGameConfig.Builder();
			CompGameConfig compGameConfig = gameConfigBuilder.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE).build();
			getActivityFace().openFragment(GameCompFragment.createInstance(compGameConfig));
			if (positionMode == RIGHT_MENU_MODE) {
				getActivityFace().toggleRightMenu();
			}
		} else {
			handleLiveModeClicks(view);
		}
	}

	private void handleLiveModeClicks(View view) {
		int id = view.getId();
		boolean liveModeButton = false;
		for (Button button : liveButtonsModeMap.values()) {
			if (id == button.getId()) {
				liveModeButton = true;
				break;
			}
		}

		if (liveModeButton) {
			for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
				Button button = buttonEntry.getValue();
				button.setSelected(false);
				if (id == button.getId()) {
					setDefaultQuickLiveMode(view, buttonEntry.getKey());
				}
			}
		}
	}

	private void setDefaultQuickLiveMode(View view, int mode) {
		view.setSelected(true);
		liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[mode]));
		liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[mode]);
		getAppData().setDefaultLiveMode(mode);
	}

	private void toggleLiveOptionsView() {
		liveOptionsVisible = !liveOptionsVisible;
		for (View view : liveOptionsGroup) {
			view.setVisibility(liveOptionsVisible ? View.VISIBLE : View.GONE);
		}

		int selectedLiveTimeMode = getAppData().getDefaultLiveMode();
		for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
			Button button = buttonEntry.getValue();
			button.setVisibility(liveOptionsVisible ? View.VISIBLE : View.GONE);
			if (liveOptionsVisible) {
				if (selectedLiveTimeMode == buttonEntry.getKey()) {
					button.setSelected(true);
				}
			}
		}
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = dailyGameConfigBuilder.build();

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessLoadUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}
	}

	@Override
	public void onOpened() {

	}

	@Override
	public void onOpenedRight() {
		if (positionMode == RIGHT_MENU_MODE && !isPaused) {
			setRatings();
			loadRecentOpponents();
		}
	}

	private void setRatings() {
		// set live rating
		int liveRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal(), getUsername());
		liveRatingTxt.setText(String.valueOf(liveRating));

		// set daily rating
		int dailyRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal(), getUsername());
		dailyRatingTxt.setText(String.valueOf(dailyRating));
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

			View homePlayScrollView = view.findViewById(R.id.homePlayScrollView);
			homePlayScrollView.setBackgroundResource(R.color.white);

			TextView liveChessHeaderTxt = (TextView) view.findViewById(R.id.liveChessHeaderTxt);
			Button liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
			TextView dailyChessHeaderTxt = (TextView) view.findViewById(R.id.dailyChessHeaderTxt);
			TextView vsRandomTxt = (TextView) view.findViewById(R.id.vsRandomTxt);
			TextView challengeFriendTxt = (TextView) view.findViewById(R.id.challengeFriendTxt);
			TextView vsComputerHeaderTxt = (TextView) view.findViewById(R.id.vsComputerHeaderTxt);

			liveChessHeaderTxt.setTextColor(darkTextColor);
			liveTimeSelectBtn.setTextColor(darkBtnColor);
			dailyChessHeaderTxt.setTextColor(darkTextColor);
			vsRandomTxt.setTextColor(darkTextColor);
			friendUserName1Txt.setTextColor(darkTextColor);
			friendRealName1Txt.setTextColor(darkTextColor);
			friendUserName2Txt.setTextColor(darkTextColor);
			friendRealName2Txt.setTextColor(darkTextColor);
			challengeFriendTxt.setTextColor(darkTextColor);
			vsComputerHeaderTxt.setTextColor(darkTextColor);
		}

		liveRatingTxt = (TextView) view.findViewById(R.id.liveRatingTxt);
		dailyRatingTxt = (TextView) view.findViewById(R.id.dailyRatingTxt);

		liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
		liveTimeSelectBtn.setOnClickListener(this);
		view.findViewById(R.id.livePlayBtn).setOnClickListener(this);
		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.liveHeaderView).setOnClickListener(this);
		view.findViewById(R.id.dailyHeaderView).setOnClickListener(this);
		view.findViewById(R.id.vsCompHeaderView).setOnClickListener(this);

		{ // live options
			if (JELLY_BEAN_PLUS_API) {
				ViewGroup liveOptionsView = (ViewGroup) view.findViewById(R.id.homePlayLinLay);
				LayoutTransition layoutTransition = liveOptionsView.getLayoutTransition();
				layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
			}

			liveOptionsGroup = new ArrayList<View>();
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelStandardTxt));
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelBlitzTxt));
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelBulletTxt));

			liveButtonsModeMap = new HashMap<Integer, Button>();
			liveButtonsModeMap.put(0, (Button) view.findViewById(R.id.standard1SelectBtn));
			liveButtonsModeMap.put(1, (Button) view.findViewById(R.id.blitz1SelectBtn));
			liveButtonsModeMap.put(2, (Button) view.findViewById(R.id.blitz2SelectBtn));
			liveButtonsModeMap.put(3, (Button) view.findViewById(R.id.bullet1SelectBtn));
			liveButtonsModeMap.put(4, (Button) view.findViewById(R.id.standard2SelectBtn));
			liveButtonsModeMap.put(5, (Button) view.findViewById(R.id.blitz3SelectBtn));
			liveButtonsModeMap.put(6, (Button) view.findViewById(R.id.blitz4SelectBtn));
			liveButtonsModeMap.put(7, (Button) view.findViewById(R.id.bullet2SelectBtn));

			int mode = getAppData().getDefaultLiveMode();
			darkBtnColor = getResources().getColor(R.color.text_controls_icons_white);
			// set texts to buttons
			newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
			for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
				int key = buttonEntry.getKey();
				buttonEntry.getValue().setText(getLiveModeButtonLabel(newGameButtonsArray[key]));
				buttonEntry.getValue().setOnClickListener(this);

				if (positionMode == CENTER_MODE) {
					buttonEntry.getValue().setTextColor(darkBtnColor);
				}

				if (key == mode) {
					setDefaultQuickLiveMode(buttonEntry.getValue(), buttonEntry.getKey());
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
/*
page 9 is the "New Game" screen. that should show if they have NO games (in progress, or completed).
it should also show if they click on the NEW GAME button http://i.imgur.com/QOz3DQB.png  HomePlaySetupFragment

if they have games where it is their turn to move, show screen 10 - hide the NEW GAME button and just show the games.

if they have no moves to make, show the NEW GAME button as in screen 11.

screen 12 shows the completed games. those are ALWAYS there, at the bottom of games in progress.
	 */

/*
those are to challenge your friend to play. it just creates a challenge.

those should be random friends who have been online in the last 30 days. (new api! :D)

never display more than 2. but if only 1, or none, show only 1, or none :)

(Friend Name) means their real name. so, for dallin, i would see:

ignoble (Dallin)    [invite]

invite button will just automatically create a challenge, and then show a success message!

play a friend i think is supposed to open friends screen, yes.

Auto-Match should be just a random, open, rated, 3-day seek.
	 */