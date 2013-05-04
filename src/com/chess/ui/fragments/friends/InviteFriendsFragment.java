package com.chess.ui.fragments.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.UserSettingsFragment;
import com.facebook.widget.WebDialog;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.04.13
 * Time: 21:39
 */
public class InviteFriendsFragment extends CommonLogicFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_invite_friend_to_play_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.inviteHeaderView).setOnClickListener(this);
		view.findViewById(R.id.chesscomFriendsView).setOnClickListener(this);
//		view.findViewById(R.id.opponentEditBtn).setOnClickListener(this); // TODO
		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.facebookFriendsView).setOnClickListener(this);
		view.findViewById(R.id.yourContactsView).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.inviteHeaderView) {
			getActivityFace().toggleMenu(SlidingMenu.RIGHT);
		} else if (id == R.id.chesscomFriendsView) {
			getActivityFace().openFragment(new FriendsFragment());
			getActivityFace().toggleMenu(SlidingMenu.RIGHT);
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
			getActivityFace().changeRightFragment(UserSettingsFragment.showFromRightInvites());
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
}
