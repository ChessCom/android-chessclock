package com.chess.ui.fragments.live;

import android.database.Cursor;
import android.os.Bundle;
import com.chess.R;
import com.chess.backend.entity.api.LiveArchiveGameData;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.BaseGameItem;
import com.chess.model.DataHolder;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.daily.GameDailyAnalysisFragment;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.09.13
 * Time: 8:41
 */
public class GameLiveArchiveAnalysisFragment extends GameDailyAnalysisFragment {

	private LoadFromDbUpdateListener loadFromDbUpdateListener;
	private LiveArchiveGameData currentGame;

	public static GameLiveArchiveAnalysisFragment createInstance(long gameId) {
		GameLiveArchiveAnalysisFragment fragment = new GameLiveArchiveAnalysisFragment();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	protected void loadGame() {
		// load game from DB. After load update
		new LoadDataFromDbTask(loadFromDbUpdateListener, DbHelper.getLiveArchiveGame(gameId, getUsername()),
				getContentResolver()).executeTask();
	}

	@Override
	protected void init() {
		super.init();
		labelsConfig = new LabelsConfig();

		loadFromDbUpdateListener = new LoadFromDbUpdateListener();
	}

	protected class LoadFromDbUpdateListener extends AbstractUpdateListener<Cursor> {

		public LoadFromDbUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			currentGame = DbDataManager.getLiveArchiveGameFromCursor(returnedObj);
			returnedObj.close();

			adjustBoardForGame();
		}
	}

	@Override
	protected void adjustBoardForGame() {
		userPlayWhite = currentGame.getWhiteUsername().equals(getAppData().getUsername());

		labelsConfig.topAvatar = opponentAvatarDrawable;
		labelsConfig.bottomAvatar = userAvatarDrawable;

		if (userPlayWhite) {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
			labelsConfig.topPlayerName = currentGame.getBlackUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getBlackRating());
			labelsConfig.bottomPlayerName = currentGame.getWhiteUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getWhiteRating());
			labelsConfig.topPlayerAvatar = currentGame.getBlackAvatar();
			labelsConfig.bottomPlayerAvatar = currentGame.getWhiteAvatar();
			labelsConfig.topPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, currentGame.getBlackUserCountry());
			labelsConfig.bottomPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, currentGame.getWhiteUserCountry());
			labelsConfig.topPlayerPremiumStatus = currentGame.getBlackPremiumStatus();
			labelsConfig.bottomPlayerPremiumStatus = currentGame.getWhitePremiumStatus();
		} else {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
			labelsConfig.topPlayerName = currentGame.getWhiteUsername();
			labelsConfig.topPlayerRating = String.valueOf(currentGame.getWhiteRating());
			labelsConfig.bottomPlayerName = currentGame.getBlackUsername();
			labelsConfig.bottomPlayerRating = String.valueOf(currentGame.getBlackRating());
			labelsConfig.topPlayerAvatar = currentGame.getWhiteAvatar();
			labelsConfig.bottomPlayerAvatar = currentGame.getBlackAvatar();
			labelsConfig.topPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, currentGame.getWhiteUserCountry());
			labelsConfig.bottomPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, currentGame.getBlackUserCountry());
			labelsConfig.topPlayerPremiumStatus = currentGame.getWhitePremiumStatus();
			labelsConfig.bottomPlayerPremiumStatus = currentGame.getBlackPremiumStatus();
		}

		DataHolder.getInstance().setInOnlineGame(currentGame.getGameId(), true);

		controlsView.enableGameControls(true);
		boardView.lockBoard(false);

		getBoardFace().setFinished(false);

		long secondsRemain = currentGame.getTimeRemaining();
		String timeRemains;
		if (secondsRemain == 0) {
			timeRemains = getString(R.string.less_than_60_sec);
		} else {
			timeRemains = AppUtils.getTimeLeftFromSeconds(secondsRemain, getActivity());
		}

		String defaultTime = getString(R.string.days_arg, currentGame.getDaysPerMove());
//		boolean userMove = isUserMove();
		boolean userMove = true;
		if (userMove) {
			labelsConfig.topPlayerTime = defaultTime;
			labelsConfig.bottomPlayerTime = timeRemains;
		} else {
			labelsConfig.topPlayerTime = timeRemains;
			labelsConfig.bottomPlayerTime = defaultTime;
		}

		topPanelView.showTimeLeftIcon(!userMove);
		bottomPanelView.showTimeLeftIcon(userMove);

		ChessBoardOnline.resetInstance();
		BoardFace boardFace = getBoardFace();
		if (currentGame.getGameType() == BaseGameItem.CHESS_960) {
			boardFace.setChess960(true);
		}

		boardFace.setupBoard(currentGame.getStartingFenPosition());
		if (!userPlayWhite) {
			boardFace.setReside(true);
		}

		boardFace.checkAndParseMovesList(currentGame.getMoveList());

		boardView.resetValidMoves();

		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

		boardFace.setJustInitialized(false);
	}

}
