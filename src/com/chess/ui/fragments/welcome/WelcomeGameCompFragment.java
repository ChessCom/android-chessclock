package com.chess.ui.fragments.welcome;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.chess.FontsHelper;
import com.chess.MultiDirectionSlidingDrawer;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppConstants;
import com.chess.live.client.PieceColor;
import com.chess.model.CompEngineItem;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.engine.stockfish.CompEngineHelper;
import com.chess.ui.engine.stockfish.StartEngineTask;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.interfaces.FragmentTabsFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameCompFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.PanelInfoWelcomeView;
import com.chess.ui.views.chess_boards.ChessBoardCompView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.ui.views.game_controls.ControlsCompView;
import com.nineoldandroids.animation.ObjectAnimator;
import org.petero.droidfish.GameMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.05.13
 * Time: 17:15
 */
public class WelcomeGameCompFragment extends GameBaseFragment implements GameCompFace,
		PopupListSelectionFace, AdapterView.OnItemClickListener, MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {

	private static final int PLAY_ONLINE_ITEM = 1;
	private static final int CHALLENGE_ITEM = 2;
	private static final int REMATCH_ITEM = 3;
	private static final int TACTICS_ITEM = 4;
	private static final int LESSONS_ITEM = 5;
	private static final int VIDEOS_ITEM = 6;

	private static final String PLAY_ONLINE_TAG = "play online tag";
	private static final String CHALLENGE_TAG = "challenge friend tag";
	private static final String TACTICS_TAG = "tactics tag";
	private static final String LESSONS_TAG = "lessons tag";
	private static final String VIDEOS_TAG = "videos tag";
	private static final String OPTION_SELECTION = "option select popup";

	// game op action ids
	private static final int ID_NEW_GAME = 0;
	private static final int ID_EMAIL_GAME = 1;
	private static final int ID_FLIP_BOARD = 2;
	private static final int ID_SETTINGS = 3;

	private static final long BLINK_DELAY = 10 * 1000;
	private static final long UNBLINK_DELAY = 400;
	private static final int FADE_ANIM_DURATION = 300;
	private static final long DRAWER_APPEAR_DELAY = 100;
	private static final long END_GAME_DELAY = 1000L;
	private FragmentTabsFace parentFace;

	private ChessBoardCompView boardView;

	private PanelInfoWelcomeView topPanelView;
	private PanelInfoWelcomeView bottomPanelView;
	private ControlsCompView controlsCompView;

	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;

	private LabelsConfig labelsConfig;
	private boolean labelsSet;

	private NotationView notationsView;
	private boolean humanBlack;
	private SparseArray<String> optionsList;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private PromotesAdapter resultsAdapter;
	private MultiDirectionSlidingDrawer slidingDrawer;
	private RoboTextView resultTxt;
	private ObjectAnimator fadeBoardAnimator;
	private ObjectAnimator fadeDrawerAnimator;
	private TextView whatIsTxt;
	private ColorStateList whatIsTextColor;
	private int whiteTextColor;
	private boolean tourWasClicked;

	// new engine
	private int[] compStrengthArray;
	private String[] compTimeLimitArray;
	private String[] compDepth;
	private TextView engineThinkingPath;
	private Bundle savedInstanceState;

	private CompGameConfig compGameConfig;

	public WelcomeGameCompFragment() {
		CompGameConfig config = new CompGameConfig.Builder().build();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		setArguments(bundle);
	}

	public static WelcomeGameCompFragment createInstance(FragmentTabsFace parentFace, CompGameConfig config) {
		WelcomeGameCompFragment fragment = new WelcomeGameCompFragment();
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
		return inflater.inflate(R.layout.new_game_welcome_comp_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		enableSlideMenus(false);

		setTitle(R.string.vs_computer);

		showActionBar(false);

		init();

		widgetsInit(view);

		{ // Engine init
			engineThinkingPath = (TextView) view.findViewById(R.id.engineThinkingPath);
			compStrengthArray = getResources().getIntArray(R.array.comp_strength);
			compTimeLimitArray = getResources().getStringArray(R.array.comp_time_limit);
			compDepth = getResources().getStringArray(R.array.comp_book_depth);
		}

		this.savedInstanceState = savedInstanceState;
	}

	@Override
	public void onResume() {
		super.onResume();

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				enableSlideMenus(false);
			}
		}, 100);

		ChessBoardComp.resetInstance();
		getBoardFace().setMode(compGameConfig.getMode());
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

		startGame(savedInstanceState);

		if (!tourWasClicked) {
			handler.postDelayed(blinkWhatIs, BLINK_DELAY);
		}
	}

	@Override
	public void onPause() {
		CompEngineHelper.getInstance().stop();

		super.onPause();
		if (getAppData().isComputerVsComputerGameMode(getBoardFace()) || getAppData().isComputerVsHumanGameMode(getBoardFace())
				&& boardView.isComputerMoving()) { // probably isComputerMoving() is only necessary to check without extra check of game mode

			boardView.stopComputerMove();
			ChessBoardComp.resetInstance();
		}

		if (!tourWasClicked) {
			handler.removeCallbacks(blinkWhatIs);
			handler.removeCallbacks(unBlinkWhatIs);
		}
		labelsSet = false;
	}

	/*@Override
	public void onDestroy() {
		Log.d("DEBUGDEBUG", "DESTROY CompEngineHelper.getInstance().isInitialized() " + CompEngineHelper.getInstance().isInitialized());
		if (CompEngineHelper.getInstance().isInitialized())
			CompEngineHelper.getInstance().shutdownEngine();
		super.onDestroy();
	}*/

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (CompEngineHelper.getInstance().isInitialized()) {
			byte[] data = CompEngineHelper.getInstance().toByteArray();
			outState.putByteArray(CompEngineHelper.GAME_STATE, data);
			outState.putInt(CompEngineHelper.GAME_STATE_VERSION_NAME, CompEngineHelper.GAME_STATE_VERSION);
		}
		outState.putParcelable(CONFIG, compGameConfig);
	}

	private void startGame(Bundle savedInstanceState) {
		int gameMode;
		if (getBoardFace().isAnalysis()) {
			gameMode = GameMode.ANALYSIS;
		} else {
			gameMode = CompEngineHelper.mapGameMode(getBoardFace().getMode());
		}
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

		new StartEngineTask(compEngineItem, this,
				PreferenceManager.getDefaultSharedPreferences(getActivity()), savedInstanceState, getActivity().getApplicationContext(),
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
		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsList);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION);
	}

	@Override
	public void updateAfterMove() {
	}

	@Override
	public void invalidateGameScreen() {
		if (!labelsSet) {
			String userName = getString(R.string.you);
			switch (getBoardFace().getMode()) {
				case AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE: {    //w - human; b - comp
					humanBlack = false;
					labelsConfig.userSide = ChessBoard.WHITE_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = userName;
					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK: {    //w - comp; b - human
					humanBlack = true;
					labelsConfig.userSide = ChessBoard.BLACK_SIDE;

					labelsConfig.topPlayerLabel = getString(R.string.computer);
					labelsConfig.bottomPlayerLabel = userName;
					break;
				}
				case AppConstants.GAME_MODE_2_PLAYERS: {    //w - human; b - human
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

		Log.d(CompEngineHelper.TAG, "updateEngineMove getBoardFace().getHply()=" + getBoardFace().getHply());
		Log.d(CompEngineHelper.TAG, "updateEngineMove getBoardFace().getMovesCount()=" + getBoardFace().getMovesCount());

		if (!boardView.isHint() && getBoardFace().getHply() < getBoardFace().getMovesCount()) { // ignoring Forward move fired by engine
			return;
		}

		Log.d(CompEngineHelper.TAG, "updateComputerMove " + engineMove);

		int[] moveFT = MoveParser.parseCoordinate(getBoardFace(), engineMove.toString());
		final Move move = getBoardFace().convertMove(moveFT);

		Log.d(CompEngineHelper.TAG, "comp make move: " + move);
		Log.d(CompEngineHelper.TAG, "isHint = " + boardView.isHint());

		if (boardView.isHint()) {
			//onPlayerMove();
			CompEngineHelper.getInstance().undoHint();
		}

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (boardView.isHint()) {
					onPlayerMove();
					//CompEngineHelper.getInstance().undoHint();
				}

				boardView.setMoveAnimator(move, true);
				boardView.resetValidMoves();
				getBoardFace().makeMove(move);

				if (boardView.isHint()) {
					//if (/*AppData.isComputerVsComputerGameMode(getBoardFace()) || */(!AppData.isHumanVsHumanGameMode(getBoardFace()))) {
					handler.postDelayed(reverseHintTask, ChessBoardCompView.HINT_REVERSE_DELAY);
					//}
				} else {
					boardView.setComputerMoving(false);
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
			boardView.setComputerMoving(false);
			boardView.setMoveAnimator(getBoardFace().getLastMove(), false);
			getBoardFace().takeBack();
			boardView.invalidate();

			boardView.setHint(false);
			controlsCompView.enableGameControls(true);
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
		getBoardFace().setMode(compGameConfig.getMode());
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

		/*Log.d("debugengine", "getAppData().getCompSavedGame() " + getAppData().getCompSavedGame());
		Log.d("debugengine", "getAppData().getCompSavedGame().split(RestHelper.SYMBOL_PARAMS_SPLIT_SLASH) " + getAppData().getCompSavedGame().split(RestHelper.SYMBOL_PARAMS_SPLIT_SLASH));
		Log.d("debugengine", "getBoardFace().getHply() " + getBoardFace().getHply());*/

		getBoardFace().setMovesCount(getBoardFace().getHply());
		boardView.resetValidMoves();
		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		getActivityFace().changeRightFragment(WelcomeCompGameOptionsFragment.createInstance(parentFace));
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				getActivityFace().toggleRightMenu();
			}
		}, 100);
	}

	@Override
	public void switch2Analysis() {
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
		CharSequence moves = getBoardFace().getMoveListSAN();
		String whitePlayerName = getAppData().getUsername();
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
			blackPlayerName = getAppData().getUsername();
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
	public void onGameOver(final String message, boolean need2Finish) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				boolean userWon = !message.equals(getString(R.string.black_wins));

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

		if (view.getId() == PanelInfoWelcomeView.WHAT_IS_TXT_ID) {
			if (parentFace != null)
				parentFace.changeInternalFragment(WelcomeTabsFragment.FEATURES_FRAGMENT);
			tourWasClicked = true;

			handler.removeCallbacks(blinkWhatIs);
			handler.removeCallbacks(unBlinkWhatIs);
		}
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
	public void onValueSelected(int code) {
		if (code == ID_NEW_GAME) {
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case PLAY_ONLINE_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.you_must_have_account_to, getString(R.string.play_online)), PLAY_ONLINE_TAG);
				break;
			case CHALLENGE_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.you_must_have_account_to, getString(R.string.challenge_friend)), CHALLENGE_TAG);
				break;
			case REMATCH_ITEM:
				parentFace.changeInternalFragment(WelcomeTabsFragment.GAME_FRAGMENT);
				break;
			case TACTICS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.you_must_have_account_to, getString(R.string.solve_tactics_puzzles)), TACTICS_TAG);
				break;
			case LESSONS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.you_must_have_account_to, getString(R.string.learn_lessons)), LESSONS_TAG);
				break;
			case VIDEOS_ITEM:
				popupItem.setPositiveBtnId(R.string.log_in);
				popupItem.setNegativeBtnId(R.string.sign_up);
				showPopupDialogTouch(getString(R.string.you_must_have_account_to, getString(R.string.watch_videos)), VIDEOS_TAG);
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
		ChessBoardComp.resetInstance();
		getAppData().clearSavedCompGame();
		notationsView.resetNotations();
		boardView.invalidate();
	}

	@Override
	public void onDrawerClosed() {
		slidingDrawer.setVisibility(View.GONE);
		fadeBoardAnimator.reverse();
		fadeDrawerAnimator.start();
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
		ChessBoardComp.resetInstance();
		getBoardFace().setMode(compGameConfig.getMode());

		ArrayList<PromoteItem> menuItems = new ArrayList<PromoteItem>();
		menuItems.add(new PromoteItem(R.string.play_online, R.string.ic_play_online));
		menuItems.add(new PromoteItem(R.string.challenge_friend, R.string.ic_challenge_friend));
		menuItems.add(new PromoteItem(R.string.rematch_computer, R.string.ic_comp_game));
		menuItems.add(new PromoteItem(R.string.tactics_and_puzzles, R.string.ic_help));
		menuItems.add(new PromoteItem(R.string.interactive_lessons, R.string.ic_lessons));
		menuItems.add(new PromoteItem(R.string.videos, R.string.ic_play));

		resultsAdapter = new PromotesAdapter(getActivity(), menuItems);
	}

	private void widgetsInit(View view) {
		Activity activity = getActivity();

		controlsCompView = (ControlsCompView) view.findViewById(R.id.controlsCompView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoWelcomeView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoWelcomeView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		{ // animate whatIsChess.com
			whatIsTxt = (TextView) bottomPanelView.findViewById(PanelInfoWelcomeView.WHAT_IS_TXT_ID);
			whatIsTxt.setVisibility(View.VISIBLE);
			whatIsTxt.setOnClickListener(this);
			whatIsTextColor = activity.getResources().getColorStateList(R.color.text_controls_icons);
			whiteTextColor = activity.getResources().getColor(R.color.white);
		}

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
		boardView.setControlsView(controlsCompView);
		boardView.setNotationsView(notationsView);

		boardView.setGameActivityFace(this);
		setBoardView(boardView);

		boardView.lockBoard(true);

		controlsCompView.enableHintButton(true);
		notationsView.resetNotations();

		{// options list setup
			optionsList = new SparseArray<String>();
			optionsList.put(ID_NEW_GAME, getString(R.string.new_game));
			optionsList.put(ID_FLIP_BOARD, getString(R.string.flip_board));
			optionsList.put(ID_EMAIL_GAME, getString(R.string.email_game));
			optionsList.put(ID_SETTINGS, getString(R.string.settings));
		}

		{ // Results part
			ListView resultsListView = (ListView) view.findViewById(R.id.listView);
			// results part
			resultTxt = new RoboTextView(activity);
			resultTxt.setTextColor(activity.getResources().getColor(R.color.white));
			resultTxt.setFont(FontsHelper.BOLD_FONT);
			resultTxt.setTextSize(30);
			resultTxt.setGravity(Gravity.CENTER);
			resultTxt.setMinHeight(activity.getResources().getDimensionPixelSize(R.dimen.result_title_min_height));
			ButtonDrawableBuilder.setBackgroundToView(resultTxt, R.style.ListItem);


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
	}

	@Override
	public final void onEngineThinkingInfo(final String thinkingStr1, final String variantStr, final ArrayList<ArrayList<org.petero.droidfish.gamelogic.Move>> pvMoves, final ArrayList<org.petero.droidfish.gamelogic.Move> variantMoves, final ArrayList<org.petero.droidfish.gamelogic.Move> bookMoves) {

		CompEngineHelper.log("thinkingStr1 " + thinkingStr1);
		CompEngineHelper.log("variantStr " + variantStr);

		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		activity.runOnUiThread(new Runnable() {
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
						boolean isWhite = CompEngineHelper.getInstance().isWhitePiece(move.from);
						PieceColor pieceColor = isWhite ? PieceColor.WHITE : PieceColor.BLACK;
						hintsMap.put(move, pieceColor);
					}
				}

				boardView.setMoveHints(hintsMap);

				CompEngineHelper.log("Thinking info:\n" + log);
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

	private Runnable blinkWhatIs = new Runnable() {
		@Override
		public void run() {
			whatIsTxt.setTextColor(whiteTextColor);

			handler.removeCallbacks(unBlinkWhatIs);
			handler.postDelayed(unBlinkWhatIs, UNBLINK_DELAY);
		}
	};

	private Runnable unBlinkWhatIs = new Runnable() {
		@Override
		public void run() {
			whatIsTxt.setTextColor(whatIsTextColor);

			handler.removeCallbacks(blinkWhatIs);
			handler.postDelayed(blinkWhatIs, BLINK_DELAY);
		}
	};

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
			View view = inflater.inflate(R.layout.new_results_menu_item, parent, false);
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
