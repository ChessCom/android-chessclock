package com.chess.backend;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.chess.R;
import com.chess.backend.entity.api.themes.BackgroundSingleItem;
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
import com.chess.statics.IntentConstants;
import com.chess.ui.activities.MainFragmentFaceActivity;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.02.14
 * Time: 7:52
 */
public class GetAndSaveBackground extends Service {

	public static final int BACKGROUND_PORT = 0;
	public static final int BACKGROUND_LAND = 2;

	public static final int INDETERMINATE = -1;
	public static final int DONE = -2;
	private static final long SHUTDOWN_DELAY = 4 * 1000;

	private int screenWidth;
	private int screenHeight;
	private int backgroundWidth;
	private int backgroundHeight;
	private String backgroundUrlPort;
	private String backgroundUrlLand;
	private ImageDownloaderToListener imageDownloader;
	private ImageUpdateListener backgroundUpdateListener;
	private ImageUpdateListener backgroundLandUpdateListener;

	private ImageSaveListener mainBackgroundImgSaveListener;
	private ImageSaveListener mainBackgroundLandImgSaveListener;

	private AppData appData;

	private NotificationManager notifyManager;
	private NotificationCompat.Builder notificationBuilder;

	private ServiceBinder serviceBinder = new ServiceBinder();
	private FileReadyListener progressUpdateListener;
	private Handler handler;
	private boolean isTablet;
	private BackgroundSingleItem.Data backgroundData;

	private boolean installingBackground;

	public class ServiceBinder extends Binder {
		public GetAndSaveBackground getService() {
			return GetAndSaveBackground.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		isTablet = AppUtils.isTablet(this);

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
		notificationBuilder.setContentTitle(getString(R.string.downloading_arg, getString(R.string.background)))
				.setTicker(getString(R.string.downloading_arg, getString(R.string.background)))
				.setContentText(getString(R.string.downloading_arg, getString(R.string.background)))
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setAutoCancel(true);
		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);

		backgroundUpdateListener = new ImageUpdateListener(BACKGROUND_PORT);
		backgroundLandUpdateListener = new ImageUpdateListener(BACKGROUND_LAND);
		mainBackgroundImgSaveListener = new ImageSaveListener(BACKGROUND_PORT);
		mainBackgroundLandImgSaveListener = new ImageSaveListener(BACKGROUND_LAND);

		imageDownloader = new ImageDownloaderToListener(this);
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
		return START_NOT_STICKY;
	}

	public void loadBackground(BackgroundSingleItem.Data backgroundData, int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		if (backgroundData == null) {
			return;
		}

		{ // load background
			QueryParams queryParams = DbHelper.getTableRecordById(DbScheme.Tables.THEME_BACKGROUNDS,
					backgroundData.getBackgroundId());
			Cursor cursor = DbDataManager.query(getContentResolver(), queryParams);

			if (cursor != null && cursor.moveToFirst()) {
				backgroundData = DbDataManager.getThemeBackgroundItemFromCursor(cursor);

				boolean themeLoaded = true;
				if (!isTablet) {
					if (TextUtils.isEmpty(backgroundData.getLocalPathPort())) {
						themeLoaded = false;
					}
				} else {
					if (TextUtils.isEmpty(backgroundData.getLocalPathPort())
							|| TextUtils.isEmpty(backgroundData.getLocalPathLand())) {
						themeLoaded = false;
					}
				}

				if (themeLoaded) {
					appData.setThemeBackgroundName(backgroundData.getName());
					appData.setThemeBackgroundPreviewUrl(backgroundData.getBackgroundPreviewUrl());
					appData.setThemeBackPathPort(backgroundData.getLocalPathPort());
					appData.setThemeBackPathLand(backgroundData.getLocalPathLand());
					appData.setThemeFontColor(backgroundData.getFontColor());

					sendBroadcast(new Intent(IntentConstants.UPDATE_BACKGROUND));
					return;
				}
			}
		}
		installingBackground = true;

		this.backgroundData = backgroundData;

		showIndeterminateNotification(getString(R.string.downloading_arg, getString(R.string.background)));

		backgroundWidth = screenWidth;
		backgroundHeight = screenHeight;

		LoadItem loadItem;
		if (!isTablet) {
			loadItem = LoadHelper.getBackgroundById(getUserToken(), backgroundData.getBackgroundId(),
					screenWidth, screenHeight, RestHelper.V_HANDSET);
		} else {
			if (screenWidth > screenHeight) {
				backgroundWidth = screenHeight;
				backgroundHeight = screenWidth;
			}

			// we need to download port and landscape backgrounds for tablets
			loadItem = LoadHelper.getBackgroundById(getUserToken(), backgroundData.getBackgroundId(),
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

			int backgroundId = backgroundData.getBackgroundId();
			backgroundData = returnedObj.getData(); // update data from server and reset backgroundId
			backgroundData.setBackgroundId(backgroundId);

			showIndeterminateNotification(getString(R.string.downloading_arg, getString(R.string.background)));

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

				LoadItem loadItem = LoadHelper.getBackgroundById(getUserToken(), backgroundId,
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
				installingBackground = false;

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
				showIndeterminateNotification(getString(R.string.downloading_arg, getString(R.string.background)));

				String filename = String.valueOf(backgroundUrlPort.hashCode()); // TODO rename to MD5
				new SaveImageToSdTask(mainBackgroundImgSaveListener, bitmap).executeTask(filename);
			} else if (listenerCode == BACKGROUND_LAND) {
				showIndeterminateNotification(getString(R.string.downloading_arg, getString(R.string.background)));

				String filename = String.valueOf(backgroundUrlLand.hashCode()); // TODO rename to MD5
				new SaveImageToSdTask(mainBackgroundLandImgSaveListener, bitmap).executeTask(filename);
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

					getAppData().setThemeFontColor(backgroundData.getFontColor());
					getAppData().setThemeBackPathPort(imgFile.getAbsolutePath());

					DbDataManager.saveThemeBackgroundItemToDb(getContentResolver(), backgroundData);

				} catch (IOException e) {
					e.printStackTrace();
				}

				notificationBuilder.setContentText(getString(R.string.downloading_arg, getString(R.string.background)));
				showCompleteToNotification();

				getAppData().setThemeBackgroundPreviewUrl(backgroundData.getBackgroundPreviewUrl());
				getAppData().setThemeBackgroundName(backgroundData.getName());

				sendBroadcast(new Intent(IntentConstants.UPDATE_BACKGROUND));
			} else if (listenerCode == BACKGROUND_LAND) {
				// set main background image as theme
				String filename = String.valueOf(backgroundUrlLand.hashCode());
				try {
					File imgFile = AppUtils.openFileByName(getContext(), filename);

					backgroundData.setLocalPathLand(imgFile.getAbsolutePath());
					getAppData().setThemeBackPathLand(imgFile.getAbsolutePath());

					DbDataManager.saveThemeBackgroundItemToDb(getContentResolver(), backgroundData);

				} catch (IOException e) {
					e.printStackTrace();
				}

				sendBroadcast(new Intent(IntentConstants.UPDATE_BACKGROUND));
			}
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

		installingBackground = false;

		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(DONE);
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

		installingBackground = false;

		if (progressUpdateListener != null) {
			progressUpdateListener.setProgress(DONE);
		}

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

	public boolean isInstallingBackground() {
		return installingBackground;
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
