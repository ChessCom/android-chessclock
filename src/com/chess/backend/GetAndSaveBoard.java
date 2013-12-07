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
import com.chess.backend.entity.api.themes.BoardSingleItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.backend.tasks.SaveImageToSdTask;
import com.chess.model.SelectionItem;
import com.chess.statics.AppData;
import com.chess.ui.activities.MainFragmentFaceActivity;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.10.13
 * Time: 16:39
 */
public class GetAndSaveBoard extends Service {

	public static final int INDETERMINATE = -1;
	public static final int DONE = -2;
	private static final long SHUTDOWN_DELAY = 4 * 1000;

	public static final int BOARD_SIZE_STEP = 8;
	public static final int BOARD_START_NAME = 20;
	public static final int BOARD_START_SIZE = 160;
	public static final int BOARD_END_NAME = 180;
	public static final int BOARD_END_SIZE = 1440;

	private NotificationManager notifymanager;
	private NotificationCompat.Builder notificationBuilder;
	private AppData appData;
	private BoardSingleItemUpdateListener boardSingleItemUpdateListener;
	private ImageSaveListener boardImgSaveListener;
	private String boardUrl;
	private ImageDownloaderToListener imageDownloader;
	private ImageUpdateListener boardUpdateListener;
	private int screenWidth;
	private ServiceBinder serviceBinder = new ServiceBinder();
	private FileReadyListener progressUpdateListener;
	private Handler handler;
	private boolean installingBoard;
	private SelectionItem selectedThemeBoardItem;
	private BoardSingleItem.Data boardData;

	@Override
	public void onCreate() {
		super.onCreate();

		handler = new Handler();
		notifymanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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


		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle(getString(R.string.loading_board))
				.setContentText(getString(R.string.loading_board))
				.setSmallIcon(R.drawable.ic_stat_download)
				.setAutoCancel(true);
		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);
	}

	public void setProgressUpdateListener(FileReadyListener progressUpdateListener) {
		this.progressUpdateListener = progressUpdateListener;
	}

	public boolean isInstallingBoard() {
		return installingBoard;
	}

	public class ServiceBinder extends Binder {
		public GetAndSaveBoard getService(){
			return GetAndSaveBoard.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	public void loadBoard(int selectedBoardId, SelectionItem selectedThemeBoardItem, int screenWidth) {
		this.selectedThemeBoardItem = selectedThemeBoardItem;
		installingBoard = true;

		this.screenWidth = screenWidth ;

		boardImgSaveListener = new ImageSaveListener();
		boardSingleItemUpdateListener = new BoardSingleItemUpdateListener();
		imageDownloader = new ImageDownloaderToListener(this);
		boardUpdateListener = new ImageUpdateListener();


		// start loading board
		LoadItem loadItem = LoadHelper.getBoardById(getUserToken(), selectedBoardId);
		new RequestJsonTask<BoardSingleItem>(boardSingleItemUpdateListener).executeTask(loadItem);
	}

	private class BoardSingleItemUpdateListener extends AbstractUpdateListener<BoardSingleItem> {

		private BoardSingleItemUpdateListener() {
			super(getContext(), BoardSingleItem.class);
		}

		@Override
		public void updateData(BoardSingleItem returnedObj) {

			boardData = returnedObj.getData();
			String coordinateColorLight = boardData.getCoordinateColorLight();
			String coordinateColorDark = boardData.getCoordinateColorDark();
			String highlightColor = boardData.getHighlightColor();

			getAppData().setThemeBoardCoordinateLight(Color.parseColor(coordinateColorLight));
			getAppData().setThemeBoardCoordinateDark(Color.parseColor(coordinateColorDark));
			getAppData().setThemeBoardHighlight(Color.parseColor(highlightColor));

			// get boards dir in s3
			String boardDir = boardData.getThemeDir();

			showIndeterminateNotification(getString(R.string.loading_board));

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

			boardUrl = BoardSingleItem.PATH + boardDir + "/" + name + ".png";

			// Start loading board image
			imageDownloader.download(boardUrl, boardUpdateListener, screenWidth);
		}
	}

	private class ImageUpdateListener implements ImageReadyListener {

		private int previousProgress;

		@Override
		public void onImageReady(Bitmap bitmap) {
			if (bitmap == null) {
				logTest("error loading image. Internal error");
				installingBoard = false;
				return;
			}

			notificationBuilder.setContentText(getString(R.string.saving_board));
			updateProgressToNotification(0);

			String filename = String.valueOf(boardUrl.hashCode()); // TODO rename to MD5
			new SaveImageToSdTask(boardImgSaveListener, bitmap).executeTask(filename);
		}

		@Override
		public void setProgress(final int progress) {
			if (previousProgress + 5 < progress) {
				previousProgress = progress;
				updateProgressToNotification(progress);
			}
		}
	}

	private class ImageSaveListener extends AbstractUpdateListener<Bitmap> {

		public ImageSaveListener() {
			super(getContext());
		}

		@Override
		public void updateData(Bitmap returnedObj) {

			// set board image as theme
			String filename = String.valueOf(boardUrl.hashCode());

			try {
				File imgFile = AppUtils.openFileByName(getContext(), filename);
				String drawablePath = imgFile.getAbsolutePath();

				// save board theme name to appData
				getAppData().setUseThemeBoard(true);
				getAppData().setThemeBoardPath(drawablePath);
				getAppData().setThemeBoardId(boardData.getThemeBoardId());
				getAppData().setThemeBoardName(boardData.getName());
				getAppData().setThemeBoardPreviewUrl(boardData.getPreviewUrl());

			} catch (IOException e) {
				e.printStackTrace();
			}

			showCompleteToNotification();
		}
	}

	private void showIndeterminateNotification(String title) {
		notificationBuilder.setContentText(title);
		notificationBuilder.setProgress(0, 0, true);
		// Displays the progress bar for the first time.
		notifymanager.notify(R.id.notification_message, notificationBuilder.build());

		if (progressUpdateListener != null) {
			progressUpdateListener.changeTitle(title);
			progressUpdateListener.setProgress(INDETERMINATE);
		}
	}

	private void updateProgressToNotification(int progress) {
		notificationBuilder.setProgress(100, progress, false);
		// Displays the progress bar for the first time.
		notifymanager.notify(R.id.notification_message, notificationBuilder.build());
		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(progress);
		}
	}

	private void showCompleteToNotification() {
		notificationBuilder.setContentText(getString(R.string.download_comlete))
				// Removes the progress bar
				.setProgress(0, 0, false);
		notifymanager.notify(R.id.notification_message, notificationBuilder.build());
		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(DONE);
		}

		installingBoard = false;

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				notifymanager.cancel(R.id.notification_message);

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
