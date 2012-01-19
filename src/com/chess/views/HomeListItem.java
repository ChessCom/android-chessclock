package com.chess.views;

import android.graphics.drawable.Drawable;

public class HomeListItem {
	private String text;
	private Drawable image;

	public Drawable getImage() {
		return image;
	}

	public void setImage(Drawable image) {
		this.image = image;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
