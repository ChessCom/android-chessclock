package com.chess.ui.engine;

import java.io.File;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.chess.backend.statics.AppConstants;
import com.chess.ui.interfaces.GameCompActivityFace;
import org.petero.droidfish.*;
import org.petero.droidfish.book.BookOptions;
import org.petero.droidfish.gamelogic.*;
import org.petero.droidfish.gamelogic.GameTree.Node;
import org.petero.droidfish.gamelogic.Move;
import org.petero.droidfish.gtb.Probe;
import android.content.Context;
import android.os.Environment;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 23.04.13
 * Time: 14:44
 */

public class CompEngineHelper implements GUIInterface {

	private static final String ENGINE = "stockfish";
	//private static final String ENGINE = "cuckoochess";

	public static final String GAME_STATE = "gameState";
	public static final String GAME_STATE_VERSION_NAME = "gameStateVersion";
	public static final int GAME_STATE_VERSION = 3;

	private static final boolean LOGGING_ON = true;
	public static final String TAG = "COMPENGINE";

	private static DroidChessController engineCtrl = null;

	private boolean whiteBasedScores;

	int depth;
	private GameMode gameMode;
	private boolean mPonderMode;
	private int mEngineThreads;

	private final static String bookDir = "ComChess";
	/*private final static String pgnDir = "ComChess/pgn";
	private final static String fenDir = "ComChess/epd";*/
	private final static String engineDir = "ComChess/uci";
	private final static String gtbDefaultDir = "ComChess/gtb";
	private BookOptions bookOptions = new BookOptions();
	private PGNOptions pgnOptions = new PGNOptions();
	private EngineOptions engineOptions = new EngineOptions();

	private Context context;
	private GameCompActivityFace gameCompActivityFace;
	private byte[] stateBeforeHint;
	private TimeControlData timeControlData;
	private String variantStr = "";

	public DroidChessController init(Context context) {

		//long start = System.currentTimeMillis();

		this.context = context;

		PgnToken.PgnTokenReceiver pgnTokenReceiver = new PgnToken.PgnTokenReceiver() { // updates moves list
			@Override
			public boolean isUpToDate() {
				return true; // todo
			}

			@Override
			public void clear() {
			}

			@Override
			public void processToken(Node node, int type, String token) {
				/*log("PgnTokenReceiver processToken: node =" + node);
				log("PgnTokenReceiver processToken: type =" + type);
				log("PgnTokenReceiver processToken: token =" + token);*/
			}

			@Override
			public void setCurrent(Node node) {
				//log("PgnTokenReceiver setCurrent: node =" + node);
			}
		};

		createDirectories();

		if (engineCtrl != null)
			engineCtrl.shutdownEngine();

		engineCtrl = new DroidChessController(this, pgnTokenReceiver, pgnOptions);
		//egtbForceReload = true;
		readPrefs();

		return engineCtrl;
	}

	public void startGame(int gameMode, boolean restoreGame, int strength, int time, int depth, GameCompActivityFace gameCompActivityFace, SharedPreferences settings, Bundle savedInstanceState) {

		log("INIT ENGINE AND START GAME");

		this.gameMode = new GameMode(gameMode);
		this.gameCompActivityFace = gameCompActivityFace;
		this.depth = depth;

		initTimeControlData(time);

		engineCtrl.newGame(this.gameMode, timeControlData, depth);

		if (restoreGame) {
			byte[] data = null;
			int version = 1;
			if (savedInstanceState != null) {
				data = savedInstanceState.getByteArray(CompEngineHelper.GAME_STATE);
				version = savedInstanceState.getInt(GAME_STATE_VERSION_NAME, version);
			} else {
				String dataStr = settings.getString(CompEngineHelper.GAME_STATE, null);
				version = settings.getInt(GAME_STATE_VERSION_NAME, version);
				if (dataStr != null)
					data = strToByteArr(dataStr);
			}
			if (data != null)
				engineCtrl.fromByteArray(data, version);
		}

		engineCtrl.setGuiPaused(true);
		engineCtrl.setGuiPaused(false);

		//String fenNew = fen == null ? TextIO.startPosFEN : fen;
		/*if (fen != null) {
			try {
				engineCtrl.setFENOrPGN(fen);
				log("RESTORE set engine setFENOrPGN = " + fen);
			} catch (ChessParseError chessParseError) {
				log("setFENOrPGN: " + chessParseError);
				chessParseError.printStackTrace();
			}
		}*/

		log("set strength = " + strength);
		engineCtrl.setEngineStrength(ENGINE, strength);

		engineCtrl.startGame(); // it was before setFENOrPGN
	}

	public void makeMove(String move,/* boolean force,*/ GameCompActivityFace gameCompActivityFace) {
		this.gameCompActivityFace = gameCompActivityFace;

		log("MAKE MOVE position\n" + engineCtrl.getCurrentPosition());

		/*log("UNDO force=" + force);
		if (force) {
			engineCtrl.undoMove(); // undo does not work after restarted game
		}*/

		log("human make move: " + move);
		engineCtrl.makeHumanMove(TextIO.UCIstringToMove(move));
	}

	private final static void createDirectories() {

		// todo @refactor: create directories only first time

		File extDir = Environment.getExternalStorageDirectory();

		log("createDirectories extDir=" + extDir);

		String sep = File.separator;
		boolean result = new File(extDir + sep + bookDir).mkdirs();
		log("createDirectories result=" + result);
		/*result = new File(extDir + sep + pgnDir).mkdirs();
		log("createDirectories result=" + result);
		result = new File(extDir + sep + fenDir).mkdirs();
		log("createDirectories result=" + result);*/
		result = new File(extDir + sep + engineDir).mkdirs();
		log("createDirectories result=" + result);
		result = new File(extDir + sep + gtbDefaultDir).mkdirs();
		log("createDirectories result=" + result);
	}

	private final void readPrefs() {
		whiteBasedScores = false;
		mEngineThreads = 1;

		mPonderMode = false;
		if (!mPonderMode)
			engineCtrl.stopPonder();

		bookOptions.filename = "";
		bookOptions.maxLength = 1000000;
		bookOptions.preferMainLines = false;
		bookOptions.tournamentMode = false;
		bookOptions.random = (500 - 500) * (3.0 / 500); // check
		setBookOptions();

		pgnOptions.view.variations  = true;
		pgnOptions.view.comments    = true;
		pgnOptions.view.nag         = true;
		pgnOptions.view.headers     = false;
		pgnOptions.view.pieceType   = PGNOptions.PT_LOCAL;
		pgnOptions.imp.variations   = true;
		pgnOptions.imp.comments     = true;
		pgnOptions.imp.nag          = true;
		pgnOptions.exp.variations   = true;
		pgnOptions.exp.comments     = true;
		pgnOptions.exp.nag          = true;
		pgnOptions.exp.playerAction = false;
		pgnOptions.exp.clockInfo    = false;

		engineOptions.hashMB = 16;
		engineOptions.hints = false;
		engineOptions.hintsEdit = false;
		engineOptions.rootProbe = true;
		engineOptions.engineProbe = true;
		/*String gtbPath = "";
		if (gtbPath.length() == 0) {
			File extDir = Environment.getExternalStorageDirectory();
			String sep = File.separator;
			gtbPath = extDir.getAbsolutePath() + sep + gtbDefaultDir;
		}
		engineOptions.gtbPath = gtbPath;*/
		setEngineOptions(false);
	}

	@Override
	public void updateEngineTitle() {
	}

	@Override
	public void updateMaterialDifferenceTitle(Util.MaterialDiff diff) {
	}

	@Override
	public void updateTimeControlTitle() {
	}

	private final void setBookOptions() {
		BookOptions options = new BookOptions(bookOptions);
		if (options.filename.length() > 0) {
			File extDir = Environment.getExternalStorageDirectory();
			String sep = File.separator;
			options.filename = extDir.getAbsolutePath() + sep + bookDir + sep + options.filename;
		}
		engineCtrl.setBookOptions(options);
	}

	private boolean egtbForceReload = false;

	private final void setEngineOptions(boolean restart) {
		engineCtrl.setEngineOptions(new EngineOptions(engineOptions), restart);
		Probe.getInstance().setPath(engineOptions.gtbPath, egtbForceReload);
		egtbForceReload = false;
	}

	@Override
	public void setSelection(int sq) {
	}

	@Override
	public void setStatus(GameStatus s) {
		log("setStatus - gameStatus = " + s);
	}

	@Override
	public void moveListUpdated() {
	}

	@Override
	public boolean whiteBasedScores() {
		return whiteBasedScores;
	}

	@Override
	public boolean ponderMode() {
		return mPonderMode;
	}

	@Override
	public int engineThreads() {
		return mEngineThreads;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public String playerName() {
		return null;
	}

	@Override
	public boolean discardVariations() {
		return false;
	}

	public void setAnimMove(Position sourcePos, org.petero.droidfish.gamelogic.Move engineMove, boolean forward) {
		final GameMode gameMode = engineCtrl.getGameMode();

		//log("setAnimMove: sourcePos =\n" + sourcePos);
		log("engineCtrl.getCurrentPosition() " + engineCtrl.getCurrentPosition());
		log("setAnimMove: move = " + engineMove);
		log("setAnimMove: forward = " + forward);
		log("setAnimMove gameMode.playerWhite() " + gameMode.playerWhite());
		log("setAnimMove gameMode.playerBlack() " + gameMode.playerBlack());

		boolean isWhiteUserTurn = gameMode.playerWhite() && sourcePos.whiteMove;
		boolean isBlackUserTurn = gameMode.playerBlack() && !sourcePos.whiteMove;
		if (!forward || isWhiteUserTurn || isBlackUserTurn) {
			log("setAnimMove ignore " + engineMove);
			return;
		}

		log("setAnimMove set game mode " + gameMode);
		log("setAnimMove isWhiteUserTurn " + isWhiteUserTurn);
		log("setAnimMove isBlackUserTurn " + isBlackUserTurn);
		//this.gameMode = gameMode;

		gameCompActivityFace.updateCompMove(engineMove.toString());
	}

	/*public String getFen() {
		return engineCtrl.getFEN();
	}*/

	@Override
	public void setPosition(Position pos, String variantInfo, ArrayList<org.petero.droidfish.gamelogic.Move> variantMoves) {
		log("setPosition - pos = \n" + pos);
		log("setPosition - variantInfo =" + variantInfo);
		log("setPosition - variantMoves =" + variantMoves.toString());

		variantStr = variantInfo;
	}

	@Override
	public void setThinkingInfo(String pvStr, String statStr, String bookInfo,
								ArrayList<ArrayList<org.petero.droidfish.gamelogic.Move>> pvMoves, ArrayList<org.petero.droidfish.gamelogic.Move> bookMoves) {

		log("setThinkingInfo - pvStr =" + pvStr);
		log("setThinkingInfo - statStr =" + statStr);
		log("setThinkingInfo - bookInfo =" + bookInfo);
		log("setThinkingInfo - pvMoves =" + pvMoves);
		log("setThinkingInfo - bookMoves =" + bookMoves);

        String thinkingStr1 = pvStr;
		//String thinkingStr2 = statStr;
		String bookInfoStr = bookInfo;

 		gameCompActivityFace.onEngineThinkingInfo(thinkingStr1, variantStr);

        /*if (engineCtrl.computerBusy()) {
            lastComputationMillis = System.currentTimeMillis();
        } else {
            lastComputationMillis = 0;
        }
        updateNotification();*/
	}

	@Override
	public void requestPromotePiece() {
	}

	@Override
	public void reportInvalidMove(Move m) {
		log("reportInvalidMove: move =" + m);
	}

	@Override
	public void reportEngineName(String engine) {
		log("reportEngineName: engine =" + engine);
	}

	@Override
	public void reportEngineError(String errMsg) {
		log("reportEngineError: errMsg =" + errMsg);
	}

	@Override
	public void computerMoveMade() {
		log("computerMoveMade");
	}

	@Override
	public void setRemainingTime(int wTime, int bTime, int nextUpdate) {
	}

	@Override
	public void runOnUIThread(Runnable runnable) {
		gameCompActivityFace.run(runnable);
	}

	public void setGameMode(int newMode) {
		final GameMode newGameMode = new GameMode(newMode);
		engineCtrl.setGameMode(newGameMode);
		//gameMode = newGameMode;
	}

	public void setAnalysisMode() {
		setGameMode(GameMode.ANALYSIS);
	}

	public void makeHint() {

		// todo @compengine: use DroidFish hint way as soon as it will be implemented

		Log.d(TAG, "UNDO engineCtrl.getCurrentPosition() before undo " + engineCtrl.getCurrentPosition());

		stateBeforeHint = engineCtrl.toByteArray();
		inverseColor();
	}

	public void undoHint() {

		// todo @compengine: use DroidFish hint way as soon as it will be implemented

		engineCtrl.newGame(this.gameMode, timeControlData, depth);
		engineCtrl.fromByteArray(stateBeforeHint, GAME_STATE_VERSION);
		// todo @compengine: check strength level of restarted game
		engineCtrl.startGame();
	}

	private void inverseColor() {
		int inversedColor = engineCtrl.getCurrentPosition().whiteMove ? GameMode.PLAYER_BLACK : GameMode.PLAYER_WHITE;
		setGameMode(inversedColor);
	}

	public void moveBack() {
		engineCtrl.undoMove();
	}

	public void moveForward() {
		engineCtrl.redoMove();
	}

	private final byte[] strToByteArr(String str) {
		if (str == null)
			return null;
		int nBytes = str.length() / 2;
		byte[] ret = new byte[nBytes];
		for (int i = 0; i < nBytes; i++) {
			int c1 = str.charAt(i * 2) - 'A';
			int c2 = str.charAt(i * 2 + 1) - 'A';
			ret[i] = (byte)(c1 * 16 + c2);
		}
		return ret;
	}

	public final String byteArrToString(byte[] data) {
		if (data == null)
			return null;
		StringBuilder ret = new StringBuilder(32768);
		int nBytes = data.length;
		for (int i = 0; i < nBytes; i++) {
			int b = data[i]; if (b < 0) b += 256;
			char c1 = (char)('A' + (b / 16));
			char c2 = (char)('A' + (b & 15));
			ret.append(c1);
			ret.append(c2);
		}
		return ret.toString();
	}

	public static int mapGameMode(int mode) {
		int engineMode = 0;
		switch (mode) {
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE:
				engineMode = GameMode.PLAYER_WHITE;
				break;
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK:
				engineMode = GameMode.PLAYER_BLACK;
				break;
			case AppConstants.GAME_MODE_HUMAN_VS_HUMAN:
				engineMode = GameMode.TWO_PLAYERS;
				break;
			case AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER:
				engineMode = GameMode.TWO_COMPUTERS;
				break;
		}

		return engineMode;
	}

	private TimeControlData initTimeControlData(int engineMaxTime) {
		//int timeControl = 5 * 60 * 1000;
	    int timeControl = Integer.MAX_VALUE;
		int movesPerSession = 60; // check
		int timeIncrement = 0;

		timeControlData = new TimeControlData();
		timeControlData.setTimeControl(timeControl, movesPerSession, timeIncrement, engineMaxTime);

		return timeControlData;
	}

	public boolean isInitialized() {
		return engineCtrl != null;
	}

	public byte[] toByteArray() {
		return engineCtrl.toByteArray();
	}

	public void setPaused(boolean paused) {
		engineCtrl.setGuiPaused(paused);
	}

	public void shutdownEngine() {
		engineCtrl.shutdownEngine();
	}

	public static void log(String message) {
		if (LOGGING_ON) {
			Log.d(TAG, message);
		}
	}
}
