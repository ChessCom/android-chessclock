package com.chess.ui.interfaces;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.02.13
 * Time: 13:14
 */
public interface GameNetworkActivityFace extends GameActivityFace{

	void showSubmitButtonsLay(boolean show);

	void switch2Chat();

	void playMove();

	void cancelMove();

}
