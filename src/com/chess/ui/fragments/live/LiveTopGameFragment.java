package com.chess.ui.fragments.live;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.lcc.android.DataNotValidException;
import com.chess.model.GameLiveItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.fragments.popup_fragments.PopupGameEndFragment;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.utilities.LogMe;


public class LiveTopGameFragment extends GameLiveFragment {

	// todo: adjust LiveTopGameFragment and GameLiveFragment, lock board, load avatars, game end, chat, options dialog etc

	private static final String TAG = "LccLog-LiveTopGameFragment";

	public LiveTopGameFragment() {
	}

	public static LiveTopGameFragment createInstance() {
		LiveTopGameFragment fragment = new LiveTopGameFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			LiveChessService liveService = getLiveService();
			liveService.setGameTaskListener(gameTaskListener);
			liveService.setLccEventListener(this);
			liveService.setLccObserveEventListener(this);

			liveService.runObserveTopGameTask();

		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live);

		widgetsInit(view);
		blockGame(true);
		/*try {
			init();
		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
		}*/
		enableSlideMenus(false);
	}

	@Override
	public void startGameFromService() {

		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					init();
				} catch (DataNotValidException e) {
					logLiveTest(e.getMessage());
				}

			}
		});

		super.startGameFromService();
	}

	@Override
	protected void init() throws DataNotValidException {

		LiveChessService liveService = getLiveService();
		GameLiveItem currentGame = liveService.getGameItem();
		if (currentGame == null) {
			throw new DataNotValidException(DataNotValidException.GAME_NOT_EXIST);
		}

		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName());
		boardView.updateBoardAndPiecesImgs();
		//notationsView.resetNotations();
		enableScreenLockTimer();
		if (!liveService.isCurrentGameExist()) {
			controlsView.enableAnalysisMode(true);
			getBoardFace().setFinished(true);
		}
		//liveService.setLccEventListener(this);
		liveService.setLccChatMessageListener(this);
		//liveService.setGameTaskListener(gameTaskListener);

		{// fill labels
			labelsConfig = new LabelsConfig();
			//if (isUserColorWhite()) {
				labelsConfig.userSide = ChessBoard.WHITE_SIDE;
				labelsConfig.topPlayerName = currentGame.getBlackUsername();
				labelsConfig.topPlayerRating = String.valueOf(currentGame.getBlackRating());
				labelsConfig.bottomPlayerName = currentGame.getWhiteUsername();
				labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getWhiteRating());
			/*} else {
				labelsConfig.userSide = ChessBoard.BLACK_SIDE;
				labelsConfig.topPlayerName = currentGame.getWhiteUsername();
				labelsConfig.topPlayerRating = String.valueOf(currentGame.getWhiteRating());
				labelsConfig.bottomPlayerName = currentGame.getBlackUsername();
				labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getBlackRating());
			}*/
		}

		{// set avatars
			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			/*String opponentName;
			if (isUserColorWhite()) {
				opponentName = currentGame.getBlackUsername();
			} else {
				opponentName = currentGame.getWhiteUsername();
			}

			String opponentAvatarUrl = liveService.getCurrentGame().getOpponentForPlayer(opponentName).getAvatarUrl(); // TODO test
			imageDownloader.download(opponentAvatarUrl, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);*/
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
				boardView.lockBoard(true); // todo: do not lock controls
			}
		});
	}

	@Override
	public void showOptions() {
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

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		//layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.analyzePopupBtn).setOnClickListener(this);
		//layout.findViewById(R.id.sharePopupBtn).setOnClickListener(this);

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
}
