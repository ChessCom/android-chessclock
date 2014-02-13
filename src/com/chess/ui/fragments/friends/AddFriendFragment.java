package com.chess.ui.fragments.friends;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import com.chess.backend.entity.api.RequestItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.model.OpponentItem;
import com.chess.statics.AppConstants;
import com.chess.ui.adapters.RecentOpponentsItemsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.widgets.EditButton;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.UserSettingsFragment;
import com.facebook.widget.WebDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.06.13
 * Time: 16:16
 */
public class AddFriendFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private EditButton usernameEditBtn;
	private View headerView;
	private View emailIconTxt;
	private View emailTxt;
	private EditButton emailEditBtn;
	private Button addEmailBtn;
	private RecentOpponentsItemsAdapter opponentsItemsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Cursor cursor = DbDataManager.getRecentOpponentsCursor(getActivity(), getUsername());
		List<OpponentItem> opponentItems = new ArrayList<OpponentItem>();
		List<String> opponentNames = new ArrayList<String>();
		String username = getUsername();

		if (cursor != null && cursor.moveToFirst()) {
			do {

				String opponentName = DbDataManager.getString(cursor, DbScheme.V_BLACK_USERNAME);
				String avatarUrl = DbDataManager.getString(cursor, DbScheme.V_BLACK_AVATAR);
				if (opponentName.equals(username)) {
					opponentName = DbDataManager.getString(cursor, DbScheme.V_WHITE_USERNAME);
					avatarUrl = DbDataManager.getString(cursor, DbScheme.V_WHITE_AVATAR);
				}

				if (!opponentNames.contains(opponentName)) {
					opponentNames.add(opponentName);
					opponentItems.add(new OpponentItem(opponentName, avatarUrl));
				}
			} while (cursor.moveToNext());
		}

		if (cursor != null) {
			cursor.close();
		}

		opponentsItemsAdapter = new RecentOpponentsItemsAdapter(getActivity(), opponentItems, getImageFetcher());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		headerView = inflater.inflate(R.layout.add_friend_header_view, null, false);
		return inflater.inflate(R.layout.add_friend_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.add_friend);

		ListView listView = (ListView) view.findViewById(R.id.listView);

		listView.addHeaderView(headerView);
		listView.setAdapter(opponentsItemsAdapter);
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

		facebookUiHelper = new UiLifecycleHelper(getActivity(), this);
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
			sendEmailInvite();
		} else if (id == R.id.yourEmailView) {
			showEmailEdit(true);
		} else if (id == R.id.addEmailBtn) {
			createFriendRequestByEmail(getTextFromField(emailEditBtn));
		}
	}

	private void createFriendRequestByEmail(String email) {
		LoadItem loadItem = LoadHelper.postFriendByEmail(getUserToken(), email, getString(R.string.add_friend_request_message));

		new RequestJsonTask<RequestItem>(new RequestFriendListener()).executeTask(loadItem);
		showEmailEdit(false);
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

	public void sendEmailInvite() {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType(AppConstants.MIME_TYPE_MESSAGE_RFC822);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Let's Play on Chess.com!");
		emailIntent.putExtra(Intent.EXTRA_TEXT, "Hey let's Play on Chess.com!");
		startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		OpponentItem opponentItem = (OpponentItem) parent.getItemAtPosition(position);

		createFriendRequest(opponentItem.getName(), getString(R.string.add_friend_request_message));
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
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}


}
