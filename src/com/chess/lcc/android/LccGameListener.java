package com.chess.lcc.android;

import android.content.Context;
import android.util.Log;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.Game;
import com.chess.live.client.GameListener;
import com.chess.live.client.User;
import com.chess.live.client.impl.GameImpl;
import com.chess.utilities.AppUtils;

import java.util.Collection;
import java.util.List;

public class LccGameListener implements GameListener {

	private LccHelper lccHelper;
    private Long latestGameId;
    private Context context;
    private static final String TAG = "LCCLOG-GAME";

	public LccGameListener(LccHelper lccHelper) {
        this.lccHelper = lccHelper;
        context = lccHelper.getContext();
    }

	@Override
	public void onGameListReceived(Collection<? extends Game> games) {
        Log.d(TAG, "GAME LISTENER: Game list received.");
        latestGameId = 0L;
        if (games.size() > 1) {
            Log.w(TAG, "GAME LISTENER: Game list received. Games count: " + games.size());
        }
        for (Game game : games) {
            if (isMyGame(game) && game.getId() > latestGameId) {
                latestGameId = game.getId();
                Log.w(TAG, "GAME LISTENER: Game list received. latestGameId=" + game.getId());
            }
        }
        for (Game game : games) {
            if (!game.getId().equals(latestGameId)) {
                Log.d(TAG, "GAME LISTENER: Game list received. Exit game id=" + game.getId());
                lccHelper.getClient().exitGame(game);
            }
        }
    }

    public void onGameArchiveReceived(User user, Collection<? extends Game> games) {
        // TODO: Implement when necessary
    }

//    public void onFollowedUserListReceived(Collection<? extends User> users) {
//        // TODO: Implement when necessary
//    }
//
//    public void onFollowConfirmed(String followedUser, boolean succeed) {
//        // TODO: Implement when necessary
//    }
//
//    public void onUnfollowConfirmed(String unfollowedUser, boolean succeed) {
//        // TODO: Implement when necessary
//    }

    private boolean isOldGame(Long gameId) {
        return gameId < latestGameId;
    }

    @Override
    public void onFullGameReceived(Game game) { // todo: move logic from onGameListReceived and onGameStarted to onGameReset in new LCC
        Log.d(TAG, "GAME LISTENER: Full GameItem received: " + game);
		Long gameId = game.getId();

		if (!isMyGame(game)) {
			lccHelper.getClient().unobserveGame(gameId);
			Log.d(TAG, "GAME LISTENER: unobserve game " + gameId);
			return;
		}

        if (isOldGame(gameId)) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + gameId);
            return;
        }
        Game oldGame = lccHelper.getGame(gameId);
        if (oldGame != null) {
            Log.d(TAG, "LCC: PROCESS EXTRA onFullGameReceived, game id= " + gameId);
			/*lccHelper.currentFGTime = System.currentTimeMillis();
			lccHelper.previousFGGameId = lccHelper.currentFGGameId;
			lccHelper.currentFGGameId = game.getId();*/
        }
        if (game.isEnded()) {
            lccHelper.putGame(game);
            return;
        }
        lccHelper.setCurrentGameId(gameId);
        lccHelper.processFullGame(game);
    }

    @Override
    public void onServerStatusChanged(Game game, Game.ServerStatus oldStatus, Game.ServerStatus newStatus) {
    }

    @Override
    public void onGameStarted(Game game) {
		Long gameId = game.getId();
        Log.d(TAG, "GAME LISTENER: onGameStarted id=" + gameId);
		if (!isMyGame(game)) {
			lccHelper.getClient().unobserveGame(gameId);
			Log.d(TAG, "GAME LISTENER: unobserve game " + gameId);
			return;
		} else if (lccHelper.isUserPlayingAnotherGame(gameId)) {
            Log.d(TAG, "GAME LISTENER: onGameStarted() abort and exit second game");
            lccHelper.getClient().abortGame(game, "abort second game");
            lccHelper.getClient().exitGame(game);
        } else if (isOldGame(gameId)) {
            Log.d(TAG, "GAME LISTENER: onGameStarted() exit old game");
            lccHelper.getClient().exitGame(game);
        } else {
            lccHelper.clearOwnChallenges();
            lccHelper.clearChallenges();
            lccHelper.clearSeeks();

            //lccHelper.getClient().unsubscribeFromSeekList(lccHelper.getSeekListSubscriptionId());
            lccHelper.setCurrentGameId(gameId);
            if (gameId > latestGameId) {
                latestGameId = gameId;
                Log.w(TAG, "GAME LISTENER: onGameStarted() latestGameId=" + gameId);
            }
        }

		/*if (TESTING_GAME) {
			if (game.isMoveOf(lccHelper.getUser()) && game.getSeq() == 0) {
				Log.d(TAG, "First move by: " + lccHelper.getUsername() + ", the movie: "
						+ TEST_MOVES_COORD[game.getSeq()]);
				lccHelper.getClient().makeMove(game, TEST_MOVES_COORD[game.getSeq()].trim());
			}
		}*/
    }

    @Override
    public void onGameEnded(Game game) {
        Log.d(TAG, "GAME LISTENER: Game ended: " + game);
        lccHelper.putGame(game);
        if (isOldGame(game.getId())) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + game.getId());
            return;
        }

        /*lccHelper.getClient().subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1,
                                                        lccHelper.getSeekListListener());*/
		Long lastGameId = lccHelper.getCurrentGameId() != null ? lccHelper.getCurrentGameId() : game.getId();
		lccHelper.setLastGameId(lastGameId);

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
				message = winnerUsername + context.getString(R.string.won_game_abandoned);
                break;
            case ABORTED:
				message = context.getString(R.string.game_aborted);
                break;
        }
        //message = whiteUsername + " vs. " + blackUsername + " - " + message;
        Log.d(TAG, "GAME LISTENER: GAME OVER - " + message);

		if (lccHelper.getWhiteClock() != null) {
			lccHelper.getWhiteClock().setRunning(false);
		}
		if (lccHelper.getBlackClock() != null) {
			lccHelper.getBlackClock().setRunning(false);
		}

		String abortedCodeMessage = ((GameImpl)game).getCodeMessage(); // used only for aborted games
		if (abortedCodeMessage != null) {
			final String messageI18n = AppUtils.getI18nString(context, abortedCodeMessage);
			if (messageI18n != null) {
				message = messageI18n;
			}
		}

		if (lccHelper.getCurrentGameId() == null) {
			lccHelper.setCurrentGameId(game.getId());
		}

        if (lccHelper.isActivityPausedMode()) {
			Log.d(TAG, "ActivityPausedMode = true");
            final GameEvent gameEndedEvent = new GameEvent();
			gameEndedEvent.setGameId(game.getId());
            gameEndedEvent.setEvent(GameEvent.Event.END_OF_GAME);
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

    @Override
    public void onGameAborted(Game game) {
        Log.d(TAG, "GAME LISTENER: GameItem aborted: " + game);
    }

    @Override
    public void onMoveMade(Game game, User moveMaker, String move) {
        Log.d(TAG, "GAME LISTENER: The move #" + game.getSeq() + " received by user: " + lccHelper.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", mover=" + moveMaker.getUsername() + ", move=" + move + ", allMoves=" + game.getMoves());
        if (isOldGame(game.getId())) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + game.getId());
            return;
        }
        lccHelper.doMoveMade(game, moveMaker, game.getSeq() - 1);

		/*if (TESTING_GAME && game.isMoveOf(lccHelper.getUser()) && game.getState() == Game.State.Started) {
			if (game.getSeq() < TEST_MOVES_COORD.length) {
				lccHelper.getClient().makeMove(game, TEST_MOVES_COORD[game.getSeq()].trim());
			}
		}*/
    }

    @Override
    public void onResignMade(Game game, User resignMaker, User winner) {
        Log.d(TAG, "GAME LISTENER: Game resigned: resigner=" + resignMaker.getUsername() + ", winner=" + winner.getUsername() +
                        ", game=" + game);
    }

    @Override
    public void onDrawOffered(Game game, User offerer) {
        Log.d(TAG, "GAME LISTENER: Draw offered at the move #" + game.getSeq() + AppConstants.LISTENER + lccHelper.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", offerer=" + offerer.getUsername() + ", game=" + game);
        if (isOldGame(game.getId())) {
            return;
        }
        if (offerer.getUsername().equals(lccHelper.getUser().getUsername())) {
            return;
        }
        if (lccHelper.isActivityPausedMode()) {
            final GameEvent drawOfferedEvent = new GameEvent();
            drawOfferedEvent.setEvent(GameEvent.Event.DRAW_OFFER);
            drawOfferedEvent.setGameId(game.getId());
            drawOfferedEvent.setDrawOffererUsername(offerer.getUsername());
            lccHelper.getPausedActivityGameEvents().put(drawOfferedEvent.getEvent(), drawOfferedEvent);
        } else {
            lccHelper.getLccEventListener().onDrawOffered(offerer.getUsername());
        }
    }

    @Override
    public void onDrawAccepted(Game game, User acceptor) {
        Log.d(TAG, "GAME LISTENER: Draw accepted at the move #" + game.getSeq() + AppConstants.LISTENER + lccHelper.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", acceptor=" + acceptor.getUsername() + ", game=" + game);
    }

    @Override
    public void onDrawRejected(Game game, User rejector) {
        final String rejectorUsername = (rejector != null ? rejector.getUsername() : null);
        Log.d(TAG, "GAME LISTENER: Draw rejected at the move #" + game.getSeq() + AppConstants.LISTENER + lccHelper.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", rejector=" + rejectorUsername + ", game=" + game);
        if (!rejectorUsername.equals(lccHelper.getUser().getUsername())) {
			lccHelper.getLccEventListener().onInform(context.getString(R.string.draw_declined),
					rejectorUsername + StaticData.SYMBOL_SPACE + context.getString(R.string.has_declined_draw));
        }
    }

    @Override
    public void onClockAdjusted(Game game, User player, Integer newClockValue, Integer clockAdjustment) {
        // TODO: Implement if necessary
    }

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
