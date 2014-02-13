package com.chess.ui.fragments.messages;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ConversationItem;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveConversationsInboxTask;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.ConversationsCursorAdapter;
import com.chess.ui.adapters.MessagesInboxPaginationAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.07.13
 * Time: 20:56
 */
public class MessagesInboxFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private ListView listView;
	private SaveConversationsListener saveConversationsListener;
	private ConversationCursorUpdateListener conversationCursorUpdateListener;
	private ConversationsCursorAdapter conversationsAdapter;
	private TextView emptyView;
	private MessagesInboxPaginationAdapter paginationAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.messages);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(paginationAdapter);
		listView.setOnItemClickListener(this);

		emptyView = (TextView) view.findViewById(R.id.emptyView);

		// adjust actionBar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_edit, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (isNetworkAvailable()) {
			updateData();
		} else {
			loadFromDb();
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		if (isNetworkAvailable()) {
			updateData();
		}
	}

	protected void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_MESSAGES_INBOX);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		paginationAdapter.updateLoadItem(loadItem);
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(conversationCursorUpdateListener, DbHelper.getInboxMessages(getUsername()),
				getContentResolver()).executeTask();
	}

	private void init() {
		saveConversationsListener = new SaveConversationsListener();
		conversationsAdapter = new ConversationsCursorAdapter(getActivity(), null, getImageFetcher());
		conversationsAdapter.setFilterQueryProvider(new QueryFilterProvider());
		conversationCursorUpdateListener = new ConversationCursorUpdateListener();
		paginationAdapter = new MessagesInboxPaginationAdapter(getActivity(), conversationsAdapter,
				new ConversationsUpdateListener(), null);
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
	public void onSearchAutoCompleteQuery(String query) {
		if (!inSearch && !need2update) { // search only if we received data
			inSearch = true;
			if (conversationsAdapter == null) {
				return;
			}
			Cursor cursor = conversationsAdapter.runQueryOnBackgroundThread(query);
			if (cursor != null) {
				conversationsAdapter.changeCursor(cursor);
			}
			inSearch = false;
		}
	}

	private class QueryFilterProvider implements FilterQueryProvider {

		@Override
		public Cursor runQuery(CharSequence constraint) {
			if (getActivity() == null) { // if fragment was closed
				return null;
			}

			String query = (String) constraint;
			String[] selectionArgs = new String[] {DbScheme.V_OTHER_USER_USERNAME, DbScheme.V_LAST_MESSAGE_CONTENT};
			String selection = DbDataManager.concatLikeArguments(selectionArgs);
			selection += DbDataManager.AND_ + DbScheme.V_USER + DbDataManager.EQUALS_ARG_;

			String[] arguments = new String[selectionArgs.length];
			arguments[0] = DbDataManager.concatArguments(query);

			for (int i = 1; i < selectionArgs.length; i++) {
				arguments[i] = DbDataManager.anyLikeMatch(query);
			}

			QueryParams queryParams = new QueryParams();
			queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.CONVERSATIONS_INBOX.ordinal()]);
			queryParams.setSelection(selection);
			queryParams.setArguments(arguments);
			queryParams.setOrder(DbScheme.V_LAST_MESSAGE_CREATED_AT + DbDataManager.DESCEND);

			Cursor cursor = DbDataManager.query(getContentResolver(), queryParams);
			cursor.moveToFirst();
			return cursor;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long conversationId = DbDataManager.getLong(cursor, DbScheme.V_ID);
		String otherUserName = DbDataManager.getString(cursor, DbScheme.V_OTHER_USER_USERNAME);

		getActivityFace().openFragment(MessagesConversationFragment.createInstance(conversationId, otherUserName));
	}

	private class ConversationsUpdateListener extends ChessUpdateListener<ConversationItem.Data> {

		@Override
		public void updateListData(List<ConversationItem.Data> itemsList) {
			super.updateListData(itemsList);

			if (itemsList.size() == 0) {
				emptyView.setText(R.string.no_messages);
				showEmptyView(true);
				return;
			}

			new SaveConversationsInboxTask(saveConversationsListener, itemsList, getContentResolver()).executeTask();
		}
	}

	private class SaveConversationsListener extends ChessUpdateListener<ConversationItem.Data> {

		@Override
		public void updateData(ConversationItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFromDb();
		}
	}

	private class ConversationCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			conversationsAdapter.changeCursor(returnedObj);
			paginationAdapter.notifyDataSetChanged();
			need2update = false;
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_messages);
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
