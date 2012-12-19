package com.chess.backend.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * CheckUpdateTask class
 *
 * @author alien_roger
 * @created at: 14.03.12 5:35
 */
public class CheckUpdateTask extends AbstractUpdateTask<Boolean, String> {

	private SharedPreferences.Editor preferencesEditor;
	private Context context;
	private static final String TAG = "CheckUpdateTask";

	public CheckUpdateTask(TaskUpdateInterface<Boolean> taskFace) {
		super(taskFace);
		try {
			context = getTaskFace().getMeContext();
			preferencesEditor = AppData.getPreferences(getTaskFace().getMeContext()).edit();
		} catch (IllegalStateException ex) {
			cancel(true);
			Log.e("CheckUpdateTask", ex.toString());
		}
	}

	@Override
	protected Integer doTheTask(String... urls) {
		item = false;  // forceFlag field
		Log.d(TAG, "retrieving from url = " + urls[0]);
		try {
			URL updateURL = new URL(urls[0]);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			final String s = new String(baf.toByteArray());
			String[] valuesArray = s.trim().split("\\|", 2);

			int actualVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			int minimumVersion = Integer.valueOf(valuesArray[0].trim());
			int preferredVersion = Integer.valueOf(valuesArray[1].trim());

			if (actualVersion < preferredVersion) {
				preferencesEditor.putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false);
				result = StaticData.RESULT_OK;
			} else {
				result = StaticData.DATA_EXIST;
			}

			if (actualVersion < minimumVersion) {
				item = true; // need to force update
				// drop start day
				preferencesEditor.putLong(AppConstants.START_DAY, 0);
			} else {
				// Save last update time to prevent update on every home screen resume
				Log.d("CheckUpdateTask","START_DAY saved at =" + System.currentTimeMillis());
				preferencesEditor.putLong(AppConstants.START_DAY, System.currentTimeMillis());
			}
			preferencesEditor.commit();

		} catch (Exception e) {
			result = StaticData.UNKNOWN_ERROR;
			e.printStackTrace();
			Log.d(TAG, e.toString());
			return result;
		}

		return result;
	}

}
