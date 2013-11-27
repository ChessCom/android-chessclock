package com.chess.ui.fragments.home;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.DailySeekItem;
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.daily.DailyGameOptionsFragment;
import com.chess.ui.fragments.popup_fragments.PopupDailyTimeOptionsFragment;
import com.chess.ui.interfaces.AbstractGameNetworkFaceHelper;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.11.13
 * Time: 5:40
 */
public class HomePlayFragmentTablet extends CommonLogicFragment implements ViewTreeObserver.OnGlobalLayoutListener {

	private static final String OPTION_SELECTION_TAG = "options select popup";
	private static final String END_VACATION_TAG = "end vacation popup";
	private int[] newGameButtonsArray;
	private Button timeSelectBtn;
	private GameFaceHelper gameFaceHelper;
	private PopupDailyTimeOptionsFragment timeOptionsFragment;
	private DailyGameConfig.Builder gameConfigBuilder;
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private TimeOptionSelectedListener timeOptionSelectedListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameConfigBuilder = new DailyGameConfig.Builder();

		gameFaceHelper = new GameFaceHelper();

		createChallengeUpdateListener = new CreateChallengeUpdateListener();
		timeOptionSelectedListener = new TimeOptionSelectedListener();

		if (inLandscape()) {
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.add(R.id.optionsFragmentContainer, HomePlayFragment.createInstance(RIGHT_MENU_MODE))
					.commitAllowingStateLoss();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_no_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);
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
			showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.YOUR_ARE_ON_VACATAION) {
					showPopupDialog(R.string.leave_vacation_to_play_q, END_VACATION_TAG);
				} else {
					super.errorHandle(resultCode);
				}
			}
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

	private void setDefaultTimeMode(View view, int mode) {
		view.setSelected(true);
		timeSelectBtn.setText(getDaysString(newGameButtonsArray[mode]));
		gameConfigBuilder.setDaysPerMove(newGameButtonsArray[mode]);
		getAppData().setDefaultDailyMode(mode);
	}

	@Override
	public void onGlobalLayout() {
		if (getView() == null || getView().getViewTreeObserver() == null) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
		} else {
			getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}

		Resources resources = getResources();

		{ // new game overlay setup
			View startOverlayView = getView().findViewById(R.id.startOverlayView);

			// let's make it to match board properties
			// it should be 2.5 squares inset from top of border and 3 squares tall + 1.5 squares from sides

			View boardview = getView().findViewById(R.id.boardview);
			int boardWidth = boardview.getWidth();
			int squareSize = boardWidth / 8; // one square size
			int borderOffset = resources.getDimensionPixelSize(R.dimen.invite_overlay_top_offset);
			// now we add few pixel to compensate shadow addition
			int shadowOffset = resources.getDimensionPixelSize(R.dimen.overlay_shadow_offset);
			borderOffset += shadowOffset;
			int overlayHeight = squareSize * 3 + borderOffset + shadowOffset;

			int popupWidth = squareSize * 5 + shadowOffset * 2 + borderOffset;  // for tablets we need more width
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(popupWidth, overlayHeight);
			int topMargin = (int) (squareSize * 2.5f + borderOffset - shadowOffset * 2);

			params.setMargins((int) (squareSize * 1.5f - shadowOffset), topMargin, squareSize - borderOffset, 0);
			params.addRule(RelativeLayout.ALIGN_TOP, R.id.boardView);
			startOverlayView.setLayoutParams(params);
			startOverlayView.setVisibility(View.VISIBLE);
		}
	}

	protected void widgetsInit(View view) {
		view.getViewTreeObserver().addOnGlobalLayoutListener(this);

		view.findViewById(R.id.gamePlayBtn).setOnClickListener(this);

		{ // Time mode adjustments
			int mode = getAppData().getDefaultDailyMode();
			// set texts to buttons
			newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
			// TODO add sliding from outside animation for time modes in popup
			timeSelectBtn = (Button) view.findViewById(R.id.timeSelectBtn);
			timeSelectBtn.setOnClickListener(this);
			timeSelectBtn.setText(getDaysString(newGameButtonsArray[mode]));
		}

		ChessBoardBaseView boardView = (ChessBoardBaseView) view.findViewById(R.id.boardview);
		boardView.setGameFace(gameFaceHelper);
	}


	private class GameFaceHelper extends AbstractGameNetworkFaceHelper {

		@Override
		public SoundPlayer getSoundPlayer() {
			return SoundPlayer.getInstance(getActivity());
		}
	}
}
