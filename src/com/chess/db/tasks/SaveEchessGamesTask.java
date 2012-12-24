package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.new_api.DailyGameBaseData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;

import java.util.List;

public abstract class SaveEchessGamesTask<T extends DailyGameBaseData> extends AbstractUpdateTask<T , Long> {

//	private static final String TAG = "SaveEchessGamesTask";
//	private final LoadItem loadItem;
	protected ContentResolver contentResolver;
	protected static String[] arguments = new String[2];

	public SaveEchessGamesTask(TaskUpdateInterface<T> taskFace, List<T> currentItems, ContentResolver resolver) {
		super(taskFace);
		itemList = currentItems;
		this.contentResolver = resolver;
//		loadItem = new LoadItem();
	}

//	protected void updateOnlineGame(long gameId, String userName, String userToken) {
//		loadItem.setLoadPath(RestHelper.GET_GAME_V5);
//		loadItem.addRequestParams(RestHelper.P_ID, userToken);
//		loadItem.addRequestParams(RestHelper.P_GID, gameId);
//
//		GameOnlineItem currentGame = getData(RestHelper.formCustomRequest(loadItem));
//		if (currentGame != null) {
//			DBDataManager.updateOnlineGame(contentResolver, currentGame, userName);
//		}
//	}

//	protected GameOnlineItem getData(String url) {
//		Log.d(TAG, "retrieving from url = " + url);
//
//		long tag = System.currentTimeMillis();
//		BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_REQUEST, "tag=" + tag + " " + url);
//		HttpURLConnection connection = null;
//		try {
//			URL urlObj = new URL(url);
//			connection = (HttpURLConnection) urlObj.openConnection();
//
//			final int statusCode = connection.getResponseCode();
//			if (statusCode != HttpStatus.SC_OK) {
//				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
//
//				result = StaticData.UNKNOWN_ERROR;
//				return null;
//			}
//
//			String returnedObj = convertStreamToString(connection.getInputStream());
//
//			result = StaticData.RESULT_OK;
//			Log.d(TAG, "WebRequest SERVER RESPONSE: " + item);
//			BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_RESPONSE, "tag=" + tag + " " + item);
//
//			return ChessComApiParser.getGameParseV3(returnedObj);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			result = StaticData.INTERNAL_ERROR;
//		} catch (IOException e) {
//			Log.e(TAG, "I/O error while retrieving data from " + url, e);
//			result = StaticData.NO_NETWORK;
//		} catch (IllegalStateException e) {
//			Log.e(TAG, "Incorrect URL: " + url, e);
//			result = StaticData.UNKNOWN_ERROR;
//		} catch (Exception e) {
//			Log.e(TAG, "Error while retrieving data from " + url, e);
//			result = StaticData.UNKNOWN_ERROR;
//		} finally {
//			if (connection != null) {
//				connection.disconnect();
//			}
//		}
//		return null;
//	}
}
