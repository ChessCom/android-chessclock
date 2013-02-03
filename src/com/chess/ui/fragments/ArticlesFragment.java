package com.chess.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ArticleCategoryItem;
import com.chess.backend.entity.new_api.ArticleItem;
import com.chess.backend.entity.new_api.VideoCategoryItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveArticleCategoriesTask;
import com.chess.db.tasks.SaveArticlesListTask;
import com.chess.db.tasks.SaveVideoCategoriesTask;
import com.chess.ui.adapters.NewArticlesSectionedCursorAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 6:56
 */
public class ArticlesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	public static final String GREY_COLOR_DIVIDER = "##";
	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

	private ViewHolder holder;
	private ForegroundColorSpan foregroundSpan;

	private ListView listView;
	private View loadingView;
	private TextView emptyView;

	private NewArticlesSectionedCursorAdapter articlesCursorAdapter;

	private ArticleItemUpdateListener articleListUpdateListener;
	private SaveArticlesUpdateListener saveArticlesUpdateListener;
	private ArticlesCursorUpdateListener articlesCursorUpdateListener;

	private ArticleCategoriesUpdateListener articleCategoriesUpdateListener;
	private SaveArticleCategoriesUpdateListener saveArticleCategoriesUpdateListener;
	private boolean need2Update = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		articlesCursorAdapter = new NewArticlesSectionedCursorAdapter(getActivity(), null);

		int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_articles_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(articlesCursorAdapter);
		listView.setOnItemClickListener(this);

		holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		if (need2Update) {

			boolean haveSavedData = DBDataManager.haveSavedArticles(getActivity());

			if (AppUtils.isNetworkAvailable(getActivity())) {
				updateData();
				getCategories();
			} else if (!haveSavedData) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}

			if (haveSavedData) {
				loadFromDb();
			}
		}
	}



	@Override
	public void onStop() {
		super.onStop();

		releaseResources();
	}

	private void init() {
		articleListUpdateListener = new ArticleItemUpdateListener();
		saveArticlesUpdateListener = new SaveArticlesUpdateListener();
		articlesCursorUpdateListener = new ArticlesCursorUpdateListener();

		articleCategoriesUpdateListener = new ArticleCategoriesUpdateListener();
		saveArticleCategoriesUpdateListener = new SaveArticleCategoriesUpdateListener();
	}

	private void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLES_LIST);

		new RequestJsonTask<ArticleItem>(articleListUpdateListener).executeTask(loadItem);
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLES_CATEGORIES);
		new RequestJsonTask<ArticleCategoryItem>(articleCategoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (articlesCursorAdapter.isHeader(position)) {
			String sectionName = articlesCursorAdapter.getSectionName(position);

			getActivityFace().openFragment(ArticleCategoriesFragment.newInstance(sectionName));
		} else {
			position = articlesCursorAdapter.getRelativePosition(position);
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			getActivityFace().openFragment(ArticleDetailsFragment.newInstance(DBDataManager.getId(cursor)));
		}

	}

	private class ArticleItemUpdateListener extends ActionBarUpdateListener<ArticleItem> {

		public ArticleItemUpdateListener() {
			super(getInstance(), ArticleItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(ArticleItem returnedObj) {

/*
			ArticleItem.Data item = returnedObj.getData().get(0);
			String firstName = item.getFirstName();
			CharSequence chessTitle = item.getChessTitle();
			String lastName = item.getLastName();
			CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + StaticData.SYMBOL_SPACE
					+ firstName + StaticData.SYMBOL_SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
			holder.authorTxt.setText(authorStr);

			holder.titleTxt.setText(item.getTitle());
			holder.dateTxt.setText(dateFormatter.format(new Date(item.getCreate_date())));
*/

			new SaveArticlesListTask(saveArticlesUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
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

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView dateTxt;
	}

	private void releaseResources() {
		articleListUpdateListener.releaseContext();
		articleListUpdateListener = null;
	}


	private class ArticleCategoriesUpdateListener extends ActionBarUpdateListener<ArticleCategoryItem> {
		public ArticleCategoriesUpdateListener() {
			super(getInstance(), ArticleCategoryItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(ArticleCategoryItem returnedObj) {
			super.updateData(returnedObj);

			new SaveArticleCategoriesTask(saveArticleCategoriesUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();

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


	private class SaveArticleCategoriesUpdateListener extends ActionBarUpdateListener<ArticleCategoryItem.Data> {
		public SaveArticleCategoriesUpdateListener() {
			super(getInstance());
		}
	}

	private class SaveArticlesUpdateListener extends ActionBarUpdateListener<ArticleItem.Data>{
		public SaveArticlesUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(ArticleItem.Data returnedObj) {
			super.updateData(returnedObj);
			if (getActivity() == null){
				return;
			}

			loadFromDb();
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

	private void loadFromDb() {
		new LoadDataFromDbTask(articlesCursorUpdateListener, DbHelper.getArticlesListParams(),
				getContentResolver()).executeTask();
	}

	private class ArticlesCursorUpdateListener extends ActionBarUpdateListener<Cursor>{
		public ArticlesCursorUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			articlesCursorAdapter.changeCursor(returnedObj);
			listView.setAdapter(articlesCursorAdapter);

			need2Update = false;
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

			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			if (articlesCursorAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
