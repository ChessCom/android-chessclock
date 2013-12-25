package com.chess.ui.fragments.videos;

import android.content.Intent;
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
import com.chess.db.tasks.SaveVideoCategoriesTask;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragmentTablet;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.11.13
 * Time: 10:30
 */
public class VideosFragmentTablet extends CommonLogicFragment implements AdapterView.OnItemClickListener, FragmentParentFace {

	private ListView listView;
	private View loadingView;
	private TextView emptyView;

	private CommonCategoriesCursorAdapter categoriesAdapter;

	private VideoCategoriesUpdateListener videoCategoriesUpdateListener;
	private SaveVideoCategoriesUpdateListener saveVideoCategoriesUpdateListener;
	private boolean noCategoriesFragmentsAdded;

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

		setTitle(R.string.videos);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		{ // Library mode init
			listView = (ListView) view.findViewById(R.id.listView);

			if (isNeedToUpgrade()) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				View headerView = inflater.inflate(R.layout.new_lessons_upgrade_view, null, false);
				headerView.findViewById(R.id.upgradeBtn).setOnClickListener(this);
				TextView textView = (TextView) headerView.findViewById(R.id.lessonsUpgradeMessageTxt);
				textView.setText(R.string.improve_game_with_videos);
				listView.addHeaderView(headerView);
			}

			listView.setAdapter(categoriesAdapter);
			listView.setOnItemClickListener(this);
		}

		showLibrary();

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	private void showLibrary() {
		if (need2update) {

			// get saved categories
			Cursor categoriesCursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.VIDEO_CATEGORIES.ordinal()], null, null, null, null);

			if (categoriesCursor != null && categoriesCursor.moveToFirst()) {
				categoriesAdapter.changeCursor(categoriesCursor);
			}

			if (AppUtils.isNetworkAvailable(getActivity())) {
				getCategories();
			}

		} else { // load data to listHeader view
			categoriesAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == VideoDetailsFragment.WATCH_VIDEO_REQUEST) {
			FragmentManager fragmentManager = getChildFragmentManager();
			Fragment fragment = fragmentManager.findFragmentByTag(VideoDetailsFragment.class.getSimpleName());
			if (fragment != null) {
				fragment.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEO_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(videoCategoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search_btn:
				getActivityFace().changeRightFragment(new VideosSearchFragment());
				getActivityFace().toggleRightMenu();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.upgradeBtn) {
			if (!isTablet) {
				getActivityFace().openFragment(new UpgradeFragment());
			} else {
				getActivityFace().openFragment(new UpgradeFragmentTablet());
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		boolean headerAdded = listView.getHeaderViewsCount() > 0; // used to check if header added
//		int offset = headerAdded ? -1 : 0;

		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		String sectionName = DbDataManager.getString(cursor, DbScheme.V_NAME);

		if (noCategoriesFragmentsAdded) {
			openInternalFragment(VideoCategoriesFragmentTablet.createInstance(sectionName, this));
			noCategoriesFragmentsAdded = false;
		} else {
			changeInternalFragment(VideoCategoriesFragmentTablet.createInstance(sectionName, this));
		}
	}

	private class VideoCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem> {
		public VideoCategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(CommonFeedCategoryItem returnedObj) {
			super.updateData(returnedObj);

			List<CommonFeedCategoryItem.Data> dataList = returnedObj.getData();
			for (CommonFeedCategoryItem.Data category : dataList) {
				category.setName(category.getName().replace(Symbol.AMP_CODE, Symbol.AMP));
			}

			new SaveVideoCategoriesTask(saveVideoCategoriesUpdateListener, dataList, getContentResolver()).executeTask();
		}
	}

	private class SaveVideoCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem.Data> {

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(CommonFeedCategoryItem.Data returnedObj) {
			// get saved categories
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.VIDEO_CATEGORIES));
			if (cursor.moveToFirst()) {
				categoriesAdapter.changeCursor(cursor);
				listView.setAdapter(categoriesAdapter);

				need2update = false;
			}
		}
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

	private void init() {
		categoriesAdapter = new CommonCategoriesCursorAdapter(getActivity(), null);
		categoriesAdapter.setLayoutId(R.layout.new_common_titled_list_item_thin_white);

		videoCategoriesUpdateListener = new VideoCategoriesUpdateListener();
		saveVideoCategoriesUpdateListener = new SaveVideoCategoriesUpdateListener();

		changeInternalFragment(VideosCurriculumFragmentTablet.createInstance(this));

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
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment, fragment.getClass().getSimpleName());
		transaction.addToBackStack(fragment.getClass().getSimpleName());
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
			if (stackEntry != null && stackEntry.getName().equals(VideoCategoriesFragmentTablet.class.getSimpleName())) {
				noCategoriesFragmentsAdded = true;
			}

			return getChildFragmentManager().popBackStackImmediate();
		} else {
			return super.showPreviousFragment();
		}
	}

}
