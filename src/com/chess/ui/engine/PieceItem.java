package com.chess.ui.engine;

/**
 * PieceItem class
 *
 * @author alien_roger
 * @created at: 08.03.12 8:00
 */
public class PieceItem {

	private int code;


	private String stringCode;
	private boolean isWhite;

	private int layersCnt;

	/*
	* Used to find piece in captured piece layout
	* */
	private int pieceId;
	private int pieceFrameId;

	public boolean isCaptured() {
		return isCaptured;
	}

	public void setCaptured(boolean captured) {
		isCaptured = captured;
	}

	private boolean isCaptured;


	private int currentLevel;


	public String getStringCode() {
		return stringCode;
	}

	public void setStringCode(String stringCode) {
		this.stringCode = stringCode;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		setCaptured(true);
		this.code = code;
	}

	public boolean isWhite() {
		return isWhite;
	}

	public void setWhite(boolean white) {
		isWhite = white;
	}

	public int getPieceId() {
		return pieceId;
	}

	public void setPieceId(int pieceId) {
		this.pieceId = pieceId;
	}

	public int getPieceFrameId() {
		return pieceFrameId;
	}

	public void setPieceFrameId(int pieceFrameId) {
		this.pieceFrameId = pieceFrameId;
	}

	public int getLayersCnt() {
		return layersCnt;
	}

	public void setLayersCnt(int layersCnt) {
		this.layersCnt = layersCnt;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}
}
