package com.chess.model;

import android.graphics.drawable.Drawable;
import com.chess.backend.statics.StaticData;

public class SelectionItem {
	public Drawable image;
	public String text = StaticData.SYMBOL_EMPTY;

	public SelectionItem(Drawable image, String text) {
		this.image = image;
		this.text = text;
	}
}
