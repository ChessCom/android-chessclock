package com.chess.ui.fragments.live;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.lcc.android.DataNotValidException;
import com.chess.model.GameAnalysisItem;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.fragments.game.GameAnalyzeFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupGameEndFragment;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.utilities.LogMe;
import com.chess.widgets.RoboButton;


public class GameLiveObserveFragment extends GameLiveFragment {

	// todo: adjust GameLiveObserveFragment and GameLiveFragment, lock board, load avatars, game end, chat, options dialog etc

	private static final String TAG = "LccLog-GameLiveObserveFragment";
	private static final long HIDE_POPUP_DELAY = 4000;
	private ObserveTaskListener observeTaskListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		observeTaskListener = new ObserveTaskListener();
		try {
			LiveChessService liveService = getLiveService();
			liveService.setLccObserveEventListener(this);

			liveService.runObserveTopGameTask(observeTaskListener);

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

		LiveChessService liveService = getLiveService();
		GameLiveItem currentGame = liveService.getGameItem();
		if (currentGame == null) {
			return;
		}

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		boardView.updateBoardAndPiecesImgs();
		//notationsView.resetNotations();
		enableScreenLockTimer();
		if (!liveService.isActiveGamePresent()) {
			controlsView.enableAnalysisMode(true);
			getBoardFace().setFinished(true);
		}
		liveService.setLccChatMessageListener(this);

		{// fill labels
			labelsConfig = new LabelsConfig();
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
			labelsConfig.topPlayerName = currentGame.getBlackUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getBlackRating());
			labelsConfig.bottomPlayerName = currentGame.getWhiteUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getWhiteRating());
		}

		{// set avatars
			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			String topAvatarUrl = liveService.getCurrentGame()
					.getOpponentForPlayer(currentGame.getWhiteUsername()).getAvatarUrl();
			String bottomAvatarUrl = liveService.getCurrentGame()
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

		{ // get opponent info
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), labelsConfig.topPlayerName);
			new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.TOP_PLAYER)).executeTask(loadItem);
		}
		{ // get users info
			LoadItem loadItem = LoadHelper.getUserInfo(labelsConfig.bottomPlayerName);
			new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.BOTTOM_PLAYER)).executeTask(loadItem);
		}

		/*int resignTitleId = liveService.getResignTitle();
		{// options list setup
			optionsMap = new SparseArray<String>();
			optionsMap.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsMap.put(ID_OFFER_DRAW, getString(R.string.offer_draw));
			optionsMap.put(ID_ABORT_RESIGN, getString(resignTitleId));
			optionsMap.put(ID_REMATCH, getString(R.string.rematch));
			optionsMap.put(ID_SETTINGS, getString(R.string.settings));
		}*/

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
				boardView.lockBoard(true);
			}
		});
	}

	@Override
	public void showOptions() {
		GameAnalysisItem analysisItem = new GameAnalysisItem();  // TODO reuse later
		analysisItem.setGameType(RestHelper.V_GAME_CHESS);
		analysisItem.setFen(getBoardFace().generateFullFen());
		analysisItem.setMovesList(getBoardFace().getMoveListSAN());
		analysisItem.copyLabelConfig(labelsConfig);

		getActivityFace().openFragment(GameAnalyzeFragment.createInstance(analysisItem));
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.newGamePopupBtn) {
			getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));
		} else if (view.getId() == R.id.rematchPopupBtn) {
			if (isLCSBound) {
				LiveChessService liveService;
				try {
					liveService = getLiveService();
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
					return;
				}
				liveService.rematch();
			}
			dismissEndGameDialog();
		} else {
			super.onClick(view);
		}
	}

	@Override
	protected void showGameEndPopup(View layout, String title, String message) {
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
				dismissEndGameDialog();

				try {
					getLiveService().runObserveTopGameTask(observeTaskListener);
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
		TextView rematchPopupBtn = (TextView) layout.findViewById(R.id.rematchPopupBtn);
		rematchPopupBtn.setText(R.string.new_game);
		rematchPopupBtn.setOnClickListener(this);

		// Next Game (Top)
		RoboButton newGamePopupBtn = (RoboButton) layout.findViewById(R.id.newGamePopupBtn);
		newGamePopupBtn.setDrawableStyle(R.style.Button_Green);
		newGamePopupBtn.setText(R.string.next_game);
		newGamePopupBtn.setOnClickListener(this);

		layout.findViewById(R.id.analyzePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.sharePopupBtn).setOnClickListener(this);


		/*if (AppUtils.isNeedToUpgrade(getActivity())) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}*/
	}

	@Override
	public void expireGame() {
		goHome();
	}

	@Override
	public void goHome() {
		try {
			getLiveService().exitGameObserving();
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

}
