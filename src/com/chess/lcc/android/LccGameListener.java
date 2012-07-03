package com.chess.lcc.android;

import android.content.Context;
import android.util.Log;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.Game;
import com.chess.live.client.GameListener;
import com.chess.live.client.User;
import com.chess.live.util.Utils;

import java.util.Collection;
import java.util.List;

public class LccGameListener implements GameListener {
	private final boolean TESTING_GAME = false;
    private static final String[] TEST_MOVES_COORD = {"d2d4", "g8f6", "g1f3", "c7c6", "e2e3", "d7d6", "f1d3", "c8g4", "h2h3", "g4f3", "d1f3",
			"e7e5", "d4e5", "d6e5", "b1d2", "f8b4", "a2a3", "b4d2", "c1d2", "e8g8", "e1c1", "b8d7", "d3f5", "d8c7", "e3e4",
			"a7a5", "f3g3", "g7g6", "d2h6", "f8e8", "h3h4", "f6h5", "g3g5", "f7f6", "g5g4", "d7c5", "f5g6", "h7g6", "g4g6",
			"h5g7", "g6f6", "e8f8", "f6g6", "a8d8", "d1d8", "f8d8", "g2g4", "c7f7", "g6g5", "c5e6", "h1h2"};

	private LccHolder lccHolder;
    private Long latestGameId;
    private Context context;
    private static final String TAG = "LccGameListener";

	public LccGameListener(LccHolder lccHolder) {
        this.lccHolder = lccHolder;
        context = lccHolder.getContext();
    }

	@Override
	public void onGameListReceived(Collection<? extends Game> games) {
        Log.d(TAG, "GAME LISTENER: Game list received.");
        latestGameId = 0L;
        if (games.size() > 1) {
            Log.w(TAG, "GAME LISTENER: Game list received. Games count: " + games.size());
        }
        for (Game game : games) {
            if (game.getId() > latestGameId) {
                latestGameId = game.getId();
                Log.w(TAG, "GAME LISTENER: Game list received. latestGameId=" + game.getId());
            }
        }
        for (Game game : games) {
            if (!game.getId().equals(latestGameId)) {
                Log.d(TAG, "GAME LISTENER: Game list received. Exit game id=" + game.getId());
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
		Log.d("Live Game","onFullGameReceived ");
        Log.d(TAG, "GAME LISTENER: Full GameItem received: " + game);
        Long gameId = game.getId();
        if (isOldGame(gameId)) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + gameId);
            return;
        }
        Game oldGame = lccHolder.getGame(gameId);
        if (oldGame != null) {
            Log.d(TAG, "LCC: PROCESS EXTRA onFullGameReceived, game id= " + gameId);
            // todo: do current game end?
//            if (lccHolder.getAndroidStuff().getLccEventListener() != null) {
//                lccHolder.getAndroidStuff().getLccEventListener().finish(); // todo: refactoring, finish or just reinitialize

//                lccHolder.previousFGTime = lccHolder.currentFGTime;
                lccHolder.currentFGTime = System.currentTimeMillis();
                lccHolder.previousFGGameId = lccHolder.currentFGGameId;
                lccHolder.currentFGGameId = game.getId();
			Log.d("TEST", "onFullGameReceived-> lccEventListener set to null");
//            }
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
        Log.d(TAG, "GAME LISTENER: onGameStarted id=" + game.getId());
        if (lccHolder.isUserPlayingAnotherGame(game.getId())) {
            Log.d(TAG, "GAME LISTENER: onGameStarted() abort and exit second game");
            lccHolder.getClient().abortGame(game, "abort second game");
            lccHolder.getClient().exitGame(game);
        } else if (isOldGame(game.getId())) {
            Log.d(TAG, "GAME LISTENER: onGameStarted() exit old game");
            lccHolder.getClient().exitGame(game);
        } else {
            lccHolder.clearOwnChallenges();
            lccHolder.clearChallenges();
            lccHolder.clearSeeks();

            //lccHolder.getClient().unsubscribeFromSeekList(lccHolder.getSeekListSubscriptionId());
            lccHolder.setCurrentGameId(game.getId());
            if (game.getId() > latestGameId) {
                latestGameId = game.getId();
                Log.w(TAG, "GAME LISTENER: onGameStarted() latestGameId=" + game.getId());
            }
        }

        if (TESTING_GAME) {
            if (game.isMoveOf(lccHolder.getUser())) {
                Utils.sleep(8000L);
                Log.d(TAG, "First move by: " + lccHolder.getUser().getUsername() + ", the movie: "
                        + TEST_MOVES_COORD[game.getSeq()]);
                lccHolder.getClient().makeMove(game, TEST_MOVES_COORD[game.getSeq()]);
            }
        }
    }

    @Override
    public void onGameEnded(Game game) {
        Log.d(TAG, "GAME LISTENER: Game ended: " + game);
        lccHolder.putGame(game);
        if (isOldGame(game.getId())) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + game.getId());
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

        String message = StaticData.SYMBOL_EMPTY;
        switch (result) {
            case TIMEOUT:
                message = winnerUsername + context.getString(R.string.won_on_time);
                break;
            case RESIGNED:
                message = winnerUsername + context.getString(R.string.won_by_resignation);
                break;
            case CHECKMATED:
                message = winnerUsername + context.getString(R.string.won_by_checkmate);
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

        if (lccHolder.isActivityPausedMode()) {
			Log.d(TAG, "ActivityPausedMode = true");
            final GameEvent gameEndedEvent = new GameEvent();
            gameEndedEvent.setEvent(GameEvent.Event.END_OF_GAME);
            gameEndedEvent.setGameEndedMessage(message);
            lccHolder.getPausedActivityGameEvents().put(gameEndedEvent.getEvent(), gameEndedEvent);
            if (lccHolder.getLccEventListener() == null) {// if activity is not started yet
                lccHolder.processFullGame(game);
				Log.d(TAG, "processFullGame");
            }
        } else {
            lccHolder.getLccEventListener().onGameEnd(message);
        }
        lccHolder.getWhiteClock().setRunning(false);
        lccHolder.getBlackClock().setRunning(false);
    }

    @Override
    public void onGameAborted(Game game) {
        Log.d(TAG, "GAME LISTENER: GameItem aborted: " + game);
    }

    @Override
    public void onMoveMade(Game game, User moveMaker, String move) {
        Log.d(TAG, "GAME LISTENER: The move #" + game.getSeq() + " received by user: " + lccHolder.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", mover=" + moveMaker.getUsername() + ", move=" + move + ", allMoves=" + game.getMoves());
        if (isOldGame(game.getId())) {
            Log.d(TAG, AppConstants.GAME_LISTENER_IGNORE_OLD_GAME_ID + game.getId());
            return;
        }
        lccHolder.doMoveMade(game, moveMaker, move, game.getSeq() - 1);

        if (TESTING_GAME && game.isMoveOf(lccHolder.getUser()) && game.getState() == Game.State.Started) {
            Utils.sleep(0.5F);
            if (game.getSeq() < TEST_MOVES_COORD.length) {
                lccHolder.getClient().makeMove(game, TEST_MOVES_COORD[game.getSeq()]);
            }
        }
    }

    @Override
    public void onResignMade(Game game, User resignMaker, User winner) {
        Log.d(TAG, "GAME LISTENER: Game resigned: resigner=" + resignMaker.getUsername() + ", winner=" + winner.getUsername() +
                        ", game=" + game);
    }

    @Override
    public void onDrawOffered(Game game, User offerer) {
        Log.d(TAG, "GAME LISTENER: Draw offered at the move #" + game.getSeq() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
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

    @Override
    public void onDrawAccepted(Game game, User acceptor) {
        Log.d(TAG, "GAME LISTENER: Draw accepted at the move #" + game.getSeq() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", acceptor=" + acceptor.getUsername() + ", game=" + game);
    }

    @Override
    public void onDrawRejected(Game game, User rejector) {
        final String rejectorUsername = (rejector != null ? rejector.getUsername() : null);
        Log.d(TAG, "GAME LISTENER: Draw rejected at the move #" + game.getSeq() + AppConstants.LISTENER + lccHolder.getUser().getUsername() +
                        ", game.id=" + game.getId() + ", rejector=" + rejectorUsername + ", game=" + game);
        if (!rejectorUsername.equals(lccHolder.getUser().getUsername())) {
//            lccHolder.getAndroidStuff().sendBroadcastMessageIntent(0, IntentConstants.ACTION_GAME_INFO, "DRAW DECLINED",
//                    rejectorUsername + " has declined a draw");
			lccHolder.getLccEventListener().onInform(context.getString(R.string.draw_declined),
					rejectorUsername + context.getString(R.string.has_declined_draw));
        }
    }

    //	@Override
    public void onClockAdjusted(Game game, User player, Integer newClockValue, Integer clockAdjustment) {
        // TODO: Implement if necessary
    }
}
