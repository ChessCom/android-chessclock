package com.chess.ui.fragments.home;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailySeekItem;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.daily.DailyGamesOptionsFragment;
import com.chess.ui.fragments.friends.InviteFriendsFragment;
import com.chess.ui.fragments.game.GameCompFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

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
public class HomePlayFragment extends CommonLogicFragment {
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

	public HomePlayFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, CENTER_MODE);
		setArguments(bundle);
	}

	public static HomePlayFragment newInstance(int mode) {
		HomePlayFragment fragment = new HomePlayFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dailyGameConfigBuilder = new DailyGameConfig.Builder();
		liveGameConfigBuilder = new LiveGameConfig.Builder();
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null) {
			positionMode = getArguments().getInt(MODE);
		} else {
			positionMode = savedInstanceState.getInt(MODE);
		}
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
		if (getArguments().getInt(MODE) == CENTER_MODE) {
			inflater.inflate(R.layout.new_home_live_options_view, liveHomeOptionsFrame, true);
		} else {
			inflater.inflate(R.layout.new_right_live_options_view, liveHomeOptionsFrame, true);
			View liveHeaderView = view.findViewById(R.id.liveHeaderView);
			View dailyHeaderView = view.findViewById(R.id.dailyHeaderView);
			View vsCompHeaderView = view.findViewById(R.id.vsCompHeaderView);
			ButtonDrawableBuilder.setBackgroundToView(liveHeaderView, R.style.ListItem_Header_Dark);
			ButtonDrawableBuilder.setBackgroundToView(dailyHeaderView, R.style.ListItem_Header_Dark);
			ButtonDrawableBuilder.setBackgroundToView(vsCompHeaderView, R.style.ListItem_Header_Dark);
		}

		widgetsInit(view);
	}

	@Override
	public void onStart() {
		super.onStart();

		setRatings();
		// TODO load friends, get only 2
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveTimeSelectBtn) {
			toggleLiveOptionsView();
		} else if (view.getId() == R.id.livePlayBtn) {
			createLiveChallenge();
		} else if (view.getId() == R.id.dailyPlayBtn) {
			createDailyChallenge(); // TODO adjust
		} else if (view.getId() == R.id.liveHeaderView) {
			getActivityFace().openFragment(StatsGameFragment.newInstance(StatsGameFragment.LIVE_STANDARD));
		} else if (view.getId() == R.id.dailyHeaderView) {
			if (positionMode == CENTER_MODE) {
				getActivityFace().openFragment(StatsGameFragment.newInstance(StatsGameFragment.DAILY_CHESS));
			} else {
				getActivityFace().changeRightFragment(new DailyGamesOptionsFragment());
			}
		} else if (view.getId() == R.id.playFriendView) {
			getActivityFace().changeRightFragment(new InviteFriendsFragment());
			if (positionMode == CENTER_MODE) {
				getActivityFace().toggleRightMenu();
			}
		} else if (view.getId() == R.id.vsCompHeaderView) {
			CompGameConfig.Builder gameConfigBuilder = new CompGameConfig.Builder();
			CompGameConfig compGameConfig = gameConfigBuilder.setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE).build();
			getActivityFace().openFragment(GameCompFragment.newInstance(compGameConfig));
			if (positionMode == RIGHT_MENU_MODE) {
				getActivityFace().toggleRightMenu();
			}
		} else {
			handleLiveModeClicks(view);
		}
	}

	private void setRatings() {
		// set live rating
		int liveRating = DBDataManager.getUserCurrentRating(getActivity(), DBConstants.GAME_STATS_LIVE_STANDARD);
		liveRatingTxt.setText(String.valueOf(liveRating));

		// set daily rating
		int dailyRating = DBDataManager.getUserCurrentRating(getActivity(), DBConstants.GAME_STATS_DAILY_CHESS);
		dailyRatingTxt.setText(String.valueOf(dailyRating));

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
		AppData.setDefaultLiveMode(getActivity(), mode);
	}

	private void toggleLiveOptionsView() {
		liveOptionsVisible = !liveOptionsVisible;
		for (View view : liveOptionsGroup) {
			view.setVisibility(liveOptionsVisible ? View.VISIBLE : View.GONE);
		}

		int selectedLiveTimeMode = AppData.getDefaultLiveMode(getActivity());
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

		int color = dailyGameConfig.getUserColor();
		int days = dailyGameConfig.getDaysPerMove();
		int gameType = dailyGameConfig.getGameType();
		String isRated = dailyGameConfig.isRated() ? RestHelper.V_TRUE : RestHelper.V_FALSE;
		String opponentName = dailyGameConfig.getOpponentName();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_SEEKS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		if (!TextUtils.isEmpty(opponentName)) {
			loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);
		}

		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.online_game_created);
		}

		@Override
		public void errorHandle(String resultMessage) {
			showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);
		}
	}

	private void createLiveChallenge() {
		getActivityFace().openFragment(LiveGameWaitFragment.newInstance(liveGameConfigBuilder.build()));
	}

	private void widgetsInit(View view) {
		int darkBtnColor = getResources().getColor(R.color.stats_label_grey);
		if (getArguments().getInt(MODE) == CENTER_MODE) { // we use white background and dark titles for centered mode
			int darkTextColor = getResources().getColor(R.color.new_main_back);

			View homePlayScrollView = view.findViewById(R.id.homePlayScrollView);
			homePlayScrollView.setBackgroundResource(R.color.white);

			TextView liveChessHeaderTxt = (TextView) view.findViewById(R.id.liveChessHeaderTxt);
			Button liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
			TextView dailyChessHeaderTxt = (TextView) view.findViewById(R.id.dailyChessHeaderTxt);
			TextView vsRandomTxt = (TextView) view.findViewById(R.id.vsRandomTxt);
			TextView friendUserName1Txt = (TextView) view.findViewById(R.id.friendUserName1Txt);
			TextView friendRealName1Txt = (TextView) view.findViewById(R.id.friendRealName1Txt);
			TextView friendUserName2Txt = (TextView) view.findViewById(R.id.friendUserName2Txt);
			TextView friendRealName2Txt = (TextView) view.findViewById(R.id.friendRealName2Txt);
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

			int mode = AppData.getDefaultLiveMode(getActivity());
			darkBtnColor = getResources().getColor(R.color.text_controls_icons_white);
			// set texts to buttons
			newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
			for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
				int key = buttonEntry.getKey();
				buttonEntry.getValue().setText(getLiveModeButtonLabel(newGameButtonsArray[key]));
				buttonEntry.getValue().setOnClickListener(this);

				if (getArguments().getInt(MODE) == CENTER_MODE) {
					buttonEntry.getValue().setTextColor(darkBtnColor);
				}

				if (key == mode) {
					setDefaultQuickLiveMode(buttonEntry.getValue(), buttonEntry.getKey());
				}
			}
		}
	}

	private String getLiveModeButtonLabel(String label) {
		if (label.contains(StaticData.SYMBOL_SLASH)) { // "5 | 2"
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