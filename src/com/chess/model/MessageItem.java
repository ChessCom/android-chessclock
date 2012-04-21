package com.chess.model;

import com.chess.ui.core.AppConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MessageItem {
	public String owner = AppConstants.SYMBOL_EMPTY;
	public String message = AppConstants.SYMBOL_EMPTY;

	public MessageItem(String owner, String msg) {
		this.owner = owner;
		try {
			this.message = URLDecoder.decode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
