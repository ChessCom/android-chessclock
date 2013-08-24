package com.chess.ui.fragments.articles;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ArticleItem;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveArticleCategoriesTask;
import com.chess.db.tasks.SaveArticlesListTask;
import com.chess.ui.adapters.ArticlesThumbCursorAdapter;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 6:56
 */
public class ArticlesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	public static final String GREY_COLOR_DIVIDER = "##";
	private static final int LATEST_ARTICLES_CNT = 3;

	private static final int LATEST_SECTION = 0;
	private static final int CATEGORIES_SECTION = 1;

	private ListView listView;
	private View loadingView;
	private TextView emptyView;

	private ArticlesThumbCursorAdapter articlesCursorAdapter;
	private CommonCategoriesCursorAdapter categoriesAdapter;

	private ArticleItemUpdateListener latestArticleUpdateListener;
	private SaveArticlesUpdateListener saveArticlesUpdateListener;
	private ArticlesCursorUpdateListener articlesCursorUpdateListener;

	private CategoriesUpdateListener categoriesUpdateListener;
	private SaveCategoriesUpdateListener saveCategoriesUpdateListener;

	private CustomSectionedAdapter sectionedAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_light,
				new int[]{LATEST_SECTION, CATEGORIES_SECTION});

		articlesCursorAdapter = new ArticlesThumbCursorAdapter(getActivity(), null);
		categoriesAdapter = new CommonCategoriesCursorAdapter(getActivity(), null);

		sectionedAdapter.addSection(getString(R.string.articles), articlesCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.category), categoriesAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setDivider(null);
		listView.setDividerHeight(0);
		listView.setOnItemClickListener(this);

		// adjust actionBar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		init();

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedArticles(getActivity());

			if (!loadCategoriesFromDB()) {
				getCategories();
			}

			if (haveSavedData) {
				loadFromDb();
			} else {
				emptyView.setText(R.string.no_data);
				showEmptyView(true);
			}

		} else {
			loadCategoriesFromDB();
			loadFromDb();
		}
	}

	private boolean loadCategoriesFromDB() {
		Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.ARTICLE_CATEGORIES.ordinal()], null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			categoriesAdapter.changeCursor(cursor);
			sectionedAdapter.notifyDataSetChanged();

			listView.setAdapter(sectionedAdapter);
			return true;
		}
		return false;
	}

	private void init() {
		latestArticleUpdateListener = new ArticleItemUpdateListener();
		saveArticlesUpdateListener = new SaveArticlesUpdateListener();
		articlesCursorUpdateListener = new ArticlesCursorUpdateListener();

		categoriesUpdateListener = new CategoriesUpdateListener();
		saveCategoriesUpdateListener = new SaveCategoriesUpdateListener();
	}

	private void updateUiData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLES_LIST);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, LATEST_ARTICLES_CNT);

		new RequestJsonTask<ArticleItem>(latestArticleUpdateListener).executeTask(loadItem);
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLES_CATEGORIES);
		new RequestJsonTask<CommonFeedCategoryItem>(categoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		int section = sectionedAdapter.getCurrentSection(position);

		if (section == LATEST_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			long articleId = DbDataManager.getLong(cursor, DbScheme.V_ID);
			getActivityFace().openFragment(ArticleDetailsFragment.createInstance(articleId));
		} else if (section == CATEGORIES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			String sectionName = DbDataManager.getString(cursor, DbScheme.V_NAME);

			getActivityFace().openFragment(ArticleCategoriesFragment.createInstance(sectionName));
		}
	}

	private class ArticleItemUpdateListener extends ChessUpdateListener<ArticleItem> {

		public ArticleItemUpdateListener() {
			super(ArticleItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(ArticleItem returnedObj) {
			new SaveArticlesListTask(saveArticlesUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
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

	private class CategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem> {
		public CategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
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
			showEmptyView(true);
		}
	}

	private class SaveCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem.Data> {
		@Override
		public void updateData(CommonFeedCategoryItem.Data returnedObj) {
			super.updateData(returnedObj);

			// show list of categories
			loadCategoriesFromDB();

			// loading articles
			updateUiData();
		}
	}

	private class SaveArticlesUpdateListener extends ChessUpdateListener<ArticleItem.Data> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(ArticleItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFromDb();
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

	private void loadFromDb() {
		new LoadDataFromDbTask(articlesCursorUpdateListener, DbHelper.getArticlesList(LATEST_ARTICLES_CNT),
				getContentResolver()).executeTask();
	}

	private class ArticlesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			articlesCursorAdapter.changeCursor(returnedObj);
			sectionedAdapter.notifyDataSetChanged();

			need2update = false;
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
			if (sectionedAdapter.getCount() == 0) {
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
