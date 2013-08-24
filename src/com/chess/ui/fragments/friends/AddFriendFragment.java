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
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.RequestItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.ui.adapters.RecentOpponentsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.UserSettingsFragment;
import com.facebook.widget.WebDialog;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.06.13
 * Time: 16:16
 */
public class AddFriendFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final int CONTACT_PICKER_RESULT = 1001;

	private EditButton usernameEditBtn;
	private View headerView;
	private View emailIconTxt;
	private View emailTxt;
	private EditButton emailEditBtn;
	private Button addEmailBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		headerView = inflater.inflate(R.layout.new_add_friend_header_view, null, false);
		return inflater.inflate(R.layout.new_add_friend_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.add_friend);

		ListView listView = (ListView) view.findViewById(R.id.listView);

		Cursor cursor = DbDataManager.getRecentOpponentsCursor(getActivity(), getUsername());

		RecentOpponentsCursorAdapter adapter = new RecentOpponentsCursorAdapter(getActivity(), cursor);

		listView.addHeaderView(headerView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		usernameEditBtn = (EditButton) headerView.findViewById(R.id.usernameEditBtn);
		headerView.findViewById(R.id.addFriendBtn).setOnClickListener(this);
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		facebookUiHelper = new UiLifecycleHelper(getActivity(), callback);
		facebookUiHelper.onCreate(savedInstanceState);
		setFacebookActive(true);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.addFriendBtn) {
			createFriendRequest(getTextFromField(usernameEditBtn), getString(R.string.add_friend_request_message));
		} else if (id == R.id.facebookFriendsView) {
			sendRequestDialog();
		} else if (id == R.id.yourContactsView) {
			startContactPicker();
		} else if (id == R.id.yourEmailView) {
			showEmailEdit(true);
		} else if (id == R.id.addEmailBtn) {
			LoadItem loadItem = LoadHelper.postFriendByEmail(getUserToken(), getTextFromField(emailEditBtn), getString(R.string.add_friend_request_message));

			new RequestJsonTask<RequestItem>(new RequestFriendListener()).executeTask(loadItem);
			showEmailEdit(false);
		}
	}

	private void showEmailEdit(boolean show) {
		emailIconTxt.setVisibility(show ? View.GONE : View.VISIBLE);
		emailTxt.setVisibility(show ? View.GONE : View.VISIBLE);
		emailEditBtn.setVisibility(show ? View.VISIBLE : View.GONE);
		addEmailBtn.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	private void sendRequestDialog() {
		Session facebookSession = Session.getActiveSession();
		if (facebookSession == null || facebookSession.isClosed() || !facebookSession.isOpened()) {
			getActivityFace().openFragment(UserSettingsFragment.showWithCloseBtn(UserSettingsFragment.CENTER_INVITES_ID));
			return;
		}

		Bundle params = new Bundle();
		params.putString("message", getString(R.string.facebook_invite_friend_to_play));

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

	public void startContactPicker() {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		String opponentName;
		if (DbDataManager.getInt(cursor, DbScheme.V_I_PLAY_AS) == RestHelper.P_BLACK) {
			opponentName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
		} else {
			opponentName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
		}
		createFriendRequest(opponentName, getString(R.string.add_friend_request_message));
	}

	private void createFriendRequest(String username, String message) {
		LoadItem loadItem = LoadHelper.postFriend(getUserToken(), username, message);

		new RequestJsonTask<RequestItem>(new RequestFriendListener()).executeTask(loadItem);
	}

	private class RequestFriendListener extends ChessLoadUpdateListener<RequestItem> {

		private RequestFriendListener() {
			super(RequestItem.class);
		}

		@Override
		public void updateData(RequestItem returnedObj) {
			super.updateData(returnedObj);

			showToast(R.string.request_sent);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.RESOURCE_NOT_FOUND) {

					showSinglePopupDialog(R.string.username_not_found);
				} else {
					super.errorHandle(resultCode);
				}
			} else {
				super.errorHandle(resultCode);
			}
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
				createFriendRequest(email,  getString(R.string.add_friend_request_message));
//				showToast("email = " + email); // TODO maybe add email confirmation logic
			}
		}
	}
}
