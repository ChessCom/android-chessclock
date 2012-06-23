package com.chess.backend.tasks;

import android.content.Context;
import android.content.SharedPreferences;
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
public class CheckUpdateTask2 extends AbstractUpdateTask<Boolean, String> {

	private SharedPreferences.Editor preferencesEditor;
	private Context context;

	public CheckUpdateTask2(TaskUpdateInterface<Boolean> taskFace) {
		super(taskFace);
		context = taskFace.getMeContext();
		preferencesEditor = AppData.getPreferences(taskFace.getMeContext()).edit();
	}

	@Override
	protected Integer doTheTask(String... urls) {
		item = null;  // forceFlag field
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
				item = false;
//				if (item != null) {

					preferencesEditor.putLong(AppConstants.START_DAY, System.currentTimeMillis());
					preferencesEditor.putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false);
					preferencesEditor.commit();
//				}
			}
			if (actualVersion < minimumVersion) {
				item = true; // need to force update
			}

			result = StaticData.RESULT_OK;
		} catch (Exception e) {
			result = StaticData.UNKNOWN_ERROR;
			return result;
		}

//		if (item != null && !item) {
//		if (item != null) {
//
//			preferencesEditor.putLong(AppConstants.START_DAY, System.currentTimeMillis());
//			preferencesEditor.putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false);
//			preferencesEditor.commit();
//		}
		return result;
	}

}
