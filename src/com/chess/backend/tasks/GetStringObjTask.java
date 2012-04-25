package com.chess.backend.tasks;

import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.ui.core.AppConstants;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GetStringObjTask extends AbstractUpdateTask<String,LoadItem> {
	private static final String TAG = "GetStringObjTask";
    private static int statusCode = -1;
    private static String reason = AppConstants.SYMBOL_EMPTY;

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
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, Integer.MAX_VALUE);

		HttpClient httpClient = new DefaultHttpClient(httpParameters);

		Log.d(TAG, "retrieving from url = " + url);

		HttpRequestBase httpGet = new HttpGet(url);
		try {
			// test server login support
			httpGet.addHeader("Authorization", "Basic Ym9iYnk6ZmlzY2hlcg==");
			HttpResponse response = httpClient.execute(httpGet);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving dat from " + url);
				return StaticData.UNKNOWN_ERROR;
			}
			if (response != null){
				item = EntityUtils.toString(response.getEntity());
				result = StaticData.RESULT_OK;
				Log.d(TAG,"WebRequest SERVER RESPONSE: " + item);
			}

		} catch (IOException e) {
			httpGet.abort();
			Log.e(TAG, "I/O error while retrieving data from " + url, e);
			result = StaticData.UNKNOWN_ERROR;
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

}
