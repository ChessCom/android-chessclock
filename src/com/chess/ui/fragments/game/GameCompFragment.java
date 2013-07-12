package com.chess.ui.fragments.game;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.PieceColor;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.engine.stockfish.CompEngineHelper;
import com.chess.ui.engine.stockfish.StartEngineTask;
import com.chess.ui.fragments.CompGameSetupFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameCompActivityFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardCompView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsCompView;
import org.petero.droidfish.GameMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.01.13
 * Time: 6:42
 */
public class GameCompFragment extends GameBaseFragment implements GameCompActivityFace, PopupListSelectionFace {

	private static final String OPTION_SELECTION = "option select popup";
	private static final String MODE = "mode";
	private static final String COMP_DELAY = "comp_delay";
	// Quick action ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_EMAIL_GAME = 1;
	private static final int ID_FLIP_BOARD = 2;
	private static final int ID_SETTINGS = 3;

	private ChessBoardCompView boardView;

	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;

	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private ControlsCompView controlsCompView;

	private LabelsConfig labelsConfig;
	private boolean labelsSet;

	private NotationView notationsView;
	private boolean humanBlack;
	private SparseArray<String> optionsArray;
	private PopupOptionsMenuFragment optionsSelectFragment;

	// new engine
	private int[] compStrengthArray;
	private String[] compTimeLimitArray;
	private String[] compDepth;
	private TextView engineThinkingPath;

	public GameCompFragment() {
		CompGameConfig config = new CompGameConfig.Builder().build();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE,  config.getMode());
		bundle.putInt(COMP_DELAY, config.getMode());
		setArguments(bundle);
	}

	public static GameCompFragment createInstance(CompGameConfig config) {
		GameCompFragment frag = new GameCompFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, config.getMode());
		bundle.putInt(COMP_DELAY, config.getCompDelay());
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (AppData.getCompEngineHelper() != null && AppData.getCompEngineHelper().isInitialized()) {
			byte[] data = AppData.getCompEngineHelper().toByteArray();
			outState.putByteArray(CompEngineHelper.GAME_STATE, data);
			outState.putInt(CompEngineHelper.GAME_STATE_VERSION_NAME, CompEngineHelper.GAME_STATE_VERSION);
		}
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

		topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		init();

		widgetsInit(view);

		{ // Engine init
			engineThinkingPath = (TextView) view.findViewById(R.id.engineThinkingPath);
			compStrengthArray = getResources().getIntArray(R.array.comp_strength);
			compTimeLimitArray = getResources().getStringArray(R.array.comp_time_limit);
			compDepth = getResources().getStringArray(R.array.comp_book_depth);
		}

		startGame(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();

		ChessBoardComp.resetInstance();
		getBoardFace().setMode(getArguments().getInt(MODE));
		if (getAppData().haveSavedCompGame()) {
			loadSavedGame();
		}
		resideBoardIfCompWhite();
		invalidateGameScreen();

		if (!getBoardFace().isAnalysis()) {

			boolean isComputerMove = (getAppData().isComputerVsComputerGameMode(getBoardFace()))
					|| (getAppData().isComputerVsHumanWhiteGameMode(getBoardFace()) && !getBoardFace().isWhiteToMove())
					|| (getAppData().isComputerVsHumanBlackGameMode(getBoardFace()) && getBoardFace().isWhiteToMove());

			if (isComputerMove) {
				computerMove();
			}
		}

		Log.d("", "testtest 6 " + AppData.getCompEngineHelper());

		if (AppData.getCompEngineHelper() != null && AppData.getCompEngineHelper().isInitialized()) {
			AppData.getCompEngineHelper().setPaused(false);
		}
	}

	@Override
	public void onPause() {
		// todo @compengine: extract method and put to engine helper
		if (AppData.getCompEngineHelper().isInitialized()) {
			AppData.getCompEngineHelper().setPaused(true);
			byte[] data = AppData.getCompEngineHelper().toByteArray();
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
			String dataStr = AppData.getCompEngineHelper().byteArrToString(data);
			editor.putString(CompEngineHelper.GAME_STATE, dataStr);
			editor.putInt(CompEngineHelper.GAME_STATE_VERSION_NAME, CompEngineHelper.GAME_STATE_VERSION);
			editor.commit();
		}

		super.onPause();
		if (getAppData().isComputerVsComputerGameMode(getBoardFace()) || getAppData().isComputerVsHumanGameMode(getBoardFace())
				&& boardView.isComputerMoving()) { // probably isComputerMoving() is only necessary to check without extra check of game mode

			boardView.stopComputerMove();
			ChessBoardComp.resetInstance();
		}

		// there is shouldn't be such logic for fragment
//		if (getBoardFace().getMode() != getArguments().getInt(AppConstants.GAME_MODE)) {
//			Intent intent = getIntent();
//			intent.putExtra(AppConstants.GAME_MODE, getBoardFace().getMode());
//			getIntent().replaceExtras(intent);
//		}
	}

	/*@Override
	public void onDestroy() {
		if (AppData.getCompEngineHelper().isInitialized())
			AppData.getCompEngineHelper().shutdownEngine();
		super.onDestroy();
	}*/

	private void startGame(Bundle... savedInstanceState) {
		int engineMode;
		if (getBoardFace().isAnalysis()) {
			engineMode = GameMode.ANALYSIS;
		} else {
			engineMode = CompEngineHelper.mapGameMode(getBoardFace().getMode());
		}
		int strength = compStrengthArray[getAppData().getCompStrength(getContext())];
		int time = Integer.parseInt(compTimeLimitArray[getAppData().getCompStrength(getContext())]);
		int depth = Integer.parseInt(compDepth[getAppData().getCompStrength(getContext())]);
		boolean restoreGame = getAppData().haveSavedCompGame() || getBoardFace().isAnalysis();

		Bundle state = savedInstanceState.length > 0 ? savedInstanceState[0] : null;

		new StartEngineTask(engineMode, restoreGame, strength, time, depth, this,
				PreferenceManager.getDefaultSharedPreferences(getActivity()), state, getActivity().getApplicationContext(),
				new InitComputerEngineUpdateListener()).executeTask();
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
	public void showOptions(View view) {
		if (optionsSelectFragment != null) {
			return;
		}
		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION);
	}

	@Override
	public void updateAfterMove() {
	}

	@Override
	public void invalidateGameScreen() {
		if (!labelsSet) {
			String userName = getAppData().getUserName();
			switch (getBoardFace().getMode()) {
				case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {    //w - human; b - comp
					humanBlack = false;
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = userName;
					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {    //w - comp; b - human
					humanBlack = true;
					labelsConfig.userSide = ChessBoard.BLACK_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = userName;
					break;
				}
				case AppConstants.GAME_MODE_HUMAN_VS_HUMAN: {    //w - human; b - human
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = userName;
					labelsConfig.bottomPlayerLabel = userName;
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

		topPanelView.setPlayerName(labelsConfig.topPlayerLabel);
		bottomPanelView.setPlayerName(labelsConfig.bottomPlayerLabel);

		if (topSide == ChessBoard.NO_SIDE) {
			topPanelView.showFlags(false);
			bottomPanelView.showFlags(true);
		} else {
			topPanelView.showFlags(true);
			bottomPanelView.showFlags(false);
		}

		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	public void onPlayerMove() {
//		controlsCompView.enableGameControls(true);
	}

	@Override
	public void onCompMove() {
//		controlsCompView.enableGameControls(false);
	}

	// todo: use only our Move class
	@Override
	public void updateEngineMove(final org.petero.droidfish.gamelogic.Move engineMove) {

		// TODO @compengine: extract logic and put probably to ChessBoardView

		if (getBoardFace().getHply() < getBoardFace().getMovesCount()) { // ignoring Forward move fired by engine
			return;
		}

		Log.d(CompEngineHelper.TAG, "updateComputerMove " + engineMove);

		int[] moveFT = MoveParser.parseCoordinate(getBoardFace(), engineMove.toString());

		final Move move;
		if (moveFT.length == 4) {
			if (moveFT[3] == 2) {
				move = new com.chess.ui.engine.Move(moveFT[0], moveFT[1], 0, 2);
			} else {
				move = new com.chess.ui.engine.Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
			}
		} else {
			move = new com.chess.ui.engine.Move(moveFT[0], moveFT[1], 0, 0);
		}

		Log.d(CompEngineHelper.TAG, "comp make move: " + move);
		Log.d(CompEngineHelper.TAG, "isHint = " + boardView.isHint());

		if (boardView.isHint()) {
			//onPlayerMove();
			AppData.getCompEngineHelper().undoHint();
		}

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (boardView.isHint()) {
					onPlayerMove();
					//AppData.getCompEngineHelper().undoHint();
				}

				boardView.setComputerMoving(false);

				boardView.addMoveAnimator(move, true);
				getBoardFace().makeMove(move);

				if (boardView.isHint()) {
					//if (/*AppData.isComputerVsComputerGameMode(getBoardFace()) || */(!AppData.isHumanVsHumanGameMode(getBoardFace()))) {
					handler.postDelayed(reverseHintTask, ChessBoardCompView.HINT_REVERSE_DELAY);
					//}
				} else {
					invalidateGameScreen();
					onPlayerMove();

					getBoardFace().setMovesCount(getBoardFace().getHply());
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
			getBoardFace().takeBack();
			boardView.invalidate();

			boardView.setHint(false);
			controlsCompView.enableGameControls(false);
//			gamePanelView.toggleControlButton(GamePanelView.B_HINT_ID, false);
		}
	};

	@Override
	public void toggleSides() {
		if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
		} else {
			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		}
		BoardAvatarDrawable tempDrawable = labelsConfig.topAvatar;
		labelsConfig.topAvatar = labelsConfig.bottomAvatar;
		labelsConfig.bottomAvatar = tempDrawable;

		String tempLabel = labelsConfig.topPlayerLabel;
		labelsConfig.topPlayerLabel = labelsConfig.bottomPlayerLabel;
		labelsConfig.bottomPlayerLabel = tempLabel;
	}

	@Override
	protected void restoreGame() {
		ChessBoardComp.resetInstance();
		ChessBoardComp.getInstance(this).setJustInitialized(false);
		boardView.setGameActivityFace(this);
		getBoardFace().setMode(getArguments().getInt(AppConstants.GAME_MODE));
		loadSavedGame();

		resideBoardIfCompWhite();
	}

	private void loadSavedGame() {
		String[] moves = getAppData().getCompSavedGame().split(RestHelper.SYMBOL_PARAMS_SPLIT_SLASH);

		BoardFace boardFace = getBoardFace();
		boardFace.setMovesCount(moves.length);

		int i;
		for (i = 1; i < moves.length; i++) {
			String[] move = moves[i].split(RestHelper.SYMBOL_PARAMS_SPLIT);
			try {
				getBoardFace().makeMove(new Move(
						Integer.parseInt(move[0]),
						Integer.parseInt(move[1]),
						Integer.parseInt(move[2]),
						Integer.parseInt(move[3])), false);
			} catch (Exception e) {
				String debugInfo = "move=" + moves[i] + getAppData().getCompSavedGame();
				BugSenseHandler.addCrashExtraData("APP_COMP_DEBUG", debugInfo);
				throw new IllegalArgumentException(debugInfo, e);
			}
		}

		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		getActivityFace().openFragment(new CompGameSetupFragment());
	}

	@Override
	public void switch2Analysis() {
		ChessBoardComp.resetInstance();

	}

	@Override
	public Boolean isUserColorWhite() {
		return getAppData().isComputerVsHumanWhiteGameMode(getBoardFace());
	}

	@Override
	public Long getGameId() {
		return null;
	}

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
		String whitePlayerName = getAppData().getUserName();
		String blackPlayerName = getString(R.string.comp);
		String result = GAME_GOES;

		if (getBoardFace().isFinished()) {// means in check state
			if (getBoardFace().getSide() == ChessBoard.WHITE_SIDE) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
		}
		if (!isUserColorWhite()) {
			whitePlayerName = getString(R.string.comp);
			blackPlayerName = getAppData().getUserName();
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
		((TextView) layout.findViewById(R.id.endGameTitleTxt)).setText(message);
		String winner;
		if (message.equals(getString(R.string.black_wins))) {
			if (labelsConfig.userSide == ChessBoard.BLACK_SIDE) {
				winner = labelsConfig.bottomPlayerLabel;
			} else { // labelsConfig.userSide == ChessBoard.WHITE_SIDE
				winner = labelsConfig.topPlayerLabel;
			}

		} else { // message.equals(getString(R.string.white_wins))
			if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
				winner = labelsConfig.bottomPlayerLabel;
			} else { // labelsConfig.userSide == ChessBoard.BLACK_SIDE
				winner = labelsConfig.topPlayerLabel;
			}
//			winner = labelsConfig.topPlayerLabel;
		}
		((TextView) layout.findViewById(R.id.endGameReasonTxt)).setText(getString(R.string.won_by_checkmate, winner)); // TODO adjust
		layout.findViewById(R.id.ratingTitleTxt).setVisibility(View.GONE);
		layout.findViewById(R.id.yourRatingTxt).setVisibility(View.GONE);

//		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
//		MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.shareBtn).setOnClickListener(this);

		getAppData().clearSavedCompGame();

		controlsCompView.enableHintButton(false);
//		if (AppUtils.isNeedToUpgrade(getActivity())) {
//			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
//		}
	}

	private void resideBoardIfCompWhite() {
		if (getAppData().isComputerVsHumanBlackGameMode(getBoardFace())) {
			getBoardFace().setReside(true);
			boardView.invalidate();
		}
	}

	private void computerMove() {
		boardView.computerMove(getAppData().getCompThinkTime());
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.newGamePopupBtn) {
			newGame(); // TODO adjust comp game setup screen
			dismissDialogs();
		} else if (view.getId() == R.id.rematchPopupBtn) {
			newGame();
			dismissDialogs();
		}  else if (view.getId() == R.id.shareBtn) {
			ShareItem shareItem = new ShareItem();

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareItem.composeMessage());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareItem.getTitle());
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_game)));
			dismissDialogs();
		}
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
//			getActivityFace().openFragment(new CompGameSetupFragment()); // TODO
			newGame();
		} else if (code == ID_FLIP_BOARD) {
			boardView.flipBoard();
		} else if (code == ID_EMAIL_GAME) {
			sendPGN();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsBoardFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
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

	private class ImageUpdateListener implements ImageReadyListener {

		private static final int TOP_AVATAR = 0;
		private static final int BOTTOM_AVATAR = 1;
		private int code;

		private ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}
			switch (code) {
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(getContext(), bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topPanelView.invalidate();
					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(getContext(), bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomPanelView.invalidate();
					break;
			}
		}
	}

	public class ShareItem {

		public String composeMessage() {
			String vsStr = getString(R.string.vs);
			String space = StaticData.SYMBOL_SPACE;
			return getAppData().getUserName()+ space + vsStr + space + getString(R.string.vs_computer)
					+ " - " + getString(R.string.chess) + space	+ getString(R.string.via_chesscom);
		}

		public String getTitle() {
			String vsStr = getString(R.string.vs);
			String space = StaticData.SYMBOL_SPACE;
			return "Chess: " + getAppData().getUserName()+ space + vsStr + space + getString(R.string.vs_computer); // TODO adjust i18n
		}
	}

	private void init() {
		labelsConfig = new LabelsConfig();
		getBoardFace().setMode(getArguments().getInt(MODE));
	}

	private void widgetsInit(View view) {

		controlsCompView = (ControlsCompView) view.findViewById(R.id.controlsCompView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		{// set avatars
			Drawable user = new IconDrawable(getActivity(), R.string.ic_profile,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
			Drawable src = new IconDrawable(getActivity(), R.string.ic_comp_game,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);

			labelsConfig.topAvatar = new BoardAvatarDrawable(getActivity(), src);
			if (getBoardFace().getMode() == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
				user = src;
			}
			labelsConfig.bottomAvatar = new BoardAvatarDrawable(getActivity(), user);

			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			if (getBoardFace().getMode() != AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER) {
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
		boardView.setControlsView(controlsCompView);
		boardView.setNotationsView(notationsView);

		boardView.setGameActivityFace(this);
		setBoardView(boardView);

		boardView.lockBoard(true);

		controlsCompView.enableHintButton(true);

		{// options list setup
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsArray.put(ID_EMAIL_GAME, getString(R.string.email_game));
			optionsArray.put(ID_FLIP_BOARD, getString(R.string.flip_board));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

	@Override
	public final void onEngineThinkingInfo(final String thinkingStr1, final String variantStr, final ArrayList<ArrayList<org.petero.droidfish.gamelogic.Move>> pvMoves, final ArrayList<org.petero.droidfish.gamelogic.Move> variantMoves, final ArrayList<org.petero.droidfish.gamelogic.Move> bookMoves) {

		CompEngineHelper.log("thinkingStr1 " + thinkingStr1);
		CompEngineHelper.log("variantStr " + variantStr);

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
					engineThinkingPath.setText(s, TextView.BufferType.SPANNABLE);
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
				List<org.petero.droidfish.gamelogic.Move> hints = null;
				if (/*mShowThinking ||*/ getBoardFace().isAnalysis()) {
					ArrayList<ArrayList<org.petero.droidfish.gamelogic.Move>> pvMovesTmp = pvMoves;
					if (pvMovesTmp.size() == 1) {
						hints = pvMovesTmp.get(0);
					} else if (pvMovesTmp.size() > 1) {
						hints = new ArrayList<org.petero.droidfish.gamelogic.Move>();
						for (ArrayList<org.petero.droidfish.gamelogic.Move> pv : pvMovesTmp)
							if (!pv.isEmpty())
								hints.add(pv.get(0));
					}
				}
				/*if ((hints == null) && mShowBookHints)
					hints = bookMoves;*/
				if (((hints == null) || hints.isEmpty()) &&
						(variantMoves != null) && variantMoves.size() > 1) {
					hints = variantMoves;
				}
				if ((hints != null) && (hints.size() > CompEngineHelper.MAX_NUM_HINT_ARROWS)) {
					hints = hints.subList(0, CompEngineHelper.MAX_NUM_HINT_ARROWS);
				}

				HashMap<org.petero.droidfish.gamelogic.Move, PieceColor> hintsMap =
						new HashMap<org.petero.droidfish.gamelogic.Move, PieceColor>();
				if (hints != null) {
					for (org.petero.droidfish.gamelogic.Move move : hints) {
						boolean isWhite = AppData.getCompEngineHelper().isWhitePiece(move.from);
						PieceColor pieceColor = isWhite ? PieceColor.WHITE : PieceColor.BLACK;
						hintsMap.put(move, pieceColor);
					}
				}

				boardView.setMoveHints(hintsMap);

				CompEngineHelper.log("Thinking info:\n" + log);
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
