package com.chess.ui.fragments.messages;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ConversationSingleItem;
import com.chess.backend.entity.new_api.MessagesItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveMessagesForConversationTask;
import com.chess.ui.adapters.MessagesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.08.13
 * Time: 20:44
 */
public class MessagesConversationFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, TextView.OnEditorActionListener {

	private static final long KEYBOARD_DELAY = 50;
	private static final String CONVERSATION_ID = "conversation_id";
	private static final String OTHER_USERNAME = "otherUserName";

	private ListView listView;
	private boolean need2update = true;
	private MessagesUpdateListener messagesUpdateListener;
	private SaveMessagesListener saveMessagesListener;
	private MessagesCursorAdapter messagesCursorAdapter;
	private long conversationId;
	private View replyView;
	private EditText messageBodyEdt;
	private String otherUsername;
	private ReplyCreateListener replyCreateListener;
	private TextView emptyView;
	private MessagesCursorUpdateListener messageCursorUpdateListener;
	private int paddingSide;

	public MessagesConversationFragment() {}

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_conversations_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.message);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(messagesCursorAdapter);
		listView.setOnItemClickListener(this);

		replyView = view.findViewById(R.id.replyView);
		messageBodyEdt = (EditText) view.findViewById(R.id.messageBodyEdt);
		messageBodyEdt.setOnEditorActionListener(this);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		// adjust actionBar icons
		getActivityFace().showActionMenu(R.id.menu_edit, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (need2update) {
			updateUiData();
		} else {
			listView.setAdapter(messagesCursorAdapter);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(CONVERSATION_ID, conversationId);
		outState.putString(OTHER_USERNAME, otherUsername);
	}

	private void updateUiData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_MESSAGE_CONVERSATION_BY_ID(conversationId));
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<MessagesItem>(messagesUpdateListener).executeTask(loadItem);
	}

	private void init() {
		paddingSide = getResources().getDimensionPixelSize(R.dimen.default_scr_side_padding);

		replyCreateListener = new ReplyCreateListener();

		messagesUpdateListener = new MessagesUpdateListener();
		saveMessagesListener = new SaveMessagesListener();
		messagesCursorAdapter = new MessagesCursorAdapter(getActivity(), null);
		messageCursorUpdateListener = new MessagesCursorUpdateListener();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_edit:
				showReplyView();
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

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				messageBodyEdt.requestFocus();
				showKeyBoard(messageBodyEdt);
				showKeyBoardImplicit(messageBodyEdt);
			}
		}, KEYBOARD_DELAY);
	}


	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.FLAG_EDITOR_ACTION
				|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			if (!AppUtils.isNetworkAvailable(getActivity())) { // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			} else {
				createMessage();
			}
		}
		return false;
	}

	private void createMessage() {

		String body = getTextFromField(messageBodyEdt);
		if (TextUtils.isEmpty(body)) {
			messageBodyEdt.requestFocus();
			messageBodyEdt.setError(getString(R.string.can_not_be_empty));
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_MESSAGES);

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

			updateUiData(); // TODO improve performance

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					hideKeyBoard(messageBodyEdt);
					hideKeyBoard();
				}
			}, KEYBOARD_DELAY);

			replyView.setVisibility(View.GONE);
			messageBodyEdt.setText(StaticData.SYMBOL_EMPTY);
		}
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

			new LoadDataFromDbTask(messageCursorUpdateListener,
					DbHelper.getConversationMessagesById(conversationId, getUsername()),
					getContentResolver()).executeTask();
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

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_data);
			}
			showEmptyView(true);
		}
	}

	private void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}
}
