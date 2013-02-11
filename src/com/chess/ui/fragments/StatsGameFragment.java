package com.chess.ui.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.stats.GameStatsItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.tasks.SaveGameStatsTask;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ChessDarkSpinnerIconAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.01.13
 * Time: 10:37
 */
public class StatsGameFragment extends CommonLogicFragment implements AdapterView.OnItemSelectedListener {

	private static final String TAG = "StatsGameFragment";

	private final static int LIVE_STANDARD = 0;
	private final static int LIVE_BLITZ = 1;
	private final static int LIVE_LIGHTNING = 2;
	private final static int DAILY_CHESS = 3;
	private final static int DAILY_CHESS960 = 4;

	private Spinner statsSpinner;
	private StatsItemUpdateListener statsItemUpdateListener;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private String gameType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_stats_game_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		statsSpinner = (Spinner) view.findViewById(R.id.statsSpinner);

		List<SelectionItem> sortList = createSpinnerList(getActivity());
		statsSpinner.setAdapter(new ChessDarkSpinnerIconAdapter(getActivity(), sortList));
		statsSpinner.setOnItemSelectedListener(this);
		statsSpinner.setSelection(0);  // TODO remember last selection.
	}

	private void init() {
		statsItemUpdateListener = new StatsItemUpdateListener();
		saveStatsUpdateListener = new SaveStatsUpdateListener();
	}

	private void updateData() {
		SelectionItem selectionItem = (SelectionItem) statsSpinner.getSelectedItem();
		gameType = selectionItem.getCode();

		// get full stats
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAME_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);

		new RequestJsonTask<GameStatsItem>(statsItemUpdateListener).executeTask(loadItem);

		statsSpinner.setEnabled(false);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		updateData();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	private class StatsItemUpdateListener extends ActionBarUpdateListener<GameStatsItem> {

		public StatsItemUpdateListener() {
			super(getInstance(), GameStatsItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			// TODO -> File | Settings | File Templates.
		}

		@Override
		public void updateData(GameStatsItem returnedObj) {
			super.updateData(returnedObj);

			// Save stats to DB
			new SaveGameStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver(), gameType).executeTask();

			// get selected position of spinner
			int position = statsSpinner.getSelectedItemPosition();

			switch (position){
				case LIVE_STANDARD:
					changeInternalFragment(StatsGameLiveFragment.newInstance(LIVE_STANDARD));
					break;
				case LIVE_BLITZ:
					changeInternalFragment(StatsGameLiveFragment.newInstance(LIVE_BLITZ));
					break;
				case LIVE_LIGHTNING:
					changeInternalFragment(StatsGameLiveFragment.newInstance(LIVE_LIGHTNING));
					break;
				case DAILY_CHESS:
					changeInternalFragment(new StatsGameLiveFragment());
					break;
				case DAILY_CHESS960:
					changeInternalFragment(new StatsGameLiveFragment());
					break;
			}
		}
	}

	private class SaveStatsUpdateListener extends ActionBarUpdateListener<GameStatsItem.Data> {

		public SaveStatsUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(GameStatsItem.Data returnedObj) {
			super.updateData(returnedObj);
			statsSpinner.setEnabled(true);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			statsSpinner.setEnabled(true);

			showToast(" code " + resultCode);
		}
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.stats_content_frame, fragment).commit();
	}


	private List<SelectionItem> createSpinnerList(Context context) {
		ArrayList<SelectionItem> selectionItems = new ArrayList<SelectionItem>();

		String[] categories = context.getResources().getStringArray(R.array.game_stats_categories);
		String[] codes = context.getResources().getStringArray(R.array.game_stats_types);
		for (int i = 0; i < categories.length; i++) {
			String category = categories[i];
			SelectionItem selectionItem = new SelectionItem(getIconByCategory(i), category);
			selectionItem.setCode(codes[i]);
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
			case LIVE_LIGHTNING:
				return getResources().getDrawable(R.drawable.ic_live_bullet);
			case DAILY_CHESS:
				return getResources().getDrawable(R.drawable.ic_daily_game);
			case DAILY_CHESS960:
				return getResources().getDrawable(R.drawable.ic_daily960_game);
			default: // case CHESS_MENTOR:
				return getResources().getDrawable(R.drawable.ic_tactics_game);
		}
	}
}
