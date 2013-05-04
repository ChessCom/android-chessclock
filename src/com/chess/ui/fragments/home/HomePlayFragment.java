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
import com.chess.ui.engine.configs.NewCompGameConfig;
import com.chess.ui.engine.configs.NewDailyGameConfig;
import com.chess.ui.engine.configs.NewLiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.daily.DailyGamesOptionsFragment;
import com.chess.ui.fragments.friends.InviteFriendsFragment;
import com.chess.ui.fragments.game.GameCompFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 18:29
 */
public class HomePlayFragment extends CommonLogicFragment {
	private static final String ERROR_TAG = "error popup";

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

	private TextView liveRatingTxt;
	private TextView dailyRatingTxt;
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private NewDailyGameConfig.Builder dailyGameConfigBuilder;
	private NewLiveGameConfig.Builder liveGameConfigBuilder;
	private int positionMode;
	private List<View> liveOptionsGroup;
	private boolean liveOptionsVisible;
	private Button standard1SelectBtn;
	private Button blitz1SelectBtn;
	private Button blitz2SelectBtn;
	private Button bullet1SelectBtn;
	private Button standard2SelectBtn;
	private Button blitz3SelectBtn;
	private Button blitz4SelectBtn;
	private Button bullet2SelectBtn;
	private List<Button> liveGameButtonsGroup;
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

		dailyGameConfigBuilder = new NewDailyGameConfig.Builder();
		liveGameConfigBuilder = new NewLiveGameConfig.Builder();
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
		}

		widgetsInit(view);

	}


	@Override
	public void onStart() {
		super.onStart();

		setRatings();
		// load friends, get only 2

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
			getActivityFace().openFragment(new LiveGameWaitFragment());
		} else if (view.getId() == R.id.autoMatchBtn) {
			createDailyChallenge(); // TODO adjust
		} else if (view.getId() == R.id.dailyPlayBtn) {
			createDailyChallenge(); // TODO adjust
		} else if (view.getId() == R.id.inviteFriend1Btn) {
			createDailyChallenge(); // TODO adjust
		} else if (view.getId() == R.id.inviteFriend2Btn) {
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
				getActivityFace().toggleMenu(SlidingMenu.RIGHT);
			}
		} else if (view.getId() == R.id.vsCompHeaderView) {
			NewCompGameConfig.Builder gameConfigBuilder = new NewCompGameConfig.Builder();
			NewCompGameConfig compGameConfig = gameConfigBuilder.setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE).build();
			getActivityFace().openFragment(GameCompFragment.newInstance(compGameConfig));
			if (positionMode == RIGHT_MENU_MODE) {
				getActivityFace().toggleMenu(SlidingMenu.RIGHT);
			}
		}

		handleLiveModeClicks(view);
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
		for (Button button : liveGameButtonsGroup) {
			button.setSelected(false);
		}

		int id = view.getId();
		if (id == R.id.standard1SelectBtn) {
			standard1SelectBtn.setSelected(true);
			liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[0]));
			liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[0]);
		} else if (id == R.id.blitz1SelectBtn) {
			blitz1SelectBtn.setSelected(true);
			liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[1]));
			liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[1]);
		} else if (id == R.id.blitz2SelectBtn) {
			blitz2SelectBtn.setSelected(true);
			liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[2]));
			liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[2]);
		} else if (id == R.id.bullet1SelectBtn) {
			bullet1SelectBtn.setSelected(true);
			liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[3]));
			liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[3]);
		} else if (id == R.id.standard2SelectBtn) {
			standard2SelectBtn.setSelected(true);
			liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[4]));
			liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[4]);
		} else if (id == R.id.blitz3SelectBtn) {
			blitz3SelectBtn.setSelected(true);
			liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[5]));
			liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[5]);
		} else if (id == R.id.blitz4SelectBtn) {
			blitz4SelectBtn.setSelected(true);
			liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[6]));
			liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[6]);
		} else if (id == R.id.bullet2SelectBtn) {
			bullet2SelectBtn.setSelected(true);
			liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[7]));
			liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[7]);
		}
	}

	private void toggleLiveOptionsView() {
		liveOptionsVisible = !liveOptionsVisible;
		for (View view : liveOptionsGroup) {
			view.setVisibility(liveOptionsVisible ? View.VISIBLE : View.GONE);
		}
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		NewDailyGameConfig newDailyGameConfig = dailyGameConfigBuilder.build();

		int color = newDailyGameConfig.getUserColor();
		int days = newDailyGameConfig.getDaysPerMove();
		int gameType = newDailyGameConfig.getGameType();
		String isRated = newDailyGameConfig.isRated() ? RestHelper.V_TRUE : RestHelper.V_FALSE;
		String opponentName = newDailyGameConfig.getOpponentName();

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

	private void widgetsInit(View view) {
		liveRatingTxt = (TextView) view.findViewById(R.id.liveRatingTxt);
		dailyRatingTxt = (TextView) view.findViewById(R.id.dailyRatingTxt);

		liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
		liveTimeSelectBtn.setOnClickListener(this);
		view.findViewById(R.id.livePlayBtn).setOnClickListener(this);
		view.findViewById(R.id.autoMatchBtn).setOnClickListener(this);
		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.inviteFriend1Btn).setOnClickListener(this);
		view.findViewById(R.id.inviteFriend2Btn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.liveHeaderView).setOnClickListener(this);
		view.findViewById(R.id.dailyHeaderView).setOnClickListener(this);
		view.findViewById(R.id.vsCompHeaderView).setOnClickListener(this);

		{ // live options
			if (HONEYCOMB_PLUS_API) {
				ViewGroup liveOptionsView = (ViewGroup) view.findViewById(R.id.homePlayLinLay);
				LayoutTransition layoutTransition = liveOptionsView.getLayoutTransition();
				layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
			}

			View liveLabelStandardTxt = view.findViewById(R.id.liveLabelStandardTxt);
			View liveLabelBlitzTxt = view.findViewById(R.id.liveLabelBlitzTxt);
			View liveLabelBulletTxt = view.findViewById(R.id.liveLabelBulletTxt);

			standard1SelectBtn = (Button) view.findViewById(R.id.standard1SelectBtn);
			blitz1SelectBtn = (Button) view.findViewById(R.id.blitz1SelectBtn);
			blitz2SelectBtn = (Button) view.findViewById(R.id.blitz2SelectBtn);
			bullet1SelectBtn = (Button) view.findViewById(R.id.bullet1SelectBtn);
			standard2SelectBtn = (Button) view.findViewById(R.id.standard2SelectBtn);
			blitz3SelectBtn = (Button) view.findViewById(R.id.blitz3SelectBtn);
			blitz4SelectBtn = (Button) view.findViewById(R.id.blitz4SelectBtn);
			bullet2SelectBtn = (Button) view.findViewById(R.id.bullet2SelectBtn);

			liveOptionsGroup = new ArrayList<View>();
			liveOptionsGroup.add(liveLabelStandardTxt);
			liveOptionsGroup.add(liveLabelBlitzTxt);
			liveOptionsGroup.add(liveLabelBulletTxt);
			liveOptionsGroup.add(standard1SelectBtn);
			liveOptionsGroup.add(blitz1SelectBtn);
			liveOptionsGroup.add(blitz2SelectBtn);
			liveOptionsGroup.add(bullet1SelectBtn);
			liveOptionsGroup.add(standard2SelectBtn);
			liveOptionsGroup.add(blitz3SelectBtn);
			liveOptionsGroup.add(blitz4SelectBtn);
			liveOptionsGroup.add(bullet2SelectBtn);

			// set texts to buttons
			newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
			standard1SelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[0]));
			blitz1SelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[1]));
			blitz2SelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[2]));
			bullet1SelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[3]));
			standard2SelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[4]));
			blitz3SelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[5]));
			blitz4SelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[6]));
			bullet2SelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[7]));

			liveGameButtonsGroup = new ArrayList<Button>();
			liveGameButtonsGroup.add(standard1SelectBtn);
			liveGameButtonsGroup.add(blitz1SelectBtn);
			liveGameButtonsGroup.add(blitz2SelectBtn);
			liveGameButtonsGroup.add(bullet1SelectBtn);
			liveGameButtonsGroup.add(standard2SelectBtn);
			liveGameButtonsGroup.add(blitz3SelectBtn);
			liveGameButtonsGroup.add(blitz4SelectBtn);
			liveGameButtonsGroup.add(bullet2SelectBtn);

			standard1SelectBtn.setOnClickListener(this);
			blitz1SelectBtn.setOnClickListener(this);
			blitz2SelectBtn.setOnClickListener(this);
			bullet1SelectBtn.setOnClickListener(this);
			standard2SelectBtn.setOnClickListener(this);
			blitz3SelectBtn.setOnClickListener(this);
			blitz4SelectBtn.setOnClickListener(this);
			bullet2SelectBtn.setOnClickListener(this);
		}
	}

	private String getLiveModeButtonLabel(String label) {
		if (label.contains(StaticData.SYMBOL_SLASH)) { // "5 | 2")
			return label;
		} else { // "10 min")
			return getString(R.string.min_arg, label);
		}
	}
}
