package com.chess.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 24.10.13
 * Time: 17:25
 */
public class LogMe {

	private static final Logger LOG = LoggerFactory.getLogger(LogMe.class); // todo: check class

	private static final String DEBUG_LIVE_TAG = "LCCLOG";
	private static final String TAG_MESSAGE_SEPARATOR = ": ";

	public static void dl(String tag, String message) {
		LOG.debug(tag + TAG_MESSAGE_SEPARATOR + message);
	}

	public static void dl(String message) {
		LOG.debug(DEBUG_LIVE_TAG + TAG_MESSAGE_SEPARATOR + message);
	}
}
