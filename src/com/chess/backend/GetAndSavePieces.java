package com.chess.backend;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.chess.R;
import com.chess.backend.entity.api.themes.PieceSingleItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.tasks.GetAndSaveFileToSdTask;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.statics.AppData;
import com.chess.ui.activities.MainFragmentFaceActivity;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.fragments.settings.SettingsThemeFragment;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.10.13
 * Time: 5:13
 */
public class GetAndSavePieces extends Service {

	public static final int INDETERMINATE = -1;
	public static final int DONE = -2;
	private static final long SHUTDOWN_DELAY = 4 * 1000;
	public static final String SLASH = "/";
	public static final String PNG = ".png";

	private ServiceBinder serviceBinder = new ServiceBinder();

	private Handler handler;
	private NotificationManager notifyManager;
	private NotificationCompat.Builder notificationBuilder;
	private FileReadyListener progressUpdateListener;
	private boolean installingPieces;
	private AppData appData;
	private PiecesPackSaveListener piecesPackSaveListener;
	private String selectedPieceDir;
	private int screenWidth;
	private PieceSingleItem.Data piecesData;
	private PiecesSingleItemUpdateListener piecesSingleItemUpdateListener;
	private String userToken;

	public class ServiceBinder extends Binder {
		public GetAndSavePieces getService() {
			return GetAndSavePieces.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder;
	}

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

		String downloadingPiecesStr = getString(R.string.downloading_arg, getString(R.string.pieces));
		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setContentTitle(downloadingPiecesStr)
				.setTicker(downloadingPiecesStr)
				.setContentText(downloadingPiecesStr)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setAutoCancel(true);
		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);

		userToken = appData.getUserToken();

		piecesSingleItemUpdateListener = new PiecesSingleItemUpdateListener();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setFlagsForNotifyIntent(Intent notifyIntent) {
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	}

	public void setProgressUpdateListener(FileReadyListener progressUpdateListener) {
		this.progressUpdateListener = progressUpdateListener;
	}

	public boolean isInstallingPieces() {
		return installingPieces;
	}

	public void loadPieces(int selectedPieceId, int screenWidth) {
		installingPieces = true;

		this.screenWidth = screenWidth;
		piecesPackSaveListener = new PiecesPackSaveListener();

		LoadItem loadItem = LoadHelper.getPiecesById(userToken, selectedPieceId);
		new RequestJsonTask<PieceSingleItem>(piecesSingleItemUpdateListener).executeTask(loadItem);
	}

	private class PiecesSingleItemUpdateListener extends AbstractUpdateListener<PieceSingleItem> {

		private PiecesSingleItemUpdateListener() {
			super(getContext(), PieceSingleItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			if (show) {
				showIndeterminateNotification(getString(R.string.downloading_arg, getString(R.string.pieces)));
			}
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
				imagesToLoad[i] = PieceSingleItem.PATH + selectedPieceDir + SLASH + pieceWidth + SLASH + imageCode + PNG;
			}

			String[] blackPieceImageCodes = ChessBoard.blackPieceImageCodes;

			for (int i = 0; i < blackPieceImageCodes.length; i++) {
				String imageCode = blackPieceImageCodes[i];
				imagesToLoad[6 + i] = PieceSingleItem.PATH + selectedPieceDir + SLASH + pieceWidth + SLASH + imageCode + PNG;
			}

			showIndeterminateNotification(getString(R.string.downloading_arg, getString(R.string.pieces)));

			// Start loading pieces image
			new GetAndSaveFileToSdTask(piecesPackSaveListener, AppUtils.getLocalDirForPieces(getContext(), selectedPieceDir))
					.executeTask(imagesToLoad);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);

			installingPieces = false;
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

			appData.setUseThemePieces(true);
			appData.setThemePiecesPath(selectedPieceDir);

			if (selectedPieceDir.contains(SettingsThemeFragment._3D_PART)) {
				appData.setThemePieces3d(true);
			} else {
				appData.setThemePieces3d(false);
			}

			// save pieces theme name to appData
			appData.setThemePiecesId(piecesData.getThemePieceId());
			appData.setThemePiecesName(piecesData.getName());
			appData.setThemePiecesPreviewUrl(piecesData.getPreviewUrl());

			showCompleteToNotification();
		}

		@Override
		public void changeTitle(final String title) {
			showIndeterminateNotification(title);
		}

		@Override
		public void setProgress(final int progress) {
			if (previousProgress + 5 < progress) {
				previousProgress = progress;
				updateProgressToNotification(progress);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);

			installingPieces = false;
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

		installingPieces = false;

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				notifyManager.cancel(R.id.notification_id);

				stopSelf();
			}
		}, SHUTDOWN_DELAY);
	}

	private Context getContext() {
		return this;
	}
}
