/*
 * SoundPlayer.java
 */

package com.chess.utilities;

import com.chess.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

public class SoundPlayer
{
  private MediaPlayer capture;
  private MediaPlayer castle;
  private MediaPlayer gameEnd;
  private MediaPlayer gameStart;
  private MediaPlayer moveOpponent;
  private MediaPlayer moveSelf;
  private MediaPlayer moveOpponentCheck;
  private MediaPlayer moveSelfCheck;
  private MediaPlayer notify;
  private Context context;

  public SoundPlayer(Context context)
  {
    capture = MediaPlayer.create(context, R.raw.capture);
    castle = MediaPlayer.create(context, R.raw.castle);
    gameEnd = MediaPlayer.create(context, R.raw.game_end);
    gameStart = MediaPlayer.create(context, R.raw.game_start);
    moveOpponent = MediaPlayer.create(context, R.raw.move_opponent);
    moveSelf = MediaPlayer.create(context, R.raw.move_self);
    moveOpponentCheck = MediaPlayer.create(context, R.raw.move_opponent_check);
    moveSelfCheck = MediaPlayer.create(context, R.raw.move_self_check);
    notify = MediaPlayer.create(context, R.raw.notify);
    this.context = context;
  }

  public void playCapture()
  {
    playSound(capture);
  }

  public void playCastle()
  {
    playSound(castle);
  }

  public void playGameEnd()
  {
    playSound(gameEnd);
  }

  public void playGameStart()
  {
    playSound(gameStart);
  }

  public void playMoveOpponent()
  {
    playSound(moveOpponent);
  }

  public void playMoveSelf()
  {
    playSound(moveSelf);
  }

  public void playMoveOpponentCheck()
  {
    playSound(moveOpponentCheck);
  }

  public void playMoveSelfCheck()
  {
    playSound(moveSelfCheck);
  }

  public void playNotify()
  {
    playSound(notify);
  }

  private void playSound(MediaPlayer mediaPlayer)
  {
    final SharedPreferences SharedPreferences = context.getSharedPreferences("sharedData", 0);
    if (SharedPreferences.getBoolean(SharedPreferences.getString("username", "") + "enableSounds", true))
    {
      mediaPlayer.start();
    }
  }
}

