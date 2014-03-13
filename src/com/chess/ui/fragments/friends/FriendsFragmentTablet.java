package com.chess.ui.fragments.friends;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import com.chess.R;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.messages.NewMessageFragment;
import com.chess.ui.fragments.profiles.ProfileTabsFragmentTablet;
import com.chess.ui.interfaces.FragmentParentFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.11.13
 * Time: 12:00
 */
public class FriendsFragmentTablet extends FriendsFragment {

	private FragmentParentFace parentFace;

	public FriendsFragmentTablet() {
	}

	public static FriendsFragmentTablet createInstance(FragmentParentFace parentFace, String username) {
		FriendsFragmentTablet fragment = new FriendsFragmentTablet();
		fragment.parentFace = parentFace;
		fragment.username = username;
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.common_categories_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// hide spinner
		view.findViewById(R.id.categoriesSpinner).setVisibility(View.GONE);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.challengeImgBtn) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);
			opponentName = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

			String title = getString(R.string.challenge) + Symbol.SPACE + opponentName + Symbol.QUESTION;
			popupItem.setNegativeBtnId(R.string.daily);
			popupItem.setPositiveBtnId(R.string.live);
			showPopupDialog(title, CREATE_CHALLENGE_TAG);

		} else if (view.getId() == R.id.messageImgBtn) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);
			String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

			parentFace.changeFragment(NewMessageFragment.createInstance(username));
		} else if (view.getId() == R.id.friendListItemView) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);
			String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

			getActivityFace().openFragment(ProfileTabsFragmentTablet.createInstance(username));
		} else {
			super.onClick(view);
		}
	}

	@Override
	protected void setAdapter(ListAdapter adapter) {
		((GridView) listView).setAdapter(adapter);
	}

	@Override
	protected void widgetsInit(View view) {
		listView = (GridView) view.findViewById(R.id.listView);
		setAdapter(paginationAdapter);

		// showing ads if needed
		initUpgradeAndAdWidgets(view);
	}
}
