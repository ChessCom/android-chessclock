/*
 * SoundPlayer.java
 */

package com.chess.utilities;

import com.chess.R;

import android.content.Context;
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
  }

  public void playCapture()
  {
    capture.start();
  }

  public void playCastle()
  {
    castle.start();
  }

  public void playGameEnd()
  {
    gameEnd.start();
  }

  public void playGameStart()
  {
    gameStart.start();
  }

  public void playMoveOpponent()
  {
    moveOpponent.start();
  }

  public void playMoveSelf()
  {
    moveSelf.start();
  }

  public void playMoveOpponentCheck()
  {
    moveOpponentCheck.start();
  }

  public void playMoveSelfCheck()
  {
    moveSelfCheck.start();
  }

}

