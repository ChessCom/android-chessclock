package com.chess.ui.fragments.articles;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ArticleCommentItem;
import com.chess.backend.entity.api.ArticleDetailsItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.ui.adapters.CommentsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class ArticleDetailsFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	public static final String ITEM_ID = "item_id";
	public static final String GREY_COLOR_DIVIDER = "##";

	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

	private TextView authorTxt;

	private TextView titleTxt;
	private ProgressImageView articleImg;
	private ProgressImageView authorImg;
	private ImageView countryImg;
	private TextView dateTxt;
	private TextView viewsCntTxt;
	private TextView commentsCntTxt;
	private TextView contentTxt;
	private long articleId;
	private CommentsUpdateListener commentsUpdateListener;
	private CommentsCursorAdapter commentsCursorAdapter;
	private ArticleUpdateListener articleUpdateListener;
	private EnhancedImageDownloader imageDownloader;
	private int imgSize;
	private SparseArray<String> countryMap;
	private int widthPixels;


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
		imgSize = (int) (40 * resources.getDisplayMetrics().density);

		widthPixels = resources.getDisplayMetrics().widthPixels;

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_article_details_header_frame, null, false);

		loadingView = view.findViewById(R.id.loadingView);

		titleTxt = (TextView) headerView.findViewById(R.id.titleTxt);
		articleImg = (ProgressImageView) headerView.findViewById(R.id.articleImg);
		authorImg = (ProgressImageView) headerView.findViewById(R.id.thumbnailAuthorImg);
		countryImg = (ImageView) headerView.findViewById(R.id.countryImg);
		dateTxt = (TextView) headerView.findViewById(R.id.dateTxt);
		viewsCntTxt = (TextView) headerView.findViewById(R.id.viewsCntTxt);
		commentsCntTxt = (TextView) headerView.findViewById(R.id.commentsCntTxt);
		contentTxt = (TextView) headerView.findViewById(R.id.contentTxt);
		authorTxt = (TextView) headerView.findViewById(R.id.authorTxt);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setDivider(null);
		listView.setDividerHeight(0);
		listView.addHeaderView(headerView);
		listView.setAdapter(commentsCursorAdapter);
		listView.setOnItemClickListener(this);

		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		loadFromDb();

		// get full body text from server
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLE_BY_ID(articleId));

		new RequestJsonTask<ArticleDetailsItem>(articleUpdateListener).executeTask(loadItem);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(ITEM_ID, articleId);
	}

	private void loadFromDb() {
		Cursor cursor = DbDataManager.executeQuery(getContentResolver(), DbHelper.getArticleById(articleId));

		if (cursor.moveToFirst()) {
			int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
			String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
			CharSequence chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
			String lastName = DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
			CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ StaticData.SYMBOL_SPACE + firstName + StaticData.SYMBOL_SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, new ForegroundColorSpan(lightGrey));
			authorTxt.setText(authorStr);

			titleTxt.setText(DbDataManager.getString(cursor, DbScheme.V_TITLE));
			imageDownloader.download(DbDataManager.getString(cursor, DbScheme.V_USER_AVATAR), authorImg, imgSize);

			imageDownloader.download(DbDataManager.getString(cursor, DbScheme.V_PHOTO_URL), articleImg, widthPixels);

			Drawable drawable = AppUtils.getCountryFlagScaled(getActivity(), countryMap.get(DbDataManager.getInt(cursor, DbScheme.V_COUNTRY_ID)));
			countryImg.setImageDrawable(drawable);

			dateTxt.setText(dateFormatter.format(new Date(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE))));
			contentTxt.setText(DbDataManager.getString(cursor, DbScheme.V_BODY));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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

			String viewsCntStr = getString(R.string.comments_arg, commentCount);
			String commentsCntStr = getString(R.string.reads_arg, viewCount);
			viewsCntTxt.setText(StaticData.SYMBOL_SLASH + StaticData.SYMBOL_SPACE + viewsCntStr);
			commentsCntTxt.setText(StaticData.SYMBOL_SLASH + StaticData.SYMBOL_SPACE + commentsCntStr);
			contentTxt.setText(Html.fromHtml(articleData.getBody()));

			DbDataManager.saveArticleItem(getContentResolver(), articleData);

			// Load comments
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_ARTICLE_COMMENTS(articleId));

			new RequestJsonTask<ArticleCommentItem>(commentsUpdateListener).executeTask(loadItem);
		}
	}

	private class CommentsUpdateListener extends ChessUpdateListener<ArticleCommentItem> {

		private CommentsUpdateListener() {
			super(ArticleCommentItem.class);
		}

		@Override
		public void updateData(ArticleCommentItem returnedObj) {
			super.updateData(returnedObj);

			DbDataManager.updateArticleCommentToDb(getContentResolver(), returnedObj, articleId);

			Cursor cursor = DbDataManager.executeQuery(getContentResolver(), DbHelper.getArticlesCommentsById(articleId));
			if (cursor != null && cursor.moveToFirst()) {
				commentsCursorAdapter.changeCursor(cursor);
			}
		}
	}


	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
