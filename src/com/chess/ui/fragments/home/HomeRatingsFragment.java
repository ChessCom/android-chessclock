package com.chess.ui.fragments.home;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.FriendsItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveFriendsListTask;
import com.chess.model.RatingListItem;
import com.chess.ui.adapters.FriendsCursorGridAdapter;
import com.chess.ui.adapters.RatingsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.friends.FriendsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.fragments.stats.StatsGameTacticsFragment;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 22:04
 */
public class HomeRatingsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final int AVATAR_SIZE = 80;

	private final static int DAILY_CHESS = 0;
	private final static int LIVE_STANDARD = 1;
	private final static int LIVE_BLITZ = 2;
	private final static int LIVE_LIGHTNING = 3;
	private final static int DAILY_CHESS960 = 4;
	private final static int TACTICS = 5;
	private final static int CHESS_MENTOR = 6;

	private List<RatingListItem> ratingList;
	private RatingsAdapter ratingsAdapter;
	private TextView userNameTxt;
	private TextView userCountryTxt;
	private ImageView userCountryImg;
	private FriendsCursorUpdateListener friendsCursorUpdateListener;
	private FriendsCursorGridAdapter friendsAdapter;
	private FriendsUpdateListener friendsUpdateListener;
	private SaveFriendsListUpdateListener saveFriendsListUpdateListener;
	private FrameLayout userPhotoImg;
	private EnhancedImageDownloader imageDownloader;
	private ScrollView mainScrollView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		imageDownloader = new EnhancedImageDownloader(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_ratings_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mainScrollView = (ScrollView) view.findViewById(R.id.mainScrollView);

		userPhotoImg = (FrameLayout) view.findViewById(R.id.userPhotoImg);
		userNameTxt = (TextView) view.findViewById(R.id.userNameTxt);
		userCountryTxt = (TextView) view.findViewById(R.id.userCountryTxt);
		userCountryImg = (ImageView) view.findViewById(R.id.userCountryImg);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		ratingList = createStatsList(getActivity());
		ratingsAdapter = new RatingsAdapter(getActivity(), ratingList);
		listView.setAdapter(ratingsAdapter);
		listView.setOnItemClickListener(this);

		GridView friendsGridView = (GridView) view.findViewById(R.id.friendsGridView);
		friendsAdapter = new FriendsCursorGridAdapter(getActivity(), null);
		friendsGridView.setAdapter(friendsAdapter);

		setGridViewToCenter(friendsGridView);

		friendsCursorUpdateListener = new FriendsCursorUpdateListener();
		friendsUpdateListener = new FriendsUpdateListener();
		saveFriendsListUpdateListener = new SaveFriendsListUpdateListener();

		view.findViewById(R.id.friendsHeaderView).setOnClickListener(this);
		view.findViewById(R.id.trophiesHeaderView).setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		fillUserStats();

		if (DBDataManager.haveSavedFriends(getActivity(), getUserName())) {
			loadFriendsFromDb();
		} else {
			getFriends();
		}

		setBadgeValueForId(R.id.menu_games, 2); // TODO use properly later for notifications

		userNameTxt.setText(getAppData().getUserName());
		userCountryTxt.setText(getAppData().getUserCountry());
		userCountryImg.setImageDrawable(AppUtils.getUserFlag(getActivity()));

		{// load user avatar
			ProgressImageView progressImageView = new ProgressImageView(getContext(), AVATAR_SIZE);
			int imageSize = (int) (AVATAR_SIZE * getResources().getDisplayMetrics().density);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageSize, imageSize);

			ViewGroup parent = (ViewGroup) progressImageView.getParent();
			if (parent != null) {
				parent.removeAllViews();
			}
			userPhotoImg.addView(progressImageView, params);
			imageDownloader.download(getAppData().getUserAvatar(), progressImageView, AVATAR_SIZE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case LIVE_STANDARD:
			case LIVE_BLITZ:
			case LIVE_LIGHTNING:
			case DAILY_CHESS:
			case DAILY_CHESS960:
				getActivityFace().openFragment(StatsGameFragment.createInstance(position));
				break;
			case TACTICS:
				getActivityFace().openFragment(new StatsGameTacticsFragment());
				break;
			case CHESS_MENTOR: // not used yet
				getActivityFace().openFragment(new StatsGameTacticsFragment());
				break;
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if (v.getId() == R.id.friendsHeaderView) {
			getActivityFace().openFragment(new FriendsFragment());
		} else if (v.getId() == R.id.trophiesHeaderView) {

		}
	}

	private void fillUserStats() {
		// fill ratings
		String[] argument = new String[]{getAppData().getUserName()};

		{// standard
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_LIVE_STANDARD],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(LIVE_STANDARD).setValue(currentRating);
			}
		}
		{// blitz
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_LIVE_BLITZ],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(LIVE_BLITZ).setValue(currentRating);
			}
		}
		{// bullet
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_LIVE_LIGHTNING],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(LIVE_LIGHTNING).setValue(currentRating);
			}
		}
		{// chess
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_DAILY_CHESS],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(DAILY_CHESS).setValue(currentRating);
			}
		}
		{// chess960
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_DAILY_CHESS960],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(DAILY_CHESS960).setValue(currentRating);
			}
		}
		{// tactics
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_TACTICS],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(TACTICS).setValue(currentRating);
			}
		}
		{// chess mentor
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_CHESS_MENTOR],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(CHESS_MENTOR).setValue(currentRating);
			}
		}

		ratingsAdapter.notifyDataSetInvalidated();
	}

	private void loadFriendsFromDb() {
		new LoadDataFromDbTask(friendsCursorUpdateListener,
				DbHelper.getUserParams(getAppData().getUserName(), DBConstants.FRIENDS),
				getContentResolver()).executeTask();
	}

	private void getFriends() {
		if (!AppUtils.isNetworkAvailable(getActivity())) {
			return;
		}

		LoadItem loadItem = LoadHelper.getFriends(getUserToken());
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
				showToast("Internal error occurred");
			}
		}
	}

	private class SaveFriendsListUpdateListener extends ChessUpdateListener<FriendsItem.Data> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(FriendsItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFriendsFromDb();
		}
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
			mainScrollView.fullScroll(View.FOCUS_UP);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				showToast(R.string.you_havent_added_friends_yet);
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				showToast(R.string.no_network);
			}
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			showToast("Loading ...");
		} else {
			showToast("Done!");
		}
	}

	private void setGridViewToCenter(GridView friendsGridView) {
		// Convert DIPs to pixels
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int mSizePx = (int) Math.floor(50 * metrics.scaledDensity);
		int mSpacingPx = (int) Math.floor(5 * metrics.scaledDensity);


		// Find out the extra space gridView uses for selector on its sides.
		Rect p = new Rect();
		friendsGridView.getSelector().getPadding(p);
		int selectorPadding = p.left + p.right;

		// Determine the number of columns we can fit, given screen width,
		// thumbnail width, and spacing between thumbnails.
		int numColumns = (int) Math.floor(1f * (metrics.widthPixels - selectorPadding + mSpacingPx)
				/ (mSizePx + mSpacingPx));

		int contentWidth = numColumns * mSizePx; // Width of items
		contentWidth += (numColumns - 1) * mSpacingPx; // Plus spaces between items
		contentWidth += selectorPadding; // Plus extra space for selector on sides

		// Now calculate amount of left and right margin so the grid gets
		// centered. This is what we
		// unfortunately cannot do with layout_width="wrap_content"
		// and layout_gravity="center_horizontal"
		int slack = metrics.widthPixels - contentWidth;

		friendsGridView.setNumColumns(numColumns);
		friendsGridView.setPadding(slack / 2, 0, slack / 2, (int) (10 * metrics.scaledDensity));
	}

	private List<RatingListItem> createStatsList(Context context) {
		ArrayList<RatingListItem> selectionItems = new ArrayList<RatingListItem>();

		String[] categories = context.getResources().getStringArray(R.array.user_stats_categories);
		for (int i = 0; i < categories.length; i++) {
			String category = categories[i];
			RatingListItem ratingListItem = new RatingListItem(getIconByCategory(i), category);
			selectionItems.add(ratingListItem);
		}
		return selectionItems;
	}

	/**
	 * Fill list according :
	 * Live - Standard
	 * Live - Blitz
	 * Live - Bullet
	 * Daily - Chess
	 * Daily - Chess960
	 * Tactics
	 * Coach Manager
	 *
	 * @param index to get needed drawable
	 * @return Drawable icon for index
	 */
	private Drawable getIconByCategory(int index) {
		Context context = getActivity();
		switch (index) {
			case LIVE_STANDARD:
				return new IconDrawable(context, R.string.ic_live_standard);
			case LIVE_BLITZ:
				return new IconDrawable(context, R.string.ic_live_blitz);
			case LIVE_LIGHTNING:
				return new IconDrawable(context, R.string.ic_live_bullet);
			case DAILY_CHESS:
				return new IconDrawable(context, R.string.ic_daily_game);
			case DAILY_CHESS960:
				return new IconDrawable(context, R.string.ic_daily960_game);
			case TACTICS:
				return new IconDrawable(context, R.string.ic_help);
			default: // case CHESS_MENTOR:
				return new IconDrawable(context, R.string.ic_lessons);
		}
	}
}
