package com.chess.ui.fragments.articles;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.*;
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
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.GameDiagramItem;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CommentsCursorAdapter;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.game.GameDiagramFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.ControlledListView;
import com.chess.utilities.AppUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class ArticleDetailsFragment extends CommonLogicFragment implements ItemClickListenerFace,
		AdapterView.OnItemClickListener {

	public static final String ITEM_ID = "item_id";
	public static final String GREY_COLOR_DIVIDER = "##";
	public static final String P_TAG_OPEN = "<p>";
	public static final String P_TAG_CLOSE = "</p>";
	private static final long KEYBOARD_DELAY = 100;
	public static final int DIAGRAM_PREFIX = 0x00009000;
	public static final int IMAGE_PREFIX = 0x0000A000;
	public static final int TEXT_PREFIX = 0x0000B000;

	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
	public static final String SLASH_DIVIDER = " | ";
	private static final long NON_EXIST = -1;
	private static final long READ_DELAY = 2 * 1000;
	public static final String NO_ITEM_IMAGE = "no_item_image";
	public static final String DIAGRAM_START_TAG = "<!-- CHESS_COM_DIAGRAM";
	private static final int ID_POSITION = 1;
	private static final int ZERO = 0;
	private static final long DISAPPEAR_DURATION = 300;
	private static final long ANIMATION_SET_DURATION = 350;

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
	private int heightPixels;
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
	private HashMap<Integer, Boolean> activeIdsMap;
	private ControlledListView listView;
	private boolean diagramsLoaded;
	private int controlButtonHeight;
	private int actionBarHeight;
	private String[] contentParts;
	private int parsedPartCnt;

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
		heightPixels = resources.getDisplayMetrics().heightPixels;
		controlButtonHeight = (int) resources.getDimension(R.dimen.game_controls_button_diagram_height);
		actionBarHeight = resources.getDimensionPixelSize(R.dimen.actionbar_compat_height);

		diagramIdsList = new ArrayList<Integer>();
		activeIdsMap = new HashMap<Integer, Boolean>();

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

		widgetsInit(view);

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
			contentTxt.setText(Html.fromHtml(bodyStr));

			diagramsList = DbDataManager.getArticleDiagramItemFromDb(getContentResolver(), getUsername());
			loadDiagramsFromContent(bodyStr, diagramsList);
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

		if (id == TEXT_PREFIX + ZERO) { // if first text part in article
			// detect active diagram

			// deactivate previous diagram
			for (final Integer previousClickedId : activeIdsMap.keySet()) {
				// remove fragment if active
				if (activeIdsMap.get(previousClickedId)) {
					hideDiagramById(previousClickedId);
				}
			}

			// restore listView scrolling
			enableListViewScrolling(true);

			return;
		}

		// if clicked on image
		for (Integer diagramId : diagramIdsList) {
			final int clickedId = id - IMAGE_PREFIX;
			if (diagramId.equals(clickedId)) {

				// deactivate previous diagram
				for (Integer previousClickedId : activeIdsMap.keySet()) {
					// hide view
					if (activeIdsMap.get(previousClickedId)) {

						// remove fragment
						hideDiagramById(previousClickedId);
					}
				}

				activeIdsMap.put(clickedId, true);

				ArticleDetailsItem.Diagram diagramToShow = null;
				for (ArticleDetailsItem.Diagram diagram : diagramsList) {
					if (diagram.getDiagramId() == diagramId) {
						diagramToShow = diagram;
						break;
					}
				}

				final GameDiagramItem diagramItem = new GameDiagramItem();
				diagramItem.setUserColor(ChessBoard.WHITE_SIDE);
				if (diagramToShow.getType() == ArticleDetailsItem.Diagram.PUZZLE) {
					diagramItem.setMovesList(diagramToShow.getMoveList());
				} else if (diagramToShow.getType() == ArticleDetailsItem.Diagram.CHESS_GAME) {
					diagramItem.setMovesList(diagramToShow.getMoveList());
				} else if (diagramToShow.getType() == ArticleDetailsItem.Diagram.SIMPLE) {
					diagramItem.setFen(diagramToShow.getFen());
				} else { // non valid format
					return;
				}

				// hide image
				AnimatorSet animatorSet = new AnimatorSet();
				Animator imageAnimator = ObjectAnimator.ofFloat(view, "alpha", 1, 0f);
				imageAnimator.setDuration(DISAPPEAR_DURATION);
				ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f);
				ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f);

				animatorSet.play(imageAnimator).with(scaleX).with(scaleY);
				animatorSet.setDuration(ANIMATION_SET_DURATION);
				animatorSet.addListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animator) {

					}

					@Override
					public void onAnimationEnd(Animator animator) {
						GameDiagramFragment fragment = GameDiagramFragment.createInstance(diagramItem);
						FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
						transaction.replace(DIAGRAM_PREFIX + clickedId, fragment, fragment.getClass().getSimpleName());
						transaction.addToBackStack(fragment.getClass().getSimpleName());
						transaction.commit();
					}

					@Override
					public void onAnimationCancel(Animator animator) {

					}

					@Override
					public void onAnimationRepeat(Animator animator) {

					}
				});
				animatorSet.start();

				// scroll listView to the top of diagram view
				int[] locationCoordinates = new int[2];
				view.getLocationOnScreen(locationCoordinates);
				int fragmentDiagramHeight = controlButtonHeight + widthPixels;
				int viewYPosition = locationCoordinates[1];

//				int listViewScrollOffset = heightPixels - (viewYPosition + fragmentDiagramHeight);
//				logTest( " viewYPosition = " + viewYPosition +  " fragmentDiagramHeight = " + fragmentDiagramHeight
//						+ " heightPixels = " + heightPixels + " listViewScrollOffset = " + listViewScrollOffset);

				// if distance is positive then it scrolls to the top else to the bottom
				int spaceLeft = heightPixels + actionBarHeight - fragmentDiagramHeight;
				int topOptimalPosition = spaceLeft / 2; // 708/2 = 354
				int distance = viewYPosition - topOptimalPosition;

				listView.smoothScrollBy(distance, 200); // if distance is positive we scroll to the top

				// block listView scrolling
				enableListViewScrolling(false);

				return;
			}
		}

		// if clicked on text below first and other diagrams
		for (Integer diagramId : diagramIdsList) {
			int clickedId = id - TEXT_PREFIX;
			if (diagramId.equals(clickedId)) {

				// deactivate previous diagram
				for (Integer previousClickedId : activeIdsMap.keySet()) {
					// hide view
					if (activeIdsMap.get(previousClickedId)) {
						// remove fragment
						hideDiagramById(previousClickedId);
					}
				}

				// restore listView scrolling
				enableListViewScrolling(true);

				return;
			}
		}
	}

	private void enableListViewScrolling(boolean enable) {
		listView.setScrollingEnabled(enable);
	}

	private void hideDiagramById(Integer previousClickedId) {
		final Fragment fragmentById = getChildFragmentManager().findFragmentById(DIAGRAM_PREFIX + previousClickedId);

		if (fragmentById != null) {

			AnimatorSet animatorSet = new AnimatorSet();

			final View fragmentView = fragmentById.getView();
			AnimatorSet.Builder animationBuilder = null;
			if (fragmentView != null) {

				Animator fragmentAnimator = ObjectAnimator.ofFloat(fragmentView, "alpha", 1, 0);

				ObjectAnimator fragmentScaleX = ObjectAnimator.ofFloat(fragmentView, "scaleX", 1f, 0.8f);
				ObjectAnimator fragmentScaleY = ObjectAnimator.ofFloat(fragmentView, "scaleY", 1f, 0.8f);
				animationBuilder = animatorSet.play(fragmentAnimator).with(fragmentScaleX).with(fragmentScaleY);
			}

			// start image animation
			final ImageView imageView = (ImageView) getView().findViewById(IMAGE_PREFIX + previousClickedId);

			Animator imageAlphaAnimator = ObjectAnimator.ofFloat(imageView, "alpha", 0, 1);
			ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1.3f, 1f);
			ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.3f, 1f);


			if (animationBuilder != null) {
				animationBuilder.with(imageAlphaAnimator).with(scaleX).with(scaleY);
			} else {
				animatorSet.play(imageAlphaAnimator).with(scaleX).with(scaleY);
			}

			final AnimatorSet.Builder finalAnimationBuilder = animationBuilder;
			animatorSet.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animator) {

				}

				@Override
				public void onAnimationEnd(Animator animator) {
					if (finalAnimationBuilder != null) {
						FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
						transaction.remove(fragmentById).commit();
					}
				}

				@Override
				public void onAnimationCancel(Animator animator) {

				}

				@Override
				public void onAnimationRepeat(Animator animator) {

				}
			});
			animatorSet.setDuration(ANIMATION_SET_DURATION);
			animatorSet.start();
			activeIdsMap.put(previousClickedId, false);
		}
	}

	public Bitmap getBitmapFromView(View view, int width, int height) {
		Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(returnedBitmap);
		canvas.drawColor(Color.WHITE);
		view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
		view.layout(0, 0, width, height);
		view.draw(canvas);
		return returnedBitmap;
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

			diagramsList = articleData.getDiagrams();
			String bodyStr = articleData.getBody();
			if (!diagramsLoaded) {
				loadDiagramsFromContent(bodyStr, diagramsList);
			}

			contentTxt.setText(Html.fromHtml(bodyStr)); // Shouldn't be used if complex view is used

			DbDataManager.saveArticleItem(getContentResolver(), articleData, true);

			if (diagramsList != null) {
				for (ArticleDetailsItem.Diagram diagram : diagramsList) {
					DbDataManager.saveArticlesDiagramItem(getContentResolver(), diagram);
				}
			}
			updateComments();

			handler.postDelayed(markAsReadRunnable, READ_DELAY);
		}
	}

	private class DiagramLoaderTask extends AbstractUpdateTask<String, String> {

		public DiagramLoaderTask(TaskUpdateInterface<String> taskFace) {
			super(taskFace);
		}

		@Override
		protected Integer doTheTask(String... params) {
			logTest(" task in progress start");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logTest(" task in progress end");

			item = params[0];
			return StaticData.RESULT_OK;
		}
	}

	private class DiagramUpdateListener extends ChessUpdateListener<String> {
		private final int textColor;
		private final int textSize;
		private List<ArticleDetailsItem.Diagram> diagramList;

		private DiagramUpdateListener(List<ArticleDetailsItem.Diagram> diagramList) {
			super();
			this.diagramList = diagramList;
			Resources resources = getResources();
			textColor = resources.getColor(R.color.new_subtitle_dark_grey);
			textSize = (int) (resources.getDimensionPixelSize(R.dimen.content_text_size) / density);
			paddingSide = resources.getDimensionPixelSize(R.dimen.default_scr_side_padding);
		}

		@Override
		public void updateData(String part) {
			{

				String diagramPart = part.substring(part.indexOf("<div "));
				String partAfterDiagram = diagramPart.substring(diagramPart.indexOf("</div>") + "</div>".length());

				String diagramIdStr = diagramPart.substring(diagramPart.indexOf("id=\"chess_com_diagram_")
						+ "id=\"chess_com_diagram_".length());
				diagramIdStr = diagramIdStr.substring(0, diagramIdStr.indexOf("\" class"));
				String[] diagramIdParts = diagramIdStr.split("_");
				final int diagramId = Integer.parseInt(diagramIdParts[ID_POSITION]);
				diagramIdsList.add(diagramId);

				FrameLayout frameLayout = new FrameLayout(getActivity());
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPixels,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				params.gravity = Gravity.CENTER;
				frameLayout.setId(DIAGRAM_PREFIX + diagramId);
				frameLayout.setLayoutParams(params);

				complexContentLinLay.addView(frameLayout);

				// take 80% of screen width
				int imageSize = (int) (widthPixels * 0.8f);
				FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(imageSize, imageSize);
				imageParams.gravity = Gravity.CENTER;

				final ImageView imageView = new ImageView(getActivity());
				imageView.setImageResource(R.drawable.board_white_grey_full_size);
				imageView.setLayoutParams(imageParams);
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				imageView.setId(IMAGE_PREFIX + diagramId);

				// set click handler and then get this tag from view to show diagram
				imageView.setOnClickListener(ArticleDetailsFragment.this);

				frameLayout.addView(imageView);

				handler.postDelayed(new DiagramFragmentCreator(diagramId, diagramList), 200);


				RoboTextView textView = new RoboTextView(getActivity());
				textView.setTextSize(textSize);
				textView.setTextColor(textColor);
				textView.setText(Html.fromHtml(partAfterDiagram));
				textView.setPadding(paddingSide, paddingSide, paddingSide, 0);
				textView.setId(TEXT_PREFIX + diagramId);
				textView.setOnClickListener(ArticleDetailsFragment.this);

				complexContentLinLay.addView(textView);
			}
			// starting next task
			parsedPartCnt++;

			if (parsedPartCnt >= contentParts.length) {
				return;
			}

			if (contentParts[parsedPartCnt].contains("chess_com_diagram")) {
				logTest(" part starting task");
				new DiagramLoaderTask(new DiagramUpdateListener(diagramList)).executeTask(contentParts[parsedPartCnt]);


			} else {
				RoboTextView textView = new RoboTextView(getActivity());
				textView.setTextSize(textSize);
				textView.setTextColor(textColor);
				textView.setText(Html.fromHtml(contentParts[parsedPartCnt]));
				textView.setPadding(paddingSide, 0, paddingSide, 0);
				textView.setId(TEXT_PREFIX + ZERO);
				textView.setOnClickListener(ArticleDetailsFragment.this);

				complexContentLinLay.addView(textView);
			}
		}
	}

	private void loadDiagramsFromContent(String bodyStr, List<ArticleDetailsItem.Diagram> diagramList) {
		if (bodyStr.contains(DIAGRAM_START_TAG)) {

			Resources resources = getResources();
			int textColor = resources.getColor(R.color.new_subtitle_dark_grey);
			int textSize = (int) (resources.getDimensionPixelSize(R.dimen.content_text_size) / density);
			int paddingSide = resources.getDimensionPixelSize(R.dimen.default_scr_side_padding);
			// we go through the article body and divide it to parts
			contentParts = bodyStr.split(DIAGRAM_START_TAG);
			// hide simple container
			contentTxt.setVisibility(View.GONE);

			// show complex container
			complexContentLinLay.setVisibility(View.VISIBLE);

			// divide text and add corresponding views: TextView for text part and image for diagram part
			for (parsedPartCnt = 0; parsedPartCnt < contentParts.length; parsedPartCnt++) {
				if (contentParts[parsedPartCnt].contains("chess_com_diagram")) {
					logTest(" part starting task");
					new DiagramLoaderTask(new DiagramUpdateListener(diagramList)).executeTask(contentParts[parsedPartCnt]);
					diagramsLoaded = true;

					break;
				} else {
					RoboTextView textView = new RoboTextView(getActivity());
					textView.setTextSize(textSize);
					textView.setTextColor(textColor);
					textView.setText(Html.fromHtml(contentParts[parsedPartCnt]));
					textView.setPadding(paddingSide, 0, paddingSide, 0);
					textView.setId(TEXT_PREFIX + ZERO);
					textView.setOnClickListener(this);

					complexContentLinLay.addView(textView);
				}
			}

//			diagramsLoaded = true;
		} else {
			complexContentLinLay.setVisibility(View.GONE);
			contentTxt.setVisibility(View.VISIBLE);
		}
	}

	private class DiagramFragmentCreator implements Runnable {

		private int diagramId;
		private List<ArticleDetailsItem.Diagram> diagramList;

		public DiagramFragmentCreator(int diagramId, List<ArticleDetailsItem.Diagram> diagramList) {
			this.diagramId = diagramId;
			this.diagramList = diagramList;
		}

		@Override
		public void run() {
			if (getActivity() == null) {
				return;
			}

			for (ArticleDetailsItem.Diagram diagramToShow : diagramList) {
				if (diagramToShow.getDiagramId() == diagramId) {
					// create a real fragment
					logTest(" create a real fragment for id = " + diagramId);

					final GameDiagramItem diagramItem = new GameDiagramItem();
					diagramItem.setShowAnimation(false);
					diagramItem.setUserColor(ChessBoard.WHITE_SIDE);
					if (diagramToShow.getType() == ArticleDetailsItem.Diagram.PUZZLE) {
						diagramItem.setMovesList(diagramToShow.getMoveList());
						logTest(" id = " + diagramId + " use moves");
					} else if (diagramToShow.getType() == ArticleDetailsItem.Diagram.CHESS_GAME) {
						diagramItem.setMovesList(diagramToShow.getMoveList());
						logTest(" id = " + diagramId + " use moves");
					} else if (diagramToShow.getType() == ArticleDetailsItem.Diagram.SIMPLE) {
						diagramItem.setFen(diagramToShow.getFen());
						logTest(" id = " + diagramId + " use fen = " + diagramToShow.getFen());
					} else { // non valid format
						return;
					}

					// load it
					final GameDiagramFragment fragment = GameDiagramFragment.createInstance(diagramItem);
					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.replace(DIAGRAM_PREFIX + diagramId, fragment, fragment.getClass().getSimpleName());
//					transaction.addToBackStack(fragment.getClass().getSimpleName());
					transaction.commit();

					// then hide to receive an exact view and set to imageView
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (getActivity() == null) {
								return;
							}

							final Fragment fragment = getChildFragmentManager().findFragmentById(DIAGRAM_PREFIX + diagramId);

							if (fragment == null) {
								return;
							}
							final View fragmentView = fragment.getView();
							// get bitmap from fragmentView
							Bitmap bitmapFromView = getBitmapFromView(fragmentView, (int) (widthPixels * 0.8f),
									(int) (widthPixels * 0.8f));

							BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmapFromView);
							drawable.setBounds(0, 0, (int) (widthPixels * 0.8f), (int) (widthPixels * 0.8f));
							ImageView imageView = (ImageView) getView().findViewById(IMAGE_PREFIX + diagramId);
							imageView.setImageDrawable(drawable);
							logTest(" diagramId = " + diagramId + " fragment = " + fragment + " \n imageView = " + imageView);

							// remove fragment
							FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
							transaction.remove(fragment).commit();
						}
					}, 200);

					break;
				}
			}
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

	private void widgetsInit(View view) {
		ViewGroup headerView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.new_article_details_header_frame, null, false);

		if (JELLY_BEAN_PLUS_API) {
			LayoutTransition layoutTransition = headerView.getLayoutTransition();
			layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		}
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

		listView = (ControlledListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(commentsCursorAdapter);
		listView.setOnItemClickListener(this);

		replyView = view.findViewById(R.id.replyView);
		newPostEdt = (EditText) view.findViewById(R.id.newPostEdt);
	}

}
