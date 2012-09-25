package com.chess.model;

import com.chess.ui.interfaces.BoardFace;

/**
 * ComputeMoveItem class
 *
 * @author alien_roger
 * @created at: 25.09.12 5:36
 */
public class ComputeMoveItem {
	private int moveTime;
	private int[] pieces_tmp;
	private int[] colors_tmp;
	private BoardFace boardFace;

	public BoardFace getBoardFace() {
		return boardFace;
	}

	public void setBoardFace(BoardFace boardFace) {
		this.boardFace = boardFace;
	}

	public int[] getColors_tmp() {
		return colors_tmp;
	}

	public void setColors_tmp(int[] colors_tmp) {
		this.colors_tmp = colors_tmp;
	}

	public int getMoveTime() {
		return moveTime;
	}

	public void setMoveTime(int moveTime) {
		this.moveTime = moveTime;
	}

	public int[] getPieces_tmp() {
		return pieces_tmp;
	}

	public void setPieces_tmp(int[] pieces_tmp) {
		this.pieces_tmp = pieces_tmp;
	}

}
