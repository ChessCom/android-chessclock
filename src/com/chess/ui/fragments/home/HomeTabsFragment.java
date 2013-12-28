package com.chess.ui.fragments.home;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.*;
import com.chess.backend.entity.api.DailyCurrentGameData;
import com.chess.backend.entity.api.DailyGamesAllItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.NavigationMenuFragment;
import com.chess.ui.fragments.daily.DailyGamesFragment;
import com.chess.ui.fragments.daily.DailyGamesFragmentTablet;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 21:41
 */
public class HomeTabsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener,
		FragmentParentFace {

	private static final long FIRST_INIT_DELAY = 5 * 1000;

	private RadioGroup tabRadioGroup;
	private int previousCheckedId = NON_INIT;
	private DailyGamesUpdateListener dailyGamesUpdateListener;
	private Boolean showDailyGamesFragment;
	private View tabsLoadProgressBar;
	private boolean haveSavedData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dailyGamesUpdateListener = new DailyGamesUpdateListener();

		if (!getAppData().isFirstInitFinished()) {
			handler.postDelayed(firstInitRunnable, FIRST_INIT_DELAY);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_base_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(true);

		getActivityFace().setCustomActionBarViewId(R.layout.new_home_actionbar);

		((TextView) view.findViewById(R.id.leftTabBtn)).setText(R.string.play);
		((TextView) view.findViewById(R.id.centerTabBtn)).setText(R.string.learn);
		view.findViewById(R.id.rightTabBtn).setVisibility(View.GONE);
//		((TextView) view.findViewById(R.id.rightTabBtn)).setText(R.string.feed);

		showActionBar(true);

		tabRadioGroup = (RadioGroup) view.findViewById(R.id.tabRadioGroup);
		tabRadioGroup.setOnCheckedChangeListener(this);

		tabsLoadProgressBar = view.findViewById(R.id.tabsLoadProgressBar);
		tabsLoadProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// activate Left
		CommonLogicFragment leftMenuFragment = (CommonLogicFragment) findFragmentByTag(NavigationMenuFragment.class.getSimpleName());
		if (leftMenuFragment == null) {
			leftMenuFragment = new NavigationMenuFragment();
		}
		getActivityFace().changeLeftFragment(leftMenuFragment);

		// and right menu fragments
		getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));
	}

	@Override
	public void onResume() {
		super.onResume();

		new LoadDataFromDbTask(new DbCursorUpdateListener(DbScheme.Tables.USER_STATS_LIVE_STANDARD.name()),
				DbHelper.getAll(DbScheme.Tables.USER_STATS_LIVE_STANDARD),
				getContentResolver()).executeTask();
		new LoadDataFromDbTask(new DbCursorUpdateListener(DbScheme.Tables.USER_STATS_LIVE_BLITZ.name()),
				DbHelper.getAll(DbScheme.Tables.USER_STATS_LIVE_BLITZ),
				getContentResolver()).executeTask();
		new LoadDataFromDbTask(new DbCursorUpdateListener(DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.name()),
				DbHelper.getAll(DbScheme.Tables.USER_STATS_LIVE_LIGHTNING),
				getContentResolver()).executeTask();

		// check if user have daily games in progress or completed. May check in DB
		// get games_id's and compare it to local DB
		// if there are game_id which we don't have, then fetch it

		haveSavedData = DbDataManager.haveSavedDailyGame(getActivity(), getUsername());
		if (haveSavedData) {
			showDailyGamesFragment = true;
			if (previousCheckedId == NON_INIT) {
				tabRadioGroup.check(R.id.leftTabBtn);
			}
			updateTabs();
		} else if (isNetworkAvailable() && !TextUtils.isEmpty(getUserToken())) { // this check is for logout quick process
			LoadItem loadItem = LoadHelper.getAllGamesFiltered(getUserToken(), RestHelper.V_ID);
			new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
		} else {
			if (!isNetworkAvailable()) {
				showDailyGamesFragment = false;
			}

			if (previousCheckedId == NON_INIT) {
				tabRadioGroup.check(R.id.leftTabBtn);
			}
			updateTabs();
		}
	}

	private class DbCursorUpdateListener extends ChessUpdateListener<Cursor> { // use to show Db table content

		private String tableName;

		public DbCursorUpdateListener(String tableName) {
			this.tableName = tableName;
		}  // Used for test

		@Override
		public void updateData(Cursor cursor) {
			super.updateData(cursor);

			if (HONEYCOMB_PLUS_API) {
				AppUtils.printTableContent(cursor, tableName);
			}
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateTabs();
	}

	private void updateTabs() {
		int checkedButtonId = tabRadioGroup.getCheckedRadioButtonId();
		if (checkedButtonId != previousCheckedId) {
			if (showDailyGamesFragment == null && checkedButtonId == R.id.leftTabBtn) { // don't change internal fragment during orientation change while data is loading
				return;
			}
			previousCheckedId = checkedButtonId;
			switch (checkedButtonId) {
				case R.id.leftTabBtn: {
					Fragment fragment;
					if (showDailyGamesFragment) {
						if (!isTablet) {
							fragment = DailyGamesFragment.createInstance(HomeTabsFragment.this, DailyGamesFragment.HOME_MODE);
						} else {
							fragment = DailyGamesFragmentTablet.createInstance(HomeTabsFragment.this, DailyGamesFragment.HOME_MODE);
						}
					} else {
						if (!isTablet) {
							fragment = findFragmentByTag(HomePlayFragmentNew.class.getSimpleName());
							if (fragment == null) {
								fragment = new HomePlayFragmentNew();
							}
						} else {
							fragment = findFragmentByTag(HomePlayFragmentTablet.class.getSimpleName());
							if (fragment == null) {
								fragment = new HomePlayFragmentTablet();
							}
						}
					}
					changeInternalFragment(fragment);
					break;
				}
				case R.id.centerTabBtn: {
					Fragment fragment = findFragmentByTag(HomeLearnFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new HomeLearnFragment();
					}
					changeInternalFragment(fragment);
					break;
				}
//				case R.id.rightTabBtn: { // not used in first iteration
//					Fragment fragment = findFragmentByTag(HomeFeedFragment.class.getSimpleName());
//					if (fragment == null) {
//						fragment = new HomeFeedFragment();
//					}
//					changeInternalFragment(fragment);
//					break;
//				}
			}
		}
		tabsLoadProgressBar.setVisibility(View.GONE);
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commit();
	}

	@Override
	public void changeFragment(BasePopupsFragment fragment) {
		changeInternalFragment(fragment);
	}

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyGamesAllItem> {

		public DailyGamesUpdateListener() {
			super(DailyGamesAllItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (getActivityFace().getPullToRefreshAttacher() != null) {
				getActivityFace().getPullToRefreshAttacher().showProgress(show);
			} else {
				tabsLoadProgressBar.setVisibility(show? View.VISIBLE : View.GONE);
			}
		}

		@Override
		public void updateData(DailyGamesAllItem returnedObj) {
			super.updateData(returnedObj);

			// current games
			List<DailyCurrentGameData> currentGamesList = returnedObj.getData().getCurrent();

			showDailyGamesFragment = DbDataManager.checkAndDeleteNonExistCurrentGames(getContentResolver(), currentGamesList, getUsername());

			if (previousCheckedId == NON_INIT) {
				tabRadioGroup.check(R.id.leftTabBtn);
			}

			updateTabs();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			showDailyGamesFragment = haveSavedData;
			updateTabs();
		}
	}

	private Runnable firstInitRunnable =  new Runnable() {
		@Override
		public void run() {
			if (getActivity() == null) {
				return;
			}

			if (!DbDataManager.haveSavedFriends(getActivity(), getUsername()) && AppUtils.isNetworkAvailable(getActivity())) {
				getActivity().startService(new Intent(getActivity(), GetAndSaveFriends.class));
			}
			if (!DbDataManager.haveSavedDailyStats(getActivity(), getUsername()) && AppUtils.isNetworkAvailable(getActivity())) {
				// update stats in async intent service and save in Db there
				getActivity().startService(new Intent(getActivity(), GetAndSaveUserStats.class));
			}
		}
	};
}
