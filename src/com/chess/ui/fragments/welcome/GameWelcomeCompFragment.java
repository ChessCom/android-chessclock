package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.model.CompEngineItem;
import com.chess.model.PgnItem;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.engine.stockfish.CompEngineHelper;
import com.chess.ui.engine.stockfish.StartEngineTask;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsGeneralFragment;
import com.chess.ui.interfaces.FragmentTabsFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameCompFace;
import com.chess.ui.views.NotationsView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.PanelInfoWelcomeView;
import com.chess.ui.views.chess_boards.ChessBoardCompView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.ui.views.game_controls.ControlsCompView;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.MultiDirectionSlidingDrawer;
import com.chess.widgets.RoboTextView;
import com.nineoldandroids.animation.ObjectAnimator;
import org.petero.droidfish.GameMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.05.13
 * Time: 17:15
 */
public class GameWelcomeCompFragment extends GameBaseFragment implements GameCompFace,
		PopupListSelectionFace, AdapterView.OnItemClickListener, MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {

	protected static final int WHAT_IS_CHESSCOM = 1;
	protected static final int PLAY_ONLINE_ITEM = 2;
	protected static final int CHALLENGE_ITEM = 3;
	protected static final int REMATCH_ITEM = 4;
	protected static final int TACTICS_ITEM = 5;
	protected static final int LESSONS_ITEM = 6;
	protected static final int VIDEOS_ITEM = 7;

	protected static final String PLAY_ONLINE_TAG = "play online tag";
	protected static final String CHALLENGE_TAG = "challenge friend tag";
	protected static final String TACTICS_TAG = "tactics tag";
	protected static final String LESSONS_TAG = "lessons tag";
	protected static final String VIDEOS_TAG = "videos tag";
	private static final String OPTION_SELECTION = "option select popup";

	// game op action ids
	private static final int ID_NEW_GAME_WHITE = 0;
	private static final int ID_NEW_GAME_BLACK = 1;
	private static final int ID_ENTER_MOVES = 2;
	private static final int ID_SHARE_PGN = 3;
	private static final int ID_FLIP_BOARD = 4;
	private static final int ID_SETTINGS = 5;

	private static final int FADE_ANIM_DURATION = 300;
	protected static final long DRAWER_APPEAR_DELAY = 100;
	protected static final long END_GAME_DELAY = 1000L;
	protected FragmentTabsFace parentFace;

	protected ChessBoardCompView boardView;

	protected PanelInfoWelcomeView topPanelView;
	protected PanelInfoWelcomeView bottomPanelView;
	protected ControlsCompView controlsView;

	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;

	private LabelsConfig labelsConfig;
	protected boolean labelsSet;

	protected NotationsView notationsView;
	private boolean humanBlack;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private PromotesAdapter resultsAdapter;
	protected MultiDirectionSlidingDrawer slidingDrawer;
	protected RoboTextView resultTxt;
	protected ObjectAnimator fadeBoardAnimator;
	protected ObjectAnimator fadeDrawerAnimator;

	// new engine
	private int[] compStrengthArray;
	private String[] compTimeLimitArray;
	private String[] compDepth;
	private TextView engineThinkingPath;
	private boolean engineThinkingPathVisible;
	private boolean showVariationLine = false;
	private boolean mShowBookHints = true;
	private boolean mShowStats = false;

	protected CompGameConfig compGameConfig;

	public GameWelcomeCompFragment() {
		CompGameConfig config = new CompGameConfig.Builder().build();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		setArguments(bundle);
	}

	public static GameWelcomeCompFragment createInstance(FragmentTabsFace parentFace, CompGameConfig config) {
		GameWelcomeCompFragment fragment = new GameWelcomeCompFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		fragment.setArguments(bundle);
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			compGameConfig = getArguments().getParcelable(CONFIG);
		} else {
			compGameConfig = savedInstanceState.getParcelable(CONFIG);
		}

		labelsConfig = new LabelsConfig();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.game_welcome_comp_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		setTitle(R.string.computer);

		showActionBar(false);

		init();

		widgetsInit(view);

		{ // Engine init
			engineThinkingPath = (TextView) view.findViewById(R.id.engineThinkingPath);
			compStrengthArray = getResources().getIntArray(R.array.comp_strength);
			compTimeLimitArray = getResources().getStringArray(R.array.comp_time_limit);
			compDepth = getResources().getStringArray(R.array.comp_book_depth);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// explicitly disable slide menus
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				enableSlideMenus(false);
			}
		}, 1000);

		resetInstance();

		if (getAppData().haveSavedCompGame()) {
			loadSavedGame();
		} else {
			getBoardFace().setMode(compGameConfig.getMode());
		}
		resideBoardIfCompWhite();
		invalidateGameScreen();

		// todo: check restore on comp move navigated back
		if (!getBoardFace().isAnalysis() && boardView.isComputerToMove() && getBoardFace().isCurrentPositionLatest()) {
			computerMove();
		}

		startGame();
	}

	@Override
	public void onPause() {
		super.onPause();

		CompEngineHelper.getInstance().stop();

		if (ChessBoard.isComputerVsComputerGameMode(getBoardFace()) || ChessBoard.isComputerVsHumanGameMode(getBoardFace())
				&& boardView.isComputerMoving()) { // probably isComputerMoving() is only necessary to check without extra check of game mode

			boardView.stopComputerMove();
			resetInstance();
		}
		labelsSet = false;
	}

	// todo: check rotate screen when it will be actual
	/*@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (CompEngineHelper.getInstance().isInitialized()) {
			byte[] data = CompEngineHelper.getInstance().toByteArray();
			outState.putByteArray(CompEngineHelper.GAME_STATE, data);
			outState.putInt(CompEngineHelper.GAME_STATE_VERSION_NAME, CompEngineHelper.GAME_STATE_VERSION);
		}
		outState.putParcelable(CONFIG, compGameConfig);
	}*/

	private void startGame() {
		int gameMode;
		if (getBoardFace().isAnalysis()) {
			gameMode = GameMode.ANALYSIS;
		} else {
			gameMode = CompEngineHelper.mapGameMode(getBoardFace().getMode());
		}
		// apply changes from settings immediately
		compGameConfig.setStrength(getAppData().getCompLevel());

		int strength = compStrengthArray[compGameConfig.getStrength()];
		int time = Integer.parseInt(compTimeLimitArray[compGameConfig.getStrength()]);
		int depth = Integer.parseInt(compDepth[compGameConfig.getStrength()]);
		boolean isRestoreGame = getAppData().haveSavedCompGame() || getBoardFace().isAnalysis();
		String fen = null;

		CompEngineItem compEngineItem = new CompEngineItem();
		compEngineItem.setGameMode(gameMode);
		compEngineItem.setDepth(depth);
		compEngineItem.setFen(fen);
		compEngineItem.setRestoreGame(isRestoreGame);
		compEngineItem.setStrength(strength);
		compEngineItem.setTime(time);

		new StartEngineTask(compEngineItem, this, new InitComputerEngineUpdateListener()).executeTask();
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
		if (chessBoard == null) {
			chessBoard = new ChessBoardComp(this);
		}
		return chessBoard;
	}

	@Override
	public void showOptions() {
		if (optionsSelectFragment != null) {
			return;
		}
		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION);
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME_WHITE) {
			compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE);
			startNewGame();
		} else if (code == ID_NEW_GAME_BLACK) {
			compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK);
			startNewGame();
		} else if (code == ID_ENTER_MOVES) {
			compGameConfig.setMode(AppConstants.GAME_MODE_2_PLAYERS);
			startNewGame();
		} else if (code == ID_FLIP_BOARD) {
			boardView.flipBoard();
		} else if (code == ID_SHARE_PGN) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(SettingsGeneralFragment.createInstance(SettingsGeneralFragment.WELCOME_MODE));
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	@Override
	public void updateAfterMove() {
	}

	@Override
	public void invalidateGameScreen() {
		if (!labelsSet) {
			String username = getString(R.string.you);
			switch (getBoardFace().getMode()) {
				case AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE: {    //w - human; b - comp
					humanBlack = false;
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = username;
					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK: {    //w - comp; b - human
					humanBlack = true;
					labelsConfig.userSide = ChessBoard.BLACK_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = username;
					break;
				}
				case AppConstants.GAME_MODE_2_PLAYERS: {    //w - human; b - human
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = username;
					labelsConfig.bottomPlayerLabel = username;
					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {    //w - comp; b - comp
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = getString(R.string.computer);
					break;
				}
			}
			labelsSet = true;
		}
		topAvatarImg.setImageDrawable(labelsConfig.topAvatar); // check
		bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);

		topPanelView.setSide(labelsConfig.getOpponentSide());
		bottomPanelView.setSide(labelsConfig.userSide);

		int topSide;
		int bottomSide;

		if (humanBlack) {
			if (getBoardFace().isReside()) {  // if user on top
				topSide = ChessBoard.NO_SIDE;
				bottomSide = labelsConfig.userSide;
			} else {
				topSide = labelsConfig.getOpponentSide();
				bottomSide = ChessBoard.NO_SIDE;
			}
		} else {
			if (getBoardFace().isReside()) {
				topSide = labelsConfig.getOpponentSide();
				bottomSide = ChessBoard.NO_SIDE;
			} else {
				topSide = ChessBoard.NO_SIDE;
				bottomSide = labelsConfig.userSide;
			}
		}

		if (getBoardFace().getMode() == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
			topSide = ChessBoard.NO_SIDE;
			bottomSide = ChessBoard.NO_SIDE;
		}

		labelsConfig.topAvatar.setSide(topSide);
		labelsConfig.bottomAvatar.setSide(bottomSide);

		topPanelView.setPlayerName(labelsConfig.topPlayerLabel);
		bottomPanelView.setPlayerName(labelsConfig.bottomPlayerLabel);

		boardView.updateNotations(getBoardFace().getNotationsArray());
	}

	@Override
	public void onPlayerMove() {
		topPanelView.showThinkingView(false);
		notationsView.setClickable(true);
	}

	@Override
	public void computer() {
		engineThinkingPathVisible = !engineThinkingPathVisible;
		engineThinkingPath.setVisibility(engineThinkingPathVisible ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onCompMove() {
		topPanelView.showThinkingView(true);
		notationsView.setClickable(false);
	}

	@Override
	public void onGameStarted(final int currentMovePosition) {
		Log.d(CompEngineHelper.TAG, " onGameStarted " + currentMovePosition);

		FragmentActivity activity = getActivity();
		if (activity == null || isPaused) {
			return;
		}

		boardView.updateBoardPosition(currentMovePosition);
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}

				invalidateGameScreen();
			}
		});
	}

	// todo: use only our Move class
	@Override
	public void updateEngineMove(final org.petero.droidfish.gamelogic.Move engineMove) {

		// TODO @compengine: extract logic and put probably to ChessBoardView

		final BoardFace boardFace = getBoardFace();
		Log.d(CompEngineHelper.TAG, "updateEngineMove getBoardFace().getPly()=" + boardFace.getPly());
		Log.d(CompEngineHelper.TAG, "updateEngineMove getBoardFace().getMovesCount()=" + boardFace.getMovesCount());

		if (!boardView.isHint() && boardFace.getPly() < boardFace.getMovesCount()) { // ignoring Forward move fired by engine
			return;
		}

		Log.d(CompEngineHelper.TAG, "updateComputerMove " + engineMove);

		int[] moveFT = boardFace.parseCoordinate(engineMove.toString());
		final Move move = boardFace.convertMove(moveFT);

		Log.d(CompEngineHelper.TAG, "comp make move: " + move);
		Log.d(CompEngineHelper.TAG, "isHint = " + boardView.isHint());

		if (boardView.isHint()) {
			CompEngineHelper.getInstance().undoHint();
		}

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//boardView.goToLatestMove();

				boardView.setMoveAnimator(move, true);
				boardView.resetValidMoves();

				if (boardView.isHint()) {
					boardFace.makeHintMove(move);
					boardView.invalidate();

					handler.postDelayed(reverseHintTask, ChessBoardCompView.HINT_REVERSE_DELAY);
				} else {
					boardFace.makeMove(move);
					boardView.setComputerMoving(false);
					invalidateGameScreen();
					onPlayerMove();

					boardFace.setMovesCount(boardFace.getPly());
					// check if game is over. Then show end game popup
					boardView.isGameOver();
				}
			}
		});
	}

	private Runnable reverseHintTask = new Runnable() {
		@Override
		public void run() {
			boardView.setComputerMoving(false);
			boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
			getBoardFace().takeBack();

			getBoardFace().restoreBoardAfterHint();

			boardView.invalidate();

			boardView.setHint(false);
			controlsView.enableGameControls(true);
			onPlayerMove();
		}
	};

	@Override
	public void toggleSides() {
		if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
		} else {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		}
	}

	@Override
	protected void restoreGame() {
		resetInstance();
//		ChessBoardComp.getInstance(this).setJustInitialized(false);
		topPanelView.resetPieces();
		boardView.setGameUiFace(this);
		loadSavedGame();

		resideBoardIfCompWhite();
	}

	private void loadSavedGame() {
		String[] savedGame = getAppData().getCompSavedGame().split(RestHelper.SYMBOL_PARAMS_SPLIT_SLASH);

		int i;
		for (i = 1; i < savedGame.length; i++) {
			String[] move = savedGame[i].split(RestHelper.SYMBOL_PARAMS_SPLIT);
			try {
				getBoardFace().makeMove(new Move(
						Integer.parseInt(move[0]),
						Integer.parseInt(move[1]),
						Integer.parseInt(move[2]),
						Integer.parseInt(move[3])), false);
			} catch (Exception e) {
				String debugInfo = "move=" + savedGame[i] + getAppData().getCompSavedGame();
				BugSenseHandler.addCrashExtraData("APP_COMP_DEBUG", debugInfo);
				throw new IllegalArgumentException(debugInfo, e);
			}
		}

		int gameMode = Integer.valueOf(savedGame[0].substring(0, 1));
		getBoardFace().setMode(gameMode);

		getBoardFace().setMovesCount(getBoardFace().getPly());
		boardView.resetValidMoves();
		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		startNewGame();
	}

	@Override
	public void switch2Analysis() {
	}

	@Override
	public boolean isUserColorWhite() {
		return ChessBoard.isComputerVsHumanWhiteGameMode(getBoardFace());
	}

	@Override
	public Long getGameId() {
		return null;
	}

	private void sendPGN() {
		String moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = userPlayWhite ? getUsername() : getString(R.string.comp);
		String blackPlayerName = userPlayWhite ? getString(R.string.comp) : getUsername();
		String result = GAME_GOES;

		boolean finished = getBoardFace().isFinished();
		if (finished) {// means in check state
			if (getBoardFace().getSide() == ChessBoard.WHITE_SIDE) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
		}

		String date = datePgnFormat.format(Calendar.getInstance().getTime());

		StringBuilder builder = new StringBuilder();
		builder.append("[Event \"").append(getString(R.string.computer)).append("\"]")
				.append("\n [Site \" Chess.com\"]")
				.append("\n [Date \"").append(date).append("\"]")
				.append("\n [White \"").append(whitePlayerName).append("\"]")
				.append("\n [Black \"").append(blackPlayerName).append("\"]")
				.append("\n [Result \"").append(result).append("\"]")
				.append("\n [WhiteElo \"").append("--").append("\"]")
				.append("\n [BlackElo \"").append("--").append("\"]")
				.append("\n [TimeControl \"").append("--").append("\"]");
		if (finished) {
			builder.append("\n [Termination \"").append(endGameReason).append("\"]");
		}
		builder.append("\n ").append(moves).append(Symbol.SPACE).append(result)
				.append("\n \n Sent from my Android");

		PgnItem pgnItem = new PgnItem(whitePlayerName, blackPlayerName);
		pgnItem.setStartDate(date);
		pgnItem.setPgn(builder.toString());

		sendPGN(pgnItem);
	}

	@Override
	public void onGameOver(final String title, String reason) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				boolean userWon = !title.equals(getString(R.string.black_wins)); // how it works for Black user? and how it works for human vs. human mode?

				topPanelView.resetPieces();
				bottomPanelView.resetPieces();

				handler.postDelayed(new Runnable() { // delay to show fling animation
					@Override
					public void run() {
						slidingDrawer.animateOpen();
					}
				}, DRAWER_APPEAR_DELAY);

				slidingDrawer.setVisibility(View.VISIBLE);
				fadeDrawerAnimator.reverse();
				fadeBoardAnimator.start();

				if (userWon) {
					resultTxt.setText(R.string.you_won);
				} else {
					resultTxt.setText(R.string.you_lose);
				}
			}
		}, END_GAME_DELAY);
	}

	@Override
	public boolean isUserAbleToMove(int color) {
		if (ChessBoard.isHumanVsHumanGameMode(getBoardFace())) {
			return getBoardFace().isWhiteToMove() ? color == ChessBoard.WHITE_SIDE : color == ChessBoard.BLACK_SIDE;
		} else {
			return super.isUserAbleToMove(color);
		}
	}

	private void resideBoardIfCompWhite() {
		if (ChessBoard.isComputerVsHumanBlackGameMode(getBoardFace())) {
			getBoardFace().setReside(true);
			boardView.invalidate();
		}
	}

	private void computerMove() {
		boardView.computerMove();
	}

	private class InitComputerEngineUpdateListener extends ChessLoadUpdateListener<CompEngineHelper> {

		@Override
		public void updateData(CompEngineHelper returnedObj) {

			boardView.lockBoard(false);

			// todo @compengine: enable board after full init od engine, show progress

			/*Log.d(CompEngineHelper.TAG, "InitComputerEngineUpdateListener updateData");

			//AppData.setCompEngineHelper(returnedObj);

			if (!getBoardFace().isAnalysis() && !AppData.isHumanVsHumanGameMode(getBoardFace())) {

				boolean isComputerMoveAfterRestore = ((AppData.isComputerVsHumanWhiteGameMode(getBoardFace()) && !getBoardFace().isWhiteToMove())
						|| (AppData.isComputerVsHumanBlackGameMode(getBoardFace()) && getBoardFace().isWhiteToMove() && getBoardFace().getMovesCount() > 0));

				Log.d(CompEngineHelper.TAG, "isComputerMove " + isComputerMoveAfterRestore);

				if (isComputerMoveAfterRestore) {
					Log.d(CompEngineHelper.TAG, "undo last move " + getBoardFace().getLastMove());
					boardView.postMoveToEngine(getBoardFace().getLastMove());
				}
			}*/
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.whatIsChessComTxt) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.WELCOME_FRAGMENT);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case WHAT_IS_CHESSCOM:
				parentFace.changeInternalFragment(WelcomeTabsFragment.WELCOME_FRAGMENT);
				break;
			case PLAY_ONLINE_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_play_online), PLAY_ONLINE_TAG);
				break;
			case CHALLENGE_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_friends), CHALLENGE_TAG);
				break;
			case REMATCH_ITEM:
				int mode = compGameConfig.getMode();
				if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE) {
					compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK);
				} else if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK) {
					compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE);
				}
				getAppData().setCompGameMode(compGameConfig.getMode());
				parentFace.changeInternalFragment(WelcomeTabsFragment.GAME_FRAGMENT);
				break;
			case TACTICS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_tactics), TACTICS_TAG);
				break;
			case LESSONS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_lessons), LESSONS_TAG);
				break;
			case VIDEOS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.please_sign_up_for_videos), VIDEOS_TAG);
				break;
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);

		String tag = fragment.getTag();
		if (isTagEmpty(fragment)) {
			return;
		}

		if (tag.equals(PLAY_ONLINE_TAG) || tag.equals(CHALLENGE_TAG) || tag.equals(TACTICS_TAG)
				|| tag.equals(LESSONS_TAG) || tag.equals(VIDEOS_TAG)) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.SIGN_IN_FRAGMENT);
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);

		String tag = fragment.getTag();
		if (isTagEmpty(fragment)) {
			return;
		}

		if (tag.equals(PLAY_ONLINE_TAG) || tag.equals(CHALLENGE_TAG) || tag.equals(TACTICS_TAG)
				|| tag.equals(LESSONS_TAG) || tag.equals(VIDEOS_TAG)) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.SIGN_UP_FRAGMENT);
		}
	}

	@Override
	public void onDrawerOpened() {
		resetInstance();
		getAppData().clearSavedCompGame();
		notationsView.resetNotations();
		boardView.invalidateMe();
	}

	@Override
	public void onDrawerClosed() {
		slidingDrawer.setVisibility(View.GONE);
		fadeBoardAnimator.reverse();
		fadeDrawerAnimator.start();

		startNewGame();
	}

	protected void startNewGame() {
		boardView.stopComputerMove();
		notationsView.resetNotations();
		resetInstance();
		labelsSet = false;
		getAppData().clearSavedCompGame();

		getBoardFace().setMode(compGameConfig.getMode());
		resideBoardIfCompWhite();
		invalidateGameScreen();
		startGame();
	}

	private class LabelsConfig {
		BoardAvatarDrawable topAvatar;
		BoardAvatarDrawable bottomAvatar;
		String topPlayerLabel;
		String bottomPlayerLabel;
		int userSide;

		int getOpponentSide() {
			return userSide == ChessBoard.WHITE_SIDE ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
		}
	}

	private void init() {
		labelsConfig = new LabelsConfig();
		resetInstance();
		getBoardFace().setMode(compGameConfig.getMode());

		ArrayList<PromoteItem> menuItems = new ArrayList<PromoteItem>();
		menuItems.add(new PromoteItem(R.string.what_is_chess_com, R.string.ic_pawn));
		menuItems.add(new PromoteItem(R.string.play_online, R.string.ic_vs_random));
		menuItems.add(new PromoteItem(R.string.challenge_friend, R.string.ic_challenge_friend));
		menuItems.add(new PromoteItem(R.string.rematch, R.string.ic_comp_game));
		menuItems.add(new PromoteItem(R.string.tactics_and_puzzles, R.string.ic_help));
		menuItems.add(new PromoteItem(R.string.interactive_lessons, R.string.ic_lessons));
		menuItems.add(new PromoteItem(R.string.videos, R.string.ic_play));

		resultsAdapter = new PromotesAdapter(getActivity(), menuItems);
	}

	protected void widgetsInit(View view) {
		Activity activity = getActivity();

		TextView whatIsChessComTxt = (TextView) view.findViewById(R.id.whatIsChessComTxt);
		Drawable icon = new IconDrawable(getActivity(), R.string.ic_round_right, R.color.semitransparent_white_75,
				R.dimen.glyph_icon_big);

		whatIsChessComTxt.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.glyph_icon_padding));
		whatIsChessComTxt.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
		whatIsChessComTxt.setOnClickListener(this);

		controlsView = (ControlsCompView) view.findViewById(R.id.controlsView);
		notationsView = (NotationsView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoWelcomeView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoWelcomeView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		{// set avatars
			Drawable user = new IconDrawable(activity, R.string.ic_profile,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
			Drawable src = new IconDrawable(activity, R.string.ic_comp_game,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
			labelsConfig.topAvatar = new BoardAvatarDrawable(activity, src);
			if (getBoardFace().getMode() == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
				user = src;
			}
			labelsConfig.bottomAvatar = new BoardAvatarDrawable(activity, user);

			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		}

		boardView = (ChessBoardCompView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);

		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsView);
		boardView.setNotationsFace(notationsView);

		boardView.setGameUiFace(this);
		setBoardView(boardView);

		boardView.lockBoard(true);

		controlsView.enableHintButton(true);
//		notationsView.resetNotations(); // don't call explicit view updates

		{// options list setup
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_NEW_GAME_WHITE, getString(R.string.new_game_arg, getString(R.string.white)));
			optionsArray.put(ID_NEW_GAME_BLACK, getString(R.string.new_game_arg, getString(R.string.black)));
			optionsArray.put(ID_ENTER_MOVES, getString(R.string.enter_moves));
			optionsArray.put(ID_FLIP_BOARD, getString(R.string.flip_board));
			optionsArray.put(ID_SHARE_PGN, getString(R.string.share_pgn));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}

		{ // Results part
			ListView resultsListView = (ListView) view.findViewById(R.id.listView);
			// results part
			resultTxt = new RoboTextView(activity);
			resultTxt.setTextColor(activity.getResources().getColor(R.color.white));
			resultTxt.setFont(FontsHelper.BOLD_FONT);
			resultTxt.setTextSize(30);
			resultTxt.setGravity(Gravity.CENTER);
			resultTxt.setMinHeight(getResources().getDimensionPixelSize(R.dimen.result_title_min_height));
			ButtonDrawableBuilder.setBackgroundToView(resultTxt, getStyleForResultTitle());


			resultsListView.setOnItemClickListener(this);
			resultsListView.addHeaderView(resultTxt);
			resultsListView.setAdapter(resultsAdapter);

			slidingDrawer = (MultiDirectionSlidingDrawer) view.findViewById(R.id.slidingDrawer);
			slidingDrawer.setOnDrawerOpenListener(this);
			slidingDrawer.setOnDrawerCloseListener(this);

			fadeDrawerAnimator = ObjectAnimator.ofFloat(slidingDrawer, "alpha", 1, 0);
			fadeDrawerAnimator.setDuration(FADE_ANIM_DURATION);
			slidingDrawer.setVisibility(View.GONE);
			fadeDrawerAnimator.start();
		}

		View boardLinLay = view.findViewById(R.id.boardLinLay);
		fadeBoardAnimator = ObjectAnimator.ofFloat(boardLinLay, "alpha", 1, 0);
		fadeBoardAnimator.setDuration(FADE_ANIM_DURATION);

		controlsView.enableGameControls(false);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				controlsView.enableGameControls(true);
			}
		}, ControlsBaseView.BUTTONS_RE_ENABLE_DELAY);
	}

	protected int getStyleForResultTitle() {
		return R.style.ListItem;
	}

	@Override
	public final void onEngineThinkingInfo(final String thinkingStr1, final String statStr, final String variantStr,
										   final ArrayList<ArrayList<org.petero.droidfish.gamelogic.Move>> pvMoves,
										   final ArrayList<org.petero.droidfish.gamelogic.Move> variantMoves,
										   final ArrayList<org.petero.droidfish.gamelogic.Move> bookMoves) {

		Activity activity = getActivity();
		if (activity == null) {
			return;
		}
		// todo: move to CompEngineHelper and refactor

//		CompEngineHelper.log("thinkingStr1 " + thinkingStr1);
//		CompEngineHelper.log("variantStr " + variantStr);

//		logTest(" variantStr = " + statStr + " thinkingStr1 = " + thinkingStr1
//				+ " pvMoves = " + pvMoves.size() + " variantMoves = " + variantMoves.size());

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				String log;

				boolean thinkingEmpty = true;
				{
					//if (mShowThinking || gameMode.analysisMode()) { // getBoardFace().isAnalysis(
					String s;
					s = thinkingStr1;
					if (s.length() > 0) {
						thinkingEmpty = false;
						if (mShowStats) {
							if (!thinkingEmpty)
								s += "\n";
							s += statStr;
							if (s.length() > 0) thinkingEmpty = false;
						}
					}
					//}
					//
					// engineThinkingPath is always invisible
					engineThinkingPath.setText(s, TextView.BufferType.SPANNABLE);
					log = s;
				}
				// todo @compengine: show book hints for human player
//				if (mShowBookHints && (bookInfoStr.length() > 0)) {
//					String s = "";
//					if (!thinkingEmpty)
//						s += "<br>";
//					s += Util.boldStart + "Book" + Util.boldStop + bookInfoStr;
//					engineThinkingPath.append(Html.fromHtml(s));
//					log += s;
//					thinkingEmpty = false;
//				}
				if (showVariationLine && (variantStr.indexOf(' ') >= 0)) { // showVariationLine
					String s = "";
					if (!thinkingEmpty)
						s += ". ";
					s += "Var: " + variantStr;
					engineThinkingPath.append(s);
					log += s;
					thinkingEmpty = false;
				}
				setThinkingVisibility(!thinkingEmpty);

				// hints arrow
//				List<org.petero.droidfish.gamelogic.Move> hints = null;
//				if (/*mShowThinking ||*/ getBoardFace().isAnalysis()) {
//					ArrayList<ArrayList<org.petero.droidfish.gamelogic.Move>> pvMovesTmp = pvMoves;
//					if (pvMovesTmp.size() == 1) {
//						hints = pvMovesTmp.get(0);
//					} else if (pvMovesTmp.size() > 1) {
//						hints = new ArrayList<org.petero.droidfish.gamelogic.Move>();
//						for (ArrayList<org.petero.droidfish.gamelogic.Move> pv : pvMovesTmp)
//							if (!pv.isEmpty())
//								hints.add(pv.get(0));
//					}
//				}
//				/*if ((hints == null) && mShowBookHints)
//					hints = bookMoves;*/
//				/*if (((hints == null) || hints.isEmpty()) &&
//						(variantMoves != null) && variantMoves.size() > 1) {
//					hints = variantMoves;
//				}
//				if ((hints != null) && (hints.size() > CompEngineHelper.MAX_NUM_HINT_ARROWS)) {
//					hints = hints.subList(0, CompEngineHelper.MAX_NUM_HINT_ARROWS);
//				}*/
//
//				HashMap<org.petero.droidfish.gamelogic.Move, PieceColor> hintsMap =
//						new HashMap<org.petero.droidfish.gamelogic.Move, PieceColor>();
//				if (hints != null) {
//					for (org.petero.droidfish.gamelogic.Move move : hints) {
//						boolean isWhite = CompEngineHelper.getInstance().isWhitePiece(move.from);
//						PieceColor pieceColor = isWhite ? PieceColor.WHITE : PieceColor.BLACK;
//						hintsMap.put(move, pieceColor);
//					}
//				}
//
//				boardView.setMoveHints(hintsMap);
//
//				CompEngineHelper.log("Thinking info:\n" + log);
			}
		});
	}

	private void setThinkingVisibility(boolean visible) {     // TODO adjust properly in notations view
//		if (visible) {
//			engineThinkingPath.setVisibility(View.VISIBLE);
//		} else {
//			engineThinkingPath.setVisibility(View.GONE);
//		}
	}

	@Override
	public void run(Runnable runnable) { // todo @compengine: check and refactor
		FragmentActivity activity = getActivity();
		if (activity != null) { // can be killed at any time
			activity.runOnUiThread(runnable);
		}
	}

	private class PromoteItem {
		public int nameId;
		public int iconRes;
		public boolean selected;

		public PromoteItem(int nameId, int iconRes) {
			this.nameId = nameId;
			this.iconRes = iconRes;
		}
	}

	private class PromotesAdapter extends ItemsAdapter<PromoteItem> {

		public PromotesAdapter(Context context, List<PromoteItem> menuItems) {
			super(context, menuItems);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.results_menu_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.icon = (TextView) view.findViewById(R.id.iconTxt);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);
			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(PromoteItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.icon.setText(item.iconRes);
			holder.title.setText(item.nameId);
		}

		public Context getContext() {
			return context;
		}

		public class ViewHolder {
			TextView icon;
			TextView title;
		}
	}

}
