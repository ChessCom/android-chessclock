package com.chess.ui.fragments.articles;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ArticleItem;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.ui.adapters.ArticleItemAdapter;
import com.chess.ui.fragments.BaseSearchFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.09.13
 * Time: 11:18
 */
public class ArticlesSearchFragment extends BaseSearchFragment  {

	private ArticleItemUpdateListener articleItemUpdateListener;
	private ArticleItemAdapter articleItemAdapter;
	private SparseBooleanArray articlesViewedMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		articlesViewedMap = new SparseBooleanArray();

		articleItemUpdateListener = new ArticleItemUpdateListener();
		articleItemAdapter = new ArticleItemAdapter(getActivity(), null, getImageFetcher());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		// get viewed marks
		Cursor cursor = DbDataManager.getArticleViewedCursor(getActivity(), getUsername());
		if (cursor != null) {
			do {
				int videoId = DbDataManager.getInt(cursor, DbScheme.V_ID);
				boolean isViewed = DbDataManager.getInt(cursor, DbScheme.V_DATA_VIEWED) > 0;
				articlesViewedMap.put(videoId, isViewed);
			} while (cursor.moveToNext());
			cursor.close();
		}
		articleItemAdapter.addViewedMap(articlesViewedMap);
	}

	@Override
	protected ListAdapter getAdapter() {
		return articleItemAdapter;
	}

	@Override
	protected QueryParams getQueryParams() {
		return DbHelper.getAll(DbScheme.Tables.ARTICLE_CATEGORIES);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ArticleItem.Data articleItem = (ArticleItem.Data) parent.getItemAtPosition(position);
		long articleId = articleItem.getId();
		getActivityFace().openFragment(ArticleDetailsFragment.createInstance(articleId));
	}

	@Override
	protected void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLES_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(new CategoriesUpdateListener()).executeTask(loadItem);
	}

	private class CategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem> {

		public CategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void updateData(CommonFeedCategoryItem returnedObj) {
			super.updateData(returnedObj);

			for (CommonFeedCategoryItem.Data currentItem : returnedObj.getData()) {
				DbDataManager.saveArticleCategory(getContentResolver(), currentItem);
			}

			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.ARTICLE_CATEGORIES));
			if (cursor != null && cursor.moveToFirst()) {
				fillCategoriesList(cursor);
			}
		}
	}

	@Override
	protected void startSearch(String keyword, int categoryId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLES_LIST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_KEYWORD, keyword);
		if (categoryId != -1) {
			loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, categoryId);
		}

		new RequestJsonTask<ArticleItem>(articleItemUpdateListener).executeTask(loadItem);
	}

	private class ArticleItemUpdateListener extends ChessLoadUpdateListener<ArticleItem> {

		private ArticleItemUpdateListener() {
			super(ArticleItem.class);
		}

		@Override
		public void updateData(ArticleItem returnedObj) {
			super.updateData(returnedObj);

			if (returnedObj.getData().size() == 0) {
				showSinglePopupDialog(R.string.no_results_found);
				return;
			}

			for (ArticleItem.Data currentItem : returnedObj.getData()) {
				DbDataManager.saveArticleItem(getContentResolver(), currentItem, false);
			}

			articleItemAdapter.setItemsList(returnedObj.getData());
			need2update = false;

			resultsFound = true;

			showSearchResults();
		}
	}
}