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
  private Context context;

  public SoundPlayer(Context context)
  {
    this.context = context;
  }

  public void playCapture()
  {
    playSound(R.raw.capture);
  }

  public void playCastle()
  {
    playSound(R.raw.castle);
  }

  public void playGameEnd()
  {
    playSound(R.raw.game_end);
  }

  public void playGameStart()
  {
    playSound(R.raw.game_start);
  }

  public void playMoveOpponent()
  {
    playSound(R.raw.move_opponent);
  }

  public void playMoveSelf()
  {
    playSound(R.raw.move_self);
  }

  public void playMoveOpponentCheck()
  {
    playSound(R.raw.move_opponent_check);
  }

  public void playMoveSelfCheck()
  {
    playSound(R.raw.move_self_check);
  }

  public void playNotify()
  {
    playSound(R.raw.notify);
  }

  public void playTenSeconds()
  {
    playSound(R.raw.tenseconds);
  }

  private void playSound(int soundResource)
  {
    final SharedPreferences SharedPreferences = context.getSharedPreferences("sharedData", 0);
    if(SharedPreferences.getBoolean(SharedPreferences.getString("username", "") + "enableSounds", true))
    {
      try
      {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundResource);
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
          public void onCompletion(MediaPlayer mediaPlayer)
          {
            mediaPlayer.release();
          }
        });
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
  }
}

