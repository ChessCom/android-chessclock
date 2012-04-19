package com.chess.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MessageItem {
	public String owner = "";
	public String message = "";

	public MessageItem(String owner, String msg) {
		this.owner = owner;
		try {
			this.message = URLDecoder.decode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
