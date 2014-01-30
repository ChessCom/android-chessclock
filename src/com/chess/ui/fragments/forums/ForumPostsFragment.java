package com.chess.ui.fragments.forums;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ArticleDetailsItem;
import com.chess.backend.entity.api.ForumPostItem;
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.image_load.bitmapfun.DiagramImageProcessor;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveForumPostsTask;
import com.chess.model.GameDiagramItem;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ForumPostsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.articles.ArticleDetailsFragment;
import com.chess.ui.fragments.diagrams.GameDiagramFragment;
import com.chess.ui.fragments.diagrams.GameDiagramFragmentTablet;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.PageIndicatorView;
import com.chess.widgets.LinLayout;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 6:43
 */
public class ForumPostsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		PageIndicatorView.PagerFace, ItemClickListenerFace {

	private static final String TOPIC_ID = "topic_id";
	public static final String P_TAG_OPEN = "<p>";
	public static final String P_TAG_CLOSE = "</p>";
	private static final long KEYBOARD_DELAY = 100;
	private static final long NON_EXIST = -1;
	private static final String IMAGE_CACHE_DIR = "diagrams";
	private int topicId;

	private ForumPostsCursorAdapter postsCursorAdapter;
	private SavePostsListener savePostsListener;
	private PostsUpdateListener postsUpdateListener;
	private TextView forumHeaderTxt;
	private String topicTitle;
	private PageIndicatorView pageIndicatorView;
	private DiagramImageProcessor diagramImageProcessor;
	private int currentPage;
	private int pagesToShow;
	private View replyView;
	private EditText newPostEdt;
	private int paddingSide;
	private TopicCreateListener topicCreateListener;
	private String topicUrl;
	private long commentId;
	private boolean inEditMode;
	private String commentForEditStr;

	public ForumPostsFragment() { }

	public static ForumPostsFragment createInstance(int topicId){
		ForumPostsFragment fragment = new ForumPostsFragment();
		fragment.topicId = topicId;
		Bundle bundle = new Bundle();
		bundle.putInt(TOPIC_ID, topicId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			topicId = getArguments().getInt(TOPIC_ID);
		} else {
			topicId = savedInstanceState.getInt(TOPIC_ID);
		}

		init();

		pullToRefresh(true);
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_forum_posts_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.forums);

		widgetsInit(view);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_edit, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		forumHeaderTxt.setText(Html.fromHtml(topicTitle));

		requestPage(currentPage);

		diagramImageProcessor.setExitTasksEarly(false);
	}

	@Override
	public void onPause() {
		super.onPause();

		diagramImageProcessor.setPauseWork(false);
		diagramImageProcessor.setExitTasksEarly(true);
		diagramImageProcessor.flushCache();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(TOPIC_ID, topicId);
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		requestPage(currentPage);
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

		LoadItem loadItem = LoadHelper.getForumPostsForTopic(getUserToken(), topicId, page);

		new RequestJsonTask<ForumPostItem>(postsUpdateListener).executeTask(loadItem);
		// lock page changing
		pageIndicatorView.setEnabled(false);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.quoteTxt) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);

			Cursor cursor = (Cursor) postsCursorAdapter.getItem(position);
			String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
			String body = DbDataManager.getString(cursor, DbScheme.V_DESCRIPTION);

			replyView.setVisibility(View.VISIBLE);
			replyView.setBackgroundResource(R.color.header_light);
			replyView.setPadding(paddingSide, paddingSide, paddingSide, paddingSide);

			// add quote text
			String quote = "<div class=\"fquote\"><span class=\"quoted-user\">" + getString(R.string.username_wrote, username) +"</span>" +
					"<div class=\"quoted-text\">\n" +
					"<p>" + body + "</p>\n" +
					"</div></div>";
			newPostEdt.setText(quote);
			newPostEdt.setSelection(newPostEdt.getText().length());
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					newPostEdt.requestFocus();
					showKeyBoard(newPostEdt);
					showKeyBoardImplicit(newPostEdt);

					showEditView(true);

				}
			}, KEYBOARD_DELAY);
		} else if (id == ArticleDetailsFragment.IMAGE_PREFIX || id == ArticleDetailsFragment.ICON_PREFIX) {
			//get diagram from view tag
			Object tag = view.getTag(R.id.list_item_id);
			if (tag instanceof GameDiagramItem) {
				final GameDiagramItem diagramItem = (GameDiagramItem) tag;
				showDiagramAnimated(diagramItem);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// get commentId
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		if (cursor == null) { // TODO investigate when it might happen
			return;
		}
		String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
		if (username.equals(getUsername())) {
			commentId = DbDataManager.getLong(cursor, DbScheme.V_COMMENT_ID);

			commentForEditStr = String.valueOf(Html.fromHtml(DbDataManager.getString(cursor, DbScheme.V_DESCRIPTION)));

			inEditMode = true;
			showEditView(true);
		}
	}

	private boolean showDiagramAnimated(final GameDiagramItem diagramItem) {
		// don't handle clicks on simple diagrams
		if (diagramItem.getDiagramType().equals(ArticleDetailsItem.Diagram.SIMPLE)) {
			return false;
		}

		diagramItem.setShowAnimation(true);
		if (!isTablet) {
			getActivityFace().openFragment(GameDiagramFragment.createInstance(diagramItem));
		} else {
			getActivityFace().openFragment(GameDiagramFragmentTablet.createInstance(diagramItem));
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_cancel:
				showEditView(false);

				return true;
			case R.id.menu_accept:
				if (inEditMode) {
					createPost(commentId);
				} else {
					createPost();
				}
				return true;
			case R.id.menu_edit:
				showEditView(true);
				return true;
			case R.id.menu_share:
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this topic - "
						+ RestHelper.getInstance().BASE_URL + "/" + topicUrl);
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showEditView(boolean show) {
		if (show) {
			replyView.setVisibility(View.VISIBLE);
			replyView.setBackgroundResource(R.color.header_light);
			replyView.setPadding(paddingSide, paddingSide, paddingSide, paddingSide);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					newPostEdt.requestFocus();
					showKeyBoard(newPostEdt);
					showKeyBoardImplicit(newPostEdt);

					if (inEditMode) {
						newPostEdt.setText(commentForEditStr);
						newPostEdt.setSelection(commentForEditStr.length());
					}
					showEditMode(true);
				}
			}, KEYBOARD_DELAY);
		} else {

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					hideKeyBoard(newPostEdt);
					hideKeyBoard();

					replyView.setVisibility(View.GONE);
					newPostEdt.setText(Symbol.EMPTY);
				}
			}, KEYBOARD_DELAY);

			showEditMode(false);
			inEditMode = false;
		}
	}

	private void showEditMode(boolean show) {
		getActivityFace().showActionMenu(R.id.menu_share, !show);
		getActivityFace().showActionMenu(R.id.menu_edit, !show);
		getActivityFace().showActionMenu(R.id.menu_cancel, show);
		getActivityFace().showActionMenu(R.id.menu_accept, show);

		getActivityFace().updateActionBarIcons();
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

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private class PostsUpdateListener extends ChessUpdateListener<ForumPostItem>{

		private PostsUpdateListener() {
			super(ForumPostItem.class);
		}

		@Override
		public void updateData(ForumPostItem returnedObj) {
			pagesToShow = (int) Math.ceil((returnedObj.getData().getCommentsCount() / (float) (RestHelper.DEFAULT_ITEMS_PER_PAGE)));
			pageIndicatorView.setTotalPageCnt(pagesToShow);
			if (currentPage == pagesToShow - 1) {
				pageIndicatorView.enableRightBtn(false);
			} else {
				pageIndicatorView.enableRightBtn(true);
			}

			new SaveForumPostsTask(savePostsListener, returnedObj.getData().getPosts(),
					getContentResolver(), topicId, currentPage).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);

			updateUiData();
		}
	}

	private class SavePostsListener extends ChessUpdateListener<ForumPostItem.Post> {

		@Override
		public void updateData(ForumPostItem.Post returnedObj) {
			super.updateData(returnedObj);

			updateUiData();
		}
	}

	private void updateUiData() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getForumPostsById(topicId, currentPage));
		if (cursor != null && cursor.moveToFirst()) {
			postsCursorAdapter.changeCursor(cursor);
		} else {
			showToast("Internal error");
		}

		// unlock page changing
		pageIndicatorView.setEnabled(true);
	}

	private void createPost() {
		createPost(NON_EXIST);
	}

	private void createPost(long commentId) {
		String body = getTextFromField(newPostEdt);
		if (TextUtils.isEmpty(body)) {
			newPostEdt.requestFocus();
			newPostEdt.setError(getString(R.string.can_not_be_empty));
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_FORUMS_COMMENTS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_FORUM_TOPIC_ID, topicId);
		if (commentId == NON_EXIST) {

		} else {
			loadItem.addRequestParams(RestHelper.P_COMMENT_ID, commentId);

		}

		loadItem.addRequestParams(RestHelper.P_BODY, P_TAG_OPEN + body + P_TAG_CLOSE);

		new RequestJsonTask<VacationItem>(topicCreateListener).executeTask(loadItem); // use Vacation item as a simple return obj to get status
	}

	private class TopicCreateListener extends ChessLoadUpdateListener<VacationItem> {

		private TopicCreateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			if(returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
				showToast(R.string.posted);
			} else {
				showToast(R.string.error);
			}
			showEditView(false);

			requestPage(currentPage);
		}
	}

	private void init() {
		{// set imageCache params for diagramProcessor
			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

			cacheParams.setMemCacheSizePercent(0.15f); // Set memory cache to 25% of app memory

			diagramImageProcessor = new DiagramImageProcessor(getActivity(), DiagramImageProcessor.DEFAULT);
			diagramImageProcessor.setLoadingImage(R.drawable.board_green_default);
			diagramImageProcessor.setNeedLoadingImage(false);
			diagramImageProcessor.setChangingDrawable(getResources().getDrawable(R.drawable.board_green_default));
			diagramImageProcessor.addImageCache(getFragmentManager(), cacheParams);
		}

		postsCursorAdapter = new ForumPostsCursorAdapter(this, null, getImageFetcher(), diagramImageProcessor);
		savePostsListener = new SavePostsListener();
		postsUpdateListener = new PostsUpdateListener();
		topicCreateListener = new TopicCreateListener();

		paddingSide = getResources().getDimensionPixelSize(R.dimen.default_scr_side_padding);

		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getForumTopicById(topicId));
		cursor.moveToFirst();
		topicTitle = DbDataManager.getString(cursor, DbScheme.V_TITLE);
		topicUrl = DbDataManager.getString(cursor, DbScheme.V_URL);
		cursor.close();


	}

	private void widgetsInit(View view) {
		// add headerView
		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_forum_header_view, null, false);
		forumHeaderTxt = (TextView) headerView.findViewById(R.id.forumHeaderTxt);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(postsCursorAdapter);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					diagramImageProcessor.setPauseWork(true);
				} else {
					diagramImageProcessor.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		LinLayout pageIndicatorLay = (LinLayout) view.findViewById(R.id.pageIndicatorLay);
		pageIndicatorView = (PageIndicatorView) view.findViewById(R.id.pageIndicatorView);
		pageIndicatorView.setPagerFace(this);

		replyView = view.findViewById(R.id.replyView);
		newPostEdt = (EditText) view.findViewById(R.id.newPostEdt);

		initUpgradeAndAdWidgets(view);

		if (!isNeedToUpgrade()) {// we need to bind to bottom if there is no ad banner
			((RelativeLayout.LayoutParams) pageIndicatorLay.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}
	}
}
