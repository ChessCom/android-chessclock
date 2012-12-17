package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.model.GameListCurrentItem;
import com.chess.model.GameOnlineItem;
import com.chess.utilities.ChessComApiParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


public class SaveEchessCurrentGamesListTask extends AbstractUpdateTask<GameListCurrentItem, Long> {

	private static final String TAG = "SaveEchessCurrentGamesListTask";
	private ContentResolver contentResolver;
	private static String[] arguments = new String[2];
	private final LoadItem loadItem;

	public SaveEchessCurrentGamesListTask(TaskUpdateInterface<GameListCurrentItem> taskFace, List<GameListCurrentItem> currentItems) {
        super(taskFace);
		itemList = currentItems;

		contentResolver = taskFace.getMeContext().getContentResolver();
		loadItem = new LoadItem();

	}

    @Override
    protected Integer doTheTask(Long... ids) {
		String userName = AppData.getUserName(getTaskFace().getMeContext());
		for (GameListCurrentItem currentItem : itemList) {

			arguments[0] = String.valueOf(userName);
			arguments[1] = String.valueOf(currentItem.getGameId());

			Uri uri = DBConstants.ECHESS_CURRENT_LIST_GAMES_CONTENT_URI;
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
					DBDataManager.SELECTION_GAME_ID, arguments, null);
			if (cursor.moveToFirst()) {
				contentResolver.update(Uri.parse(uri.toString() + DBDataManager.SLASH_ + DBDataManager.getId(cursor)),
						DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName), null, null);
			} else {
				contentResolver.insert(uri, DBDataManager.putEchessGameListCurrentItemToValues(currentItem, userName));
			}

			cursor.close();

			updateOnlineGame(currentItem.getGameId(), userName);
		}

        result = StaticData.RESULT_OK;

        return result;
    }

	private void updateOnlineGame(long gameId, String userName) {
		loadItem.setLoadPath(RestHelper.GET_GAME_V5);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getTaskFace().getMeContext()));
		loadItem.addRequestParams(RestHelper.P_GID, gameId);

		GameOnlineItem currentGame = getData(RestHelper.formCustomRequest(loadItem));
		if (currentGame != null) {
//			ContentValues values = DBDataManager.putGameOnlineItemToValues(currentGame, userName);
			DBDataManager.updateOnlineGame(contentResolver, currentGame, userName);
		}

	}

	private GameOnlineItem getData(String url) {
		Log.d(TAG, "retrieving from url = " + url);

		long tag = System.currentTimeMillis();
		BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_REQUEST, "tag=" + tag + " " + url);
		HttpURLConnection connection = null;
		try {
			URL urlObj = new URL(url);
			connection = (HttpURLConnection) urlObj.openConnection();

			final int statusCode = connection.getResponseCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);

				result = StaticData.UNKNOWN_ERROR;
				return null;
			}

			String returnedObj = convertStreamToString(connection.getInputStream());

			result = StaticData.RESULT_OK;
			Log.d(TAG, "WebRequest SERVER RESPONSE: " + item);
			BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_RESPONSE, "tag=" + tag + " " + item);

			return ChessComApiParser.getGameParseV3(returnedObj);
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
//			httpClient.getConnectionManager().shutdown();
			if (connection != null) {
				connection.disconnect();
			}
		}


		return null;
	}
}
