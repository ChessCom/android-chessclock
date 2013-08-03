package com.chess.ui.fragments.forums;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ForumPostItem;
import com.chess.backend.entity.new_api.VacationItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.SaveForumPostsTask;
import com.chess.ui.adapters.ForumPostsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.PageIndicatorView;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 6:43
 */
public class ForumPostsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		PageIndicatorView.PagerFace, TextView.OnEditorActionListener, ItemClickListenerFace {


	private static final String TOPIC_ID = "topic_id";
	public static final String P_TAG_OPEN = "<p>";
	public static final String P_TAG_CLOSE = "</p>";
	private static final long KEYBOARD_DELAY = 100;
	private int topicId;
	private ForumPostsCursorAdapter postsCursorAdapter;
	private SavePostsListener savePostsListener;
	private PostsUpdateListener postsUpdateListener;
	private TextView forumHeaderTxt;
	private String topicTitle;
	private PageIndicatorView pageIndicatorView;
	private int currentPage;
	private int pagesToShow;
	private View replyView;
	private EditText topicBodyEdt;
	private int paddingSide;
	private TopicCreateListener topicCreateListener;
	private String topicUrl;

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

		postsCursorAdapter = new ForumPostsCursorAdapter(this, null);
		savePostsListener = new SavePostsListener();
		postsUpdateListener = new PostsUpdateListener();
		topicCreateListener = new TopicCreateListener();

		paddingSide = getResources().getDimensionPixelSize(R.dimen.default_scr_side_padding);

		Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumTopicById(topicId));
		cursor.moveToFirst();
		topicTitle = DBDataManager.getString(cursor, DBConstants.V_TITLE);
		topicUrl = DBDataManager.getString(cursor, DBConstants.V_URL);

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

		replyView = view.findViewById(R.id.replyView);
		topicBodyEdt = (EditText) view.findViewById(R.id.topicBodyEdt);
		topicBodyEdt.setOnEditorActionListener(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		forumHeaderTxt.setText(topicTitle);

		requestPage(currentPage);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(TOPIC_ID, topicId);
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

		if (view.getId() == R.id.quoteTxt) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);

			Cursor cursor = (Cursor) postsCursorAdapter.getItem(position);
			String username = DBDataManager.getString(cursor, DBConstants.V_USERNAME);
			String body = DBDataManager.getString(cursor, DBConstants.V_DESCRIPTION);

			replyView.setVisibility(View.VISIBLE);
			replyView.setBackgroundResource(R.color.header_light);
			replyView.setPadding(paddingSide, paddingSide, paddingSide, paddingSide);

			// add quote text
			String quote = "<div class=\"fquote\"><span class=\"quoted-user\">" + getString(R.string.username_wrote, username) +"</span>" +
					"<div class=\"quoted-text\">\n" +
					"<p>" + body + "</p>\n" +
					"</div></div>";
			topicBodyEdt.setText(quote);
			topicBodyEdt.setSelection(topicBodyEdt.getText().length());
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					topicBodyEdt.requestFocus();
					showKeyBoard(topicBodyEdt);
					showKeyBoardImplicit(topicBodyEdt);
				}
			}, KEYBOARD_DELAY);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		replyView.setVisibility(View.VISIBLE);
		replyView.setBackgroundResource(R.color.header_light);
		replyView.setPadding(paddingSide, paddingSide, paddingSide, paddingSide);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				topicBodyEdt.requestFocus();
				showKeyBoard(topicBodyEdt);
				showKeyBoardImplicit(topicBodyEdt);
			}
		}, KEYBOARD_DELAY);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_share:
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this topic - "
						+ RestHelper.BASE_URL + "/" + topicUrl);
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
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
		requestPage(page - 1);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.FLAG_EDITOR_ACTION
				|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			if (!AppUtils.isNetworkAvailable(getActivity())) { // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			} else {
				createPost();
			}
		}
		return false;
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

			new SaveForumPostsTask(savePostsListener, returnedObj.getData().getPosts(), getContentResolver(), topicId, currentPage).executeTask();
		}
	}

	private class SavePostsListener extends ChessUpdateListener<ForumPostItem.Post> {

		@Override
		public void updateData(ForumPostItem.Post returnedObj) {
			super.updateData(returnedObj);

			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumPostsById(topicId, currentPage));
			if (cursor.moveToFirst()) {
				postsCursorAdapter.changeCursor(cursor);
				postsCursorAdapter.notifyDataSetChanged();
			} else {
				showToast("Internal error");
			}

			// unlock page changing
			pageIndicatorView.setEnabled(true);
			pageIndicatorView.activateCurrentPage(currentPage);
		}
	}

	private void createPost() {
		String body = getTextFromField(topicBodyEdt);
		if (TextUtils.isEmpty(body)) {
			topicBodyEdt.requestFocus();
			topicBodyEdt.setError(getString(R.string.can_not_be_empty));
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_FORUMS_COMMENTS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_PARENT_TOPIC_ID, topicId);
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
				showToast(R.string.post_created);
			} else {
				showToast(R.string.error);
			}
			replyView.setVisibility(View.GONE);
			topicBodyEdt.setText(StaticData.SYMBOL_EMPTY);

			// update page
			requestPage(currentPage);
			// lock page changing
			pageIndicatorView.setEnabled(false);

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					hideKeyBoard(topicBodyEdt);
					hideKeyBoard();

				}
			}, KEYBOARD_DELAY);
		}
	}
}
