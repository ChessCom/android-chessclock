package com.chess.backend.tasks;

import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class PostJsonDataTask extends AbstractUpdateTask<String, LoadItem> {

    private static final String TAG = "PostDataTask";

	public PostJsonDataTask(TaskUpdateInterface<String> taskFace) {
		super(taskFace);
	}

	@Override
	protected Integer doTheTask(LoadItem... loadItem) {

		String url = RestHelper.formPostRequest(loadItem[0]);
        result = postData(url, loadItem[0]);
		return result;
	}


	private int postData(String url, LoadItem loadItem) {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, Integer.MAX_VALUE);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

		Log.d(TAG, "posting to url = " + url);

		HttpPost httpPost = new HttpPost(url);
		try {
			StringEntity stringEntity = new StringEntity(formJsonData(loadItem.getRequestParams()), HTTP.UTF_8);
			Log.d(TAG,"sending JSON object = " + formJsonData(loadItem.getRequestParams()));

			httpPost.setEntity(stringEntity);
			if (RestHelper.IS_TEST_SERVER_MODE)
			  httpPost.addHeader(RestHelper.AUTHORIZATION_HEADER, RestHelper.AUTHORIZATION_HEADER_VALUE);
		} catch (UnsupportedEncodingException e) {
			AppUtils.logD(TAG, e.toString());
		}

		try {
			HttpResponse response = httpClient.execute(httpPost);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
				return StaticData.UNKNOWN_ERROR;
			}
			if (response != null){
				item = EntityUtils.toString(response.getEntity());
				result = StaticData.RESULT_OK;
				Log.d(TAG,"WebRequest SERVER RESPONSE: " + item);
			}

		} catch (IOException e) {
			httpPost.abort();
			Log.e(TAG, "I/O error while retrieving data from " + url, e);
			result = StaticData.UNKNOWN_ERROR;
		} catch (IllegalStateException e) {
			httpPost.abort();
			Log.e(TAG, "Incorrect URL: " + url, e);
			result = StaticData.UNKNOWN_ERROR;
		} catch (Exception e) {
			httpPost.abort();
			Log.e(TAG, "Error while retrieving data from " + url, e);
			result = StaticData.UNKNOWN_ERROR;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	private String formJsonData(List<NameValuePair> requestParams){
		StringBuilder data = new StringBuilder();
		String separator = StaticData.SYMBOL_EMPTY;
		data.append(RestHelper.OBJ_START);
			for (NameValuePair requestParam : requestParams) {

				data.append(separator);
				separator = StaticData.SYMBOL_COMMA;
				data.append(RestHelper.SYMBOL_QUOTE)
						.append(requestParam.getName()).append(RestHelper.SYMBOL_QUOTE)
						.append(RestHelper.OBJ_DIVIDER)
						.append(RestHelper.SYMBOL_QUOTE)
						.append(requestParam.getValue())
						.append(RestHelper.SYMBOL_QUOTE);
			}
		data.append(RestHelper.OBJ_END);
		return data.toString();
	}

}
