package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.StartEngineTask;
import com.chess.live.client.PieceColor;
import com.chess.model.PopupItem;
import com.chess.ui.engine.*;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.GameCompActivityFace;
import com.chess.ui.views.ChessBoardCompView;
import com.chess.ui.views.GamePanelView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.InneractiveAdHelper;
import com.inneractive.api.ads.InneractiveAd;
import org.petero.droidfish.GameMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * GameTacticsScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class GameCompScreenActivity extends GameBaseActivity implements GameCompActivityFace {

    private MenuOptionsDialogListener menuOptionsDialogListener;
	private ChessBoardCompView boardView;
	protected TextView thinking;
	private int[] compStrengthArray;
	private String[] compTimeLimitArray;
	private String[] compDepth;
	private TextView engineThinkingPath;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (AppData.getCompEngineHelper().isInitialized()) {
			byte[] data = AppData.getCompEngineHelper().toByteArray();
			outState.putByteArray(CompEngineHelper.GAME_STATE, data);
			outState.putInt(CompEngineHelper.GAME_STATE_VERSION_NAME, CompEngineHelper.GAME_STATE_VERSION);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.boardview_comp);

		init();
		widgetsInit();

		startGame(savedInstanceState);
	}

	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		thinking = (TextView) findViewById(R.id.thinking);

		boardView = (ChessBoardCompView) findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setGamePanelView(gamePanelView);

		ChessBoardComp chessBoardComp = ChessBoardComp.getInstance(this);
		boardView.setBoardFace(chessBoardComp);
		boardView.setGameActivityFace(this);
		setBoardView(boardView);

        getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));

		gamePanelView.turnCompMode();

		engineThinkingPath = (TextView)findViewById(R.id.engineThinkingPath);

		gamePanelView.activateAnalysis(false);

        if (getBoardFace().isAnalysis()) {
            boardView.enableAnalysis();
			//AppData.getCompEngineHelper().setAnalysisMode();
            return;
        }

		if (AppData.haveSavedCompGame(this)) {
			if (chessBoardComp.isJustInitialized()) {
				chessBoardComp.setJustInitialized(false);
				loadSavedGame();
			} else {
				chessBoardComp.setHint(false);
				chessBoardComp.setComputerMoving(false);
			}
		} else {
			//getBoardFace().setFen(TextIO.startPosFEN);
		}
		resideBoardIfCompWhite();
	}

	public void init() {
		menuOptionsItems = new CharSequence[] {
				getString(R.string.ngwhite),
				getString(R.string.ngblack),
				getString(R.string.emailgame),
				getString(R.string.settings)};

		menuOptionsDialogListener = new MenuOptionsDialogListener();

		compStrengthArray = getResources().getIntArray(R.array.comp_strength);
		compTimeLimitArray = getResources().getStringArray(R.array.comp_time_limit);
		compDepth = getResources().getStringArray(R.array.comp_book_depth);

	}

	@Override
	protected void onResume() {

		Log.d("", "testtest 6 " + AppData.getCompEngineHelper());

		if (AppData.getCompEngineHelper() != null && AppData.getCompEngineHelper().isInitialized()) {
			AppData.getCompEngineHelper().setPaused(false);
		}

		super.onResume();

		if (boardView.isComputerMoving()) { // explicit init
			ChessBoardComp.getInstance(this);
		}

	}

	@Override
	protected void onPause() {

		// todo @compengine: extract method and put to engine helper
		if (AppData.getCompEngineHelper().isInitialized()) {
			AppData.getCompEngineHelper().setPaused(true);
			byte[] data = AppData.getCompEngineHelper().toByteArray();
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			String dataStr = AppData.getCompEngineHelper().byteArrToString(data);
			editor.putString(CompEngineHelper.GAME_STATE, dataStr);
			editor.putInt(CompEngineHelper.GAME_STATE_VERSION_NAME, CompEngineHelper.GAME_STATE_VERSION);
			editor.commit();
		}

		super.onPause();
		if (AppData.isComputerVsComputerGameMode(getBoardFace()) || AppData.isComputerVsHumanGameMode(getBoardFace())
				&& boardView.isComputerMoving()) { // probably isComputerMoving() is only necessary to check without extra check of game mode
			//boardView.stopThinking();
			boardView.stopComputerMove(); // TODO: @compengine stop computer move?
		}

		if (getBoardFace().getMode() != extras.getInt(AppConstants.GAME_MODE)) {
			Intent intent = getIntent();
			intent.putExtra(AppConstants.GAME_MODE, getBoardFace().getMode());
			getIntent().replaceExtras(intent);
		}
	}

	@Override
	protected void onDestroy() {
		if (AppData.getCompEngineHelper().isInitialized())
			AppData.getCompEngineHelper().shutdownEngine();
		super.onDestroy();
	}

	private void startGame(Bundle... savedInstanceState) {
		int engineMode;
		if (getBoardFace().isAnalysis()) {
			engineMode = GameMode.ANALYSIS;
		} else {
			engineMode = CompEngineHelper.mapGameMode(getBoardFace().getMode());
		}
		int strength = compStrengthArray[AppData.getCompStrength(getContext())];
		int time = Integer.parseInt(compTimeLimitArray[AppData.getCompStrength(getContext())]);
		int depth = Integer.parseInt(compDepth[AppData.getCompStrength(getContext())]);
		boolean restoreGame = AppData.haveSavedCompGame(this) || getBoardFace().isAnalysis();

		Bundle state = savedInstanceState.length > 0 ? savedInstanceState[0] : null;

		new StartEngineTask(engineMode, restoreGame, strength, time, depth, this, PreferenceManager.getDefaultSharedPreferences(this), state, getApplicationContext(), new InitComputerEngineUpdateListener()).executeTask();
	}

	@Override
	public String getWhitePlayerName() {
		return null;
	}

	@Override
	public String getBlackPlayerName() {
		return null;
	}

	@Override
	public boolean currentGameExist() {
		return true;
	}

	@Override
	public void showOptions() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.options)
				.setItems(menuOptionsItems, menuOptionsDialogListener).show();
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {
	}

    @Override
    public void updateAfterMove() {
    }

	@Override
	public void invalidateGameScreen() {
		switch (getBoardFace().getMode()) {
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {	//w - human; b - comp
				whitePlayerLabel.setText(AppData.getUserName(this));
				blackPlayerLabel.setText(getString(R.string.Computer));
				updatePlayerDots(getBoardFace().isWhiteToMove());
				break;
			}
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {	//w - comp; b - human
				whitePlayerLabel.setText(getString(R.string.Computer));
				blackPlayerLabel.setText(AppData.getUserName(this));
				updatePlayerDots(getBoardFace().isWhiteToMove());
				break;
			}
			case AppConstants.GAME_MODE_HUMAN_VS_HUMAN: {	//w - human; b - human
				whitePlayerLabel.setText(getString(R.string.Human));
				blackPlayerLabel.setText(getString(R.string.Human));
				updatePlayerDots(getBoardFace().isWhiteToMove());
				break;
			}
			case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER: {	//w - comp; b - comp
				whitePlayerLabel.setText(getString(R.string.Computer));
				blackPlayerLabel.setText(getString(R.string.Computer));
				break;
			}
		}

		boardView.setMovesLog(getBoardFace().getMoveListSAN());
	}

	@Override
	public void onPlayerMove() {
		whitePlayerLabel.setVisibility(View.VISIBLE);
		blackPlayerLabel.setVisibility(View.VISIBLE);
		thinking.setVisibility(View.GONE);
	}

	@Override
	public void onCompMove() {
		whitePlayerLabel.setVisibility(View.GONE);
		blackPlayerLabel.setVisibility(View.GONE);
		thinking.setVisibility(View.VISIBLE);
	}

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

		runOnUiThread(new Runnable() {
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
				}
				else {
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
			gamePanelView.toggleControlButton(GamePanelView.B_HINT_ID, false);
		}
	};

	@Override
	protected void restoreGame() {

		//startGame();

		ChessBoardComp.resetInstance();
		ChessBoardComp chessBoardComp = ChessBoardComp.getInstance(this);
		boardView.setBoardFace(chessBoardComp);
		boardView.setGameActivityFace(this);
		getBoardFace().setMode(extras.getInt(AppConstants.GAME_MODE));
		loadSavedGame();
		chessBoardComp.setJustInitialized(false);

		resideBoardIfCompWhite();
	}

	private void loadSavedGame() {
		int i;
		String[] moves = AppData.getCompSavedGame(this).split("[|]");

		/*if (moves[1].startsWith("FEN")) {
			getBoardFace().setFen(moves[1].split(":")[1]);
		}*/

		for (i = 1; i < moves.length; i++) {
			String[] move = moves[i].split(":");
			try {
				getBoardFace().makeMove(new Move(
						Integer.parseInt(move[0]),
						Integer.parseInt(move[1]),
						Integer.parseInt(move[2]),
						Integer.parseInt(move[3])), false);
			} catch (Exception e) {
				String debugInfo = "move=" + moves[i] + AppData.getCompSavedGame(this);
				BugSenseHandler.addCrashExtraData("APP_COMP_DEBUG", debugInfo);
				throw new IllegalArgumentException(debugInfo, e);
			}
		}

		playLastMoveAnimation();
	}

	@Override
	public void newGame() {
		onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.game_comp, menu);
		return super.onCreateOptionsMenu(menu);
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

	private class MenuOptionsDialogListener implements DialogInterface.OnClickListener {
		private final int NEW_GAME_WHITE = 0;
		private final int NEW_GAME_BLACK = 1;
		private final int EMAIL_GAME = 2;
		private final int SETTINGS = 3;

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case NEW_GAME_WHITE: {
					ChessBoardComp.resetInstance();
					boardView.setBoardFace(ChessBoardComp.getInstance(GameCompScreenActivity.this));
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
					boardView.invalidate();
					invalidateGameScreen();
					startGame();
					break;
				}
				case NEW_GAME_BLACK: {
					// TODO encapsulate
					ChessBoardComp.resetInstance();
					boardView.setBoardFace(ChessBoardComp.getInstance(GameCompScreenActivity.this));
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
					getBoardFace().setReside(true);
					boardView.invalidate();
					invalidateGameScreen();
					startGame();
					break;
				}
				case EMAIL_GAME: {
					sendPGN();
					break;
				}
				case SETTINGS: {
					startActivity(new Intent(getContext(), PreferencesScreenActivity.class));
					break;
				}
			}
		}
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
		String whitePlayerName = AppData.getUserName(getContext());
		String blackPlayerName = getString(R.string.comp);
		String result = GAME_GOES;
		if(boardView.isFinished()){// means in check state
			if (getBoardFace().getSide() == ChessBoard.LIGHT) {
				result = BLACK_WINS;
			} else {
				result = WHITE_WINS;
			}
		}
		if(!isUserColorWhite()){
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
	public void onGameOver(String message, boolean need2Finish) {
		super.onGameOver(message, need2Finish);
		AppData.getCompEngineHelper().setAnalysisMode();

		// todo @compengine: disable button returning from analysis mode
		preferencesEditor.putString(AppData.getUserName(this) + AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY);
		preferencesEditor.commit();
	}

	@Override
    protected void showGameEndPopup(View layout, String message) {

        TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
        endGameReasonTxt.setText(message);

		/*LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
        MopubHelper.showRectangleAd(adViewWrapper, this);*/

		inneractiveRectangleAd = (InneractiveAd) layout.findViewById(R.id.inneractiveRectangleAd);
		InneractiveAdHelper.showRectangleAd(inneractiveRectangleAd, this);

        PopupItem popupItem = new PopupItem();
        popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.newInstance(popupItem);
        endPopupFragment.show(getSupportFragmentManager(), END_GAME_TAG);

        layout.findViewById(R.id.newGamePopupBtn).setVisibility(View.GONE);
        layout.findViewById(R.id.rematchPopupBtn).setVisibility(View.GONE);
        layout.findViewById(R.id.homePopupBtn).setVisibility(View.GONE);

		Button reviewBtn = (Button) layout.findViewById(R.id.reviewPopupBtn);
        reviewBtn.setText(R.string.ok);
        reviewBtn.setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(this)) {
			/*LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
        MopubHelper.showRectangleAd(adViewWrapper, this);*/
			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
		}
	}

	private void resideBoardIfCompWhite() {
		if (AppData.isComputerVsHumanBlackGameMode(getBoardFace())) {
			getBoardFace().setReside(true);
			boardView.invalidate();
		}
	}

	private class InitComputerEngineUpdateListener extends AbstractUpdateListener<CompEngineHelper> {
		public InitComputerEngineUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(CompEngineHelper returnedObj) {
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

	private void setThinkingVisibility(boolean visible) {
		if (visible) {
			statusBarLay.setVisibility(View.GONE);
			engineThinkingPath.setVisibility(View.VISIBLE);
		} else {
			statusBarLay.setVisibility(View.VISIBLE);
			engineThinkingPath.setVisibility(View.GONE);
		}
	}

	public final void onEngineThinkingInfo(final String thinkingStr1, final String variantStr, final ArrayList<ArrayList<org.petero.droidfish.gamelogic.Move>> pvMoves, final ArrayList<org.petero.droidfish.gamelogic.Move> variantMoves, final ArrayList<org.petero.droidfish.gamelogic.Move> bookMoves) {

		CompEngineHelper.log("thinkingStr1 " + thinkingStr1);
		CompEngineHelper.log("variantStr " + variantStr);

		runOnUiThread(new Runnable() {
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

	public void run(Runnable runnable) { // todo @compengine: check and refactor
		runOnUiThread(runnable);
	}
}