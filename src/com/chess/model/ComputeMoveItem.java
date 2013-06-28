package com.chess.model;

import com.chess.ui.interfaces.BoardFace;

/**
 * ComputeMoveItem class
 *
 * @author alien_roger
 * @created at: 25.09.12 5:36
 */
public class ComputeMoveItem {

	private String move;
	private BoardFace boardFace;
	//private boolean force;

	public BoardFace getBoardFace() {
		return boardFace;
	}

	public void setBoardFace(BoardFace boardFace) {
		this.boardFace = boardFace;
	}

	public String getMove() {
		return move;
	}

	public void setMove(String move) {
		this.move = move;
	}

	/*public void setForce(boolean force) {
		this.force = force;
	}

	public boolean isForce() {
		return force;
	}*/
}
