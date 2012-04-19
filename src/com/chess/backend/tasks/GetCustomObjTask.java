package com.chess.backend.tasks;

import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
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
import java.util.Iterator;

public class GetCustomObjTask<T> extends AbstractUpdateTask<T,LoadItem> {
    private static int statusCode = -1;
    private static String reason = "";

	public GetCustomObjTask(TaskUpdateInterface<T> taskFace) {
		super(taskFace);
	}

	@Override
	protected Integer doTheTask(LoadItem... loadItems) {

		String url = RestHelper.formCustomRequest(loadItems[0]);

		return result;
	}

    private String request(String url, String method, HashMap<String, String> headers, HttpEntity entity) {
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
                responseBody = EntityUtils.toString(response.getEntity());
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

}
