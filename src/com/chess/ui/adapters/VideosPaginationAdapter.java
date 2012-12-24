package com.chess.ui.adapters;

import android.content.Context;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.BaseResponseItem;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.model.VideoItemOld;
import com.chess.utilities.AppUtils;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

//public class VideosPaginationAdapter extends PaginationAdapter<VideoItemOld> {
public class VideosPaginationAdapter extends PaginationAdapter<VideoItem.VideoDataItem> {

	private static final String TAG = "VideosPaginationAdapter";

	protected LoadItem loadItem;


//	public VideosPaginationAdapter(Context context, ItemsAdapter<VideoItemOld> adapter,
//									TaskUpdateInterface<VideoItemOld> taskFace, LoadItem loadItem) {
	public VideosPaginationAdapter(Context context, ItemsAdapter<VideoItem.VideoDataItem> adapter,
								   TaskUpdateInterface<VideoItem.VideoDataItem> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
        this.loadItem = loadItem;
		setFirstPage(1);
	}

	@Override
	protected List<VideoItem.VideoDataItem> fetchMoreItems(int page) {
//		final String url = RestHelper.formCustomPaginationRequest(loadItem, page + 1);
//        result = getJsonData(url);
        result = getData(loadItem, page);
        return itemList;
	}

    protected int getJsonData(String url) {
/*        if(isTaskCanceled())
            return result = StaticData.EMPTY_DATA;

        // Instantiate the custom HttpClient
		DefaultHttpClient httpClient = new DefaultHttpClient();

        Log.d(TAG, "retrieving from url = " + url);

        final HttpGet httpost = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpost);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
                return StaticData.UNKNOWN_ERROR;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {

					String responseText =  EntityUtils.toString(response.getEntity());
					Log.d(TAG, "received raw JSON response = " + responseText);

//                    itemList = parseJson2List(responseText);
                    if(itemList != null && itemList.size() > 0){
                        result = StaticData.RESULT_OK;
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (IOException e) {
            httpost.abort();
            Log.e(TAG, "I/O error while retrieving data from " + url, e);
            result = StaticData.UNKNOWN_ERROR;
        } catch (IllegalStateException e) {
            httpost.abort();
            Log.e(TAG, "Incorrect URL: " + url, e);
            result = StaticData.UNKNOWN_ERROR;
        } catch (Exception e) {
            httpost.abort();
            Log.e(TAG, "Error while retrieving data from " + url, e);
            result = StaticData.UNKNOWN_ERROR;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
 */       return result;
    }

	private int getData(LoadItem loadItem, int page) {
		if(isTaskCanceled())
			return result = StaticData.EMPTY_DATA;

		String url = RestHelper.formCustomPaginationRequest(loadItem, page /*+ 1*/);
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

				resultString = AppUtils.convertStreamToString(inputStream);
				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
				if (baseResponse.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
					VideoItem item = parseJson(resultString);
					if(item != null) {
						itemList = item.getData().getVideos();
						result = StaticData.RESULT_OK;
//						Log.d(TAG, "received JSON object = " + parseServerRequest(item));
					}

				} else {
					result = baseResponse.getCode() | 0x100; // TODO set proper mask
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

	private VideoItem parseJson(String jRespString) {
		Gson gson = new Gson();
		return gson.fromJson(jRespString, VideoItem.class);
	}

	private <CustomType> CustomType parseJson(String jRespString, Class<CustomType> clazz) {
		Gson gson = new Gson();
		return gson.fromJson(jRespString, clazz);
	}

	private List<VideoItemOld> parseJson2List(String returnedObj) {
		List<VideoItemOld> itemList = new ArrayList<VideoItemOld>();
		String[] responseArray = returnedObj.trim().split(RestHelper.SYMBOL_ITEM_SPLIT);
		if (responseArray.length == 3) {
			responseArray = responseArray[2].split("<--->");
		} else {
			result = StaticData.MAX_REACHED;
			return itemList;
		}

		for (String responseItem : responseArray) {
			itemList.add(new VideoItemOld(responseItem.split("<->")));
		}
		return itemList;
	}

}
