package com.chess.ui.fragments.home;

import android.content.Context;
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
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.daily.DailyGameOptionsFragment;
import com.chess.ui.interfaces.AbstractGameNetworkFaceHelper;
import com.chess.ui.interfaces.ChallengeModeSetListener;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.utilities.ChallengeHelper;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.11.13
 * Time: 5:40
 */
public class HomePlayFragmentTablet extends CommonLogicFragment implements ViewTreeObserver.OnGlobalLayoutListener, ChallengeModeSetListener {

	private static final String END_VACATION_TAG = "end vacation popup";

	private Button timeSelectBtn;
	private GameFaceHelper gameFaceHelper;
	private boolean startDailyGame;
	private ChallengeHelper challengeHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameFaceHelper = new GameFaceHelper();
		challengeHelper = new ChallengeHelper(this);


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
			View parent = (View) view.getParent();
			challengeHelper.show((View)parent.getParent());
		} else if (view.getId() == R.id.gamePlayBtn) {
			if (startDailyGame) {
				challengeHelper.createDailyChallenge();
			} else {
				challengeHelper.createLiveChallenge();
			}
		} else if (view.getId() == R.id.newGameHeaderView) {
			getActivityFace().changeRightFragment(DailyGameOptionsFragment.createInstance(CENTER_MODE));
			getActivityFace().toggleRightMenu();
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

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void setDefaultDailyTimeMode(int mode) {
		String daysString = challengeHelper.getDailyModeButtonLabel(mode);
		timeSelectBtn.setText(daysString);

		startDailyGame = true;
	}

	@Override
	public void setDefaultLiveTimeMode(int mode) {
		String liveLabel = challengeHelper.getLiveModeButtonLabel(mode);
		timeSelectBtn.setText(liveLabel);

		startDailyGame = false;
	}

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		public VacationUpdateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
		}
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
			timeSelectBtn = (Button) view.findViewById(R.id.timeSelectBtn);
			timeSelectBtn.setOnClickListener(this);
			// set texts to buttons
			boolean dailyMode = getAppData().isLastUsedDailyMode();
			if (dailyMode) {
				int timeMode = getAppData().getDefaultDailyMode();
				setDefaultLiveTimeMode(timeMode);
			} else {
				int timeMode = getAppData().getDefaultLiveMode();
				setDefaultLiveTimeMode(timeMode);
			}
		}

		ChessBoardBaseView boardView = (ChessBoardBaseView) view.findViewById(R.id.boardview);
		boardView.setGameFace(gameFaceHelper);
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
