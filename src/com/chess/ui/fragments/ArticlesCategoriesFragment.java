package com.chess.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ArticleCategoryItem;
import com.chess.backend.entity.new_api.ArticleItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.ChessDarkSpinnerAdapter;
import com.chess.ui.adapters.NewArticlesAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 6:56
 */
public class ArticlesCategoriesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	private ArticleItemUpdateListener articleListUpdateListener;

	private NewArticlesAdapter articlesAdapter;
	private Spinner categorySpinner;
	private ArticleCategoriesUpdateListener articleCategoriesUpdateListener;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		articlesAdapter = new NewArticlesAdapter(getActivity(), new ArrayList<ArticleItem.Data>());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_categories_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
		Spinner sortSpinner = (Spinner) view.findViewById(R.id.sortSpinner);

		List<String> sortList = new ArrayList<String>();  // TODO set list of sort parameters
		sortList.add("Latest");
		sortList.add("Date");
		sortList.add("Author's Name");
		sortList.add("Author's Country");
		sortSpinner.setAdapter(new ChessDarkSpinnerAdapter(getActivity(), sortList));

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(articlesAdapter);
		listView.setOnItemClickListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		updateData();
	}

	@Override
	public void onStop() {
		super.onStop();

		releaseResources();
	}

	private void init() {

		articleListUpdateListener = new ArticleItemUpdateListener();
		articleCategoriesUpdateListener = new ArticleCategoriesUpdateListener();
	}

	private void updateData() {

		// get list of categories;
		{
			// get list
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_ARTICLES_CATEGORIES);

			new RequestJsonTask<ArticleCategoryItem>(articleCategoriesUpdateListener).executeTask(loadItem);
		}


		// get list
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLES_LIST);
		loadItem.replaceRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_LIST_CNT);

		new RequestJsonTask<ArticleItem>(articleListUpdateListener).executeTask(loadItem);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	private class ArticleCategoriesUpdateListener extends ActionBarUpdateListener<ArticleCategoryItem> {

		public ArticleCategoriesUpdateListener() {
			super(getInstance(), ArticleCategoryItem.class);
		}

		@Override
		public void updateData(ArticleCategoryItem returnedObj) {
			List<ArticleCategoryItem.Data> list = returnedObj.getData();

			List<String> sortList = new ArrayList<String>();
			for (ArticleCategoryItem.Data categoryItem : list) {
				sortList.add(categoryItem.getName());
			}
			categorySpinner.setAdapter(new ChessDarkSpinnerAdapter(getActivity(), sortList));
		}
	}

	private class ArticleItemUpdateListener extends ActionBarUpdateListener<ArticleItem> {

		public ArticleItemUpdateListener() {
			super(getInstance(), ArticleItem.class);
		}

		@Override
		public void updateData(ArticleItem returnedObj) {

			articlesAdapter.setItemsList(returnedObj.getData());
			articlesAdapter.notifyDataSetInvalidated();
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


}
