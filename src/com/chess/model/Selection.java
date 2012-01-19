package com.chess.model;

import android.graphics.drawable.Drawable;

public class Selection {
	public Drawable image;
	public String text = "";

	public Selection(Drawable image, String text) {
		this.image = image;
		this.text = text;
	}
}
