package com.chess.model;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 23.07.13
 * Time: 14:24
 */
public class CompEngineItem {

	private int gameMode;
	private int strength;
	private int time;
	private int depth;
	private boolean restoreGame;
	private String fen;

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getFen() {
		return fen;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public int getGameMode() {
		return gameMode;
	}

	public void setGameMode(int gameMode) {
		this.gameMode = gameMode;
	}

	public boolean isRestoreGame() {
		return restoreGame;
	}

	public void setRestoreGame(boolean restoreGame) {
		this.restoreGame = restoreGame;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
}
