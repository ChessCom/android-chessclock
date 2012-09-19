/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chess;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerUtilities;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.PostJsonDataTask;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	@SuppressWarnings("hiding")
	private static final String TAG = "GCMIntentService";
	private Context context;

	public GCMIntentService() {
        super(ServerUtilities.SENDER_ID);
		context = this;
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.d(TAG, "Device registered: regId = " + registrationId);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GCM_REGISTER);
		loadItem.addRequestParams(RestHelper.GCM_P_ID, AppData.getUserSessionId(context));
		loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

		new PostJsonDataTask(new PostUpdateListener()).execute(loadItem);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.d(TAG, "Device unregistered");

        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.d(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d(TAG, "Received message");

//		AppUtils.showNewMoveStatusNotification(context,
//				context.getString(R.string.your_move),
//				context.getString(R.string.your_turn_in_game_with,
//						gameListItem.getOpponentUsername(),
//						gameListItem.getLastMoveFromSquare() + gameListItem.getLastMoveToSquare()),
//				StaticData.MOVE_REQUEST_CODE,
//				gameListItem);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.d(TAG, "Received deleted messages notification, cnt = "+ total);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.d(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.d(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

//    /**
//     * Issues a notification to inform the user that server has sent a message.
//     */
//    private static void generateNotification(Context context, String message) {
//        int icon = R.drawable.ic_stat_chess;
//        long when = System.currentTimeMillis();
//        NotificationManager notificationManager = (NotificationManager)
//                context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification = new Notification(icon, message, when);
//        String title = context.getString(R.string.app_name);
//        Intent notificationIntent = new Intent(context, HomeScreenActivity.class);
//        // set intent so it does not start a new activity
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
//        notification.setLatestEventInfo(context, title, message, intent);
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notificationManager.notify(0, notification);
//    }

	private class PostUpdateListener extends AbstractUpdateListener<String>{
		public PostUpdateListener() {
			super(GCMIntentService.this);
		}

		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);
			// TODO parse json

			GCMRegistrar.setRegisteredOnServer(context, true);
		}
	}
}
