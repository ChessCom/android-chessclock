package com.chess.backend.tasks;

import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.BaseResponseItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

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
		result = getData(loadItems[0]);
		return result;
	}

	private int getData(LoadItem loadItem) {
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
//				connection.addRequestProperty(RestHelper.AUTHORIZATION_HEADER, RestHelper.AUTHORIZATION_HEADER_VALUE);
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(RestHelper.V_TEST_NAME, RestHelper.V_TEST_NAME2.toCharArray());
					}
				});
//				httpGet.addHeader(RestHelper.AUTHORIZATION_HEADER, RestHelper.AUTHORIZATION_HEADER_VALUE);
			}

			if (loadItem.getRequestMethod().equals(RestHelper.POST)){
				submitPostData(connection, loadItem);
			}

			final int statusCode = connection.getResponseCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);

				return StaticData.INTERNAL_ERROR;
			}

			InputStream inputStream = null;
			String resultString = null;
			try {
//				String responseText =  EntityUtils.toString(response.getEntity());   // don't remove for quick debug
//				Log.d(TAG, "received raw JSON response = " + responseText);
				inputStream = connection.getInputStream();

				resultString = convertStreamToString(inputStream);
				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
				if (baseResponse.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
//					if (baseResponse.getCount() > 0) {
//
//					} else {
//
//					}
					item = parseJson(resultString);
					if(item != null) {
						result = StaticData.RESULT_OK;
//						Log.d(TAG, "received JSON object = " + parseServerRequest(item));
					}

				} else {
					result = baseResponse.getCode() | 0x100; // TODO set proper mask
				}
//				if (useList) {
//					itemList = parseJson2List(resultString);
////						Log.d(TAG, "received JSON list object = " + parseServerRequestList(itemList));
//					if(itemList.size() > 0)
//						result = StaticData.RESULT_OK;
//				} else {
//					item = parseJson(resultString);
//					if(item != null)
//						result = StaticData.RESULT_OK;
////						Log.d(TAG, "received JSON object = " + parseServerRequest(item));
//				}

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
