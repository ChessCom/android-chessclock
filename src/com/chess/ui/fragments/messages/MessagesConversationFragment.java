package com.chess.ui.fragments.messages;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ConversationSingleItem;
import com.chess.backend.entity.api.MessagesItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveMessagesForConversationTask;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.MessagesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.08.13
 * Time: 20:44
 */
public class MessagesConversationFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final long KEYBOARD_DELAY = 50;
	private static final String CONVERSATION_ID = "conversation_id";
	private static final String OTHER_USERNAME = "otherUserName";

	private ListView listView;
	private MessagesUpdateListener messagesUpdateListener;
	private SaveMessagesListener saveMessagesListener;
	private MessagesCursorAdapter messagesCursorAdapter;
	private long conversationId;
	private View replyView;
	private EditText newPostEdt;
	private String otherUsername;
	private ReplyCreateListener replyCreateListener;
	private MessagesCursorUpdateListener messageCursorUpdateListener;
	private int paddingSide;
	private boolean inEditMode;

	public MessagesConversationFragment() {
	}

	public static MessagesConversationFragment createInstance(long conversationId, String otherUserName) {
		MessagesConversationFragment fragment = new MessagesConversationFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(CONVERSATION_ID, conversationId);
		bundle.putString(OTHER_USERNAME, otherUserName);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			conversationId = getArguments().getLong(CONVERSATION_ID);
			otherUsername = getArguments().getString(OTHER_USERNAME);
		} else {
			conversationId = savedInstanceState.getLong(CONVERSATION_ID);
			otherUsername = savedInstanceState.getString(OTHER_USERNAME);
		}

		init();
		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.conversations_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.messages);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(messagesCursorAdapter);
		listView.setOnItemClickListener(this);

		replyView = view.findViewById(R.id.replyView);
		newPostEdt = (EditText) view.findViewById(R.id.messageBodyEdt);

		// adjust actionBar icons
		getActivityFace().showActionMenu(R.id.menu_edit, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			if (isNetworkAvailable()) {
				updateData();
			}

			loadFromDb();
		} else {
			listView.setAdapter(messagesCursorAdapter);
		}

		DbDataManager.deleteNewMessageNotification(getContentResolver(), getUsername(), otherUsername);
		updateNotificationBadges();
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(messageCursorUpdateListener,
				DbHelper.getConversationMessagesById(conversationId, getUsername()),
				getContentResolver()).executeTask();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(CONVERSATION_ID, conversationId);
		outState.putString(OTHER_USERNAME, otherUsername);
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);

		if (isNetworkAvailable()) {
			updateData();
		}
	}

	private void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_MESSAGE_CONVERSATION_BY_ID(conversationId));
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<MessagesItem>(messagesUpdateListener).executeTask(loadItem);
	}

	private void init() {
		paddingSide = getResources().getDimensionPixelSize(R.dimen.default_scr_side_padding);

		replyCreateListener = new ReplyCreateListener();

		messagesUpdateListener = new MessagesUpdateListener();
		saveMessagesListener = new SaveMessagesListener();
		messagesCursorAdapter = new MessagesCursorAdapter(getActivity(), null, getImageFetcher());
		messageCursorUpdateListener = new MessagesCursorUpdateListener();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_edit:
				showReplyView();
				return true;
			case R.id.menu_cancel:
				showEditView(false);

				return true;
			case R.id.menu_accept:
				createMessage();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		showReplyView();
	}

	private void showReplyView() {
		replyView.setVisibility(View.VISIBLE);
		replyView.setBackgroundResource(R.color.header_light);
		replyView.setPadding(paddingSide, paddingSide, paddingSide, paddingSide);

		inEditMode = true;
		showEditView(true);
	}

	private void createMessage() {
		String body = getTextFromField(newPostEdt);
		if (TextUtils.isEmpty(body)) {
			newPostEdt.requestFocus();
			newPostEdt.setError(getString(R.string.can_not_be_empty));
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_MESSAGES);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_USERNAME, otherUsername);
		loadItem.addRequestParams(RestHelper.P_CONTENT, body);

		new RequestJsonTask<ConversationSingleItem>(replyCreateListener).executeTask(loadItem);
	}

	private class ReplyCreateListener extends ChessLoadUpdateListener<ConversationSingleItem> {

		private ReplyCreateListener() {
			super(ConversationSingleItem.class);
		}

		@Override
		public void updateData(ConversationSingleItem returnedObj) {
			MessagesConversationFragment.this.updateData(); // TODO improve performance

			showEditView(false);
		}
	}

	private void showEditView(boolean show) {
		if (show) {
			replyView.setVisibility(View.VISIBLE);
			replyView.setBackgroundResource(R.color.header_light);
			replyView.setPadding(paddingSide, paddingSide, paddingSide, paddingSide);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					newPostEdt.requestFocus();
					showKeyBoard(newPostEdt);
					showKeyBoardImplicit(newPostEdt);

					showEditMode(true);
				}
			}, KEYBOARD_DELAY);
		} else {

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					hideKeyBoard(newPostEdt);
					hideKeyBoard();

					replyView.setVisibility(View.GONE);
					newPostEdt.setText(Symbol.EMPTY);
				}
			}, KEYBOARD_DELAY);

			showEditMode(false);
			inEditMode = false;
		}
	}

	private void showEditMode(boolean show) {
		getActivityFace().showActionMenu(R.id.menu_share, !show);
		getActivityFace().showActionMenu(R.id.menu_edit, !show);
		getActivityFace().showActionMenu(R.id.menu_cancel, show);
		getActivityFace().showActionMenu(R.id.menu_accept, show);

		getActivityFace().updateActionBarIcons();
	}

	private class MessagesUpdateListener extends ChessLoadUpdateListener<MessagesItem> {

		private MessagesUpdateListener() {
			super(MessagesItem.class);
		}

		@Override
		public void updateData(MessagesItem returnedObj) {
			super.updateData(returnedObj);

			new SaveMessagesForConversationTask(saveMessagesListener, returnedObj.getData(),
					getContentResolver(), conversationId).executeTask();
		}
	}

	private class SaveMessagesListener extends ChessLoadUpdateListener<MessagesItem.Data> {

		@Override
		public void updateData(MessagesItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFromDb();
		}
	}

	private class MessagesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			if (returnedObj.moveToFirst()) {
				messagesCursorAdapter.changeCursor(returnedObj);
			} else {
				showToast("Internal error");
			}

			need2update = false;
		}
	}

}
