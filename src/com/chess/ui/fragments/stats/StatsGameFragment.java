package com.chess.ui.fragments.stats;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.stats.GameStatsItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.tasks.SaveGameStatsTask;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.DarkSpinnerIconAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.drawables.IconDrawable;

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

	public final static int LIVE_STANDARD = 0;
	public final static int LIVE_BLITZ = 1;
	public final static int LIVE_LIGHTNING = 2;
	public final static int DAILY_CHESS = 3;
	public final static int DAILY_CHESS960 = 4;

	private static final String CATEGORY = "mode";

	private Spinner statsSpinner;
	private StatsItemUpdateListener statsItemUpdateListener;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private String gameType;

	public StatsGameFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY, LIVE_STANDARD);
		setArguments(bundle);
	}

	public static StatsGameFragment createInstance(int code) {
		StatsGameFragment frag = new StatsGameFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY, code);
		frag.setArguments(bundle);
		return frag;
	}

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
		statsSpinner.setAdapter(new DarkSpinnerIconAdapter(getActivity(), sortList));
		statsSpinner.setOnItemSelectedListener(this);
		int selectedPosition = getArguments().getInt(CATEGORY);
		statsSpinner.setSelection(selectedPosition);
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
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
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


	private class StatsItemUpdateListener extends ChessUpdateListener<GameStatsItem> {

		public StatsItemUpdateListener() {
			super(GameStatsItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingProgress(show);
		}

		@Override
		public void updateData(GameStatsItem returnedObj) {
			super.updateData(returnedObj);

			// Save stats to DB
			new SaveGameStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver(), gameType).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			// get selected position of spinner
			int position = statsSpinner.getSelectedItemPosition(); // specify which data to load in details

			changeInternalFragment(StatsGameDetailsFragment.createInstance(position));
		}
	}

	private class SaveStatsUpdateListener extends ChessUpdateListener<GameStatsItem.Data> {

		@Override
		public void showProgress(boolean show) {
			showLoadingProgress(show);
		}

		@Override
		public void updateData(GameStatsItem.Data returnedObj) {
			super.updateData(returnedObj);
			statsSpinner.setEnabled(true);

			// get selected position of spinner
			int position = statsSpinner.getSelectedItemPosition(); // specify which data to load in details

			changeInternalFragment(StatsGameDetailsFragment.createInstance(position));
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
		Context context = getActivity();
		switch (index) {
			case LIVE_STANDARD:
				return new IconDrawable(context, R.string.ic_live_standard);
			case LIVE_BLITZ:
				return new IconDrawable(context, R.string.ic_live_blitz);
			case LIVE_LIGHTNING:
				return new IconDrawable(context, R.string.ic_live_bullet);
			case DAILY_CHESS:
				return new IconDrawable(context, R.string.ic_daily_game);
			case DAILY_CHESS960:
				return new IconDrawable(context, R.string.ic_daily960_game);
			default: // case CHESS_MENTOR:
				return new IconDrawable(context, R.string.ic_help);
		}
	}
}
