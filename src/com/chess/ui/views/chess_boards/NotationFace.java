package com.chess.ui.views.chess_boards;

import com.chess.ui.views.NotationsView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.11.13
 * Time: 6:24
 */
public interface NotationFace {

	void moveBack(int ply);

	void moveForward(int ply);

	void updateNotations(String[] notations, NotationsView.BoardForNotationFace updateFace, int ply);

	void rewindBack();

	void rewindForward();

	void resetNotations();

	void setClickable(boolean active);
}
