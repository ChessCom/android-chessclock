package com.chess.ui.fragments.live;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.live.client.Game;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.popup_fragments.PopupGameEndFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsLiveChessFragment;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.LogMe;
import com.chess.utilities.MopubHelper;
import com.chess.widgets.ProfileImageView;
import com.chess.widgets.RoboButton;


public class GameLiveObserveFragment extends GameLiveFragment {

	// Options ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_SETTINGS = 1;

	private static final String TAG = "LccLog-GameLiveObserveFragment";
	private static final long HIDE_POPUP_DELAY = 4000;
	private ObserveTaskListener observeTaskListener;
	private PopupOptionsMenuFragment optionsSelectFragment;

	public static GameLiveFragment createInstance(long id) {
		GameLiveFragment fragment = new GameLiveObserveFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		observeTaskListener = new ObserveTaskListener();
		try {
			runNewObserverGame();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.top_game);

		widgetsInit(view);
		boardView.lockBoard(true);
		boardView.lockBoardControls(false);

		enableSlideMenus(false);
	}

	@Override
	protected void onGameStarted() throws DataNotValidException {
		init();
		super.onGameStarted();

		getControlsView().showDefault();
		getControlsView().showHome(true);
	}

	@Override
	protected void init() throws DataNotValidException {

		LiveConnectionHelper liveHelper = getLiveHelper();
		GameLiveItem currentGame = liveHelper.getGameItem();
		if (currentGame == null) {
			return;
		}

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		boardView.updateBoardAndPiecesImgs();
		//notationsView.resetNotations();
		enableScreenLockTimer();
		if (!liveHelper.isActiveGamePresent()) {
			controlsView.enableAnalysisMode(true);
			getBoardFace().setFinished(true);
		}
		liveHelper.setLccChatMessageListener(this);

		{// fill labels
			labelsConfig = new LabelsConfig();
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
			labelsConfig.topPlayerName = currentGame.getBlackUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getBlackRating());
			labelsConfig.bottomPlayerName = currentGame.getWhiteUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getWhiteRating());
		}

		{// set avatars
			topAvatarImg = (ProfileImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ProfileImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			String topAvatarUrl = liveHelper.getCurrentGame()
					.getOpponentForPlayer(currentGame.getWhiteUsername()).getAvatarUrl();
			String bottomAvatarUrl = liveHelper.getCurrentGame()
					.getOpponentForPlayer(currentGame.getBlackUsername()).getAvatarUrl();

			if (topAvatarUrl != null && !topAvatarUrl.contains(StaticData.GIF)) {
				imageDownloader.download(topAvatarUrl, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
			} else {
				Drawable src = new IconDrawable(getActivity(), R.string.ic_profile,
						R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
				labelsConfig.topAvatar = new BoardAvatarDrawable(getActivity(), src);

				labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
				topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
				topPanelView.invalidate();
			}

			if (bottomAvatarUrl != null && !bottomAvatarUrl.contains(StaticData.GIF)) {
				imageDownloader.download(bottomAvatarUrl, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);
			} else {
				Drawable src = new IconDrawable(getActivity(), R.string.ic_profile,
						R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
				labelsConfig.bottomAvatar = new BoardAvatarDrawable(getActivity(), src);

				labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
				bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
				bottomPanelView.invalidate();
			}
		}

		{ // get top player info
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), labelsConfig.topPlayerName);
			new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.TOP_PLAYER)).executeTask(loadItem);
		}
		{ // get bottom player info
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), labelsConfig.bottomPlayerName);
			new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.BOTTOM_PLAYER)).executeTask(loadItem);
		}

		optionsMapInit();

		lccInitiated = true;
	}

	@Override
	protected void logLiveTest(String messageToLog) {
		LogMe.dl(TAG, "LIVE OBSERVE TOP GAME FRAGMENT: " + messageToLog);
	}

	@Override
	protected void blockGame(final boolean block) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null || fadeLay == null) {
					return;
				}
				showLoadingProgress(block);
				fadeLay.setVisibility(block ? View.VISIBLE : View.INVISIBLE);
				boardView.lockBoard(true);
			}
		});
	}

	@Override
	public void showOptions() {
		if (optionsSelectFragment != null) {
			return;
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsMap);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
			try {
				runNewObserverGame();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				showToast(e.getMessage());
			}
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(SettingsLiveChessFragment.createInstance(true));
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.newGamePopupBtn) { // Next Game
			dismissEndGameDialog();
			try {
				runNewObserverGame();
			} catch (DataNotValidException e) {
				logLiveTest(e.getMessage());
				showToast(e.getMessage());
			}
		} else if (view.getId() == R.id.rematchPopupBtn) { // New Game Self
			dismissEndGameDialog();
			try {
				getLiveHelper().exitGameObserving();
			} catch (DataNotValidException e) {
				e.printStackTrace();
				getActivityFace().showPreviousFragment();
				return;
			}

			LiveGameConfig liveGameConfig = getAppData().getLiveGameConfigBuilder().build();
			getActivityFace().openFragment(LiveGameWaitFragment.createInstance(liveGameConfig));
		} else {
			super.onClick(view);
		}
	}

	@Override
	protected void showGameEndPopup(View layout, String title, String message, Game game) {
		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);

		endGameTitleTxt.setText(title);
		endGameReasonTxt.setText(message);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(layout);

		PopupGameEndFragment endPopupFragment = PopupGameEndFragment.createInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		// hide popup after 2 seconds and go to next game!
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				dismissEndGameDialog();

				try {
					runNewObserverGame();
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
					showToast(e.getMessage());
					getActivityFace().showPreviousFragment();
				}
			}
		}, HIDE_POPUP_DELAY);


		// New Rating
		View ratingTitleTxt = layout.findViewById(R.id.ratingTitleTxt);
		ratingTitleTxt.setVisibility(View.GONE);

		// New Game (Self)
		int mode = getAppData().getDefaultLiveMode();
		// set texts to buttons
		String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
		String newGameStr = getString(R.string.new_arg, AppUtils.getLiveModeButtonLabel(newGameButtonsArray[mode], getContext()));
		Button newGameButton = (Button) layout.findViewById(R.id.rematchPopupBtn);
		newGameButton.setText(newGameStr);
		newGameButton.setOnClickListener(this);

		// Next Game (Top)
		RoboButton newGamePopupBtn = (RoboButton) layout.findViewById(R.id.newGamePopupBtn);
		newGamePopupBtn.setDrawableStyle(R.style.Button_Green);
		newGamePopupBtn.setText(R.string.next_game);
		newGamePopupBtn.setOnClickListener(this);

		layout.findViewById(R.id.analyzePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.sharePopupBtn).setOnClickListener(this);

		if (isNeedToUpgrade()) {
			initPopupAdWidget(layout);
			MopubHelper.showRectangleAd(getMopubRectangleAd(), getActivity());
		}
	}

	private void runNewObserverGame() throws DataNotValidException {
		LiveConnectionHelper liveHelper = getLiveHelper();

		if (!liveHelper.isUserPlaying()) {

			liveHelper.exitGameObserving();
			liveHelper.setLccObserveEventListener(this);

			liveHelper.runObserveTopGameTask(observeTaskListener);
		}
	}

	@Override
	public void startGameFromService() {
		logLiveTest("startGameFromService");

		final LiveConnectionHelper liveHelper;
		try {
			liveHelper = getLiveHelper();
		} catch (DataNotValidException e) {
			logTest(e.getMessage());
			showToast(e.getMessage());
			return;
		}

		boolean needToChangeFragment = !liveHelper.isCurrentGameObserved();
		checkFragmentAndStartGame(needToChangeFragment, liveHelper);
	}

	@Override
	public void expireGame() {
		logTest("expireGame");

		goHome();
	}

	@Override
	public void goHome() {
		logTest("goHome");
		try {
			getLiveHelper().exitGameObserving();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
		}
		super.goHome();
	}

	private class ObserveTaskListener extends ChessLoadUpdateListener<Void> {
		public ObserveTaskListener() {
			super();
		}
	}

	@Override
	public boolean isUserAbleToMove(int color) {
		return false;
	}

	@Override
	public boolean isObservingMode() {
		return true;
	}

	@Override
	protected void optionsMapInit() {
		optionsMap = new SparseArray<String>();
		optionsMap.put(ID_NEW_GAME, getString(R.string.new_game));
		optionsMap.put(ID_SETTINGS, getString(R.string.settings));
	}
}
