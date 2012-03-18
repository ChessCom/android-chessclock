package com.chess.core;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;
import com.chess.R;
import com.chess.core.interfaces.BoardFace;
import com.chess.lcc.android.LccHolder;
import com.chess.model.Game;
import com.chess.model.GameListElement;
import com.chess.model.Tactic;
import com.chess.utilities.BitmapLoader;
import com.chess.utilities.SoundPlayer;
import com.chess.views.BoardView;
import com.mobclix.android.sdk.MobclixAdView;

import java.io.IOException;
import java.util.ArrayList;


public class MainApp extends Application {

	public static String APP_ID = "2427617054";

	private SharedPreferences sharedData;
	private SharedPreferences.Editor sharedDataEditor;

	private TabHost tabHost;
	public static int loadPrev = 0;

	private LccHolder lccHolder;
	private boolean liveChess;
	private SoundPlayer soundPlayer;
	private MobclixAdView rectangleAdview;
	private MobclixAdView bannerAdview;
	private LinearLayout bannerAdviewWrapper;
	private boolean adviewPaused;
	private boolean networkChangedNotification;
	private boolean forceBannerAdOnFailedLoad;
	private boolean forceBannerAdFirstLoad;
	private boolean forceRectangleAd;
	private static Drawable backgroundImage;

	public boolean guest = false;
	public boolean noInternet = false;
	public boolean offline = false;
	public boolean acceptdraw = false;
	private Bitmap[][] piecesBitmap;
	private Bitmap boardBitmap;
	private ArrayList<GameListElement> gameListItems = new ArrayList<GameListElement>();
	private Game currentGame;
	private String gameId = "";
	private ArrayList<Tactic> tacticsBatch;

	private Tactic tactic;
	public int currentTacticProblem = 0;

	public int[] strength = {1000, 3000, 5000, 10000, 30000, 60000};
	public String[] res_boards = {"blue",
			"brown",
			"green",
			"grey",
			"marble",
			"red",
			"tan",
			"wood_light",
			"wood_dark"};
	public String[] res_pieces = {"alpha",
			"book",
			"cases",
			"classic",
			"club",
			"condal",
			"maya",
			"modern",
			"vintage"};

	public Bitmap capturedBB, capturedBN, capturedBP, capturedBQ, capturedBR,
			capturedWB, capturedWN, capturedWP, capturedWQ, capturedWR;

	public void LoadBoard(String b) {
		boardBitmap = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(b, "drawable", "com.chess"));
	}

	public void LoadPieces(String p) {
		piecesBitmap = new Bitmap[2][6];
		piecesBitmap[0][0] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_wp", "drawable", "com.chess"));
		piecesBitmap[0][1] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_wn", "drawable", "com.chess"));
		piecesBitmap[0][2] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_wb", "drawable", "com.chess"));
		piecesBitmap[0][3] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_wr", "drawable", "com.chess"));
		piecesBitmap[0][4] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_wq", "drawable", "com.chess"));
		piecesBitmap[0][5] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_wk", "drawable", "com.chess"));
		piecesBitmap[1][0] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_bp", "drawable", "com.chess"));
		piecesBitmap[1][1] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_bn", "drawable", "com.chess"));
		piecesBitmap[1][2] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_bb", "drawable", "com.chess"));
		piecesBitmap[1][3] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_br", "drawable", "com.chess"));
		piecesBitmap[1][4] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_bq", "drawable", "com.chess"));
		piecesBitmap[1][5] = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(p + "_bk", "drawable", "com.chess"));
	}

	public void loadCapturedPieces() {
		capturedBB = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_bb", "drawable", "com.chess"));
		capturedBN = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_bn", "drawable", "com.chess"));
		capturedBP = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_bp", "drawable", "com.chess"));
		capturedBQ = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_bq", "drawable", "com.chess"));
		capturedBR = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_br", "drawable", "com.chess"));
		capturedWB = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_wb", "drawable", "com.chess"));
		capturedWN = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_wn", "drawable", "com.chess"));
		capturedWP = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_wp", "drawable", "com.chess"));
		capturedWQ = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_wq", "drawable", "com.chess"));
		capturedWR = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier("captured_wr", "drawable", "com.chess"));
	}

	/*public void onCreate()
	  {
		soundPlayer = new SoundPlayer(this);
	  }*/

	public void ShowMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	public void ShowDialog(Context ctx, String title, String message) {
		if (message == null || message.trim().equals("")) {
			return;
		}
		new AlertDialog.Builder(ctx)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create().show();
	}

	public LccHolder getLccHolder() {
		if (lccHolder == null) {
			try {
				String versionName = "";
				try {
					versionName = getPackageManager().getPackageInfo("com.chess", 0).versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				lccHolder = LccHolder.getInstance(getAssets().open("chesscom.pkcs12"), versionName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//lccClient = lccHolder.getClient();
			lccHolder.getAndroid().setContext(this);
		}

		return lccHolder;
	}

	public boolean isLiveChess() {
		return liveChess;
	}

	public void setLiveChess(boolean liveChess) {
		LccHolder.LOG.info("LCCLOG: Set Live Chess mode to: " + liveChess);
		this.liveChess = liveChess;
	}

	public SoundPlayer getSoundPlayer() {
		if (soundPlayer == null) {
			soundPlayer = new SoundPlayer(this);
		}
		return soundPlayer;
	}

	public MobclixAdView getBannerAdview() {
		return bannerAdview;
	}

	public void setBannerAdview(MobclixAdView bannerAdview) {
		this.bannerAdview = bannerAdview;
	}

	public MobclixAdView getRectangleAdview() {  // TODO comment back
		return rectangleAdview;
	}

	public void setRectangleAdview(MobclixAdView rectangleAdview) {
		this.rectangleAdview = rectangleAdview;
	}


	public boolean isNetworkChangedNotification() {
		return networkChangedNotification;
	}

	public void setNetworkChangedNotification(boolean networkChangedNotification) {
		this.networkChangedNotification = networkChangedNotification;
	}

	public boolean isAdviewPaused() {
		return adviewPaused;
	}

	public void setAdviewPaused(boolean adviewPaused) {
		this.adviewPaused = adviewPaused;
	}

	public LinearLayout getBannerAdviewWrapper() {
		return bannerAdviewWrapper;
	}

	public void setBannerAdviewWrapper(LinearLayout bannerAdviewWrapper) {
		this.bannerAdviewWrapper = bannerAdviewWrapper;
	}

	public void setForceBannerAdOnFailedLoad(boolean forceBannerAdOnFailedLoad) {
		this.forceBannerAdOnFailedLoad = forceBannerAdOnFailedLoad;
	}

	public boolean isForceBannerAdOnFailedLoad() {
		return forceBannerAdOnFailedLoad;
	}

	public boolean isForceRectangleAd() {
		return forceRectangleAd;
	}

	public void setForceRectangleAd(boolean forceRectangleAd) {
		this.forceRectangleAd = forceRectangleAd;
	}

	public boolean isForceBannerAdFirstLoad() {
		return forceBannerAdFirstLoad;
	}

	public void setForceBannerAdFirstLoad(boolean forceBannerAdFirstLoad) {
		this.forceBannerAdFirstLoad = forceBannerAdFirstLoad;
	}

	public Bitmap getBoardBitmap() {
		return boardBitmap;
	}

	public Game getCurrentGame() {
		return currentGame;
	}

	public void setCurrentGame(Game currentGame) {
		this.currentGame = currentGame;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public ArrayList<GameListElement> getGameListItems() {
		return gameListItems;
	}

	public Bitmap[][] getPiecesBitmap() {
		return piecesBitmap;
	}

	public SharedPreferences getSharedData() {
		return sharedData;
	}

	public void setSharedData(SharedPreferences sharedData) {
		this.sharedData = sharedData;
	}

	public SharedPreferences.Editor getSharedDataEditor() {
		return sharedDataEditor;
	}

	public void setSharedDataEditor(SharedPreferences.Editor sharedDataEditor) {
		this.sharedDataEditor = sharedDataEditor;
	}

	public TabHost getTabHost() {
		return tabHost;
	}

	public void setTabHost(TabHost tabHost) {
		this.tabHost = tabHost;
	}

	public Tactic getTactic() {
		return tactic;
	}

	public void setTactic(Tactic tactic) {
		this.tactic = tactic;
	}

	public ArrayList<Tactic> getTacticsBatch() {
		return tacticsBatch;
	}

	public void setTacticsBatch(ArrayList<Tactic> tacticsBatch) {
		this.tacticsBatch = tacticsBatch;
	}

	public static boolean isTacticsGameMode(int mode) {
		return mode == AppConstants.GAME_MODE_TACTICS;
	}

	public static boolean isTacticsGameMode(BoardFace boardFace) {
		return isTacticsGameMode(boardFace.getMode());
	}

	// todo: should be removed
	public static boolean isTacticsGameMode(BoardView boardView) {
		return isTacticsGameMode(boardView.getBoard().mode);
	}

	public static boolean isFinishedEchessGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS;
	}

	// todo: should be removed
	public static boolean isFinishedEchessGameMode(BoardView boardView) {
		return boardView.getBoard().mode == AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS;
	}

	public static boolean isComputerGameMode(BoardView boardView) {
		final int mode = boardView.getBoard().mode;
		return mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE || mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK
				|| mode == AppConstants.GAME_MODE_HUMAN_VS_HUMAN || mode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
	}


	public static boolean isComputerGameMode(BoardFace boardFace) {
		final int mode = boardFace.getMode();
		return mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE || mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK
				|| mode == AppConstants.GAME_MODE_HUMAN_VS_HUMAN || mode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	public static boolean isLiveOrEchessGameMode(int mode) {
		return mode == AppConstants.GAME_MODE_LIVE_OR_ECHESS;
	}

	public static boolean isLiveOrEchessGameMode(BoardView boardView) {
		return isLiveOrEchessGameMode(boardView.getBoard().mode);
	}

	public static boolean isLiveOrEchessGameMode(BoardFace boardFace) {
		return isLiveOrEchessGameMode(boardFace.getMode());
	}


	public static boolean isComputerVsComputerGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	public static boolean isComputerVsComputerGameMode(BoardView boardView) {
		return boardView.getBoard().mode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	// todo: should be removed
	public static boolean isComputerVsHumanGameMode(BoardView boardView) {
		final int mode = boardView.getBoard().mode;
		return mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE
				|| mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public static boolean isComputerVsHumanGameMode(BoardFace boardFace) {
		final int mode = boardFace.getMode();
		return mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE
				|| mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public static boolean isHumanVsHumanGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_HUMAN_VS_HUMAN;
	}

	// todo: should be removed
	public static boolean isHumanVsHumanGameMode(BoardView boardView) {
		return boardView.getBoard().mode == AppConstants.GAME_MODE_HUMAN_VS_HUMAN;
	}

	public static boolean isComputerVsHumanWhiteGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
	}

	// todo: should be removed
	public static boolean isComputerVsHumanWhiteGameMode(BoardView boardView) {
		return boardView.getBoard().mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
	}

	public static boolean isComputerVsHumanBlackGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	// todo: should be removed
	public static boolean isComputerVsHumanBlackGameMode(BoardView boardView) {
		return boardView.getBoard().mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}
}
