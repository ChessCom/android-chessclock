package com.chess.backend.entity;

/**
 * GSMServerResponseItem class
 *
 * @author alien_roger
 * @created at: 19.09.12 23:55
 */
public class GSMServerResponseItem {
	/*
	"status":false,"code":401,"message":"Bad request"
	 */
	private boolean status;
	private int code;
	private String message;

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean isStatus() {
		return status;
	}
}
