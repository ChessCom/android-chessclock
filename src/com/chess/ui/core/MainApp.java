package com.chess.ui.core;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameItem;
import com.chess.model.GameListItem;
import com.chess.model.TacticItem;
import com.chess.ui.interfaces.BoardFace;
import com.chess.utilities.BitmapLoader;
import com.chess.utilities.SoundPlayer;
import com.mobclix.android.sdk.MobclixAdView;

import java.io.IOException;
import java.util.ArrayList;


public class MainApp extends Application {

	public static String APP_ID = "2427617054";

	private SharedPreferences sharedData;
	private SharedPreferences.Editor sharedDataEditor;

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
	private Bitmap[][] piecesBitmaps;
	private Bitmap boardBitmap;
	private ArrayList<GameListItem> gameListItems = new ArrayList<GameListItem>();
	private GameItem currentGame;

	private long gameId;
	private ArrayList<TacticItem> tacticsBatch;

	private TacticItem tactic;
	public int currentTacticProblem = 0;
	private Context context;
	private Resources resources;

	// TODO move to array resources
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
	
	public static final int P_ALPHA_ID 	= 0;
	public static final int P_BOOK_ID 	= 1;
	public static final int P_CASES_ID 	= 2;
	public static final int P_CLASSIC_ID = 3;
	public static final int P_CLUB_ID 	= 4;
	public static final int P_CONDAL_ID = 5;
	public static final int P_MAYA_ID 	= 6;
	public static final int P_MODERN_ID = 7;
	public static final int P_VINTAGE_ID = 8;

	private int[] alphaPiecesDrawableIds = new int[]{
			R.drawable.alpha_wp,
			R.drawable.alpha_wn,
			R.drawable.alpha_wb,
			R.drawable.alpha_wr,
			R.drawable.alpha_wq,
			R.drawable.alpha_wk,
			R.drawable.alpha_bp,
			R.drawable.alpha_bn,
			R.drawable.alpha_bb,
			R.drawable.alpha_br,
			R.drawable.alpha_bq,
			R.drawable.alpha_bk,
	};

	private int[] bookPiecesDrawableIds = new int[]{
			R.drawable.book_wp,
			R.drawable.book_wn,
			R.drawable.book_wb,
			R.drawable.book_wr,
			R.drawable.book_wq,
			R.drawable.book_wk,
			R.drawable.book_bp,
			R.drawable.book_bn,
			R.drawable.book_bb,
			R.drawable.book_br,
			R.drawable.book_bq,
			R.drawable.book_bk,
	};

	private int[] casesPiecesDrawableIds = new int[]{
			R.drawable.cases_wp,
			R.drawable.cases_wn,
			R.drawable.cases_wb,
			R.drawable.cases_wr,
			R.drawable.cases_wq,
			R.drawable.cases_wk,
			R.drawable.cases_bp,
			R.drawable.cases_bn,
			R.drawable.cases_bb,
			R.drawable.cases_br,
			R.drawable.cases_bq,
			R.drawable.cases_bk,
	};

	private int[] classicPiecesDrawableIds = new int[]{
			R.drawable.classic_wp,
			R.drawable.classic_wn,
			R.drawable.classic_wb,
			R.drawable.classic_wr,
			R.drawable.classic_wq,
			R.drawable.classic_wk,
			R.drawable.classic_bp,
			R.drawable.classic_bn,
			R.drawable.classic_bb,
			R.drawable.classic_br,
			R.drawable.classic_bq,
			R.drawable.classic_bk,
	};

	private int[] clubPiecesDrawableIds = new int[]{
			R.drawable.club_wp,
			R.drawable.club_wn,
			R.drawable.club_wb,
			R.drawable.club_wr,
			R.drawable.club_wq,
			R.drawable.club_wk,
			R.drawable.club_bp,
			R.drawable.club_bn,
			R.drawable.club_bb,
			R.drawable.club_br,
			R.drawable.club_bq,
			R.drawable.club_bk,
	};


	private int[] condalPiecesDrawableIds = new int[]{
			R.drawable.condal_wp,
			R.drawable.condal_wn,
			R.drawable.condal_wb,
			R.drawable.condal_wr,
			R.drawable.condal_wq,
			R.drawable.condal_wk,
			R.drawable.condal_bp,
			R.drawable.condal_bn,
			R.drawable.condal_bb,
			R.drawable.condal_br,
			R.drawable.condal_bq,
			R.drawable.condal_bk,
	};

	private int[] mayaPiecesDrawableIds = new int[]{
			R.drawable.maya_wp,
			R.drawable.maya_wn,
			R.drawable.maya_wb,
			R.drawable.maya_wr,
			R.drawable.maya_wq,
			R.drawable.maya_wk,
			R.drawable.maya_bp,
			R.drawable.maya_bn,
			R.drawable.maya_bb,
			R.drawable.maya_br,
			R.drawable.maya_bq,
			R.drawable.maya_bk,
	};

	private int[] modernPiecesDrawableIds = new int[]{
			R.drawable.modern_wp,
			R.drawable.modern_wn,
			R.drawable.modern_wb,
			R.drawable.modern_wr,
			R.drawable.modern_wq,
			R.drawable.modern_wk,
			R.drawable.modern_bp,
			R.drawable.modern_bn,
			R.drawable.modern_bb,
			R.drawable.modern_br,
			R.drawable.modern_bq,
			R.drawable.modern_bk,
	};

	private int[] vintagePiecesDrawableIds = new int[]{
			R.drawable.vintage_wp,
			R.drawable.vintage_wn,
			R.drawable.vintage_wb,
			R.drawable.vintage_wr,
			R.drawable.vintage_wq,
			R.drawable.vintage_wk,
			R.drawable.vintage_bp,
			R.drawable.vintage_bn,
			R.drawable.vintage_bb,
			R.drawable.vintage_br,
			R.drawable.vintage_bq,
			R.drawable.vintage_bk,
	};

    private CharSequence whitePlayerName;
    private CharSequence blackPlayerName;

	public void loadBoard(String boardName, View progressView) {
		context = this;
//		new BitmapLoaderTask(new BitmapLoadUpdateListener(progressView)).execute(boardName);
		boardBitmap = BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(boardName, "drawable", AppConstants.PACKAGE_NAME));
	}

//	private class BitmapLoadUpdateListener extends AbstractUpdateListener<Bitmap,String>{
//
//		public BitmapLoadUpdateListener(View progressView) {
//			super(context, progressView);
//		}
//
//		@Override
//		public Bitmap backgroundMethod(String params) {
//			return BitmapLoader.loadFromResource(getResources(), getResources().getIdentifier(params, "drawable", "com.chess"));
//		}
//
//		@Override
//		public void updateData(Bitmap returnedObj) {
//			super.updateData(returnedObj);
//			boardBitmap = returnedObj;
//		}
//
//	}

//	public void loadPieces(String piecesName, View progressView) {
////		new PiecesLoaderTask(new PiecesLoadUpdateListener(progressView)).equals(p);
//		resources = getResources();
////		piecesBitmaps = new Bitmap[2][6]; // TODO refactor with more native way
//		piecesBitmaps[0][0] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_wp", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[0][1] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_wn", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[0][2] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_wb", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[0][3] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_wr", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[0][4] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_wq", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[0][5] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_wk", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[1][0] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_bp", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[1][1] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_bn", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[1][2] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_bb", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[1][3] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_br", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[1][4] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_bq", "drawable", AppConstants.PACKAGE_NAME));
//		piecesBitmaps[1][5] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(piecesName + "_bk", "drawable", AppConstants.PACKAGE_NAME));
//
//	}

	
	private void setPieceBitmapFromArray(int[] drawableArray){
		piecesBitmaps = new Bitmap[2][6];
		resources = getResources();
		for(int j=0; j<6; j++){
			piecesBitmaps[0][j] = ((BitmapDrawable)resources.getDrawable(drawableArray[j])).getBitmap();
		}
		for(int j=0; j<6; j++){
			piecesBitmaps[1][j] = ((BitmapDrawable)resources.getDrawable(drawableArray[6+j])).getBitmap();
		}
	}
	
	public void loadPieces(int piecesSetId, View progressView) {
//		new PiecesLoaderTask(new PiecesLoadUpdateListener(progressView)).equals(p);
		switch (piecesSetId){
			case P_ALPHA_ID:
				setPieceBitmapFromArray(alphaPiecesDrawableIds);
				break;
			case P_BOOK_ID:
				setPieceBitmapFromArray(bookPiecesDrawableIds);
				break;
			case P_CASES_ID:
				setPieceBitmapFromArray(casesPiecesDrawableIds);
				break;
			case P_CLASSIC_ID:
				setPieceBitmapFromArray(classicPiecesDrawableIds);
				break;
			case P_CLUB_ID:
				setPieceBitmapFromArray(clubPiecesDrawableIds);
				break;
			case P_CONDAL_ID:
				setPieceBitmapFromArray(condalPiecesDrawableIds);
				break;
			case P_MAYA_ID:
				setPieceBitmapFromArray(mayaPiecesDrawableIds);
				break;
			case P_MODERN_ID:
				setPieceBitmapFromArray(modernPiecesDrawableIds);
				break;
			case P_VINTAGE_ID:
				setPieceBitmapFromArray(vintagePiecesDrawableIds);
				break;
		}
	}

    public CharSequence getWhitePlayerName() {
        whitePlayerName = getCurrentGame().values.get(AppConstants.WHITE_USERNAME) + "\n(" + getCurrentGame().values.get(GameItem.WHITE_RATING) + ")";
        return whitePlayerName;
    }

    public CharSequence getBlackPlayerName() {
        blackPlayerName = getCurrentGame().values.get(AppConstants.BLACK_USERNAME) + "\n(" + getCurrentGame().values.get(GameItem.BLACK_RATING) + ")";
        return blackPlayerName;
    }

	public String getUserName() {
		return getSharedData().getString(AppConstants.USERNAME, "");
	}

	private class PiecesLoadUpdateListener extends AbstractUpdateListener<Bitmap[][], String> {

		public PiecesLoadUpdateListener(View progressView) {
			super(context, progressView);
		}

		@Override
		public void updateData(Bitmap[][] returnedObj) {
			super.updateData(returnedObj);
			piecesBitmaps = returnedObj;
		}

	}
	/*public void onCreate()
		  {
			soundPlayer = new SoundPlayer(this);
		  }*/

	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	public void showDialog(Context ctx, String title, String message) {
		if (message == null || message.trim().equals("")) {
			return;
		}
		new AlertDialog.Builder(ctx)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create().show();
	}

	public LccHolder getLccHolder() {
		if (lccHolder == null) {
			try {
				String versionName = "";
				try {
					versionName = getPackageManager().getPackageInfo(AppConstants.PACKAGE_NAME, 0).versionName;
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

	public GameItem getCurrentGame() {
		return currentGame;
	}

	public long getCurrentGameId() {
		return Long.parseLong(currentGame.values.get(GameListItem.GAME_ID));
	}

	public void setCurrentGame(GameItem currentGame) {
		this.currentGame = currentGame;
	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public ArrayList<GameListItem> getGameListItems() {
		return gameListItems;
	}

	public Bitmap[][] getPiecesBitmaps() {
		return piecesBitmaps;
	}

	public SharedPreferences getSharedData() { // TODO eliminate, make deafult calls
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

	public TacticItem getTactic() {
		return tactic;
	}

	public void setTactic(TacticItem tactic) {
		this.tactic = tactic;
	}

	public ArrayList<TacticItem> getTacticsBatch() {
		return tacticsBatch;
	}

	public void setTacticsBatch(ArrayList<TacticItem> tacticsBatch) {
		this.tacticsBatch = tacticsBatch;
	}

	public static boolean isTacticsGameMode(int mode) {
		return mode == AppConstants.GAME_MODE_TACTICS;
	}

	public static boolean isTacticsGameMode(BoardFace boardFace) {
		return isTacticsGameMode(boardFace.getMode());
	}

	public static boolean isFinishedEchessGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS;
	}

	public static boolean isComputerGameMode(BoardFace boardFace) {
		final int mode = boardFace.getMode();
		return mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE || mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK
				|| mode == AppConstants.GAME_MODE_HUMAN_VS_HUMAN || mode == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	public static boolean isLiveOrEchessGameMode(int mode) {
		return mode == AppConstants.GAME_MODE_LIVE_OR_ECHESS;
	}

	public static boolean isLiveOrEchessGameMode(BoardFace boardFace) {
		return isLiveOrEchessGameMode(boardFace.getMode());
	}


	public static boolean isComputerVsComputerGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	public static boolean isComputerVsHumanGameMode(BoardFace boardFace) {
		final int mode = boardFace.getMode();
		return mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE
				|| mode == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}

	public static boolean isHumanVsHumanGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_HUMAN_VS_HUMAN;
	}

	public static boolean isComputerVsHumanWhiteGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
	}

	public static boolean isComputerVsHumanBlackGameMode(BoardFace boardFace) {
		return boardFace.getMode() == AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
	}


	public Intent getMembershipIntent(String param) {
		final String uri = "http://www." + LccHolder.HOST + AppConstants.LOGIN_HTML_ALS + sharedData.getString(AppConstants.USER_TOKEN, "") + "&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html" + param;
		return new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
	}

	public Intent getMembershipAndroidIntent() {
		return getMembershipIntent("?c=androidads");
	}

	public Intent getMembershipVideoIntent() {
		return getMembershipIntent("?c=androidvideos");
	}
}
