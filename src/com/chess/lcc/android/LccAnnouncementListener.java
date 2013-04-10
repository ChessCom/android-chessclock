package com.chess.lcc.android;

import android.util.Log;
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
	private final LccHelper lccHelper;

	public LccAnnouncementListener(LccHelper lccHelper) {
		this.lccHelper = lccHelper;
	}

	public void onAnnounceMessageReceived(ChatMessage chatMessage) {
		Log.d(TAG, "onAnnounceMessageReceived: " + chatMessage);

		final String message = chatMessage.getMessage();

		if (message != null) {
			String messageI18n = AppUtils.getI18nString(lccHelper.getContext(), message);
			lccHelper.getLiveChessClientEventListener().onAdminAnnounce(messageI18n == null ? message : messageI18n);
		}

	}
}
