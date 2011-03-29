/*
 * AndroidStuff.java
 */

package com.chess.lcc.android;

import java.io.Serializable;

import com.chess.activities.Game;
import com.chess.core.MainApp;
import com.chess.utilities.WebService;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

public class AndroidStuff
{
  private MainApp context;
  private SharedPreferences sharedData;
  private SharedPreferences.Editor sharedDataEditor;
  private ProgressDialog currentProgressDialog;
  private LccHolder lccHolder;
  private Game gameActivity;
  private Handler clockHandler = new Handler();
  private Handler updateBoardHandler = new Handler();
  private ProgressDialog reconnectingIndicator;

  public AndroidStuff(LccHolder lccHolder)
  {
    this.lccHolder = lccHolder;
  }

  public MainApp getContext()
  {
    return context;
  }

  public void setContext(final MainApp context)
  {
    this.context = context;
  }

  public void setCurrentProgressDialog(ProgressDialog currentProgressDialog)
  {
    this.currentProgressDialog = currentProgressDialog;
  }

  public SharedPreferences getSharedData()
  {
    if(sharedData == null)
    {
      sharedData = context.getSharedPreferences("sharedData", 0);
    }
    return sharedData;
  }

  public SharedPreferences.Editor getSharedDataEditor()
  {
    if(sharedDataEditor == null)
    {
      sharedDataEditor = getSharedData().edit();
    }
    return sharedDataEditor;
  }

  public Game getGameActivity()
  {
    return gameActivity;
  }

  public void setGameActivity(Game gameActivity)
  {
    this.gameActivity = gameActivity;
  }

  public Handler getClockHandler()
  {
    return clockHandler;
  }

  public Handler getUpdateBoardHandler()
  {
    return updateBoardHandler;
  }

  public void sendConnectionBroadcastIntent(boolean result, int code, String... errorMessage)
  {
    lccHolder.getAndroid().getContext().sendBroadcast(new Intent(WebService.BROADCAST_ACTION)
                                                        .putExtra("repeatble", false)
                                                        .putExtra("code", code)
                                                        .putExtra("result",
                                                                  result ? "Success" : "Error+" + errorMessage[0])
    );
    /*if(currentProgressDialog != null)
    {
      currentProgressDialog.dismiss();
    }*/
  }

  public void sendBroadcastObjectIntent(int code, String broadcastAction, Serializable object)
  {
    //LccHolder.LOG.info("ANDROID: sendBroadcastObjectIntent action=" + broadcastAction);
    lccHolder.getAndroid().getContext().sendBroadcast(
      new Intent(broadcastAction)
        .putExtra("code", code)
        .putExtra("object", object)
    );
    if(currentProgressDialog != null)
    {
      currentProgressDialog.dismiss();
    }
  }

  public void sendBroadcastMessageIntent(int code, String broadcastAction, String title, String message)
  {
    LccHolder.LOG.info("ANDROID: sendBroadcastMessageIntent action=" + broadcastAction);
    lccHolder.getAndroid().getContext().sendBroadcast(
      new Intent(broadcastAction)
        .putExtra("code", code)
        .putExtra("title", title)
        .putExtra("message", message)
    );
    if(currentProgressDialog != null)
    {
      currentProgressDialog.dismiss();
    }
  }

  public void sendBroadcastIntent(int code, String broadcastAction)
  {
    lccHolder.getAndroid().getContext().sendBroadcast(
      new Intent(broadcastAction)
        .putExtra("code", code)
    );
    if(currentProgressDialog != null)
    {
      currentProgressDialog.dismiss();
    }
  }

  public void updateChallengesList()
  {
    sendBroadcastIntent(0, "com.chess.lcc.android-challenges-list-update");
  }

  public void processMove(Long gameId, int moveIndex)
  {
    final com.chess.model.Game gameData =
      new com.chess.model.Game(lccHolder.getGameData(gameId.toString(), moveIndex), true);
    lccHolder.getAndroid().sendBroadcastObjectIntent(9, "com.chess.lcc.android-game-move", gameData);
  }

  public void processDrawOffered(String offererUsername)
  {
    lccHolder.getAndroid().sendBroadcastMessageIntent(0, "com.chess.lcc.android-game-draw-offered", "DRAW OFFER",
                                                      offererUsername + " has offered a draw");
  }

  public void processGameEnd(String message)
  {
    lccHolder.getAndroid().sendBroadcastMessageIntent(0, "com.chess.lcc.android-game-end", "GAME OVER", message);
  }

  public void setReconnectingIndicator(ProgressDialog reconnectingIndicator)
  {
    this.reconnectingIndicator = reconnectingIndicator;
  }

  public ProgressDialog getReconnectingIndicator()
  {
    return reconnectingIndicator;
  }

  public void manageProgressDialog(String broadcastAction, boolean enable, String message)
  {
    lccHolder.getAndroid().getContext().sendBroadcast(
      new Intent(broadcastAction)
        .putExtra("enable", enable)
        .putExtra("message", message)
    );
  }

  public void showReconnectingIndicator()
  {
    manageProgressDialog("com.chess.lcc.android-connection-info", true, "Reconnecting...");
  }

  public void closeReconnectingIndicator()
  {
    manageProgressDialog("com.chess.lcc.android-connection-info", false, "");
  }

  public void processOtherClientEntered()
  {
    informAndExit("com.chess.lcc.android-info-exit", "", "Another login has been detected.");
  }

  public void informAndExit(String broadcastAction, String title, String message)
  {
    LccHolder.LOG.info("ANDROID: sendBroadcastMessageIntent action=" + broadcastAction);
    lccHolder.getAndroid().getContext().sendBroadcast(
      new Intent(broadcastAction)
        .putExtra("title", title)
        .putExtra("message", message)
    );
  }

  public void processObsoleteProtocolVersion()
  {
    lccHolder.getAndroid().getContext().sendBroadcast(new Intent("com.chess.lcc.android-obsolete-protocol-version"));
  }
}

