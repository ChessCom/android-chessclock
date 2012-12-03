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
import com.chess.live.rules.GameResult;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.chess.live.rules.GameResult.*;

public class LccGameListener implements GameListener {

	private LccHolder lccHolder;
    private Long latestGameId;
    private Context context;
    private static final String TAG = "LCCLOG-GAME";

	public LccGameListener(LccHolder lccHolder) {
        this.lccHolder = lccHolder;
        context = lccHolder.getContext();
    }

	public void onGameListReceived(Collection<? extends Game> games) {
        Log.d(TAG, "GAME LISTENER: Game list received");
        latestGameId = 0L;
        if (games.size() > 1) {
            Log.d(TAG, "GAME LISTENER: Game list received. Games count: " + games.size());
        }

		List<Game> myGames = new ArrayList<Game>();
        for (Game game : games) {
            if (!game.isGameOver() && isMyGame(game) && game.getId() > latestGameId) {
                latestGameId = game.getId();
            }
        }
		if (latestGameId == 0L) {
			for (Game game : games) {
				if (game.isGameOver() && isMyGame(game) && game.getId() > latestGameId) {
					latestGameId = game.getId();
				}
			}
		}
		for (Game game : games) {
			if (latestGameId.equals(game.getId())) {
				Log.d(TAG, "GAME LISTENER: Game list received. latestGameId=" + latestGameId);
				Log.d(TAG, "GAME LISTENER: Subscribe to game: " + game);
				myGames.add(game);
			} else {
				Log.d(TAG, "GAME LISTENER: ignore game: " + game);
				lccHolder.getGameIdsToBeIgnored().add(game.getId());
			}
		}

		games.retainAll(myGames);

    }

    public void onGameArchiveReceived(User user, Collection<? extends Game> games) {
    }

	public void onGameReset(Game game) {
		Log.d(TAG, "GAME LISTENER: onGameReset id=" + game.getId());

		// check isMyTurn

		if (!isActualGame(game)) {
			return;
		}

		doResetGame(game);
		doUpdateGame(false, game);
	}

	public void onGameUpdated(Game game) {
		Log.d(TAG, "GAME LISTENER: onGameUpdated id=" + game.getId());

		// check isMyTurn
		// update clock

		if (!isActualGame(game)) {
			return;
		}

		doUpdateGame(true, game);
	}

	public void onGameOver(Game game) {
		Log.d(TAG, "GAME LISTENER: onGameOver " + game);
		doEndGame(game);
	}

	public void onGameClockAdjusted(Game game, User player, Integer newClockValue, Integer clockAdjustment, Integer resultClock) {
		// UPDATELCC todo:
		Log.d(TAG, "Game Clock adjusted: gameId=" + game.getId() + ", player=" + player.getUsername() +
				", newClockValue=" + newClockValue + ", clockAdjustment=" + clockAdjustment);
	}

    private boolean isOldGame(Long gameId) {
        return gameId < latestGameId;
    }

    /*public void onFullGameReceived(Game game) { // todo: move logic from onGameListReceived and onGameStarted to onGameReset in new LCC
        Log.d(TAG, "GAME LISTENER: Full GameItem received: " + game);
		Long gameId = game.getId();

        if (isOldGame(gameId)) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + gameId);
            return;
        }
        Game oldGame = lccHolder.getGame(gameId);
        if (oldGame != null) {
            Log.d(TAG, "LCC: PROCESS EXTRA onFullGameReceived, game id= " + gameId);
			*//*lccHolder.currentFGTime = System.currentTimeMillis();
			lccHolder.previousFGGameId = lccHolder.currentFGGameId;
			lccHolder.currentFGGameId = game.getId();*//*
        }
        if (game.isGameOver()) {
            lccHolder.putGame(game);
            return;
        }
        lccHolder.setCurrentGameId(gameId);
        lccHolder.processFullGame(game);
    }*/

    /*public void onGameStarted(Game game) {
		Long gameId = game.getId();
        Log.d(TAG, "GAME LISTENER: onGameStarted id=" + gameId);
		if (!isMyGame(game)) {
			lccHolder.getClient().unobserveGame(gameId);
			Log.d(TAG, "GAME LISTENER: unobserve game " + gameId);
			return;
		} else if (lccHolder.isUserPlayingAnotherGame(gameId)) {
            Log.d(TAG, "GAME LISTENER: onGameStarted() abort and exit second game");
            lccHolder.getClient().abortGame(game, "abort second game");
            lccHolder.getClient().exitGame(game);
        } else if (isOldGame(gameId)) {
            Log.d(TAG, "GAME LISTENER: onGameStarted() exit old game");
            lccHolder.getClient().exitGame(game);
        } else {
            lccHolder.clearOwnChallenges();
            lccHolder.clearChallenges();
            lccHolder.clearSeeks();

            //lccHolder.getClient().unsubscribeFromSeekList(lccHolder.getSeekListSubscriptionId());
            lccHolder.setCurrentGameId(gameId);
            if (gameId > latestGameId) {
                latestGameId = gameId;
                Log.w(TAG, "GAME LISTENER: onGameStarted() latestGameId=" + gameId);
            }
        }
    }*/

	private boolean isActualGame(Game game) {
		Long gameId = game.getId();

		/*if (!isMyGame(game)) {
			lccHolder.getClient().unobserveGame(gameId);
			Log.d(TAG, "GAME LISTENER: unobserve game " + gameId);
			return false;
		} else*/ if (lccHolder.isUserPlayingAnotherGame(gameId)) {
			Log.d(TAG, "GAME LISTENER: abort and exit second game");
			lccHolder.getClient().abortGame(game, "abort second game");
			lccHolder.getClient().exitGame(game);
			return false;
		} /*else if (isOldGame(gameId)) {
			Log.d(TAG, "GAME LISTENER: exit old game");
			lccHolder.getClient().exitGame(game);
			return false;
		}*/ else {
			lccHolder.clearOwnChallenges();
			lccHolder.clearChallenges();
			lccHolder.clearSeeks();
			//lccHolder.getClient().unsubscribeFromSeekList(lccHolder.getSeekListSubscriptionId());
			lccHolder.setCurrentGameId(gameId);
			if (gameId > latestGameId) {
				latestGameId = gameId;
				Log.d(TAG, "GAME LISTENER: latestGameId=" + gameId);
			}
			return true;
		}
	}

	private void doResetGame(Game game) {
		if (game.isGameOver()) {
			lccHolder.putGame(game);
			return;
		}
		lccHolder.setCurrentGameId(game.getId());
		lccHolder.processFullGame(game);
	}

	private void doUpdateGame(boolean checkMoves, Game game) {

		// todo: maybe create two separate methods: new move check and draw check

		if (checkMoves && game.getMoveCount() > lccHolder.getLatestMoveNumber()) { // do not check moves if it was
			User moveMaker = game.getLastMoveMaker();
			String move = game.getLastMove();
			Log.d(TAG, "GAME LISTENER: The move #" + game.getMoveCount() + " received by user: " + lccHolder.getUser().getUsername() +
					", game.id=" + game.getId() + ", mover=" + moveMaker.getUsername() + ", move=" + move + ", allMoves=" + game.getMoves());
			lccHolder.doMoveMade(game, moveMaker, game.getMoveCount() - 1);
		}
		// todo: check draw etc.

	}

    private void doEndGame(Game game) {
        lccHolder.putGame(game);

		Long gameId = game.getId();

        if (isOldGame(gameId)) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + gameId);
            return;
        }

        /*lccHolder.getClient().subscribeToSeekList(LiveChessClient.SeekListOrderBy.Default, 1,
                                                        lccHolder.getSeekListListener());*/
		Long lastGameId = lccHolder.getCurrentGameId() != null ? lccHolder.getCurrentGameId() : gameId;
		lccHolder.setLastGameId(lastGameId);

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
				message = winnerUsername + context.getString(R.string.won_game_abandoned);
                break;
            case ABORTED:
				message = context.getString(R.string.game_aborted);
                break;
        }
        //message = whiteUsername + " vs. " + blackUsername + " - " + message;
        Log.d(TAG, "GAME LISTENER: " + message);

		if (lccHolder.getWhiteClock() != null) {
			lccHolder.getWhiteClock().setRunning(false);
		}
		if (lccHolder.getBlackClock() != null) {
			lccHolder.getBlackClock().setRunning(false);
		}

		String abortedCodeMessage = ((GameImpl)game).getCodeMessage(); // used only for aborted games
		if (abortedCodeMessage != null) {
			final String messageI18n = AppUtils.getI18nString(context, abortedCodeMessage);
			if (messageI18n != null) {
				message = messageI18n;
			}
		}

		if (lccHolder.getCurrentGameId() == null) {
			lccHolder.setCurrentGameId(gameId);
		}

        if (lccHolder.isActivityPausedMode()) {
			Log.d(TAG, "ActivityPausedMode = true");
            final GameEvent gameEndedEvent = new GameEvent();
			gameEndedEvent.setGameId(gameId);
            gameEndedEvent.setEvent(GameEvent.Event.END_OF_GAME);
            gameEndedEvent.setGameEndedMessage(message);
            lccHolder.getPausedActivityGameEvents().put(gameEndedEvent.getEvent(), gameEndedEvent);
            if (lccHolder.getLccEventListener() == null) { // if activity is not started yet
                lccHolder.processFullGame(game);
				Log.d(TAG, "processFullGame");
            }
        } else {
            lccHolder.getLccEventListener().onGameEnd(message);
        }
    }

    /*public void onGameAborted(Game game) {
        Log.d(TAG, "GAME LISTENER: GameItem aborted: " + game);
    }*/

    /*public void onMoveMade(Game game, User moveMaker, String move) {
        Log.d(TAG, "GAME LISTENER: The move #" + game.getMoveCount() + " received by user: " + lccHolder.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", mover=" + moveMaker.getUsername() + ", move=" + move + ", allMoves=" + game.getMoves());
        if (isOldGame(game.getId())) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + game.getId());
            return;
        }
        lccHolder.doMoveMade(game, moveMaker, game.getMoveCount() - 1);
    }*/

    /*public void onResignMade(Game game, User resignMaker, User winner) {
        Log.d(TAG, "GAME LISTENER: Game resigned: resigner=" + resignMaker.getUsername() + ", winner=" + winner.getUsername() +
                        ", game=" + game);
    }*/

    public void onDrawOffered(Game game, User offerer) {
        Log.d(TAG, "GAME LISTENER: Draw offered at the move #" + game.getMoveCount() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", offerer=" + offerer.getUsername() + ", game=" + game);
        if (isOldGame(game.getId())) {
            return;
        }
        if (offerer.getUsername().equals(lccHolder.getUser().getUsername())) {
            return;
        }
        if (lccHolder.isActivityPausedMode()) {
            final GameEvent drawOfferedEvent = new GameEvent();
            drawOfferedEvent.setEvent(GameEvent.Event.DRAW_OFFER);
            drawOfferedEvent.setGameId(game.getId());
            drawOfferedEvent.setDrawOffererUsername(offerer.getUsername());
            lccHolder.getPausedActivityGameEvents().put(drawOfferedEvent.getEvent(), drawOfferedEvent);
        } else {
            lccHolder.getLccEventListener().onDrawOffered(offerer.getUsername());
        }
    }

    /*public void onDrawAccepted(Game game, User acceptor) {
        Log.d(TAG, "GAME LISTENER: Draw accepted at the move #" + game.getMoveCount() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", acceptor=" + acceptor.getUsername() + ", game=" + game);
    }*/

    public void onDrawRejected(Game game, User rejector) {
        final String rejectorUsername = (rejector != null ? rejector.getUsername() : null);
        Log.d(TAG, "GAME LISTENER: Draw rejected at the move #" + game.getMoveCount() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", rejector=" + rejectorUsername + ", game=" + game);
        if (!rejectorUsername.equals(lccHolder.getUser().getUsername())) {
			lccHolder.getLccEventListener().onInform(context.getString(R.string.draw_declined),
					rejectorUsername + StaticData.SYMBOL_SPACE + context.getString(R.string.has_declined_draw));
        }
    }

	private boolean isMyGame(Game game) {
		String whiteUsername = game.getWhitePlayer().getUsername().toLowerCase();
		String blackUsername = game.getBlackPlayer().getUsername().toLowerCase();
		String userName = lccHolder.getUsername().toLowerCase();

		boolean isMyGame = userName.equals(whiteUsername) || userName.equals(blackUsername);

		if (!isMyGame) {
			Log.d(TAG, "not own game " + game.getId());
		}

		return isMyGame;
	}
}
