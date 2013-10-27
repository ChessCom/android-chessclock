package com.chess.backend;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.10.13
 * Time: 8:24
 */
public class GetAndSaveTheme extends IntentService {

	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 * Use name the worker thread, important only for debugging.
	 */
	public GetAndSaveTheme() {
		super("GetAndSaveTheme");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}
}
