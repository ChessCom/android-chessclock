package com.chess.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.chess.R;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.ui.activities.OnlineScreenActivity;
import com.chess.utilities.AppUtils;


public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_MOVE_STATUS);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(context));
		new GetStringObjTask(new UpdateListener(context)).executeTask(loadItem);
	}

	private class UpdateListener extends AbstractUpdateListener<String> {
		public UpdateListener(Context context) {
			super(context);
		}

		@Override
		public void updateData(String returnedObj) {
			if(returnedObj.contains(RestHelper.R_YOUR_MOVE)){
				AppUtils.showMoveStatusNotification(getMeContext(),
						getMeContext().getString(R.string.your_move),
						getMeContext().getString(R.string.online_challenge_wait),
						StaticData.MOVE_REQUEST_CODE,
						OnlineScreenActivity.class);

				getMeContext().sendBroadcast(new Intent(IntentConstants.CHALLENGES_LIST_UPDATE));
			}
		}
	}

}
