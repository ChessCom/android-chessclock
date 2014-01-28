package com.chess.ui.fragments.friends;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.AvatarView;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.FriendsPaginationAdapter;
import com.chess.ui.adapters.FriendsSimpleCursorAdapter;
import com.chess.ui.fragments.RightPlayFragment;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.01.14
 * Time: 9:18
 */
public class FriendsRightFragment extends FriendsFragment implements AdapterView.OnItemClickListener {

	public static final int DAILY_OPPONENT_REQUEST = 0;
	public static final int LIVE_OPPONENT_REQUEST = 1;
	private int code;

	public FriendsRightFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, DAILY_OPPONENT_REQUEST);
		setArguments(bundle);
	}

	public static FriendsRightFragment createInstance(int requestCode) {
		FriendsRightFragment fragment = new FriendsRightFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, requestCode);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			code = getArguments().getInt(MODE);
		} else {
			code = savedInstanceState.getInt(MODE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(MODE, code);
	}

	@Override
	protected void init() {
		friendsCursorUpdateListener = new FriendsCursorUpdateListener();
		saveFriendsListUpdateListener = new SaveFriendsListUpdateListener();

		friendsAdapter = new FriendsSimpleCursorAdapter(this, null, getImageFetcher(), this);
		friendsAdapter.setFilterQueryProvider(new QueryFilterProvider());
		friendsUpdateListener = new FriendsUpdateListener();
		paginationAdapter = new FriendsPaginationAdapter(getActivity(), friendsAdapter, friendsUpdateListener, null);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.friendListItemView) {
			boolean headerAdded = ((ListView)listView).getHeaderViewsCount() > 0; // used to check if header added
			int offset = headerAdded ? 1 : 0;

			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position + offset);
			opponentName = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

			RightPlayFragment gameOptionsFragment;
			if (code == DAILY_OPPONENT_REQUEST) {
				gameOptionsFragment = RightPlayFragment.createInstance(RightPlayFragment.DAILY_CHALLENGE_MODE, opponentName);
			} else {
				gameOptionsFragment = RightPlayFragment.createInstance(RightPlayFragment.LIVE_CHALLENGE_MODE, opponentName);
			}

			getActivityFace().changeRightFragment(gameOptionsFragment);
		} else {
			super.onClick(view);
		}
	}

	@Override
	protected void widgetsInit(View view) {
		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		{ // add header for Random Opponent
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View headerView = inflater.inflate(R.layout.new_recent_opponent_item, null, false);
			AvatarView playerImg = (AvatarView) headerView.findViewById(R.id.playerImg);
			TextView playerNameTxt = (TextView) headerView.findViewById(R.id.playerNameTxt);

			Drawable src = new IconDrawable(getActivity(), R.string.ic_vs_random,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
			playerImg.setImageDrawable(new BoardAvatarDrawable(getActivity(), src));

			playerNameTxt.setText(R.string.random);

			((ListView) listView).addHeaderView(headerView);
		}

		setAdapter(paginationAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		boolean headerAdded = ((ListView) listView).getHeaderViewsCount() > 0; // used to check if header added

		if (headerAdded && position == 0) {
			RightPlayFragment gameOptionsFragment;
			if (code == DAILY_OPPONENT_REQUEST) {
				gameOptionsFragment = RightPlayFragment.createInstance(RightPlayFragment.DAILY_CHALLENGE_MODE, Symbol.EMPTY);
			} else {
				gameOptionsFragment = RightPlayFragment.createInstance(RightPlayFragment.LIVE_CHALLENGE_MODE, Symbol.EMPTY);
			}

			getActivityFace().changeRightFragment(gameOptionsFragment);
		}
	}
}
