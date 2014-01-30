package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ForumTopicItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveForumTopicsTask;
import com.chess.ui.adapters.ForumTopicsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.PageIndicatorView;
import com.chess.widgets.LinLayout;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 6:44
 */
public class ForumTopicsFragment extends CommonLogicFragment implements PageIndicatorView.PagerFace, AdapterView.OnItemClickListener {

	protected static final String CATEGORY_ID = "category_id";
	private int categoryId;

	private ForumTopicsCursorAdapter topicsCursorAdapter;
	private SaveForumTopicsListener saveForumTopicsListener;
	private SparseArray<String> categoriesMap;
	private TopicsUpdateListener topicsUpdateListener;
	private TextView forumHeaderTxt;
	private PageIndicatorView pageIndicatorView;
	private int pagesToShow;
	private int currentPage;

	public ForumTopicsFragment() { }

	public static ForumTopicsFragment createInstance(int categoryId) {
		ForumTopicsFragment fragment = new ForumTopicsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY_ID, categoryId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			categoryId = getArguments().getInt(CATEGORY_ID);
		} else {
			categoryId = savedInstanceState.getInt(CATEGORY_ID);
		}

		topicsCursorAdapter = new ForumTopicsCursorAdapter(getActivity(), null);
		topicsUpdateListener = new TopicsUpdateListener();
		saveForumTopicsListener = new SaveForumTopicsListener();

		categoriesMap = new SparseArray<String>();
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getForumCategories());
		if (cursor.moveToFirst()) {
			do {
				categoriesMap.put(DbDataManager.getInt(cursor, DbScheme.V_ID), DbDataManager.getString(cursor, DbScheme.V_NAME));
			} while (cursor.moveToNext());
		}

		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_forum_topics_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.forums);
		widgetsInit(view);


		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_add, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}



	@Override
	public void onResume() {
		super.onResume();

		forumHeaderTxt.setText(categoriesMap.get(categoryId));
		// always update as we can create topic in down fragments
		requestPage(currentPage);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(CATEGORY_ID, categoryId);
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		requestPage(currentPage);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		boolean headerAdded = listView.getHeaderViewsCount() > 0;
//		int offset = headerAdded ? -1 : 0;

		if (position != 0) { // if NOT listView header
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			int topicId = DbDataManager.getInt(cursor, DbScheme.V_ID);
			getActivityFace().openFragment(ForumPostsFragment.createInstance(topicId));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search_btn:
				getActivityFace().openFragment(new ForumSearchFragment());
				return true;
			case R.id.menu_add:
				getActivityFace().openFragment(new ForumNewTopicFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void showPrevPage() {
		requestPage(currentPage - 1);
	}

	@Override
	public void showNextPage() {
		requestPage(currentPage + 1);
	}

	@Override
	public void showPage(int page) {
		requestPage(page);
	}

	private void requestPage(int page){
		currentPage = page;

		if (currentPage == 0) {
			pageIndicatorView.enableLeftBtn(false);
		} else {
			pageIndicatorView.enableLeftBtn(true);
		}

		if (currentPage == pagesToShow) {
			pageIndicatorView.enableRightBtn(false);
		} else {
			pageIndicatorView.enableRightBtn(true);
		}

		LoadItem loadItem = LoadHelper.getForumTopicsForCategory(getUserToken(), categoryId, page);
		new RequestJsonTask<ForumTopicItem>(topicsUpdateListener).executeTask(loadItem);
		// lock page changing
		pageIndicatorView.setEnabled(false);
	}

	private class TopicsUpdateListener extends ChessUpdateListener<ForumTopicItem> {

		private TopicsUpdateListener() {
			super(ForumTopicItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			pageIndicatorView.setEnabled(!show);
		}

		@Override
		public void updateData(ForumTopicItem returnedObj) {
			List<ForumTopicItem.Topic> topics = returnedObj.getData().getTopics();
			pagesToShow = (int) Math.ceil((returnedObj.getData().getTopicsTotalCount() / (float) (RestHelper.DEFAULT_ITEMS_PER_PAGE)));
			pageIndicatorView.setTotalPageCnt(pagesToShow);
			if (currentPage == pagesToShow - 1) {
				pageIndicatorView.enableRightBtn(false);
			} else {
				pageIndicatorView.enableRightBtn(true);
			}

			new SaveForumTopicsTask(saveForumTopicsListener, topics, getContentResolver(), categoriesMap, currentPage).executeTask();
		}
	}

	private class SaveForumTopicsListener extends ChessUpdateListener<ForumTopicItem.Topic> {

		@Override
		public void updateData(ForumTopicItem.Topic returnedObj) {
			super.updateData(returnedObj);

			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getForumTopicByCategory(categoryId, currentPage));
			if (cursor.moveToFirst()) {
				topicsCursorAdapter.changeCursor(cursor);
			} else {
				showToast("Internal error");
			}

			// unlock page changing
			pageIndicatorView.setEnabled(true);
		}
	}

	private void widgetsInit(View view) {
		// add headerView
		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_forum_header_view, null, false);
		forumHeaderTxt = (TextView) headerView.findViewById(R.id.forumHeaderTxt);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(topicsCursorAdapter);
		listView.setOnItemClickListener(this);

		LinLayout pageIndicatorLay = (LinLayout) view.findViewById(R.id.pageIndicatorLay);
		pageIndicatorView = (PageIndicatorView) view.findViewById(R.id.pageIndicatorView);
		pageIndicatorView.setPagerFace(this);

		initUpgradeAndAdWidgets(view);

		if (!isNeedToUpgrade()) {// we need to bind to bottom if there is no ad banner
			((RelativeLayout.LayoutParams) pageIndicatorLay.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}
	}
}
