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
import com.chess.backend.entity.new_api.stats.UserStatsItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.tasks.SaveUserStatsTask;
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
public class StatsUserFragment extends CommonLogicFragment implements AdapterView.OnItemSelectedListener {

	private static final String TAG = "StatsUserFragment";

	private final static int LIVE_STANDARD = 0;
	private final static int LIVE_BLITZ = 1;
	private final static int LIVE_LIGHTNING = 2;
	private final static int DAILY_CHESS = 3;
	private final static int DAILY_CHESS960 = 4;
	private final static int TACTICS = 5;         // use for user stats
	private final static int CHESS_MENTOR = 6;

	private Spinner statsSpinner;
	private StatsItemUpdateListener statsItemUpdateListener;
	private SaveStatsUpdateListener saveStatsUpdateListener;

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
		statsSpinner.setSelection(0);  // TODO remember last selection.
	}

	private void init() {
		statsItemUpdateListener = new StatsItemUpdateListener();
		saveStatsUpdateListener = new SaveStatsUpdateListener();
	}

	private void updateData() {
		// get full stats
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_USER_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<UserStatsItem>(statsItemUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		updateData();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	private class StatsItemUpdateListener extends ChessUpdateListener<UserStatsItem> {

		public StatsItemUpdateListener() {
			super(UserStatsItem.class);
		}

		@Override
		public void updateData(UserStatsItem returnedObj) {
			super.updateData(returnedObj);

			// Save stats to DB
			new SaveUserStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();


			// get selected position of spinner
			int position = statsSpinner.getSelectedItemPosition();

			switch (position){
				case LIVE_STANDARD:
					changeInternalFragment(StatsGameDetailsFragment.createInstance(LIVE_STANDARD));
					break;
				case LIVE_BLITZ:
					changeInternalFragment(StatsGameDetailsFragment.createInstance(LIVE_BLITZ));
					break;
				case LIVE_LIGHTNING:
					changeInternalFragment(StatsGameDetailsFragment.createInstance(LIVE_LIGHTNING));
					break;
				case DAILY_CHESS:
					changeInternalFragment(new StatsGameDetailsFragment());
					break;
				case DAILY_CHESS960:
					changeInternalFragment(new StatsGameDetailsFragment());
					break;
				case TACTICS:
					changeInternalFragment(new StatsGameTacticsFragment());
					break;
				case CHESS_MENTOR:
					changeInternalFragment(new StatsGameDetailsFragment());
					break;
			}
		}
	}

	private class SaveStatsUpdateListener extends ChessUpdateListener<UserStatsItem.Data> {

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
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
			case TACTICS:
				return new IconDrawable(context, R.string.ic_help);
			default: // case CHESS_MENTOR:
				return new IconDrawable(context, R.string.ic_lessons);
		}
	}
}
