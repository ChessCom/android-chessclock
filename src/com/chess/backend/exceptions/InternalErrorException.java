package com.chess.backend.exceptions;

import android.util.Log;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.02.13
 * Time: 10:05
 */
public class InternalErrorException extends IOException {

	private static final String TAG = "InternalErrorException";
	private int code;

	public InternalErrorException(int code) {
		this.code = code;
	}

	public InternalErrorException(Throwable cause, int code) {
		super(cause.getMessage());
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void logMe(){
		Log.e(TAG, " requestData return code " + getCode() + " trace:" + getMessage());
	}
}
