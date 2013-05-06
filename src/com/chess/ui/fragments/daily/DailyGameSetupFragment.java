package com.chess.ui.fragments.daily;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailySeekItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.engine.configs.NewDailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.friends.InviteFriendsFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.04.13
 * Time: 12:29
 */
public class DailyGameSetupFragment extends CommonLogicFragment {

	private static final String ERROR_TAG = "error popup";

	private NewDailyGameConfig.Builder gameConfigBuilder;
	private CreateChallengeUpdateListener createChallengeUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameConfigBuilder = new NewDailyGameConfig.Builder();
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_daily_game_setup_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.daily_chess);

		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.dailyTimeSelectionBtn).setOnClickListener(this);
		view.findViewById(R.id.inviteFriend1Btn).setOnClickListener(this);
		view.findViewById(R.id.inviteFriend2Btn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.dailyOptionsView).setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		// TODO -> File | Settings | File Templates.
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.dailyPlayBtn) {
			createDailyChallenge();
		} else if (id == R.id.autoMatchBtn) {
		} else if (id == R.id.dailyTimeSelectionBtn) {
		} else if (id == R.id.inviteFriend1Btn) {
		} else if (id == R.id.inviteFriend2Btn) {
		} else if (id == R.id.playFriendView) {
			getActivityFace().changeRightFragment(new InviteFriendsFragment());
			getActivityFace().toggleRightMenu();
		} else if (id == R.id.dailyOptionsView) {
			getActivityFace().changeRightFragment(new DailyGamesOptionsFragment());
			getActivityFace().toggleRightMenu();
		}
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		NewDailyGameConfig newDailyGameConfig = gameConfigBuilder.build();

		int color = newDailyGameConfig.getUserColor();
		int days = newDailyGameConfig.getDaysPerMove();
		int gameType = newDailyGameConfig.getGameType();
		String isRated = newDailyGameConfig.isRated() ? RestHelper.V_TRUE : RestHelper.V_FALSE;
		String opponentName = newDailyGameConfig.getOpponentName();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_SEEKS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		if (!TextUtils.isEmpty(opponentName)) {
			loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);
		}

		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.online_game_created);
		}

		@Override
		public void errorHandle(String resultMessage) {
			showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);
		}
	}
}
