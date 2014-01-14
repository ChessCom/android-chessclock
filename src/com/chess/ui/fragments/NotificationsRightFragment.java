package com.chess.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.FriendRequestResultItem;
import com.chess.backend.entity.api.daily_games.DailyChallengeItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.*;
import com.chess.ui.fragments.daily.DailyChatFragment;
import com.chess.ui.fragments.daily.DailyInviteFragment;
import com.chess.ui.fragments.daily.GameDailyFinishedFragment;
import com.chess.ui.fragments.messages.MessagesFragmentTablet;
import com.chess.ui.fragments.messages.MessagesInboxFragment;
import com.chess.ui.fragments.profiles.ProfileTabsFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.08.13
 * Time: 20:21
 */
public class NotificationsRightFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		ItemClickListenerFace, SlidingMenu.OnOpenedListener {

	private static final int FRIEND_REQUEST_SECTION = 0;
	private static final int CHALLENGES_SECTION = 1;
	private static final int NEW_MESSAGES_SECTION = 2;
	private static final int NEW_CHATS_SECTION = 3;
	private static final int GAME_OVER_SECTION = 4;

	private CustomSectionedAdapter sectionedAdapter;
	private CommonAcceptDeclineCursorAdapter friendRequestsAdapter;
	private DailyChallengesGamesAdapter challengesGamesAdapter;
	private NewChatMessagesCursorAdapter messagesAdapter;
	private NewChatMessagesCursorAdapter chatMessagesAdapter;
	private DailyGamesOverCursorAdapter gamesOverAdapter;
	private NewChallengesUpdateListener newChallengesUpdateListener;
	private List<Long> newChallengeIds;
	private int successToastMsgId;
	private DailyChallengeItem.Data selectedChallengeItem;
	private ChallengeUpdateListener challengeInviteUpdateListener;
	private TextView emptyView;
	private ListView listView;
	private boolean emptyData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		emptyData = true;

		challengeInviteUpdateListener = new ChallengeUpdateListener(ChallengeUpdateListener.INVITE);
		newChallengesUpdateListener = new NewChallengesUpdateListener();
		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_dark);
		friendRequestsAdapter = new CommonAcceptDeclineCursorAdapter(new FriendAcceptDeclineFace(), null, getImageFetcher());
		challengesGamesAdapter = new DailyChallengesGamesAdapter(new ChallengeAcceptDeclineFace(), null, getImageFetcher(), this);
		messagesAdapter = new NewChatMessagesCursorAdapter(new NewMessageClearFace(), null, getImageFetcher());
		chatMessagesAdapter = new NewChatMessagesCursorAdapter(new NewChatClearFace(), null, getImageFetcher());
		gamesOverAdapter = new DailyGamesOverCursorAdapter(new GameOverClearFace(), null, getImageFetcher());

		sectionedAdapter.addSection(getString(R.string.friend_requests), friendRequestsAdapter);
		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.new_message), messagesAdapter);
		sectionedAdapter.addSection(getString(R.string.new_chat_message), chatMessagesAdapter);
		sectionedAdapter.addSection(upCaseFirst(getString(R.string.game_over)), gamesOverAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_notifications_right_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		emptyView = (TextView) view.findViewById(R.id.emptyView);
		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setAdapter(sectionedAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivityFace().addOnOpenMenuListener(this);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int section = sectionedAdapter.getCurrentSection(position);

		if (section == FRIEND_REQUEST_SECTION) {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);

			String likelyFriend = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

			DbDataManager.updateNewFriendRequestNotification(getContentResolver(), getUsername(), likelyFriend, true);

			updateNotificationBadges();

			getActivityFace().openFragment(ProfileTabsFragment.createInstance(likelyFriend));
			getActivityFace().toggleRightMenu();

		} else if (section == CHALLENGES_SECTION) {
			DailyChallengeItem.Data challengeItem = (DailyChallengeItem.Data) parent.getItemAtPosition(position);

			// decrease number of new notifications on badge
			for (Long challengeId : newChallengeIds) {
				if (challengeId == challengeItem.getGameId()) {

					// remove value from DB
					DbDataManager.deleteNewChallengeNotification(getContentResolver(), getUsername(), challengeId);

					updateNotificationBadges();
					break;
				}
			}

			getActivityFace().openFragment(DailyInviteFragment.createInstance(challengeItem));
			getActivityFace().toggleRightMenu();
		} else if (section == NEW_MESSAGES_SECTION) {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			String opponentName = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

			DbDataManager.deleteNewMessageNotification(getContentResolver(), getUsername(), opponentName);

			updateNotificationBadges();
			BasePopupsFragment fragmentByTag;
			if (!isTablet) {
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(MessagesInboxFragment.class.getSimpleName());
			} else {
				fragmentByTag = (BasePopupsFragment) findFragmentByTag(MessagesFragmentTablet.class.getSimpleName());
			}
			if (fragmentByTag == null) {
				if (!isTablet) {
					fragmentByTag = new MessagesInboxFragment();
				} else {
					fragmentByTag = new MessagesFragmentTablet();
				}
			}

			getActivityFace().openFragment(fragmentByTag);
			getActivityFace().toggleRightMenu();
		} else if (section == NEW_CHATS_SECTION) {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			String opponentName = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
			long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

			DbDataManager.deleteNewChatMessageNotification(getContentResolver(), getUsername(), opponentName);

			updateNotificationBadges();

			getActivityFace().openFragment(DailyChatFragment.createInstance(gameId, opponentName));
			getActivityFace().toggleRightMenu();
		} else if (section == GAME_OVER_SECTION) {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);
			DbDataManager.deleteGameOverNotification(getContentResolver(), getUsername(), gameId);

			updateNotificationBadges();

			getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(gameId));
			getActivityFace().toggleRightMenu();
		}
	}

	@Override
	public void onOpened() {

	}

	@Override
	public void onOpenedRight() {
		if (getActivity() == null) {
			return;
		}

		loadNotifications();

		// request data from server
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_CHALLENGES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		new RequestJsonTask<DailyChallengeItem>(newChallengesUpdateListener).executeTask(loadItem);
	}

	private void loadNotifications() {
		emptyData = true;
		{ // get friend requests
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_FRIEND_REQUEST));
			if (cursor.moveToFirst()) {
				friendRequestsAdapter.changeCursor(cursor);
				emptyData = false;
			}
		}
		{ // get new challenge notifications
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_NEW_CHALLENGES));

			newChallengeIds = new ArrayList<Long>();
			if (cursor != null && cursor.moveToFirst()) {
				emptyData = false;

				do {
					long challengeId = DbDataManager.getLong(cursor, DbScheme.V_ID);
					newChallengeIds.add(challengeId);

				} while (cursor.moveToNext());
			}
		}
		{ // get new messages notifications
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_NEW_MESSAGES));
			if (cursor.moveToFirst()) {
				emptyData = false;
				messagesAdapter.changeCursor(cursor);
			}
		}
		{ // get new chat notifications
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_NEW_CHAT_MESSAGES));
			if (cursor.moveToFirst()) {
				emptyData = false;
				chatMessagesAdapter.changeCursor(cursor);
			}
		}
		{ // get game over notifications
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_GAMES_OVER));
			if (cursor.moveToFirst()) {
				emptyData = false;
				gamesOverAdapter.changeCursor(cursor);
			}
		}

		if (emptyData) {
			emptyView.setText(R.string.no_alerts);
			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private class NewChallengesUpdateListener extends ChessUpdateListener<DailyChallengeItem> {

		public NewChallengesUpdateListener() {
			super(DailyChallengeItem.class);
		}

		@Override
		public void updateData(DailyChallengeItem returnedObj) {
			super.updateData(returnedObj);

			if (returnedObj.getData().size() == 0 && emptyData) {
				emptyView.setText(R.string.no_alerts);
				emptyView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			} else {
				challengesGamesAdapter.setItemsList(returnedObj.getData());
				sectionedAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode != ServerErrorCodes.INVALID_LOGIN_TOKEN_SUPPLIED) {
					showToast(ServerErrorCodes.getUserFriendlyMessage(getActivity(), serverCode));
					return;
				}
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred"); // TODO adjust properly
			}
			super.errorHandle(resultCode);
		}
	}

	private class FriendAcceptDeclineFace implements ItemClickListenerFace {

		@Override
		public Context getMeContext() {
			return getActivity();
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.acceptBtn) {
				Integer position = (Integer) view.getTag(R.id.list_item_id);
				Cursor cursor = (Cursor) friendRequestsAdapter.getItem(position);
				acceptFriendRequest(DbDataManager.getLong(cursor, DbScheme.V_ID));
			} else if (view.getId() == R.id.cancelBtn) {
				Integer position = (Integer) view.getTag(R.id.list_item_id);
				Cursor cursor = (Cursor) friendRequestsAdapter.getItem(position);
				declineFriendRequest(DbDataManager.getLong(cursor, DbScheme.V_ID));
			}
		}
	}

	private class ChallengeAcceptDeclineFace implements ItemClickListenerFace {

		@Override
		public Context getMeContext() {
			return getActivity();
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.acceptBtn) {
				Integer position = (Integer) view.getTag(R.id.list_item_id);
				selectedChallengeItem = challengesGamesAdapter.getItem(position);
				acceptChallenge();
			} else if (view.getId() == R.id.cancelBtn) {
				Integer position = (Integer) view.getTag(R.id.list_item_id);
				selectedChallengeItem = challengesGamesAdapter.getItem(position);
				declineChallenge();
			}
		}
	}

	private class GameOverClearFace implements ItemClickListenerFace {

		@Override
		public Context getMeContext() {
			return getActivity();
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.clearBtn) {
				Cursor cursor = (Cursor) view.getTag(R.id.list_item_id);
				long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

				DbDataManager.deleteGameOverNotification(getContentResolver(), getUsername(), gameId);

				updateNotificationBadges();

				loadNotifications();
			}
		}
	}

	private class NewChatClearFace implements ItemClickListenerFace {

		@Override
		public Context getMeContext() {
			return getActivity();
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.clearBtn) {
				Cursor cursor = (Cursor) view.getTag(R.id.list_item_id);
				String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

				DbDataManager.deleteNewChatMessageNotification(getContentResolver(), getUsername(), username);

				updateNotificationBadges();

				loadNotifications();
			}
		}
	}

	private class NewMessageClearFace implements ItemClickListenerFace {

		@Override
		public Context getMeContext() {
			return getActivity();
		}

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.clearBtn) {
				Cursor cursor = (Cursor) view.getTag(R.id.list_item_id);
				String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

				DbDataManager.deleteNewMessageNotification(getContentResolver(), getUsername(), username);

				updateNotificationBadges();

				loadNotifications();
			}
		}
	}

	private void acceptFriendRequest(long requestId) {
		LoadItem loadItem = LoadHelper.acceptFriendRequest(getUserToken(), requestId);
		successToastMsgId = R.string.request_accepted;
		new RequestJsonTask<FriendRequestResultItem>(new FriendRequestUpdateListener(requestId)).executeTask(loadItem);
	}

	private void declineFriendRequest(long requestId) {
		LoadItem loadItem = LoadHelper.declineFriendRequest(getUserToken(), requestId);
		successToastMsgId = R.string.request_declined;

		new RequestJsonTask<FriendRequestResultItem>(new FriendRequestUpdateListener(requestId)).executeTask(loadItem);
	}

	private void acceptChallenge() {
		LoadItem loadItem = LoadHelper.acceptChallenge(getUserToken(), selectedChallengeItem.getGameId());
		successToastMsgId = R.string.challenge_accepted;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	private void declineChallenge() {
		LoadItem loadItem = LoadHelper.declineChallenge(getUserToken(), selectedChallengeItem.getGameId());
		successToastMsgId = R.string.challenge_declined;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	private class ChallengeUpdateListener extends ChessLoadUpdateListener<BaseResponseItem> {
		public static final int INVITE = 3;
		public static final int DRAW = 4;

		private int itemCode;

		public ChallengeUpdateListener(int itemCode) {
			super(BaseResponseItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {

			// remove that item from challenges list adapter
			challengesGamesAdapter.remove(selectedChallengeItem);

			// clear notification badge
			DbDataManager.deleteNewChallengeNotification(getContentResolver(), getUsername(),
					selectedChallengeItem.getGameId());
			updateNotificationBadges();

			loadNotifications();

			getActivity().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
		}
	}

	private class FriendRequestUpdateListener extends ChessLoadUpdateListener<FriendRequestResultItem> {

		private long requestId;

		public FriendRequestUpdateListener(long requestId) {
			super(FriendRequestResultItem.class);
			this.requestId = requestId;
		}

		@Override
		public void updateData(FriendRequestResultItem returnedObj) {
			showToast(successToastMsgId);

			DbDataManager.deleteNewFriendRequestNotification(getContentResolver(), getUsername(),
					requestId);

			updateNotificationBadges();

			loadNotifications();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);

			DbDataManager.deleteNewFriendRequestNotification(getContentResolver(), getUsername(),
					requestId);

			updateNotificationBadges();

			loadNotifications();
		}
	}
}
