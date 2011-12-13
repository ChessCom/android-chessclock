package com.chess.core;

import java.io.IOException;
import java.util.ArrayList;

import com.chess.lcc.android.LccHolder;
import com.chess.model.*;
import com.chess.utilities.BitmapLoader;
import com.chess.utilities.SoundPlayer;
import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixIABRectangleMAdView;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.widget.LinearLayout;
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
  private MobclixAdView rectangleAdview;
  private MobclixAdView bannerAdview;
  private LinearLayout bannerAdviewWrapper;
  private boolean adviewPaused;
  private boolean networkChangedNotification;
  private boolean forceLoadAd;
  private boolean forceRectangleAd;

	/*public void onCreate()
	  {
		soundPlayer = new SoundPlayer(this);
	  }*/

	public void ShowMessage(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	public void ShowDialog(Context ctx, String title, String message){
    if (message == null || message.trim().equals(""))
    {
      return;
    }
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
    board = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(b, "drawable", "com.chess"));
  }

	public void LoadPieces(String p){
		pieces = new Bitmap[2][6];
		pieces[0][0] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_wp", "drawable", "com.chess"));
		pieces[0][1] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_wn", "drawable", "com.chess"));
		pieces[0][2] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_wb", "drawable", "com.chess"));
		pieces[0][3] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_wr", "drawable", "com.chess"));
		pieces[0][4] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_wq", "drawable", "com.chess"));
		pieces[0][5] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_wk", "drawable", "com.chess"));
		pieces[1][0] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_bp", "drawable", "com.chess"));
		pieces[1][1] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_bn", "drawable", "com.chess"));
		pieces[1][2] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_bb", "drawable", "com.chess"));
		pieces[1][3] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_br", "drawable", "com.chess"));
		pieces[1][4] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_bq", "drawable", "com.chess"));
		pieces[1][5] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p+"_bk", "drawable", "com.chess"));
  }

  public LccHolder getLccHolder()
  {
    if (lccHolder == null)
    {
      try
      {
		String versionName = "";
		try {
			versionName = getPackageManager().getPackageInfo("com.chess", 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		lccHolder = LccHolder.getInstance(getAssets().open("chesscom.pkcs12"), versionName);
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
    LccHolder.LOG.info("LCCLOG: Set Live Chess mode to: " + liveChess);
    this.liveChess = liveChess;
  }

  public SoundPlayer getSoundPlayer()
  {
    if (soundPlayer == null)
    {
      soundPlayer = new SoundPlayer(this);
    }
    return soundPlayer;
  }

  public MobclixAdView getBannerAdview()
  {
    return bannerAdview;
  }

  public void setBannerAdview(MobclixAdView bannerAdview)
  {
    this.bannerAdview = bannerAdview;
  }

  public MobclixAdView getRectangleAdview()
  {
    return rectangleAdview;
  }

  public void setRectangleAdview(MobclixAdView rectangleAdview)
  {
    this.rectangleAdview = rectangleAdview;
  }
  
  public void setNetworkChangedNotification(boolean networkChangedNotification)
  {
    this.networkChangedNotification = networkChangedNotification;
  }
  
  public boolean isNetworkChangedNotification() {
    return networkChangedNotification;
  }

	public boolean isAdviewPaused() {
		return adviewPaused;
	}

	public void setAdviewPaused(boolean adviewPaused) {
		this.adviewPaused = adviewPaused;
	}

	public LinearLayout getBannerAdviewWrapper() {
		return bannerAdviewWrapper;
	}

	public void setBannerAdviewWrapper(LinearLayout bannerAdviewWrapper) {
		this.bannerAdviewWrapper = bannerAdviewWrapper;
	}

	public void setForceLoadAd(boolean forceLoadAd)
	{
		this.forceLoadAd = forceLoadAd;
	}

	public boolean isForceLoadAd()
	{
		return forceLoadAd;
	}

	public boolean isForceRectangleAd() {
		return forceRectangleAd;
	}

	public void setForceRectangleAd(boolean forceRectangleAd) {
		this.forceRectangleAd = forceRectangleAd;
	}
}
