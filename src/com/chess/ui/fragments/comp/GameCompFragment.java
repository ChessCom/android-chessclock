package com.chess.ui.fragments.comp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.model.CompEngineItem;
import com.chess.model.GameAnalysisItem;
import com.chess.model.PgnItem;
import com.chess.model.PopupItem;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.engine.stockfish.CompEngineHelper;
import com.chess.ui.engine.stockfish.StartEngineTask;
import com.chess.ui.fragments.game.GameAnalyzeFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsGeneralFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameCompFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardCompView;
import com.chess.ui.views.chess_boards.NotationFace;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.ui.views.game_controls.ControlsCompView;
import com.chess.utilities.MopubHelper;
import com.chess.widgets.ProfileImageView;
import org.petero.droidfish.GameMode;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.01.13
 * Time: 6:42
 */
public class GameCompFragment extends GameBaseFragment implements GameCompFace, PopupListSelectionFace {

	// Quick action ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_NEW_GAME_WHITE = 1;
	private static final int ID_NEW_GAME_BLACK = 2;
	private static final int ID_SHARE_PGN = 3;
	private static final int ID_FLIP_BOARD = 4;
	private static final int ID_SETTINGS = 5;

	private static final long AUTO_FLIP_DELAY = 500;

	private ChessBoardCompView boardView;
	private ControlsCompView controlsView;

	private boolean labelsSet;

	private NotationFace notationsFace;
	private boolean humanBlack;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;

	// new engine
	private int[] compStrengthArray;
	private String[] compTimeLimitArray;
	private String[] compDepth;
//	private TextView engineThinkingPath;

	private CompGameConfig compGameConfig;
	private boolean isAutoFlip;

	public GameCompFragment() {
		CompGameConfig config = new CompGameConfig.Builder().build();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		setArguments(bundle);
	}

	public static GameCompFragment createInstance(CompGameConfig config) {
		GameCompFragment fragment = new GameCompFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		fragment.setArguments(bundle);
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

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_comp_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.vs_computer);

		widgetsInit(view);

		{ // Engine init
//			engineThinkingPath = (TextView) view.findViewById(R.id.engineThinkingPath);
			compStrengthArray = getResources().getIntArray(R.array.comp_strength);
			compTimeLimitArray = getResources().getStringArray(R.array.comp_time_limit);
			compDepth = getResources().getStringArray(R.array.comp_book_depth);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		updateData();
	}

	private void updateData() {
		getNotationsFace().resetNotations();
		ChessBoardComp.resetInstance();

		isAutoFlip = getAppData().isAutoFlipFor2Players();

		if (compGameConfig.getFen() != null) {
			getBoardFace().setupBoard(compGameConfig.getFen());
			getBoardFace().setMode(compGameConfig.getMode());
		} else if (getAppData().haveSavedCompGame()) {
			loadSavedGame();
		} else {
			getBoardFace().setMode(compGameConfig.getMode());
		}
		resideBoardIfCompWhite();
		invalidateGameScreen();

		if (!getBoardFace().isAnalysis() && boardView.isComputerToMove() && getBoardFace().isCurrentPositionLatest()) {
			computerMove();
		}

		startGame();
	}

	@Override
	public void onPause() {
		CompEngineHelper.getInstance().stop();

		super.onPause();
		if (ChessBoard.isComputerVsComputerGameMode(getBoardFace()) || ChessBoard.isComputerVsHumanGameMode(getBoardFace())
				&& boardView.isComputerMoving()) { // probably isComputerMoving() is only necessary to check without extra check of game mode

			boardView.stopComputerMove();
			ChessBoardComp.resetInstance();
		}
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
		String fen = compGameConfig.getFen();

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
		return labelsConfig.userSide == ChessBoard.WHITE_SIDE ? getUsername() : getString(R.string.comp);
	}

	@Override
	public String getBlackPlayerName() {
		return labelsConfig.userSide == ChessBoard.BLACK_SIDE ? getUsername() : getString(R.string.comp);
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
		if (optionsSelectFragment != null) {
			return;
		}

		if (getBoardFace().getPly() > 0) {
			optionsArray.put(ID_SHARE_PGN, getString(R.string.share_pgn));
		} else {
			optionsArray.remove(ID_SHARE_PGN);
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	@Override
	public void updateAfterMove() {
		if (getBoardFace().getMode() == AppConstants.GAME_MODE_2_PLAYERS && isAutoFlip) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					boardView.flipBoard();
				}
			}, AUTO_FLIP_DELAY);
		}
	}

	@Override
	public void invalidateGameScreen() {
		if (!labelsSet) {
			String username = getAppData().getUsername();
			String blackStr = getString(R.string.black);
			String whiteStr = getString(R.string.white);
			switch (getBoardFace().getMode()) {
				case AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE: {    //w - human; b - comp
					humanBlack = false;
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerName = getString(R.string.computer);
					labelsConfig.bottomPlayerName = username;
					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK: {    //w - comp; b - human
					humanBlack = true;
					labelsConfig.userSide = ChessBoard.BLACK_SIDE;

					labelsConfig.topPlayerName = getString(R.string.computer);
					labelsConfig.bottomPlayerName = username;
					break;
				}
				case AppConstants.GAME_MODE_2_PLAYERS: {    //w - human; b - human
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					if(getBoardFace().isReside()) {
						labelsConfig.topPlayerName = whiteStr;
						labelsConfig.bottomPlayerName = blackStr;
					} else {
						labelsConfig.topPlayerName = blackStr;
						labelsConfig.bottomPlayerName = whiteStr;
					}

					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {    //w - comp; b - comp
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerName = getString(R.string.computer);
					labelsConfig.bottomPlayerName = getString(R.string.computer);
					break;
				}
			}
			labelsSet = true;
		}
		topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
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

		topPanelView.setPlayerName(labelsConfig.topPlayerName);
		bottomPanelView.setPlayerName(labelsConfig.bottomPlayerName);

		if (topSide == ChessBoard.NO_SIDE) {
			topPanelView.showFlags(false);
			bottomPanelView.showFlags(true);
		} else {
			topPanelView.showFlags(true);
			bottomPanelView.showFlags(false);
		}

		boardView.updateNotations(getBoardFace().getFullNotationsArray());
	}

	@Override
	public void onPlayerMove() {
		topPanelView.showThinkingView(false);
		getNotationsFace().setClickable(true);
	}

	@Override
	public void onCompMove() {
		topPanelView.showThinkingView(true);
		getNotationsFace().setClickable(false);
	}

	@Override
	public void onGameStarted(final int currentMovePosition) {
		if (getActivity() == null) {
			return;
		}

		boardView.updateBoardPosition(currentMovePosition);
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				invalidateGameScreen();
				controlsView.enableHintButton(true);
				boardView.invalidate();
			}
		});
	}

	// todo: use only our Move class
	@Override
	public void updateEngineMove(final org.petero.droidfish.gamelogic.Move engineMove) {

		// TODO @compengine: extract logic and put probably to ChessBoardView . Maybe in ChessBoardCompView instead.
		final BoardFace boardFace = getBoardFace();
		if (!boardView.isHint() && boardFace.getPly() < boardFace.getMovesCount()) { // ignoring Forward move fired by engine
			return;
		}

		Log.d(CompEngineHelper.TAG, "updateComputerMove " + engineMove);

		int[] moveFT = boardFace.parseCoordinate(engineMove.toString());
		final Move move = boardFace.convertMove(moveFT);

//		Log.d(CompEngineHelper.TAG, "comp make move: " + move);
//		Log.d(CompEngineHelper.TAG, "isHint = " + boardView.isHint());

		if (boardView.isHint()) {
			CompEngineHelper.getInstance().undoHint();
		}

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				boardView.setMoveAnimator(move, true);
				boardView.resetValidMoves();

				if (boardView.isHint()) {
					boardFace.makeHintMove(move);

					//if (/*AppData.isComputerVsComputerGameMode(getBoardFace()) || */(!AppData.isHumanVsHumanGameMode(getBoardFace()))) {
					handler.postDelayed(reverseHintTask, ChessBoardCompView.HINT_REVERSE_DELAY);
					//}

				} else {
					boardFace.makeMove(move);

					boardView.setComputerMoving(false);
					invalidateGameScreen();
					onPlayerMove();

					boardFace.setMovesCount(boardFace.getPly());
					if (boardView.isGameOver())
						return;
				}
				boardView.invalidate();
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
			getControlsView().enableGameControls(true);
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

		if (getBoardFace().getMode() == AppConstants.GAME_MODE_2_PLAYERS) {
			String blackStr = getString(R.string.black);
			String whiteStr = getString(R.string.white);
			if(getBoardFace().isReside()) {
				labelsConfig.topPlayerName = whiteStr;
				labelsConfig.bottomPlayerName = blackStr;
			} else {
				labelsConfig.topPlayerName = blackStr;
				labelsConfig.bottomPlayerName = whiteStr;
			}
		}
	}

	@Override
	protected void restoreGame() {
		//notationsFace.resetNotations();
		ChessBoardComp.resetInstance();
		ChessBoardComp.getInstance(this).setJustInitialized(false);
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

		boardView.resetValidMoves();
		getBoardFace().setMovesCount(getBoardFace().getPly());
		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		getActivityFace().changeRightFragment(new CompGameOptionsFragment());
		getActivityFace().toggleRightMenu();
	}

	@Override
	public void switch2Analysis() {
		ChessBoardComp.resetInstance();

	}

	@Override
	public boolean isUserColorWhite() {
		return ChessBoard.isComputerVsHumanWhiteGameMode(getBoardFace());
	}

	@Override
	public Long getGameId() {
		return null;
	}

	@Override
	public boolean isUserAbleToMove(int color) {
		if (ChessBoard.isHumanVsHumanGameMode(getBoardFace())) {
			return getBoardFace().isWhiteToMove() ? color == ChessBoard.WHITE_SIDE : color == ChessBoard.BLACK_SIDE;
		}  else {
			return super.isUserAbleToMove(color);
		}
	}

	private void sendPGN() {
		String moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = userPlayWhite? getUsername() : getString(R.string.comp);
		String blackPlayerName = userPlayWhite? getString(R.string.comp) : getUsername();
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
		builder.append("[Event \"").append(getString(R.string.vs_computer)).append("\"]")
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
	protected void showGameEndPopup(View layout, String title, String reason) {
		((TextView) layout.findViewById(R.id.endGameTitleTxt)).setText(title);
		((TextView) layout.findViewById(R.id.endGameReasonTxt)).setText(reason);
		layout.findViewById(R.id.ratingTitleTxt).setVisibility(View.GONE);
		layout.findViewById(R.id.resultRatingTxt).setVisibility(View.GONE);
		layout.findViewById(R.id.resultRatingChangeTxt).setVisibility(View.GONE);

		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView(layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.sharePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.analyzePopupBtn).setOnClickListener(this);

		getAppData().clearSavedCompGame();

		if (isNeedToUpgrade()) {
			initPopupAdWidget(layout);
			MopubHelper.showRectangleAd(getMopubRectangleAd(), getActivity());
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

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.newGamePopupBtn) {
			newGame();
			dismissEndGameDialog();
		} else if (view.getId() == R.id.analyzePopupBtn) {
			GameAnalysisItem analysisItem = new GameAnalysisItem();
			analysisItem.setGameType(RestHelper.V_GAME_CHESS);
			analysisItem.setFen(getBoardFace().generateFullFen());
			analysisItem.setMovesList(getBoardFace().getMoveListSAN());
			analysisItem.copyLabelConfig(labelsConfig);

			getActivityFace().openFragment(GameAnalyzeFragment.createInstance(analysisItem));
			dismissEndGameDialog();
		} else if (view.getId() == R.id.rematchPopupBtn) {
			// change sides
			int mode = compGameConfig.getMode();
			if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE) {
				compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK);
			} else if (mode == AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK) {
				compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE);
			}
			dismissEndGameDialog();

			startNewGame();
		}  else if (view.getId() == R.id.sharePopupBtn) {
			ShareItem shareItem = new ShareItem();

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
			dismissEndGameDialog();
		}
	}

	private void startNewGame() {
		getNotationsFace().resetNotations();
		ChessBoardComp.resetInstance();
		getBoardFace().setMode(compGameConfig.getMode());
		resideBoardIfCompWhite();
		invalidateGameScreen();
		startGame();
	}

	public void updateConfig(CompGameConfig config) {
		compGameConfig = config;
		getBoardFace().setMode(compGameConfig.getMode());

		labelsSet = false;
		invalidateGameScreen();
		boardView.invalidate();
		updateData();
	}

	private class InitComputerEngineUpdateListener extends ChessLoadUpdateListener<CompEngineHelper> {

		@Override
		public void updateData(CompEngineHelper returnedObj) {

			boardView.lockBoard(false);

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
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
			newGame();
		} else if (code == ID_NEW_GAME_WHITE) {
			compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE);
			startNewGame();
		} else if (code == ID_NEW_GAME_BLACK) {
			compGameConfig.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK);
			startNewGame();
		} else if (code == ID_FLIP_BOARD) {
			boardView.flipBoard();
		} else if (code == ID_SHARE_PGN) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsGeneralFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

//	private class LabelsConfig {
//		BoardAvatarDrawable topAvatar;
//		BoardAvatarDrawable bottomAvatar;
//		String topPlayerName;
//		String bottomPlayerName;
//		int userSide;
//
//		int getOpponentSide() {
//			return userSide == ChessBoard.WHITE_SIDE ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
//		}
//	}

	public class ShareItem {

		public String composeMessage() {
			String vsStr = getString(R.string.vs);
			String space = Symbol.SPACE;
			return getAppData().getUsername()+ space + vsStr + space + getString(R.string.vs_computer)
					+ " - " + getString(R.string.chess) + space	+ getString(R.string.via_chesscom);
		}

		public String getTitle() {
			String vsStr = getString(R.string.vs);
			String space = Symbol.SPACE;
			return "Chess: " + getAppData().getUsername()+ space + vsStr + space + getString(R.string.vs_computer); // TODO adjust i18n
		}
	}

	protected ControlsCompView getControlsView() {
		return controlsView;
	}

	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsCompView) controlsView;
	}

	public void setNotationsFace(View notationsView) {
		this.notationsFace = (NotationFace) notationsView;
	}

	public NotationFace getNotationsFace() {
		return notationsFace;
	}

	private void init() {
		labelsConfig = new LabelsConfig();
		getBoardFace().setMode(compGameConfig.getMode());
	}

	private void widgetsInit(View view) {
		setControlsView(view.findViewById(R.id.controlsView));
		if (inPortrait()) {
			setNotationsFace(view.findViewById(R.id.notationsView));
		} else {
			setNotationsFace(view.findViewById(R.id.notationsViewTablet));
		}

		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ProfileImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ProfileImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		int mode = getBoardFace().getMode();
		{// set avatars
			Drawable user = new IconDrawable(getActivity(), R.string.ic_profile,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
			Drawable src = new IconDrawable(getActivity(), R.string.ic_comp_game,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);

			labelsConfig.topAvatar = new BoardAvatarDrawable(getActivity(), src);
			if (mode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
				user = src;
			}
			labelsConfig.bottomAvatar = new BoardAvatarDrawable(getActivity(), user);

			topAvatarImg = (ProfileImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ProfileImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			if (mode != AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER	&& mode != AppConstants.GAME_MODE_2_PLAYERS) {
				ImageDownloaderToListener imageDownloader = new ImageDownloaderToListener(getContext());

				String userAvatarUrl = getAppData().getUserAvatar();
				ImageUpdateListener imageUpdateListener = new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR);
				imageDownloader.download(userAvatarUrl, imageUpdateListener, AVATAR_SIZE);
			}
		}
		// hide timeLeft
		topPanelView.showTimeRemain(false);
		bottomPanelView.showTimeRemain(false);

		boardView = (ChessBoardCompView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);

		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(getControlsView());
		boardView.setNotationsFace(getNotationsFace());
		getNotationsFace().resetNotations();

		boardView.setGameUiFace(this);
		setBoardView(boardView);

		boardView.lockBoard(true);

		{// options list setup
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsArray.put(ID_NEW_GAME_WHITE, getString(R.string.new_game_arg, getString(R.string.white)));
			optionsArray.put(ID_NEW_GAME_BLACK, getString(R.string.new_game_arg, getString(R.string.black)));
			optionsArray.put(ID_SHARE_PGN, getString(R.string.share_pgn));
			optionsArray.put(ID_FLIP_BOARD, getString(R.string.switch_sides));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}

		getControlsView().enableGameControls(false);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getActivity() == null) {
					return;
				}
				getControlsView().enableGameControls(true);
			}
		}, ControlsBaseView.BUTTONS_RE_ENABLE_DELAY);
	}

	@Override
	public final void onEngineThinkingInfo(final String thinkingStr1, final String variantStr, final ArrayList<ArrayList<org.petero.droidfish.gamelogic.Move>> pvMoves, final ArrayList<org.petero.droidfish.gamelogic.Move> variantMoves, final ArrayList<org.petero.droidfish.gamelogic.Move> bookMoves) {

//		CompEngineHelper.log("thinkingStr1 " + thinkingStr1);
//		CompEngineHelper.log("variantStr " + variantStr);

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				String log;

				boolean thinkingEmpty = true;
				{
					//if (mShowThinking || gameMode.analysisMode()) { // getBoardFace().isAnalysis(
					String s = "";
					s = thinkingStr1;
					if (s.length() > 0) {
						thinkingEmpty = false;
						/*if (mShowStats) {
							if (!thinkingEmpty)
								s += "\n";
							s += thinkingStr2;
							if (s.length() > 0) thinkingEmpty = false;
						}*/
					}
					//}
					// TODO reuse when someone will need to need that enhancements. For example in computer analysis
					// engineThinkingPath is always invisible
//					engineThinkingPath.setText(s, TextView.BufferType.SPANNABLE);
					log = s;
				}
				// todo @compengine: show book hints for human player
				/*if (mShowBookHints && (bookInfoStr.length() > 0)) {
					String s = "";
					if (!thinkingEmpty)
						s += "<br>";
					s += Util.boldStart + getString(R.string.book) + Util.boldStop + bookInfoStr;
					engineThinkingPath.append(Html.fromHtml(s));
					log += s;
					thinkingEmpty = false;
				}*/
				/*if (showVariationLine && (variantStr.indexOf(' ') >= 0)) { // showVariationLine
					String s = "";
					if (!thinkingEmpty)
						s += "<br>";
					s += Util.boldStart + "Var:" + Util.boldStop + variantStr;
					engineThinkingPath.append(Html.fromHtml(s));
					log += s;
					thinkingEmpty = false;
				}*/
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

	private void setThinkingVisibility(boolean visible) { // TODO adjust properly in notations view
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

}
