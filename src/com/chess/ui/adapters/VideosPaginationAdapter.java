package com.chess.ui.adapters;

import android.content.Context;
import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.model.VideoItem;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class VideosPaginationAdapter extends PaginationAdapter<VideoItem> {

	private static final String TAG = "VideosPaginationAdapter";

	protected LoadItem loadItem;
    private String url;


    public VideosPaginationAdapter(Context context, ItemsAdapter<VideoItem> adapter,
								   TaskUpdateInterface<VideoItem> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
        this.loadItem = loadItem;
	}

	@Override
	protected List<VideoItem> fetchMoreItems(int page) {
        url = RestHelper.formCustomPaginationRequest(loadItem, page + 1);
        result = getJsonData(url);
        return itemList;
	}

    protected int getJsonData(String url) {
        if(isTaskCanceled())
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
//                    inputStream = entity.getContent();

                    itemList = parseJson2List(responseText);
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
        return result;
    }

	private List<VideoItem> parseJson2List(String returnedObj) {
		itemList = new ArrayList<VideoItem>();
		String[] responseArray = returnedObj.trim().split("[|]");
		if (responseArray.length == 3) {
			responseArray = responseArray[2].split("<--->");
		} else {
			result = StaticData.MAX_REACHED;
			return itemList;
		}

		for (String responseItem : responseArray) {
			itemList.add(new VideoItem(responseItem.split("<->")));
		}
		return itemList;
	}

}
