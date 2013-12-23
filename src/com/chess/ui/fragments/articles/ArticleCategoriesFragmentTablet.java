package com.chess.ui.fragments.articles;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveArticleCategoriesTask;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ArticlesCursorAdapter;
import com.chess.ui.adapters.ArticlesCursorAdapterTablet;
import com.chess.ui.adapters.ArticlesPaginationAdapter;
import com.chess.ui.interfaces.FragmentParentFace;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.11.13
 * Time: 17:46
 */
public class ArticleCategoriesFragmentTablet extends ArticleCategoriesFragment {

	private ArticlesCursorAdapterTablet articlesAdapter;
	private FragmentParentFace parentFace;
	private CategoriesUpdateListener categoriesUpdateListener;
	private SaveCategoriesUpdateListener saveCategoriesUpdateListener;

	public ArticleCategoriesFragmentTablet() {
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, Symbol.EMPTY);
		setArguments(bundle);
	}

	public static ArticleCategoriesFragmentTablet createInstance(String sectionName, FragmentParentFace parentFace) {
		ArticleCategoriesFragmentTablet fragment = new ArticleCategoriesFragmentTablet();
		fragment.parentFace = parentFace;
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(sectionName);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// don't process clicks on pending view
		if (position == articlesAdapter.getCount()) {
			return;
		}

		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long articleId = DbDataManager.getLong(cursor, DbScheme.V_ID);

		if (inPortrait()) {
			getActivityFace().openFragment(ArticleDetailsFragment.createInstance(articleId));
		} else {
			if (parentFace == null) {
				getActivityFace().showPreviousFragment();
			}
			parentFace.changeFragment(ArticleDetailsFragment.createInstance(articleId));
		}
	}

	protected void setAdapter(ArticlesCursorAdapterTablet adapter) {
		this.articlesAdapter = adapter;
	}

	@Override
	protected ArticlesCursorAdapter getAdapter() {
		return articlesAdapter;
	}

	@Override
	protected void init() {
		categoriesNames = new ArrayList<String>();
		categoriesMap = new HashMap<String, Integer>();
		categoriesUpdateListener = new CategoriesUpdateListener();
		saveCategoriesUpdateListener = new SaveCategoriesUpdateListener();
		viewedArticlesMap = new SparseBooleanArray();

		setAdapter(new ArticlesCursorAdapterTablet(getActivity(), null, getImageFetcher()));
		getAdapter().addViewedMap(viewedArticlesMap);
		paginationAdapter = new ArticlesPaginationAdapter(getActivity(), getAdapter(), new ArticleUpdateListener(), null);
	}

	@Override
	protected void widgetsInit(View view) {
		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(paginationAdapter);
		listView.setOnItemClickListener(this);

		// get viewed marks
		Cursor cursor = DbDataManager.getArticleViewedCursor(getActivity(), getUsername());
		if (cursor != null) {
			do {
				int videoId = DbDataManager.getInt(cursor, DbScheme.V_ID);
				boolean isViewed = DbDataManager.getInt(cursor, DbScheme.V_DATA_VIEWED) > 0;
				viewedArticlesMap.put(videoId, isViewed);
			} while (cursor.moveToNext());
			cursor.close();
		}

		boolean loaded = categoriesMap.size() != 0 || fillCategories();

		if (loaded) {
			int position;
			if (TextUtils.isEmpty(sectionName)) {
				sectionName = categoriesNames.get(0);
				selectedCategoryId = categoriesMap.get(categoriesNames.get(0));
				setTitle(sectionName);
			} else {
				for (position = 0; position < categoriesNames.size(); position++) {
					String category = categoriesNames.get(position);
					if (category.equals(sectionName)) {
						selectedCategoryId = categoriesMap.get(category);
						break;
					}
				}
			}

			updateByCategory();
		} else {
			getCategories();
		}
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLES_CATEGORIES);
		new RequestJsonTask<CommonFeedCategoryItem>(categoriesUpdateListener).executeTask(loadItem);
	}

	private class CategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem> {
		public CategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void updateData(CommonFeedCategoryItem returnedObj) {
			super.updateData(returnedObj);

			new SaveArticleCategoriesTask(saveCategoriesUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
		}
	}

	private class SaveCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem.Data> {
		@Override
		public void updateData(CommonFeedCategoryItem.Data returnedObj) {
			super.updateData(returnedObj);

			if (fillCategories()) {
				int position;
				if (TextUtils.isEmpty(sectionName)) {
					sectionName = categoriesNames.get(0);
					selectedCategoryId = categoriesMap.get(categoriesNames.get(0));
					setTitle(sectionName);
				} else {
					for (position = 0; position < categoriesNames.size(); position++) {
						String category = categoriesNames.get(position);
						if (category.equals(sectionName)) {
							selectedCategoryId = categoriesMap.get(category);
							break;
						}
					}
				}
				updateByCategory();
			}
		}
	}
}
