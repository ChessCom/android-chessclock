package com.chess.ui.fragments.messages;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.ChipsAutoCompleteTextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.ConversationSingleItem;
import com.chess.statics.Symbol;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.08.13
 * Time: 6:05
 */
public class NewMessageFragment extends CommonLogicFragment implements TextView.OnEditorActionListener {

	private static final String SENDER_STR = "sender_str";
	private static final String CONTENT_STR = "content_str";
	public static final String FRIEND_NAME = "username";

	private MessageCreateListener messageCreateListener;
	private ChipsAutoCompleteTextView receiverNameEdt;
	private EditText messageBodyEdt;
	private String contentStr;
	private String receiverName;
	private String friendName;

	public NewMessageFragment() {
		Bundle bundle = new Bundle();
		bundle.putString(FRIEND_NAME, Symbol.EMPTY);
		setArguments(bundle);
	}

	public static NewMessageFragment createInstance(String username) {
		NewMessageFragment fragment = new NewMessageFragment();
		Bundle bundle = new Bundle();
		bundle.putString(FRIEND_NAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			friendName = getArguments().getString(FRIEND_NAME);
		} else {
			friendName = savedInstanceState.getString(FRIEND_NAME);
		}

		if (savedInstanceState != null) {
			receiverName = savedInstanceState.getString(SENDER_STR);
			contentStr = savedInstanceState.getString(CONTENT_STR);
		}

		if (!TextUtils.isEmpty(friendName)) {
			receiverName = friendName;
		}

		messageCreateListener = new MessageCreateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_conversation_message_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.new_message);

		receiverNameEdt = (ChipsAutoCompleteTextView) view.findViewById(R.id.receiverNameEdt);
		messageBodyEdt = (EditText) view.findViewById(R.id.messageBodyEdt);
		messageBodyEdt.setOnEditorActionListener(this);

		String[] friendsList = new String[]{};
		{ // load friends from DB
			final String[] arguments1 = new String[1];
			arguments1[0] = getAppData().getUsername();
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.FRIENDS.ordinal()],
					DbDataManager.PROJECTION_USERNAME, DbDataManager.SELECTION_USER, arguments1, null);

			if (cursor != null && cursor.moveToFirst()) { // TODO check if friends already loaded
				friendsList = new String[cursor.getCount()];
				int i = 0;
				do{
					friendsList[i++] = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
				} while (cursor.moveToNext());
			}
		}

		receiverNameEdt.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, friendsList));

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_accept, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!TextUtils.isEmpty(receiverName)) {
			receiverNameEdt.setText(receiverName);
		}

		if (!TextUtils.isEmpty(contentStr)) {
			messageBodyEdt.setText(contentStr);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		receiverName = getTextFromField(receiverNameEdt);
		contentStr = getTextFromField(messageBodyEdt);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(SENDER_STR, receiverName);
		outState.putString(CONTENT_STR, contentStr);
		outState.putString(FRIEND_NAME, friendName);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_accept:
				createMessage();
				break;
		}
		return super.onOptionsItemSelected(item);
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
		receiverName = getTextFromField(receiverNameEdt);
		if (TextUtils.isEmpty(receiverName)) {
			receiverNameEdt.requestFocus();
			receiverNameEdt.setError(getString(R.string.can_not_be_empty));
			return;
		}

		contentStr = getTextFromField(messageBodyEdt);
		if (TextUtils.isEmpty(contentStr)) {
			messageBodyEdt.requestFocus();
			messageBodyEdt.setError(getString(R.string.can_not_be_empty));
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_MESSAGES);

		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_USERNAME, receiverName);
		loadItem.addRequestParams(RestHelper.P_CONTENT, contentStr);

		new RequestJsonTask<ConversationSingleItem>(messageCreateListener).executeTask(loadItem);
	}

	private class MessageCreateListener extends ChessLoadUpdateListener<ConversationSingleItem> {

		private MessageCreateListener() {
			super(ConversationSingleItem.class);
		}

		@Override
		public void updateData(ConversationSingleItem returnedObj) {
			if(returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
				showToast(R.string.message_sent);
				getActivityFace().showPreviousFragment();
			} else {
				showToast(R.string.error);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.RESOURCE_NOT_FOUND) {

					showSinglePopupDialog(R.string.username_not_found);
				} else {
					super.errorHandle(resultCode);
				}
			} else {
				super.errorHandle(resultCode);
			}
		}
	}

}
