package com.chess.ui.fragments.articles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.ui.adapters.ArticlesThumbCursorAdapter;
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

	private ArticlesThumbCursorAdapter articlesAdapter;

	private Spinner categorySpinner;
	private View loadingView;
	private TextView emptyView;
	private ListView listView;
	private boolean categoriesLoaded;
	private ArticleItemUpdateListener articleItemUpdateListener;
	private HashMap<String, Integer> categoriesMap;
	private String categoryName;
	private DarkSpinnerAdapter spinnerAdapter;

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

		categoriesMap = new HashMap<String, Integer>();
		spinnerAdapter = new DarkSpinnerAdapter(getActivity(), null);
		articlesAdapter = new ArticlesThumbCursorAdapter(getActivity(), null);
		articleItemUpdateListener = new ArticleItemUpdateListener();
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

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
		categorySpinner.setAdapter(spinnerAdapter);
		categorySpinner.setOnItemSelectedListener(this);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(articlesAdapter);
		listView.setOnItemClickListener(this);

		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!categoriesLoaded) {
			// get list of categories
			categoriesLoaded = fillCategories();
		}
	}

	private boolean fillCategories() {
		Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.ARTICLE_CATEGORIES.ordinal()], null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			List<String> list = new ArrayList<String>();

			do {
				String name = DbDataManager.getString(cursor, DbScheme.V_NAME);
				int id = DbDataManager.getInt(cursor, DbScheme.V_CATEGORY_ID);
				categoriesMap.put(name, id);
				list.add(name);
			} while (cursor.moveToNext());

			// get passed argument
			String selectedCategory = getArguments().getString(SECTION_NAME);

			int sectionId;
			for (sectionId = 0; sectionId < list.size(); sectionId++) {
				String category = list.get(sectionId);
				if (category.equals(selectedCategory)) {
					break;
				}
			}

			spinnerAdapter.setItemsList(list);
			categorySpinner.setSelection(sectionId);  // TODO remember last selection.
			return true;
		} else {
			showToast("categories are not loaded");
			return false;
		}
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

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLES_LIST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, categoryId);

		new RequestJsonTask<ArticleItem>(articleItemUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private class ArticleItemUpdateListener extends ChessUpdateListener<ArticleItem> {

		public ArticleItemUpdateListener() {
			super(ArticleItem.class);
		}

		@Override
		public void showProgress(boolean show) {

			showLoadingView(show);
		}

		@Override
		public void updateData(ArticleItem returnedObj) {
			String[] arguments = new String[1];

			for (ArticleItem.Data currentItem : returnedObj.getData()) {
				arguments[0] = String.valueOf(currentItem.getId());

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[DbScheme.Tables.ARTICLES.ordinal()];

				Cursor cursor = getContentResolver().query(uri, DbDataManager.PROJECTION_ITEM_ID,
						DbDataManager.SELECTION_ITEM_ID, arguments, null);

				ContentValues values = DbDataManager.putArticleItemToValues(currentItem);

				DbDataManager.updateOrInsertValues(getContentResolver(), cursor, uri, values);
			}

			Cursor cursor = DbDataManager.executeQuery(getContentResolver(), DbHelper.getArticlesListByCategory(categoryName));
			if (cursor != null && cursor.moveToFirst()) {
				articlesAdapter.changeCursor(cursor);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
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

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			if (articlesAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
