package com.chess.lcc.android;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.05.13
 * Time: 6:52
 */
public class DataNotValidException extends IOException {

	public static final String SERVICE_NULL = "Service has become NULL";
	public static final String LCC_HELPER_NULL = "LccHelper has become NULL, we leaved or signed out";
	public static final String USER_NULL = "User has become NULL";
	public static final String NOT_CONNECTED = "lcc is not connected";
	public static final String GAME_NOT_EXIST = "live game doesn't exist";

	public DataNotValidException(String message) {
		super(message);

	}
}
