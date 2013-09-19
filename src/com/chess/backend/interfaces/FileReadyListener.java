package com.chess.backend.interfaces;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.09.13
 * Time: 10:29
 */
public interface FileReadyListener {

	void changeTitle(final String title);

	/**
	 * @param progress in percent of downloaded value
	 */
	void setProgress(final int progress);
}
