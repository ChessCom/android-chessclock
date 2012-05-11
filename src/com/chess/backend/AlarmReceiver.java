package com.chess.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.chess.backend.entity.AppData;
import com.chess.backend.tasks.UpdateStatusTask;


public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		new UpdateStatusTask(context).execute(AppData.getUserToken(context));
	}

}
