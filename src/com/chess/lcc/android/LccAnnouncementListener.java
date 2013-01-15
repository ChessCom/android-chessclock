package com.chess.lcc.android;

import android.util.Log;
import com.chess.live.client.AnnounceListener;
import com.chess.live.client.User;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 19.07.12
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */

public class LccAnnouncementListener implements AnnounceListener {

	private static final String TAG = "LCCLOG-ANNOUNCE";
	private final LccHolder lccHolder;

	public LccAnnouncementListener(LccHolder lccHolder) {
		this.lccHolder = lccHolder;
	}

	public void onAnnounceMessageReceived(User from, AnnounceType type, String codeMessage, String txt, Object object) {
		// todo: UPDATELCC. use new method params
		Log.d(TAG, "onAnnounceMessageReceived: author=" + (from != null ? from.getUsername() : null) + ", type=" + type
				+ ", codeMessage=" + codeMessage + ", txt=" + txt + ", object=" + object);


		if (type == AnnounceType.Shutdown && codeMessage == null && txt != null) {
			Integer minutes = Integer.parseInt(txt) / 60;
			String messageI18n = AppUtils.getI18nString(lccHolder.getContext(), "announcement.server_restarting", minutes.toString());
			Log.d(TAG, messageI18n);
			// UPDATELCC todo: handle with new UI. Show onAdminAnnounce check for background mode in onAdminAnnounce
			// the same way as we do for ConnectionFailure
			//lccHolder.getLiveChessClientEventListener().onAdminAnnounce(messageI18n);
		}

		/*Some notes:
		On Server Shutdown (or on Cancelling the Server Shutdown) the client will receive 2 (TWO!) Announce messages,
		i.e. the following method will be invoked twice:

		1) In the first case the server sends non-null codeMessage="announcement.server_restarting" or
		"announcement.server_restart_cancelled", type=AnnounceType.System, txt=null, object=null.

		2) And the second message: type=AnnounceType.Shutdown, codeMessage=null,
		txt="NumberOfSecondsTillShutdownInTextualForm" (e.g. txt="60"), object=null.

		If it is cancellation of the shutdown, then txt="0" for the second message.*/
	}
}
