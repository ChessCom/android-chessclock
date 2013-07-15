package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ForumTopicItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.SaveForumTopicsTask;
import com.chess.ui.adapters.ForumTopicsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.PageIndicatorView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 6:44
 */
public class ForumTopicsFragment extends CommonLogicFragment implements PageIndicatorView.PagerFace, AdapterView.OnItemClickListener {

	private static final String CATEGORY_ID = "category_id";
	private static final int DEFAULT_PAGE = 0;
	private int categoryId;

	private ForumTopicsCursorAdapter topicsCursorAdapter;
	private SaveForumTopicsListener saveForumTopicsListener;
	private SparseArray<String> categoriesMap;
	private TopicsUpdateListener topicsUpdateListener;
	private TextView forumHeaderTxt;
	private boolean need2update = true;
	private PageIndicatorView pageIndicatorView;
	private int pagesToShow;
	private int currentPage = -1;
	private ListView listView;

	public ForumTopicsFragment() {

	}

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

		topicsCursorAdapter = new ForumTopicsCursorAdapter(getActivity(), null);
		topicsUpdateListener = new TopicsUpdateListener();
		saveForumTopicsListener = new SaveForumTopicsListener();

		categoriesMap = new SparseArray<String>();
		Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumCategoriesParams());
		if (cursor.moveToFirst()) {
			do {
				categoriesMap.put(DBDataManager.getInt(cursor, DBConstants.V_ID), DBDataManager.getString(cursor, DBConstants.V_NAME));
			} while (cursor.moveToNext());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_forum_topics_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.forums);

		// add headerView
		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_forum_header_view, null, false);
		forumHeaderTxt = (TextView) headerView.findViewById(R.id.forumHeaderTxt);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(topicsCursorAdapter);
		listView.setOnItemClickListener(this);

		pageIndicatorView = (PageIndicatorView) view.findViewById(R.id.pageIndicatorView);
		pageIndicatorView.setPagerFace(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_add, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null) {
			categoryId = getArguments().getInt(CATEGORY_ID);
		} else {
			categoryId = savedInstanceState.getInt(CATEGORY_ID);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		forumHeaderTxt.setText(categoriesMap.get(categoryId));
		if (need2update) {
			requestPage(DEFAULT_PAGE);
		} else {
			pageIndicatorView.setTotalPageCnt(pagesToShow);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(CATEGORY_ID, categoryId);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		boolean headerAdded = listView.getHeaderViewsCount() > 0;
		int offset = headerAdded ? -1 : 0;

		if (position == 0) { // if listView header
			// see onClick(View) handle
		} else {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			int topicId = DBDataManager.getInt(cursor, DBConstants.V_ID);
			String topicTitle = DBDataManager.getString(cursor, DBConstants.V_TITLE);
			getActivityFace().openFragment(ForumPostsFragment.createInstance(topicId, topicTitle));
		}
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
		requestPage(page-1);
	}

	private void requestPage(int page){
		if (page == currentPage) {
			return;
		}
		currentPage = page;

		if (currentPage == DEFAULT_PAGE) {
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

			new SaveForumTopicsTask(saveForumTopicsListener, topics, getContentResolver(), categoriesMap).executeTask();
		}
	}

	private class SaveForumTopicsListener extends ChessUpdateListener<ForumTopicItem.Topic> {

		@Override
		public void updateData(ForumTopicItem.Topic returnedObj) {
			super.updateData(returnedObj);

			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumTopicByCategoryParams(categoryId));
			if (cursor.moveToFirst()) {
				topicsCursorAdapter.changeCursor(cursor);
			} else {
				showToast("Internal error");
			}

			need2update = false;
		}
	}
}
