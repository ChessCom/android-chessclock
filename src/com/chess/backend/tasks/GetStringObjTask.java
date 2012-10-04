package com.chess.backend.tasks;

import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GetStringObjTask extends AbstractUpdateTask<String, LoadItem> {
	private static final String TAG = "GetStringObjTask";
	private static final int TIMEOUT = 10000;

	public GetStringObjTask(TaskUpdateInterface<String> taskFace) {
		super(taskFace);
	}

	@Override
	protected Integer doTheTask(LoadItem... loadItems) {
		String url = RestHelper.formCustomRequest(loadItems[0]);
		result = getData(url);
		return result;
	}

	private int getData(String url) {
		// Instantiate the custom HttpClient
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters, Integer.MAX_VALUE);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

		Log.d(TAG, "retrieving from url = " + url);

		long tag = System.currentTimeMillis();
		BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_REQUEST, "tag=" + tag + " " + url);

		HttpRequestBase httpGet = new HttpGet(url);
		try {
            if (RestHelper.IS_TEST_SERVER_MODE)
                httpGet.addHeader(RestHelper.AUTHORIZATION_HEADER, RestHelper.AUTHORIZATION_HEADER_VALUE);
			HttpResponse response = httpClient.execute(httpGet);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
				return StaticData.UNKNOWN_ERROR;
			}
			if (response != null) {
				item = EntityUtils.toString(response.getEntity());
				result = StaticData.RESULT_OK;
				Log.d(TAG, "WebRequest SERVER RESPONSE: " + item);
				BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_RESPONSE, "tag=" + tag + " " + item);
			}

		} catch (IOException e) {
			httpGet.abort();
			Log.e(TAG, "I/O error while retrieving data from " + url, e);
			result = StaticData.NO_NETWORK;
		} catch (IllegalStateException e) {
			httpGet.abort();
			Log.e(TAG, "Incorrect URL: " + url, e);
			result = StaticData.UNKNOWN_ERROR;
		} catch (Exception e) {
			httpGet.abort();
			Log.e(TAG, "Error while retrieving data from " + url, e);
			result = StaticData.UNKNOWN_ERROR;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		blockScreenRotation(false);

		if(isCancelled()) {
			return;
		}
		taskFace.showProgress(false);
		if (result == StaticData.RESULT_OK) {
//			if (useList) // we never use list
//				taskFace.updateListData(itemList);
//			else
			if (item.contains(RestHelper.R_SUCCESS)) {
				taskFace.updateData(item);
			} else if (item.contains(RestHelper.R_ERROR)) {
				taskFace.errorHandle(item.substring(RestHelper.R_ERROR.length()));
			}

		} else {
			taskFace.errorHandle(result);
		}
	}

}
