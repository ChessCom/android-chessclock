package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.backend.RestHelper;

public class GameListCurrentItem extends BaseGameOnlineItem {

	public GameListCurrentItem(String[] values) {
		super(values);
		isMyTurn = values[15].equals(RestHelper.V_ONE);
		hasNewMessage = values[16].equals(RestHelper.V_ONE);
	}

	public GameListCurrentItem() {
	}

	public static GameListCurrentItem newInstance(String[] gcmMessageInfo){
		GameListCurrentItem gameListCurrentItem = new GameListCurrentItem();
		gameListCurrentItem.gameId = Long.parseLong(gcmMessageInfo[0]);
		gameListCurrentItem.timeRemainingAmount = Integer.parseInt(gcmMessageInfo[1]);
		gameListCurrentItem.timeRemainingUnits = gcmMessageInfo[2];
		return gameListCurrentItem;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		writeBaseGameParcel(parcel);

		writeBaseGameOnlineParcel(parcel);
	}



	public static final Parcelable.Creator<GameListCurrentItem> CREATOR = new Parcelable.Creator<GameListCurrentItem>() {
		@Override
		public GameListCurrentItem createFromParcel(Parcel in) {
			return new GameListCurrentItem(in);
		}

		@Override
		public GameListCurrentItem[] newArray(int size) {
			return new GameListCurrentItem[size];
		}
	};

	private GameListCurrentItem(Parcel in) {
		readBaseGameParcel(in);

		readBaseGameOnlineParcel(in);
	}

}
