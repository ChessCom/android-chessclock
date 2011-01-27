package com.chess.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class Web {	
	public static int StatusCode = -1;
	public static String Reason = "";
	public static String Request(String url, String method, HashMap<String, String> headers, HttpEntity entity){
		StatusCode = -1;
		Reason = "";
	    String responseBody = "";
	    
	    HttpParams httpParameters = null;	   
	    HttpResponse response = null;
		HttpRequestBase base = null;
		HttpClient httpclient = null;
	    
	    try {
	    	httpParameters = new BasicHttpParams();
			
			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			HttpConnectionParams.setSoTimeout(httpParameters, 10000);			
			
			httpclient = new DefaultHttpClient(httpParameters);
					
			if(method.equals("GET")){
				base = (HttpRequestBase)new HttpGet(url);
			} else if (method.equals("POST")){
				HttpPost post = new HttpPost(url);
				if(entity != null){
					post.setEntity(entity);
			    }
				base = (HttpRequestBase)post;
			} else if (method.equals("PUT")){
				HttpPut put = new HttpPut(url);
				if(entity != null){
					put.setEntity(entity);
			    }
				base = (HttpRequestBase)put;
			} else if (method.equals("DELETE")){
				base = (HttpRequestBase)new HttpDelete(url);
			}			
			if(headers != null){
				Iterator<String> headersIterator = headers.keySet().iterator();
			    while(headersIterator.hasNext()) {
			       String key = headersIterator.next();
			       base.addHeader(key, headers.get(key));		       
			    }
			}
		} catch (Exception e) {
			responseBody = "Sorry... "+e.getMessage();
		}
		
		try {
			response = httpclient.execute(base);
			if(response != null)
				responseBody = convertStreamToString(response.getEntity().getContent());
			
			StatusCode = response.getStatusLine().getStatusCode();
			Reason = response.getStatusLine().getReasonPhrase();
			Log.i("SERVER RESPONSE: ", responseBody);
		} catch (ClientProtocolException e) {
			responseBody = "Sorry... No Active connection (CP)";
		} catch (java.net.SocketTimeoutException e) {
			responseBody = "Sorry... No Active connection (Timeout)";
		} catch (IOException e) {
			responseBody = "Sorry... No Active connection (IO)";
		} catch (Exception e) {			
			responseBody = "Sorry... "+e.getMessage();
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
}
