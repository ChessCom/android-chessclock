package com.chess.core;

import com.chess.R;
import com.chess.activities.Singin;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.SoundPlayer;
import com.chess.utilities.Web;
import com.chess.utilities.WebService;
import com.flurry.android.FlurryAgent;
import com.mopub.mobileads.MoPubView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public abstract class CoreActivity extends Activity {

	public MainApp App;
	public Bundle extras;
	public DisplayMetrics metrics;
	public MyProgressDialog PD;
  public LccHolder lccHolder;
  private PowerManager.WakeLock wakeLock;

	public abstract void LoadNext(int code);
	public abstract void LoadPrev(int code);
	public abstract void Update(int code);

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			LoadPrev(MainApp.loadPrev);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App = (MainApp)getApplication();
		extras = getIntent().getExtras();

		//get global Shared Preferences
        App.sharedData = getSharedPreferences("sharedData", 0);
        App.SDeditor = App.sharedData.edit();

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

    lccHolder = App.getLccHolder();
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

	/*public boolean isConnected(){
	    ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
	    NetworkInfo NI = cm.getActiveNetworkInfo();
	    if(NI == null)	return false;
	    else			return NI.isConnectedOrConnecting();
	}*/

	public boolean mIsBound;
	public WebService appService = null;
	public boolean doBindService() {
		mIsBound = getApplicationContext().bindService(new Intent(this, WebService.class), onService, Context.BIND_AUTO_CREATE);
        return mIsBound;
    }
	public void doUnbindService() {
        if (mIsBound) {
        	getApplicationContext().unbindService(onService);
            mIsBound = false;
        }
    }
	public ServiceConnection onService = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			appService = ((WebService.LocalBinder)rawBinder).getService();
			Update(-1);
		}
		public void onServiceDisconnected(ComponentName className) {
			appService = null;
		}
	};

  @Override
  protected void onResume()
  {
    super.onResume();

    if (App.board == null || App.pieces == null)
    {
      new Handler().post(new Runnable()
      {
        public void run()
        {
          App.LoadBoard(App.res_boards[App.sharedData.getInt(App.sharedData.getString("username", "") + "board", 0)]);
          App.LoadPieces(App.res_pieces[App.sharedData.getInt(App.sharedData.getString("username", "") + "pieces", 0)]);
        }
      });
      if (!App.sharedData.getString("username", "").equals(""))
      {
        final Intent intent = new Intent(App, Tabs.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.startActivity(intent);
      }
      else
      {
        startActivity(new Intent(App, Singin.class));
      }
    }

    if(App.isLiveChess() && !lccHolder.isConnected()/* && !lccHolder.isConnectingInProgress()*/)
    {
      //lccHolder.getAndroid().showConnectingIndicator();
      manageConnectingIndicator(true, "Loading Live Chess");
      new Handler().post(new Runnable()
      {
        public void run()
        {
          final LccHolder lccHolder = App.getLccHolder();
          //lccHolder.setConnectingInProgress(true);
          lccHolder.getClient().disconnect();
          lccHolder.getClient()
            .connect(App.sharedData.getString("user_session_id", ""), lccHolder.getConnectionListener());
          /*appService.RunRepeatble(0, 0, 120000,
          PD = MyProgressDialog.show(this, null, getString(R.string.updatinggameslist), true));*/
        }
      });
    }
    doBindService();
    registerReceiver(receiver, new IntentFilter(WebService.BROADCAST_ACTION));
    /*if (App.isLiveChess())
    {*/
    registerReceiver(lccLoggingInInfoReceiver, new IntentFilter("com.chess.lcc.android-logging-in-info"));
    registerReceiver(lccReconnectingInfoReceiver, new IntentFilter("com.chess.lcc.android-reconnecting-info"));
    registerReceiver(drawOfferedMessageReceiver, new IntentFilter("com.chess.lcc.android-game-draw-offered"));
    registerReceiver(informAndExitReceiver, new IntentFilter("com.chess.lcc.android-info-exit"));
    registerReceiver(obsoleteProtocolVersionReceiver,
                     new IntentFilter("com.chess.lcc.android-obsolete-protocol-version"));
    registerReceiver(infoMessageReceiver, new IntentFilter("com.chess.lcc.android-info"));
    /*}*/
    if (App.sharedData.getLong("com.chess.firstTimeStart", 0) == 0)
    {
      App.SDeditor.putLong("com.chess.firstTimeStart", System.currentTimeMillis());
      App.SDeditor.putInt("com.chess.adsShowCounter", 0);
      App.SDeditor.commit();
    }
    long startDay = App.sharedData.getLong("com.chess.startDay", 0);
    if (App.sharedData.getLong("com.chess.startDay", 0) == 0 || !DateUtils.isToday(startDay))
    {
      App.SDeditor.putLong("com.chess.startDay", System.currentTimeMillis());
      App.SDeditor.putBoolean("com.chess.showedFullscreenAd", false);
      App.SDeditor.commit();
    }
  }

    @Override
    protected void onPause() {
    	super.onPause();
    	doUnbindService();
    	if(appService != null && appService.repeatble != null){
    		appService.stopSelf();
    		appService.repeatble.cancel();
    		appService = null;
    	}
    	unregisterReceiver(receiver);
      unregisterReceiver(drawOfferedMessageReceiver);
      unregisterReceiver(lccLoggingInInfoReceiver);
      unregisterReceiver(lccReconnectingInfoReceiver);
      unregisterReceiver(informAndExitReceiver);
      unregisterReceiver(obsoleteProtocolVersionReceiver);
      unregisterReceiver(infoMessageReceiver);

      // todo: how to logout user when he/she is switching to another activity?
      /*if (App.isLiveChess() && lccHolder.isConnected())
      {
        lccHolder.logout();
      }*/

    	if(PD != null)
    		PD.dismiss();
    }

    public String response = "", rep_response = "";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        //getting extras
        Bundle rExtras = intent.getExtras();
        boolean repeatble = false;
        String resp = "";
        int retCode = -2;
        try{
          repeatble = rExtras.getBoolean("repeatble");
          if(repeatble)	resp = rep_response = rExtras.getString("result");
          else			resp = response = rExtras.getString("result");

          retCode = rExtras.getInt("code");
        } catch (Exception e) {
          Update(-2);
          return;
        }

        if(Web.StatusCode == -1)	App.noInternet = true;
        else{
          if(App.noInternet)	{ /*App.ShowMessage("Online mode!");*/ App.offline = false; }
          App.noInternet = false;
        }

        if(resp.contains("Success"))
          Update(retCode);
        else{
          if(App.mTabHost != null && App.mTabHost.getCurrentTab() == 3){
            Update(-2);
            return;
          }
          if (resp.length()==0)
          {
            Update(-2);
            return;
          }
          String title = getString(R.string.error);
          String message = resp;
          if(resp.contains("Error+")){
            message = resp.split("[+]")[1];
          }
          else
          {
            Update(-2);
            return;
          }
          if (message == null || message.trim().equals(""))
          {
            Update(-2);
            return;
          }
          new AlertDialog.Builder(CoreActivity.this)
          .setIcon(android.R.drawable.ic_dialog_alert)
              .setTitle(title)
          .setMessage(message)
              .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    Update(-2);
                  }
              }).create().show();
        }
      }
	};

  private BroadcastReceiver drawOfferedMessageReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
      final com.chess.live.client.Game game = App.getLccHolder().getGame(App.gameId);
      new AlertDialog.Builder(CoreActivity.this)
        .setTitle(intent.getExtras().getString("title"))
        .setMessage(intent.getExtras().getString("message"))
        .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              App.getLccHolder().getClient().makeDraw(game, "");
            }
        })
        .setNeutralButton(getString(R.string.decline), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              App.getLccHolder().getClient().rejectDraw(game, "");
            }
        })
        /*.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              startActivity(new Intent(CoreActivity.this, Game.class).
                putExtra("mode", 4).
                putExtra("game_id", el.values.get("game_id")));
            }
        })*/
        .create().show();
    }
  };

  protected BroadcastReceiver challengesListUpdateReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      if (App.isLiveChess())
      {
        LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
        Update(intent.getExtras().getInt("code"));
      }
    }
  };

  public BroadcastReceiver lccReconnectingInfoReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      if (App.isLiveChess())
      {
        LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
        MyProgressDialog reconnectingIndicator = lccHolder.getAndroid().getReconnectingIndicator();
        boolean enable = intent.getExtras().getBoolean("enable");

        if (reconnectingIndicator != null)
        {
          reconnectingIndicator.dismiss();
          lccHolder.getAndroid().setReconnectingIndicator(null);
        }
        /*else */if (enable)
        {
          reconnectingIndicator = new MyProgressDialog(context);
          reconnectingIndicator.setMessage(intent.getExtras().getString("message"));
          reconnectingIndicator.setOnCancelListener(new DialogInterface.OnCancelListener()
          {
            public void onCancel(DialogInterface dialog)
            {
              lccHolder.logout();
              final Intent intent = new Intent(App, Singin.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              //reconnectingIndicator.dismiss();
              App.startActivity(intent);
            }
          });
          reconnectingIndicator.setCancelable(true);
          reconnectingIndicator.setIndeterminate(true);
          reconnectingIndicator.show();
          lccHolder.getAndroid().setReconnectingIndicator(reconnectingIndicator);
        }
      }
    }
  };

  public BroadcastReceiver informAndExitReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      final String message = intent.getExtras().getString("message");
      if (message == null || message.trim().equals(""))
      {
        return;
      }
      new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setCancelable(false)
        .setTitle(intent.getExtras().getString("title"))
        .setMessage(message)
        .setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            if (App.isLiveChess()/* && lccHolder.isConnected()*/)
            {
              lccHolder.logout();
            }
            final Intent intent = new Intent(App, Singin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.startActivity(intent);
          }
        }).create().show();
    }
  };

  public BroadcastReceiver obsoleteProtocolVersionReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setCancelable(false)
        .setTitle("Version Check")
        .setMessage("The client version is obsolete. Please update")
        .setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            final Handler handler = new Handler();
            handler.post(new Runnable()
            {
              public void run()
              {
                App.setLiveChess(false);
                lccHolder.setConnected(false);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.chess.com/play/android.html")));
              }
            });
            final Intent intent = new Intent(App, Tabs.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.startActivity(intent);
          }
        }).create().show();
    }
  };

   private BroadcastReceiver infoMessageReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
      App.ShowDialog(CoreActivity.this, intent.getExtras().getString("title"), intent.getExtras().getString("message"));
    }
  };

  private BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
      boolean enable = intent.getExtras().getBoolean("enable");
      manageConnectingIndicator(enable, intent.getExtras().getString("message"));
    }
  };

  private void manageConnectingIndicator(boolean enable, String message)
  {
    if(App.isLiveChess())
    {
      MyProgressDialog connectingIndicator = lccHolder.getAndroid().getConnectingIndicator();
      if(connectingIndicator != null)
      {
        connectingIndicator.dismiss();
        lccHolder.getAndroid().setConnectingIndicator(null);
      }
      else if(enable)
      {
        connectingIndicator = new MyProgressDialog(this);
        connectingIndicator.setMessage(message);
        /*connectingIndicator.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
          public void onCancel(DialogInterface dialog)
          {

            final Intent intent = new Intent(App, Singin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //connectingIndicator.dismiss();
            lccHolder.logout();
            App.startActivity(intent);
          }
        });*/
        connectingIndicator.setCancelable(true);
        connectingIndicator.setIndeterminate(true);
        connectingIndicator.show();
        lccHolder.getAndroid().setConnectingIndicator(connectingIndicator);
      }
    }
  }

  protected void disableScreenLock()
  {
    final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "com.chess.core.CoreActivity");
    wakeLock.setReferenceCounted(false);
    wakeLock.acquire();
  }

  protected void enableScreenLock()
  {
	  if (wakeLock != null)
    {
      wakeLock.release();
    }
  }

  public void unregisterReceiver(BroadcastReceiver receiver)
  {
    try {
      super.unregisterReceiver(receiver);
    } catch (IllegalArgumentException e)
    {
      e.printStackTrace();
      // hack for Android's IllegalArgumentException: Receiver not registered
    }
  }

  public Boolean isUserColorWhite()
  {
    try
    {
      return App.OnlineGame.values.get("white_username").toLowerCase().equals(App.sharedData.getString("username", ""));
    }
    catch (Exception e)
    {
      return null;
    }
  }

  public SoundPlayer getSoundPlayer()
  {
	  return App.getSoundPlayer();
  }

  public LccHolder getLccHolder()
  {
    return lccHolder;
  }

  @Override
  protected void onStart() {
	  super.onStart();
	  FlurryAgent.onStartSession(this, "M5ID55IB7UP9SAC88D3M");
  }

  @Override
  protected void onStop() {
	  super.onStop();
	  FlurryAgent.onEndSession(this);
  }

  protected boolean isShowAds()
  {
    boolean liveMembershipLevel =
      lccHolder.getUser() != null ? App.isLiveChess() && (lccHolder.getUser().getMembershipLevel() < 30) : false;
    return ((System.currentTimeMillis() - App.sharedData.getLong("com.chess.firstTimeStart", 0)) >
            (7 * 24 * 60 * 60 * 1000)) && (liveMembershipLevel || (!App.isLiveChess() && Integer.parseInt(
            App.sharedData.getString("premium_status", "0")) < 1));
  }

  protected void showRemoveAds(MoPubView adview, TextView removeAds)
  {
    int adsShowCounter = App.sharedData.getInt("com.chess.adsShowCounter", 0);
    if(adsShowCounter == 10)
    {
      adview.setVisibility(View.GONE);
      removeAds.setVisibility(View.VISIBLE);
      App.SDeditor.putInt("com.chess.adsShowCounter", 0);
      App.SDeditor.commit();
    }
    else
    {
      removeAds.setVisibility(View.GONE);
      adview.setVisibility(View.VISIBLE);
      App.SDeditor.putInt("com.chess.adsShowCounter", adsShowCounter + 1);
      App.SDeditor.commit();
    }
  }

  protected void showAds(MoPubView adview)
  {
    if(!isShowAds())
    {
      adview.setVisibility(View.GONE);
    }
    else
    {
      adview.setVisibility(View.VISIBLE);
      adview.setAdUnitId("agltb3B1Yi1pbmNyDQsSBFNpdGUYmrqmAgw");
      adview.loadAd();
    }
  }

}
