package com.chess.core;

import com.chess.R;
import com.chess.activities.Singin;
import com.chess.activities.tabs.Home;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.Web;
import com.chess.utilities.WebService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

public abstract class CoreActivity extends Activity {

	public MainApp App;
	public Bundle extras;
	public DisplayMetrics metrics;
	public ProgressDialog PD;
  public LccHolder lccHolder;

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

	public boolean isConnected(){
	    ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
	    NetworkInfo NI = cm.getActiveNetworkInfo();
	    if(NI == null)	return false;
	    else			return NI.isConnectedOrConnecting();
	}

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
    protected void onResume() {
      doBindService();
      registerReceiver(receiver, new IntentFilter(WebService.BROADCAST_ACTION));
      /*if (App.isLiveChess())
      {*/
        registerReceiver(drawOfferedMessageReceiver, new IntentFilter("com.chess.lcc.android-game-draw-offered"));
        registerReceiver(lccConnectionInfoReceiver, new IntentFilter("com.chess.lcc.android-connection-info"));
        registerReceiver(informAndExitReceiver, new IntentFilter("com.chess.lcc.android-info-exit"));
      /*}*/
      super.onResume();

      final LccHolder lccHolder = App.getLccHolder();

      if (App.isLiveChess() && !lccHolder.isConnected()/* && !lccHolder.isConnectingInProgress()*/)
        {
          //lccHolder.setConnectingInProgress(true);
          lccHolder.getClient().connect(App.sharedData.getString("username", ""), App.sharedData.getString("password", ""), lccHolder.getConnectionListener());
          /*appService.RunRepeatble(0, 0, 120000,
					  PD = ProgressDialog.show(this, null, getString(R.string.updatinggameslist), true));*/
        }
    }

    @Override
    protected void onPause() {
    	doUnbindService();
    	if(appService != null && appService.repeatble != null){
    		appService.stopSelf();
    		appService.repeatble.cancel();
    		appService = null;
    	}
    	unregisterReceiver(receiver);
      unregisterReceiver(drawOfferedMessageReceiver);
      unregisterReceiver(lccConnectionInfoReceiver);
      unregisterReceiver(informAndExitReceiver);

      // todo: how to logout user when he/she is switching to another activity?
      /*if (App.isLiveChess() && lccHolder.isConnected())
      {
        lccHolder.logout();
      }*/

    	if(PD != null)
    		PD.dismiss();
    	super.onPause();
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
          if(App.noInternet)	{ App.ShowMessage("Online mode!"); App.offline = false; }
          App.noInternet = false;
        }

        if(resp.contains("Success"))
          Update(retCode);
        else{
          if(App.mTabHost != null && App.mTabHost.getCurrentTab() == 3){
            Update(-2);
            return;
          }
          String title = getString(R.string.error);
          String message = resp;
          if(resp.contains("Error+")){
            message = resp.split("[+]")[1];
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

  public BroadcastReceiver lccConnectionInfoReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      if (App.isLiveChess())
      {
        LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());

        ProgressDialog reconnectingIndicator = lccHolder.getAndroid().getReconnectingIndicator();

        if (reconnectingIndicator == null)
        {
          reconnectingIndicator = new ProgressDialog(context);
          reconnectingIndicator.setMessage(intent.getExtras().getString("message"));
          reconnectingIndicator.setOnCancelListener(new DialogInterface.OnCancelListener()
          {
            public void onCancel(DialogInterface dialog)
            {

              final Intent intent = new Intent(App, Singin.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              //reconnectingIndicator.dismiss();
              App.startActivity(intent);
            }
          });
          reconnectingIndicator.setCancelable(true);
          reconnectingIndicator.setIndeterminate(true);
          lccHolder.getAndroid().setReconnectingIndicator(reconnectingIndicator);
        }

        if (intent.getExtras().getBoolean("enable"))
        {
          /*if (!reconnectingIndicator.isShowing())
          {*/
            reconnectingIndicator.show();
          /*}*/
        }
        else
        {
          reconnectingIndicator.hide();
        }
      }
    }
  };

  public BroadcastReceiver informAndExitReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setCancelable(false)
        .setTitle(intent.getExtras().getString("title"))
        .setMessage(intent.getExtras().getString("message"))
        .setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int whichButton)
          {
            final Intent intent = new Intent(App, Singin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.startActivity(intent);
          }
        }).create().show();
    }
  };

}
