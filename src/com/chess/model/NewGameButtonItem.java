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
//	private static final String SINGLE_DIVIDER = " ";

	public static NewGameButtonItem createNewButtonFromLabel(String label, Context context){
		NewGameButtonItem  buttonItem = new NewGameButtonItem();
		if(label.contains(PAIR_DIVIDER)){
			// "5 | 2"),
			String[] params = label.split(PAIR_DIVIDER);
			buttonItem.min = Integer.valueOf(params[0]);
			buttonItem.sec = Integer.valueOf(params[2]);
		} else {
			// "10 min"),
//			String[] params = label.split(SINGLE_DIVIDER);
//			buttonItem.min = Integer.valueOf(params[0].trim());
			buttonItem.min = Integer.valueOf(label);
		}
		buttonItem.label = context.getString(R.string.min_, label);
		return buttonItem;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getSec() {
		return sec;
	}

	public void setSec(int sec) {
		this.sec = sec;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
