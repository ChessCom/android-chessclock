package com.chess.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
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
public class ArticleDetailsFragment extends CommonLogicFragment implements ItemClickListenerFace {

	public static final String ITEM_ID = "item_id";
	public static final String GREY_COLOR_DIVIDER = "##";

	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");


	private TextView authorTxt;
	private View loadingView;
	private TextView emptyView;

	private TextView titleTxt;
	private ImageView thumbnailAuthorImg;
	private ImageView countryImg;
	private TextView dateTxt;
	private TextView contextTxt;
	private Cursor loadedCursor;

	private ArticleCursorUpdateListener articleCursorUpdateListener;


	public static ArticleDetailsFragment newInstance(long articleId) {
		ArticleDetailsFragment frag = new ArticleDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(ITEM_ID, articleId);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_article_details_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		thumbnailAuthorImg = (ImageView) view.findViewById(R.id.thumbnailAuthorImg);
		countryImg = (ImageView) view.findViewById(R.id.countryImg);
		dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		contextTxt = (TextView) view.findViewById(R.id.contextTxt);
		authorTxt = (TextView) view.findViewById(R.id.authorTxt);
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		loadFromDb();
	}

	private void loadFromDb() {
		long itemId = getArguments().getLong(ITEM_ID);

		new LoadDataFromDbTask(articleCursorUpdateListener, DbHelper.getArticlesListParams(),
				getContentResolver()).executeTask(itemId);
	}

	@Override
	public void onStop() {
		super.onStop();
		// TODO release resources
	}

	private void init() {

		articleCursorUpdateListener = new ArticleCursorUpdateListener();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.playBtn) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(DBDataManager.getString(loadedCursor, DBConstants.V_MOBILE_URL)), "video/*");
			startActivity(intent);
		}
	}

	private class ArticleCursorUpdateListener extends ActionBarUpdateListener<Cursor> {

		public ArticleCursorUpdateListener() {
			super(getInstance());

		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor cursor) {
			if (getActivity() == null) {
				return;
			}

			loadedCursor = cursor;

			int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
			String firstName = DBDataManager.getString(cursor, DBConstants.V_FIRST_NAME);
			CharSequence chessTitle = DBDataManager.getString(cursor, DBConstants.V_CHESS_TITLE);
			String lastName = DBDataManager.getString(cursor, DBConstants.V_LAST_NAME);
			CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ StaticData.SYMBOL_SPACE + firstName + StaticData.SYMBOL_SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, new ForegroundColorSpan(lightGrey));
			authorTxt.setText(authorStr);

			titleTxt.setText(DBDataManager.getString(cursor, DBConstants.V_TITLE));
//			thumbnailAuthorImg // TODO adjust image loader
			countryImg.setImageResource(R.drawable.ic_united_states); // TODO set flag properly // invent flag resources set system

			dateTxt.setText(dateFormatter.format(new Date(DBDataManager.getLong(cursor, DBConstants.V_CREATE_DATE))));

			contextTxt.setText(DBDataManager.getString(cursor, DBConstants.V_DESCRIPTION));

		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText(R.string.no_games);
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	private void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

//			emptyView.setVisibility(View.VISIBLE);
//			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
//			listView.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
//			if (videosCursorAdapter.getCount() == 0) {
//				listView.setVisibility(View.GONE);
//
//			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
//			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
