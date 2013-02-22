package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.BaseResponseItem;
import com.chess.backend.entity.new_api.DailyGameByIdItem;
import com.chess.backend.entity.new_api.FriendsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.List;


public class SaveFriendsListTask extends AbstractUpdateTask<FriendsItem.Data, Long> {
	private static final String TAG = "SaveFriendsListTask";

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[2];
	private LoadItem loadItem;

	public SaveFriendsListTask(TaskUpdateInterface<FriendsItem.Data> taskFace, List<FriendsItem.Data> currentItems,
							   ContentResolver resolver) {
        super(taskFace);
		this.itemList = currentItems;
		this.contentResolver = resolver;
		loadItem = new LoadItem();
	}


	@Override
    protected Integer doTheTask(Long... ids) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);
		String userToken = AppData.getUserToken(context);

		for (FriendsItem.Data currentItem : itemList) { // if
			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(userName);
			arguments2[1] = String.valueOf(currentItem.getUserId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DBConstants.uriArray[DBConstants.FRIENDS];
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_USER_ID, DBDataManager.SELECTION_USER_ID, arguments2, null);

			ContentValues values = DBDataManager.putFriendItemToValues(currentItem, userName);

			if (cursor.moveToFirst()) {
				contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				contentResolver.insert(uri, values);
			}

			cursor.close();

			updateFriends(currentItem.getUserId(), userName, userToken);
		}

        result = StaticData.RESULT_OK;

        return result;
    }

	private void updateFriends(long userId, String userName, String userToken) {
		loadItem.setLoadPath(RestHelper.CMD_USER);
		loadItem.addRequestParams(RestHelper.P_USER_NAME, userName);

//		FriendsItem.Data friend = getData(RestHelper.formCustomRequest(loadItem));
//		if (friend != null) {
//			DBDataManager.updateFriend(contentResolver, friend, userName);
//		}
	}

//	protected User.Data getData(String url) {
//		FriendsItem item = null;
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
//					inputStream.closeBoard();
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

	private FriendsItem parseJson(String jRespString) {
		Gson gson = new Gson();
		return gson.fromJson(jRespString, FriendsItem.class);
	}

	private <CustomType> CustomType parseJson(String jRespString, Class<CustomType> clazz) {
		Gson gson = new Gson();
		return gson.fromJson(jRespString, clazz);
	}

}
