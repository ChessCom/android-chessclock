package com.chess.ui.fragments.profiles;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.DailySeekItem;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.RatingListItem;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.RatingsAdapter;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.friends.FriendsFragmentTablet;
import com.chess.ui.fragments.messages.NewMessageFragment;
import com.chess.ui.fragments.stats.StatsGameFragmentTablet;
import com.chess.ui.fragments.stats.StatsGameLessonsFragment;
import com.chess.ui.fragments.stats.StatsGameTacticsFragment;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.11.13
 * Time: 12:10
 */
public class ProfileBaseFragmentTablet extends CommonLogicFragment implements FragmentParentFace, ItemClickListenerFace, AdapterView.OnItemClickListener {

	private final static int DAILY_CHESS = 0;
	private final static int LIVE_STANDARD = 1;
	private final static int LIVE_BLITZ = 2;
	private final static int LIVE_LIGHTNING = 3;
	private final static int DAILY_CHESS960 = 4;
	private final static int TACTICS = 5;
	private final static int LESSONS = 6;

	private static final int RATING_SECTION = 0;
	private static final int FRIENDS_SECTION = 1;

	protected static final String USERNAME = "username";
	public static final int STATS_MODE = 0;

	protected String username;
	private static final String CREATE_CHALLENGE_TAG = "create challenge confirm popup";
	private boolean noCategoriesFragmentsAdded;
	private CustomSectionedAdapter sectionedAdapter;
	private ListView listView;
	private ProgressImageView photoImg;
	private TextView usernameTxt;
	private TextView locationTxt;
	private ImageView countryImg;
	private ImageView premiumIconImg;
	private UserItem.Data userInfo;
	private int photoImageSize;
	private EnhancedImageDownloader imageLoader;
	private UserUpdateListener userUpdateListener;
	private int mode = FRIENDS_SECTION;
	private String[] categories;

	public ProfileBaseFragmentTablet() {
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, Symbol.EMPTY);
		bundle.putInt(MODE, FRIENDS_SECTION);
		setArguments(bundle);
	}

	public ProfileBaseFragmentTablet(String username) {
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		bundle.putInt(MODE, FRIENDS_SECTION);
		setArguments(bundle);
	}

	public static ProfileBaseFragmentTablet createInstance(int mode, String username) {
		ProfileBaseFragmentTablet fragment = new ProfileBaseFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mode = getArguments().getInt(MODE);
			username = getArguments().getString(USERNAME);
		} else {
			mode = savedInstanceState.getInt(MODE);
			username = savedInstanceState.getString(USERNAME);
		}

		if (TextUtils.isEmpty(username)) {
			username = getUsername();
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
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), username);
			new RequestJsonTask<UserItem>(userUpdateListener).executeTask(loadItem);
		} else {
			updateUiData();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(MODE, mode);
		outState.putString(USERNAME, username);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_message:
				getActivityFace().openFragment(NewMessageFragment.createInstance(username));
				break;
			case R.id.menu_challenge:
				String title = getString(R.string.challenge) + Symbol.SPACE + username + Symbol.QUESTION;
				showPopupDialog(title, CREATE_CHALLENGE_TAG);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(CREATE_CHALLENGE_TAG)) {
			createDailyChallenge(username);
		}
		super.onPositiveBtnClick(fragment);
	}

	private void createDailyChallenge(String opponentName) {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = new DailyGameConfig.Builder().build();
		dailyGameConfig.setOpponentName(opponentName);


		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(new CreateChallengeUpdateListener()).executeTask(loadItem);
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

		if (section == RATING_SECTION) {
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
		} else if (section == FRIENDS_SECTION) {

		}
	}

	private class CreateChallengeUpdateListener extends ChessLoadUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}
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

	private void init() {
		photoImageSize = (int) (80 * density);
		imageLoader = new EnhancedImageDownloader(getActivity());
		userUpdateListener = new UserUpdateListener();

		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_dark);
		List<RatingListItem> ratingList = createStatsList(getActivity());

		RatingsAdapter ratingsAdapter = new RatingsAdapter(getActivity(), ratingList);

//		challengesGamesAdapter = new DailyChallengesGamesAdapter(this, null, getImageFetcher());

		sectionedAdapter.addSection(getString(R.string.ratings), ratingsAdapter);
//		sectionedAdapter.addSection(getString(R.string.new_my_move), currentGamesMyCursorAdapter);

		if (mode == STATS_MODE) {
			changeInternalFragment(StatsGameFragmentTablet.createInstance(this,
					StatsGameFragmentTablet.DAILY_CHESS, username));
		} else {
			changeInternalFragment(FriendsFragmentTablet.createInstance(this, username));
		}

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
		listView.setAdapter(sectionedAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void changeFragment(Fragment fragment) {
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
