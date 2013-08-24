package com.chess.ui.fragments.articles;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
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
	private ImageView thumbnailAuthorImg;
	private ImageView countryImg;
	private TextView dateTxt;
	private TextView contentTxt;
	private long articleId;
	private CommentsUpdateListener commentsUpdateListener;
	private CommentsCursorAdapter commentsCursorAdapter;
	private ListView listView;


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

		commentsUpdateListener = new CommentsUpdateListener();
		commentsCursorAdapter = new CommentsCursorAdapter(getActivity(), null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_article_details_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		loadingView = view.findViewById(R.id.loadingView);

		titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		thumbnailAuthorImg = (ImageView) view.findViewById(R.id.thumbnailAuthorImg);
		countryImg = (ImageView) view.findViewById(R.id.countryImg);
		dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		contentTxt = (TextView) view.findViewById(R.id.contentTxt);
		authorTxt = (TextView) view.findViewById(R.id.authorTxt);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setAdapter(commentsCursorAdapter);

		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		loadFromDb();

		// Load comments
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLE_COMMENTS(articleId));

		new RequestJsonTask<ArticleCommentItem>(commentsUpdateListener).executeTask(loadItem);
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
//			thumbnailAuthorImg // TODO adjust image loader
			countryImg.setImageDrawable(AppUtils.getUserFlag(getActivity())); // TODO set flag properly // invent flag resources set system

			dateTxt.setText(dateFormatter.format(new Date(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE))));
			contentTxt.setText(DbDataManager.getString(cursor, DbScheme.V_BODY));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	private class CommentsUpdateListener extends ChessUpdateListener<ArticleCommentItem> {

		private CommentsUpdateListener() {
			super(ArticleCommentItem.class);
		}

		@Override
		public void updateData(ArticleCommentItem returnedObj) {
			super.updateData(returnedObj);

			DbDataManager.saveArticleCommentToDb(getContentResolver(), returnedObj, articleId);

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
