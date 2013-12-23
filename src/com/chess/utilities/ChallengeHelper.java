package com.chess.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.DailySeekItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.SelectionItem;
import com.chess.statics.AppData;
import com.chess.statics.Symbol;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.interfaces.ChallengeModeSetListener;
import quickaction.MultiActionItem;
import quickaction.MultiQuickAction;
import quickaction.QuickAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.12.13
 * Time: 6:32
 */
public class ChallengeHelper {

	private static final String END_VACATION_TAG = "end vacation popup";

	//action id
	public static final int ID_DAILY_1_1 = 1;
	public static final int ID_DAILY_1_2 = 2;
	public static final int ID_DAILY_1_3 = 3;

	public static final int ID_LIVE_1_1 = 4;
	public static final int ID_LIVE_1_2 = 5;
	public static final int ID_LIVE_2_1 = 6;
	public static final int ID_LIVE_2_2 = 7;
	public static final int ID_LIVE_2_3 = 8;
	public static final int ID_LIVE_3_1 = 9;
	public static final int ID_LIVE_3_2 = 10;

	// array positions
	private static final int DAY_1 = 0; // 1 day
	private static final int DAY_2 = 2; // 3 days
	private static final int DAY_3 = 4; // 7 days

	private static final int LIVE_1_1 = 4; // 5 | 15
	private static final int LIVE_1_2 = 0; // 30 min

	private static final int LIVE_2_1 = 1; // 10 min
	private static final int LIVE_2_2 = 2; // 5 | 2
	private static final int LIVE_2_3 = 6; // 3 min

	private static final int LIVE_3_1 = 3; // 2 | 1
	private static final int LIVE_3_2 = 7; // 1 min
	private ChallengeModeSetListener listener;
	private BasePopupsFragment fragment;
	private AppData appData;

	private Context context;

	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private MultiQuickAction quickAction;
	private int[] newDailyGameButtonsArray;
	private String[] newLiveGameButtonsArray;
	private DailyGameConfig.Builder dailyGameConfigBuilder;
	private LiveGameConfig.Builder liveGameConfigBuilder;


	public ChallengeHelper(ChallengeModeSetListener listener) {
		init(listener);
	}

	public ChallengeHelper(ChallengeModeSetListener listener, boolean useOnSide) {
		init(listener);
		quickAction.setUseOnSide(useOnSide);
	}

	private void init(ChallengeModeSetListener listener) {
		this.context = listener.getMeContext();
		this.listener = listener;
		this.fragment = (BasePopupsFragment) listener;

		appData = new AppData(context);

		quickAction = createMultiQuickAction();

		newDailyGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
		newLiveGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);

		dailyGameConfigBuilder = new DailyGameConfig.Builder();
		liveGameConfigBuilder = new LiveGameConfig.Builder();

		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	private Resources getResources() {
		return context.getResources();
	}

	private MultiQuickAction createMultiQuickAction() {
		//create QuickAction. Use QuickAction.VERTICAL or QuickAction.HORIZONTAL param to define layout orientation
		final MultiQuickAction quickAction = new MultiQuickAction(getActivity(), QuickAction.VERTICAL);

		{// daily row
			List<SelectionItem> actionItems = new ArrayList<SelectionItem>();
			actionItems.add(new SelectionItem(ID_DAILY_1_1, getString(R.string.day_arg, 1))); // 1 day
			actionItems.add(new SelectionItem(ID_DAILY_1_2, getString(R.string.days_arg, 3))); // 3 days
			actionItems.add(new SelectionItem(ID_DAILY_1_3, getString(R.string.days_arg, 7))); // 7 days

			quickAction.addActionItem(new MultiActionItem(R.string.ic_daily_game, actionItems));
		}

		{ // live standart row
			List<SelectionItem> actionItems = new ArrayList<SelectionItem>();
			actionItems.add(new SelectionItem(ID_LIVE_1_1, getLiveModeButtonLabel("15 | 10"))); // 15 | 10
			actionItems.add(new SelectionItem(ID_LIVE_1_2, getLiveModeButtonLabel("30"))); // 30 min

			quickAction.addActionItem(new MultiActionItem(R.string.ic_live_standard, actionItems));
		}

		{ // live blitz row
			List<SelectionItem> actionItems = new ArrayList<SelectionItem>();
			actionItems.add(new SelectionItem(ID_LIVE_2_1, getLiveModeButtonLabel("10"))); // 10
			actionItems.add(new SelectionItem(ID_LIVE_2_2, getLiveModeButtonLabel("5 | 2"))); // 5 | 2
			actionItems.add(new SelectionItem(ID_LIVE_2_3, getLiveModeButtonLabel("3"))); // 3 min

			quickAction.addActionItem(new MultiActionItem(R.string.ic_live_blitz, actionItems));
		}

		{ // live bullet row
			List<SelectionItem> actionItems = new ArrayList<SelectionItem>();
			actionItems.add(new SelectionItem(ID_LIVE_3_1, getLiveModeButtonLabel("2 | 1"))); // 2 | 1
			actionItems.add(new SelectionItem(ID_LIVE_3_2, getLiveModeButtonLabel("1"))); // 1 min

			quickAction.addActionItem(new MultiActionItem(R.string.ic_live_bullet, actionItems));
		}

		//Set listener for action item clicked
		quickAction.setOnActionItemClickListener(new MultiQuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(MultiQuickAction source, int pos, int actionId) {

				//here we can filter which action item was clicked with pos or actionId parameter
				switch (actionId) {
					case ID_DAILY_1_1:
						setDefaultDailyTimeMode(DAY_1);
						break;
					case ID_DAILY_1_2:
						setDefaultDailyTimeMode(DAY_2);
						break;
					case ID_DAILY_1_3:
						setDefaultDailyTimeMode(DAY_3);
						break;

					case ID_LIVE_1_1:
						setDefaultLiveTimeMode(LIVE_1_1);
						break;
					case ID_LIVE_1_2:
						setDefaultLiveTimeMode(LIVE_1_2);
						break;

					case ID_LIVE_2_1:
						setDefaultLiveTimeMode(LIVE_2_1);
						break;
					case ID_LIVE_2_2:
						setDefaultLiveTimeMode(LIVE_2_2);
						break;
					case ID_LIVE_2_3:
						setDefaultLiveTimeMode(LIVE_2_3);
						break;

					case ID_LIVE_3_1:
						setDefaultLiveTimeMode(LIVE_3_1);
						break;
					case ID_LIVE_3_2:
						setDefaultLiveTimeMode(LIVE_3_2);
						break;
				}
			}
		});

		return quickAction;
	}

	private void setDefaultLiveTimeMode(int mode) {
		listener.setDefaultLiveTimeMode(mode);

		liveGameConfigBuilder.setTimeFromLabel(newLiveGameButtonsArray[mode]);
		getAppData().setDefaultLiveMode(mode);
		getAppData().setLastUsedDailyMode(false);
	}

	private void setDefaultDailyTimeMode(int mode) {
		listener.setDefaultDailyTimeMode(mode);

		dailyGameConfigBuilder.setDaysPerMove(newDailyGameButtonsArray[mode]);
		getAppData().setDefaultDailyMode(mode);
		getAppData().setLastUsedDailyMode(true);
	}

	private Context getActivity() {
		return context;
	}

	public String getLiveModeButtonLabel(int mode) {
	    return getLiveModeButtonLabel(newLiveGameButtonsArray[mode]);
	}
	protected String getLiveModeButtonLabel(String label) {
		if (label.contains(Symbol.SLASH)) { // "5 | 2"
			return label;
		} else { // "10 min"
			return getString(R.string.min_arg, label);
		}
	}

	private String getString(int stringId, Object... args) {
		return context.getString(stringId, args);
	}

	public void createLiveChallenge() {
		liveGameConfigBuilder.setTimeFromLabel(newLiveGameButtonsArray[getAppData().getDefaultLiveMode()]);
		LiveGameWaitFragment waitFragment = LiveGameWaitFragment.createInstance(liveGameConfigBuilder.build());
		((CommonLogicFragment) fragment).getActivityFace().openFragment(waitFragment);
	}

	public void createDailyChallenge() {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = dailyGameConfigBuilder.build();

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private String getUserToken() {
		return appData.getUserToken();
	}

	public AppData getAppData() {
		return appData;
	}

	public void show(View view) {
		quickAction.show(view);
	}

	public void showOnSide(View view) {
		quickAction.showOnSide(view);
	}

	public void dismiss() {
		quickAction.dismiss();
	}

	public String getDailyModeButtonLabel(int mode){
		return getDaysString(newDailyGameButtonsArray[mode]);
	}


	protected String getDaysString(int cnt) {
		if (cnt > 1) {
			return getString(R.string.days_arg, cnt);
		} else {
			return getString(R.string.day_arg, cnt);
		}
	}

	private class CreateChallengeUpdateListener extends ActionBarUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super((CoreActivityActionBar) getActivity(), fragment, DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			fragment.showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.YOUR_ARE_ON_VACATAION) {
					fragment.showPopupDialog(R.string.leave_vacation_to_play_q, END_VACATION_TAG);
				} else {
					super.errorHandle(resultCode);
				}
			}
		}
	}

}
