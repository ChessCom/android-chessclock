package com.chess.ui.fragments.live;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.ui.fragments.friends.FriendsFragment;
import com.chess.ui.fragments.popup_fragments.PopupLiveTimeOptionsFragment;
import com.chess.ui.fragments.stats.StatsGameDetailsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.interfaces.AbstractGameNetworkFaceHelper;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.chess_boards.ChessBoardLiveView;
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

	private static final String OPTION_SELECTION_TAG = "time options popup";

	private GameFaceHelper gameFaceHelper;
	private Button timeSelectBtn;
	private PopupLiveTimeOptionsFragment timeOptionsFragment;
	private TimeOptionSelectedListener timeOptionSelectedListener;
	private String[] newGameButtonsArray;
	private TextView onlinePlayersCntTxt;
	private List<LiveItem> featuresList;
	private LiveGameConfig.Builder liveGameConfigBuilder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		featuresList = new ArrayList<LiveItem>();
		featuresList.add(new LiveItem(R.string.ic_binoculars, R.string.observe));
		featuresList.add(new LiveItem(R.string.ic_stats, R.string.stats));
		featuresList.add(new LiveItem(R.string.ic_challenge_friend, R.string.friends));
		featuresList.add(new LiveItem(R.string.ic_board, R.string.archive));

		liveGameConfigBuilder = new LiveGameConfig.Builder();

		gameFaceHelper = new GameFaceHelper();
		timeOptionSelectedListener = new TimeOptionSelectedListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		getAppData().setLiveChessMode(true);
		liveBaseActivity.connectLcc();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		LiveItem liveItem = (LiveItem) parent.getItemAtPosition(position);

		if (liveItem.iconId == R.string.ic_binoculars) {
			createLiveChallenge(); // TODO should connect and do observe of top game
		} else if (liveItem.iconId == R.string.ic_stats) {
			getActivityFace().openFragment(StatsGameDetailsFragment.createInstance(
					StatsGameFragment.LIVE_STANDARD, true, getUsername()));
		} else if (liveItem.iconId == R.string.ic_challenge_friend) {
			getActivityFace().openFragment(new FriendsFragment());
		} else if (liveItem.iconId == R.string.ic_board) {
			getActivityFace().openFragment(new LiveArchiveFragment());
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
		} else if (view.getId() == R.id.livePlayBtn) {
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
//		fragmentByTag = (BasePopupsFragment) findFragmentByTag(GameLiveFragment.class.getSimpleName());
//		if (fragmentByTag == null) {
//			fragmentByTag = new LiveGameWaitFragment();
//		}

		getActivityFace().openFragment(LiveGameWaitFragment.createInstance(liveGameConfigBuilder.build()));
	}

	private void widgetsInit(View view) {
		Resources resources = getResources();
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		ViewGroup headerView = (ViewGroup) inflater.inflate(R.layout.new_live_home_header_frame, null, false);

		{ // invite overlay setup
			View startOverlayView = headerView.findViewById(R.id.startOverlayView);

			// let's make it to match board properties
			// it should be 2 squares inset from top of border and 4 squares tall + 1 squares from sides
			int sideInset = resources.getDisplayMetrics().widthPixels / 8; // one square size
			int borderOffset = resources.getDimensionPixelSize(R.dimen.invite_overlay_top_offset);
			// now we add few pixel to compensate shadow addition
			int shadowOffset = resources.getDimensionPixelSize(R.dimen.overlay_shadow_offset);
			borderOffset += shadowOffset;
			int overlayHeight = sideInset * 4 + borderOffset + shadowOffset;
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					overlayHeight);
			int topMargin = sideInset * 2 + borderOffset - shadowOffset * 2;

			params.setMargins(sideInset - borderOffset, topMargin, sideInset - borderOffset, 0);
			params.addRule(RelativeLayout.ALIGN_TOP, R.id.boardView);
			startOverlayView.setLayoutParams(params);

			// set online players cnt
			onlinePlayersCntTxt = (TextView) headerView.findViewById(R.id.onlinePlayersCntTxt);
			// TODO call api here
			String playersOnlineStr = NumberFormat.getInstance().format(9745);

			onlinePlayersCntTxt.setText(getString(R.string.players_online_arg, playersOnlineStr));
		}

		headerView.findViewById(R.id.newGameHeaderView).setOnClickListener(this);
		headerView.findViewById(R.id.livePlayBtn).setOnClickListener(this);

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
		listView.setAdapter(new LiveAdapter(getActivity(), featuresList));
		listView.setOnItemClickListener(this);


		ChessBoardLiveView boardView = (ChessBoardLiveView) headerView.findViewById(R.id.boardview);
		boardView.setGameFace(gameFaceHelper);
	}

	private static class LiveItem {
		int iconId;
		int labelId;

		private LiveItem(int iconId, int labelId) {
			this.iconId = iconId;
			this.labelId = labelId;
		}
	}

	private class LiveAdapter extends ItemsAdapter<LiveItem> {

		private final int sidePadding;

		public LiveAdapter(Context context, List<LiveItem> itemList) {
			super(context, itemList);
			sidePadding = resources.getDimensionPixelSize(R.dimen.default_scr_side_padding);
		}

		@Override
		protected View createView(ViewGroup parent) {
			float density = getResources().getDisplayMetrics().density;
			View view = inflater.inflate(R.layout.new_dark_spinner_item, parent, false);

			ButtonDrawableBuilder.setBackgroundToView(view, R.style.ListItem_Header_Dark);
			ViewHolder holder = new ViewHolder();
			holder.nameTxt = (TextView) view.findViewById(R.id.categoryNameTxt);
			holder.iconTxt = (TextView) view.findViewById(R.id.iconTxt);
			holder.spinnerIcon = (TextView) view.findViewById(R.id.spinnerIcon);
			holder.spinnerIcon.setVisibility(View.GONE);
			holder.nameTxt.setPadding((int) (8 * density), 0, 0, 0);
			holder.iconTxt.setVisibility(View.VISIBLE);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(LiveItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			convertView.setPadding(sidePadding, 0, sidePadding, 0);

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

	private String getLiveModeButtonLabel(String label) {
		if (label.contains(Symbol.SLASH)) { // "5 | 2"
			return label;
		} else { // "10 min"
			return getString(R.string.min_arg, label);
		}
	}

	private void setDefaultTimeMode(int mode) {
		timeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[mode]));
		liveGameConfigBuilder.setTimeFromLabel(newGameButtonsArray[mode]);
		getAppData().setDefaultLiveMode(mode);
	}

}
