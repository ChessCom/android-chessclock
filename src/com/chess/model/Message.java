package com.chess.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Message {
	public String owner = "", message = "";

	public Message(String owner, String msg) {
		this.owner = owner;
		try {
			this.message = URLDecoder.decode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
