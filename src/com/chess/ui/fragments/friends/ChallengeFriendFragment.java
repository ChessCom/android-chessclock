package com.chess.ui.fragments.friends;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.EditButton;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailySeekItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.ui.adapters.RecentOpponentsCursorAdapter;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.UserSettingsFragment;
import com.facebook.widget.WebDialog;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.04.13
 * Time: 21:39
 */
public class ChallengeFriendFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private EditButton usernameEditBtn;
	private View headerView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		headerView = inflater.inflate(R.layout.new_challenge_friend_header_view, null, false);
		return inflater.inflate(R.layout.new_challenge_friend_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Cursor cursor = DBDataManager.getRecentOpponentsCursor(getActivity());

		RecentOpponentsCursorAdapter adapter = new RecentOpponentsCursorAdapter(getActivity(), cursor);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		view.findViewById(R.id.challengeFriendHeaderView).setOnClickListener(this);
		headerView.findViewById(R.id.chesscomFriendsView).setOnClickListener(this);
		usernameEditBtn = (EditButton) headerView.findViewById(R.id.usernameEditBtn);
		headerView.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		headerView.findViewById(R.id.facebookFriendsView).setOnClickListener(this);
		headerView.findViewById(R.id.yourContactsView).setOnClickListener(this);
		headerView.findViewById(R.id.yourEmailView).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.challengeFriendHeaderView) {
			getActivityFace().toggleRightMenu();
		} else if (id == R.id.chesscomFriendsView) {
			getActivityFace().openFragment(new FriendsFragment()); // TODO add friend selection
			getActivityFace().toggleRightMenu();
		} else if (id == R.id.dailyPlayBtn) {
			createDailyChallenge(getTextFromField(usernameEditBtn));
		} else if (id == R.id.facebookFriendsView) {
			sendRequestDialog();
		} else if (id == R.id.yourEmailView) {

		} else if (id == R.id.yourContactsView) {

		}
	}

	private void sendRequestDialog() {
		Session facebookSession = Session.getActiveSession();
		if (facebookSession == null || facebookSession.isClosed() || !facebookSession.isOpened()) {
			getActivityFace().changeRightFragment(UserSettingsFragment.showWithCloseBtn(UserSettingsFragment.RIGHT_INVITES_ID));
			return;
		}

		Bundle params = new Bundle();
		params.putString("message", "Let's play Chess via Chess.com android app");

		WebDialog requestsDialog = (
				new WebDialog.RequestsDialogBuilder(getActivity(),
						facebookSession,
						params))
				.setOnCompleteListener(new WebDialog.OnCompleteListener() {

					@Override
					public void onComplete(Bundle values, FacebookException error) {
						if (error != null) {
							if (error instanceof FacebookOperationCanceledException) {
								showToast("Request cancelled");
							} else {
								showToast("Network Error");
							}
						} else {
							final String requestId = values.getString("request");
							if (requestId != null) {
								showToast("Request sent");
							} else {
								showToast("Request cancelled");
							}
						}
					}

				})
				.build();
		requestsDialog.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		String opponentName;
		if(DBDataManager.getInt(cursor, DBConstants.V_I_PLAY_AS) == RestHelper.P_BLACK) {
			opponentName = DBDataManager.getString(cursor, DBConstants.V_WHITE_USERNAME);
		} else {
			opponentName = DBDataManager.getString(cursor, DBConstants.V_BLACK_USERNAME);
		}
		createDailyChallenge(opponentName);
	}

	private void createDailyChallenge(String opponentName) {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = new DailyGameConfig.Builder().build();

		int color = dailyGameConfig.getUserColor();
		int days = dailyGameConfig.getDaysPerMove();
		int gameType = dailyGameConfig.getGameType();
		String isRated = dailyGameConfig.isRated() ? RestHelper.V_TRUE : RestHelper.V_FALSE;

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_SEEKS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);

		new RequestJsonTask<DailySeekItem>(new CreateChallengeUpdateListener()).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingProgress(show);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}

		@Override
		public void errorHandle(String resultMessage) {
			showSinglePopupDialog(getString(R.string.error), resultMessage);
		}
	}
}
