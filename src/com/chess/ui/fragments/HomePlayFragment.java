package com.chess.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailySeekItem;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.ui.engine.NewCompGameConfig;
import com.chess.ui.engine.NewDailyGameConfig;
import com.chess.ui.fragments.game.GameCompFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 18:29
 */
public class HomePlayFragment extends CommonLogicFragment {
	private static final String ERROR_TAG = "error popup";

/*
those are to challenge your friend to play. it just creates a challenge.

those should be random friends who have been online in the last 30 days. (new api! :D)

never display more than 2. but if only 1, or none, show only 1, or none :)

(Friend Name) means their real name. so, for dallin, i would see:

ignoble (Dallin)    [invite]

invite button will just automatically create a challenge, and then show a success message!

play a friend i think is supposed to open friends screen, yes.

Auto-Match should be just a random, open, rated, 3-day seek.
	 */

	private TextView liveRatingTxt;
	private TextView dailyRatingTxt;
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private NewDailyGameConfig.Builder gameConfigBuilder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameConfigBuilder = new NewDailyGameConfig.Builder();
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_play_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		liveRatingTxt = (TextView) view.findViewById(R.id.liveRatingTxt);
		dailyRatingTxt = (TextView) view.findViewById(R.id.dailyRatingTxt);

		view.findViewById(R.id.liveTimeSelectBtn).setOnClickListener(this);
		view.findViewById(R.id.livePlayBtn).setOnClickListener(this);
		view.findViewById(R.id.autoMatchBtn).setOnClickListener(this);
		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.inviteFriend1Btn).setOnClickListener(this);
		view.findViewById(R.id.inviteFriend2Btn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.vsCompHeaderView).setOnClickListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();

		setRatings();
		// load friends, get only 2

	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveTimeSelectBtn) {

		} else if (view.getId() == R.id.livePlayBtn) {
		} else if (view.getId() == R.id.autoMatchBtn) {
			createDailyChallenge(); // TODO adjust
		} else if (view.getId() == R.id.dailyPlayBtn) {
			createDailyChallenge(); // TODO adjust
		} else if (view.getId() == R.id.inviteFriend1Btn) {
			createDailyChallenge(); // TODO adjust
		} else if (view.getId() == R.id.inviteFriend2Btn) {
			createDailyChallenge(); // TODO adjust
		} else if (view.getId() == R.id.playFriendView) {
			getActivityFace().openFragment(new FriendsFragment());
		} else if (view.getId() == R.id.vsCompHeaderView) {
			NewCompGameConfig.Builder gameConfigBuilder = new NewCompGameConfig.Builder();
			NewCompGameConfig compGameConfig = gameConfigBuilder.setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE).build();
			getActivityFace().openFragment(GameCompFragment.newInstance(compGameConfig));
		} else if (view.getId() == R.id.playFriendView) {
			getActivityFace().openFragment(new FriendsFragment());
		}

	}

	private void setRatings() {
		// set live rating
		int liveRating = DBDataManager.getUserCurrentRating(getActivity(), DBConstants.GAME_STATS_LIVE_STANDARD);
//		liveRatingTxt.setText(String.valueOf(liveRating));

		// set daily rating
		int dailyRating = DBDataManager.getUserCurrentRating(getActivity(), DBConstants.GAME_STATS_DAILY_CHESS);
//		dailyRatingTxt.setText(String.valueOf(dailyRating));

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
