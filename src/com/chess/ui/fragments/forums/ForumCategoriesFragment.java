package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ForumCategoryItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.SaveForumCategoriesTask;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.07.13
 * Time: 22:05
 */
public class ForumCategoriesFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private CommonCategoriesCursorAdapter categoriesCursorAdapter;
	private CategoriesUpdateListener categoriesUpdateListener;
	private SaveForumCategoriesListener saveForumCategoriesListener;
	private boolean need2update = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_forum_categories_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.forums);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(categoriesCursorAdapter);
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

		if (need2update) {

			if (!loadFromDb()) {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_FORUMS_CATEGORIES);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

				new RequestJsonTask<ForumCategoryItem>(categoriesUpdateListener).executeTask(loadItem);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		int categoryId = DbDataManager.getInt(cursor, DbConstants.V_ID);
		getActivityFace().openFragment(ForumTopicsFragment.createInstance(categoryId));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add:
				getActivityFace().openFragment(new ForumNewTopicFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class CategoriesUpdateListener extends ChessUpdateListener<ForumCategoryItem> {

		private CategoriesUpdateListener() {
			super(ForumCategoryItem.class);
		}

		@Override
		public void updateData(ForumCategoryItem returnedObj) {

			List<ForumCategoryItem.Data> categoriesList = returnedObj.getData();

			new SaveForumCategoriesTask(saveForumCategoriesListener, categoriesList,
					getContentResolver()).executeTask();
		}
	}

	private class SaveForumCategoriesListener extends ChessUpdateListener<ForumCategoryItem.Data> {

		@Override
		public void updateData(ForumCategoryItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFromDb();
		}
	}

	private boolean loadFromDb() {
		Cursor cursor = DbDataManager.executeQuery(getContentResolver(), DbHelper.getForumCategories());
		if (cursor != null && cursor.moveToFirst()) {
			categoriesCursorAdapter.changeCursor(cursor);

			need2update = false;
			return true;
		}

		return false;
	}

	private void init() {
		categoriesCursorAdapter = new CommonCategoriesCursorAdapter(getActivity(), null);
		categoriesUpdateListener = new CategoriesUpdateListener();
		saveForumCategoriesListener = new SaveForumCategoriesListener();
	}
}
