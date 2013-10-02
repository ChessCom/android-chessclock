package com.chess.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 15:31
 */
public class GameDiagramItem extends GameAnalysisItem {

	public static final String TYPE = "&-diagramtype:";
	public static final String FEN_CODE = "[FEN \"";
	public static final String END_PART = "\"]";

/*
	///////////////////////////
	// simpleDiagram Example //
	///////////////////////////

	&-diagramtype: simpleDiagram
	&-colorscheme: wooddark
	&-piecestyle: book
	&-float: left
	&-flip: false
	&-prompt: false
	&-coords: false
	&-size: 45
	&-lastmove:
	&-focusnode:
	&-beginnode:
	&-endnode:
	&-pgnbody:
	[Date "????.??.??"]
	[Result "*"]
	[FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"] *

	//////////////////////////
	// chessProblem Example //
	//////////////////////////
	&-diagramtype: chessProblem
	&-colorscheme: blue
	&-piecestyle: classic
	&-float: left
	&-flip: false
	&-prompt: false
	&-coords: false
	&-size: 45
	&-lastmove:
	&-focusnode:
	&-beginnode: 47
	&-endnode:
	&-pgnbody:
	[Event "Berlin 'Evergreen'"]
	[Site "?"]
	[Date "1852.??.??"]
	[Round "?"]
	[White "Anderssen, A"]
	[Black "Dufresne, J"]
	[Result "1-0"]
	[Plycount "47"]
	[Opening "Evans G"]
	1.e4 e5 2.Nf3 Nc6 3.Bc4 Bc5 4.b4 Bxb4 5.c3 Ba5 6.d4 exd4 7.O-O d3 8.Qb3 Qf6 9.e5
	Qg6 10.Re1 Nge7 11.Ba3 b5 12.Qxb5 Rb8 13.Qa4 Bb6 14.Nbd2 Bb7 15.Ne4 Qf5 16.Bxd3
	Qh5 17.Nf6+ gxf6 18.exf6 Rg8 19.Rad1 Qxf3 20.Rxe7+ Nxe7 21.Qxd7+ Kxd7 22.Bf5+ Ke8 23.Bd7+ Kf8 24.Bxe7# 1-0
	 */

	private String diagramType;
	private String moves;
	private String whitePlayerName;
	private String blackPlayerName;
	private String gameResult;
	private String colorScheme;
	private String date;
	private String eventName;
	private boolean showAnimation = true;

	public GameDiagramItem() {
	}

	public String getDiagramType() {
		return diagramType;
	}

	public void setDiagramType(String diagramType) {
		this.diagramType = diagramType;
	}

	public String getMoves() {
		return moves;
	}

	public void setMoves(String moves) {
		this.moves = moves;
	}

	public String getWhitePlayerName() {
		return whitePlayerName;
	}

	public void setWhitePlayerName(String whitePlayerName) {
		this.whitePlayerName = whitePlayerName;
	}

	public String getBlackPlayerName() {
		return blackPlayerName;
	}

	public void setBlackPlayerName(String blackPlayerName) {
		this.blackPlayerName = blackPlayerName;
	}

	public String getGameResult() {
		return gameResult;
	}

	public void setGameResult(String gameResult) {
		this.gameResult = gameResult;
	}

	public String getColorScheme() {
		return colorScheme;
	}

	public void setColorScheme(String colorScheme) {
		this.colorScheme = colorScheme;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public boolean isShowAnimation() {
		return showAnimation;
	}

	public void setShowAnimation(boolean showAnimation) {
		this.showAnimation = showAnimation;
	}


	protected GameDiagramItem(Parcel in) {
		super(in);
		diagramType = in.readString();
		moves = in.readString();
		whitePlayerName = in.readString();
		blackPlayerName = in.readString();
		gameResult = in.readString();
		colorScheme = in.readString();
		date = in.readString();
		eventName = in.readString();
		showAnimation = in.readByte() != 0x00;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(diagramType);
		dest.writeString(moves);
		dest.writeString(whitePlayerName);
		dest.writeString(blackPlayerName);
		dest.writeString(gameResult);
		dest.writeString(colorScheme);
		dest.writeString(date);
		dest.writeString(eventName);
		dest.writeByte((byte) (showAnimation ? 0x01 : 0x00));
	}

	public static final Parcelable.Creator<GameDiagramItem> CREATOR = new Parcelable.Creator<GameDiagramItem>() {
		@Override
		public GameDiagramItem createFromParcel(Parcel in) {
			return new GameDiagramItem(in);
		}

		@Override
		public GameDiagramItem[] newArray(int size) {
			return new GameDiagramItem[size];
		}
	};
}
