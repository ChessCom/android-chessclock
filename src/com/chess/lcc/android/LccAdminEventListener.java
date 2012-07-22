package com.chess.lcc.android;

import android.content.Context;
import android.util.Log;
import com.chess.R;
import com.chess.live.client.AdminEventListener;
import com.chess.live.client.LiveChessClient;
import com.chess.live.client.User;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 19.07.12
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */
public class LccAdminEventListener implements AdminEventListener {

	private static final String TAG = "LccAdminEventListener";
	private final LccHolder lccHolder;

	public LccAdminEventListener(LccHolder lccHolder) {
		this.lccHolder = lccHolder;
	}

	public void onAdminMessageReceived(User sender, User target,
									   LiveChessClient.AdminMessageType messageType, String reason, String message) {
		Log.d(TAG, "onAdminMessageReceived: sender=" + (sender != null ? sender.getUsername() : null) + ", "
				+ "targetedUser=" + target.getUsername() + ", messageType=" + messageType + ", reason=" + reason + ", "
				+ "message=" + message);

	}

	public void onServerShutdownAlertReceived(User sender, String message) {
		Log.d(TAG, "onServerShutdownAlertReceived: sender=" + (sender != null ? sender.getUsername() : null)
				+ ", message=" + message);
		final Context context = lccHolder.getContext();

		if (message != null) {
			String messageI18n = AppUtils.getI18nString(context, message, R.string.adminShutdownCancelledOriginal, R.string.adminShutdownCancelled);
			if (messageI18n == null) {
				messageI18n = AppUtils.getI18nString(context,
						R.string.adminShutdownScheduledPattern, message, R.string.adminShutdownScheduledOriginal, R.string.adminShutdownScheduled);
			}
			lccHolder.getLiveChessClientEventListener().onAdminAnnounce(messageI18n == null ? message : messageI18n);
		}
	}
}
