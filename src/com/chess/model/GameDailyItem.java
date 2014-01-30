package com.chess.model;

import android.os.Parcel;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.14
 * Time: 9:30
 */
public class GameDailyItem extends BaseGameItem {

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		writeBaseGameParcel(parcel);
	}
}
