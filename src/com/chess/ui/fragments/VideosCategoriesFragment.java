package com.chess.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
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
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.ChessDarkSpinnerAdapter;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.NewVideosAdapter;
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
public class VideosCategoriesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

	public static final String GREY_COLOR_DIVIDER = "##";
	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

	private VideosItemUpdateListener randomVideoUpdateListener;

	private String[] categories;
	private CustomSectionedAdapter sectionedAdapter;
	private NewVideosAdapter amazingGamesAdapter;
	private NewVideosAdapter endGamesGamesAdapter;
	private NewVideosAdapter openingsGamesAdapter;
	private NewVideosAdapter rulesBasicGamesAdapter;
	private NewVideosAdapter strategyGamesAdapter;
	private NewVideosAdapter tacticsGamesAdapter;


	private ForegroundColorSpan foregroundSpan;
	private Spinner categorySpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		categories = getResources().getStringArray(R.array.category);

		amazingGamesAdapter = new NewVideosAdapter(getActivity(), new ArrayList<VideoItem.VideoDataItem>());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_categories_frame, container, false); // TODO restore

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
		List<String> list = AppUtils.convertArrayToList(getResources().getStringArray(R.array.category));

		categorySpinner.setAdapter(new ChessDarkSpinnerAdapter(getActivity(), list));
		categorySpinner.setOnItemSelectedListener(this);
		categorySpinner.setSelection(1);  // TODO remember last selection


		Spinner sortSpinner = (Spinner) view.findViewById(R.id.sortSpinner);

		List<String> sortList = new ArrayList<String>();  // TODO set list of sort parameters
		sortList.add("Latest");
		sortList.add("Date");
		sortList.add("Author's Name");
		sortList.add("Author's Country");
		sortSpinner.setAdapter(new ChessDarkSpinnerAdapter(getActivity(), sortList));

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(amazingGamesAdapter);
		listView.setOnItemClickListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();

		init();

//		updateData();
	}

	@Override
	public void onStop() {
		super.onStop();
		randomVideoUpdateListener.releaseContext();
		randomVideoUpdateListener = null;
	}

	private void init() {
		randomVideoUpdateListener = new VideosItemUpdateListener();
	}

	private void updateData() {
		String category = (String) categorySpinner.getSelectedItem();
		VideosItemUpdateListener videoUpdateListener = new VideosItemUpdateListener();

		LoadItem loadItem = new LoadItem();

		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_ITEM_ONE);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);
		loadItem.addRequestParams(RestHelper.P_CATEGORY, category);
		new RequestJsonTask<VideoItem>(videoUpdateListener).executeTask(loadItem);
	}


	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		getActivityFace().openFragment(new VideosDetailsFragment());
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		updateData();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private class VideosItemUpdateListener extends ActionBarUpdateListener<VideoItem> {

		public VideosItemUpdateListener() {
			super(getInstance(), VideoItem.class);
		}

		@Override
		public void updateData(VideoItem returnedObj) {

			amazingGamesAdapter.setItemsList(returnedObj.getData().getVideos());
			amazingGamesAdapter.notifyDataSetInvalidated();
		}
	}

}
