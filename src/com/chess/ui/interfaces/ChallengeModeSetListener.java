package com.chess.ui.interfaces;

import android.content.Context;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.12.13
 * Time: 6:39
 */
public interface ChallengeModeSetListener {

	Context getMeContext();

	void setDefaultLiveTimeMode(int mode);

	void setDefaultDailyTimeMode(int mode);
}
