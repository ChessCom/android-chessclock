package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.game.GameBaseFragment;

public class GameAnalysisItem implements Parcelable {

	private String fen;
	private int gameType;
	private String movesList;
	private String topPlayerName;
	private String bottomPlayerName;
	private String topPlayerRating;
	private String bottomPlayerRating;
	private String topPlayerAvatar;
	private String bottomPlayerAvatar;
	private String topPlayerCountry;
	private String bottomPlayerCountry;
	private int topPlayerPremiumStatus;
	private int bottomPlayerPremiumStatus;
	private int userSide;
	private boolean isFinished;

	public GameAnalysisItem() {
	}

	public String getFen() {
		return fen;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public int getGameType() {
		return gameType;
	}

	/**
	 * Can be Classic chess or chess960
	 */
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public String getMovesList() {
		return movesList = movesList != null? movesList : Symbol.EMPTY;
	}

	public void setMovesList(String movesList) {
		this.movesList = movesList;
	}

	public String getTopPlayerName() {
		return topPlayerName;
	}

	public void setTopPlayerName(String topPlayerName) {
		this.topPlayerName = topPlayerName;
	}

	public String getBottomPlayerName() {
		return bottomPlayerName;
	}

	public void setBottomPlayerName(String bottomPlayerName) {
		this.bottomPlayerName = bottomPlayerName;
	}

	public String getTopPlayerRating() {
		return topPlayerRating;
	}

	public void setTopPlayerRating(String topPlayerRating) {
		this.topPlayerRating = topPlayerRating;
	}

	public String getBottomPlayerRating() {
		return bottomPlayerRating;
	}

	public void setBottomPlayerRating(String bottomPlayerRating) {
		this.bottomPlayerRating = bottomPlayerRating;
	}

	public String getTopPlayerAvatar() {
		return topPlayerAvatar;
	}

	public void setTopPlayerAvatar(String topPlayerAvatar) {
		this.topPlayerAvatar = topPlayerAvatar;
	}

	public String getBottomPlayerAvatar() {
		return bottomPlayerAvatar;
	}

	public void setBottomPlayerAvatar(String bottomPlayerAvatar) {
		this.bottomPlayerAvatar = bottomPlayerAvatar;
	}

	public String getTopPlayerCountry() {
		return topPlayerCountry;
	}

	public void setTopPlayerCountry(String topPlayerCountry) {
		this.topPlayerCountry = topPlayerCountry;
	}

	public String getBottomPlayerCountry() {
		return bottomPlayerCountry;
	}

	public void setBottomPlayerCountry(String bottomPlayerCountry) {
		this.bottomPlayerCountry = bottomPlayerCountry;
	}

	public int getTopPlayerPremiumStatus() {
		return topPlayerPremiumStatus;
	}

	public void setTopPlayerPremiumStatus(int topPlayerPremiumStatus) {
		this.topPlayerPremiumStatus = topPlayerPremiumStatus;
	}

	public int getBottomPlayerPremiumStatus() {
		return bottomPlayerPremiumStatus;
	}

	public void setBottomPlayerPremiumStatus(int bottomPlayerPremiumStatus) {
		this.bottomPlayerPremiumStatus = bottomPlayerPremiumStatus;
	}

	public int getUserSide() {
		return userSide;
	}

	public void setUserSide(int userSide) {
		this.userSide = userSide;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public void copyLabelConfig(GameBaseFragment.LabelsConfig labelsConfig) {
		topPlayerName = labelsConfig.topPlayerName;
		bottomPlayerName = labelsConfig.bottomPlayerName;
		topPlayerRating = labelsConfig.topPlayerRating;
		bottomPlayerRating = labelsConfig.bottomPlayerRating;
		topPlayerAvatar = labelsConfig.topPlayerAvatar;
		bottomPlayerAvatar = labelsConfig.bottomPlayerAvatar;
		topPlayerCountry = labelsConfig.topPlayerCountry;
		bottomPlayerCountry = labelsConfig.bottomPlayerCountry;
		topPlayerPremiumStatus = labelsConfig.topPlayerPremiumStatus;
		bottomPlayerPremiumStatus = labelsConfig.bottomPlayerPremiumStatus;
		userSide = labelsConfig.userSide;
	}

	public void fillLabelsConfig(GameBaseFragment.LabelsConfig labelsConfig) {
		labelsConfig.topPlayerName = topPlayerName;
		labelsConfig.bottomPlayerName = bottomPlayerName;
		labelsConfig.topPlayerRating = topPlayerRating;
		labelsConfig.bottomPlayerRating = bottomPlayerRating;
		labelsConfig.topPlayerAvatar = topPlayerAvatar;
		labelsConfig.bottomPlayerAvatar = bottomPlayerAvatar;
		labelsConfig.topPlayerCountry = topPlayerCountry;
		labelsConfig.bottomPlayerCountry = bottomPlayerCountry;
		labelsConfig.topPlayerPremiumStatus = topPlayerPremiumStatus;
		labelsConfig.bottomPlayerPremiumStatus = bottomPlayerPremiumStatus;
		labelsConfig.userSide = userSide;
	}


	protected GameAnalysisItem(Parcel in) {
		fen = in.readString();
		gameType = in.readInt();
		movesList = in.readString();
		topPlayerName = in.readString();
		bottomPlayerName = in.readString();
		topPlayerRating = in.readString();
		bottomPlayerRating = in.readString();
		topPlayerAvatar = in.readString();
		bottomPlayerAvatar = in.readString();
		topPlayerCountry = in.readString();
		bottomPlayerCountry = in.readString();
		topPlayerPremiumStatus = in.readInt();
		bottomPlayerPremiumStatus = in.readInt();
		userSide = in.readInt();
		isFinished = in.readByte() != 0x00;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(fen);
		dest.writeInt(gameType);
		dest.writeString(movesList);
		dest.writeString(topPlayerName);
		dest.writeString(bottomPlayerName);
		dest.writeString(topPlayerRating);
		dest.writeString(bottomPlayerRating);
		dest.writeString(topPlayerAvatar);
		dest.writeString(bottomPlayerAvatar);
		dest.writeString(topPlayerCountry);
		dest.writeString(bottomPlayerCountry);
		dest.writeInt(topPlayerPremiumStatus);
		dest.writeInt(bottomPlayerPremiumStatus);
		dest.writeInt(userSide);
		dest.writeByte((byte) (isFinished ? 0x01 : 0x00));
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<GameAnalysisItem> CREATOR = new Parcelable.Creator<GameAnalysisItem>() {
		@Override
		public GameAnalysisItem createFromParcel(Parcel in) {
			return new GameAnalysisItem(in);
		}

		@Override
		public GameAnalysisItem[] newArray(int size) {
			return new GameAnalysisItem[size];
		}
	};
}