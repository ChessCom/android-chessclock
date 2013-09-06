package com.chess.ui.fragments.videos;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonCommentItem;
import com.chess.backend.entity.api.PostCommentItem;
import com.chess.backend.entity.api.VideoViewedItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.ui.adapters.CommentsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class VideoDetailsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	public static final String ITEM_ID = "item_id";

	public static final String GREY_COLOR_DIVIDER = "##";
	private static final int WATCH_VIDEO_REQUEST = 9897;
	public static final String P_TAG_OPEN = "<p>";
	public static final String P_TAG_CLOSE = "</p>";
	private static final long KEYBOARD_DELAY = 100;
	private static final long NON_EXIST = -1;

	// 11/15/12 | 27 min
	protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

	protected View loadingView;
	protected TextView emptyView;

	protected TextView authorTxt;
	protected ProgressImageView videoBackImg;
	protected ProgressImageView authorImg;
	protected TextView titleTxt;
	protected ImageView countryImg;
	protected TextView dateTxt;
	protected TextView contentTxt;
	protected TextView playBtnTxt;
	protected long videoId;
	protected String videoUrl;
	protected int currentPlayingId;
	private View replyView;
	private EditText newPostEdt;
	private CommentsUpdateListener commentsUpdateListener;
	private CommentsCursorAdapter commentsCursorAdapter;
	private int paddingSide;
	private CommentPostListener commentPostListener;
	private int imgSize;
	private SparseArray<String> countryMap;
	private EnhancedImageDownloader imageDownloader;
	private long commentId;
	private boolean inEditMode;
	private String bodyStr;
	private String commentForEditStr;

	public static VideoDetailsFragment createInstance(long videoId) {
		VideoDetailsFragment frag = new VideoDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(ITEM_ID, (int) videoId);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			videoId = getArguments().getLong(ITEM_ID);
		} else {
			videoId = savedInstanceState.getLong(ITEM_ID);
		}

		Resources resources = getResources();
		imgSize = (int) (40 * resources.getDisplayMetrics().density);

		String[] countryNames = resources.getStringArray(R.array.new_countries);
		int[] countryCodes = resources.getIntArray(R.array.new_country_ids);
		countryMap = new SparseArray<String>();
		for (int i = 0; i < countryNames.length; i++) {
			countryMap.put(countryCodes[i], countryNames[i]);
		}
		imageDownloader = new EnhancedImageDownloader(getActivity());

		commentsUpdateListener = new CommentsUpdateListener();
		commentsCursorAdapter = new CommentsCursorAdapter(getActivity(), null);

		paddingSide = getResources().getDimensionPixelSize(R.dimen.default_scr_side_padding);
		commentPostListener = new CommentPostListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_article_details_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.videos);

		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_video_details_header_frame, null, false);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setDivider(null);
		listView.setDividerHeight(0);
		listView.addHeaderView(headerView);
		listView.setAdapter(commentsCursorAdapter);
		listView.setOnItemClickListener(this);

		replyView = view.findViewById(R.id.replyView);
		newPostEdt = (EditText) view.findViewById(R.id.newPostEdt);

		videoBackImg = (ProgressImageView) view.findViewById(R.id.videoBackImg);
		titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		authorImg = (ProgressImageView) view.findViewById(R.id.thumbnailAuthorImg);
		countryImg = (ImageView) view.findViewById(R.id.countryImg);
		dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		contentTxt = (TextView) view.findViewById(R.id.contentTxt);
		authorTxt = (TextView) view.findViewById(R.id.authorTxt);

		playBtnTxt = (TextView) view.findViewById(R.id.playBtn);
		playBtnTxt.setOnClickListener(this);
		playBtnTxt.setEnabled(false);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_edit, true);
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		updateData();

		updateComments();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == WATCH_VIDEO_REQUEST) {

			VideoViewedItem item = new VideoViewedItem(currentPlayingId, getUsername(), true);
			DbDataManager.saveVideoViewedState(getContentResolver(), item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(ITEM_ID, videoId);
	}

	protected void updateData() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getVideoById(videoId));
		if (cursor.moveToFirst()) {
			playBtnTxt.setEnabled(true);

			int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
			String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
			CharSequence chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
			String lastName = DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
			CharSequence authorStr;
			if (TextUtils.isEmpty(chessTitle)) {
				authorStr = firstName + StaticData.SYMBOL_SPACE + lastName;
			} else {
				authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
						+ StaticData.SYMBOL_SPACE + firstName + StaticData.SYMBOL_SPACE + lastName;
			}

			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, new ForegroundColorSpan(lightGrey));
			authorTxt.setText(authorStr);

			titleTxt.setText(DbDataManager.getString(cursor, DbScheme.V_TITLE));
			imageDownloader.download(DbDataManager.getString(cursor, DbScheme.V_USER_AVATAR), authorImg, imgSize);

			Drawable drawable = AppUtils.getCountryFlagScaled(getActivity(), countryMap.get(DbDataManager.getInt(cursor, DbScheme.V_COUNTRY_ID)));
			countryImg.setImageDrawable(drawable);

			int duration = DbDataManager.getInt(cursor, DbScheme.V_MINUTES);
			dateTxt.setText(dateFormatter.format(new Date(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE)))
					+ StaticData.SYMBOL_SPACE + getString(R.string.min_arg, duration));

			bodyStr = DbDataManager.getString(cursor, DbScheme.V_DESCRIPTION);
			contentTxt.setText(Html.fromHtml(bodyStr));
			videoUrl = DbDataManager.getString(cursor, DbScheme.V_URL);

			currentPlayingId =  DbDataManager.getInt(cursor, DbScheme.V_ID);

			boolean videoViewed = DbDataManager.isVideoViewed(getActivity(), getUsername(), currentPlayingId);
			if (videoViewed) {
				playBtnTxt.setText(R.string.ic_check);
			} else {
				playBtnTxt.setText(R.string.ic_play);
			}
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.playBtn) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(videoUrl), "video/*");
			startActivityForResult(Intent.createChooser(intent, getString(R.string.select_player)), WATCH_VIDEO_REQUEST);
		}
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
				String articleShareStr;
//				if (TextUtils.isEmpty(url)) {
					articleShareStr = String.valueOf(Html.fromHtml(bodyStr));
//				} else {
//					articleShareStr = RestHelper.BASE_URL + "/" + url;
//				}

				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this article - "
						+ StaticData.SYMBOL_NEW_STR + articleShareStr);
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_article)));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// get commentId
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
		if (username.equals(getUsername())) {
			commentId = DbDataManager.getLong(cursor, DbScheme.V_ID);
			commentForEditStr = String.valueOf(Html.fromHtml(DbDataManager.getString(cursor, DbScheme.V_BODY)));

			inEditMode = true;
			showEditView(true);
		}
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
					newPostEdt.setText(StaticData.SYMBOL_EMPTY);
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

	private void updateComments() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VIDEOS_COMMENTS(videoId));

		new RequestJsonTask<CommonCommentItem>(commentsUpdateListener).executeTask(loadItem);
	}

	private class CommentsUpdateListener extends ChessUpdateListener<CommonCommentItem> {

		private CommentsUpdateListener() {
			super(CommonCommentItem.class);
		}

		@Override
		public void updateData(CommonCommentItem returnedObj) {
			super.updateData(returnedObj);

			DbDataManager.updateVideoCommentsToDb(getContentResolver(), returnedObj, videoId);

			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getVideoCommentsById(videoId));
			if (cursor != null && cursor.moveToFirst()) {
				commentsCursorAdapter.changeCursor(cursor);
			}
		}
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
		if (commentId == NON_EXIST) {
			loadItem.setLoadPath(RestHelper.CMD_VIDEOS_COMMENTS(videoId));
			loadItem.setRequestMethod(RestHelper.POST);
		} else {
			loadItem.setLoadPath(RestHelper.CMD_VIDEOS_EDIT_COMMENT(videoId, commentId));
			loadItem.setRequestMethod(RestHelper.PUT);
		}
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_COMMENT_BODY, P_TAG_OPEN + body + P_TAG_CLOSE);

		new RequestJsonTask<PostCommentItem>(commentPostListener).executeTask(loadItem);
	}

	private class CommentPostListener extends ChessLoadUpdateListener<PostCommentItem> {

		private CommentPostListener() {
			super(PostCommentItem.class);
		}

		@Override
		public void updateData(PostCommentItem returnedObj) {
			if(returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
				showToast(R.string.post_created);
			} else {
				showToast(R.string.error);
			}
			showEditView(false);

			updateComments();
		}
	}
}
