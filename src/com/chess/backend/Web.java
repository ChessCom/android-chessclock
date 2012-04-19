package com.chess.backend;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class Web {
	private static int statusCode = -1;
	private static String reason = "";

	public static String Request(String url, String method, HashMap<String, String> headers, HttpEntity entity) {
		statusCode = -1;
		reason = "";
		String responseBody = "";

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
				Iterator<String> headersIterator = headers.keySet().iterator();
				while (headersIterator.hasNext()) {
					String key = headersIterator.next();
					base.addHeader(key, headers.get(key));
				}
			}
		} catch (Exception e) {
			responseBody = "Sorry... " + e.getMessage();
		}

		int i = 0;
		try {

			// test server login support
			base.addHeader("Authorization", "Basic Ym9iYnk6ZmlzY2hlcg==");

			response = httpclient.execute(base);
			i = 1;
			if (response != null)
				responseBody = convertStreamToString(response.getEntity().getContent());
			i = 2;

			statusCode = response.getStatusLine().getStatusCode();
			i = 3;
			reason = response.getStatusLine().getReasonPhrase();
			i = 4;
			Log.d("WebRequest SERVER RESPONSE: ", responseBody);
			i = 5;
		} catch (ClientProtocolException e) {
			//responseBody = "Sorry... No Active connection (CP)" + " code=" + i;
			e.printStackTrace();
			Log.d("WEB", "!!!!!!!! " + i);
		} catch (java.net.SocketTimeoutException e) {
			e.printStackTrace();
			//responseBody = "Sorry... No Active connection (Timeout)" + " code=" + i;
			Log.d("WEB", "!!!!!!!! " + i);
		} catch (IOException e) {
			e.printStackTrace();
			//responseBody = "Sorry... No Active connection (IO)" + " code=" + i;
			Log.d("WEB", "BASE: " + base.getMethod());
			Log.d("WEB", "BASE: " + base.getParams());
			Log.d("WEB", "BASE: " + base.getAllHeaders());
			Log.d("WEB", "BASE: " + base.getProtocolVersion());
			Log.d("WEB", "BASE: " + base.getRequestLine());
			Log.d("WEB", "BASE: " + base.getURI());
			Log.d("WEB", "!!!!!!!! " + i);
		} catch (Exception e) {
			e.printStackTrace();
			//responseBody = "Sorry... "+e.getMessage() + " code=" + i;
			Log.d("WEB", "!!!!!!!! " + i);
		}
		return responseBody;
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8000);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static int getStatusCode() {
		return statusCode;
	}
}
