package com.chess.backend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * MyBroadcastReceiver class
 *
 * @author alien_roger
 * @created at: 12.09.12 8:14
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

	@Override
	public final void onReceive(Context context, Intent intent) {
		MyIntentService.runIntentInService(context, intent);
		setResult(Activity.RESULT_OK, null, null);
	}
}
