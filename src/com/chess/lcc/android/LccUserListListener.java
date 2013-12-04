package com.chess.lcc.android;

import com.chess.live.client.PaginationInfo;
import com.chess.live.client.SubscriptionId;
import com.chess.live.client.User;
import com.chess.live.client.UserListListener;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 04.12.13
 * Time: 0:14
 */
public class LccUserListListener implements UserListListener {

	// todo: check LCC logic for Online/Offline statuses

	private static final String TAG = "LCCLOG-LccUserListListener";
	private final LccHelper lccHelper;

	public LccUserListListener(LccHelper lccHelper) {
		this.lccHelper = lccHelper;
	}

	@Override
	public void onUserListReceived(SubscriptionId subscriptionId, Collection<User> users, Integer total) {
		/*if (users != null) {
			LogMe.dl(TAG, "User list received: size = " + users.size() + ", total = " + total);
			for (User user : users) {
				LogMe.dl(TAG, " user: " + user);
			}
		} else {
			LogMe.dl(TAG, "User list received, but it is null");
		}*/
	}

	@Override
	public void onUserItemAdded(SubscriptionId subscriptionId, User user) {
		/*LogMe.dl(TAG, "User item added: " + "channel=" + ((SubscriptionIdImpl) subscriptionId).getFullChannelId() +
				", addedUser=" + user.getUsername());*/

	}

	@Override
	public void onUserItemRemoved(SubscriptionId subscriptionId, String username) {
		/*LogMe.dl(TAG, "User item removed: " + "channel=" + ((SubscriptionIdImpl) subscriptionId).getFullChannelId());*/
	}

	@Override
	public void onPaginationInfoReceived(SubscriptionId subscriptionId, PaginationInfo paginationInfo) {
		/*LogMe.dl(TAG, "PaginationInfo received: listId=" + ((SubscriptionIdImpl) subscriptionId).getFullChannelId() +
				", pageCount=" + paginationInfo.getPageCount() + ", itemsCount=" + paginationInfo.getItemsCount());*/
	}
}
