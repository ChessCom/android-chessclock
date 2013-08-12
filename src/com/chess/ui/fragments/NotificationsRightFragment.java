package com.chess.ui.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.gcm.FriendRequestItem;
import com.chess.backend.gcm.GameOverNotificationItem;
import com.chess.backend.gcm.NewChallengeNotificationItem;
import com.chess.backend.gcm.NewChatNotificationItem;
import com.chess.backend.statics.AppData;
import com.chess.db.DbDataManager1;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.ui.adapters.CommonAcceptDeclineCursorAdapter;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.DailyGamesCursorOverAdapter;
import com.chess.ui.adapters.NewChatMessagesCursorAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.08.13
 * Time: 20:21
 */
public class NotificationsRightFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {


	private CustomSectionedAdapter sectionedAdapter;
	private CommonAcceptDeclineCursorAdapter friendRequestsAdapter;
	private CommonAcceptDeclineCursorAdapter challengesGamesAdapter;
	private NewChatMessagesCursorAdapter chatMessagesAdapter;
	private DailyGamesCursorOverAdapter gamesOverAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_dark);

		friendRequestsAdapter = new CommonAcceptDeclineCursorAdapter(this, null);
		challengesGamesAdapter = new CommonAcceptDeclineCursorAdapter(this, null);
		chatMessagesAdapter = new NewChatMessagesCursorAdapter(getActivity(), null);
		gamesOverAdapter = new DailyGamesCursorOverAdapter(getActivity(), null);

		sectionedAdapter.addSection(getString(R.string.friend_requests), friendRequestsAdapter);
		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.new_message), chatMessagesAdapter);
		sectionedAdapter.addSection(upCaseFirst(getString(R.string.game_over)), gamesOverAdapter);

		showNewFriendRequest(null, getActivity());
		showNewChatMessage(null, getActivity());
		showGameOver(null, getActivity());
		showNewChallenge(null, getActivity());
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


		{ // get friend requests
			Cursor cursor = DbDataManager1.executeQuery(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_FRIEND_REQUEST));
			cursor.moveToFirst();
			friendRequestsAdapter.changeCursor(cursor);
		}
		{ // get new challenge notifications
			Cursor cursor = DbDataManager1.executeQuery(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_NEW_CHALLENGES));
			cursor.moveToFirst();

			challengesGamesAdapter.changeCursor(cursor);
		}
		{ // get new chat notifications
			Cursor cursor = DbDataManager1.executeQuery(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_NEW_CHAT_MESSAGES));
			cursor.moveToFirst();
			chatMessagesAdapter.changeCursor(cursor);
		}
		{ // get game over notifications
			Cursor cursor = DbDataManager1.executeQuery(getContentResolver(), DbHelper.getTableForUser(getUsername(),
					DbScheme.Tables.NOTIFICATION_GAMES_OVER));
			cursor.moveToFirst();
			gamesOverAdapter.changeCursor(cursor);
		}
	}

	private synchronized void showNewFriendRequest(Intent intent, Context context) {
		FriendRequestItem friendRequestItem = new FriendRequestItem();

		friendRequestItem.setMessage("test_message");
		friendRequestItem.setUsername("test_sender");
		friendRequestItem.setCreatedAt(0);
		friendRequestItem.setAvatar("https://secure.gravatar.com/avatar/d0d3692764da4b32f9160bfbd3e60ac5?s=140&d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png");

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();
		DbDataManager1.saveNewFriendRequest(contentResolver, friendRequestItem, username);
	}

	private synchronized void showNewChatMessage(Intent intent, Context context) {
		NewChatNotificationItem chatNotificationItem = new NewChatNotificationItem();

		chatNotificationItem.setMessage("test_message");
		chatNotificationItem.setUsername("test_user");
		chatNotificationItem.setGameId(0);
		chatNotificationItem.setCreatedAt(0);
		chatNotificationItem.setAvatar("https://secure.gravatar.com/avatar/d0d3692764da4b32f9160bfbd3e60ac5?s=140&d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png");

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();
		DbDataManager1.saveNewChatNotification(contentResolver, chatNotificationItem, username);
	}

	private synchronized void showGameOver(Intent intent, Context context) {
		GameOverNotificationItem gameOverNotificationItem = new GameOverNotificationItem();

		gameOverNotificationItem.setMessage("tst_message");
		gameOverNotificationItem.setGameId(0);
		gameOverNotificationItem.setAvatar("https://secure.gravatar.com/avatar/d0d3692764da4b32f9160bfbd3e60ac5?s=140&d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png");

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();
		DbDataManager1.saveGameOverNotification(contentResolver, gameOverNotificationItem, username);
	}

	private synchronized void showNewChallenge(Intent intent, Context context) {
		NewChallengeNotificationItem challengeNotificationItem = new NewChallengeNotificationItem();

		challengeNotificationItem.setUsername("test_user");
		challengeNotificationItem.setAvatar("https://secure.gravatar.com/avatar/d0d3692764da4b32f9160bfbd3e60ac5?s=140&d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-user-420.png");
		challengeNotificationItem.setChallengeId(0);

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();
		DbDataManager1.saveNewChallengeNotification(contentResolver, challengeNotificationItem, username);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}
}
