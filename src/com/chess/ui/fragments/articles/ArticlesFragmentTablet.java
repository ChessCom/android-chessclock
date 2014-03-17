package com.chess.ui.fragments.articles;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveArticleCategoriesTask;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.interfaces.ItemClickListenerFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.11.13
 * Time: 17:33
 */
public class ArticlesFragmentTablet extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener, FragmentParentFace {

	private ListView listView;
	private TextView emptyView;

	private CommonCategoriesCursorAdapter categoriesAdapter;

	private CategoriesUpdateListener categoriesUpdateListener;
	private SaveCategoriesUpdateListener saveCategoriesUpdateListener;

	private boolean noCategoriesFragmentsAdded;
	private boolean categoriesLoaded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_tablet_content_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		// adjust actionBar icons
		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		categoriesLoaded = loadCategoriesFromDB();

		if (need2update) {
			if (!categoriesLoaded && isNetworkAvailable()) {
				getCategories();
			}
		}
	}

	private boolean loadCategoriesFromDB() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.ARTICLE_CATEGORIES));
		if (cursor != null && cursor.moveToFirst()) {
			categoriesAdapter.changeCursor(cursor);

			listView.setAdapter(categoriesAdapter);
			return true;
		}
		return false;
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLES_CATEGORIES);
		new RequestJsonTask<CommonFeedCategoryItem>(categoriesUpdateListener).executeTask(loadItem);
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
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
		String sectionName = DbDataManager.getString(cursor, DbScheme.V_NAME);

		if (noCategoriesFragmentsAdded) {
			changeInternalFragment(ArticleCategoriesFragmentTablet.createInstance(sectionName, this));
			noCategoriesFragmentsAdded = false;
		} else {
			changeInternalFragment(ArticleCategoriesFragmentTablet.createInstance(sectionName, this));
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
			need2update = false;
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
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(CommonFeedCategoryItem.Data returnedObj) {
			super.updateData(returnedObj);

			if (!categoriesLoaded) { // if categories were not loaded we didn't show main fragment on right side
				changeInternalFragment(ArticleCategoriesFragmentTablet.createInstance(Symbol.EMPTY,
						ArticlesFragmentTablet.this));
			}
			// show list of categories
			categoriesLoaded = loadCategoriesFromDB();
		}
	}

	private void init() {
		categoriesAdapter = new CommonCategoriesCursorAdapter(getActivity(), null);
		categoriesAdapter.setLayoutId(R.layout.common_titled_list_item_thin_white);

		categoriesUpdateListener = new CategoriesUpdateListener();
		saveCategoriesUpdateListener = new SaveCategoriesUpdateListener();

		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.ARTICLE_CATEGORIES));
		categoriesLoaded = cursor != null && cursor.moveToFirst();

		if (categoriesLoaded) {
			changeInternalFragment(ArticleCategoriesFragmentTablet.createInstance(Symbol.EMPTY, this));
		}

		noCategoriesFragmentsAdded = true;
	}

	@Override
	public void changeFragment(BasePopupsFragment fragment) {
		openInternalFragment(fragment);
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment);
		transaction.commitAllowingStateLoss();
	}

	private void openInternalFragment(Fragment fragment) {
		String simpleName = fragment.getClass().getSimpleName();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment, simpleName);
		transaction.addToBackStack(simpleName);
		transaction.commitAllowingStateLoss();
	}


	@Override
	public boolean showPreviousFragment() {
		if (getActivity() == null) {
			return false;
		}
		int entryCount = getChildFragmentManager().getBackStackEntryCount();
		if (entryCount > 0) {
			int last = entryCount - 1;
			FragmentManager.BackStackEntry stackEntry = getChildFragmentManager().getBackStackEntryAt(last);
			if (stackEntry != null && stackEntry.getName().equals(ArticleCategoriesFragmentTablet.class.getSimpleName())) {
				noCategoriesFragmentsAdded = true;
			}

			return getChildFragmentManager().popBackStackImmediate();
		} else {
			return super.showPreviousFragment();
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}
}
