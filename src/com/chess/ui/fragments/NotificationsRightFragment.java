package com.chess.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.DailyChallengeItem;
import com.chess.backend.entity.api.DailyFinishedGameData;
import com.chess.backend.entity.api.FriendRequestResultItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.ui.adapters.*;
import com.chess.ui.fragments.daily.DailyInviteFragment;
import com.chess.ui.fragments.daily.GameDailyFinishedFragment;
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
public class NotificationsRightFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, ItemClickListenerFace, SlidingMenu.OnOpenedListener {

	private static final int FRIEND_REQUEST_SECTION = 0;
	private static final int CHALLENGES_SECTION = 1;
	private static final int NEW_CHATS_SECTION = 2;
	private static final int GAME_OVER_SECTION = 3;


	private CustomSectionedAdapter sectionedAdapter;
	private CommonAcceptDeclineCursorAdapter friendRequestsAdapter;
	private DailyChallengesGamesAdapter challengesGamesAdapter;
	private NewChatMessagesCursorAdapter chatMessagesAdapter;
	private DailyGamesOverCursorAdapter gamesOverAdapter;
	private DailyGamesUpdateListener dailyGamesUpdateListener;
	private List<Long> newChallengeIds;
	private int successToastMsgId;
	private DailyChallengeItem.Data selectedChallengeItem;
	private ChallengeUpdateListener challengeInviteUpdateListener;
	private FriendRequestUpdateListener friendRequestUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		friendRequestUpdateListener = new FriendRequestUpdateListener();
		challengeInviteUpdateListener = new ChallengeUpdateListener(ChallengeUpdateListener.INVITE);
		dailyGamesUpdateListener = new DailyGamesUpdateListener();
		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_dark);
		friendRequestsAdapter = new CommonAcceptDeclineCursorAdapter(new FriendAcceptDeclineFace(), null);
		challengesGamesAdapter = new DailyChallengesGamesAdapter(new ChallengeAcceptDeclineFace(), null);
		chatMessagesAdapter = new NewChatMessagesCursorAdapter(getActivity(), null);
		gamesOverAdapter = new DailyGamesOverCursorAdapter(getActivity(), null);

		sectionedAdapter.addSection(getString(R.string.friend_requests), friendRequestsAdapter);
		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.new_message), chatMessagesAdapter);
		sectionedAdapter.addSection(upCaseFirst(getString(R.string.game_over)), gamesOverAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_notifications_right_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView listView = (ListView) view.findViewById(R.id.listView);
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
			decreaseNotificationCnt();

			String likelyFriend = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

			DbDataManager.updateNewFriendRequestNotification(getContentResolver(), getUsername(), likelyFriend, true);

			getActivityFace().openFragment(ProfileTabsFragment.createInstance(likelyFriend));
			getActivityFace().toggleRightMenu();

		} else if (section == CHALLENGES_SECTION) {
			DailyChallengeItem.Data challengeItem = (DailyChallengeItem.Data) parent.getItemAtPosition(position);

			// decrease number of new notifications on badge
			for (Long challengeId : newChallengeIds) {
				if (challengeId == challengeItem.getGameId()) {
					decreaseNotificationCnt();

					// remove value from DB
					DbDataManager.deleteNewChallengeNotification(getContentResolver(), getUsername(), challengeId);
					break;
				}
			}

			getActivityFace().openFragment(DailyInviteFragment.createInstance(challengeItem));
			getActivityFace().toggleRightMenu();
		} else if (section == NEW_CHATS_SECTION) {

		} else if (section == GAME_OVER_SECTION) {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

			getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId()));
			getActivityFace().toggleRightMenu();
		}
	}

	private void decreaseNotificationCnt(){
		int notificationsCnt = getActivityFace().getValueByBadgeId(R.id.menu_notifications);
		notificationsCnt--;
		setBadgeValueForId(R.id.menu_notifications, notificationsCnt);
	}

	@Override
	public void onOpened() {

	}

	@Override
	public void onOpenedRight() {
		if (getActivity() == null) {
			return;
		}

		{ // get friend requests
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_FRIEND_REQUEST));
			cursor.moveToFirst();
			friendRequestsAdapter.changeCursor(cursor);
		}
		{ // get new challenge notifications
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_NEW_CHALLENGES));

			newChallengeIds = new ArrayList<Long>();
			if(cursor != null && cursor.moveToFirst()) {
				do {
					long challengeId = DbDataManager.getLong(cursor, DbScheme.V_ID);
					newChallengeIds.add(challengeId);

				}while (cursor.moveToNext());
			}
		}
		{ // get new chat notifications
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_NEW_CHAT_MESSAGES));
			cursor.moveToFirst();
			chatMessagesAdapter.changeCursor(cursor);
		}
		{ // get game over notifications
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_GAMES_OVER));
			cursor.moveToFirst();
			gamesOverAdapter.changeCursor(cursor);
		}

		// request data from server
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_CHALLENGES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		new RequestJsonTask<DailyChallengeItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyChallengeItem> {

		public DailyGamesUpdateListener() {
			super(DailyChallengeItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
//			showLoadingView(show);
		}

		@Override
		public void updateData(DailyChallengeItem returnedObj) {
			super.updateData(returnedObj);

			challengesGamesAdapter.setItemsList(returnedObj.getData());
			sectionedAdapter.notifyDataSetChanged();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				showToast(ServerErrorCodes.getUserFriendlyMessage(getActivity(), serverCode));
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred"); // TODO adjust properly
			}
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

	private void acceptFriendRequest(long requestId) {
		LoadItem loadItem = LoadHelper.acceptFriendRequest(getUserToken(), requestId);
		successToastMsgId = R.string.request_accepted;
		new RequestJsonTask<FriendRequestResultItem>(friendRequestUpdateListener).executeTask(loadItem);
	}

	private void declineFriendRequest(long requestId) {
		LoadItem loadItem = LoadHelper.declineFriendRequest(getUserToken(), requestId);
		successToastMsgId = R.string.request_declined;

		new RequestJsonTask<FriendRequestResultItem>(friendRequestUpdateListener).executeTask(loadItem);
	}

	private void acceptChallenge() {
		LoadItem loadItem = LoadHelper.acceptChallenge(getUserToken(),selectedChallengeItem.getGameId());
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
		}
	}

	private class FriendRequestUpdateListener extends ChessLoadUpdateListener<FriendRequestResultItem> {

		public FriendRequestUpdateListener() {
			super(FriendRequestResultItem.class);
		}

		@Override
		public void updateData(FriendRequestResultItem returnedObj) {
			showToast(successToastMsgId);

			// remove that item from challenges list adapter
			challengesGamesAdapter.remove(selectedChallengeItem);
		}
	}
}
