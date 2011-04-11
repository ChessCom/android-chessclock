package com.chess.lcc.android;

import com.chess.live.client.Game;
import com.chess.live.client.GameListener;
import com.chess.live.client.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import static com.chess.lcc.android.LccHolder.LOG;

public class LccGameListener implements GameListener
{
  private LccHolder lccHolder;
  private Long latestGameId;

  public LccGameListener(LccHolder lccHolder)
  {
    if(lccHolder == null)
    {
      throw new NullPointerException("LccHolder is null");
    }
    this.lccHolder = lccHolder;
  }

  public void onGameListReceived(Collection<? extends Game> games)
  {
    LOG.info("GAME LISTENER: Game list received.");
    latestGameId = 0L;
    if(games.size() > 1)
    {
      LOG.warn("GAME LISTENER: Game list received. Games count: " + games.size());
    }
    for(Game game : games)
    {
      if(game.getId() > latestGameId)
      {
        latestGameId = game.getId();
        LOG.warn("GAME LISTENER: Game list received. latestGameId=" + game.getId());
      }
    }
    for(Game game : games)
    {
      if(!game.getId().equals(latestGameId))
      {
        LOG.info("GAME LISTENER: Game list received. Exit game id=" + game.getId());
        lccHolder.getClient().exitGame(game);
      }
    }
  }

  public void onFollowedUserListReceived(Collection<? extends User> users)
  {
    // TODO: Implement when necessary
  }

  public void onFollowConfirmed(String followedUser, boolean succeed)
  {
    // TODO: Implement when necessary
  }

  public void onUnfollowConfirmed(String unfollowedUser, boolean succeed)
  {
    // TODO: Implement when necessary
  }

  private boolean isOldGame(Long gameId)
  {
    return gameId < latestGameId;
  }

  /**
   * Replays all the moves in the given <code>game</code> on the GUI board. It is useful on reconnection.
   *
   * @param game the LCC game to be replayed
   */
  protected void doReplayMoves(Game game)
  {
    LOG.info("GAME LISTENER: replay moves,  gameId " + game.getId());
    //final String[] sanMoves = game.getMovesInSanNotation().trim().split(" ");
    final List<String> coordMoves = new ArrayList<String>(game.getMoves());
    User whitePlayer = game.getWhitePlayer();
    User blackPlayer = game.getBlackPlayer();
    User moveMaker;
    for(int i = 0; i < coordMoves.size(); i++)
    {
      moveMaker = (i % 2 == 0) ? whitePlayer : blackPlayer;
      lccHolder.doMoveMade(game, moveMaker, coordMoves.get(i), i);
    }
  }

  public void onFullGameReceived(Game game)
  {
    LOG.info("GAME LISTENER: Full Game received: " + game);
    final Long gameId = game.getId();
    if(isOldGame(gameId))
    {
      LOG.info("GAME LISTENER: ignore old game id=" + gameId);
      return;
    }
    Game oldGame = lccHolder.getGame(gameId.toString());
    if(oldGame != null)
    {
      LOG.info("LCC: PROCESS EXTRA onFullGameReceived, game id= " + gameId);
      // todo: do current game end?
      if (lccHolder.getAndroid().getGameActivity() != null)
      {
        lccHolder.getAndroid().getGameActivity().finish(); // todo: refactoring, finish or just reinitialize
        lccHolder.getAndroid().setGameActivity(null);
      }
      //lccHolder.putGame(game);
    }
    if (game.isEnded())
    {
      lccHolder.putGame(game);
      return;
    }
    lccHolder.processFullGame(game);
  }

  public void onGameStarted(Game game)
  {
    LOG.info("GAME LISTENER: onGameStarted id=" + game.getId());
    if(lccHolder.isUserPlaying())
    {
      LOG.info("GAME LISTENER: onGameStarted() abort and exit second game");
      lccHolder.getClient().abortGame(game, "abort second game");
      lccHolder.getClient().exitGame(game);
    }
    else if(isOldGame(game.getId()))
    {
      LOG.info("GAME LISTENER: onGameStarted() exit old game");
      lccHolder.getClient().exitGame(game);
    }
    else
    {
      lccHolder.clearOwnChallenges();
      lccHolder.clearChallenges();
      lccHolder.clearSeeks();
      lccHolder.getClient().unsubscribeFromSeekList(lccHolder.getSeekListSubscriptionId());
      lccHolder.setCurrentGameId(game.getId());
      if(game.getId() > latestGameId)
      {
        latestGameId = game.getId();
        LOG.warn("GAME LISTENER: onGameStarted() latestGameId=" + game.getId());
      }
    }
  }

  public void onGameEnded(Game game)
  {
    LOG.info("GAME LISTENER: Game ended: " + game);
    if(isOldGame(game.getId()))
    {
      LOG.info("GAME LISTENER: ignore old game id=" + game.getId());
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
    if(whitePlayerResult == Game.Result.WIN)
    {
      result = blackPlayerResult;
      winnerUsername = whiteUsername;
    }
    else if(blackPlayerResult == Game.Result.WIN)
    {
      result = whitePlayerResult;
      winnerUsername = blackUsername;
    }
    else
    {
      result = whitePlayerResult;
    }
    String message = "";
    switch(result)
    {
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

    lccHolder.putGame(game);
    if (lccHolder.isActivityPausedMode())
    {
      final GameEvent gameEndedEvent = new GameEvent();
      gameEndedEvent.event = GameEvent.Event.EndOfGame;
      gameEndedEvent.gameEndedMessage = message;
      lccHolder.getPausedActivityGameEvents().put(gameEndedEvent.event, gameEndedEvent);
      if (lccHolder.getAndroid().getGameActivity() == null)
      {
        lccHolder.processFullGame(game);
      }
    }
    else
    {
      lccHolder.getAndroid().processGameEnd(message);
    }
    lccHolder.getWhiteClock().setRunning(false);
    lccHolder.getBlackClock().setRunning(false);
  }

  public void onGameAborted(Game game)
  {
    LOG.info("GAME LISTENER: Game aborted: " + game);
  }

  public void onMoveMade(Game game, User moveMaker, String move)
  {
    LOG.info(
      "GAME LISTENER: The move #" + game.getSeq() + " received by user: " + lccHolder.getUser().getUsername() +
      ", game.id=" +
      game.getId() + ", mover=" + moveMaker.getUsername() + ", move=" + move + ", allMoves=" + game.getMoves());
    if(isOldGame(game.getId()))
    {
      LOG.info("GAME LISTENER: ignore old game id=" + game.getId());
      return;
    }
    lccHolder.doMoveMade(game, moveMaker, move, /*true,*/ game.getSeq() - 1);
  }

  public void onResignMade(Game game, User resignMaker, User winner)
  {
    LOG.info(
      "GAME LISTENER: Game resigned: resigner=" + resignMaker.getUsername() + ", winner=" + winner.getUsername() +
      ", game=" + game);
  }

  public void onDrawOffered(Game game, User offerer)
  {
    LOG.info(
      "GAME LISTENER: Draw offered at the move #" + game.getSeq() + ": listener=" + lccHolder.getUser().getUsername() +
      ", game.id=" +
      game.getId() + ", offerer=" + offerer.getUsername() + ", game=" + game);
    if(isOldGame(game.getId()))
    {
      return;
    }
    if (offerer.getUsername().equals(lccHolder.getUser().getUsername()))
    {
      return;
    }
    if (lccHolder.isActivityPausedMode())
    {
      final GameEvent drawOfferedEvent = new GameEvent();
      drawOfferedEvent.event = GameEvent.Event.DrawOffer;
      drawOfferedEvent.gameId = game.getId();
      drawOfferedEvent.drawOffererUsername = offerer.getUsername();
      lccHolder.getPausedActivityGameEvents().put(drawOfferedEvent.event, drawOfferedEvent);
    }
    else
    {
      lccHolder.getAndroid().processDrawOffered(offerer.getUsername());
    }
  }

  public void onDrawAccepted(Game game, User acceptor)
  {
    LOG.info(
      "GAME LISTENER: Draw accepted at the move #" + game.getSeq() + ": listener=" + lccHolder.getUser().getUsername() +
      ", game.id=" +
      game.getId() + ", acceptor=" + acceptor.getUsername() + ", game=" + game);
  }

  public void onDrawRejected(Game game, User rejector)
  {
    final String rejectorUsername = (rejector != null ? rejector.getUsername() : null);
    LOG.info(
      "GAME LISTENER: Draw rejected at the move #" + game.getSeq() + ": listener=" + lccHolder.getUser().getUsername() +
      ", game.id=" +
      game.getId() + ", rejector=" + rejectorUsername + ", game=" + game);
    if(!rejectorUsername.equals(lccHolder.getUser().getUsername()))
    {
      lccHolder.getAndroid().sendBroadcastMessageIntent(0, "com.chess.lcc.android-game-info", "DRAW DECLINED",
                                                        rejectorUsername + " has declined a draw");
    }
  }


}
