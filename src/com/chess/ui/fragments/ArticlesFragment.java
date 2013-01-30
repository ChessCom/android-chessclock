package com.chess.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ArticleItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.NewArticlesAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

	private ArticleItemUpdateListener articleListUpdateListener;

	private NewArticlesAdapter articlesAdapter;


	private ViewHolder holder;
	private ForegroundColorSpan foregroundSpan;
	private ArticleItemUpdateListener articleRandomUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		articlesAdapter = new NewArticlesAdapter(getActivity(), new ArrayList<ArticleItem.Data>());

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

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(articlesAdapter);
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

		updateData();
	}

	@Override
	public void onStop() {
		super.onStop();

		releaseResources();
	}

	private void init() {
		articleRandomUpdateListener = new ArticleItemUpdateListener(ArticleItemUpdateListener.RANDOM);
		articleListUpdateListener = new ArticleItemUpdateListener(ArticleItemUpdateListener.ARTICLES_LIST);
	}

	private void updateData() {
		{// get first random article
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_ARTICLES_LIST);
			loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);

			new RequestJsonTask<ArticleItem>(articleRandomUpdateListener).executeTask(loadItem);
		}

		{// get list
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_ARTICLES_LIST);
			loadItem.replaceRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_LIST_CNT);

			new RequestJsonTask<ArticleItem>(articleListUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	private class ArticleItemUpdateListener extends ActionBarUpdateListener<ArticleItem> {

		final static int RANDOM = 0;
		final static int ARTICLES_LIST = 1;

		private int listenerCode;

		public ArticleItemUpdateListener(int listenerCode) {
			super(getInstance(), ArticleItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(ArticleItem returnedObj) {

			switch (listenerCode){
				case RANDOM:

					ArticleItem.Data item = returnedObj.getData().get(0);
					String firstName = item.getFirst_name();
					CharSequence chessTitle = item.getChess_title();
					String lastName =  item.getLast_name();
					CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + StaticData.SYMBOL_SPACE
							+ firstName + StaticData.SYMBOL_SPACE + lastName;
					authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
					holder.authorTxt.setText(authorStr);

					holder.titleTxt.setText(item.getTitle());
					holder.dateTxt.setText(dateFormatter.format(new Date(item.getCreate_date())));


					break;
				case ARTICLES_LIST:
					articlesAdapter.setItemsList(returnedObj.getData());
					articlesAdapter.notifyDataSetInvalidated();
					break;


			}

			// add data to sectioned adapter

//			recent.setVisibility(View.VISIBLE);
//			int cnt = Integer.parseInt(returnedObj.getData().getTotal_videos_count());
//			if (cnt > 0){
//				item = returnedObj.getData().getVideos().get(0); // new VideoItemOld(returnedObj.split(RestHelper.SYMBOL_ITEM_SPLIT)[2].split("<->"));
//				title.setText(item.getName());
//				desc.setText(item.getDescription());
//
//				playBtn.setEnabled(true);
//			}
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
