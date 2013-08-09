package com.chess.ui.fragments.messages;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ConversationItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveConversationsInboxTask;
import com.chess.ui.adapters.ConversationsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.07.13
 * Time: 20:56
 */
public class MessagesInboxFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private ListView listView;
	private ConversationsUpdateListener conversationsUpdateListener;
	private SaveConversationsListener saveConversationsListener;
	private ConversationsCursorAdapter conversationsCursorAdapter;
	private TextView emptyView;
	private ConversationCursorUpdateListener conversationCursorUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.messages);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(conversationsCursorAdapter);
		listView.setOnItemClickListener(this);

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

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_MESSAGES_INBOX);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<ConversationItem>(conversationsUpdateListener).executeTask(loadItem);
	}

	private void init() {
		conversationsUpdateListener = new ConversationsUpdateListener();
		saveConversationsListener = new SaveConversationsListener();
		conversationsCursorAdapter = new ConversationsCursorAdapter(getActivity(), null);
		conversationCursorUpdateListener = new ConversationCursorUpdateListener();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_edit:
				getActivityFace().openFragment(new NewMessageFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long conversationId = DbDataManager.getLong(cursor, DbConstants.V_ID);
		String otherUserName = DbDataManager.getString(cursor, DbConstants.V_OTHER_USER_USERNAME);
		getActivityFace().openFragment(MessagesConversationFragment.createInstance(conversationId, otherUserName));
	}

	private class ConversationsUpdateListener extends ChessUpdateListener<ConversationItem> {

		private ConversationsUpdateListener() {
			super(ConversationItem.class);
		}

		@Override
		public void updateData(ConversationItem returnedObj) {
			super.updateData(returnedObj);

			new SaveConversationsInboxTask(saveConversationsListener, returnedObj.getData(), getContentResolver()).executeTask();
		}
	}

	private class SaveConversationsListener extends ChessUpdateListener<ConversationItem.Data> {

		@Override
		public void updateData(ConversationItem.Data returnedObj) {
			super.updateData(returnedObj);

			new LoadDataFromDbTask(conversationCursorUpdateListener,
					DbHelper.getTableForUser(getUsername(), DbConstants.Tables.CONVERSATIONS_INBOX.ordinal()),
					getContentResolver()).executeTask();
		}
	}

	private class ConversationCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			if (returnedObj.moveToFirst()) {
				conversationsCursorAdapter.changeCursor(returnedObj);
			} else {
				showToast("Internal error");
			}
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
