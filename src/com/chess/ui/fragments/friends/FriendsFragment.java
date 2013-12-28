package com.chess.ui.fragments.friends;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.DailySeekItem;
import com.chess.backend.entity.api.FriendsItem;
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveFriendsListTask;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.FriendsCursorAdapter;
import com.chess.ui.adapters.FriendsPaginationAdapter;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.live.LiveGameOptionsFragment;
import com.chess.ui.fragments.messages.NewMessageFragment;
import com.chess.ui.fragments.profiles.ProfileTabsFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.01.13
 * Time: 11:38
 */
public class FriendsFragment extends CommonLogicFragment implements ItemClickListenerFace {

	protected static final String CREATE_CHALLENGE_TAG = "create challenge confirm popup";
	private static final String END_VACATION_TAG = "end vacation popup";

	protected AbsListView listView;
	protected View loadingView;
	protected TextView emptyView;
	protected FriendsCursorAdapter friendsAdapter;
	private FriendsCursorUpdateListener friendsCursorUpdateListener;
	protected FriendsUpdateListener friendsUpdateListener;
	private SaveFriendsListUpdateListener saveFriendsListUpdateListener;
	protected String opponentName;
	protected String username;
	protected FriendsPaginationAdapter paginationAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (TextUtils.isEmpty(username)) {
			username = getUsername();
		}

		init();

		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.friends);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		widgetsInit(view);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_add, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedFriends(getActivity(), username);

			if (isNetworkAvailable()) {
				updateData();
			} else if (!haveSavedData) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}

			if (haveSavedData) {
				loadFromDb();
			}
		} else {
			setAdapter(paginationAdapter);
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		if (isNetworkAvailable()) {
			updateData();
		}
	}

	protected void updateData() {
		LoadItem loadItem = LoadHelper.getFriends(getUserToken(), username);
		paginationAdapter.updateLoadItem(loadItem);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

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

			getActivityFace().openFragment(NewMessageFragment.createInstance(username));
		} else if (view.getId() == R.id.friendListItemView) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);
			String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);

			getActivityFace().openFragment(ProfileTabsFragment.createInstance(username));
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	protected void setAdapter(ListAdapter adapter) {
		((ListView) listView).setAdapter(adapter);
	}

	protected class FriendsUpdateListener extends ChessUpdateListener<FriendsItem.Data> {

		@Override
		public void updateListData(List<FriendsItem.Data> returnedObj) {
			super.updateListData(returnedObj);

			new SaveFriendsListTask(saveFriendsListUpdateListener, returnedObj, getContentResolver()).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.INTERNAL_ERROR) {
				emptyView.setText("Internal error occurred");
				showEmptyView(true);
			}
		}
	}

	private class SaveFriendsListUpdateListener extends ChessUpdateListener<FriendsItem.Data> {

		public SaveFriendsListUpdateListener() {
			super(FriendsItem.Data.class);
		}

		@Override
		public void updateData(FriendsItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFromDb();
		}
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(friendsCursorUpdateListener, DbHelper.getFriends(username),
				getContentResolver()).executeTask();
	}

	private class FriendsCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			friendsAdapter.changeCursor(returnedObj);
			paginationAdapter.notifyDataSetChanged();
			need2update = false;
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText(R.string.you_have_not_added_friends_yet);
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add:
				getActivityFace().openFragment(new AddFriendFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSearchAutoCompleteQuery(String query) {
		if (!inSearch && !need2update) {
			inSearch = true;
			if (friendsAdapter == null) {
				return;
			}
			Cursor cursor = friendsAdapter.runQueryOnBackgroundThread(query);
			if (cursor != null) {
				friendsAdapter.changeCursor(cursor);
			}
			inSearch = false;
		}
	}

	private class QueryFilterProvider implements FilterQueryProvider {

		@Override
		public Cursor runQuery(CharSequence constraint) {
			if (getActivity() == null) { // if fragment was closed
				return null;
			}

			String query = (String) constraint;
			String[] selectionArgs = new String[]{DbScheme.V_USER, DbScheme.V_USERNAME, DbScheme.V_LOCATION};
			String selection = DbDataManager.concatLikeArguments(selectionArgs);

			String[] arguments = new String[selectionArgs.length];
			arguments[0] = DbDataManager.concatArguments(query);

			for (int i = 1; i < selectionArgs.length; i++) {
				arguments[i] = DbDataManager.anyLikeMatch(query);
			}

			QueryParams queryParams = new QueryParams();
			queryParams.setUri(DbScheme.uriArray[DbScheme.Tables.FRIENDS.ordinal()]);
			queryParams.setSelection(selection);
			queryParams.setArguments(arguments);

			Cursor cursor = DbDataManager.query(getContentResolver(), queryParams);
			cursor.moveToFirst();
			return cursor;
		}
	}

	private void releaseResources() {
		friendsCursorUpdateListener.releaseContext();
		friendsCursorUpdateListener = null;
//		friendsUpdateListener.releaseContext();
//		friendsUpdateListener = null;
		saveFriendsListUpdateListener.releaseContext();
		saveFriendsListUpdateListener = null;
	}

	protected void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	protected void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			if (friendsAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(CREATE_CHALLENGE_TAG)) {
			getActivityFace().changeRightFragment(LiveGameOptionsFragment.createInstance(RIGHT_MENU_MODE, opponentName));
			getActivityFace().toggleRightMenu();
		} else if (tag.equals(END_VACATION_TAG)) {
			LoadItem loadItem = LoadHelper.deleteOnVacation(getUserToken());
			new RequestJsonTask<VacationItem>(new VacationUpdateListener()).executeTask(loadItem);
		}
		super.onPositiveBtnClick(fragment);
	}


	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(CREATE_CHALLENGE_TAG)) {
			createDailyChallenge(opponentName);
		}
		super.onNegativeBtnClick(fragment);
	}

	private void createDailyChallenge(String opponentName) {
		this.opponentName = opponentName;
		// create challenge using formed configuration
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
				if (serverCode == ServerErrorCodes.YOUR_ARE_ON_VACATAION) {

					showPopupDialog(R.string.leave_vacation_to_challenge_q, END_VACATION_TAG);
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		public VacationUpdateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			createDailyChallenge(opponentName);
		}
	}

	private void init() {
		friendsCursorUpdateListener = new FriendsCursorUpdateListener();
		saveFriendsListUpdateListener = new SaveFriendsListUpdateListener();

		friendsAdapter = new FriendsCursorAdapter(this, null, getImageFetcher());
		friendsAdapter.setFilterQueryProvider(new QueryFilterProvider());
		friendsUpdateListener = new FriendsUpdateListener();
		paginationAdapter = new FriendsPaginationAdapter(getActivity(), friendsAdapter, friendsUpdateListener, null);
	}

	protected void widgetsInit(View view) {
		listView = (ListView) view.findViewById(R.id.listView);
		setAdapter(paginationAdapter);

	}

}
