package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
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
import com.chess.backend.entity.new_api.ForumPostItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.SaveForumPostsTask;
import com.chess.ui.adapters.ForumPostsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.PageIndicatorView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 6:43
 */
public class ForumPostsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, PageIndicatorView.PagerFace {


	private static final String TOPIC_ID = "topic_id";
	private static final String TOPIC_TITLE = "topic_title";
	private static final int DEFAULT_PAGE = 0;
	private int topicId;
	private ForumPostsCursorAdapter postsCursorAdapter;
	private SavePostsListener savePostsListener;
	private boolean need2update = true;
	private PostsUpdateListener postsUpdateListener;
	private TextView forumHeaderTxt;
	private String topicTitle;
	private PageIndicatorView pageIndicatorView;
	private int currentPage = -1;
	private int pagesToShow;

	public ForumPostsFragment() {

	}

	public static ForumPostsFragment createInstance(int topicId, String topicTitle){
		ForumPostsFragment fragment = new ForumPostsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(TOPIC_ID, topicId);
		bundle.putString(TOPIC_TITLE, topicTitle);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		postsCursorAdapter = new ForumPostsCursorAdapter(getActivity(), null);
		savePostsListener = new SavePostsListener();
		postsUpdateListener = new PostsUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_forum_posts_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.forums);

		// add headerView
		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_forum_header_view, null, false);
		forumHeaderTxt = (TextView) headerView.findViewById(R.id.forumHeaderTxt);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(postsCursorAdapter);
		listView.setOnItemClickListener(this);

		pageIndicatorView = (PageIndicatorView) view.findViewById(R.id.pageIndicatorView);
		pageIndicatorView.setPagerFace(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null) {
			topicId = getArguments().getInt(TOPIC_ID);
			topicTitle = getArguments().getString(TOPIC_TITLE);
		} else {
			topicId = savedInstanceState.getInt(TOPIC_ID);
			topicTitle = savedInstanceState.getString(TOPIC_TITLE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		forumHeaderTxt.setText(topicTitle);

		if (need2update) {
			requestPage(DEFAULT_PAGE);
		}  else {
			pageIndicatorView.setTotalPageCnt(pagesToShow);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(TOPIC_ID, topicId);
		outState.putString(TOPIC_TITLE, topicTitle);
	}

	private void requestPage(int page){
		if (page == currentPage) {
			return;
		}
		currentPage = page;

		if (currentPage == 1) {
			pageIndicatorView.enableLeftBtn(false);
		} else {
			pageIndicatorView.enableLeftBtn(true);
		}

		if (currentPage == pagesToShow) {
			pageIndicatorView.enableRightBtn(false);
		} else {
			pageIndicatorView.enableRightBtn(true);
		}

		LoadItem loadItem = LoadHelper.getForumPostsForTopic(getUserToken(), topicId, page);

		new RequestJsonTask<ForumPostItem>(postsUpdateListener).executeTask(loadItem);
		// lock page changing
		pageIndicatorView.setEnabled(false);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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

	private class PostsUpdateListener extends ChessUpdateListener<ForumPostItem>{

		private PostsUpdateListener() {
			super(ForumPostItem.class);
		}

		@Override
		public void updateData(ForumPostItem returnedObj) {
			pagesToShow = (int) Math.ceil((returnedObj.getData().getCommentsCount() / (float) (RestHelper.DEFAULT_ITEMS_PER_PAGE)));
			pageIndicatorView.setTotalPageCnt(pagesToShow);
			if (currentPage == pagesToShow) {
				pageIndicatorView.enableRightBtn(false);
			} else {
				pageIndicatorView.enableRightBtn(true);
			}

			new SaveForumPostsTask(savePostsListener, returnedObj.getData().getPosts(), getContentResolver(), topicId, currentPage).executeTask();
		}
	}

	private class SavePostsListener extends ChessUpdateListener<ForumPostItem.Post> {

		@Override
		public void updateData(ForumPostItem.Post returnedObj) {
			super.updateData(returnedObj);

			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumPostsParams(topicId, currentPage));
			if (cursor.moveToFirst()) {
				postsCursorAdapter.changeCursor(cursor);
				postsCursorAdapter.notifyDataSetChanged();
			} else {
				showToast("Internal error");
			}

			need2update= false;
			// unlock page changing
			pageIndicatorView.setEnabled(true);
			pageIndicatorView.activateCurrentPage(currentPage);
		}
	}

}
