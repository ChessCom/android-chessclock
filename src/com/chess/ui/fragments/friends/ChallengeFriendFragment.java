package com.chess.ui.fragments.friends;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.daily_games.DailySeekItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.SelectionItem;
import com.chess.statics.AppConstants;
import com.chess.ui.adapters.RecentOpponentsCursorAdapter;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.daily.DailyGameOptionsFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.widgets.EditButton;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.UserSettingsFragment;
import com.facebook.widget.WebDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.04.13
 * Time: 21:39
 */
public class ChallengeFriendFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	protected static final String FRIEND_SELECTION_TAG = "friend select popup";

	private EditButton usernameEditBtn;
	private View headerView;
	private View emailIconTxt;
	private View emailTxt;
	private EditButton emailEditBtn;
	private Button addEmailBtn;
	private RecentOpponentsCursorAdapter adapter;
	private List<SelectionItem> friendsList;
	private PopupOptionsMenuFragment friendSelectFragment;
	private FriendSelectedListener friendSelectedListener;

	public ChallengeFriendFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, RIGHT_MENU_MODE);
		setArguments(bundle);
	}

	public static ChallengeFriendFragment createInstance(int mode){
		ChallengeFriendFragment fragment = new ChallengeFriendFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		{ // load friends from DB
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(), DbScheme.Tables.FRIENDS));

			friendsList = new ArrayList<SelectionItem>();
			friendsList.add(new SelectionItem(null, getString(R.string.random)));
			if (cursor != null && cursor.moveToFirst()) {
				do {
					friendsList.add(new SelectionItem(null, DbDataManager.getString(cursor, DbScheme.V_USERNAME)));
				} while (cursor.moveToNext());
			}
			if (cursor != null) {
				cursor.close();
			}

			friendsList.get(0).setChecked(true);
		}

		friendSelectedListener = new FriendSelectedListener();

		Cursor cursor = DbDataManager.getRecentOpponentsCursor(getActivity(), getUsername());
		adapter = new RecentOpponentsCursorAdapter(getActivity(), cursor, getImageFetcher());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		headerView = inflater.inflate(R.layout.new_challenge_friend_header_view, null, false);
		return inflater.inflate(R.layout.new_challenge_friend_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

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

		emailIconTxt = headerView.findViewById(R.id.emailIconTxt);
		emailTxt = headerView.findViewById(R.id.emailTxt);
		emailEditBtn = (EditButton) headerView.findViewById(R.id.emailEditBtn);
		addEmailBtn = (Button) headerView.findViewById(R.id.addEmailBtn);
		addEmailBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.challengeFriendHeaderView) {
			if (getArguments().getInt(MODE) == CENTER_MODE) {
				getActivityFace().toggleRightMenu();
			} else {
				getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));
			}
		} else if (id == R.id.chesscomFriendsView) {

			SparseArray<String> optionsMap = new SparseArray<String>();
			for (int i = 0; i < friendsList.size(); i++) {
				String friend = friendsList.get(i).getText();
				optionsMap.put(i, friend);
			}

			friendSelectFragment = PopupOptionsMenuFragment.createInstance(friendSelectedListener, optionsMap);
			friendSelectFragment.show(getFragmentManager(), FRIEND_SELECTION_TAG);
		} else if (id == R.id.dailyPlayBtn) {
			createDailyChallenge(getTextFromField(usernameEditBtn));
		} else if (id == R.id.facebookFriendsView) {
			sendRequestDialog();
		} else if (id == R.id.yourEmailView) {
			showEmailEdit(true);
		} else if (id == R.id.addEmailBtn) {
			createDailyChallenge(getTextFromField(emailEditBtn));
			showEmailEdit(false);
		} else if (id == R.id.yourContactsView) {
			sendEmailInvite();
		}
	}

	private void showEmailEdit(boolean show) {
		emailIconTxt.setVisibility(show ? View.GONE : View.VISIBLE);
		emailTxt.setVisibility(show ? View.GONE : View.VISIBLE);
		emailEditBtn.setVisibility(show ? View.VISIBLE : View.GONE);
		addEmailBtn.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void sendEmailInvite() {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType(AppConstants.MIME_TYPE_MESSAGE_RFC822);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Let's Play on Chess.com!");
		emailIntent.putExtra(Intent.EXTRA_TEXT, "Hey let's Play on Chess.com!");
		startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
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
		if(DbDataManager.getInt(cursor, DbScheme.V_I_PLAY_AS) == RestHelper.P_BLACK) {
			opponentName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
		} else {
			opponentName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
		}
		createDailyChallenge(opponentName);
	}

	private void createDailyChallenge(String opponentName) {
		DailyGameConfig dailyGameConfig = new DailyGameConfig.Builder().build();
		dailyGameConfig.setOpponentName(opponentName);

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(new CreateChallengeUpdateListener()).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessLoadUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.challenge_created, R.string.you_will_notified_when_game_starts);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.INVALID_EMAIL_DOMAIN) {
					showToast(R.string.unable_to_send_challenge);
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}

	private class FriendSelectedListener implements PopupListSelectionFace {

		@Override
		public void onValueSelected(int code) {
			friendSelectFragment.dismiss();
			friendSelectFragment = null;

			String friend = friendsList.get(code).getText();

			getActivityFace().changeRightFragment(DailyGameOptionsFragment.createInstance(RIGHT_MENU_MODE, friend));
		}

		@Override
		public void onDialogCanceled() {
			friendSelectFragment = null;
		}
	}
}
