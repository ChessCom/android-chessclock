package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailyGameBaseData;
import com.chess.backend.entity.new_api.DailyGameByIdItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBDataManager;

import java.util.List;

public abstract class SaveDailyGamesTask<T extends DailyGameBaseData> extends AbstractUpdateTask<T , Long> {

	private static final String TAG = "SaveDailyGamesTask";
	private final LoadItem loadItem;
	protected ContentResolver contentResolver;
	protected static String[] arguments = new String[2];

	public SaveDailyGamesTask(TaskUpdateInterface<T> taskFace, List<T> currentItems, ContentResolver resolver) {
		super(taskFace);
		itemList = currentItems;
		this.contentResolver = resolver;
		loadItem = new LoadItem();

		if (taskFace == null || taskFace.getMeContext() == null){
			cancel(true);
			return;
		}

		String userToken = AppData.getUserToken(taskFace.getMeContext());

		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
	}

	protected void updateOnlineGame(long gameId, String userName) {
		loadItem.setLoadPath(RestHelper.CMD_GAME_BY_ID(gameId));

		DailyGameByIdItem.Data currentGame = null;
		try {
			currentGame = RestHelper.requestData(loadItem, DailyGameByIdItem.class).getData();
		} catch (InternalErrorException e) {
			e.logMe();
		}
		if (currentGame != null) {
			result = StaticData.RESULT_OK;
			DBDataManager.updateOnlineGame(contentResolver, currentGame, userName);
		}
	}

//	protected DailyGameByIdItem.Data getData(String url) {
//		DailyGameByIdItem item = null;
//		Log.d(TAG, "retrieving from url = " + url);
//
//		long tag = System.currentTimeMillis();
//		BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_REQUEST, "tag=" + tag + " " + url);
//
//		HttpURLConnection connection = null;
//		try {
//			URL urlObj = new URL(url);
//			connection = (HttpURLConnection) urlObj.openConnection();
//			connection.setRequestMethod(loadItem.getRequestMethod());
//
//			if (RestHelper.IS_TEST_SERVER_MODE) {
//				Authenticator.setDefault(new Authenticator() {
//					protected PasswordAuthentication getPasswordAuthentication() {
//						return new PasswordAuthentication(RestHelper.V_TEST_NAME, RestHelper.V_TEST_NAME2.toCharArray());
//					}
//				});
//			}
//
//			final int statusCode = connection.getResponseCode();
//			if (statusCode != HttpStatus.SC_OK) {
//				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
//
//				InputStream inputStream = connection.getErrorStream();
//				String resultString = convertStreamToString(inputStream);
//				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
//				Log.d(TAG, "Code: " + baseResponse.getCode() + " Message: " + baseResponse.getMessage());
//				result = RestHelper.encodeServerCode(baseResponse.getCode());
//			}
//
//			InputStream inputStream = null;
//			String resultString = null;
//			try {
//				inputStream = connection.getInputStream();
//
//				resultString = convertStreamToString(inputStream);
//				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
//				if (baseResponse.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
//					item = parseJson(resultString);
//					if(item != null) {
//						result = StaticData.RESULT_OK;
//					}
//				}
//			} finally {
//				if (inputStream != null) {
//					inputStream.close();
//				}
//			}
//
//			result = StaticData.RESULT_OK;
//			Log.d(TAG, "WebRequest SERVER RESPONSE: " + resultString);
//			BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_RESPONSE, "tag=" + tag + " " + resultString);
//			return item.getData();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			result = StaticData.INTERNAL_ERROR;
//		} catch (JsonSyntaxException e) {
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
//
//	private DailyGameByIdItem parseJson(String jRespString) {
//		Gson gson = new Gson();
//		return gson.fromJson(jRespString, DailyGameByIdItem.class);
//	}
//
//	private <CustomType> CustomType parseJson(String jRespString, Class<CustomType> clazz) {
//		Gson gson = new Gson();
//		return gson.fromJson(jRespString, clazz);
//	}
}
