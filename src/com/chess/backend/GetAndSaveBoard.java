package com.chess.backend;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.chess.R;
import com.chess.backend.entity.api.themes.BoardSingleItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListener;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.backend.tasks.SaveImageToSdTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
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
	public static final int BOARD_END_NAME = 200;
	public static final int BOARD_END_SIZE = 1600;

	private NotificationManager notifyManager;
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
	private BoardSingleItem.Data boardData;

	@Override
	public void onCreate() {
		super.onCreate();

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


		String downloadingBoardStr = getString(R.string.downloading_arg, getString(R.string.board));
		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle(downloadingBoardStr)
				.setTicker(downloadingBoardStr)
				.setContentText(downloadingBoardStr)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setAutoCancel(true);
		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);

		boardImgSaveListener = new ImageSaveListener();
		boardSingleItemUpdateListener = new BoardSingleItemUpdateListener();
		imageDownloader = new ImageDownloaderToListener(this);
		boardUpdateListener = new ImageUpdateListener();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setFlagsForNotifyIntent(Intent notifyIntent) {
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	}

	public void setProgressUpdateListener(FileReadyListener progressUpdateListener) {
		this.progressUpdateListener = progressUpdateListener;
	}

	public boolean isInstallingBoard() {
		return installingBoard;
	}

	public class ServiceBinder extends Binder {
		public GetAndSaveBoard getService() {
			return GetAndSaveBoard.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

	public void loadBoard(int selectedBoardId, int screenWidth) {
		installingBoard = true;

		this.screenWidth = screenWidth;

		// check if we have saved board for that id
		QueryParams queryParams = DbHelper.getTableRecordById(DbScheme.Tables.THEME_BOARDS, selectedBoardId);
		Cursor cursor = DbDataManager.query(getContentResolver(), queryParams);

		if (cursor != null && cursor.moveToFirst()) {
			BoardSingleItem.Data boardData = DbDataManager.getThemeBoardItemFromCursor(cursor);
			if (TextUtils.isEmpty(boardData.getLocalPath())) {
				loadBoard(selectedBoardId);
				return;
			}

			appData.setUseThemeBoard(true);
			appData.setThemeBoardId(boardData.getThemeBoardId());
			appData.setThemeBoardName(boardData.getName());
			appData.setThemeBoardPreviewUrl(boardData.getLineBoardPreviewUrl());
			appData.setThemeBoardCoordinateLight(Color.parseColor(boardData.getCoordinateColorLight()));
			appData.setThemeBoardCoordinateDark(Color.parseColor(boardData.getCoordinateColorDark()));
			appData.setThemeBoardHighlight(Color.parseColor(boardData.getHighlightColor()));
			appData.setThemeBoardPath(boardData.getLocalPath());

			// update listener
			showCompleteToNotification();
		} else {
			// start loading board
			loadBoard(selectedBoardId);
		}
	}

	private void loadBoard(int selectedBoardId) {
		LoadItem loadItem = LoadHelper.getBoardById(getUserToken(), selectedBoardId);
		new RequestJsonTask<BoardSingleItem>(boardSingleItemUpdateListener).executeTask(loadItem);
	}

	private class BoardSingleItemUpdateListener extends AbstractUpdateListener<BoardSingleItem> {

		private BoardSingleItemUpdateListener() {
			super(getContext(), BoardSingleItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showIndeterminateNotification(getString(R.string.downloading_arg, getString(R.string.board)));
			}
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

			showIndeterminateNotification(getString(R.string.downloading_arg, getString(R.string.board)));

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

			notificationBuilder.setContentText(getString(R.string.downloading_arg, getString(R.string.board)));
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

				boardData.setLocalPath(drawablePath);
				DbDataManager.saveThemeBoardItemToDb(getContentResolver(), boardData);

				// save board theme name to appData
				getAppData().setUseThemeBoard(true);
				getAppData().setThemeBoardPath(drawablePath);
				getAppData().setThemeBoardId(boardData.getThemeBoardId());
				getAppData().setThemeBoardName(boardData.getName());
				getAppData().setThemeBoardPreviewUrl(boardData.getLineBoardPreviewUrl());

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
		notifyManager.notify(R.id.notification_id, notificationBuilder.build());

		if (progressUpdateListener != null) {
			progressUpdateListener.changeTitle(title);
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
		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(DONE);
		}

		installingBoard = false;

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				notifyManager.cancel(R.id.notification_id);

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
