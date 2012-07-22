package com.chess.lcc.android;

import android.content.Context;
import android.util.Log;
import com.chess.R;
import com.chess.live.client.AnnounceListener;
import com.chess.live.client.ChatMessage;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 19.07.12
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */

public class LccAnnouncementListener implements AnnounceListener {

	private static final String TAG = "LccAnnouncementListener";
	private final LccHolder lccHolder;

	public LccAnnouncementListener(LccHolder lccHolder) {
		this.lccHolder = lccHolder;
	}

	public void onAnnounceMessageReceived(ChatMessage chatMessage) {
		Log.d(TAG, "onAnnounceMessageReceived: " + chatMessage);

		final String message = chatMessage.getMessage();
		final Context context = lccHolder.getContext();

		if (message != null) {
			String messageI18n = AppUtils.getI18nString(context, message, R.string.announceRestartCancelledOriginal, R.string.announceRestartCancelled);
			if (messageI18n == null) {
				messageI18n = AppUtils.getI18nString(context,
						R.string.announceRestartingPattern, message, R.string.announceRestartingOriginal, R.string.announceRestarting);
			}
			lccHolder.getLiveChessClientEventListener().onAdminAnnounce(messageI18n == null ? message : messageI18n);
		}

	}
}
