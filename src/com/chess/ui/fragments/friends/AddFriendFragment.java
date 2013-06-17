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
import com.chess.backend.entity.new_api.RequestItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
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

	private EditButton usernameEditBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_add_friend_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.add_friend);

		ListView listView = (ListView) view.findViewById(R.id.listView);

		Cursor cursor = DBDataManager.getRecentOpponentsCursor(getActivity());

		RecentOpponentsCursorAdapter adapter = new RecentOpponentsCursorAdapter(getActivity(), cursor);

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View headerView = inflater.inflate(R.layout.new_add_friend_header_view, null, false);
		listView.addHeaderView(headerView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		usernameEditBtn = (EditButton) headerView.findViewById(R.id.usernameEditBtn);
		headerView.findViewById(R.id.addFriendBtn).setOnClickListener(this);
		headerView.findViewById(R.id.facebookFriendsView).setOnClickListener(this);
		headerView.findViewById(R.id.yourContactsView).setOnClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		facebookUiHelper = new UiLifecycleHelper(getActivity(), callback);
		facebookUiHelper.onCreate(savedInstanceState);
		facebookActive = true;

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
		}
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		String opponentName = DBDataManager.getString(cursor, DBConstants.V_OPPONENT_NAME);
		createFriendRequest(opponentName, getString(R.string.add_friend_request_message));
	}

	private void createFriendRequest(String username, String message) {
		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.setLoadPath(RestHelper.CMD_FRIENDS_REQUEST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_USERNAME, username);
		loadItem.addRequestParams(RestHelper.P_MESSAGE, message);

		new RequestJsonTask<RequestItem>(new RequestFriendListener()).executeTask(loadItem);
	}

	private class RequestFriendListener extends ChessUpdateListener<RequestItem> {

		private RequestFriendListener() {
			super(RequestItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingProgress(show);
		}

		@Override
		public void updateData(RequestItem returnedObj) {
			super.updateData(returnedObj);

			showToast("Request sent");
		}
	}

}
