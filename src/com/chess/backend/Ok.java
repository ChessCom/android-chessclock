package com.chess.backend;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.09.13
 * Time: 20:21
 */
public class Ok {
	private static Ok ourInstance = new Ok();

	public static Ok getInstance() {
		return ourInstance;
	}

	private Ok() {
	}
}
