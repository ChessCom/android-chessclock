package com.chess.ui.fragments.stats;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.stats.GameStatsItem;
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

	public final static int DAILY_CHESS = 0;
	public final static int LIVE_STANDARD = 1;
	public final static int LIVE_BLITZ = 2;
	public final static int LIVE_LIGHTNING = 3;
	public final static int DAILY_CHESS960 = 4;
	public static final int TACTICS = 5;
	public static final int LESSONS = 6;

	private static final String CATEGORY = "mode";
	private static final String USERNAME = "username";

	private Spinner statsSpinner;
	private StatsItemUpdateListener statsItemUpdateListener;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private String gameType;
	private String username;
	private int categoryId;
	private int previousPosition = -1;

	public StatsGameFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY, LIVE_STANDARD);
		setArguments(bundle);
	}


	public static StatsGameFragment createInstance(int code, String username) {
		StatsGameFragment fragment = new StatsGameFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(CATEGORY, code);
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
			categoryId = getArguments().getInt(CATEGORY);
		} else {
			username = savedInstanceState.getString(USERNAME);
			categoryId = savedInstanceState.getInt(CATEGORY);
		}

		if (TextUtils.isEmpty(username)) {
			username = getUsername();
		}

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_stats_game_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.stats);

		statsSpinner = (Spinner) view.findViewById(R.id.statsSpinner);

		List<SelectionItem> sortList = createSpinnerList(getActivity());
		statsSpinner.setAdapter(new DarkSpinnerIconAdapter(getActivity(), sortList));
		statsSpinner.setOnItemSelectedListener(this);
		int selectedPosition = categoryId;
		statsSpinner.setSelection(selectedPosition);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(CATEGORY, categoryId);
		outState.putString(USERNAME, username);
	}

	private void init() {
		statsItemUpdateListener = new StatsItemUpdateListener();
		saveStatsUpdateListener = new SaveStatsUpdateListener();
	}

	private void updateUiData() {
		SelectionItem selectionItem = (SelectionItem) statsSpinner.getSelectedItem();
		gameType = selectionItem.getCode();

		// get full stats
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAME_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		loadItem.addRequestParams(RestHelper.P_VIEW_USERNAME, username);

		new RequestJsonTask<GameStatsItem>(statsItemUpdateListener).executeTask(loadItem);

		statsSpinner.setEnabled(false);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (position == previousPosition) {
			return;
		}
		previousPosition = position;

		if (position == TACTICS) {
			changeInternalFragment(new StatsGameTacticsFragment());
		} else if (position == LESSONS) {
			changeInternalFragment(new StatsGameLessonsFragment());
		} else {
			updateUiData();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	private class StatsItemUpdateListener extends ChessLoadUpdateListener<GameStatsItem> {

		public StatsItemUpdateListener() {
			super(GameStatsItem.class);
		}

		@Override
		public void updateData(GameStatsItem returnedObj) {
			super.updateData(returnedObj);

			// Save stats to DB
			new SaveGameStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver(),
					gameType, username).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			showSelectedStats();
		}
	}

	private void showSelectedStats() {
		statsSpinner.setEnabled(true);

		// get selected position of spinner
		int position = statsSpinner.getSelectedItemPosition(); // specify which data to load in details

		changeInternalFragment(StatsGameDetailsFragment.createInstance(position, username));
	}

	private class SaveStatsUpdateListener extends ChessLoadUpdateListener<GameStatsItem.Data> {

		@Override
		public void updateData(GameStatsItem.Data returnedObj) {
			super.updateData(returnedObj);
			showSelectedStats();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			statsSpinner.setEnabled(true);
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
	 *	Daily - Chess
	 *	Live - Standard
	 *	Live - Blitz
	 *	Live - Bullet
	 *	Daily - Chess960
	 *	Tactics
	 *	Coach Manager
	 * @return Drawable icon for index
	 */
	private Drawable getIconByCategory(int index) {
		Context context = getActivity();
		switch (index) {
			case DAILY_CHESS:
				return new IconDrawable(context, R.string.ic_daily_game);
			case LIVE_STANDARD:
				return new IconDrawable(context, R.string.ic_live_standard);
			case LIVE_BLITZ:
				return new IconDrawable(context, R.string.ic_live_blitz);
			case LIVE_LIGHTNING:
				return new IconDrawable(context, R.string.ic_live_bullet);
			case DAILY_CHESS960:
				return new IconDrawable(context, R.string.ic_daily960_game);
			default: // case LESSONS:
				return new IconDrawable(context, R.string.ic_help);
		}
	}
}
