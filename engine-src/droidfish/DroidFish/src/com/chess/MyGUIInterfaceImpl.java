package com.chess;

import android.content.Context;
import org.petero.droidfish.GUIInterface;
import org.petero.droidfish.Util;
import org.petero.droidfish.gamelogic.Move;
import org.petero.droidfish.gamelogic.Position;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 19.03.13
 * Time: 23:28
 */
public class MyGUIInterfaceImpl implements GUIInterface {
	public void setPosition(Position pos, String variantInfo, ArrayList<Move> variantMoves) {
	}

	public void setSelection(int sq) {
	}

	public void setStatus(GameStatus status) {
	}

	public void moveListUpdated() {
	}

	public void setThinkingInfo(String pvStr, String statStr, String bookInfo, ArrayList<ArrayList<Move>> pvMoves, ArrayList<Move> bookMoves) {
	}

	public void requestPromotePiece() {
	}

	public void runOnUIThread(Runnable runnable) {
	}

	public void reportInvalidMove(Move m) {
	}

	public void reportEngineName(String engine) {
	}

	public void reportEngineError(String errMsg) {
	}

	public void computerMoveMade() {
	}

	@Override
	public void setRemainingTime(int wTime, int bTime, int nextUpdate) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void setRemainingTime(long wTime, long bTime, long nextUpdate) {
	}

	public void updateEngineTitle() {
	}

	public void updateMaterialDifferenceTitle(Util.MaterialDiff diff) {
	}

	@Override
	public void updateTimeControlTitle() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void setAnimMove(Position sourcePos, Move move, boolean forward) {
	}

	public boolean whiteBasedScores() {
		return false; // pref
	}

	public boolean ponderMode() {
		return true; // pref
	}

	public int engineThreads() {
		return 1; // pref
	}

	public Context getContext() {
		return getContext();
	}

	public String playerName() {
		return "chess.com-player";
	}

	public boolean discardVariations() {
		return false; // pref
	}

	// todo: rename createDirectories Fish directories
	// todo: check app pref defaults
	// todo: check which context to use - optimization
	// todo: use GameMode - w, b, human, comp, analysis
	// todo: remove not used code of engine project, tune proguard to exclude unused code

	// Features:
	// use open books
	// pondering
}
