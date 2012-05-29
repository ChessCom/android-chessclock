package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.chess.R;

/**
 * ChessSpinnerAdapter class
 *
 * @author alien_roger
 * @created at: 24.02.12 5:22
 */
public class ChessSpinnerAdapter extends ArrayAdapter<String> {

	private Context context;

	public ChessSpinnerAdapter(Context context, int entries) {
		super(context, android.R.layout.simple_spinner_item,
				context.getResources().getStringArray(entries));
		setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		this.context = context;
	}

	public ChessSpinnerAdapter(Context context, String[] entries) {
		super(context, android.R.layout.simple_spinner_item, entries);
		setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		((TextView) v.findViewById(android.R.id.text1)).setTextColor(context.getResources().getColor(R.color.hint_text));
		return v;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View v = super.getDropDownView(position, convertView, parent);
		((TextView) v.findViewById(android.R.id.text1)).setTextColor(context.getResources().getColor(R.color.black));
		return v;
	}
}

