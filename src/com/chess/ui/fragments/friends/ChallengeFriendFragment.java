package com.chess.ui.fragments.friends;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.db.DBDataManager;
import com.chess.ui.adapters.RecentOpponentsCursorAdapter;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_challenge_friend_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView listView = (ListView) view.findViewById(R.id.listView);

		Cursor cursor = DBDataManager.getRecentOpponentsCursor(getActivity());

		RecentOpponentsCursorAdapter adapter = new RecentOpponentsCursorAdapter(getActivity(), cursor);

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View headerView = inflater.inflate(R.layout.new_challenge_friend_header_view, null, false);
		listView.addHeaderView(headerView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

		view.findViewById(R.id.challengeFriendHeaderView).setOnClickListener(this);
		headerView.findViewById(R.id.chesscomFriendsView).setOnClickListener(this);
//		headerView.findViewById(R.id.opponentEditBtn).setOnClickListener(this); // TODO
		headerView.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		headerView.findViewById(R.id.facebookFriendsView).setOnClickListener(this);
		headerView.findViewById(R.id.yourContactsView).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.challengeFriendHeaderView) {
			getActivityFace().toggleRightMenu();
		} else if (id == R.id.chesscomFriendsView) {
			getActivityFace().openFragment(new FriendsFragment());
			getActivityFace().toggleRightMenu();
		} else if (id == R.id.dailyPlayBtn) {
		} else if (id == R.id.facebookFriendsView) {
			sendRequestDialog();
		} else if (id == R.id.yourContactsView) {
			getActivityFace().changeRightFragment(new UserSettingsFragment());
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

	}
}
