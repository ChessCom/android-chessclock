package com.chess.ui.fragments.articles;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
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
import com.chess.backend.statics.StaticData;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
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

	private View loadingView;
	private TextView emptyView;
	private ListView listView;
	private HashMap<String, Integer> categoriesMap;
	private String categoryName;
	private SparseBooleanArray viewedArticlesMap;
	private ArticlesPaginationAdapter paginationAdapter;
	private int previousCategoryId;
	private String sectionName;
	private List<String> categoriesNames;

	public static ArticleCategoriesFragment createInstance(String sectionName) {
		ArticleCategoriesFragment frag = new ArticleCategoriesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			sectionName = getArguments().getString(SECTION_NAME);
		} else {
			sectionName = savedInstanceState.getString(SECTION_NAME);
		}

		categoriesNames = new ArrayList<String>();
		categoriesMap = new HashMap<String, Integer>();

		viewedArticlesMap = new SparseBooleanArray();
		articlesAdapter = new ArticlesCursorAdapter(getActivity(), null);
		articlesAdapter.addViewedMap(viewedArticlesMap);
		paginationAdapter = new ArticlesPaginationAdapter(getActivity(), articlesAdapter, new ArticleUpdateListener(), null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_categories_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

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
			int sectionId;
			for (sectionId = 0; sectionId < categoriesNames.size(); sectionId++) {
				String category = categoriesNames.get(sectionId);
				if (category.equals(sectionName)) {
					break;
				}
			}

			Spinner categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
			categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), categoriesNames));
			categorySpinner.setOnItemSelectedListener(this);
			categorySpinner.setSelection(sectionId);  // TODO remember last selection.
		}

		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(SECTION_NAME, sectionName);
	}

	private boolean fillCategories() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.ARTICLE_CATEGORIES));
		if (!(cursor != null && cursor.moveToFirst())) {
			showToast("Categories are not loaded");
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long articleId = DbDataManager.getLong(cursor, DbScheme.V_ID);
		getActivityFace().openFragment(ArticleDetailsFragment.createInstance(articleId));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		categoryName = (String) parent.getItemAtPosition(position);
		int categoryId = categoriesMap.get(categoryName);

		if (need2update || categoryId != previousCategoryId) {
			previousCategoryId = categoryId;
			need2update = true;

			// clear current list
			articlesAdapter.changeCursor(null);
			// TODO add logic to check if new video was added on server since last fetch

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLES_LIST);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, categoryId);
			loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE,  RestHelper.DEFAULT_ITEMS_PER_PAGE);

			paginationAdapter.updateLoadItem(loadItem);
		} else {
			paginationAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private class ArticleUpdateListener extends ChessUpdateListener<ArticleItem.Data> {

		public ArticleUpdateListener() {
			super(ArticleItem.Data.class);
		}

		@Override
		public void updateListData(List<ArticleItem.Data> itemsList) {

			for (ArticleItem.Data currentItem : itemsList) {
				DbDataManager.saveArticleItem(getContentResolver(), currentItem);
			}
			need2update = false;

			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getArticlesListByCategory(categoryName));
			if (cursor != null && cursor.moveToFirst()) {
				articlesAdapter.changeCursor(cursor);
				if (paginationAdapter != null) {
					paginationAdapter.notifyDataSetChanged();
				}
			}
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

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
