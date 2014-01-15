package com.chess.ui.fragments.daily;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.daily_games.DailySeekItem;
import com.chess.backend.entity.api.ServersStatsItem;
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.WebViewFragment;
import com.chess.ui.fragments.friends.FriendsFragment;
import com.chess.ui.fragments.popup_fragments.PopupDailyTimeOptionsFragment;
import com.chess.ui.fragments.stats.StatsGameDetailsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.interfaces.AbstractGameNetworkFaceHelper;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.10.13
 * Time: 16:26
 */
public class DailyHomeFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final String OPTION_SELECTION_TAG = "time options popup";
	private static final String END_VACATION_TAG = "end vacation popup";

	protected List<DailyItem> featuresList;
	protected GameFaceHelper gameFaceHelper;
	private DailyGameConfig.Builder gameConfigBuilder;
	protected int[] newGameButtonsArray;
	protected Button timeSelectBtn;
	private PopupDailyTimeOptionsFragment timeOptionsFragment;
	private TimeOptionSelectedListener timeOptionSelectedListener;
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private ServerStatsUpdateListener serverStatsUpdateListener;
	protected TextView onlinePlayersCntTxt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		createFeaturesList();

		gameConfigBuilder = new DailyGameConfig.Builder();

		gameFaceHelper = new GameFaceHelper();

		createChallengeUpdateListener = new CreateChallengeUpdateListener();
		timeOptionSelectedListener = new TimeOptionSelectedListener();
		serverStatsUpdateListener = new ServerStatsUpdateListener();
	}

	protected void createFeaturesList() {
		featuresList = new ArrayList<DailyItem>();
		featuresList.add(new DailyItem(R.string.ic_stats, R.string.stats));
		featuresList.add(new DailyItem(R.string.ic_challenge_friend, R.string.friends));
		featuresList.add(new DailyItem(R.string.ic_board, R.string.archive));
		featuresList.add(new DailyItem(R.string.ic_tournaments, R.string.tournaments));
		featuresList.add(new DailyItem(R.string.ic_binoculars, R.string.open_challenges));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.daily);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		LoadItem loadItem = LoadHelper.getServerStats();
		new RequestJsonTask<ServersStatsItem>(serverStatsUpdateListener).executeTask(loadItem);
	}

	private class ServerStatsUpdateListener extends ChessUpdateListener<ServersStatsItem> {
		private ServerStatsUpdateListener() {
			super(ServersStatsItem.class);
		}

		@Override
		public void updateData(ServersStatsItem returnedObj) {
			super.updateData(returnedObj);


			long cnt = returnedObj.getData().getTotals().getOnline();
			String playersOnlineStr = NumberFormat.getInstance().format(cnt);

			onlinePlayersCntTxt.setText(getString(R.string.players_online_arg, playersOnlineStr));
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

			timeOptionsFragment = PopupDailyTimeOptionsFragment.createInstance(timeOptionSelectedListener);
			timeOptionsFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
		} else if (view.getId() == R.id.gamePlayBtn) {
			createDailyChallenge();
		} else if (view.getId() == R.id.newGameHeaderView) {
			getActivityFace().changeRightFragment(DailyGameOptionsFragment.createInstance(CENTER_MODE));
			getActivityFace().toggleRightMenu();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DailyItem dailyItem = (DailyItem) parent.getItemAtPosition(position);

		if (dailyItem.iconId == R.string.ic_stats) {
			getActivityFace().openFragment(StatsGameDetailsFragment.createInstance(
					StatsGameFragment.DAILY_CHESS, true, getUsername()));
		} else if (dailyItem.iconId == R.string.ic_challenge_friend) {
			getActivityFace().openFragment(new FriendsFragment());
		} else if (dailyItem.iconId == R.string.ic_board) {
			if (!isTablet) {
				getActivityFace().openFragment(new DailyGamesFinishedFragment());
			} else {
				getActivityFace().openFragment(new DailyGamesFinishedFragmentTablet());
			}
		} else if (dailyItem.iconId == R.string.ic_tournaments) {
			String tournamentsLink = RestHelper.getInstance().getTournamentsLink(getUserToken());
			WebViewFragment webViewFragment = WebViewFragment.createInstance(tournamentsLink, getString(R.string.tournaments));
			getActivityFace().openFragment(webViewFragment);
		} else if (dailyItem.iconId == R.string.ic_binoculars) {
			getActivityFace().openFragment(new DailyOpenChallengesFragment());
		}
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = gameConfigBuilder.build();

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
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
					showPopupDialog(R.string.leave_vacation_q, END_VACATION_TAG);
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(END_VACATION_TAG)) {
			LoadItem loadItem = LoadHelper.deleteOnVacation(getUserToken());
			new RequestJsonTask<VacationItem>(new VacationUpdateListener()).executeTask(loadItem);
		}
		super.onPositiveBtnClick(fragment);
	}

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		public VacationUpdateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
		}
	}

	private class TimeOptionSelectedListener implements PopupListSelectionFace {

		@Override
		public void onValueSelected(int code) {
			timeOptionsFragment.dismiss();
			timeOptionsFragment = null;

			setDefaultTimeMode(timeSelectBtn, code);
		}

		@Override
		public void onDialogCanceled() {
			timeOptionsFragment = null;
		}
	}

	protected void widgetsInit(View view) {
		Resources resources = getResources();
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		ViewGroup headerView = (ViewGroup) inflater.inflate(R.layout.new_play_home_header_frame, null, false);

		{ // invite overlay setup
			View startOverlayView = headerView.findViewById(R.id.startOverlayView);

			// let's make it to match board properties
			// it should be 2 squares inset from top of border and 4 squares tall + 1 squares from sides
			int squareSize = resources.getDisplayMetrics().widthPixels / 8; // one square size
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

			onlinePlayersCntTxt = (TextView) headerView.findViewById(R.id.onlinePlayersCntTxt);
		}

		headerView.findViewById(R.id.newGameHeaderView).setOnClickListener(this);
		headerView.findViewById(R.id.gamePlayBtn).setOnClickListener(this);

		{ // Time mode adjustments
			int mode = getAppData().getDefaultDailyMode();
			// set texts to buttons
			newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
			timeSelectBtn = (Button) headerView.findViewById(R.id.timeSelectBtn);
			timeSelectBtn.setOnClickListener(this);

			timeSelectBtn.setText(getDaysString(newGameButtonsArray[mode]));
		}

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(new OptionsAdapter(getActivity(), featuresList));
		listView.setOnItemClickListener(this);

		ChessBoardBaseView boardView = (ChessBoardBaseView) headerView.findViewById(R.id.boardview);
		boardView.setGameFace(gameFaceHelper);
	}

	private void setDefaultTimeMode(View view, int mode) {
		view.setSelected(true);
		timeSelectBtn.setText(getDaysString(newGameButtonsArray[mode]));
		gameConfigBuilder.setDaysPerMove(newGameButtonsArray[mode]);
		getAppData().setDefaultDailyMode(mode);
	}

	protected static class DailyItem {
		int iconId;
		int labelId;

		protected DailyItem(int iconId, int labelId) {
			this.iconId = iconId;
			this.labelId = labelId;
		}
	}

	private class OptionsAdapter extends ItemsAdapter<DailyItem> {

		private final int sidePadding;
		private final int whiteColor;

		public OptionsAdapter(Context context, List<DailyItem> itemList) {
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
		protected void bindView(DailyItem item, int pos, View convertView) {
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

	private class GameFaceHelper extends AbstractGameNetworkFaceHelper {

		@Override
		public SoundPlayer getSoundPlayer() {
			return SoundPlayer.getInstance(getActivity());
		}

		@Override
		public boolean isAlive() {
			return getActivity() != null;
		}
	}
}
