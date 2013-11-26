package com.chess.ui.fragments.home;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.engine.SoundPlayer;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.AbstractGameNetworkFaceHelper;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.11.13
 * Time: 5:40
 */
public class HomePlayFragmentTablet extends CommonLogicFragment implements ViewTreeObserver.OnGlobalLayoutListener {

	private int[] newGameButtonsArray;
	private Button timeSelectBtn;
	private GameFaceHelper gameFaceHelper;
	private TextView onlinePlayersCntTxt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameFaceHelper = new GameFaceHelper();

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
