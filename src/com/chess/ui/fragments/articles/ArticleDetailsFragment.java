package com.chess.ui.fragments.articles;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ArticleDetailsItem;
import com.chess.backend.entity.api.CommonCommentItem;
import com.chess.backend.entity.api.CommonViewedItem;
import com.chess.backend.entity.api.PostCommentItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.GameDiagramItem;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CommentsCursorAdapter;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.game.GameDiagramFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class ArticleDetailsFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	public static final String ITEM_ID = "item_id";
	public static final String GREY_COLOR_DIVIDER = "##";
	public static final String P_TAG_OPEN = "<p>";
	public static final String P_TAG_CLOSE = "</p>";
	private static final long KEYBOARD_DELAY = 100;
	public static final int DIAGRAM_PREFIX = 0x00009000;
	public static final int IMAGE_PREFIX = 0x0000A000;

	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
	public static final String SLASH_DIVIDER = " | ";
	private static final long NON_EXIST = -1;
	private static final long READ_DELAY = 2 * 1000;
	public static final String NO_ITEM_IMAGE = "no_item_image";
	public static final String DIAGRAM_START_TAG = "<!-- CHESS_COM_DIAGRAM";
	private static final int ID_POSITION = 1;

	private TextView authorTxt;

	private TextView titleTxt;
	private ProgressImageView articleImg;
	private ProgressImageView authorImg;
	private ImageView countryImg;
	private TextView dateTxt;
	private TextView contentTxt;
	private long articleId;
	private CommentsUpdateListener commentsUpdateListener;
	private CommentsCursorAdapter commentsCursorAdapter;
	private ArticleUpdateListener articleUpdateListener;
	private EnhancedImageDownloader imageDownloader;
	private int imgSize;
	private SparseArray<String> countryMap;
	private int widthPixels;
	private View replyView;
	private EditText newPostEdt;
	private int paddingSide;
	private CommentPostListener commentPostListener;
	private String url;
	private String bodyStr;
	private long commentId;
	private boolean inEditMode;
	private String commentForEditStr;
	private View loadingCommentsView;
	private LinearLayout complexContentLinLay;
	private float density;
	private List<Integer> diagramIdsList;
	private List<ArticleDetailsItem.Diagram> diagramsList;

	public static ArticleDetailsFragment createInstance(long articleId) {
		ArticleDetailsFragment frag = new ArticleDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(ITEM_ID, articleId);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			articleId = getArguments().getLong(ITEM_ID);
		} else {
			articleId = savedInstanceState.getLong(ITEM_ID);
		}
		Resources resources = getResources();
		density = resources.getDisplayMetrics().density;
		imgSize = (int) (40 * density);

		widthPixels = resources.getDisplayMetrics().widthPixels;
		diagramIdsList = new ArrayList();

		String[] countryNames = resources.getStringArray(R.array.new_countries);
		int[] countryCodes = resources.getIntArray(R.array.new_country_ids);
		countryMap = new SparseArray<String>();
		for (int i = 0; i < countryNames.length; i++) {
			countryMap.put(countryCodes[i], countryNames[i]);
		}
		imageDownloader = new EnhancedImageDownloader(getActivity());

		articleUpdateListener = new ArticleUpdateListener();
		commentsUpdateListener = new CommentsUpdateListener();
		commentsCursorAdapter = new CommentsCursorAdapter(getActivity(), null);

		paddingSide = getResources().getDimensionPixelSize(R.dimen.default_scr_side_padding);
		commentPostListener = new CommentPostListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_details_comments_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_article_details_header_frame, null, false);

		loadingView = view.findViewById(R.id.loadingView);
		loadingCommentsView = headerView.findViewById(R.id.loadingCommentsView);

		titleTxt = (TextView) headerView.findViewById(R.id.titleTxt);
		articleImg = (ProgressImageView) headerView.findViewById(R.id.articleImg);
		authorImg = (ProgressImageView) headerView.findViewById(R.id.thumbnailAuthorImg);
		countryImg = (ImageView) headerView.findViewById(R.id.countryImg);
		dateTxt = (TextView) headerView.findViewById(R.id.dateTxt);
		contentTxt = (TextView) headerView.findViewById(R.id.contentTxt);
		contentTxt.setMovementMethod(LinkMovementMethod.getInstance());
		authorTxt = (TextView) headerView.findViewById(R.id.authorTxt);

		complexContentLinLay = (LinearLayout) headerView.findViewById(R.id.complexContentLinLay);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(commentsCursorAdapter);
		listView.setOnItemClickListener(this);

		replyView = view.findViewById(R.id.replyView);
		newPostEdt = (EditText) view.findViewById(R.id.newPostEdt);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_edit, true);
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		loadFromDb();

		if (AppUtils.isNetworkAvailable(getActivity())) {
			// get full body text from server
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLE_BY_ID(articleId));

			new RequestJsonTask<ArticleDetailsItem>(articleUpdateListener).executeTask(loadItem);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(markAsReadRunnable);

		// remove diagram fragments
		for (Integer diagramId : diagramIdsList) {
			Fragment fragmentById = getChildFragmentManager().findFragmentById(DIAGRAM_PREFIX + diagramId);
			if (fragmentById != null) {
				FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
				transaction.remove(fragmentById).commit();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(ITEM_ID, articleId);
	}

	private void loadFromDb() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getArticleById(articleId));

		if (cursor.moveToFirst()) {
			int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
			String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
			CharSequence chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
			String lastName = DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
			CharSequence authorStr;
			if (TextUtils.isEmpty(chessTitle)) {
				authorStr = firstName + Symbol.SPACE + lastName;
			} else {
				authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
						+ Symbol.SPACE + firstName + Symbol.SPACE + lastName;
				authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, new ForegroundColorSpan(lightGrey));
			}
			authorTxt.setText(authorStr);

			try {
				url = cursor.getString(cursor.getColumnIndexOrThrow(DbScheme.V_URL));
			} catch (IllegalArgumentException ex) {
				url = Symbol.EMPTY;
			}
			titleTxt.setText(Html.fromHtml(DbDataManager.getString(cursor, DbScheme.V_TITLE)));
			String authorImgUrl = DbDataManager.getString(cursor, DbScheme.V_USER_AVATAR);
			imageDownloader.download(authorImgUrl, authorImg, imgSize);

			String photoUrl = DbDataManager.getString(cursor, DbScheme.V_PHOTO_URL);
			// Change main article Image params
			int imageHeight = (int) (widthPixels * 0.6671f);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthPixels, imageHeight);
			articleImg.setLayoutParams(params);

			// Change ProgressBar params
			RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			articleImg.getProgressBar().setLayoutParams(progressParams);

			if (photoUrl.contains(NO_ITEM_IMAGE)) {
				imageDownloader.download(authorImgUrl, articleImg, widthPixels);
			} else {
				imageDownloader.download(photoUrl, articleImg, widthPixels);
			}

			Drawable drawable = AppUtils.getCountryFlagScaled(getActivity(), countryMap.get(DbDataManager.getInt(cursor, DbScheme.V_COUNTRY_ID)));
			countryImg.setImageDrawable(drawable);

			dateTxt.setText(dateFormatter.format(new Date(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE))));
			bodyStr = DbDataManager.getString(cursor, DbScheme.V_BODY);
			checkDiagramInBody(bodyStr);
			contentTxt.setText(Html.fromHtml(bodyStr));

			diagramsList = DbDataManager.getArticleDiagramItemFromDb(getContentResolver(), getUsername());
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
				if (TextUtils.isEmpty(url)) {
					articleShareStr = String.valueOf(Html.fromHtml(bodyStr));
				} else {
					articleShareStr = "http://chess.com/article/view/" + url;
				}

				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this article - "
						+ Symbol.NEW_STR + articleShareStr);
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_article)));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		for (Integer diagramId : diagramIdsList) {
			int clickedId = id - IMAGE_PREFIX;
			if (diagramId == clickedId) {

				view.setVisibility(View.GONE);
				ArticleDetailsItem.Diagram diagramToShow = null;
				for (ArticleDetailsItem.Diagram diagram : diagramsList) {
					if (diagram.getDiagramId() == diagramId) {
						diagramToShow = diagram;
						break;
					}
				}

				GameDiagramItem diagramItem = new GameDiagramItem();
				diagramItem.setUserColor(ChessBoard.WHITE_SIDE);
				if (diagramToShow.getType() == ArticleDetailsItem.Diagram.PROBLEM) {
					diagramItem.setMovesList(diagramToShow.getMoveList());
				} else {
					diagramItem.setFen(diagramToShow.getFen());
				}

				GameDiagramFragment fragment = GameDiagramFragment.createInstance(diagramItem);
				FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
				transaction.replace(DIAGRAM_PREFIX + clickedId, fragment, fragment.getClass().getSimpleName());
				transaction.addToBackStack(fragment.getClass().getSimpleName());
				transaction.commit();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position != 0) { // if NOT listView header
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
	}

	private Runnable markAsReadRunnable = new Runnable() {
		@Override
		public void run() {
			if (getActivity() == null) {
				return;
			}
			CommonViewedItem item = new CommonViewedItem(articleId, getUsername());
			DbDataManager.saveArticleViewedState(getContentResolver(), item);
		}
	};

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

	private class ArticleUpdateListener extends ChessUpdateListener<ArticleDetailsItem> {

		private ArticleUpdateListener() {
			super(ArticleDetailsItem.class);
		}

		@Override
		public void updateData(ArticleDetailsItem returnedObj) {
			super.updateData(returnedObj);

			ArticleDetailsItem.Data articleData = returnedObj.getData();
			int commentCount = articleData.getCommentCount();
			int viewCount = articleData.getViewCount();
			url = articleData.getUrl();

			String commentsCntStr = getString(R.string.comments_arg, commentCount);
			String viewsCntStr = getString(R.string.views_arg, viewCount);
			CharSequence text = dateTxt.getText();
			dateTxt.setText(text + SLASH_DIVIDER + viewsCntStr + SLASH_DIVIDER + commentsCntStr);

			String bodyStr = articleData.getBody();
			checkDiagramInBody(bodyStr);

			contentTxt.setText(Html.fromHtml(bodyStr));

			DbDataManager.saveArticleItem(getContentResolver(), articleData);

			diagramsList = articleData.getDiagrams();
			if (diagramsList != null) {
				for (ArticleDetailsItem.Diagram diagram : diagramsList) {
					DbDataManager.saveArticlesDiagramItem(getContentResolver(), diagram);
				}
			}
			updateComments();

			handler.postDelayed(markAsReadRunnable, READ_DELAY);
		}
	}

	private void checkDiagramInBody(String bodyStr) {
		if (bodyStr.contains(DIAGRAM_START_TAG)) {

			Resources resources = getResources();
			int textColor = resources.getColor(R.color.new_subtitle_dark_grey);
			int textSize = (int) (resources.getDimensionPixelSize(R.dimen.content_text_size) / density);
			int paddingSide = resources.getDimensionPixelSize(R.dimen.default_scr_side_padding);
			// we go through the article body and divide it to parts
			String[] parts = bodyStr.split(DIAGRAM_START_TAG);
			// hide simple container
			contentTxt.setVisibility(View.GONE);

			// show complex container
			complexContentLinLay.setVisibility(View.VISIBLE);

			// divide text and add corresponding views: TextView for text part and image for diagram part
			for (String part : parts) {
				if (part.contains("chess_com_diagram")) {
					String diagramPart = part.substring(part.indexOf("<div "));
					String partAfterDiagram = diagramPart.substring(diagramPart.indexOf("</div>") + "</div>".length());

					String diagramId = diagramPart.substring(diagramPart.indexOf("id=\"chess_com_diagram_") + "id=\"chess_com_diagram_".length());
					diagramId = diagramId.substring(0, diagramId.indexOf("\" class"));
					String[] diagramIdParts = diagramId.split("_");
					int id = Integer.parseInt(diagramIdParts[ID_POSITION]);
					diagramIdsList.add(id);

					FrameLayout frameLayout = new FrameLayout(getActivity());
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					frameLayout.setId(DIAGRAM_PREFIX + id);
					frameLayout.setLayoutParams(params);

					complexContentLinLay.addView(frameLayout);

					ImageView imageView = new ImageView(getActivity());
					imageView.setImageResource(R.drawable.board_white_grey_full_size);
					imageView.setId(IMAGE_PREFIX + id);

					// set click handler and then get this tag from view to show diagram
					imageView.setOnClickListener(this);

					frameLayout.addView(imageView);

					RoboTextView textView = new RoboTextView(getActivity());
					textView.setTextSize(textSize);
					textView.setTextColor(textColor);
					textView.setText(Html.fromHtml(partAfterDiagram));
					textView.setPadding(paddingSide, paddingSide, paddingSide, 0);

					complexContentLinLay.addView(textView);

				} else {
					RoboTextView textView = new RoboTextView(getActivity());
					textView.setTextSize(textSize);
					textView.setTextColor(textColor);
					textView.setText(Html.fromHtml(part));
					textView.setPadding(paddingSide, 0, paddingSide, 0);

					complexContentLinLay.addView(textView);
				}
			}

//			String[] parts = bodyStr.split("<!-- CHESS_COM_DIAGRAM <div style=\"text-align:center;\"> -->");

/*
	///////////////////////////
	// simpleDiagram Example //
	///////////////////////////

	&-diagramtype: simpleDiagram
	&-colorscheme: wooddark
	&-piecestyle: book
	&-float: left
	&-flip: false
	&-prompt: false
	&-coords: false
	&-size: 45
	&-lastmove:
	&-focusnode:
	&-beginnode:
	&-endnode:
	&-pgnbody:
	[Date "????.??.??"]
	[Result "*"]
	[FEN "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"] *
*/
			GameDiagramItem diagramItem = new GameDiagramItem();
			diagramItem.setFen("rnbqkbnr/pppp1ppp/4p3/8/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 1");
			diagramItem.setUserColor(ChessBoard.WHITE_SIDE);


//			GameDiagramFragment fragment = GameDiagramFragment.createInstance(diagramItem);
//			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//			transaction.replace(R.id.diagramContainerView, fragment, fragment.getClass().getSimpleName());
//			transaction.addToBackStack(fragment.getClass().getSimpleName());
//			transaction.commit();
		} else {
			complexContentLinLay.setVisibility(View.GONE);
			contentTxt.setVisibility(View.VISIBLE);
		}
	}


	private void updateComments() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLE_COMMENTS(articleId));

		new RequestJsonTask<CommonCommentItem>(commentsUpdateListener).executeTask(loadItem);
	}

	private class CommentsUpdateListener extends ChessUpdateListener<CommonCommentItem> {

		private CommentsUpdateListener() {
			super(CommonCommentItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showCommentsLoadingView(show);
		}

		@Override
		public void updateData(CommonCommentItem returnedObj) {
			super.updateData(returnedObj);

			DbDataManager.updateArticleCommentToDb(getContentResolver(), returnedObj, articleId);

			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getArticlesCommentsById(articleId));
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
			loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLE_COMMENTS(articleId));
			loadItem.setRequestMethod(RestHelper.POST);
		} else {
			loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLE_EDIT_COMMENT(articleId, commentId));
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
			if (returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
				showToast(R.string.post_created);
			} else {
				showToast(R.string.error);
			}
			showEditView(false);

			updateComments();
		}
	}

	private void showCommentsLoadingView(boolean show) {
		loadingCommentsView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
