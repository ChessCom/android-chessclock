package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ForumCategoryItem;
import com.chess.backend.entity.new_api.ForumTopicItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.SaveForumCategoriesTask;
import com.chess.db.tasks.SaveForumTopicsTask;
import com.chess.ui.adapters.NewForumsSectionedCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.07.13
 * Time: 22:05
 */
public class ForumsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final int ITEMS_PER_CATEGORY = 3;
	private NewForumsSectionedCursorAdapter forumsTopicsCursorAdapter;
	private ListView listView;
	private CategoriesUpdateListener categoriesUpdateListener;
	private SaveForumTopicsListener saveForumTopicsListener;
	private List<ForumCategoryItem.Data> categoriesList;
	private SaveForumCategoriesListener saveForumCategoriesListener;
	private SparseArray<String> categoriesMap;

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
		listView.setAdapter(forumsTopicsCursorAdapter);
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

		new RequestJsonTask<ForumCategoryItem>(categoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	private class CategoriesUpdateListener extends ChessUpdateListener<ForumCategoryItem>{

		private CategoriesUpdateListener() {
			super(ForumCategoryItem.class);
		}

		@Override
		public void updateData(ForumCategoryItem returnedObj) {

			categoriesList = returnedObj.getData();

			for (ForumCategoryItem.Data categoryData : categoriesList) {
				categoriesMap.put(categoryData.getId(), categoryData.getCategory());
			}

			new SaveForumCategoriesTask(saveForumCategoriesListener, categoriesList,
					getContentResolver()).executeTask();

		}
	}

	private class TopicsUpdateListener extends ChessUpdateListener<ForumTopicItem>{

		private TopicsUpdateListener() {
			super(ForumTopicItem.class);
		}

		@Override
		public void updateData(ForumTopicItem returnedObj) {
			new SaveForumTopicsTask(saveForumTopicsListener, returnedObj.getData(), getContentResolver(),
					categoriesMap).executeTask();

		}
	}

	private class SaveForumTopicsListener extends ChessUpdateListener<ForumTopicItem.Data> {

		@Override
		public void updateData(ForumTopicItem.Data returnedObj) {
			super.updateData(returnedObj);

			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumTopicsParams());
			if (cursor.moveToFirst()) {
				forumsTopicsCursorAdapter.changeCursor(cursor);
				forumsTopicsCursorAdapter.notifyDataSetChanged();
			} else {
				showToast("Internal error");
			}

		}
	}

	private class SaveForumCategoriesListener extends ChessUpdateListener<ForumCategoryItem.Data> {

		@Override
		public void updateData(ForumCategoryItem.Data returnedObj) {
			super.updateData(returnedObj);

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_FORUMS_TOPICS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

			new RequestJsonTask<ForumTopicItem>(new TopicsUpdateListener()).executeTask(loadItem);


//			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumTopicsParams());
//			if (cursor.moveToFirst()) {
//				forumsTopicsCursorAdapter.changeCursor(cursor);
//				forumsTopicsCursorAdapter.notifyDataSetChanged();
//			} else {
//				showToast("Internal error");
//			}

		}
	}

	private void init() {
		categoriesMap = new SparseArray<String>();

		forumsTopicsCursorAdapter = new NewForumsSectionedCursorAdapter(getContext(), null, ITEMS_PER_CATEGORY);
		categoriesUpdateListener = new CategoriesUpdateListener();
		saveForumTopicsListener = new SaveForumTopicsListener();
		saveForumCategoriesListener = new SaveForumCategoriesListener();
	}
}
