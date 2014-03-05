package com.chess.utilities;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.chess.statics.FlurryData;
import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstraction layer for deploy tools
 * <p/>
 * Created by electrolobzik (electrolobzik@gmail.com) on 02/03/2014.
 */
public class MonitorDataHelper { // TODO could be renamed to something better

	// class is static
	private MonitorDataHelper() {
	}

	/**
	 * Saves value of flag to send with crash
	 *
	 * @param flagName name of the flag, not null
	 * @param value    flag value, may be null
	 */
	public static void setFlagValue(String flagName, String value) {

		BugSenseHandler.addCrashExtraData(flagName, value);
	}

	/**
	 * Logs and sends exception
	 *
	 * @param exception logging exception
	 */
	public static void logException(Exception exception) {

		BugSenseHandler.sendException(exception);
	}

	/**
	 * Can be used to initialize any monitoring/crash-report library
	 * Must be called in onCreate method of base/main activity of the application
	 * @param context for init
	 */
	public static void initMonitorLib(Context context) {
		Crittercism.initialize(context, AppConstants.CRITTERCISM_APP_ID);

		// TODO probably will be replaced by crittercism lib, because it have crash report system too
		// Bugsense integration
		try {
			BugSenseHandler.initAndStartSession(context, AppConstants.BUGSENSE_API_KEY);
		} catch (Exception e) {
			e.printStackTrace();
			String stackTrace = Log.getStackTraceString(e).replaceAll("\n", " ");
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.EXCEPTION, Build.MODEL + " " + stackTrace);
			FlurryAgent.logEvent(FlurryData.BUGSENSE_INIT_EXCEPTION, params);
		}
	}

	/**
	 * Add user information for tracking/monitoring system
	 * @param context to get appData
	 */
	public static void initUser(Context context) {
		AppData appData = new AppData(context);

		// instantiate metadata json object
		JSONObject metadata = new JSONObject();
		// add arbitrary metadata
		try {
			metadata.put("user_id", appData.getUserId());
			metadata.put("name", appData.getUsername());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// send metadata to crittercism (asynchronously)
		Crittercism.setMetadata(metadata);

	}
}
