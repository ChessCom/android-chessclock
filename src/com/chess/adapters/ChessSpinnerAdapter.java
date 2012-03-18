package com.chess.adapters;

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
public class ChessSpinnerAdapter extends ArrayAdapter<String>/*ItemsAdapter<String>*/ {
/*
	public ChessSpinnerAdapter(Context context, int resourceId) {
		super(context, null);
		List<String> itemsList = new ArrayList<String>();
		String[] items = context.getResources().getStringArray(resourceId);
		Collections.addAll(itemsList, items);

		super.itemsList = itemsList;
	}

	public ChessSpinnerAdapter(Context context, String[] resource) {
		super(context, null);
		List<String> itemsList = new ArrayList<String>();
		Collections.addAll(itemsList, resource);

		super.itemsList = itemsList;
	}

	@Override
	protected View createView(ViewGroup parent) {
		return inflater.inflate(R.layout.spinner_item, parent, false);
	}


	protected View createDropDownView(ViewGroup viewGroup) {
		View view = inflater.inflate(R.layout.spinner_dropdown_item, viewGroup, false);
		return view;
	}

	@Override
	protected void bindView(String item, int pos, View convertView) {
		TextView textView = (TextView) convertView;
		textView.setText(item);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return super.getDropDownView(position,convertView, parent);
//		if (convertView == null) convertView = createDropDownView(parent);
//		bindView(itemsList.get(position), position, convertView);
//		return convertView;
	}*/

	private Context context;

	public ChessSpinnerAdapter(Context context, int entries) {
		super(context, android.R.layout.simple_spinner_item,
				context.getResources().getStringArray(entries));
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.context = context;
	}

	public ChessSpinnerAdapter(Context context, String[] entries) {
		super(context, android.R.layout.simple_spinner_item, entries);
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		((TextView) v.findViewById(android.R.id.text1)).setTextColor(context.getResources().getColor(R.color.hint_text));
		return v;
	}
}

