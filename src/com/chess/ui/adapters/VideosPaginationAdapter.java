package com.chess.ui.adapters;

import android.content.Context;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;

import java.util.List;

// TODO adjust for VideoCategoriesFragment
public class VideosPaginationAdapter extends PaginationAdapter<VideoItem.Data> {

	private static final String TAG = "VideosPaginationAdapter";

	protected LoadItem loadItem;


//	public VideosPaginationAdapter(Context context, ItemsAdapter<VideoItemOld> adapter,
//									TaskUpdateInterface<VideoItemOld> taskFace, LoadItem loadItem) {
	public VideosPaginationAdapter(Context context, ItemsAdapter<VideoItem.Data> adapter,
								   TaskUpdateInterface<VideoItem.Data> taskFace, LoadItem loadItem) {
		super(context, adapter, taskFace);
        this.loadItem = loadItem;
		setFirstPage(1);
	}

	@Override
	protected List<VideoItem.Data> fetchMoreItems(int page) {
//		final String url = RestHelper.formCustomPaginationRequest(loadItem, page + 1);
//        result = getJsonData(url);
		loadItem.replaceRequestParams(RestHelper.P_PAGE, String.valueOf(page));
		VideoItem item = null;
		try {
			 item = RestHelper.requestData(loadItem, VideoItem.class, AppUtils.getAppId(context));
		} catch (InternalErrorException e) {
			e.logMe();
		}

		if (item != null) {
			result = StaticData.RESULT_OK;
			itemList = item.getData();
		}

//		result = getData(loadItem, page);
        return itemList;
	}


//	private int getData(LoadItem loadItem, int page) {
//		if(isTaskCanceled())
//			return result = StaticData.EMPTY_DATA;
//
//		String url = RestHelper.formCustomPaginationRequest(loadItem, page );
//		if (loadItem.getRequestMethod().equals(RestHelper.POST)){
//			url = RestHelper.formPostRequest(loadItem);
//		}
//		Log.d(TAG, "retrieving from url = " + url);
//
//		long tag = System.currentTimeMillis();
//		BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_REQUEST, "tag=" + tag + " " + url);
//
//		HttpURLConnection connection = null;
//		try {
//			URL urlObj = new URL(url);
//			connection = (HttpURLConnection) urlObj.openConnection();
//			connection.setRequestMethod(loadItem.getRequestMethod());
//
//			if (RestHelper.IS_TEST_SERVER_MODE) {
//				Authenticator.setDefault(new Authenticator() {
//					protected PasswordAuthentication getPasswordAuthentication() {
//						return new PasswordAuthentication(RestHelper.V_TEST_NAME, RestHelper.V_TEST_NAME2.toCharArray());
//					}
//				});
//			}
//
//			final int statusCode = connection.getResponseCode();
//			if (statusCode != HttpStatus.SC_OK) {
//				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
//
//				InputStream inputStream = connection.getErrorStream();
//				String resultString = RestHelper.convertStreamToString(inputStream);
//				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
//				Log.d(TAG, "Code: " + baseResponse.getCode() + " Message: " + baseResponse.getMessage());
//				itemList = null;
//				return RestHelper.encodeServerCode(baseResponse.getCode());
//			}
//
//			InputStream inputStream = null;
//			String resultString = null;
//			try {
////				String responseText =  EntityUtils.toString(response.getEntity());   // don't remove for quick debug
////				Log.d(TAG, "received raw JSON response = " + responseText);
//				inputStream = connection.getInputStream();
//
//				resultString = RestHelper.convertStreamToString(inputStream);
//				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
//				if (baseResponse.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
//					VideoItem item = parseJson(resultString);
//					if(item != null) {
//						itemList = item.getData().getVideos();
//						result = StaticData.RESULT_OK;
////						Log.d(TAG, "received JSON object = " + parseServerRequest(item));
//					}
//
//				} else {
//					result = baseResponse.getCode() | 0x100; // TODO set proper mask
//				}
//
//			} finally {
//				if (inputStream != null) {
//					inputStream.closeBoard();
//				}
//			}
//
//			result = StaticData.RESULT_OK;
//			Log.d(TAG, "WebRequest SERVER RESPONSE: " + resultString);
//			BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_RESPONSE, "tag=" + tag + " " + resultString);
//
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			result = StaticData.INTERNAL_ERROR;
//		} catch (IOException e) {
//			Log.e(TAG, "I/O error while retrieving data from " + url, e);
//			result = StaticData.NO_NETWORK;
//		} catch (IllegalStateException e) {
//			Log.e(TAG, "Incorrect URL: " + url, e);
//			result = StaticData.UNKNOWN_ERROR;
//		} catch (Exception e) {
//			Log.e(TAG, "Error while retrieving data from " + url, e);
//			result = StaticData.UNKNOWN_ERROR;
//		} finally {
//			if (connection != null) {
//				connection.disconnect();
//			}
//		}
//		return result;
//	}
//
//	private VideoItem parseJson(String jRespString) {
//		Gson gson = new Gson();
//		return gson.fromJson(jRespString, VideoItem.class);
//	}
//
//	private <CustomType> CustomType parseJson(String jRespString, Class<CustomType> clazz) {
//		Gson gson = new Gson();
//		return gson.fromJson(jRespString, clazz);
//	}

//	private List<VideoItemOld> parseJson2List(String returnedObj) {
//		List<VideoItemOld> itemList = new ArrayList<VideoItemOld>();
//		String[] responseArray = returnedObj.trim().split(RestHelper.SYMBOL_ITEM_SPLIT);
//		if (responseArray.length == 3) {
//			responseArray = responseArray[2].split("<--->");
//		} else {
//			result = StaticData.MAX_REACHED;
//			return itemList;
//		}
//
//		for (String responseItem : responseArray) {
//			itemList.add(new VideoItemOld(responseItem.split("<->")));
//		}
//		return itemList;
//	}

}
