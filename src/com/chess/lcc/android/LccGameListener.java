package com.chess.lcc.android;

import com.chess.live.client.Game;
import com.chess.live.client.GameListener;
import com.chess.live.client.User;
import com.chess.live.util.Utils;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.IntentConstants;

import java.util.Collection;
import java.util.List;

import static com.chess.lcc.android.LccHolder.LOG;

public class LccGameListener implements GameListener {
	private final boolean TESTING_GAME = false;
	private String[] TEST_MOVES_COORD = {"d2d4", "g8f6", "g1f3", "c7c6", "e2e3", "d7d6", "f1d3", "c8g4", "h2h3", "g4f3", "d1f3",
			"e7e5", "d4e5", "d6e5", "b1d2", "f8b4", "a2a3", "b4d2", "c1d2", "e8g8", "e1c1", "b8d7", "d3f5", "d8c7", "e3e4",
			"a7a5", "f3g3", "g7g6", "d2h6", "f8e8", "h3h4", "f6h5", "g3g5", "f7f6", "g5g4", "d7c5", "f5g6", "h7g6", "g4g6",
			"h5g7", "g6f6", "e8f8", "f6g6", "a8d8", "d1d8", "f8d8", "g2g4", "c7f7", "g6g5", "c5e6", "h1h2"};

	private LccHolder lccHolder;
	private Long latestGameId;

	public LccGameListener(LccHolder lccHolder) {
		if (lccHolder == null) {
			throw new NullPointerException(AppConstants.LCC_HOLDER_IS_NULL);
		}
		this.lccHolder = lccHolder;
	}

	@Override
	public void onGameListReceived(Collection<? extends Game> games) {
		LOG.info("GAME LISTENER: Game list received.");
		latestGameId = 0L;
		if (games.size() > 1) {
			LOG.warn("GAME LISTENER: Game list received. Games count: " + games.size());
		}
		for (Game game : games) {
			if (game.getId() > latestGameId) {
				latestGameId = game.getId();
				LOG.warn("GAME LISTENER: Game list received. latestGameId=" + game.getId());
			}
		}
		for (Game game : games) {
			if (!game.getId().equals(latestGameId)) {
				LOG.info("GAME LISTENER: Game list received. Exit game id=" + game.getId());
				lccHolder.getClient().exitGame(game);
			}
		}
	}

	public void onGameArchiveReceived(User user, Collection<? extends Game> games) {
		// TODO: Implement when necessary
	}

	public void onFollowedUserListReceived(Collection<? extends User> users) {
		// TODO: Implement when necessary
	}

	public void onFollowConfirmed(String followedUser, boolean succeed) {
		// TODO: Implement when necessary
	}

	public void onUnfollowConfirmed(String unfollowedUser, boolean succeed) {
		// TODO: Implement when necessary
	}

	private boolean isOldGame(Long gameId) {
		return gameId < latestGameId;
	}

	@Override
	public void onFullGameReceived(Game game) {
		LOG.info("GAME LISTENER: Full GameItem received: " + game);
		final Long gameId = game.getId();
		if (isOldGame(gameId)) {
			LOG.info(AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + gameId);
			return;
		}
		Game oldGame = lccHolder.getGame(gameId);
		if (oldGame != null) {
			LOG.info("LCC: PROCESS EXTRA onFullGameReceived, game id= " + gameId);
			// todo: do current game end?
			if (lccHolder.getAndroid().getGameActivity() != null) {
				lccHolder.getAndroid().getGameActivity().finish(); // todo: refactoring, finish or just reinitialize

				//
				lccHolder.previousFGTime = lccHolder.currentFGTime;
				lccHolder.currentFGTime = System.currentTimeMillis();
				lccHolder.previousFGGameId = lccHolder.currentFGGameId;
				lccHolder.currentFGGameId = game.getId();
				//

				lccHolder.getAndroid().setGameActivity(null);
			}
			//lccHolder.putGame(game);
		}
		if (game.isEnded()) {
			lccHolder.putGame(game);
			return;
		}
		lccHolder.setCurrentGameId(game.getId());
		lccHolder.processFullGame(game);
	}

	@Override
	public void onServerStatusChanged(Game game, Game.ServerStatus oldStatus, Game.ServerStatus newStatus) {
	}

	@Override
	public void onGameStarted(Game game) {
		LOG.info("GAME LISTENER: onGameStarted id=" + game.getId());
		if (lccHolder.isUserPlayingAnotherGame(game.getId())) {
			LOG.info("GAME LISTENER: onGameStarted() abort and exit second game");
			lccHolder.getClient().abortGame(game, "abort second game");
			lccHolder.getClient().exitGame(game);
		} else if (isOldGame(game.getId())) {
			LOG.info("GAME LISTENER: onGameStarted() exit old game");
			lccHolder.getClient().exitGame(game);
		} else {
			lccHolder.clearOwnChallenges();
			lccHolder.clearChallenges();
			lccHolder.clearSeeks();

			//lccHolder.getClient().unsubscribeFromSeekList(lccHolder.getSeekListSubscriptionId());
			lccHolder.setCurrentGameId(game.getId());
			if (game.getId() > latestGameId) {
				latestGameId = game.getId();
				LOG.warn("GAME LISTENER: onGameStarted() latestGameId=" + game.getId());
			}
		}

		if (TESTING_GAME) {
			if (game.isMoveOf(lccHolder.getUser())) {
				Utils.sleep(8000L);
				LOG.info("First move by: " + lccHolder.getUser().getUsername() + ", the movie: "
						+ TEST_MOVES_COORD[game.getSeq()]);
				lccHolder.getClient().makeMove(game, TEST_MOVES_COORD[game.getSeq()]);
			}
		}
	}

	@Override
	public void onGameEnded(Game game) {
		LOG.info("GAME LISTENER: Game ended: " + game);
		lccHolder.putGame(game);
		if (isOldGame(game.getId())) {
			LOG.info(AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + game.getId());
			return;
		}

		/*lccHolder.getClient().subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1,
													  lccHolder.getSeekListListener());*/
		lccHolder.setCurrentGameId(null);
		List<Game.Result> gameResults = game.getGameResults();
		final Game.Result whitePlayerResult = gameResults.get(0);
		final Game.Result blackPlayerResult = gameResults.get(1);
		final String whiteUsername = game.getWhitePlayer().getUsername();
		final String blackUsername = game.getBlackPlayer().getUsername();
		Game.Result result;
		String winnerUsername = null;
		if (whitePlayerResult == Game.Result.WIN) {
			result = blackPlayerResult;
			winnerUsername = whiteUsername;
		} else if (blackPlayerResult == Game.Result.WIN) {
			result = whitePlayerResult;
			winnerUsername = blackUsername;
		} else {
			result = whitePlayerResult;
		}
		String message = "";
		switch (result) {
			case TIMEOUT:
				message = winnerUsername + " \nwon on time";
				break;
			case RESIGNED:
				message = winnerUsername + " \nwon by resignation";
				break;
			case CHECKMATED:
				message = winnerUsername + " \nwon by checkmate";
				break;
			case DRAW_BY_REPETITION:
				message = "Game drawn \nby repetition";
				break;
			case DRAW_AGREED:
				message = "Game drawn \nby agreement";
				break;
			case STALEMATE:
				message = "Game drawn \nby stalemate";
				break;
			case DRAW_BY_INSUFFICIENT_MATERIAL:
				message = "Game drawn - \ninsufficient material";
				break;
			case DRAW_BY_50_MOVE:
				message = "Game drawn \nby 50-move rule";
				break;
			case ABANDONED:
				message = winnerUsername + " won - \ngame abandoned";
				break;
			case ABORTED:
				message = "Game aborted";
				break;
		}
		//message = whiteUsername + " vs. " + blackUsername + " - " + message;
		LOG.info("GAME LISTENER: GAME OVER - " + message);

		if (lccHolder.isActivityPausedMode()) {
			final GameEvent gameEndedEvent = new GameEvent();
			gameEndedEvent.setEvent(GameEvent.Event.EndOfGame);
			gameEndedEvent.setGameEndedMessage(message);
			lccHolder.getPausedActivityGameEvents().put(gameEndedEvent.getEvent(), gameEndedEvent);
			if (lccHolder.getAndroid().getGameActivity() == null) {
				lccHolder.processFullGame(game);
			}
		} else {
			lccHolder.getAndroid().processGameEnd(message);
		}
		lccHolder.getWhiteClock().setRunning(false);
		lccHolder.getBlackClock().setRunning(false);
	}

	@Override
	public void onGameAborted(Game game) {
		LOG.info("GAME LISTENER: GameItem aborted: " + game);
	}

	@Override
	public void onMoveMade(Game game, User moveMaker, String move) {
		LOG.info(
				"GAME LISTENER: The move #" + game.getSeq() + " received by user: " + lccHolder.getUser().getUsername() +
						", game.id=" +
						game.getId() + ", mover=" + moveMaker.getUsername() + ", move=" + move + ", allMoves=" + game.getMoves());
		if (isOldGame(game.getId())) {
			LOG.info(AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + game.getId());
			return;
		}
		lccHolder.doMoveMade(game, moveMaker, move, /*true,*/ game.getSeq() - 1);

		if (TESTING_GAME && game.isMoveOf(lccHolder.getUser()) && game.getState() == Game.State.Started) {
			Utils.sleep(0.5F);
			if (game.getSeq() < TEST_MOVES_COORD.length) {
				lccHolder.getClient().makeMove(game, TEST_MOVES_COORD[game.getSeq()]);
			} /*else {
          lccHolder.getClient().makeResign(game, "end of game");
      }*/
		}
	}

	@Override
	public void onResignMade(Game game, User resignMaker, User winner) {
		LOG.info(
				"GAME LISTENER: Game resigned: resigner=" + resignMaker.getUsername() + ", winner=" + winner.getUsername() +
						", game=" + game);
	}

	@Override
	public void onDrawOffered(Game game, User offerer) {
		LOG.info(
				"GAME LISTENER: Draw offered at the move #" + game.getSeq() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
						", game.id=" +
						game.getId() + ", offerer=" + offerer.getUsername() + ", game=" + game);
		if (isOldGame(game.getId())) {
			return;
		}
		if (offerer.getUsername().equals(lccHolder.getUser().getUsername())) {
			return;
		}
		if (lccHolder.isActivityPausedMode()) {
			final GameEvent drawOfferedEvent = new GameEvent();
			drawOfferedEvent.setEvent(GameEvent.Event.DrawOffer);
			drawOfferedEvent.setGameId(game.getId());
			drawOfferedEvent.setDrawOffererUsername(offerer.getUsername());
			lccHolder.getPausedActivityGameEvents().put(drawOfferedEvent.getEvent(), drawOfferedEvent);
		} else {
			lccHolder.getAndroid().processDrawOffered(offerer.getUsername());
		}
	}

	@Override
	public void onDrawAccepted(Game game, User acceptor) {
		LOG.info(
				"GAME LISTENER: Draw accepted at the move #" + game.getSeq() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
						", game.id=" +
						game.getId() + ", acceptor=" + acceptor.getUsername() + ", game=" + game);
	}

	@Override
	public void onDrawRejected(Game game, User rejector) {
		final String rejectorUsername = (rejector != null ? rejector.getUsername() : null);
		LOG.info(
				"GAME LISTENER: Draw rejected at the move #" + game.getSeq() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
						", game.id=" +
						game.getId() + ", rejector=" + rejectorUsername + ", game=" + game);
		if (!rejectorUsername.equals(lccHolder.getUser().getUsername())) {
			lccHolder.getAndroid().sendBroadcastMessageIntent(0, IntentConstants.ACTION_GAME_INFO, "DRAW DECLINED",
					rejectorUsername + " has declined a draw");
		}
	}


}
