/*
    DroidFish - An Android chess program.
    Copyright (C) 2011-2013  Peter Österlund, peterosterlund2@gmail.com
    Copyright (C) 2012 Leo Mayer

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.chess;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import org.petero.droidfish.*;
import org.petero.droidfish.book.BookOptions;
import org.petero.droidfish.gamelogic.*;
import org.petero.droidfish.gamelogic.GameTree.Node;
import org.petero.droidfish.gtb.Probe;

import java.io.File;
import java.util.ArrayList;

/*import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.*;
import android.util.TypedValue;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import org.petero.droidfish.*;
import org.petero.droidfish.ChessBoard.SquareDecoration;
import org.petero.droidfish.activities.*;
import org.petero.droidfish.book.BookOptions;
import org.petero.droidfish.engine.EngineUtil;
import org.petero.droidfish.gamelogic.*;
import org.petero.droidfish.gamelogic.GameTree.Node;
import org.petero.droidfish.gtb.Probe;
import java.io.*;
import java.util.*;*/

public class MyFish extends Activity implements GUIInterface {
	public static final String TAG = "FISHLOG";
	private static final String ENGINE_NAME = "stockfish";

    private ChessBoardPlay cb;
    private static DroidChessController ctrl = null;

    private boolean mWhiteBasedScores;

    private GameMode gameMode;
    private boolean mPonderMode;
    private int mEngineThreads;
    private String playerName;

    private final static String bookDir = "ComChess";
    private final static String pgnDir = "ComChess/pgn";
    private final static String fenDir = "ComChess/epd";
    private final static String engineDir = "ComChess/uci";
    private final static String gtbDefaultDir = "ComChess/gtb";
    private BookOptions bookOptions = new BookOptions();
    private PGNOptions pgnOptions = new PGNOptions();
    private EngineOptions engineOptions = new EngineOptions();

	private Context context;

	public DroidChessController init(Context context, int gameMode/*, String initialFen*/) {

		//long start = System.currentTimeMillis();

		this.context = context;

		PgnToken.PgnTokenReceiver pgnTokenReceiver = new PgnToken.PgnTokenReceiver() { // updates moves list
			@Override
			public boolean isUpToDate() {
				return false; // todo
			}

			@Override
			public void clear() {
			}

			@Override
			public void processToken(Node node, int type, String token) {
				/*Log.d(TAG, "PgnTokenReceiver processToken: node =" + node);
				Log.d(TAG, "PgnTokenReceiver processToken: type =" + type);
				Log.d(TAG, "PgnTokenReceiver processToken: token =" + token);*/
			}

			@Override
			public void setCurrent(Node node) {
				Log.d(TAG, "PgnTokenReceiver setCurrent: node =" + node);
			}
		};

		createDirectories();

		//gameTextListener = new PgnScreenText(pgnOptions);
		if (ctrl != null)
			ctrl.shutdownEngine();

		ctrl = new DroidChessController(this, pgnTokenReceiver, pgnOptions);
		egtbForceReload = true;
		readPrefs();

		this.gameMode = new GameMode(gameMode);
		ctrl.newGame(this.gameMode, new TimeControlData(), 0); // TODO: check timecontrol
		/*{
			byte[] data = null;
			if (savedInstanceState != null) {
				data = savedInstanceState.getByteArray("gameState");
			} else {
				String dataStr = settings.getString("gameState", null);
				if (dataStr != null)
					data = strToByteArr(dataStr);
			}
			if (data != null)
				ctrl.fromByteArray(data);
		}*/
		ctrl.setGuiPaused(true);
		ctrl.setGuiPaused(false);
		ctrl.startGame();
		/*if (intentPgnOrFen != null) {
			try {
				ctrl.setFENOrPGN(intentPgnOrFen);
				setBoardFlip(true);
			} catch (ChessParseError e) {
				// If FEN corresponds to illegal chess position, go into edit board mode.
				try {
					TextIO.readFEN(intentPgnOrFen);
				} catch (ChessParseError e2) {
					*//*if (e2.pos != null)
						startEditBoard(intentPgnOrFen);*//*
				}
			}
		} else if (intentFilename != null) {
			loadPGNFromFile(intentFilename);
		}*/

		/*try {
			ctrl.setFENOrPGN(initialFen);
			//setBoardFlip(true); // ?
		} catch (ChessParseError chessParseError) {
			Log.d(TAG, "setFENOrPGN: w =" + chessParseError);
			chessParseError.printStackTrace();
		}*/

		//long init = System.currentTimeMillis() - start;
		//Log.d(TAG, "INIT " + init);

		//makeMove(12, 20, Piece.EMPTY);

		return ctrl;
	}

	public void makeMove(int from, int to, int promoteTo) {
		Move move = new Move(from, to, promoteTo);
		ctrl.makeHumanMove(move);
	}





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


		/*Thread workThread = new Thread(new Runnable() {
			public void run() {*/
				init(getApplicationContext(), GameMode.PLAYER_BLACK);
			/*}
		});
		workThread.start();*/

    }

    /** Create directory structure on SD card. */
    private final static void createDirectories() {
        File extDir = Environment.getExternalStorageDirectory();

		Log.d(TAG, "createDirectories extDir=" + extDir);

        String sep = File.separator;
        boolean result = new File(extDir + sep + bookDir).mkdirs();
		Log.d(TAG, "createDirectories result=" + result);
		result = new File(extDir + sep + pgnDir).mkdirs();
		Log.d(TAG, "createDirectories result=" + result);
		result = new File(extDir + sep + fenDir).mkdirs();
		Log.d(TAG, "createDirectories result=" + result);
		result = new File(extDir + sep + engineDir).mkdirs();
		Log.d(TAG, "createDirectories result=" + result);
		result = new File(extDir + sep + gtbDefaultDir).mkdirs();
		Log.d(TAG, "createDirectories result=" + result);
    }

    @Override
    protected void onResume() {
        if (ctrl != null) {
            ctrl.setGuiPaused(false);
		}
        super.onResume();

		//makeMove(12, 20, Piece.EMPTY);
    }

    @Override
    protected void onPause() {
        if (ctrl != null) {
            //ctrl.setGuiPaused(true);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (ctrl != null)
            ctrl.shutdownEngine();
        super.onDestroy();
    }

    private final void readPrefs() {
        playerName = "Player";

        mWhiteBasedScores = false;

        mEngineThreads = 1;

        int strength = 1000;
        setEngineStrength(ENGINE_NAME, strength);

        mPonderMode = false;
        if (!mPonderMode)
            ctrl.stopPonder();

        int timeControl = 120000;
        int movesPerSession = 60;
        int timeIncrement = 0;
        //ctrl.setTimeLimit(timeControl, movesPerSession, timeIncrement);

        bookOptions.filename = "";
        bookOptions.maxLength = 1000000;
        bookOptions.preferMainLines = false;
        bookOptions.tournamentMode = false;
        bookOptions.random = (500 - 500) * (3.0 / 500); // check
        setBookOptions();

        engineOptions.hashMB = 16;
        engineOptions.hints = false;
        engineOptions.hintsEdit = false;
        engineOptions.rootProbe = true;
        engineOptions.engineProbe = true;
        String gtbPath = "";
        if (gtbPath.length() == 0) {
            File extDir = Environment.getExternalStorageDirectory();
            String sep = File.separator;
            gtbPath = extDir.getAbsolutePath() + sep + gtbDefaultDir;
        }
        engineOptions.gtbPath = gtbPath;
        setEngineOptions(false);

        /*pgnOptions.view.variations  = true;
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
        pgnOptions.exp.clockInfo    = false;*/
    }

    private final void setEngineStrength(String engine, int strength) {
        ctrl.setEngineStrength(engine, strength);
    }

    @Override
    public void updateEngineTitle() {
    }

    @Override
    public void updateMaterialDifferenceTitle(Util.MaterialDiff diff) {
    }

	@Override
	public void updateTimeControlTitle() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private final void setBookOptions() {
        BookOptions options = new BookOptions(bookOptions);
        if (options.filename.length() > 0) {
            File extDir = Environment.getExternalStorageDirectory();
            String sep = File.separator;
            options.filename = extDir.getAbsolutePath() + sep + bookDir + sep + options.filename;
        }
        ctrl.setBookOptions(options);
    }

    private boolean egtbForceReload = false;

    private final void setEngineOptions(boolean restart) {
        ctrl.setEngineOptions(new EngineOptions(engineOptions), restart);
        Probe.getInstance().setPath(engineOptions.gtbPath, egtbForceReload);
        egtbForceReload = false;
    }

    @Override
    public void setSelection(int sq) {
    }

    @Override
    public void setStatus(GameStatus s) {
		Log.d(TAG, "setStatus - gameStatus =" + s);
    }

    @Override
    public void moveListUpdated() {
		//Log.d(TAG, "moveListUpdated: gameTextListener.getSpannableData() =" + gameTextListener.getSpannableData());
    }

    @Override
    public boolean whiteBasedScores() {
        return mWhiteBasedScores;
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
        return playerName;
    }

    @Override
    public boolean discardVariations() {
        return false;
    }

    /** Report a move made that is a candidate for GUI animation. */
    @Override
	public void setAnimMove(Position sourcePos, Move move, boolean forward) {
		Log.d(TAG, "setAnimMove: sourcePos =\n" + sourcePos);
		Log.d(TAG, "setAnimMove: move =" + move);
		Log.d(TAG, "setAnimMove: forward =" + forward);
    }

    @Override
    public void setPosition(Position pos, String variantInfo, ArrayList<Move> variantMoves) {
		Log.d(TAG, "setPosition - pos =" + pos);
		Log.d(TAG, "setPosition - variantInfo =" + variantInfo);
		Log.d(TAG, "setPosition - variantMoves =" + variantMoves.toString());
	}

    @Override
    public void setThinkingInfo(String pvStr, String statStr, String bookInfo,
                                ArrayList<ArrayList<Move>> pvMoves, ArrayList<Move> bookMoves) {

		/*Log.d(TAG, "setThinkingInfo - pvStr =" + pvStr);
		Log.d(TAG, "setThinkingInfo - statStr =" + statStr);
		Log.d(TAG, "setThinkingInfo - bookInfo =" + bookInfo);
		Log.d(TAG, "setThinkingInfo - pvMoves =" + pvMoves);
		Log.d(TAG, "setThinkingInfo - bookMoves =" + bookMoves);*/

        /*thinkingStr1 = pvStr;
        thinkingStr2 = statStr;
        bookInfoStr = bookInfo;
        this.pvMoves = pvMoves;
        this.bookMoves = bookMoves;
        updateThinkingInfo();

        if (ctrl.computerBusy()) {
            lastComputationMillis = System.currentTimeMillis();
        } else {
            lastComputationMillis = 0;
        }
        updateNotification();*/
    }

    private final void startNewGame(int type) {
        if (type != 2) {
            /*int gameModeType = (type == 0) ? GameMode.PLAYER_WHITE : GameMode.PLAYER_BLACK;
            Editor editor = settings.edit();
            String gameModeStr = String.format(Locale.US, "%d", gameModeType);
            editor.putString("gameMode", gameModeStr);
            editor.commit();*/
            //gameMode = new GameMode(gameModeType);
        }
//        savePGNToFile(".autosave.pgn", true);
        ctrl.newGame(gameMode, new TimeControlData(), 0);
        ctrl.startGame();
        //setBoardFlip(true);
    }

    @Override
    public void requestPromotePiece() {
    }

    @Override
    public void reportInvalidMove(Move m) {
		Log.d(TAG, "reportInvalidMove: move =" + m);
    }

    @Override
    public void reportEngineName(String engine) {
		Log.d(TAG, "reportEngineName: engine =" + engine);
    }

    @Override
    public void reportEngineError(String errMsg) {

		try {
			throw new Exception(errMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.d(TAG, "reportEngineError: errMsg =" + errMsg);
    }

    @Override
    public void computerMoveMade() {
		Log.d(TAG, "computerMoveMade");
    }

	@Override
	public void setRemainingTime(int wTime, int bTime, int nextUpdate) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
    public void runOnUIThread(Runnable runnable) {
		runOnUiThread(runnable);
    }
}
