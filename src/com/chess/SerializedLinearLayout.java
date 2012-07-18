package com.chess;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.io.Serializable;

/**
 * SerializedLinearLayout class
 *
 * @author alien_roger
 * @created at: 18.07.12 6:43
 */
public class SerializedLinearLayout extends LinearLayout implements Serializable{

	public SerializedLinearLayout(Context context) {
		super(context);
	}

	public SerializedLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SerializedLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
}
