package com.chess.lcc.android;

import android.content.Context;
import android.util.Log;
import com.chess.live.client.Game;
import com.chess.live.client.GameListener;
import com.chess.live.client.User;
import com.chess.live.rules.GameResult;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;

import java.util.Collection;
import java.util.List;

import static com.chess.live.rules.GameResult.WIN;

public class LccGameListener implements GameListener {

	private LccHelper lccHelper;
	private Long latestGameId = 0L;
	private Context context;
	private static final String TAG = "LccLog-GAME";

	public LccGameListener(LccHelper lccHelper) {
		this.lccHelper = lccHelper;
		context = lccHelper.getContext();
	}

	public void onGameListReceived(Collection<? extends Game> games) {
		Log.d(TAG, "Game list received, total size = " + games.size());
		latestGameId = 0L;

		Long gameId;
		for (Game game : games) {
			gameId = game.getId();
			if (!isMyGame(game)) {
				lccHelper.getClient().unobserveGame(gameId);
				Log.d(TAG, "unobserve game " + gameId);
			}
			else if (gameId > latestGameId) {
				latestGameId = gameId;
			}
		}

		Log.d(TAG, "latestGameId=" + latestGameId);
	}

	@Override
	public void onGameArchiveReceived(User user, Collection<? extends Game> games) {
	}

	@Override
	public void onGameReset(Game game) {
		Log.d(TAG, "GAME LISTENER: onGameReset id=" + game.getId() + ", game=" + game);

		// check isMyTurn

		if (!isActualGame(game)) {
			return;
		}

		doResetGame(game);
		doUpdateGame(false, game);
	}

	@Override
	public void onGameUpdated(Game game) {
		Log.d(TAG, "GAME LISTENER: onGameUpdated id=" + game.getId() + ", game=" + game);

		// check isMyTurn

		if (!isActualGame(game)) {
			return;
		}

		doUpdateGame(true, game);
	}

	@Override
	public void onGameOver(Game game) {
		Log.d(TAG, "GAME LISTENER: onGameOver " + game);
		doEndGame(game);
	}

	@Override
	public void onGameClockAdjusted(Game game, User player, Integer newClockValue, Integer clockAdjustment, Integer resultClock) {
		Log.d(TAG, "Game Clock adjusted: gameId=" + game.getId() + ", player=" + player.getUsername() +
				", newClockValue=" + newClockValue + ", clockAdjustment=" + clockAdjustment);
	}

	@Override
	public void onGameComputerAnalysisRequested(Long aLong, boolean b, String s) {
	}

	private boolean isOldGame(Long gameId) {
		return gameId < latestGameId;
	}

	private boolean isActualGame(Game game) {
		Long gameId = game.getId();

		if (!isMyGame(game)) { // TODO: check case
			lccHelper.getClient().unobserveGame(gameId);
			Log.d(TAG, "GAME LISTENER: unobserve game " + gameId);
			return false;
		} else
		if (lccHelper.isUserPlayingAnotherGame(gameId)) {
			Log.d(TAG, "GAME LISTENER: abort and exit second game");
			lccHelper.getClient().abortGame(game, "abort second game");
			lccHelper.getClient().exitGame(game);
			return false;
		} else if (isOldGame(gameId)) { // TODO: check case
			Log.d(TAG, "GAME LISTENER: exit old game");
			lccHelper.getClient().exitGame(game);
			return false;
		} else {
			lccHelper.clearOwnChallenges();
			lccHelper.clearChallenges();
			lccHelper.clearSeeks();
			//lccHelper.getClient().unsubscribeFromSeekList(lccHelper.getSeekListSubscriptionId());
			lccHelper.setCurrentGameId(gameId);
			if (gameId > latestGameId) {
				latestGameId = gameId;
				Log.d(TAG, "GAME LISTENER: latestGameId=" + gameId);
			}
			return true;
		}
	}

	private void doResetGame(Game game) {
		//lccHelper.setLatestGame(game);
		if (game.isGameOver()) {
			lccHelper.putGame(game);
			return;
		}
		lccHelper.setCurrentGameId(game.getId());
		lccHelper.setGameActivityPausedMode(true);
		lccHelper.processFullGame(game);
	}

	private void doUpdateGame(boolean checkMoves, Game game) {

		// todo: maybe create two separate methods: new move check and draw check

		if (checkMoves && game.getMoveCount() > lccHelper.getLatestMoveNumber()) { // do not check moves if it was
			User moveMaker = game.getLastMoveMaker();
			String move = game.getLastMove();
			Log.d(TAG, "GAME LISTENER: The move #" + game.getMoveCount() + " received by user: " + lccHelper.getUser().getUsername() +
					", game.id=" + game.getId() + ", mover=" + moveMaker.getUsername() + ", move=" + move + ", allMoves=" + game.getMoves());
			lccHelper.doMoveMade(game, moveMaker, game.getMoveCount() - 1);
		}

		final String opponentName = lccHelper.getOpponentName();

		if (opponentName != null && game.isDrawOfferedByPlayer(opponentName)) { // check if game is not ended?
			Log.d(TAG, "GAME LISTENER: Draw offered at the move #" + game.getMoveCount() + ", game.id=" + game.getId()
					+ ", offerer=" + opponentName + ", game=" + game);

			if (lccHelper.isGameActivityPausedMode()) {
				final LiveGameEvent drawOfferedEvent = new LiveGameEvent();
				drawOfferedEvent.setEvent(LiveGameEvent.Event.DRAW_OFFER);
				drawOfferedEvent.setGameId(game.getId());
				drawOfferedEvent.setDrawOffererUsername(opponentName);
				lccHelper.getPausedActivityGameEvents().put(drawOfferedEvent.getEvent(), drawOfferedEvent);
				Log.d(TAG, "DRAW PAUSED");
			} else {
				Log.d(TAG, "DRAW SHOW");
				lccHelper.getLccEventListener().onDrawOffered(opponentName);
			}
		}
	}

	private void doEndGame(Game game) {
		lccHelper.putGame(game);

		Long gameId = game.getId();

		if (isOldGame(gameId)) {
			Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + gameId);
			return;
		}

        /*lccHelper.getClient().subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1,
                                                        lccHelper.getSeekListListener());*/
		Long lastGameId = lccHelper.getCurrentGameId() != null ? lccHelper.getCurrentGameId() : gameId;
		lccHelper.setLastGameId(lastGameId);

		List<GameResult> gameResults = game.getResults();
		final GameResult whitePlayerResult = gameResults.get(0);
		final GameResult blackPlayerResult = gameResults.get(1);
		final String whiteUsername = game.getWhitePlayer().getUsername();
		final String blackUsername = game.getBlackPlayer().getUsername();

		GameResult result;
		String winnerUsername = null;

		if (whitePlayerResult == WIN) {
			result = blackPlayerResult;
			winnerUsername = whiteUsername;
		} else if (blackPlayerResult == WIN) {
			result = whitePlayerResult;
			winnerUsername = blackUsername;
		} else {
			result = whitePlayerResult;
		}

		String message = StaticData.SYMBOL_EMPTY;
		switch (result) {
			case TIMEOUT:
				message = winnerUsername + StaticData.SYMBOL_SPACE + context.getString(R.string.won_on_time);
				break;
			case RESIGNED:
				message = winnerUsername + StaticData.SYMBOL_SPACE + context.getString(R.string.won_by_resignation);
				break;
			case CHECKMATED:
				message = winnerUsername + StaticData.SYMBOL_SPACE + context.getString(R.string.won_by_checkmate);
				break;
			case DRAW_BY_REPETITION:
				message = context.getString(R.string.game_draw_by_repetition);
				break;
			case DRAW_AGREED:
				message = context.getString(R.string.game_drawn_by_agreement);
				break;
			case STALEMATE:
				message = context.getString(R.string.game_drawn_by_stalemate);
				break;
			case DRAW_BY_INSUFFICIENT_MATERIAL:
				message = context.getString(R.string.game_drawn_insufficient_material);
				break;
			case DRAW_BY_50_MOVE:
				message = context.getString(R.string.game_drawn_by_fifty_move_rule);
				break;
			case ABANDONED:
				message = winnerUsername + StaticData.SYMBOL_SPACE + context.getString(R.string.won_game_abandoned);
				break;
			case ABORTED:
				message = context.getString(R.string.game_aborted);
				break;
		}
		//message = whiteUsername + " vs. " + blackUsername + " - " + message;
		Log.d(TAG, "GAME LISTENER: " + message);

		if (lccHelper.getWhiteClock() != null) {
			lccHelper.getWhiteClock().setRunning(false);
		}
		if (lccHelper.getBlackClock() != null) {
			lccHelper.getBlackClock().setRunning(false);
		}

		String abortedCodeMessage = game.getCodeMessage(); // used only for aborted games
		if (abortedCodeMessage != null) {
			final String messageI18n = AppUtils.getI18nString(context, abortedCodeMessage, game.getAborterUsername());
			if (messageI18n != null) {
				message = messageI18n;
			}
		}

		if (lccHelper.getCurrentGameId() == null) {
			lccHelper.setCurrentGameId(gameId);
		}

		if (lccHelper.isGameActivityPausedMode()) {
			Log.d(TAG, "ActivityPausedMode = true");
			final LiveGameEvent gameEndedEvent = new LiveGameEvent();
			gameEndedEvent.setGameId(gameId);
			gameEndedEvent.setEvent(LiveGameEvent.Event.END_OF_GAME);
			gameEndedEvent.setGameEndedMessage(message);
			lccHelper.getPausedActivityGameEvents().put(gameEndedEvent.getEvent(), gameEndedEvent);
			if (lccHelper.getLccEventListener() == null) { // if activity is not started yet
				lccHelper.processFullGame(game);
				Log.d(TAG, "processFullGame");
			}
		} else {
			lccHelper.getLccEventListener().onGameEnd(message);
		}
	}

    /*public void onDrawRejected(Game game, User rejector) {
        final String rejectorUsername = (rejector != null ? rejector.getUsername() : null);
        Log.d(TAG, "GAME LISTENER: Draw rejected at the move #" + game.getMoveCount() +
                        ", game.id=" + game.getId() + ", rejector=" + rejectorUsername + ", game=" + game);
        if (!rejectorUsername.equals(lccHelper.getUser().getUsername())) {
			lccHelper.getLccEventListener().onInform(context.getString(R.string.draw_declined),
					rejectorUsername + StaticData.SYMBOL_SPACE + context.getString(R.string.has_declined_draw));
        }
    }*/

	private boolean isMyGame(Game game) {
		String whiteUsername = game.getWhitePlayer().getUsername().toLowerCase();
		String blackUsername = game.getBlackPlayer().getUsername().toLowerCase();
		String userName = lccHelper.getUsername().toLowerCase();

		boolean isMyGame = userName.equals(whiteUsername) || userName.equals(blackUsername);

		if (!isMyGame) {
			Log.d(TAG, "not own game " + game.getId());
		}

		return isMyGame;
	}
}
