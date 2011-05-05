package com.chess.core;

import java.io.IOException;
import java.util.ArrayList;

import com.chess.lcc.android.LccHolder;
import com.chess.model.*;
import com.chess.utilities.SoundPlayer;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TabHost;
import android.widget.Toast;

public class MainApp extends Application {

	public static String APP_ID = "2427617054";

	public SharedPreferences sharedData;
	public SharedPreferences.Editor SDeditor;

	public TabHost mTabHost;
	public static int loadPrev = 0;

  private LccHolder lccHolder;
  private boolean liveChess;

  private SoundPlayer soundPlayer;

  public void onCreate()
  {
    soundPlayer = new SoundPlayer(this);
  }

	public void ShowMessage(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	public void ShowDialog(Context ctx, String title, String message){
		new AlertDialog.Builder(ctx)
		.setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
		.setMessage(message)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).create().show();
	}

	public boolean guest = false, noInternet = false, offline = false, acceptdraw = false;
	public Bitmap[][] pieces;
	public Bitmap board;
	public ArrayList<GameListElement> GameListItems = new ArrayList<GameListElement>();
	public com.chess.model.Game OnlineGame;
	public String gameId = "";
	public ArrayList<Tactic> TacticsBatch;
	public Tactic Tactic;
	public int currentTacticProblem = 0;

	public int[] strength = {1000, 3000, 5000, 10000, 30000, 60000};
	public String[] res_boards = {	"blue",
									"brown",
									"green",
									"grey",
									"marble",
									"red",
									"tan",
									"wood_light",
									"wood_dark"};
	public String[] res_pieces = {	"alpha",
									"book",
									"cases",
									"classic",
									"club",
									"condal",
									"maya",
									"modern",
									"vintage"};
	public void LoadBoard(String b){
		board = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(b, "drawable", "com.chess"));
    }
	public void LoadPieces(String p){
		pieces = new Bitmap[2][6];
		pieces[0][0] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_wp", "drawable", "com.chess"));
		pieces[0][1] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_wn", "drawable", "com.chess"));
		pieces[0][2] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_wb", "drawable", "com.chess"));
		pieces[0][3] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_wr", "drawable", "com.chess"));
		pieces[0][4] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_wq", "drawable", "com.chess"));
		pieces[0][5] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_wk", "drawable", "com.chess"));
		pieces[1][0] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_bp", "drawable", "com.chess"));
		pieces[1][1] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_bn", "drawable", "com.chess"));
		pieces[1][2] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_bb", "drawable", "com.chess"));
		pieces[1][3] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_br", "drawable", "com.chess"));
		pieces[1][4] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_bq", "drawable", "com.chess"));
		pieces[1][5] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(p+"_bk", "drawable", "com.chess"));
  }

  public LccHolder getLccHolder()
  {
    if (lccHolder == null)
    {
      try
      {
        lccHolder = LccHolder.getInstance(getAssets().open("chesscom.pkcs12"));
      }
      catch(IOException e)
      {
        e.printStackTrace();
      }
      //lccClient = lccHolder.getClient();
      lccHolder.getAndroid().setContext(this);
    }

    return lccHolder;
  }

  public boolean isLiveChess()
  {
    return liveChess;
  }

  public void setLiveChess(boolean liveChess)
  {
    LccHolder.LOG.info("INFO: Set Live Chess mode to: " + liveChess);
    this.liveChess = liveChess;
  }

  public SoundPlayer getSoundPlayer()
  {
	  return soundPlayer;
  }
}
