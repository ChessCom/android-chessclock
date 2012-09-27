package com.chess;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.io.Serializable;

/**
 * SerialLinLay class
 *
 * @author alien_roger
 * @created at: 18.07.12 6:43
 */
public class SerialLinLay extends LinearLayout implements Serializable{

	private static final long serialVersionUID = -8464648125307185315L;

	public SerialLinLay(Context context) {
		super(context);
	}

	public SerialLinLay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SerialLinLay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
}
