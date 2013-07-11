package com.chess.ui.fragments.friends;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.chess.EditButton;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailySeekItem;
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

	private static final int CONTACT_PICKER_RESULT = 1001;

	private EditButton usernameEditBtn;
	private View headerView;
	private View emailIconTxt;
	private View emailTxt;
	private EditButton emailEditBtn;
	private Button addEmailBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		headerView = inflater.inflate(R.layout.new_challenge_friend_header_view, null, false);
		return inflater.inflate(R.layout.new_challenge_friend_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Cursor cursor = DBDataManager.getRecentOpponentsCursor(getActivity(), getUserName());

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
			getActivityFace().toggleRightMenu();
		} else if (id == R.id.chesscomFriendsView) {
			getActivityFace().openFragment(new FriendsFragment()); // TODO add friend selection
			getActivityFace().toggleRightMenu();
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
			startContactPicker();
		}
	}

	private void showEmailEdit(boolean show) {
		emailIconTxt.setVisibility(show ? View.GONE : View.VISIBLE);
		emailTxt.setVisibility(show ? View.GONE : View.VISIBLE);
		emailEditBtn.setVisibility(show ? View.VISIBLE : View.GONE);
		addEmailBtn.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void startContactPicker() {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
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
		int isRated = dailyGameConfig.isRated() ? 1 : 0;

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), days, color, isRated, gameType, opponentName);
		new RequestJsonTask<DailySeekItem>(new CreateChallengeUpdateListener()).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessLoadUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == CONTACT_PICKER_RESULT) {
			// handle contact results
			Bundle extras = data.getExtras();
			if (extras == null) {
				return;
			}

			Uri result = data.getData();
			if (result == null) {
				return;
			}
			// get the contact id from the Uri
			String id = result.getLastPathSegment();

			// query for everything email
			Cursor cursor = getContentResolver().query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
					new String[]{id}, null);

			if (cursor != null && cursor.moveToFirst()) {
				int emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
				String email = cursor.getString(emailIdx);
				createDailyChallenge(email);
//				showToast("email = " + email); // TODO maybe add email confirmation logic
			}
		}
	}
}
