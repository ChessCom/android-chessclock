package com.chess.backend.tasks;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.Web;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.activities.OnlineScreenActivity;
import com.chess.utilities.Utils;

/**
 * UpdateStatusTask class
 *
 * @author alien_roger
 * @created at: 21.04.12 7:01
 */
public class UpdateStatusTask extends AsyncTask<String, Void, Boolean>{

	private Context context;
	private SharedPreferences preferences;

	public UpdateStatusTask(Context context) {
		this.context = context;
		preferences = context.getSharedPreferences("sharedData", Context.MODE_PRIVATE);
	}

	@Override
	protected Boolean doInBackground(String... tokens) {  // TODO use rest helper
		Boolean need2Update = false;
		String response = Web.Request("http://www." + LccHolder.HOST
				+ "/api/get_move_status?id=" + tokens[0], "GET", null, null);

		if(response.trim().contains(RestHelper.R_YOUR_MOVE)){
			String params[] = response.split(RestHelper.SYMBOL_PARAMS_SPLIT);
			if(!preferences.getString(StaticData.SHP_USER_LAST_MOVE_UPDATE_TIME, "0").equals(params[2])){
				// If it's new time show notification
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(StaticData.SHP_USER_LAST_MOVE_UPDATE_TIME, params[2]);
				editor.commit();
				need2Update = true;
			}
		}
		return need2Update;
	}

	@Override
	protected void onPostExecute(Boolean need2Update) {
		super.onPostExecute(need2Update);

		if(need2Update){
			Utils.showMoveStatusNotification(context,
					context.getString(R.string.your_move),
					context.getString(R.string.online_challenge_wait),
					StaticData.MOVE_REQUEST_CODE,
					OnlineScreenActivity.class); // TODO check
		}else{
			NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notifyManager.cancel(R.id.notification_message);
		}
	}
}
