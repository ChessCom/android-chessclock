package com.chess.lcc.android;

import android.content.Context;
import android.util.Log;
import com.chess.live.client.Game;
import com.chess.live.client.GameListener;
import com.chess.live.client.User;
import com.chess.statics.AppConstants;

import java.util.Collection;

public class LccGameListener implements GameListener {

	private static final String TAG = "LccLog-GAME";

	private LccHelper lccHelper;
	private Long latestGameId = 0L;
	private Context context;

	public LccGameListener(LccHelper lccHelper) {
		this.lccHelper = lccHelper;
		context = lccHelper.getContext();
	}

	@Override
	public void onGameListReceived(Collection<? extends Game> games) {
		Log.d(TAG, "Game list received, total size = " + games.size());

		Long previousGameId = latestGameId;
		latestGameId = 0L;

		Long gameId;
		for (Game game : games) {
			gameId = game.getId();
			if (!isMyGame(game)) {
				// todo: check for observed
				/*lccHelper.getClient().unobserveGame(gameId);
				Log.d(TAG, "unobserve game " + gameId);*/
			}
			else if (gameId > latestGameId) {
				latestGameId = gameId;
			}
		}

		Log.d(TAG, "latestGameId=" + latestGameId);

		if (!latestGameId.equals(previousGameId) && lccHelper.getLccEventListener() != null) {
			Log.d(TAG, "onGameListReceived: game is expired");
			lccHelper.getLccEventListener().expireGame();
		}

		/*if (latestGameId == 0) {
			// todo: fix NPE
			if (lccHelper == null) {
				Log.d(TAG, "onGameListReceived lccHelper is NULL");
			}
//			else if (lccHelper.getLccEventListener() == null) {
//				Log.d(TAG, "onGameListReceived lccEventListener is NULL");
//			}

			if (lccHelper.getLccEventListener() != null) {
				lccHelper.getLccEventListener().createSeek();
			}
		}*/
	}

	@Override
	public void onGameArchiveReceived(User user, Collection<? extends Game> games) {
	}

	@Override
	public void onGameReset(Game game) {
		Log.d(TAG, "GAME LISTENER: onGameReset id=" + game.getId() + ", game=" + game);


		if (lccHelper.isObserveGame(game)) {

			// todo: check usage of currentGame, latestGame for observe game

		} else {

			if (!isActualGame(game)) {
				return;
			}
		}

		lccHelper.putGame(game);

		doResetGame(game);
		doUpdateGame(false, game);
	}

	@Override
	public void onGameUpdated(Game game) {
		Log.d(TAG, "GAME LISTENER: onGameUpdated id=" + game.getId() + ", game=" + game);

		if (!lccHelper.isConnected()) {
			Log.d(TAG, "ignore onGameUpdated before onConnectionRestored"); // remove after cometd/lcc fix
			return;
		}

		if (!isActualGame(game)) {
			return;
		}

		lccHelper.putGame(game);
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

		/*if (!isMyGame(game)) {
			lccHelper.getClient().unobserveGame(gameId);
			Log.d(TAG, "GAME LISTENER: unobserve game " + gameId);
			return false;
		} else*/
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
		synchronized (LccHelper.LOCK) {
			lccHelper.setCurrentGameId(game.getId());
			if (game.isGameOver()) {
				lccHelper.putGame(game);
				return;
			}
			//lccHelper.setGameActivityPausedMode(true);
			lccHelper.processFullGame();
		}
	}

	private void doUpdateGame(boolean checkMoves, Game game) {

		if (checkMoves && (game.getMoveCount() == 1 || game.getMoveCount() - 1 > lccHelper.getLatestMoveNumber())) { // do not check moves if it was
			User moveMaker = game.getLastMoveMaker();
			String move = game.getLastMove();
			Log.d(TAG, "GAME LISTENER: The move #" + game.getMoveCount() + " received by user: " + lccHelper.getUser().getUsername() +
					", game.id=" + game.getId() + ", mover=" + moveMaker.getUsername() + ", move=" + move + ", allMoves=" + game.getMoves());
			synchronized (LccHelper.LOCK) {
				lccHelper.doMoveMade(game, game.getMoveCount() - 1);
			}
		}

		lccHelper.checkAndProcessDrawOffer(game);
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

		// Long lastGameId = lccHelper.getCurrentGameId() != null ? lccHelper.getCurrentGameId() : gameId; // vm: looks redundant
		lccHelper.setLastGameId(gameId);

		lccHelper.checkAndProcessEndGame(game);
	}

    /*public void onDrawRejected(Game game, User rejector) {
        final String rejectorUsername = (rejector != null ? rejector.getUsername() : null);
        Log.d(TAG, "GAME LISTENER: Draw rejected at the move #" + game.getMoveCount() +
                        ", game.id=" + game.getId() + ", rejector=" + rejectorUsername + ", game=" + game);
        if (!rejectorUsername.equals(lccHelper.getUser().getUsername())) {
			lccHelper.getLccEventListener().onInform(context.getString(R.string.draw_declined),
					rejectorUsername + StaticData.SPACE + context.getString(R.string.has_declined_draw));
        }
    }*/

	private boolean isMyGame(Game game) {
		String whiteUsername = game.getWhitePlayer().getUsername();
		String blackUsername = game.getBlackPlayer().getUsername();
		String username = lccHelper.getUsername();

		boolean isMyGame = username.equals(whiteUsername) || username.equals(blackUsername);

		if (!isMyGame) {
			Log.d(TAG, "not own game " + game.getId());
		}

		return isMyGame;
	}
}
