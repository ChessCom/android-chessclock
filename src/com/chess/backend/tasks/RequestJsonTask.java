package com.chess.backend.tasks;

import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.BaseResponseItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpStatus;

import org.apache.http.protocol.HTTP;


import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.util.List;

public class RequestJsonTask<ItemType> extends AbstractUpdateTask<ItemType, LoadItem> {
	private static final String TAG = "RequestJsonTask";

	public RequestJsonTask(TaskUpdateInterface<ItemType> taskFace) {
		super(taskFace);
	}

	@Override
	protected Integer doTheTask(LoadItem... loadItems) {
		result = requestData(loadItems[0]);
//		result = postData(loadItems[0].getLoadPath(), loadItems[0]);
		return result;
	}

	private int requestData(LoadItem loadItem) {
		String url = RestHelper.formCustomRequest(loadItem);
		if (loadItem.getRequestMethod().equals(RestHelper.POST)){
		    url = RestHelper.formPostRequest(loadItem);
		}
		Log.d(TAG, "retrieving from url = " + url);

		long tag = System.currentTimeMillis();
		BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_REQUEST, "tag=" + tag + " " + url);

		HttpURLConnection connection = null;
		try {
			URL urlObj = new URL(url);
			connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod(loadItem.getRequestMethod());

			if (RestHelper.IS_TEST_SERVER_MODE) {
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(RestHelper.V_TEST_NAME, RestHelper.V_TEST_NAME2.toCharArray());
					}
				});
			}

			if (loadItem.getRequestMethod().equals(RestHelper.POST)){
				submitPostData(connection, loadItem);
			}

			final int statusCode = connection.getResponseCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);

				InputStream inputStream = connection.getErrorStream();
				String resultString = convertStreamToString(inputStream);
				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
				Log.d(TAG, "Code: " + baseResponse.getCode() + " Message: " + baseResponse.getMessage());
				return RestHelper.encodeServerCode(baseResponse.getCode());
			}

			InputStream inputStream = null;
			String resultString = null;
			try {
				inputStream = connection.getInputStream();

				resultString = convertStreamToString(inputStream);
				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
				if (baseResponse.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
					item = parseJson(resultString);
					if(item != null) {
						result = StaticData.RESULT_OK;
					}

				}
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}

			result = StaticData.RESULT_OK;
			Log.d(TAG, "WebRequest SERVER RESPONSE: " + resultString);
			BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_RESPONSE, "tag=" + tag + " " + resultString);

		} catch (MalformedURLException e) {
			e.printStackTrace();
			result = StaticData.INTERNAL_ERROR;
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			result = StaticData.INTERNAL_ERROR;
		} catch (IOException e) {
			Log.e(TAG, "I/O error while retrieving data from " + url, e);
			result = StaticData.NO_NETWORK;
		} catch (IllegalStateException e) {
			Log.e(TAG, "Incorrect URL: " + url, e);
			result = StaticData.UNKNOWN_ERROR;
		} catch (Exception e) {
			Log.e(TAG, "Error while retrieving data from " + url, e);
			result = StaticData.UNKNOWN_ERROR;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return result;
	}

	private void submitPostData(URLConnection connection, LoadItem loadItem) throws IOException {
		String query = RestHelper.formPostData(loadItem);
		String charset = HTTP.UTF_8;
		connection.setDoOutput(true); // Triggers POST.
//		connection.setRequestProperty("Accept-Charset", charset);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
		OutputStream output = null;
		try {
			output = connection.getOutputStream();
			output.write(query.getBytes(charset));
		} finally {
			if (output != null) try {
				output.close();
			} catch (IOException ex) {
				Log.e(TAG, "Error while submiting POST data " + ex.toString());
			}
		}

	}

	private ItemType parseJson(InputStream jRespString) {
		Gson gson = new Gson();
		Reader reader = new InputStreamReader(jRespString);
		return gson.fromJson(reader, getTaskFace().getClassType());
	}

	private ItemType parseJson(String jRespString) {
		Gson gson = new Gson();
		return gson.fromJson(jRespString, getTaskFace().getClassType());
	}

	private <CustomType> CustomType parseJson(String jRespString, Class<CustomType> clazz) {
		Gson gson = new Gson();
		return gson.fromJson(jRespString, clazz);
	}

	private List<ItemType> parseJson2List(InputStream jRespString) {
		Gson gson = new Gson();
		Reader reader = new InputStreamReader(jRespString);
		Type t = getTaskFace().getListType();
		return gson.fromJson(reader, t);
	}

	private List<ItemType> parseJson2List(String jRespString) {
		Gson gson = new Gson();
		Type t = getTaskFace().getListType();
		return gson.fromJson(jRespString, t);
	}

	private String parseServerRequest(ItemType jRequest) {
		Gson gson = new Gson();
		return gson.toJson(jRequest);
	}

	private String parseServerRequestList(List<ItemType> jRequest) {
		Gson gson = new Gson();
		return gson.toJson(jRequest);
	}
}
