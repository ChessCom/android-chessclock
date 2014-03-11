package com.chess.utilities;

import android.content.Context;
import android.util.Log;
import com.chess.statics.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 24.10.13
 * Time: 17:25
 */
public class LogMe {

	public static final boolean ENABLED = false; // use it only for builds that need it

	private static final Logger LOG = LoggerFactory.getLogger(LogMe.class); // todo: check class

	private static final String DEBUG_LIVE_TAG = "LCCLOG";
	private static final String TAG_MESSAGE_SEPARATOR = ": ";

	private static final Marker markerNotify = MarkerFactory.getMarker("NOTIFY");

	public static void debugAndMail(String tag, String message, Context context) {
		if (ENABLED) {
			forceMail(tag, message, context);
		} else {
			Log.d(tag, message);
		}
	}

	public static void forceMail(String tag, String message, Context context) {
		if (context != null && AppUtils.isNetworkAvailable(context)) {
			LOG.debug(markerNotify, tag + TAG_MESSAGE_SEPARATOR + message); // markerNotify sends mail buffer immediately
		} else {
			dl(tag, message);
		}
	}

	public static void dl(String tag, String message) {
		if (message == null) { // sometimes it happens...
			return;
		}
		if (ENABLED) {
			LOG.debug(tag + TAG_MESSAGE_SEPARATOR + message);
		} else {
			Log.d(tag, message);
		}
	}

	public static void dl(String message) {
		if (message == null) { // sometimes it happens...
			return;
		}
		if (ENABLED) {
			LOG.debug(DEBUG_LIVE_TAG + TAG_MESSAGE_SEPARATOR + message);
		} else {
			Log.d(DEBUG_LIVE_TAG, message);
		}
	}

	public static void dl(String tag, Throwable throwable) {
		if (ENABLED) {
			LOG.debug(tag, throwable);
		} else {
			Log.d(DEBUG_LIVE_TAG, Symbol.EMPTY, throwable);
		}
	}
}