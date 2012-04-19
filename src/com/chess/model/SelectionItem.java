package com.chess.model;

import android.graphics.drawable.Drawable;

public class SelectionItem {
	public Drawable image;
	public String text = "";

	public SelectionItem(Drawable image, String text) {
		this.image = image;
		this.text = text;
	}
}
