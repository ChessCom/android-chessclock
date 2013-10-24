package com.chess.ui.fragments.live;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import com.chess.backend.LiveChessService;
import com.chess.lcc.android.DataNotValidException;
import com.chess.model.GameLiveItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.utilities.LogMe;


public class LiveTopGameFragment extends GameLiveFragment {

	// todo: adjust LiveTopGameFragment and GameLiveFragment, lock board, load avatars, game end, chat, options dialog etc

	private static final String TAG = "LccLog-LiveTopGameFragment";

	public LiveTopGameFragment() {
	}

	public static LiveTopGameFragment createInstance(/*long id*/) {
		LiveTopGameFragment fragment = new LiveTopGameFragment();
		/*Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, id);
		fragment.setArguments(bundle);*/
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			LiveChessService liveService = getLiveService();
			liveService.setGameTaskListener(gameTaskListener);
			liveService.setLccEventListener(this);

			liveService.observeTopGame();

		} catch (DataNotValidException e) {
			logLiveTest(e.getMessage());
		}

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
			controlsLiveView.enableAnalysisMode(true);
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

	protected void logLiveTest(String messageToLog) {
		LogMe.dl(TAG, "LIVE OBSERVE TOP GAME FRAGMENT: " + messageToLog);
	}
}
