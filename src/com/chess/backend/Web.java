package com.chess.backend;

import android.util.Log;
import com.chess.backend.statics.StaticData;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * Use RestHelper and GetCustomObjectTask instead
 */
@Deprecated
public class Web {
	private static int statusCode = -1;
	private static final String TAG = "Web";

	@Deprecated
	public static String Request(String url, String method, HashMap<String, String> headers, HttpEntity entity) {
		statusCode = -1;
		String reason = StaticData.SYMBOL_EMPTY;
		String responseBody = StaticData.SYMBOL_EMPTY;

		HttpParams httpParameters = null;
		HttpResponse response = null;
		HttpRequestBase base = null;
		HttpClient httpclient = null;

		try {
			httpParameters = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			HttpConnectionParams.setSoTimeout(httpParameters, Integer.MAX_VALUE);

			httpclient = new DefaultHttpClient(httpParameters);
			Log.d("WebRequest", "requesting url = " + url);
			if (method.equals("GET")) {
				base = new HttpGet(url);
			} else if (method.equals("POST")) {
				HttpPost post = new HttpPost(url);
				if (entity != null) {
					post.setEntity(entity);
				}
				base = post;
			} else if (method.equals("PUT")) {
				HttpPut put = new HttpPut(url);
				if (entity != null) {
					put.setEntity(entity);
				}
				base = put;
			} else if (method.equals("DELETE")) {
				base = new HttpDelete(url);
			}
			if (headers != null) {
				for (String key : headers.keySet()) {
					base.addHeader(key, headers.get(key));
				}
			}
		} catch (Exception e) {
			responseBody = "Sorry... " + e.getMessage();
		}

		try {
			// test server login support
			//base.addHeader("Authorization", "Basic Ym9iYnk6ZmlzY2hlcg==");

			response = httpclient.execute(base);
			if (response != null) {
				responseBody = EntityUtils.toString(response.getEntity());
				Log.d(TAG,"WebRequest SERVER RESPONSE: " + responseBody);
			}

			statusCode = response.getStatusLine().getStatusCode();
			reason = response.getStatusLine().getReasonPhrase();
			Log.d(TAG,"Reason of response: " + reason);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "HTTP protocol error happen, while retrieving data from  " + url, e);
		} catch (java.net.SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "I/O error while retrieving data from " + url, e);
			Log.d(TAG, "BASE: " + base.getMethod());
			Log.d(TAG, "BASE: " + base.getParams());
			Log.d(TAG, "BASE: " + base.getAllHeaders());
			Log.d(TAG, "BASE: " + base.getProtocolVersion());
			Log.d(TAG, "BASE: " + base.getRequestLine());
			Log.d(TAG, "BASE: " + base.getURI());
		} catch (Exception e) {
			Log.e(TAG, "Error while retrieving data from " + url, e);
		}
		return responseBody;
	}

	public static int getStatusCode() {
		return statusCode;
	}
}
