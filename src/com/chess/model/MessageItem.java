package com.chess.model;

import com.chess.statics.Symbol;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MessageItem {
	public String owner = Symbol.EMPTY;
	public String message = Symbol.EMPTY;

	public MessageItem(String owner, String msg) {
		this.owner = owner;
		try {
			this.message = URLDecoder.decode(msg, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
