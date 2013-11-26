package com.chess.ui.fragments.profiles;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.*;
import com.chess.backend.entity.api.stats.UserStatsItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveFriendsListTask;
import com.chess.db.tasks.SaveUserStatsTask;
import com.chess.model.RatingListItem;
import com.chess.model.SelectionItem;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.adapters.RatingsAdapter;
import com.chess.ui.adapters.SectionedListAdapter;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.NavigationMenuFragment;
import com.chess.ui.fragments.friends.FriendsFragmentTablet;
import com.chess.ui.fragments.live.LiveGameOptionsFragment;
import com.chess.ui.fragments.messages.NewMessageFragment;
import com.chess.ui.fragments.stats.StatsGameFragmentTablet;
import com.chess.ui.fragments.stats.StatsGameLessonsFragment;
import com.chess.ui.fragments.stats.StatsGameTacticsFragment;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.11.13
 * Time: 11:10
 */
public class ProfileTabsFragmentTablet extends CommonLogicFragment implements FragmentParentFace, ItemClickListenerFace, AdapterView.OnItemClickListener {

	protected static final String CREATE_CHALLENGE_TAG = "create challenge confirm popup";
	private static final String END_VACATION_TAG = "end vacation popup";

	private final static int DAILY_CHESS = 0;
	private final static int LIVE_STANDARD = 1;
	private final static int LIVE_BLITZ = 2;
	private final static int LIVE_LIGHTNING = 3;
	private final static int DAILY_CHESS960 = 4;
	private final static int TACTICS = 5;
	private final static int LESSONS = 6;

	private static final int FRIEND_OPTIONS_SECTION = 0;
	private static final int RATING_SECTION = 1;
	private static final int FRIENDS_SECTION = 2;

	private boolean noCategoriesFragmentsAdded;
	private String username;
	private ProgressImageView photoImg;
	private TextView usernameTxt;
	private TextView locationTxt;
	private ImageView countryImg;
	private ImageView premiumIconImg;
	private UserItem.Data userInfo;
	private UserUpdateListener userUpdateListener;
	private int photoImageSize;
	private EnhancedImageDownloader imageLoader;
	private View footerView;
	private String[] categories;
	private List<RatingListItem> ratingList;
	private RatingsAdapter ratingsAdapter;
	private SectionedListAdapter sectionedAdapter;
	private ListView listView;
	private StatsItemUpdateListener statsItemUpdateListener;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private ArrayList<SelectionItem> menuItems;
	private OptionsAdapter optionsAdapter;
	private FragmentParentFace parentFace;
	private FriendsUpdateListener friendsUpdateListener;
	private ProfileBaseFragmentTablet.OpponentsAdapter friendsAdapter;

	public static ProfileTabsFragmentTablet createInstance(FragmentParentFace parentFace, String username) {
		ProfileTabsFragmentTablet fragment = new ProfileTabsFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
		} else {
			username = savedInstanceState.getString(USERNAME);
		}

		init();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_tablet_content_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(username);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			{
				// get full stats
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.getInstance().CMD_USER_STATS);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
				loadItem.addRequestParams(RestHelper.P_USERNAME, username);

				new RequestJsonTask<UserStatsItem>(statsItemUpdateListener).executeTask(loadItem);
			}

			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), username);
			new RequestJsonTask<UserItem>(userUpdateListener).executeTask(loadItem);
		} else {
			updateUiData();
			fillUserStats();
		}

		// get friends for user
		LoadItem loadItem = LoadHelper.getFriends(getUserToken(), username);
		new RequestJsonTask<FriendsItem>(friendsUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		boolean headerAdded = listView.getHeaderViewsCount() > 0; // use to check if header added
		int offset = headerAdded ? -1 : 0;

		int section = sectionedAdapter.getCurrentSection(position + offset);

		if (section == FRIEND_OPTIONS_SECTION) {
			SelectionItem optionItem = (SelectionItem) parent.getItemAtPosition(position);
			if (optionItem.getText().equals(getString(R.string.add_friend))) {
				createFriendRequest(username, getString(R.string.add_friend_request_message));

			} else if (optionItem.getText().equals(getString(R.string.send_message))){
				changeInternalFragment(NewMessageFragment.createInstance(username));
			} else if (optionItem.getText().equals(getString(R.string.challenge_to_play))){
				String title = getString(R.string.challenge) + Symbol.SPACE + username + Symbol.QUESTION;
				popupItem.setNegativeBtnId(R.string.daily);
				popupItem.setPositiveBtnId(R.string.live);
				showPopupDialog(title, CREATE_CHALLENGE_TAG);
			}
		} else if (section == RATING_SECTION) {
			RatingListItem ratingListItem = (RatingListItem) parent.getItemAtPosition(position);
			String sectionName = ratingListItem.getText();
			if (sectionName.equals(categories[DAILY_CHESS])) {
				changeInternalFragment(StatsGameFragmentTablet.createInstance(this, DAILY_CHESS, username));
			} else if (sectionName.equals(categories[LIVE_STANDARD])) {
				changeInternalFragment(StatsGameFragmentTablet.createInstance(this, LIVE_STANDARD, username));
			} else if (sectionName.equals(categories[LIVE_BLITZ])) {
				changeInternalFragment(StatsGameFragmentTablet.createInstance(this, LIVE_BLITZ, username));
			} else if (sectionName.equals(categories[LIVE_LIGHTNING])) {
				changeInternalFragment(StatsGameFragmentTablet.createInstance(this, LIVE_LIGHTNING, username));
			} else if (sectionName.equals(categories[LIVE_STANDARD])) {
				changeInternalFragment(StatsGameFragmentTablet.createInstance(this, LIVE_STANDARD, username));
			} else if (sectionName.equals(categories[DAILY_CHESS960])) {
				changeInternalFragment(StatsGameFragmentTablet.createInstance(this, DAILY_CHESS960, username));
			} else if (sectionName.equals(categories[TACTICS])) {
				changeInternalFragment(StatsGameTacticsFragment.createInstance(username));
			} else if (sectionName.equals(categories[LESSONS])) {
				changeInternalFragment(StatsGameLessonsFragment.createInstance(username)); // TODO adjust Lessons
			}
		}
	}

	private class FriendsUpdateListener extends ChessUpdateListener<FriendsItem> {

		public FriendsUpdateListener() {
			super(FriendsItem.class);
		}

		@Override
		public void updateData(FriendsItem returnedObj) {
			super.updateData(returnedObj);

			List<SelectionItem> friendsList = new ArrayList<SelectionItem>();
			for (FriendsItem.Data friend : returnedObj.getData()) {
				SelectionItem selectionItem = new SelectionItem(null, friend.getUsername());
				selectionItem.setCode(friend.getAvatarUrl());
				friendsList.add(selectionItem);
			}

			friendsAdapter.setItemsList(friendsList);
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
			getActivityFace().changeRightFragment(LiveGameOptionsFragment.createInstance(RIGHT_MENU_MODE, username));
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
			createDailyChallenge(username);
		}
		super.onNegativeBtnClick(fragment);
	}

	private void createDailyChallenge(String opponentName) {

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
			showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.YOUR_ARE_ON_VACATAION) {

					showPopupDialog(R.string.leave_vacation_to_challenge_q, END_VACATION_TAG);
				} else {
					super.errorHandle(resultCode);
				}
			}
		}
	}

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		public VacationUpdateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			createDailyChallenge(username);
		}
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


	private class StatsItemUpdateListener extends ChessUpdateListener<UserStatsItem> {

		public StatsItemUpdateListener() {
			super(UserStatsItem.class);
		}

		@Override
		public void updateData(UserStatsItem returnedObj) {
			super.updateData(returnedObj);

			new SaveUserStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver(), username).executeTask();
		}
	}

	private class SaveStatsUpdateListener extends ChessUpdateListener<UserStatsItem.Data> {

		@Override
		public void updateData(UserStatsItem.Data returnedObj) {
			super.updateData(returnedObj);

			fillUserStats();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			showToast(" code " + resultCode);
		}
	}

	private class UserUpdateListener extends ChessUpdateListener<UserItem> {

		public UserUpdateListener() {
			super(UserItem.class);
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);
			userInfo = returnedObj.getData();

			updateUiData();

			need2update = false;
		}
	}

	private void updateUiData() {
		imageLoader.download(userInfo.getAvatar(), photoImg, photoImageSize);
		usernameTxt.setText(userInfo.getFirstName() + Symbol.SPACE + userInfo.getLastName());
		locationTxt.setText(userInfo.getLocation());
		countryImg.setImageDrawable(AppUtils.getCountryFlagScaled(getActivity(), userInfo.getCountryName()));
		premiumIconImg.setImageResource(AppUtils.getPremiumIcon(userInfo.getPremiumStatus()));
	}


	private void fillUserStats() {
		// fill ratings
		String[] argument = new String[]{username};

		{// standard
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				ratingList.get(LIVE_STANDARD).setValue(currentRating);
			}
		}
		{// blitz
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				ratingList.get(LIVE_BLITZ).setValue(currentRating);
			}
		}
		{// bullet
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);

				ratingList.get(LIVE_LIGHTNING).setValue(currentRating);
			}
		}
		{// chess
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				ratingList.get(DAILY_CHESS).setValue(currentRating);
			}
		}
		{// chess960
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_DAILY_CHESS960.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				ratingList.get(DAILY_CHESS960).setValue(currentRating);
			}
		}
		{// tactics
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_TACTICS.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				ratingList.get(TACTICS).setValue(currentRating);
			}
		}
		{// chess mentor
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_LESSONS.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				ratingList.get(LESSONS).setValue(currentRating);
			}
		}

		ratingsAdapter.notifyDataSetInvalidated();
		need2update = false;
	}

	private List<RatingListItem> createStatsList(Context context) {
		List<RatingListItem> ratingListItems = new ArrayList<RatingListItem>();

		categories = context.getResources().getStringArray(R.array.user_stats_categories);
		for (int i = 0; i < categories.length; i++) {
			String category = categories[i];
			RatingListItem ratingListItem = new RatingListItem(getIconByCategory(i), category);
			ratingListItems.add(ratingListItem);
		}
		return ratingListItems;
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
				return new IconDrawable(context, R.string.ic_live_standard, R.color.hint_text, R.dimen.glyph_icon_big);
			case LIVE_BLITZ:
				return new IconDrawable(context, R.string.ic_live_blitz, R.color.hint_text, R.dimen.glyph_icon_big);
			case LIVE_LIGHTNING:
				return new IconDrawable(context, R.string.ic_live_bullet, R.color.hint_text, R.dimen.glyph_icon_big);
			case DAILY_CHESS:
				return new IconDrawable(context, R.string.ic_daily_game, R.color.hint_text, R.dimen.glyph_icon_big);
			case DAILY_CHESS960:
				return new IconDrawable(context, R.string.ic_daily960_game, R.color.hint_text, R.dimen.glyph_icon_big);
			case TACTICS:
				return new IconDrawable(context, R.string.ic_help, R.color.hint_text, R.dimen.glyph_icon_big);
			default: // case LESSONS:
				return new IconDrawable(context, R.string.ic_lessons, R.color.hint_text, R.dimen.glyph_icon_big);
		}
	}

	private class FriendsClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			changeInternalFragment(FriendsFragmentTablet.createInstance(ProfileTabsFragmentTablet.this, username));
		}
	}

	private void init() {
		FragmentActivity context = getActivity();

		userUpdateListener = new UserUpdateListener();
		friendsUpdateListener = new FriendsUpdateListener();

		photoImageSize = (int) (80 * density);
		imageLoader = new EnhancedImageDownloader(context);
		statsItemUpdateListener = new StatsItemUpdateListener();
		saveStatsUpdateListener = new SaveStatsUpdateListener();
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_dark,
				new int[]{FRIEND_OPTIONS_SECTION});

		ratingList = createStatsList(context);

		ratingsAdapter = new RatingsAdapter(context, ratingList);

		menuItems = new ArrayList<SelectionItem>();
		{ // Add Friend
			IconDrawable icon = new IconDrawable(context, R.string.ic_add, R.color.hint_text, R.dimen.glyph_icon_big);
			SelectionItem selectionItem = new SelectionItem(icon, getString(R.string.add_friend));
			menuItems.add(selectionItem);
		}
		{ // Challenge to play
			IconDrawable icon = new IconDrawable(context, R.string.ic_board, R.color.hint_text, R.dimen.glyph_icon_big);
			SelectionItem selectionItem = new SelectionItem(icon, getString(R.string.challenge_to_play));
			menuItems.add(selectionItem);
		}
		{ // Send message
			IconDrawable icon = new IconDrawable(context, R.string.ic_email_dark, R.color.hint_text, R.dimen.glyph_icon_big);

			SelectionItem selectionItem = new SelectionItem(icon, getString(R.string.send_message));
			menuItems.add(selectionItem);
		}
//		{ // Follow %username%
//			IconDrawable icon = new IconDrawable(context, R.string.ic_board, R.color.hint_text, R.dimen.glyph_icon_big);
//			SelectionItem selectionItem = new SelectionItem(null, getString(R.string.challenge_to_play));
//			menuItems.add(selectionItem);
//		}
		optionsAdapter = new OptionsAdapter(context, menuItems);

		sectionedAdapter.addSection(getString(R.string.options), optionsAdapter);
		sectionedAdapter.addSection(getString(R.string.ratings), ratingsAdapter);

		footerView = LayoutInflater.from(context).inflate(R.layout.new_frineds_gridview, null, false);
		friendsAdapter = new ProfileBaseFragmentTablet.OpponentsAdapter(context, null, getImageFetcher());

		GridView gridView = (GridView) footerView.findViewById(R.id.gridView);
		gridView.setAdapter(friendsAdapter);
		gridView.setOnItemClickListener(new FriendsClickListener());

		changeInternalFragment(ProfileTabsUserFragmentTablet.createInstance(this, username));

		noCategoriesFragmentsAdded = true;
	}

	private void widgetsInit(View view) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View headerView = inflater.inflate(R.layout.new_profiles_header_view, null, false);

		{// Header init
			photoImg = (ProgressImageView) headerView.findViewById(R.id.photoImg);
			usernameTxt = (TextView) headerView.findViewById(R.id.usernameTxt);
			locationTxt = (TextView) headerView.findViewById(R.id.locationTxt);
			countryImg = (ImageView) headerView.findViewById(R.id.countryImg);
			premiumIconImg = (ImageView) headerView.findViewById(R.id.premiumIconImg);
		}
		listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.addFooterView(footerView);
		listView.setAdapter(sectionedAdapter);
		listView.setOnItemClickListener(this);
	}

	private class OptionsAdapter extends ItemsAdapter<SelectionItem> {

		private final int sidePadding;
		private final int whiteColor;

		public OptionsAdapter(Context context, List<SelectionItem> itemList) {
			super(context, itemList);
			sidePadding = (int) (8 * density);
			whiteColor = resources.getColor(R.color.semitransparent_white_75);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_rating_list_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.labelTxt = (TextView) view.findViewById(R.id.ratingLabelTxt);
			holder.valueTxt = (TextView) view.findViewById(R.id.ratingValueTxt);
			holder.valueTxt.setVisibility(View.GONE);
			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(SelectionItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.labelTxt.setText(item.getText());

			Drawable drawable = item.getImage();
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			holder.labelTxt.setCompoundDrawables(drawable, null, null, null);
		}

		private class ViewHolder {
			TextView labelTxt;
			TextView valueTxt;
		}
	}


	@Override
	public void changeFragment(BasePopupsFragment fragment) {
		openInternalFragment(fragment);
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment).commitAllowingStateLoss();
	}

	private void openInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment, fragment.getClass().getSimpleName());
		transaction.addToBackStack(fragment.getClass().getSimpleName());
		transaction.commit();
	}

	@Override
	public boolean showPreviousFragment() {
		if (getActivity() == null) {
			return false;
		}
		int entryCount = getChildFragmentManager().getBackStackEntryCount();
		if (entryCount > 0) {
			int last = entryCount - 1;
			FragmentManager.BackStackEntry stackEntry = getChildFragmentManager().getBackStackEntryAt(last);
			if (stackEntry != null && stackEntry.getName().equals(FriendsFragmentTablet.class.getSimpleName())) {
				noCategoriesFragmentsAdded = true;    // TODO adjust
			}

			return getChildFragmentManager().popBackStackImmediate();
		} else {
			return super.showPreviousFragment();
		}
	}
}
