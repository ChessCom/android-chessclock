package com.chess.ui.fragments.friends;

import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.FriendsItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveFriendsListTask;
import com.chess.ui.adapters.FriendsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.01.13
 * Time: 11:38
 */
public class FriendsFragment extends CommonLogicFragment {

	private ListView listView;
	private View loadingView;
	private TextView emptyView;
	private FriendsCursorAdapter friendsAdapter;
	private FriendsCursorUpdateListener friendsCursorUpdateListener;
	private FriendsUpdateListener friendsUpdateListener;
	private SaveFriendsListUpdateListener saveFriendsListUpdateListener;
	private boolean need2update = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		friendsAdapter = new FriendsCursorAdapter(getContext(), null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_friends_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.friends);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(friendsAdapter);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_add, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		init();

		if (need2update){
			boolean haveSavedData = DBDataManager.haveSavedFriends(getActivity());

			if (AppUtils.isNetworkAvailable(getActivity())) {
				updateData();
			} else if(!haveSavedData){
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}

			if (haveSavedData) {
				loadFromDb();
			}
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		releaseResources();
	}

	private void init() {
		friendsCursorUpdateListener = new FriendsCursorUpdateListener();
		saveFriendsListUpdateListener = new SaveFriendsListUpdateListener();
		friendsUpdateListener = new FriendsUpdateListener();

	}

	private void updateData() {
		if (!AppUtils.isNetworkAvailable(getActivity())) {
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_FRIENDS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));

		new RequestJsonTask<FriendsItem>(friendsUpdateListener).executeTask(loadItem);
	}

	private class FriendsUpdateListener extends ChessUpdateListener<FriendsItem> {

		public FriendsUpdateListener() {
			super(FriendsItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(FriendsItem returnedObj) {
			super.updateData(returnedObj);

			new SaveFriendsListTask(saveFriendsListUpdateListener, returnedObj.getData(),
					getContentResolver()).executeTask();

		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (resultCode == StaticData.INTERNAL_ERROR) {
				emptyView.setText("Internal error occurred");
				showEmptyView(true);
			}
		}
	}

	private class SaveFriendsListUpdateListener extends ChessUpdateListener<FriendsItem.Data> {
		public SaveFriendsListUpdateListener() {
			super();
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(FriendsItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFromDb();
		}
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(friendsCursorUpdateListener,
				DbHelper.getUserParams(AppData.getUserName(getActivity()), DBConstants.FRIENDS),
				getContentResolver()).executeTask();

	}

	private class FriendsCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			friendsAdapter.changeCursor(returnedObj);
			listView.setAdapter(friendsAdapter);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText(R.string.no_games);
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

	private void releaseResources() {
		friendsCursorUpdateListener.releaseContext();
		friendsCursorUpdateListener = null;
		friendsUpdateListener.releaseContext();
		friendsUpdateListener = null;
		saveFriendsListUpdateListener.releaseContext();
		saveFriendsListUpdateListener = null;
	}

	private void showEmptyView(boolean show) {
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

	private void showLoadingView(boolean show) {
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
}
