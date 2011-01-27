package com.chess.utilities;

import java.util.Timer;
import java.util.TimerTask;

import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class WebService extends Service {

	public Timer repeatble = null;
	private int CODE = 0;
	private ProgressDialog PD;
	public static final String BROADCAST_ACTION = "GetServerResponse";

  private LccHolder lccHolder;

  public void onCreate() {
  }

	private final IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder {
		public WebService getService() {
            return WebService.this;
        }
    }
	@Override
	public IBinder onBind(Intent i) {
		return mBinder;
	}

	public void RunSingleTask(int CODE, String query, ProgressDialog PD){
		this.CODE = CODE;
		this.PD = PD;
		new SingleTask().execute(query);
	}

	public void RunRepeatbleTask(final int CODE, final int DELAY, final int INTERVAL, final String query, final ProgressDialog PD){
		this.CODE = CODE;
		this.PD = PD;

		if(repeatble != null){
			repeatble.cancel();
			repeatble = null;
		}

		repeatble = new Timer();
		repeatble.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				sendBroadcast(new Intent(BROADCAST_ACTION)
					.putExtra("repeatble", true)
					.putExtra("code", CODE)
					.putExtra("result", Web.Request(query, "GET", null, null))
				);
				if(PD != null)
					PD.dismiss();
				stopSelf();
			}
		}, DELAY, INTERVAL);
	}

	public class SingleTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... query) {

			sendBroadcast(new Intent(BROADCAST_ACTION)
				.putExtra("repeatble", false)
				.putExtra("code", CODE)
				.putExtra("result", Web.Request(query[0], "GET", null, null))
			);
			if(PD != null)
				PD.dismiss();
			stopSelf();
			return null;
		}
	}

  /*public void RunChesscomConnectionTask(LccHolder lccHolder, *//*ProgressDialog PD,*//* String... credentials) {
	  	//this.CODE = CODE;
		//this.PD = PD;
    this.lccHolder = lccHolder;
    //lccHolder.getAndroid().setCurrentProgressDialog(PD);
	new ChesscomConnectionTask().execute(credentials);
  }

  public class ChesscomConnectionTask extends AsyncTask<String, Void, Void> {
    protected Void doInBackground(String... credentials) {
      //LOG.info("A user is being logged-in: user=" + credentials[0]);
      lccHolder.getClient().connect(credentials[0], credentials[1], lccHolder.getConnectionListener());
      stopSelf();
      return null;
    }
  }*/

  public void RunChesscomSendChallengeTask(LccHolder lccHolder, ProgressDialog PD, Challenge challenge) {
		//this.CODE = CODE;
		//this.PD = PD;
	  this.lccHolder = lccHolder;
	  lccHolder.getAndroid().setCurrentProgressDialog(PD);
	  new ChesscomSendChallengeTask().execute(challenge);
  }

  public class ChesscomSendChallengeTask extends AsyncTask<Challenge, Void, Void> {
    protected Void doInBackground(Challenge... challenge) {
      lccHolder.getClient().sendChallenge(challenge[0], lccHolder.getChallengeListener());
      stopSelf();
      return null;
    }
  }

  public void RunRepeatble(final int CODE, final int DELAY, final int INTERVAL, final ProgressDialog PD){
		this.CODE = CODE;
		this.PD = PD;

		if(repeatble != null){
			repeatble.cancel();
			repeatble = null;
		}

		repeatble = new Timer();
		repeatble.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				sendBroadcast(new Intent(BROADCAST_ACTION)
					.putExtra("repeatble", true)
					.putExtra("code", CODE)
					.putExtra("result", "Success")
				);
				if(PD != null)
					PD.dismiss();
				stopSelf();
			}
		}, DELAY, INTERVAL);
	}
}

