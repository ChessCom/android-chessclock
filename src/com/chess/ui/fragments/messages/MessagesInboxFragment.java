package com.chess.ui.fragments.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.MessagesItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.07.13
 * Time: 20:56
 */
public class MessagesInboxFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private ListView listView;
	private boolean need2update;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (need2update) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_MESSAGES_INBOX);

			new RequestJsonTask<MessagesItem>(new MessagesUpdateListener()).executeTask(loadItem);

		} else {
//			listView.setAdapter();
		}
	}

	private void init() {
		// TODO -> File | Settings | File Templates.

	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	private class MessagesUpdateListener extends ChessLoadUpdateListener<MessagesItem> {

		private MessagesUpdateListener() {
			super(MessagesItem.class);
		}

		@Override
		public void updateData(MessagesItem returnedObj) {
			super.updateData(returnedObj);

//			new SaveForumPostsTask(new SaveMessagesListener(), returnedObj.getData(), getContentResolver()).executeTask();
		}
	}

//	private class SaveMessagesListener extends ChessUpdateListener<MessagesItem.Data> {
//
//		@Override
//		public void updateData(ForumPostItem.Post returnedObj) {
//			super.updateData(returnedObj);
//
//			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumPostsById(topicId, currentPage));
//			if (cursor.moveToFirst()) {
//				postsCursorAdapter.changeCursor(cursor);
//				postsCursorAdapter.notifyDataSetChanged();
//			} else {
//				showToast("Internal error");
//			}
//
//
//		}
//	}
}
