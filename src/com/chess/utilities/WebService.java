package com.chess.utilities;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.core.AppConstants;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WebService extends Service {

	private Timer repeatableTimer = null;
	private int code = 0;
	private MyProgressDialog progressDialog;
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

	public void RunSingleTask(int code, String query, MyProgressDialog progressDialog) {
		this.code = code;
		this.progressDialog = progressDialog;
		new SingleTask().execute(query, "GET");
	}

	public void RunSingleTaskPost(int code, String query, MyProgressDialog progressDialog, String... parameters) {
		this.code = code;
		this.progressDialog = progressDialog;
		new SingleTask().execute(query, "POST", parameters[0], parameters[1], parameters[2], parameters[3]);
	}

	public void RunRepeatableTask(final int code, final int DELAY, final int INTERVAL,
								  final String query, final MyProgressDialog progressDialog) {
		this.code = code;
		this.progressDialog = progressDialog;

		if (repeatableTimer != null) {
			repeatableTimer.cancel();
			repeatableTimer = null;
		}

		repeatableTimer = new Timer();
		repeatableTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				sendBroadcast(new Intent(BROADCAST_ACTION)
						.putExtra(AppConstants.REPEATABLE_TASK, true)
						.putExtra(AppConstants.CALLBACK_CODE, code)
						.putExtra(AppConstants.REQUEST_RESULT, Web.Request(query, "GET", null, null))
				);
				if (progressDialog != null)
					progressDialog.dismiss();
				stopSelf();
			}
		}, DELAY, INTERVAL);
	}

	public class SingleTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... options) {

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			if (options.length >= 3 && options[2] != null) {
				nameValuePairs.add(new BasicNameValuePair(options[2], options[3]));
				nameValuePairs.add(new BasicNameValuePair(options[4], options[5]));

			}
			try {
				sendBroadcast(new Intent(BROADCAST_ACTION)
						.putExtra(AppConstants.REPEATABLE_TASK, false)
						.putExtra(AppConstants.CALLBACK_CODE, code)
						.putExtra(AppConstants.REQUEST_RESULT, Web.Request(options[0], options[1], null,
								new UrlEncodedFormEntity(nameValuePairs)))
				);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (progressDialog != null)
				progressDialog.dismiss();
			stopSelf();
			return null;
		}
	}

	/*public void RunChesscomConnectionTask(LccHolder lccHolder, *//*MyProgressDialog progressDialog,*//* String... credentials) {
	  	//this.code = code;
		//this.progressDialog = progressDialog;
    this.lccHolder = lccHolder;
    //lccHolder.getAndroid().setCurrentProgressDialog(progressDialog);
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


	public void RunRepeatble(final int code, final int DELAY, final int INTERVAL, final MyProgressDialog progressDialog) {
		this.code = code;
		this.progressDialog = progressDialog;

		if (repeatableTimer != null) {
			repeatableTimer.cancel();
			repeatableTimer = null;
		}

		repeatableTimer = new Timer();
		repeatableTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				sendBroadcast(new Intent(BROADCAST_ACTION)
						.putExtra(AppConstants.REPEATABLE_TASK, true)
						.putExtra(AppConstants.CALLBACK_CODE, code)
						.putExtra(AppConstants.REQUEST_RESULT, AppConstants.SUCCESS)
				);
				if (progressDialog != null)
					progressDialog.dismiss();
				stopSelf();
			}
		}, DELAY, INTERVAL);
	}

	public Timer getRepeatableTimer() {
		return repeatableTimer;
	}

	public void setRepeatableTimer(Timer repeatableTimer) {
		this.repeatableTimer = repeatableTimer;
	}
}

