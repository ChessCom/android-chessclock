package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ForumItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.SaveForumsListTask;
import com.chess.ui.adapters.NewForumsSectionedCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.07.13
 * Time: 22:05
 */
public class ForumsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final int ITEMS_PER_CATEGORY = 3;
	private NewForumsSectionedCursorAdapter forumsCursorAdapter;
	private ListView listView;
	private ForumsUpdateListener forumsUpdateListener;
	private SaveForumsListener saveForumsListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_forums_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(forumsCursorAdapter);
		listView.setOnItemClickListener(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_add, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_FORUMS_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<ForumItem>(forumsUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	private class ForumsUpdateListener extends ChessLoadUpdateListener<ForumItem>{

		private ForumsUpdateListener() {
			super(ForumItem.class);
		}

		@Override
		public void updateData(ForumItem returnedObj) {
			new SaveForumsListTask(saveForumsListener, returnedObj.getData(), getContentResolver()).executeTask();

		}
	}

	private class SaveForumsListener extends ChessLoadUpdateListener<ForumItem.Data> {

		@Override
		public void updateData(ForumItem.Data returnedObj) {
			super.updateData(returnedObj);

			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumsListParams());
			if (cursor.moveToFirst()) {
				forumsCursorAdapter.changeCursor(cursor);
				forumsCursorAdapter.notifyDataSetChanged();
			} else {
				showToast("Internal error");
			}

		}
	}

	private void init() {
		forumsCursorAdapter = new NewForumsSectionedCursorAdapter(getContext(), null, ITEMS_PER_CATEGORY);
		forumsUpdateListener = new ForumsUpdateListener();
		saveForumsListener = new SaveForumsListener();
	}
}
