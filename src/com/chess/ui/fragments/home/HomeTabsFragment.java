package com.chess.ui.fragments.home;

import android.content.Intent;
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
import com.chess.backend.GetAndSaveUserStats;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameData;
import com.chess.backend.entity.api.daily_games.DailyCurrentGamesItem;
import com.chess.backend.entity.api.daily_games.MyMoveItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.LeftNavigationFragment;
import com.chess.ui.fragments.daily.DailyGamesFragment;
import com.chess.ui.fragments.daily.DailyGamesFragmentTablet;
import com.chess.ui.fragments.daily.DailyGamesRightFragment;
import com.chess.ui.interfaces.FragmentParentFace;

import java.util.List;

import static com.chess.backend.RestHelper.P_FIELDS_;
import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 21:41
 */
public class HomeTabsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener, FragmentParentFace {

	private static final String WHATS_NEW_TAG = "whats new popup";

	private RadioGroup tabRadioGroup;
	private int previousCheckedId = NON_INIT;
	private MyMoveUpdateListener myMoveUpdateListener;
	private DailyGamesUpdateListener dailyGamesUpdateListener;
	private Boolean showDailyGamesFragment;
	private View tabsLoadProgressBar;
	private boolean haveSavedData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myMoveUpdateListener = new MyMoveUpdateListener();
		dailyGamesUpdateListener = new DailyGamesUpdateListener();

		if (!getAppData().isUserSawWhatsNewFeature()) {
			String title = getString(R.string.whats_new);
			String message = getString(R.string.whats_new_features);
			popupItem.setButtons(1);
			showPopupDialog(title, message, WHATS_NEW_TAG);

			getAppData().setUserSawWhatsNewFeature(true);

			if (!getAppData().isFirstInitFinished()) {
				// update user stats
				getActivity().startService(new Intent(getActivity(), GetAndSaveUserStats.class));
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.base_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(true);

		getActivityFace().setCustomActionBarViewId(R.layout.home_actionbar);

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
		CommonLogicFragment leftMenuFragment = (CommonLogicFragment) findFragmentByTag(LeftNavigationFragment.class.getSimpleName());
		if (leftMenuFragment == null) {
			leftMenuFragment = new LeftNavigationFragment();
		}
		getActivityFace().changeLeftFragment(leftMenuFragment);

		// and right menu fragments
		getActivityFace().changeRightFragment(new DailyGamesRightFragment());
	}

	@Override
	public void onResume() {
		super.onResume();

//		new LoadDataFromDbTask(new DbCursorUpdateListener(DbScheme.Tables.NOTIFICATION_GAMES_OVER.name()),
//				DbHelper.getAll(DbScheme.Tables.NOTIFICATION_GAMES_OVER),
//				getContentResolver()).executeTask();

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
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_GAME_MOVES);
			loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());

			new RequestJsonTask<MyMoveItem>(myMoveUpdateListener).executeTask(loadItem);
		} else {
			if (!isNetworkAvailable()) {
				showDailyGamesFragment = false;
			}

			if (previousCheckedId == NON_INIT) {
				tabRadioGroup.check(R.id.leftTabBtn);
			} else {
				updateTabs();
			}
		}
	}

//	private class DbCursorUpdateListener extends ChessUpdateListener<Cursor> { // use to show Db table content, do not remove
//
//		private String tableName;
//
//		public DbCursorUpdateListener(String tableName) {
//			this.tableName = tableName;
//		}  // Used for test
//
//		@Override
//		public void updateData(Cursor cursor) {
//			super.updateData(cursor);
//
//			if (HONEYCOMB_PLUS_API) {
//				AppUtils.printCursorContent(cursor, tableName);
//			}
//		}
//	}

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
							fragment = DailyGamesFragment.createInstance(DailyGamesFragment.HOME_MODE);
						} else {
							fragment = DailyGamesFragmentTablet.createInstance(HomeTabsFragment.this, DailyGamesFragment.HOME_MODE);
						}
					} else {
						if (!isTablet) {
							fragment = findFragmentByTag(HomePlayFragment.class.getSimpleName());
							if (fragment == null) {
								fragment = new HomePlayFragment();
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

	private class MyMoveUpdateListener extends ChessUpdateListener<MyMoveItem> {

		public MyMoveUpdateListener() {
			super(MyMoveItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			tabsLoadProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
		}

		@Override
		public void updateData(MyMoveItem returnedObj) {
			super.updateData(returnedObj);

			boolean isMyTurn = returnedObj.getData().isIsMyTurn();
			if (isMyTurn) {
				showDailyGamesFragment = true;

				if (previousCheckedId == NON_INIT) {
					tabRadioGroup.check(R.id.leftTabBtn);
				} else {
					updateTabs();
				}
			} else { // we might have games in progress, but it's not my turn
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_CURRENT);
				loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());
				loadItem.addRequestParams(P_FIELDS_, RestHelper.V_ID);

				new RequestJsonTask<DailyCurrentGamesItem>(dailyGamesUpdateListener).executeTask(loadItem);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			showDailyGamesFragment = haveSavedData;
			updateTabs();
		}
	}

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyCurrentGamesItem> {

		public DailyGamesUpdateListener() {
			super(DailyCurrentGamesItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			tabsLoadProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
		}

		@Override
		public void updateData(DailyCurrentGamesItem returnedObj) {
			super.updateData(returnedObj);

			// current games
			List<DailyCurrentGameData> currentGamesList = returnedObj.getData();
			showDailyGamesFragment = currentGamesList.size() > 0;
			DbDataManager.checkAndDeleteNonExistCurrentGames(getContentResolver(), currentGamesList, getUsername());

			if (previousCheckedId == NON_INIT) {
				tabRadioGroup.check(R.id.leftTabBtn);
			} else {
				updateTabs();
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			showDailyGamesFragment = haveSavedData;
			updateTabs();
		}
	}

	@Override
	protected void afterLogin() {
		super.afterLogin();

		backToHomeFragment();
	}
}
