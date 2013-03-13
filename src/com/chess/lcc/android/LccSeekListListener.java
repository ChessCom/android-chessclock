/*
 * LccSeekListListener.java
 */

package com.chess.lcc.android;

import android.util.Log;
import com.chess.backend.statics.AppConstants;
import com.chess.live.client.Challenge;
import com.chess.live.client.PaginationInfo;
import com.chess.live.client.SeekListListener;
import com.chess.live.client.SubscriptionId;

import java.util.Collection;

public class LccSeekListListener implements SeekListListener {
	private static final String TAG = "LCCLOG-SEEK";
	private final LccHelper lccHelper;

	public LccSeekListListener(LccHelper lccHelper) {
		this.lccHelper = lccHelper;
	}

	public void onSeekListReceived(SubscriptionId id, Collection<Challenge> challenges, Integer total) {
		if (challenges != null) {
			Log.i(TAG, "SEEK LIST LISTENER: Public Seek list received: size = " + challenges.size() + ", total = " + total);
			/*LccUser.LOG.debug(
					"SEEK LIST LISTENER: Public Seek received: subscriptionId = " + ((SubscriptionIdImpl) id).toDebugString() +
					", size = " + challenges.size() + ", total = " + total);*/
			lccHelper.clearSeeks();
			for (Challenge challenge : challenges) {
				addSeek(challenge);
			}
			lccHelper.setSeekListSubscriptionId(id);
		} else {
			Log.i(TAG, "SEEK LIST LISTENER: Public Seek list received, but it is null");
		}
	}

	@Override
	public void onSeekItemAdded(SubscriptionId id, Challenge challenge) {
		//Log.i("Seek item added: user: " + lccHelper.getUser().getUsername() + ", challenge: " + challenge);
		addSeek(challenge);
	}

	@Override
	public void onSeekItemRemoved(SubscriptionId id, Long seekId) {
		if (lccHelper.isSeekContains(seekId)) {
			Log.i(TAG, "Seek item removed: user: " + lccHelper.getUser().getUsername() + AppConstants.CHALLENGE + seekId);
			/*Seek seek = user.getConnection().getJinSeek(seekId);
				  if(seek == null)
				  {
					return;
				  }
				  user.getListenerManager().fireSeekEvent(new SeekEvent(user.getConnection(), null, SeekEvent.SEEK_REMOVED, seek));*/
			lccHelper.removeSeek(seekId);
		}
	}

	@Override
	public void onPaginationInfoReceived(SubscriptionId id, PaginationInfo info) {
		/*Log.i(
			  "PAGINATION INFO LISTENER: Pagination info received: pageCount = " + info.getPageCount() + ", itemsCount=" +
			  info.getItemsCount());*/
	}

	private void addSeek(Challenge challenge) {
		if (challenge.getFrom().isComputer()) {
			//Log.i("Seek received: ignore computer player");
			return;
		}
		if (lccHelper.isUserBlocked(challenge.getFrom().getUsername())) {
			Log.i(TAG, "Add seek: blocked user");
			return;
		}
		if (lccHelper.isSeekContains(challenge.getId())) {
			Log.i(TAG, "Add seek: ignore seek, already exists");
			return;
		}
		/*if (challenge.getFrom().getUsername().equals(lccHelper.getUser().getUsername()))
			{
			  Log.i("Seek item added: user: " + lccHelper.getUser().getUsername() + ", challenge: " + challenge);
			  lccHelper.putSeek(challenge);
			}*/
	}

}
