package com.chess.ui.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.*;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ChessDarkSpinnerIconAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.01.13
 * Time: 10:37
 */
public class StatsLiveFragment extends CommonLogicFragment implements AdapterView.OnItemSelectedListener {

	private static final String TAG = "StatsFragment";

	public static final int HIGHEST_ID = 0x00002000;
	public static final int LOWEST_ID = 0x00002100;
	public static final int AVERAGE_ID = 0x00002200;
	public static final int BEST_WIN_ID = 0x00002300;

	public static final int RATING_TIMESTAMP_ID = 0x00000001;
	public static final int RATING_VALUE_ID = 0x00000002;
	// 05/27/08
	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");

	private final static int LIVE_STANDARD = 0;
	private final static int LIVE_BLITZ = 1;
	private final static int LIVE_BULLET = 2;
	private final static int DAILY_CHESS = 3;
	private final static int DAILY_CHESS960 = 4;
	private final static int TACTICS = 5;
	private final static int CHESS_MENTOR = 6;



	private StatsItemUpdateListener statsItemUpdateListener;
	private Spinner ratingSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_stats_live_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ratingSpinner = (Spinner) view.findViewById(R.id.ratingSpinner);

		
		List<SelectionItem> sortList = createSpinnerList(getActivity());
		ratingSpinner.setAdapter(new ChessDarkSpinnerIconAdapter(getActivity(), sortList));
		ratingSpinner.setOnItemSelectedListener(this);
		ratingSpinner.setSelection(0);  // TODO remember last selection.

		LinearLayout ratingsLinearView = (LinearLayout) view.findViewById(R.id.ratingsLinearView);

		addRatingsViews(ratingsLinearView);
	}

	private void addRatingsViews(LinearLayout ratingsLinearView) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		// set id's to view for further set data to them
		{// Highest Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.highest_rating);
			highestRatingView.findViewById(R.id.timestampTxt).setId(HIGHEST_ID + RATING_TIMESTAMP_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(HIGHEST_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Lowest Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.lowest_rating);
			highestRatingView.findViewById(R.id.timestampTxt).setId(LOWEST_ID + RATING_TIMESTAMP_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(LOWEST_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Average Opponent Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.avg_opponent_rating);
			highestRatingView.findViewById(R.id.timestampTxt).setId(AVERAGE_ID + RATING_TIMESTAMP_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(AVERAGE_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Best Win Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.best_win_rating);
			highestRatingView.findViewById(R.id.timestampTxt).setId(BEST_WIN_ID + RATING_TIMESTAMP_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(BEST_WIN_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

	}

	@Override
	public void onStart() {
		super.onStart();

//		updateData();
	}

	private void init() {
		statsItemUpdateListener = new StatsItemUpdateListener();

	}

	private void updateData() {
		int itemPosition = ratingSpinner.getSelectedItemPosition();

		// get stats from DB

		// get full stats
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));

		new RequestJsonTask<StatsItem>(statsItemUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		updateData();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commit();
	}

	private class StatsItemUpdateListener extends ActionBarUpdateListener<StatsItem> {

		public StatsItemUpdateListener() {
			super(getInstance(), StatsItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			// TODO -> File | Settings | File Templates.
		}

		@Override
		public void updateData(StatsItem returnedObj) {
			super.updateData(returnedObj);



			// get selected position of spinner
			int position = ratingSpinner.getSelectedItemPosition();

			switch (position){
				case LIVE_STANDARD:
					fillLiveStandard(returnedObj.getData().getLive().getStandard());
					break;
				case LIVE_BLITZ:
					fillLiveBlitz(returnedObj.getData().getLive().getBlitz());
					break;
				case LIVE_BULLET:
					fillLiveBullet(returnedObj.getData().getLive().getLightning());
					break;
				case DAILY_CHESS:
					fillDaily(returnedObj.getData().getDaily().getChess());
					break;
				case DAILY_CHESS960:
					fillDaily960(returnedObj.getData().getDaily().getChess960());
					break;
				case TACTICS:
					fillTactics(returnedObj.getData().getTactics());
					break;
				case CHESS_MENTOR:
					fillChessMentor(returnedObj.getData().getChessMentor());
					break;
			}
		}


	}

	private void fillChessMentor(ChessMentorData chessMentor) {
		// TODO -> File | Settings | File Templates.

	}

	private void fillTactics(TacticsStatsData tactics) {
		// TODO -> File | Settings | File Templates.

	}

	private void fillLiveBullet(LiveStatsData.Stats lightning) {
		// TODO -> File | Settings | File Templates.

	}

	private void fillLiveBlitz(LiveStatsData.Stats blitz) {
		// TODO -> File | Settings | File Templates.

	}

	private void fillLiveStandard(LiveStatsData.Stats ratingData) {
		if (ratingData == null) {
			Log.e(TAG, "No Live Data");
			return;
		}

		int liveHighestRating = ratingData.getRating().getHighest().getRating();
		long liveHighestRatingTime = ratingData.getRating().getHighest().getTimestamp();

		((TextView) getView().findViewById(HIGHEST_ID + RATING_TIMESTAMP_ID)).setText(dateFormatter.format(new Date(liveHighestRatingTime)));
		((TextView) getView().findViewById(HIGHEST_ID + RATING_VALUE_ID)).setText(liveHighestRating);

	}

	private void fillDaily(DailyStatsData.ChessStatsData ratingData) {
		if (ratingData == null) {
			Log.e(TAG, "No Live Data");
			return;
		}

		int liveHighestRating = ratingData.getRating().getHighest().getRating();
		long liveHighestRatingTime = ratingData.getRating().getHighest().getTimestamp();

		((TextView) getView().findViewById(HIGHEST_ID + RATING_TIMESTAMP_ID)).setText(dateFormatter.format(new Date(liveHighestRatingTime)));
		((TextView) getView().findViewById(HIGHEST_ID + RATING_VALUE_ID)).setText(liveHighestRating);

	}


	private void fillDaily960(DailyStatsData.ChessStatsData ratingData) {
		if (ratingData == null) {
			Log.e(TAG, "No Live Data");
			return;
		}

		int liveHighestRating = ratingData.getRating().getHighest().getRating();
		long liveHighestRatingTime = ratingData.getRating().getHighest().getTimestamp();

		((TextView) getView().findViewById(HIGHEST_ID + RATING_TIMESTAMP_ID)).setText(dateFormatter.format(new Date(liveHighestRatingTime)));
		((TextView) getView().findViewById(HIGHEST_ID + RATING_VALUE_ID)).setText(liveHighestRating);

	}

	private List<SelectionItem> createSpinnerList(Context context) {
		ArrayList<SelectionItem> selectionItems = new ArrayList<SelectionItem>();

		String[] categories = context.getResources().getStringArray(R.array.ratings_categories);
		for (int i = 0; i < categories.length; i++) {
			String category = categories[i];
			SelectionItem selectionItem = new SelectionItem(getIconByCategory(i), category);
			selectionItems.add(selectionItem);
		}
		return selectionItems;
	}



	/**
	 *  Fill list according :
	 *	Live - Standard
	 *	Live - Blitz
	 *	Live - Bullet
	 *	Daily - Chess
	 *	Daily - Chess960
	 *	Tactics
	 *	Coach Manager
	 * @param index
	 * @return Drawable icon for index
	 */
	private Drawable getIconByCategory(int index) {
		switch (index) {
			case LIVE_STANDARD:
				return getResources().getDrawable(R.drawable.ic_live_game);
			case LIVE_BLITZ:
				return getResources().getDrawable(R.drawable.ic_live_blitz);
			case LIVE_BULLET:
				return getResources().getDrawable(R.drawable.ic_live_bullet);
			case DAILY_CHESS:
				return getResources().getDrawable(R.drawable.ic_daily_game);
			case DAILY_CHESS960:
				return getResources().getDrawable(R.drawable.ic_daily960_game);
			case TACTICS:
				return getResources().getDrawable(R.drawable.ic_tactics_game);
			default: // case CHESS_MENTOR:
				return getResources().getDrawable(R.drawable.ic_tactics_game);
		}
	}
}
