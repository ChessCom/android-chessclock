package com.chess.model;

import android.graphics.drawable.Drawable;
import com.chess.ui.core.AppConstants;

public class SelectionItem {
	public Drawable image;
	public String text = AppConstants.SYMBOL_EMPTY;

	public SelectionItem(Drawable image, String text) {
		this.image = image;
		this.text = text;
	}
}
