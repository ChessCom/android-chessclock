package com.chess.backend;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
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
import com.chess.statics.Symbol;
import com.chess.ui.activities.MainFragmentFaceActivity;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.SoundPlayer;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.10.13
 * Time: 8:24
 */
public class GetAndSaveTheme extends Service {

	public static final int BACKGROUND_PORT = 0;
	public static final int BACKGROUND_LAND = 2;

	static final int BOARD = 1;

	public static final int INDETERMINATE = -1;
	public static final int DONE = -2;
	private static final long SHUTDOWN_DELAY = 4 * 1000;

	public static final int BOARD_SIZE_STEP = 8;
	public static final int BOARD_START_NAME = 20;
	public static final int BOARD_START_SIZE = 160;
	public static final int BOARD_END_NAME = 180;
	public static final int BOARD_END_SIZE = 1440;

	private ThemeItem.Data selectedThemeItem;
	private int screenWidth;
	private int screenHeight;
	private int backgroundWidth;
	private int backgroundHeight;
	private String backgroundUrlPort;
	private String backgroundUrlLand;
	private ImageDownloaderToListener imageDownloader;
	private ImageUpdateListener backgroundUpdateListener;
	private ImageUpdateListener backgroundLandUpdateListener;
	private ImageUpdateListener boardUpdateListener;
	private ImageSaveListener mainBackgroundImgSaveListener;
	private ImageSaveListener mainBackgroundLandImgSaveListener;
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
	private NotificationManager notifyManager;
	private NotificationCompat.Builder notificationBuilder;

	private ServiceBinder serviceBinder = new ServiceBinder();
	private FileReadyListener progressUpdateListener;
	private Handler handler;
	private boolean installingTheme;
	private boolean isTablet;
	private HashMap<ThemeItem.Data, ThemeState> themesQueue;
	private BackgroundSingleItem.Data backgroundData;
	private BoardSingleItem.Data boardData;
	private PieceSingleItem.Data piecesData;
	private SoundSingleItem.Data soundsData;
	private String userToken;

	public class ServiceBinder extends Binder {
		public GetAndSaveTheme getService(){
			return GetAndSaveTheme.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		isTablet = AppUtils.isTablet(this);

		themesQueue = new HashMap<ThemeItem.Data, ThemeState>();
		appData = new AppData(this);
		handler = new Handler();
		notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Creates an Intent for the Activity
		Intent notifyIntent = new Intent(this, MainFragmentFaceActivity.class);
		// Sets the Activity to start in a new, empty task
		if (AppUtils.HONEYCOMB_PLUS_API) {
			setFlagsForNotifyIntent(notifyIntent);
		} else {
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		// Creates the PendingIntent
		PendingIntent pendingIntent = PendingIntent.getActivity(
						this,
						0,
						notifyIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);

		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle(getString(R.string.installing_theme))
				.setTicker(getString(R.string.installing_theme))
				.setContentText(getString(R.string.loading_background))
				.setSmallIcon(R.drawable.ic_stat_download)
				.setAutoCancel(true);
		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);

		backgroundUpdateListener = new ImageUpdateListener(BACKGROUND_PORT);
		backgroundLandUpdateListener = new ImageUpdateListener(BACKGROUND_LAND);
		boardUpdateListener = new ImageUpdateListener(BOARD);
		mainBackgroundImgSaveListener = new ImageSaveListener(BACKGROUND_PORT);
		mainBackgroundLandImgSaveListener = new ImageSaveListener(BACKGROUND_LAND);
		boardImgSaveListener = new ImageSaveListener(BOARD);
		boardSingleItemUpdateListener = new BoardSingleItemUpdateListener();
		piecesItemUpdateListener = new PiecesItemUpdateListener();
		soundsItemUpdateListener = new SoundsItemUpdateListener();
		soundPackSaveListener = new SoundPackSaveListener();
		piecesPackSaveListener = new PiecesPackSaveListener();

		imageDownloader = new ImageDownloaderToListener(this);

		userToken = appData.getUserToken();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setFlagsForNotifyIntent(Intent notifyIntent) {
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
		if (installingTheme) { // Enqueue load if we already loading theme
			themesQueue.put(selectedThemeItem, ThemeState.ENQUIRED);
			DbDataManager.updateThemeLoadingStatus(getContentResolver(), selectedThemeItem,	ThemeState.ENQUIRED);
			return;
		}

		DbDataManager.updateThemeLoadingStatus(getContentResolver(), selectedThemeItem, ThemeState.LOADING);
		themesQueue.put(selectedThemeItem, ThemeState.LOADING);

		installingTheme = true;

		this.selectedThemeItem = selectedThemeItem;

		Log.d("TEST", "screenWidth = " + screenWidth);
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		if (selectedThemeItem == null) {
			return;
		}

		showIndeterminateNotification(getString(R.string.loading_background));

		LoadItem loadItem;
		if (!isTablet) {
			loadItem = LoadHelper.getBackgroundById(userToken, selectedThemeItem.getBackgroundId(),
					screenWidth, screenHeight, RestHelper.V_HANDSET);
		} else {
			if (screenWidth > screenHeight) {
				backgroundWidth = screenHeight;
				backgroundHeight = screenWidth;
			} else {
				backgroundWidth = screenWidth;
				backgroundHeight = screenHeight;
			}

			// we need to download port and landscape backgrounds for tablets
			loadItem = LoadHelper.getBackgroundById(userToken, selectedThemeItem.getBackgroundId(),
					backgroundWidth, backgroundHeight, RestHelper.V_HANDSET);
		}

		new RequestJsonTask<BackgroundSingleItem>(new BackgroundItemUpdateListener()).executeTask(loadItem);
	}

	private class BackgroundItemUpdateListener extends AbstractUpdateListener<BackgroundSingleItem> {

		private int code;

		private BackgroundItemUpdateListener() {
			super(getContext(), BackgroundSingleItem.class);
			this.code = BACKGROUND_PORT;
		}

		private BackgroundItemUpdateListener(int code) {
			super(getContext(), BackgroundSingleItem.class);
			this.code = code;
		}

		@Override
		public void updateData(BackgroundSingleItem returnedObj) {

			backgroundData = returnedObj.getData();

			showIndeterminateNotification(getString(R.string.loading_background));

			// Start loading background image
			if (code == BACKGROUND_PORT) {
				backgroundUrlPort = backgroundData.getResizedImage();
				imageDownloader.download(backgroundUrlPort, backgroundUpdateListener, backgroundWidth, backgroundHeight);
			} else { // LAND
				backgroundWidth = screenWidth;
				backgroundHeight = screenHeight;

				// 800 < 1280
				if (screenWidth < screenHeight) {
					backgroundHeight = screenWidth;
					backgroundWidth = screenHeight;
				}
				backgroundUrlLand = backgroundData.getResizedImage();
				imageDownloader.download(backgroundUrlLand, backgroundLandUpdateListener, backgroundWidth, backgroundHeight);
			}

			// load second image for tablet
			if (isTablet && code == BACKGROUND_PORT) {
				backgroundWidth = screenWidth;
				backgroundHeight = screenHeight;

				// 800 < 1280   // for landscape
				if (screenWidth < screenHeight) {
					backgroundHeight = screenWidth;
					backgroundWidth = screenHeight;
				}

				LoadItem loadItem = LoadHelper.getBackgroundById(userToken, selectedThemeItem.getBackgroundId(),
						backgroundWidth, backgroundHeight, RestHelper.V_TABLET);
				new RequestJsonTask<BackgroundSingleItem>(new BackgroundItemUpdateListener(BACKGROUND_LAND)).executeTask(loadItem);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			dropThemeLoadingState();
		}
	}

	private class BoardSingleItemUpdateListener extends AbstractUpdateListener<BoardSingleItem> {

		private BoardSingleItemUpdateListener() {
			super(getContext(), BoardSingleItem.class);
		}

		@Override
		public void updateData(BoardSingleItem returnedObj) {

			boardData = returnedObj.getData();

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
//				}
				boardSize += BOARD_SIZE_STEP;
			}

			boardUrl = BoardSingleItem.PATH + boardDir + "/" + name + ".png";

			showIndeterminateNotification(getString(R.string.loading_board));

			// Start loading board image
			imageDownloader.download(boardUrl, boardUpdateListener, screenWidth);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			dropThemeLoadingState();
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
				installingTheme = false;

				showIndeterminateNotification("Error loading image");
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						showCompleteToNotification();
					}
				}, SHUTDOWN_DELAY);
				return;
			}

			if (listenerCode == BACKGROUND_PORT) {
				showIndeterminateNotification(getString(R.string.saving_background));

				String filename = String.valueOf(backgroundUrlPort.hashCode()); // TODO rename to MD5
				new SaveImageToSdTask(mainBackgroundImgSaveListener, bitmap).executeTask(filename);
			} else if (listenerCode == BACKGROUND_LAND) {
				showIndeterminateNotification(getString(R.string.saving_background));

				String filename = String.valueOf(backgroundUrlLand.hashCode()); // TODO rename to MD5
				new SaveImageToSdTask(mainBackgroundLandImgSaveListener, bitmap).executeTask(filename);
			} else if (listenerCode == BOARD) {
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

			if (listenerCode == BACKGROUND_PORT) {

				// set main background image as theme
				String filename = String.valueOf(backgroundUrlPort.hashCode());
				try {
					File imgFile = AppUtils.openFileByName(getContext(), filename);

					backgroundData.setLocalPathPort(imgFile.getAbsolutePath());
					backgroundData.setBackgroundId(selectedThemeItem.getBackgroundId());
					DbDataManager.saveThemeBackgroundItemToDb(getContentResolver(), backgroundData);

				} catch (IOException e) {
					e.printStackTrace();
				}

				// Get board main path
				LoadItem loadItem = LoadHelper.getBoardById(getUserToken(), selectedThemeItem.getBoardId());
				new RequestJsonTask<BoardSingleItem>(boardSingleItemUpdateListener).executeTask(loadItem);

				notificationBuilder.setContentText(getString(R.string.loading_board));
				updateProgressToNotification(0);

			} else if (listenerCode == BACKGROUND_LAND) {
				// set main background image as theme
				String filename = String.valueOf(backgroundUrlLand.hashCode());
				try {
					File imgFile = AppUtils.openFileByName(getContext(), filename);

					backgroundData.setLocalPathLand(imgFile.getAbsolutePath());
					backgroundData.setBackgroundId(selectedThemeItem.getBackgroundId());
					DbDataManager.saveThemeBackgroundItemToDb(getContentResolver(), backgroundData);

				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (listenerCode == BOARD) {
				// set board image as theme
				String filename = String.valueOf(boardUrl.hashCode());
				try {
					File imgFile = AppUtils.openFileByName(getContext(), filename);
					String drawablePath = imgFile.getAbsolutePath();
					boardData.setLocalPath(drawablePath);

					DbDataManager.saveThemeBoardItemToDb(getContentResolver(), boardData);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Get pieces main path on s3
				LoadItem loadItem = LoadHelper.getPiecesById(getUserToken(), selectedThemeItem.getPiecesId());
				new RequestJsonTask<PieceSingleItem>(piecesItemUpdateListener).executeTask(loadItem);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			dropThemeLoadingState();
		}

	}

	private class PiecesItemUpdateListener extends AbstractUpdateListener<PieceSingleItem> {

		private PiecesItemUpdateListener() {
			super(getContext(), PieceSingleItem.class);
		}

		@Override
		public void updateData(PieceSingleItem returnedObj) {

			piecesData = returnedObj.getData();

			// get pieces dir in s3
			selectedPieceDir = piecesData.getThemeDir();
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

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			dropThemeLoadingState();
		}

	}

	private class SoundPackSaveListener extends AbstractUpdateListener<String> implements FileReadyListener {

		private int previousProgress;

		public SoundPackSaveListener() {
			super(getContext());
		}

		@Override
		public void updateData(String filePath) {
			super.updateData(filePath);

			soundsData.setLocalPath(filePath);
			DbDataManager.saveThemeSoundsItemToDb(getContentResolver(), soundsData);
			DbDataManager.saveSoundPathToDb(getContentResolver(), selectedSoundPackUrl, filePath);

			// hide progress
			showCompleteToNotification();
		}

		@Override
		public void changeTitle(final String title) {
			showIndeterminateNotification(title);
		}

		@Override
		public void setProgress(final int progress) {
			if (progress == -1) {
				showIndeterminateProgress();
			} else if (previousProgress + 5 < progress) {
				previousProgress = progress;
				updateProgressToNotification(progress);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			dropThemeLoadingState();
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

			piecesData.setLocalPath(selectedPieceDir);
			DbDataManager.saveThemePieceItemToDb(getContentResolver(), piecesData);

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
			if (progress == -1) {
				showIndeterminateProgress();
			} else if (previousProgress + 5 < progress || progress == 100) {
				previousProgress = progress;
				updateProgressToNotification(progress);
				if (progress == 100 || progress == 99) {
					previousProgress = 0;
				}
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			dropThemeLoadingState();
		}

	}

	private class SoundsItemUpdateListener extends AbstractUpdateListener<SoundSingleItem> {

		private SoundsItemUpdateListener() {
			super(getContext(), SoundSingleItem.class);
		}

		@Override
		public void updateData(SoundSingleItem returnedObj) {
			soundsData = returnedObj.getData();
			DbDataManager.saveThemeSoundsItemToDb(getContentResolver(), soundsData);

			// get sounds dir in s3
			selectedSoundPackUrl = soundsData.getSoundPackZipUrl();

			new GetAndSaveFileToSdTask(soundPackSaveListener, true, AppUtils.getLocalDirForSounds(getContext()))
					.executeTask(selectedSoundPackUrl);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			dropThemeLoadingState();
		}
	}

	private void showIndeterminateNotification(String title) {
		notificationBuilder.setContentText(title);
		notificationBuilder.setProgress(0, 0, true);
		// Displays the progress bar for the first time.
		notifyManager.notify(R.id.notification_id, notificationBuilder.build());

		if (progressUpdateListener != null) {
			progressUpdateListener.changeTitle(title);
			progressUpdateListener.setProgress(INDETERMINATE);
		}
	}


	private void showIndeterminateProgress() {
		notificationBuilder.setProgress(0, 0, true);
		// Displays the progress bar for the first time.
		notifyManager.notify(R.id.notification_id, notificationBuilder.build());

		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(INDETERMINATE);
		}
	}

	private void updateProgressToNotification(int progress) {
		notificationBuilder.setProgress(100, progress, false);
		// Displays the progress bar for the first time.
		notifyManager.notify(R.id.notification_id, notificationBuilder.build());
		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(progress);
		}
	}

	private void showCompleteToNotification() {
		notificationBuilder.setContentText(getString(R.string.download_complete))
				// Removes the progress bar
				.setProgress(0, 0, false);
		notifyManager.notify(R.id.notification_id, notificationBuilder.build());

		// mark item as loaded
		themesQueue.put(selectedThemeItem, ThemeState.LOADED);
		DbDataManager.updateThemeLoadingStatus(getContentResolver(), selectedThemeItem, ThemeState.LOADED);

		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(DONE);
		}

		installingTheme = false;

		// load next theme from queue
		for (Map.Entry<ThemeItem.Data, ThemeState> entry : themesQueue.entrySet()) {
			ThemeState status = entry.getValue();
			if (status.equals(ThemeState.ENQUIRED)) {
				loadTheme(entry.getKey(), screenWidth, screenHeight);
				return;
			}
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				notifyManager.cancel(R.id.notification_id);

			}
		}, SHUTDOWN_DELAY);
	}

	private void dropThemeLoadingState() {
		notificationBuilder.setContentText(getString(R.string.error))
				// Removes the progress bar
				.setProgress(0, 0, false);
		notifyManager.notify(R.id.notification_id, notificationBuilder.build());

		// mark item as loaded
		themesQueue.put(selectedThemeItem, ThemeState.DEFAULT);
		DbDataManager.updateThemeLoadingStatus(getContentResolver(), selectedThemeItem, ThemeState.DEFAULT);

		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(DONE);
		}

		installingTheme = false;

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				notifyManager.cancel(R.id.notification_id);

			}
		}, SHUTDOWN_DELAY);
	}

	public void setProgressUpdateListener(FileReadyListener progressUpdateListener) {
		this.progressUpdateListener = progressUpdateListener;
	}

	public ThemeItem.Data getLoadingTheme() {
		return selectedThemeItem;
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
