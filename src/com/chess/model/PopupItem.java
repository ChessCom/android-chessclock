package com.chess.model;

import com.chess.R;

/**
 * PopupItem class
 *
 * @author alien_roger
 * @created at: 07.04.12 7:14
 */
public class PopupItem {
	private int titleId;
	private int messageId;
	private String title;
	private String message;
	private int leftBtnId;
	private int rightBtnId;

	public PopupItem() {
		this.leftBtnId = R.string.ok;
		this.rightBtnId = R.string.cancel;
	}

	public int getLeftBtnId() {
		return leftBtnId;
	}

	public void setLeftBtnId(int leftBtnId) {
		this.leftBtnId = leftBtnId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getRightBtnId() {
		return rightBtnId;
	}

	public void setRightBtnId(int rightBtnId) {
		this.rightBtnId = rightBtnId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTitleId() {
		return titleId;
	}

	public void setTitle(int titleId) {
		this.titleId = titleId;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessage(int messageId) {
		this.messageId = messageId;
	}
}
