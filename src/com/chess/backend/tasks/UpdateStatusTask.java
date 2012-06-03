package com.chess.backend.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.Web;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.activities.OnlineScreenActivity;
import com.chess.utilities.AppUtils;

/**
 * UpdateStatusTask class
 *
 * @author alien_roger
 * @created at: 21.04.12 7:01
 */
public class UpdateStatusTask extends AsyncTask<String, Void, Boolean>{

	private Context context;

	public UpdateStatusTask(Context context) {
		this.context = context;
	}

	@Override
	protected Boolean doInBackground(String... tokens) {  // TODO use rest helper
		String response = Web.Request("http://www." + LccHolder.HOST
				+ "/api/get_move_status?id=" + tokens[0], "GET", null, null);

		return response.contains(RestHelper.R_YOUR_MOVE);
	}

	@Override
	protected void onPostExecute(Boolean need2Update) {
		super.onPostExecute(need2Update);

		if(need2Update){
			AppUtils.showMoveStatusNotification(context,
					context.getString(R.string.your_move),
					context.getString(R.string.online_challenge_wait),
					StaticData.MOVE_REQUEST_CODE,
					OnlineScreenActivity.class);

			context.sendBroadcast(new Intent(IntentConstants.CHALLENGES_LIST_UPDATE));
		}else{
		}
	}
}
