package com.chess.model;

import android.graphics.drawable.Drawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.02.13
 * Time: 10:44
 */
public class RatingListItem extends SelectionItem {
	private int value;

	public RatingListItem(Drawable image, String text) {
		super(image, text);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
