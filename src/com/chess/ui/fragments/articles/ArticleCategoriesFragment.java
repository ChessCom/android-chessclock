package com.chess.ui.fragments.articles;

import android.content.Context;
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
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ArticleItem;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ArticlesCursorAdapter;
import com.chess.ui.adapters.ArticlesPaginationAdapter;
import com.chess.ui.adapters.DarkSpinnerAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 6:56
 */
public class ArticleCategoriesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

	public static final String SECTION_NAME = "section_name";

	private ArticlesCursorAdapter articlesAdapter;

	protected TextView emptyView;
	private ListView listView;
	protected HashMap<String, Integer> categoriesMap;
	protected SparseBooleanArray viewedArticlesMap;
	protected ArticlesPaginationAdapter paginationAdapter;
	private int previousCategoryId;
	protected String sectionName;
	protected List<String> categoriesNames;
	protected int selectedCategoryId;

	public ArticleCategoriesFragment() {
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, Symbol.EMPTY);
		setArguments(bundle);
	}

	public static ArticleCategoriesFragment createInstance(String sectionName) {
		ArticleCategoriesFragment fragment = new ArticleCategoriesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			sectionName = getArguments().getString(SECTION_NAME);
		} else {
			sectionName = savedInstanceState.getString(SECTION_NAME);
		}

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_categories_ads_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		emptyView = (TextView) view.findViewById(R.id.emptyView);

		widgetsInit(view);

		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(SECTION_NAME, sectionName);
	}

	protected boolean fillCategories() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.ARTICLE_CATEGORIES));
		if (!(cursor != null && cursor.moveToFirst())) {
			return false;
		}

		do {
			String name = DbDataManager.getString(cursor, DbScheme.V_NAME);
			categoriesNames.add(name);
			categoriesMap.put(name, DbDataManager.getInt(cursor, DbScheme.V_CATEGORY_ID));
		} while (cursor.moveToNext());

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search_btn:
				getActivityFace().openFragment(new ArticlesSearchFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// don't process clicks on pending view
		if (position == articlesAdapter.getCount()) {
			return;
		}
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long articleId = DbDataManager.getLong(cursor, DbScheme.V_ID);
		getActivityFace().openFragment(ArticleDetailsFragment.createInstance(articleId));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		sectionName = (String) parent.getItemAtPosition(position);
		selectedCategoryId = categoriesMap.get(sectionName);

		updateByCategory();
	}

	protected void updateByCategory() {
		if (need2update || selectedCategoryId != previousCategoryId) {
			previousCategoryId = selectedCategoryId;
			need2update = true;

			// clear current list
			getAdapter().changeCursor(null);

			if (isNetworkAvailable()) {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLES_LIST);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
				loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, selectedCategoryId);
				loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.DEFAULT_ITEMS_PER_PAGE);

				paginationAdapter.updateLoadItem(loadItem);
			} else {
				loadFromDb();
			}
		} else {
			paginationAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	protected class ArticleUpdateListener extends ChessUpdateListener<ArticleItem.Data> {

		public ArticleUpdateListener() {
			super(ArticleItem.Data.class);
		}

		@Override
		public void updateListData(List<ArticleItem.Data> itemsList) {
			for (ArticleItem.Data currentItem : itemsList) {
				DbDataManager.saveArticleItem(getContentResolver(), currentItem, false);
			}
			need2update = false;

			loadFromDb();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}
		}
	}

	private void loadFromDb() {
		Cursor cursor;
		if (selectedCategoryId == 0) { // load all
			cursor = DbDataManager.query(getContentResolver(), DbHelper.getAllArticlesList());
		} else {
			cursor = DbDataManager.query(getContentResolver(), DbHelper.getArticlesListByCategory(selectedCategoryId));
		}
		if (cursor != null && cursor.moveToFirst()) {
			getAdapter().changeCursor(cursor);
			if (paginationAdapter != null) {
				paginationAdapter.notifyDataSetChanged();
			}
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

	protected ArticlesCursorAdapter getAdapter() {
		return articlesAdapter;
	}

	protected void setAdapter(ArticlesCursorAdapter adapter) {
		articlesAdapter = adapter;
	}

	protected void init() {
		categoriesNames = new ArrayList<String>();
		categoriesMap = new HashMap<String, Integer>();

		viewedArticlesMap = new SparseBooleanArray();

		setAdapter(new ArticlesCursorAdapter(getActivity(), null, getImageFetcher()));
		getAdapter().addViewedMap(viewedArticlesMap);
		paginationAdapter = new ArticlesPaginationAdapter(getActivity(), getAdapter(), new ArticleUpdateListener(), null);
	}

	protected void widgetsInit(View view) {
		listView = (ListView) view.findViewById(R.id.listView);
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
			int position = 0;
			if (TextUtils.isEmpty(sectionName)) {
				sectionName = categoriesNames.get(0);
				selectedCategoryId = categoriesMap.get(categoriesNames.get(0));
			} else {
				for (position = 0; position < categoriesNames.size(); position++) {
					String category = categoriesNames.get(position);
					if (category.equals(sectionName)) {
						selectedCategoryId = categoriesMap.get(category);
						break;
					}
				}
			}

			Spinner categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
			categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), categoriesNames));
			categorySpinner.setOnItemSelectedListener(this);
			categorySpinner.setSelection(position);
		}

		initUpgradeAndAdWidgets(view);
	}


	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
