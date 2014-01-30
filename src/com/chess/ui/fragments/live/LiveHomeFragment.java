package com.chess.ui.fragments.live;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.ServersStatsItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.lcc.android.DataNotValidException;
import com.chess.live.client.Game;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupLiveTimeOptionsFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.stats.StatsGameDetailsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.interfaces.GameFaceHelper;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.10.13
 * Time: 6:20
 */
public class LiveHomeFragment extends LiveBaseFragment implements PopupListSelectionFace, AdapterView.OnItemClickListener {

	private static final String TAG = "LccLog-LiveHomeFragment";
	private static final String OPTION_SELECTION_TAG = "time options popup";
	protected static final String FRIEND_SELECTION_TAG = "friend select popup";

	protected GameFaceHelper gameFaceHelper;
	protected Button timeSelectBtn;
	private PopupLiveTimeOptionsFragment timeOptionsFragment;
	private TimeOptionSelectedListener timeOptionSelectedListener;
	protected String[] newGameButtonsArray;
	protected TextView onlinePlayersCntTxt;
	protected List<LiveItem> featuresList;
	private ServerStatsUpdateListener serverStatsUpdateListener;
	private OptionsAdapter optionsAdapter;
	private LiveItem currentGameItem;
	private LiveItem topGameItem;
	protected PopupOptionsMenuFragment friendSelectFragment;
	protected FriendSelectedListener friendSelectedListener;
	protected String[] liveFriends;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		currentGameItem = new LiveItem(R.string.ic_live_standard, R.string.current_game);
		topGameItem = new LiveItem(R.string.ic_binoculars, R.string.top_game);
		featuresList = new ArrayList<LiveItem>();
		featuresList.add(topGameItem);
		featuresList.add(new LiveItem(R.string.ic_stats, R.string.stats));
		featuresList.add(new LiveItem(R.string.ic_challenge_friend, R.string.friends));
		featuresList.add(new LiveItem(R.string.ic_board, R.string.archive));
		optionsAdapter = new OptionsAdapter(getActivity(), featuresList);

		gameFaceHelper = new GameFaceHelper(getActivity());
		timeOptionSelectedListener = new TimeOptionSelectedListener();
		friendSelectedListener = new FriendSelectedListener();
		serverStatsUpdateListener = new ServerStatsUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live);

		getAppData().setLiveChessMode(true); // we should set it before parent call to update isLCSBound flag

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		//LogMe.dl("LCCLOG", "LiveHomeFragment onResume isNetworkAvailable() " + isNetworkAvailable());

		try {

			if (!isNetworkAvailable()) {
				dismissNetworkCheckDialog();
				popupItem.setPositiveBtnId(R.string.check_connection);
				showPopupDialog(R.string.no_network, NETWORK_CHECK_TAG);
			}

			if (!isLCSBound) {
				liveBaseActivity.connectLcc();
				showPopupProgressDialog();
			} else {
				adjustFeaturesList(getLiveService());
			}

		} catch (DataNotValidException e) {
			e.printStackTrace();
		}

		if (isNetworkAvailable()) {
			// get online players count
			LoadItem loadItem = LoadHelper.getServerStats();
			new RequestJsonTask<ServersStatsItem>(serverStatsUpdateListener).executeTask(loadItem);
		}
	}

	/*
	@Override
	public void onPause() {
		super.onPause();

		liveBaseActivity.stopConnectTimer();
	}
	*/

	protected void adjustFeaturesList(LiveChessService liveService) {
		if (liveService.isActiveGamePresent() && !liveService.isCurrentGameObserved()) {
			if (!featuresList.contains(currentGameItem)) {
				featuresList.add(0, currentGameItem);
				optionsAdapter.notifyDataSetChanged();
			}
			featuresList.remove(topGameItem);
		} else {
			if (!featuresList.contains(topGameItem)) {
				featuresList.add(0, topGameItem);
				optionsAdapter.notifyDataSetChanged();
			}
			featuresList.remove(currentGameItem);
		}
	}

	private class ServerStatsUpdateListener extends ChessUpdateListener<ServersStatsItem> {
		private ServerStatsUpdateListener() {
			super(ServersStatsItem.class);
		}

		@Override
		public void updateData(ServersStatsItem returnedObj) {
			super.updateData(returnedObj);

			long cnt = returnedObj.getData().getTotals().getLive();
			String playersOnlineStr = NumberFormat.getInstance().format(cnt);

			onlinePlayersCntTxt.setText(getString(R.string.players_online_arg, playersOnlineStr));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		LiveItem liveItem = (LiveItem) parent.getItemAtPosition(position);

		if (liveItem == null) {
			return;
		}

		if (liveItem.iconId == R.string.ic_binoculars) {  // Top Game
			if (!isLCSBound) {
				showToast(R.string.still_connecting);
				return;
			}
			try { // check for valid data
				 getLiveService();
			} catch (DataNotValidException e) {
				e.printStackTrace();
				showToast(R.string.still_connecting);
				return;
			}

			Fragment fragmentByTag;
			fragmentByTag = ((LiveBaseActivity)getActivity()).getGameLiveObserverFragment();
			if (fragmentByTag == null) {
				if (!isTablet) {
					fragmentByTag = new GameLiveObserveFragment();
				} else {
					fragmentByTag = new GameLiveObserveFragmentTablet();
				}
			}
//			getActivityFace().openFragment((BasePopupsFragment) fragmentByTag, true);
			getActivityFace().openFragment((BasePopupsFragment) fragmentByTag);
		} else if (liveItem.iconId == R.string.ic_live_standard) { // Current game
			Fragment fragmentByTag;
			fragmentByTag = ((LiveBaseActivity)getActivity()).getGameLiveFragment();
			if (fragmentByTag == null) {
				if (!isTablet) {
					fragmentByTag = new GameLiveFragment();
				} else {
					fragmentByTag = new GameLiveFragmentTablet();
				}
			}
//			getActivityFace().openFragment((BasePopupsFragment) fragmentByTag, true);
			getActivityFace().openFragment((BasePopupsFragment) fragmentByTag);
		} else if (liveItem.iconId == R.string.ic_stats) { // Stats
			getActivityFace().openFragment(StatsGameDetailsFragment.createInstance(
					StatsGameFragment.LIVE_STANDARD, true, getUsername()));
		} else if (liveItem.iconId == R.string.ic_challenge_friend) { // Friends
			if (isLCSBound) {
				try {
					LiveChessService liveService = getLiveService();
					liveFriends = liveService.getOnlineFriends();

					if (liveFriends == null || liveFriends.length == 0) {
						showToast(R.string.no_friends_online);
						return;
					}

					SparseArray<String> optionsMap = new SparseArray<String>();
					for (int i = 0; i < liveFriends.length; i++) {
						String friend = liveFriends[i];
						optionsMap.put(i, friend);
					}

					friendSelectFragment = PopupOptionsMenuFragment.createInstance(friendSelectedListener, optionsMap);
					friendSelectFragment.show(getFragmentManager(), FRIEND_SELECTION_TAG);
				} catch (DataNotValidException e) {
					e.printStackTrace();
				}
			}
		} else if (liveItem.iconId == R.string.ic_board) { // Archive
			getActivityFace().openFragment(new LiveGamesArchiveFragment());
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.timeSelectBtn) {
			// show popup
			if (timeOptionsFragment != null) {
				return;
			}

			timeOptionsFragment = PopupLiveTimeOptionsFragment.createInstance(timeOptionSelectedListener);
			timeOptionsFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
		} else if (view.getId() == R.id.gamePlayBtn) {
			createLiveChallenge();
		} else if (view.getId() == R.id.newGameHeaderView) {
			getActivityFace().changeRightFragment(LiveGameOptionsFragment.createInstance(CENTER_MODE));
			getActivityFace().toggleRightMenu();
		}
	}

	@Override
	public void onValueSelected(int code) {
		setDefaultTimeMode(code);

		timeOptionsFragment.dismiss();
		timeOptionsFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		timeOptionsFragment = null;
	}

	private void createLiveChallenge() {
		LiveGameConfig.Builder gameConfigBuilder = getAppData().getLiveGameConfigBuilder();
		int minRatingOffset = gameConfigBuilder.getMinRatingOffset();
		int maxRatingOffset = gameConfigBuilder.getMaxRatingOffset();
		if (minRatingOffset == 0 || maxRatingOffset == 0) {
			minRatingOffset = LiveGameConfig.RATING_STEP;
			maxRatingOffset = LiveGameConfig.RATING_STEP;
			gameConfigBuilder.setMinRatingOffset(minRatingOffset);
			gameConfigBuilder.setMaxRatingOffset(maxRatingOffset);
		}

		String username = getAppData().getUsername();

		int rating = AppConstants.DEFAULT_PLAYER_RATING;
		if (gameConfigBuilder.getTimeMode() == LiveGameConfig.STANDARD) {
			rating = DbDataManager.getUserRatingFromUsersStats(getActivity(),
					DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal(), username);
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BLITZ) {
			rating = DbDataManager.getUserRatingFromUsersStats(getActivity(),
					DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal(), username);
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BULLET) {
			rating = DbDataManager.getUserRatingFromUsersStats(getActivity(),
					DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal(), username);
		}

		gameConfigBuilder.setTimeFromMode(getAppData().getDefaultLiveMode());
		gameConfigBuilder.setRating(rating);

		getActivityFace().openFragment(LiveGameWaitFragment.createInstance(gameConfigBuilder.build()));
	}

	@Override
	public void onLiveClientConnected() {
		super.onLiveClientConnected();
		dismissProgressDialog();
		dismissNetworkCheckDialog();
	}

	@Override
	public void startGameFromService() {
		//LogMe.dl(TAG, "startGameFromService");

		final FragmentActivity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LiveChessService liveService;
					try {
						liveService = getLiveService();
					} catch (DataNotValidException e) {
						logTest(e.getMessage());
						showToast(e.getMessage());
						return;
					}
					logTest("challenge created, ready to start");

					Long gameId = liveService.getCurrentGameId();
					logTest("gameId = " + gameId);

					GameLiveFragment gameLiveFragment;
					if (liveService.isCurrentGameObserved()) {
						gameLiveFragment = ((LiveBaseActivity) getActivity()).getGameLiveObserverFragment();
						if (gameLiveFragment == null) {
							if (isTablet) {
								gameLiveFragment = GameLiveObserveFragmentTablet.createInstance(gameId);
							} else {
								gameLiveFragment = GameLiveObserveFragment.createInstance(gameId);
							}
						}
					} else {
						gameLiveFragment = ((LiveBaseActivity) getActivity()).getGameLiveFragment();
						if (gameLiveFragment == null) {
							if (isTablet) {
								gameLiveFragment = GameLiveFragmentTablet.createInstance(gameId);
							} else {
								gameLiveFragment = GameLiveFragment.createInstance(gameId);
							}
						}
					}
					if (gameLiveFragment != null) {
//						getActivityFace().openFragment(gameLiveFragment, true);
						getActivityFace().openFragment(gameLiveFragment);
					}
				}
			});
		}
	}

	protected void widgetsInit(View view) {
		Resources resources = getResources();
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		ViewGroup headerView = (ViewGroup) inflater.inflate(R.layout.new_play_home_header_frame, null, false);

		inviteOverlaySetup(resources,  headerView.findViewById(R.id.startOverlayView), resources.getDisplayMetrics().widthPixels / 8); // one square size);
		onlinePlayersCntTxt = (TextView) headerView.findViewById(R.id.onlinePlayersCntTxt);

		headerView.findViewById(R.id.newGameHeaderView).setOnClickListener(this);
		headerView.findViewById(R.id.gamePlayBtn).setOnClickListener(this);

		{ // Time mode adjustments
			int mode = getAppData().getDefaultLiveMode();
			// set texts to buttons
			newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
			// TODO add sliding from outside animation for time modes in popup
			timeSelectBtn = (Button) headerView.findViewById(R.id.timeSelectBtn);
			timeSelectBtn.setOnClickListener(this);
			timeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[mode]));
		}

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(optionsAdapter);
		listView.setOnItemClickListener(this);

		ChessBoardBaseView boardView = (ChessBoardBaseView) headerView.findViewById(R.id.boardview);
		boardView.setGameFace(gameFaceHelper);
	}

	protected void inviteOverlaySetup(Resources resources, View startOverlayView, int squareSize) {
		// let's make it to match board properties
		// it should be 2 squares inset from top of border and 4 squares tall + 1 squares from sides
		int borderOffset = resources.getDimensionPixelSize(R.dimen.invite_overlay_top_offset);
		// now we add few pixel to compensate shadow addition
		int shadowOffset = resources.getDimensionPixelSize(R.dimen.overlay_shadow_offset);
		borderOffset += shadowOffset;
		int overlayHeight = squareSize * 4 + borderOffset + shadowOffset;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				overlayHeight);
		int topMargin = squareSize * 2 + borderOffset - shadowOffset * 2;

		params.setMargins(squareSize - borderOffset, topMargin, squareSize - borderOffset, 0);
		params.addRule(RelativeLayout.ALIGN_TOP, R.id.boardView);
		startOverlayView.setLayoutParams(params);
		startOverlayView.setVisibility(View.VISIBLE);
	}

	protected static class LiveItem {
		int iconId;
		int labelId;

		private LiveItem(int iconId, int labelId) {
			this.iconId = iconId;
			this.labelId = labelId;
		}
	}

	private class OptionsAdapter extends ItemsAdapter<LiveItem> {

		private final int sidePadding;
		private final int whiteColor;

		public OptionsAdapter(Context context, List<LiveItem> itemList) {
			super(context, itemList);
			sidePadding = (int) (8 * density);
			whiteColor = resources.getColor(R.color.white);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_dark_spinner_item, parent, false);

			ButtonDrawableBuilder.setBackgroundToView(view, R.style.ListItem_Header_Dark);
			view.setPadding(sidePadding, 0, sidePadding, 0);

			ViewHolder holder = new ViewHolder();
			holder.nameTxt = (TextView) view.findViewById(R.id.categoryNameTxt);
			holder.iconTxt = (TextView) view.findViewById(R.id.iconTxt);
			holder.spinnerIcon = (TextView) view.findViewById(R.id.spinnerIcon);
			holder.spinnerIcon.setVisibility(View.GONE);

			holder.nameTxt.setPadding(sidePadding, 0, 0, 0);
			holder.nameTxt.setTextColor(whiteColor);
			holder.iconTxt.setVisibility(View.VISIBLE);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(LiveItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();


			holder.nameTxt.setText(item.labelId);
			holder.iconTxt.setText(item.iconId);
		}

		private class ViewHolder {
			TextView iconTxt;
			TextView nameTxt;
			TextView spinnerIcon;
		}
	}

	private class TimeOptionSelectedListener implements PopupListSelectionFace {

		@Override
		public void onValueSelected(int code) {
			timeOptionsFragment.dismiss();
			timeOptionsFragment = null;

			setDefaultTimeMode(code);
		}

		@Override
		public void onDialogCanceled() {
			timeOptionsFragment = null;
		}
	}

	private class FriendSelectedListener implements PopupListSelectionFace {

		@Override
		public void onValueSelected(int code) {
			friendSelectFragment.dismiss();
			friendSelectFragment = null;

			String friend = liveFriends[code];

			getActivityFace().changeRightFragment(LiveGameOptionsFragment.createInstance(RIGHT_MENU_MODE, friend));
			getActivityFace().toggleRightMenu();
		}

		@Override
		public void onDialogCanceled() {
			friendSelectFragment = null;
		}
	}

	protected String getLiveModeButtonLabel(String label) {
		if (label.contains(Symbol.SLASH)) { // like "5 | 2"
			return label;
		} else { // "10 min"
			return getString(R.string.min_arg, label);
		}
	}

	private void setDefaultTimeMode(int mode) {
		timeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[mode]));
		getAppData().setDefaultLiveMode(mode);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(NETWORK_CHECK_TAG)) {
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		}
		super.onPositiveBtnClick(fragment);
	}

	private void dismissNetworkCheckDialog() {
		dismissFragmentDialogByTag(NETWORK_CHECK_TAG);
	}

	@Override
	public void onGameEnd(Game game, String gameEndMessage) {
		try {
			adjustFeaturesList(getLiveService());
		} catch (DataNotValidException e) {
			e.printStackTrace();
		}
	}
}
