package com.chess.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.VideosAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class VideosFragment extends CommonLogicFragment implements ItemClickListenerFace {

	private VideosItemUpdateListener randomVideoUpdateListener;

	private String[] categories;
	private CustomSectionedAdapter sectionedAdapter;
	private VideosAdapter amazingGamesAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		categories = getResources().getStringArray(R.array.category);
/*
<item>Amazing games</item>
<item>Endgames</item>
<item>Openings</item>
<item>Rules Basics</item>
<item>Strategy</item>
<item>Tactics</item>
*/

		amazingGamesAdapter = new VideosAdapter(this, new ArrayList<VideoItem.VideoDataItem>());
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_arrow_section_header);
		sectionedAdapter.addSection(categories[0], amazingGamesAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		return inflater.inflate(R.layout.new_videos_frame, container, false); // TODO restore
		return inflater.inflate(R.layout.new_common_test, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// TODO create sectioned adapter, where first section will be random video thumbnail
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		// updateData();
	}

	@Override
	public void onStop() {
		super.onStop();
		randomVideoUpdateListener.releaseContext();
		randomVideoUpdateListener = null;
	}

	private void init() {
		randomVideoUpdateListener = new VideosItemUpdateListener(VideosItemUpdateListener.RANDOM);


	}

	private void updateData() {
		// get random video
		// get 2 items from every category



		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.GET_VIDEOS);
		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_ITEM_ONE);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);

//		new GetStringObjTask(randomVideoUpdateListener).executeTask(loadItem);
		new RequestJsonTask<VideoItem>(randomVideoUpdateListener).executeTask(loadItem);

		for (int i = 0; i < categories.length; i++) {
			makeNextCategoryRequest(i);
		}

	}

	private void makeNextCategoryRequest(int code){
		String category = categories[code];
		VideosItemUpdateListener videoUpdateListener = new VideosItemUpdateListener(code);


		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.GET_VIDEOS);
		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_ITEM_ONE);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);
		loadItem.addRequestParams(RestHelper.P_CATEGORY, category);
//		new GetStringObjTask(randomVideoUpdateListener).executeTask(loadItem);
		new RequestJsonTask<VideoItem>(videoUpdateListener).executeTask(loadItem);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private class VideosItemUpdateListener extends ActionBarUpdateListener<VideoItem> {



		final static int AMAZING_GAMES = 0;
		final static int END_GAMES = 1;
		final static int OPENINGS = 2;
		final static int RULES_BASICS = 3;
		final static int STRATEGY = 4;
		final static int TACTICS = 5;
		final static int RANDOM = 6;

		private int listenerCode;

		public VideosItemUpdateListener(int listenerCode) {
			super(getInstance(), VideoItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(VideoItem returnedObj) {

			switch (listenerCode){
				case RANDOM:

					break;
				case AMAZING_GAMES:

					amazingGamesAdapter.setItemsList(returnedObj.getData().getVideos());
					break;
				case END_GAMES:

					break;
				case OPENINGS:

					break;
				case RULES_BASICS:

					break;
				case STRATEGY:

					break;
				case TACTICS:

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
}
