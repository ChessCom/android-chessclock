package com.chess.backend.entity;

/**
 * GCMServerResponseItem class
 *
 * @author alien_roger
 * @created at: 19.09.12 23:55
 */
public class GCMServerResponseItem {
	/*
	"status":false,"code":401,"message":"Bad request"
	 */
	private boolean status;
	private int code;
	private String message;

    public static GCMServerResponseItem createFailResponse(){
        GCMServerResponseItem responseItem = new GCMServerResponseItem();
        responseItem.code = 404;
        return responseItem;
    }

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
