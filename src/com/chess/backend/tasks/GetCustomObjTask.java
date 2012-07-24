package com.chess.backend.tasks;

import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class GetCustomObjTask<T> extends AbstractUpdateTask<T,LoadItem> {
	private static final String TAG = "GetCustomObjTask";

    private static int statusCode = -1;
    private static String reason = StaticData.SYMBOL_EMPTY;

	public GetCustomObjTask(TaskUpdateInterface<T> taskFace) {
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
		DefaultHttpClient httpClient = new DefaultHttpClient();

		Log.d(TAG, "retrieving from url = " + url);

		final HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
				return StaticData.UNKNOWN_ERROR;
			}
			if (response != null){
				item = (T) EntityUtils.toString(response.getEntity());
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
