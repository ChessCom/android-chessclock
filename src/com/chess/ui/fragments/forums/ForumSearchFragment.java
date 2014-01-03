package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ForumCategoryItem;
import com.chess.backend.entity.api.ForumTopicItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.ui.adapters.ForumTopicsItemAdapter;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.fragments.BaseSearchFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.09.13
 * Time: 7:24
 */
public class ForumSearchFragment extends BaseSearchFragment {

	private ForumTopicsUpdateListener forumTopicsUpdateListener;
	private ForumTopicsItemAdapter forumTopicsAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		forumTopicsUpdateListener = new ForumTopicsUpdateListener();
		forumTopicsAdapter = new ForumTopicsItemAdapter(getActivity(), null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.forums);
	}

	@Override
	protected ListAdapter getAdapter() {
		return forumTopicsAdapter;
	}

	@Override
	protected QueryParams getQueryParams() {
		return DbHelper.getAll(DbScheme.Tables.FORUM_CATEGORIES);
	}

	@Override
	protected void fillCategoriesList(Cursor cursor) {
		do {
			int id = DbDataManager.getInt(cursor, DbScheme.V_ID);
			String name = DbDataManager.getString(cursor, DbScheme.V_NAME);
			categoriesArray.put(id, name);
			categories.add(name);
		} while (cursor.moveToNext());
		cursor.close();

		categorySpinner.setAdapter(new StringSpinnerAdapter(getActivity(), categories));
	}

	@Override
	protected void startSearch(String keyword, int categoryId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_FORUMS_TOPICS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_KEYWORD, keyword);
		if (categoryId != -1) {
			loadItem.addRequestParams(RestHelper.P_FORUM_CATEGORY_ID, categoryId);
		}

		new RequestJsonTask<ForumTopicItem>(forumTopicsUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ForumTopicItem.Topic topicItem = (ForumTopicItem.Topic) parent.getItemAtPosition(position);
		int topicId = topicItem.getId();
		getActivityFace().openFragment(ForumPostsFragment.createInstance(topicId));
	}

	@Override
	protected void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_FORUMS_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<ForumCategoryItem>(new CategoriesUpdateListener()).executeTask(loadItem);
	}

	private class CategoriesUpdateListener extends ChessUpdateListener<ForumCategoryItem> {

		private CategoriesUpdateListener() {
			super(ForumCategoryItem.class);
		}

		@Override
		public void updateData(ForumCategoryItem returnedObj) {

			for (ForumCategoryItem.Data currentItem : returnedObj.getData()) {
				DbDataManager.saveForumCategoryItem(getContentResolver(), currentItem);
			}
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.FORUM_CATEGORIES));
			if (cursor != null && cursor.moveToFirst()) {
				fillCategoriesList(cursor);
			}
		}
	}

	private class ForumTopicsUpdateListener extends ChessLoadUpdateListener<ForumTopicItem> {

		private ForumTopicsUpdateListener() {
			super(ForumTopicItem.class);
		}

		@Override
		public void updateData(ForumTopicItem returnedObj) {
			super.updateData(returnedObj);

			if (returnedObj.getData().getTopics().size() == 0) {
				showSinglePopupDialog(R.string.no_results);
				return;
			}

			forumTopicsAdapter.setItemsList(returnedObj.getData().getTopics());
			need2update = false;

			resultsFound = true;
			showSearchResults();
		}
	}
}
