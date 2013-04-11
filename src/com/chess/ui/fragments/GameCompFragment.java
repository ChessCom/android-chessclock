package com.chess.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameCompActivityFace;
import com.chess.ui.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.views.*;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MopubHelper;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.01.13
 * Time: 6:42
 */
public class GameCompFragment extends GameBaseFragment implements GameCompActivityFace {

	private static final String MODE = "mode";
	private static final String COMP_DELAY = "comp_delay";
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;

//	private MenuOptionsDialogListener menuOptionsDialogListener;
	private ChessBoardCompView boardView;

	private int[] compStrengthArray;
	private NotationView notationsView;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private BoardAvatarDrawable opponentAvatarDrawable;
	private BoardAvatarDrawable userAvatarDrawable;
	private LabelsConfig labelsConfig;
	private ControlsCompView controlsCompView;

	public static GameCompFragment newInstance(NewGameCompView.NewCompGameConfig config) {
		GameCompFragment frag = new GameCompFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, config.getMode());
		bundle.putInt(COMP_DELAY, config.getMode());
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		labelsConfig = new LabelsConfig();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_boardview_comp, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.vs_computer);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		// set avatars
		Bitmap src = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();
		opponentAvatarDrawable = new BoardAvatarDrawable(getActivity(), src);
		userAvatarDrawable = new BoardAvatarDrawable(getActivity(), src);

		topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		// hide timeLeft
		topPanelView.showTimeLeft(false);
		bottomPanelView.showTimeLeft(false);

		init();

		widgetsInit(view);
	}

	private void widgetsInit(View view) {
		controlsCompView = (ControlsCompView) view.findViewById(R.id.controlsCompView);

		boardView = (ChessBoardCompView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsCompView);
		boardView.setNotationsView(notationsView);

		ChessBoardComp chessBoardComp = ChessBoardComp.getInstance(this);
//		boardView.setBoardFace(chessBoardComp);
		boardView.setGameActivityFace(this);
		setBoardView(boardView);

		getBoardFace().setMode(getArguments().getInt(MODE));


		if (getBoardFace().isAnalysis()) {  // TODO recheck logic
//			boardView.enableAnalysis();
			return;
		}

		if (AppData.haveSavedCompGame(getActivity()) && chessBoardComp.isJustInitialized()) {
			chessBoardComp.setJustInitialized(false);
			loadSavedGame();
		}
		resideBoardIfCompWhite();
	}

	private void init() {
//		menuOptionsItems = new CharSequence[]{
//				getString(R.string.new_game_white),
//				getString(R.string.new_game_black),
//				getString(R.string.email_game),
//				getString(R.string.settings)};
//
//		menuOptionsDialogListener = new MenuOptionsDialogListener();

		compStrengthArray = getResources().getIntArray(R.array.comp_strength);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (boardView.isComputerMoving()) { // explicit init
			ChessBoardComp.getInstance(this);
		}

		if (!getBoardFace().isAnalysis()) {

			boolean isComputerMove = (AppData.isComputerVsComputerGameMode(getBoardFace()))
					|| (AppData.isComputerVsHumanWhiteGameMode(getBoardFace()) && !getBoardFace().isWhiteToMove())
					|| (AppData.isComputerVsHumanBlackGameMode(getBoardFace()) && getBoardFace().isWhiteToMove());

			if (isComputerMove) {
				computerMove();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (AppData.isComputerVsComputerGameMode(getBoardFace()) || AppData.isComputerVsHumanGameMode(getBoardFace())
				&& boardView.isComputerMoving()) { // probably isComputerMoving() is only necessary to check without extra check of game mode
			//boardView.stopThinking();
			boardView.stopComputerMove();
			ChessBoardComp.resetInstance(); // how we restore it after resume?
			// - it was implicitly initialized in onResume - onDraw - isComputerMoving method.
			// we reset the current instance because Comp Search method stains the current board instance when searches a move.
			// one of the alternative solutions is to clone Board instance somehow and put it to Comp Search method in order to hold original board
		}

		// TODO restore
//		if (getBoardFace().getMode() != getArguments().getInt(AppConstants.GAME_MODE)) {
//			Intent intent = getIntent();
//			intent.putExtra(AppConstants.GAME_MODE, getBoardFace().getMode());
//			getIntent().replaceExtras(intent);
//		}
	}

	@Override
	public String getWhitePlayerName() {
		return null;
	}

	@Override
	public String getBlackPlayerName() {  // TODO use correct interfaces
		return null;
	}

	@Override
	public boolean currentGameExist() {
		return true;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardComp.getInstance(this);
	}

	@Override
	public void showOptions() {
//		new AlertDialog.Builder(getContext())
//				.setTitle(R.string.options)
//				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void updateAfterMove() {
	}

	@Override
	public void invalidateGameScreen() {
		switch (getBoardFace().getMode()) {
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {    //w - human; b - comp

				labelsConfig.userSide = ChessBoard.WHITE_SIDE;

				labelsConfig.topAvatar = opponentAvatarDrawable;
				labelsConfig.bottomAvatar = userAvatarDrawable;

				labelsConfig.topPlayerLabel = getString(R.string.computer);
				labelsConfig.bottomPlayerLabel = AppData.getUserName(getActivity());

				updatePlayerDots(getBoardFace().isWhiteToMove()); // TODO
				break;
			}
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {    //w - comp; b - human

				labelsConfig.userSide = ChessBoard.BLACK_SIDE;

				labelsConfig.topAvatar = opponentAvatarDrawable;
				labelsConfig.bottomAvatar = userAvatarDrawable;

				labelsConfig.topPlayerLabel = getString(R.string.computer);
				labelsConfig.bottomPlayerLabel = AppData.getUserName(getActivity());

				updatePlayerDots(getBoardFace().isWhiteToMove());
				break;
			}
			case AppConstants.GAME_MODE_HUMAN_VS_HUMAN: {    //w - human; b - human
				labelsConfig.userSide = ChessBoard.BLACK_SIDE;

				labelsConfig.topAvatar = opponentAvatarDrawable;
				labelsConfig.bottomAvatar = userAvatarDrawable;

				labelsConfig.topPlayerLabel = getString(R.string.human);
				labelsConfig.bottomPlayerLabel = AppData.getUserName(getActivity());
				updatePlayerDots(getBoardFace().isWhiteToMove());
				break;
			}
			case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {    //w - comp; b - comp
				labelsConfig.userSide = ChessBoard.BLACK_SIDE;

				labelsConfig.topAvatar = opponentAvatarDrawable;
				labelsConfig.bottomAvatar = userAvatarDrawable;

				labelsConfig.topPlayerLabel = getString(R.string.computer);
				labelsConfig.bottomPlayerLabel = getString(R.string.computer);
				break;
			}
		}

		userAvatarDrawable.setSide(labelsConfig.userSide);
		opponentAvatarDrawable.setSide(labelsConfig.getOpponentSide());

		topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
		bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);

		topPanelView.setSide(labelsConfig.getOpponentSide());
		bottomPanelView.setSide(labelsConfig.userSide);

		topPanelView.setPlayerLabel(labelsConfig.topPlayerLabel);
		bottomPanelView.setPlayerLabel(labelsConfig.bottomPlayerLabel);

//		boardView.updateNotations(getBoardFace().getMoveListSAN());
		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	public void onPlayerMove() {
//		whitePlayerLabel.setVisibility(View.VISIBLE);
//		blackPlayerLabel.setVisibility(View.VISIBLE);
//		bottomPanelView.
		controlsCompView.enableGameControls(true);
	}

	@Override
	public void onCompMove() {
//		whitePlayerLabel.setVisibility(View.GONE);
//		blackPlayerLabel.setVisibility(View.GONE);
		controlsCompView.enableGameControls(false);
		showToast(R.string.thinking_);
	}

	@Override
	protected void restoreGame() {
		ChessBoardComp.resetInstance();
		ChessBoardComp chessBoardComp = ChessBoardComp.getInstance(this);
//		boardView.setBoardFace(chessBoardComp);
		boardView.setGameActivityFace(this);
		getBoardFace().setMode(getArguments().getInt(AppConstants.GAME_MODE));
		loadSavedGame();
		chessBoardComp.setJustInitialized(false);

		resideBoardIfCompWhite();
	}

	private void loadSavedGame() {

		int i;
		String[] moves = AppData.getCompSavedGame(getActivity()).split(RestHelper.SYMBOL_ITEM_SPLIT);
		for (i = 1; i < moves.length; i++) {
			String[] move = moves[i].split(RestHelper.SYMBOL_PARAMS_SPLIT);
			try {
				getBoardFace().makeMove(new Move(
						Integer.parseInt(move[0]),
						Integer.parseInt(move[1]),
						Integer.parseInt(move[2]),
						Integer.parseInt(move[3])), false);
			} catch (Exception e) {
				String debugInfo = "move=" + moves[i] + AppData.getCompSavedGame(getActivity());
				BugSenseHandler.addCrashExtraData("APP_COMP_DEBUG", debugInfo);
				throw new IllegalArgumentException(debugInfo, e);
			}
		}

		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		getActivityFace().showPreviousFragment(); // TODO
	}

	@Override
	public void switch2Analysis(boolean isAnalysis) {
		// TODO restore
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		MenuInflater menuInflater = getMenuInflater();
		inflater.inflate(R.menu.game_comp, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_newGame:
				newGame();
				break;
			case R.id.menu_options:
				showOptions();
				break;
			case R.id.menu_reside:
				boardView.flipBoard();
				break;
			case R.id.menu_hint:
				boardView.showHint();
				break;
			case R.id.menu_previous:
				boardView.moveBack();
				break;
			case R.id.menu_next:
				boardView.moveForward();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Boolean isUserColorWhite() {
		return AppData.isComputerVsHumanWhiteGameMode(getBoardFace());
	}

	@Override
	public Long getGameId() {
		return null;
	}

//	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
//		private final int NEW_GAME_WHITE = 0;
//		private final int NEW_GAME_BLACK = 1;
//		private final int EMAIL_GAME = 2;
//		private final int SETTINGS = 3;
//
//		@Override
//		public void onClick(DialogInterface dialogInterface, int i) {
//			switch (i) {
//				case NEW_GAME_WHITE: {
//					ChessBoardComp.resetInstance();
//					getBoardFace();
//					boardView.setGameActivityFace(GameCompFragment.this);
//					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
//					boardView.invalidate();
//					invalidateGameScreen();
//					break;
//				}
//				case NEW_GAME_BLACK: {
//					// TODO encapsulate
//					ChessBoardComp.resetInstance();
//					getBoardFace();
//					boardView.setGameActivityFace(GameCompFragment.this);
//					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
//					getBoardFace().setReside(true);
//					boardView.invalidate();
//					invalidateGameScreen();
//
//					computerMove();
//					break;
//				}
//				case EMAIL_GAME: {
//					sendPGN();
//					break;
//				}
//
//				case SETTINGS: {
//					startActivity(new Intent(getContext(), SettingsScreenActivity.class));
//					break;
//				}
//			}
//		}
//	}

	private void sendPGN() {
		/*
				[Event "Let's Play!"]
				[Site "Chess.com"]
				[Date "2012.09.13"]
				[White "anotherRoger"]
				[Black "alien_roger"]
				[Result "0-1"]
				[WhiteElo "1221"]
				[BlackElo "1119"]
				[TimeControl "1 in 1 day"]
				[Termination "alien_roger won on time"]
				 */
		CharSequence moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = AppData.getUserName(getContext());
		String blackPlayerName = getString(R.string.comp);
		String result = GAME_GOES;
//		if (boardView.isFinished()) {// means in check state
		if (getBoardFace().isFinished()) {// means in check state
			if (getBoardFace().getSide() == ChessBoard.WHITE_SIDE) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
		}
		if (!isUserColorWhite()) {
			whitePlayerName = getString(R.string.comp);
			blackPlayerName = AppData.getUserName(getContext());
		}
		String date = datePgnFormat.format(Calendar.getInstance().getTime());

		StringBuilder builder = new StringBuilder();
		builder.append("\n [Site \" Chess.com\"]")
				.append("\n [Date \"").append(date).append("\"]")
				.append("\n [White \"").append(whitePlayerName).append("\"]")
				.append("\n [Black \"").append(blackPlayerName).append("\"]")
				.append("\n [Result \"").append(result).append("\"]");

		builder.append("\n ").append(moves)
				.append("\n \n Sent from my Android");

		sendPGN(builder.toString());
	}

	@Override
	protected void showGameEndPopup(View layout, String message) {

		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		endGameReasonTxt.setText(message);

		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
		MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setVisibility(View.GONE);
		layout.findViewById(R.id.rematchPopupBtn).setVisibility(View.GONE);
		layout.findViewById(R.id.homePopupBtn).setVisibility(View.GONE);
		Button reviewBtn = (Button) layout.findViewById(R.id.reviewPopupBtn);
		reviewBtn.setText(R.string.ok);
		reviewBtn.setOnClickListener(this);
		if (AppUtils.isNeedToUpgrade(getActivity())) {
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}
	}

	private void resideBoardIfCompWhite() {
		if (AppData.isComputerVsHumanBlackGameMode(getBoardFace())) {
			getBoardFace().setReside(true);
			boardView.invalidate();
		}
	}

	private void computerMove() {
		boardView.computerMove(compStrengthArray[AppData.getCompStrength(getContext())]);
	}

	private class LabelsConfig {
		int topPlayerSide;
		int bottomPlayerSide;
		String topPlayerLabel;
		String bottomPlayerLabel;
		Drawable topAvatar;
		Drawable bottomAvatar;
		int userSide;

		int getOpponentSide(){
			return userSide == ChessBoard.WHITE_SIDE? ChessBoard.BLACK_SIDE: ChessBoard.WHITE_SIDE;
		}
	}

}
