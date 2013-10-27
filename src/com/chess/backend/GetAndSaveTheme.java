package com.chess.backend;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.chess.R;
import com.chess.backend.entity.api.themes.*;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.tasks.GetAndSaveFileToSdTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.backend.tasks.SaveImageToSdTask;
import com.chess.db.DbDataManager;
import com.chess.statics.AppData;
import com.chess.statics.IntentConstants;
import com.chess.statics.Symbol;
import com.chess.ui.activities.MainFragmentFaceActivity;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.SoundPlayer;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.10.13
 * Time: 8:24
 */
//public class GetAndSaveTheme extends IntentService {
public class GetAndSaveTheme extends Service {

	static final int BACKGROUND = 0;
	static final int BOARD = 1;
	public static final int INDETERMINATE = -1;
	public static final int DONE = -2;

	public static final String _3D_PART = "3d";
	public static final int BOARD_SIZE_STEP = 8;
	public static final int BOARD_START_NAME = 20;
	public static final int BOARD_START_SIZE = 160;
	public static final int BOARD_END_NAME = 180;
	public static final int BOARD_END_SIZE = 1440;
	private static final long SHUTDOWN_DELAY = 4 * 1000;

	private ThemeItem.Data selectedThemeItem;
	private int screenWidth;
	private int screenHeight;
	private String backgroundUrl;
	private ImageDownloaderToListener imageDownloader;
	private ImageUpdateListener backgroundUpdateListener;
	private ImageUpdateListener boardUpdateListener;
	private ImageSaveListener mainBackgroundImgSaveListener;
	private ImageSaveListener boardImgSaveListener;
	private AppData appData;
	private String boardUrl;
	private String selectedPieceDir;
	private SoundPackSaveListener soundPackSaveListener;
	private PiecesPackSaveListener piecesPackSaveListener;
	private String selectedSoundPackUrl;
	private BoardSingleItemUpdateListener boardSingleItemUpdateListener;
	private PiecesItemUpdateListener piecesItemUpdateListener;
	private SoundsItemUpdateListener soundsItemUpdateListener;
	private NotificationManager mNotifyManager;
	private NotificationCompat.Builder mBuilder;

	private ServiceBinder serviceBinder = new ServiceBinder();
	private FileReadyListener progressUpdateListener;
	private Handler handler;

	public void setProgressUpdateListener(FileReadyListener progressUpdateListener) {
		this.progressUpdateListener = progressUpdateListener;
	}

	public class ServiceBinder extends Binder {
		public GetAndSaveTheme getService(){
			return GetAndSaveTheme.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		handler = new Handler();
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Creates an Intent for the Activity
		Intent notifyIntent = new Intent(this, MainFragmentFaceActivity.class);
		// Sets the Activity to start in a new, empty task
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// Creates the PendingIntent
		PendingIntent pendingIntent = PendingIntent.getActivity(
						this,
						0,
						notifyIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);


		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle(getString(R.string.installing_theme))
				.setContentText(getString(R.string.loading_background))
				.setSmallIcon(R.drawable.ic_stat_download)
				.setAutoCancel(true);
		// Puts the PendingIntent into the notification builder
		mBuilder.setContentIntent(pendingIntent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY_COMPATIBILITY; // TODO add logic to stop service once it's not needed
	}

	public void loadTheme(ThemeItem.Data selectedThemeItem, int screenWidth, int screenHeight) {

		this.selectedThemeItem = selectedThemeItem;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		if (selectedThemeItem == null) {
			return;
		}

		backgroundUpdateListener = new ImageUpdateListener(BACKGROUND);
		boardUpdateListener = new ImageUpdateListener(BOARD);
		mainBackgroundImgSaveListener = new ImageSaveListener(BACKGROUND);
		boardImgSaveListener = new ImageSaveListener(BOARD);
		boardSingleItemUpdateListener = new BoardSingleItemUpdateListener();
		piecesItemUpdateListener = new PiecesItemUpdateListener();
		soundsItemUpdateListener = new SoundsItemUpdateListener();
		soundPackSaveListener = new SoundPackSaveListener();
		piecesPackSaveListener = new PiecesPackSaveListener();

		imageDownloader = new ImageDownloaderToListener(this);

		AppData appData = new AppData(this);
		String userToken = appData.getUserToken();

		String selectedThemeName = selectedThemeItem.getThemeName();
		appData.setThemeName(selectedThemeName);

		showIndeterminateNotification(getString(R.string.loading_background));

		LoadItem loadItem = LoadHelper.getBackgroundById(userToken, selectedThemeItem.getBackgroundId(),
				screenWidth, screenHeight, RestHelper.V_HANDSET);
		new RequestJsonTask<BackgroundSingleItem>(new BackgroundItemUpdateListener()).executeTask(loadItem);
	}

	private class BackgroundItemUpdateListener extends AbstractUpdateListener<BackgroundSingleItem> {

		private BackgroundItemUpdateListener() {
			super(getContext(), BackgroundSingleItem.class);
		}

		@Override
		public void updateData(BackgroundSingleItem returnedObj) {

			backgroundUrl = returnedObj.getData().getResizedImage();

			getAppData().setThemeBackgroundName(returnedObj.getData().getName());
			getAppData().setThemeBackgroundPreviewUrl(returnedObj.getData().getBackgroundPreviewUrl());

			showIndeterminateNotification(getString(R.string.loading_background));

			// Start loading background image
			imageDownloader.download(backgroundUrl, backgroundUpdateListener, screenWidth, screenHeight);
		}
	}

	private class BoardSingleItemUpdateListener extends AbstractUpdateListener<BoardSingleItem> {

		private BoardSingleItemUpdateListener() {
			super(getContext(), BoardSingleItem.class);
		}

		@Override
		public void updateData(BoardSingleItem returnedObj) {

			BoardSingleItem.Data boardData = returnedObj.getData();
			String coordinateColorLight = boardData.getCoordinateColorLight();
			String coordinateColorDark = boardData.getCoordinateColorDark();
			String highlightColor = boardData.getHighlightColor();

			getAppData().setUseThemeBoard(true);
			getAppData().setThemeBoardName(boardData.getName());
			getAppData().setThemeBoardPreviewUrl(boardData.getLineBoardPreviewUrl());

			getAppData().setThemeBoardCoordinateLight(Color.parseColor(coordinateColorLight));
			getAppData().setThemeBoardCoordinateDark(Color.parseColor(coordinateColorDark));
			getAppData().setThemeBoardHighlight(Color.parseColor(highlightColor));

			// get boards dir in s3
			String boardDir = boardData.getThemeDir();

			// we start to count pixels until we reach needed size for board
			int boardSize = BOARD_START_SIZE;
			int name;
			for (name = BOARD_START_NAME; name < BOARD_END_NAME; name++) {
				if (boardSize == screenWidth) { // 480 == 480

					break;
				}

//				// if we step over the range and missed needed size, than take the closest one
//				if (screenWidth > boardSize) {
//
//				}
				boardSize += BOARD_SIZE_STEP;
			}

			boardUrl = BoardsItem.PATH + boardDir + "/" + name + ".png";

			showIndeterminateNotification(getString(R.string.loading_board));

			// Start loading board image
			imageDownloader.download(boardUrl, boardUpdateListener, screenWidth);
		}
	}


	private class ImageUpdateListener implements ImageReadyListener {

		private int listenerCode;
		private int previousProgress;

		private ImageUpdateListener(int listenerCode) {
			this.listenerCode = listenerCode;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {

			if (bitmap == null) {
				logTest("error loading image. Internal error");
//				return;
			}

			if (listenerCode == BACKGROUND) {
				showIndeterminateNotification(getString(R.string.saving_background));

				String filename = String.valueOf(backgroundUrl.hashCode()); // TODO rename to MD5
				new SaveImageToSdTask(mainBackgroundImgSaveListener, bitmap).executeTask(filename);
			} else if (listenerCode == BOARD) {
				logTest("taskTitleTxt - " + getString(R.string.saving_board));
				showIndeterminateNotification(getString(R.string.saving_board));

				String filename = String.valueOf(boardUrl.hashCode()); // TODO rename to MD5
				new SaveImageToSdTask(boardImgSaveListener, bitmap).executeTask(filename);
			}
		}

		@Override
		public void setProgress(final int progress) {
			if (previousProgress + 5 < progress || progress == 100) {
				previousProgress = progress;
				updateProgressToNotification(progress);
			}
		}
	}

	private class ImageSaveListener extends AbstractUpdateListener<Bitmap> {

		private int listenerCode;

		public ImageSaveListener(int listenerCode) {
			super(getContext());
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Bitmap returnedObj) {

			if (listenerCode == BACKGROUND) {

				// set main background image as theme
				String filename = String.valueOf(backgroundUrl.hashCode());
				try {
					File imgFile = AppUtils.openFileByName(getContext(), filename);
					getAppData().setThemeBackPath(imgFile.getAbsolutePath());

					sendBroadcast(new Intent(IntentConstants.BACKGROUND_LOADED));
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Get board main path
				LoadItem loadItem = LoadHelper.getBoardById(getUserToken(), selectedThemeItem.getBoardId());
				new RequestJsonTask<BoardSingleItem>(boardSingleItemUpdateListener).executeTask(loadItem);

				mBuilder.setContentText(getString(R.string.loading_board));
				updateProgressToNotification(0);

			} else if (listenerCode == BOARD) {
				// set board image as theme
				String filename = String.valueOf(boardUrl.hashCode());
				try {
					File imgFile = AppUtils.openFileByName(getContext(), filename);
					String drawablePath = imgFile.getAbsolutePath();

					getAppData().setThemeBoardPath(drawablePath);
				} catch (IOException e) {
					e.printStackTrace();

				}

				// Get pieces main path on s3
				LoadItem loadItem = LoadHelper.getPiecesById(getUserToken(), selectedThemeItem.getPiecesId());
				new RequestJsonTask<PieceSingleItem>(piecesItemUpdateListener).executeTask(loadItem);
			}
		}
	}

	private class PiecesItemUpdateListener extends AbstractUpdateListener<PieceSingleItem> {

		private PiecesItemUpdateListener() {
			super(getContext(), PieceSingleItem.class);
		}

		@Override
		public void updateData(PieceSingleItem returnedObj) {

			getAppData().setThemePiecesName(returnedObj.getData().getName());
			getAppData().setThemePiecesPreviewUrl(returnedObj.getData().getPreviewUrl());

			// get pieces dir in s3
			selectedPieceDir = returnedObj.getData().getThemeDir();
			int pieceWidth = screenWidth / 8;

			String[] imagesToLoad = new String[12]; // 6 pieces for each side
			String[] whitePieceImageCodes = ChessBoard.whitePieceImageCodes;
			for (int i = 0; i < whitePieceImageCodes.length; i++) {
				String imageCode = whitePieceImageCodes[i];
				imagesToLoad[i] = PieceSingleItem.PATH + selectedPieceDir + "/" + pieceWidth + "/" + imageCode + ".png";
			}

			String[] blackPieceImageCodes = ChessBoard.blackPieceImageCodes;

			for (int i = 0; i < blackPieceImageCodes.length; i++) {
				String imageCode = blackPieceImageCodes[i];
				imagesToLoad[6 + i] = PieceSingleItem.PATH + selectedPieceDir + "/" + pieceWidth + "/" + imageCode + ".png";
			}

			showIndeterminateNotification(getString(R.string.loading_pieces));

			// Start loading pieces image
			new GetAndSaveFileToSdTask(piecesPackSaveListener, AppUtils.getLocalDirForPieces(getContext(), selectedPieceDir))
					.executeTask(imagesToLoad);
		}
	}

	private class SoundPackSaveListener extends AbstractUpdateListener<String> implements FileReadyListener {

		private int previousProgress;

		public SoundPackSaveListener() {
			super(getContext());
		}

		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);

			DbDataManager.saveSoundPathToDb(getContentResolver(), selectedSoundPackUrl, returnedObj);

			// save sounds path to settings
			getAppData().setThemeSoundPath(returnedObj);

			// update sounds flag
			SoundPlayer.setUseThemePack(true);
			SoundPlayer.setThemePath(returnedObj);

			// hide progress
			showCompleteToNotification();
		}

		@Override
		public void changeTitle(final String title) {
			logTest("changeTitle - " + title);

			showIndeterminateNotification(title);
		}

		@Override
		public void setProgress(final int progress) {
			if (previousProgress + 5 < progress) {
				previousProgress = progress;
				updateProgressToNotification(progress);
			}
		}
	}

	private class PiecesPackSaveListener extends AbstractUpdateListener<String> implements FileReadyListener {

		private int previousProgress;

		private PiecesPackSaveListener() {
			super(getContext());
			useList = true;
		}

		@Override
		public void updateListData(List<String> itemsList) {
			super.updateListData(itemsList);

			getAppData().setUseThemePieces(true);
			getAppData().setThemePiecesPath(selectedPieceDir);

			if (selectedPieceDir.contains(_3D_PART)) {
				getAppData().setThemePieces3d(true);
			} else {
				getAppData().setThemePieces3d(false);
			}

			// Get sounds zip url if id is valid
			if (selectedThemeItem.getSoundsId() != -1) {

				LoadItem loadItem = LoadHelper.getSoundsById(getUserToken(), selectedThemeItem.getSoundsId());
				new RequestJsonTask<SoundSingleItem>(soundsItemUpdateListener).executeTask(loadItem);
			} else {
				// hide progress
				showCompleteToNotification();

				// clear sounds theme
				SoundPlayer.setUseThemePack(false);
				SoundPlayer.setThemePath(Symbol.EMPTY);
			}
		}

		@Override
		public void changeTitle(final String title) {

			showIndeterminateNotification(title);
		}

		@Override
		public void setProgress(final int progress) {
			if (previousProgress + 5 < progress || progress == 100) {
				previousProgress = progress;
				updateProgressToNotification(progress);
			}
		}
	}

	private class SoundsItemUpdateListener extends AbstractUpdateListener<SoundSingleItem> {

		private SoundsItemUpdateListener() {
			super(getContext(), SoundSingleItem.class);
		}

		@Override
		public void updateData(SoundSingleItem returnedObj) {

			// get sounds dir in s3
			selectedSoundPackUrl = returnedObj.getData().getSoundPackZipUrl();

			new GetAndSaveFileToSdTask(soundPackSaveListener, true, AppUtils.getLocalDirForSounds(getContext()))
					.executeTask(selectedSoundPackUrl);
		}
	}

	private void showIndeterminateNotification(String title) {
		mBuilder.setContentText(title);
		mBuilder.setProgress(0, 0, true);
		// Displays the progress bar for the first time.
		mNotifyManager.notify(R.id.notification_message, mBuilder.build());

		if (progressUpdateListener != null) {
			progressUpdateListener.changeTitle(title);
			progressUpdateListener.setProgress(INDETERMINATE);
		}
	}

	private void updateProgressToNotification(int progress) {
		mBuilder.setProgress(100, progress, false);
		// Displays the progress bar for the first time.
		mNotifyManager.notify(R.id.notification_message, mBuilder.build());
		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(progress);
		}
	}

	private void showCompleteToNotification() {
		mBuilder.setContentText(getString(R.string.download_comlete))
				// Removes the progress bar
				.setProgress(0, 0, false);
		mNotifyManager.notify(R.id.notification_message, mBuilder.build());
		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(DONE);
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mNotifyManager.cancel(R.id.notification_message);

				stopSelf();
			}
		}, SHUTDOWN_DELAY);
	}

	private AppData getAppData() {
		if (appData == null) {
			appData = new AppData(this);
		}
		return appData;
	}

	private String getUserToken() {
		return getAppData().getUserToken();
	}

	private Context getContext() {
		return this;
	}

	private void logTest(String message) {
		Log.d("TEST", message);
	}
}
