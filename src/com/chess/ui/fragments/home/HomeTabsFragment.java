package com.chess.ui.fragments.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.GetAndSaveFriends;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailyCurrentGameData;
import com.chess.backend.entity.new_api.DailyFinishedGameData;
import com.chess.backend.entity.new_api.DailyGamesAllItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.NavigationMenuFragment;
import com.chess.ui.fragments.daily.DailyGamesFragment;
import com.chess.ui.fragments.daily.DailyGamesNotificationFragment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 21:41
 */
public class HomeTabsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener {

	private static final int NON_INIT = -1;

	private RadioGroup tabRadioGroup;
	private int previousCheckedId = NON_INIT;
	private DailyGamesUpdateListener dailyGamesUpdateListener;
	private boolean showDailyGamesFragment = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dailyGamesUpdateListener = new DailyGamesUpdateListener();

		if (!DBDataManager.haveSavedFriends(getActivity())) {
			getActivity().startService(new Intent(getActivity(), GetAndSaveFriends.class)); // TODO adjust properly
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
		((TextView) view.findViewById(R.id.rightTabBtn)).setText(R.string.feed);

		showActionBar(true);

		tabRadioGroup = (RadioGroup) view.findViewById(R.id.tabRadioGroup);
		tabRadioGroup.setOnCheckedChangeListener(this);
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
		CommonLogicFragment rightMenuFragment = (CommonLogicFragment) findFragmentByTag(DailyGamesNotificationFragment.class.getSimpleName());
		if (rightMenuFragment == null) {
			rightMenuFragment = new DailyGamesNotificationFragment();
		}
		getActivityFace().changeRightFragment(rightMenuFragment);
	}

	@Override
	public void onStart() {
		super.onStart();

//		new LoadDataFromDbTask(new GamesCursorUpdateListener(),
//				DbHelper.getAllByUri(DBConstants.DAILY_FINISHED_LIST_GAMES),
//				getContentResolver()).executeTask();
//
//		new LoadDataFromDbTask(new GamesCursorUpdateListener(),
//				DbHelper.getAllByUri(DBConstants.DAILY_CURRENT_LIST_GAMES),
//				getContentResolver()).executeTask();

		// check if user have daily games in progress or completed. May check in DB
		// get games_id's and compare it to local DB
		// if there are game_id which we don't have, then fetch it

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES_ALL);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_ID);
		new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

//	private class GamesCursorUpdateListener extends ChessUpdateListener<Cursor> {  // Used for test
//
//		@Override
//		public void updateData(Cursor cursor) {
//			super.updateData(cursor);
//
//			if (HONEYCOMB_PLUS_API) {
//				AppUtils.printTableContent(cursor);
//			}
//		}
//	}




	@Override
	public void onResume() {
		super.onResume();
		if (previousCheckedId == NON_INIT) {
			tabRadioGroup.check(R.id.leftTabBtn);
		}
		setBadgeValueForId(R.id.menu_games, 7); // TODO use properly
//		setBadgeValueForId(R.id.menu_notifications, 7); // TODO use properly
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateTabs();
	}

	private void updateTabs() {
		int checkedButtonId = tabRadioGroup.getCheckedRadioButtonId();
		if (checkedButtonId != previousCheckedId) {
			previousCheckedId = checkedButtonId;
			switch (checkedButtonId) {
				case R.id.leftTabBtn: {
					Fragment fragment;
					if (showDailyGamesFragment) {
						fragment = findFragmentByTag(DailyGamesFragment.class.getSimpleName());
						if (fragment == null) {
							fragment = new DailyGamesFragment();
						}
					} else {
						fragment = findFragmentByTag(HomePlayFragment.class.getSimpleName());
						if (fragment == null) {
							fragment = new HomePlayFragment();
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
				case R.id.rightTabBtn: {
					Fragment fragment = findFragmentByTag(HomeFeedFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new HomeFeedFragment();
					}
					changeInternalFragment(fragment);
					break;
				}
			}
		}
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commit();
	}

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyGamesAllItem> {

		public DailyGamesUpdateListener() {
			super(DailyGamesAllItem.class);
		}

		@Override
		public void updateData(DailyGamesAllItem returnedObj) {
			super.updateData(returnedObj);

			// current games
			List<DailyCurrentGameData> currentGamesList = returnedObj.getData().getCurrent();
			boolean currentGamesLeft = DBDataManager.checkAndDeleteNonExistCurrentGames(getContext(), currentGamesList);

			// finished
			List<DailyFinishedGameData> finishedGameDataList = returnedObj.getData().getFinished();
			boolean finishedGamesLeft = DBDataManager.checkAndDeleteNonExistFinishedGames(getContext(), finishedGameDataList);

			showDailyGamesFragment = currentGamesLeft || finishedGamesLeft;

			if (previousCheckedId == NON_INIT) {
				tabRadioGroup.check(R.id.leftTabBtn);
			}

			updateTabs();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				showToast(ServerErrorCode.getUserFriendlyMessage(getActivity(), serverCode));
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred"); // TODO adjust properly
//				showEmptyView(true);
			}
			updateTabs();
		}
	}
}
