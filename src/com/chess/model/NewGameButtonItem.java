package com.chess.model;

import android.content.Context;
import com.chess.R;

/**
 * NewGameButtonItem class
 *
 * @author alien_roger
 * @created at: 22.07.12 8:00
 */
public class NewGameButtonItem {

	private int min;
	private int sec;
	private String label;
	private static final String PAIR_DIVIDER = " | ";

	public static NewGameButtonItem createNewButtonFromLabel(String label, Context context){
		NewGameButtonItem  buttonItem = new NewGameButtonItem();
		if(label.contains(PAIR_DIVIDER)){
			// "5 | 2"),
			String[] params = label.split(PAIR_DIVIDER);
			buttonItem.min = Integer.valueOf(params[0]);
			buttonItem.sec = Integer.valueOf(params[2]);
		} else {
			// "10 min"),
			buttonItem.min = Integer.valueOf(label);
		}
		buttonItem.label = context.getString(R.string.min_, label);
		return buttonItem;
	}

	public int getMin() {
		return min;
	}

	public int getSec() {
		return sec;
	}

	public String getLabel() {
		return label;
	}
}
