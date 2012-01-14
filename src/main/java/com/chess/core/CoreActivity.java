package com.chess.core;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.*;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.widget.TextView;
import org.apache.http.util.ByteArrayBuffer;

import com.chess.R;
import com.chess.activities.Singin;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.*;
import com.flurry.android.FlurryAgent;
import com.mobclix.android.sdk.MobclixAdView;

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
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;

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
		if (App.sharedData == null)
		{
			App.sharedData = getSharedPreferences("sharedData", 0);
			App.SDeditor = App.sharedData.edit();
		}

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
          App.LoadBoard(App.res_boards[App.sharedData.getInt(App.sharedData.getString("username", "") + "board", 8)]);
          App.LoadPieces(App.res_pieces[App.sharedData.getInt(App.sharedData.getString("username", "") + "pieces", 0)]);
          App.loadCapturedPieces();
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

    final MyProgressDialog reconnectingIndicator = lccHolder.getAndroid().getReconnectingIndicator();
    if (!lccHolder.isConnectingInProgress() && reconnectingIndicator != null)
    {
      reconnectingIndicator.dismiss();
      lccHolder.getAndroid().setReconnectingIndicator(null);
    }

    if(App.isLiveChess() && !lccHolder.isConnected() && !lccHolder.isConnectingInProgress())
    {
      //lccHolder.getAndroid().showConnectingIndicator();
      manageConnectingIndicator(true, "Loading Live Chess");

      //startService(new Intent(getApplicationContext(), NetworkChangeService.class));

      new AsyncTask<Void, Void, Void>()
      {
        @Override
        protected Void doInBackground(Void... voids)
        {
          final LccHolder lccHolder = App.getLccHolder();
          //lccHolder.setConnectingInProgress(true);
          lccHolder.getClient().disconnect();
          lccHolder.setNetworkTypeName(null);
          lccHolder.setConnectingInProgress(true);
          lccHolder.getClient()
                  .connect(App.sharedData.getString("user_session_id", ""), lccHolder.getConnectionListener());
          /*appService.RunRepeatble(0, 0, 120000,
          PD = MyProgressDialog.show(this, null, getString(R.string.updatinggameslist), true));*/
          return null;
        }
      }.execute();
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
    //registerReceiver(networkChangeNotificationReceiver, new IntentFilter("com.chess.lcc.android-network-change"));
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
      checkUpdate();
    }
    /*if (App.isNetworkChangedNotification())
    {
      showNetworkChangeNotification();
    }*/
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
      //unregisterReceiver(networkChangeNotificationReceiver);

      // todo: how to logout user when he/she is switching to another activity?
      /*if (App.isLiveChess() && lccHolder.isConnected())
      {
        lccHolder.logout();
      }*/

      App.SDeditor.putLong("lastActivityPauseTime", System.currentTimeMillis());
      App.SDeditor.commit();
      App.setForceBannerAdOnFailedLoad(false);

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
      LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
      final com.chess.live.client.Game game = App.getLccHolder().getGame(App.gameId);
      final AlertDialog alertDialog = new AlertDialog.Builder(CoreActivity.this)
        //.setTitle(intent.getExtras().getString("title"))
        .setMessage(intent.getExtras().getString("message"))
        .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            App.getLccHolder().getAndroid().runMakeDrawTask(game);
          }
        })
        .setNeutralButton(getString(R.string.decline), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            lccHolder.getAndroid().runRejectDrawTask(game);
          }
        })
        /*.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              startActivity(new Intent(CoreActivity.this, Game.class).
                putExtra("mode", 4).
                putExtra("game_id", el.values.get("game_id")));
            }
        })*/
        .create();
      alertDialog.setCanceledOnTouchOutside(true);
      alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
      {
        public void onCancel(DialogInterface dialogInterface)
        {
			lccHolder.getAndroid().runRejectDrawTask(game);
        }
      });
      alertDialog.getWindow().setGravity(Gravity.BOTTOM);
      alertDialog.show();
    }
  };

  protected BroadcastReceiver challengesListUpdateReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      if (App.isLiveChess())
      {
        LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
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
        LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction() + ", enable=" + intent.getExtras().getBoolean("enable"));
        MyProgressDialog reconnectingIndicator = lccHolder.getAndroid().getReconnectingIndicator();
        boolean enable = intent.getExtras().getBoolean("enable");

        if (reconnectingIndicator != null)
        {
          reconnectingIndicator.dismiss();
          lccHolder.getAndroid().setReconnectingIndicator(null);
        }
        /*else */
        if (enable)
        {
          if (MobclixHelper.isShowAds(App) && MobclixHelper.getBannerAdview(App) != null && !App.isAdviewPaused())
          {
            MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(App), App);
          }
          reconnectingIndicator = new MyProgressDialog(context);
          reconnectingIndicator.setMessage(intent.getExtras().getString("message"));
          reconnectingIndicator.setOnCancelListener(new DialogInterface.OnCancelListener()
          {
            public void onCancel(DialogInterface dialog)
            {
              lccHolder.logout();
              final Intent intent = new Intent(CoreActivity.this, Tabs.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              //reconnectingIndicator.dismiss();
              App.startActivity(intent);
            }
          });
          reconnectingIndicator.setCancelable(true);
          reconnectingIndicator.setIndeterminate(true);
          try
          {
            reconnectingIndicator.show();
            lccHolder.getAndroid().setReconnectingIndicator(reconnectingIndicator);
          }
          catch (Exception e)
          {
            lccHolder.logout();
            intent = new Intent(CoreActivity.this, Tabs.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //reconnectingIndicator.dismiss();
            App.startActivity(intent);
          }
        }
        else
        {
          if (MobclixHelper.isShowAds(App) && MobclixHelper.getBannerAdview(App) != null && App.isAdviewPaused())
          {
            MobclixHelper.resumeAdview(MobclixHelper.getBannerAdview(App), App);
          }
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
       LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
       final TextView messageView = new TextView(context);
       messageView.setMovementMethod(LinkMovementMethod.getInstance());
       messageView.setText(Html.fromHtml(intent.getExtras().getString("message")));
       messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
       messageView.setGravity(Gravity.CENTER);

       new AlertDialog.Builder(context)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .setCancelable(true)
         .setTitle(intent.getExtras().getString("title"))
         .setView(messageView)
         .setPositiveButton("OK", new DialogInterface.OnClickListener()
         {
           public void onClick(final DialogInterface dialog, int whichButton)
           {
             final Handler handler = new Handler();
             handler.post(new Runnable()
             {
               public void run()
               {
                 dialog.dismiss();
               }
             });
           }
         }).create().show();
     }
   };

  private BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
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

	/*protected void showGameEndAds(LinearLayout adviewWrapper)
    {
      if (App.isAdviewPaused())
      {
        MobclixHelper.resumeAdview(getRectangleAdview(), App);
      }
    }*/

  /*protected void showAds(MobclixMMABannerXLAdView adview)
  {
    if(!isShowAds())
    {
      adview.setVisibility(View.GONE);
    }
    else
    {
      adview.setVisibility(View.VISIBLE);
      //adview.setAdUnitId("agltb3B1Yi1pbmNyDQsSBFNpdGUYmrqmAgw");
      //adview.setAdUnitId("agltb3B1Yi1pbmNyDAsSBFNpdGUYkaoMDA"); //test
      //adview.loadAd();
    }
  }*/

  public MobclixAdView getRectangleAdview()
  {
    return App.getRectangleAdview();
  }

  public void setRectangleAdview(MobclixAdView rectangleAdview)
  {
    App.setRectangleAdview(rectangleAdview);
  }

  private void checkUpdate()
  {
    new AsyncTask<Void, Void, Void>()
    {
      @Override
      protected Void doInBackground(Void... voids)
      {
        try
        {
          URL updateURL = new URL("http://www.chess.com/api/get_android_version");
          URLConnection conn = updateURL.openConnection();
          InputStream is = conn.getInputStream();
          BufferedInputStream bis = new BufferedInputStream(is);
          ByteArrayBuffer baf = new ByteArrayBuffer(50);

          int current = 0;
          while((current = bis.read()) != -1)
          {
            baf.append((byte)current);
          }

          final String s = new String(baf.toByteArray());
          String[] valuesArray = s.trim().split("\\|", 2);
          
          int actualVersion = getPackageManager().getPackageInfo("com.chess", 0).versionCode;
          System.out.println("LCCLOG: valuesArray[1].trim() " + valuesArray[1].trim());
          
          int minimumVersion = Integer.valueOf(valuesArray[0].trim());
          int prefferedVersion = Integer.valueOf(valuesArray[1].trim());
              
          Boolean force = null;
          if (actualVersion < prefferedVersion)
          {
        	  force = false;
          }
          if (actualVersion < minimumVersion)
          {
        	  force = true;
          }
          
          if (force != null)
          {
            final boolean forceFlag = force;
            new AlertDialog.Builder(CoreActivity.this)
              .setIcon(R.drawable.icon)
              .setTitle("Update Check")
              .setMessage("An update is available! Please update")
              .setCancelable(false)
              .setPositiveButton("OK", new DialogInterface.OnClickListener() 
                {
                  public void onClick(DialogInterface dialog, int whichButton) 
                  {
                    //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.chess"));
                	if (forceFlag)
                	{
                		App.SDeditor.putLong("com.chess.startDay", 0);
                		App.SDeditor.commit();
                		startActivity(new Intent(CoreActivity.this, Singin.class));
                		finish();
                	}
                	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.chess"));
                    startActivity(intent);

                  }
                }
            )
            .show();
          }
        } 
        catch (Exception e) 
        {
        }
        return null;
      }
	  }.execute();
  }
  
  private void showNetworkChangeNotification()
  {
	  new AlertDialog.Builder(CoreActivity.this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setCancelable(false)
		.setTitle("Logout")
		.setMessage("Network was changed. Please relogin to Live")
		.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
		  public void onClick(DialogInterface dialog, int whichButton)
		  {
			//App.setNetworkChangedNotification(false);
			startActivity(new Intent(CoreActivity.this, Tabs.class));
		  }
		}).create().show();
  }

  /*private BroadcastReceiver networkChangeNotificationReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      if (App.isNetworkChangedNotification())
      {
        showNetworkChangeNotification();
      }
    }
  };*/
}