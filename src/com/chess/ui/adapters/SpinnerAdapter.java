package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SpinnerAdapter class
 *
 * @author alien_roger
 * @created at: 24.02.12 5:22
 */
// Do not remove until release!
public class SpinnerAdapter extends ItemsAdapter<String> {

	public SpinnerAdapter(Context context, int resourceId) {
		super(context, null);
		List<String> itemsList = new ArrayList<String>();
		String[] items = context.getResources().getStringArray(resourceId);
		Collections.addAll(itemsList, items);

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
		if (convertView == null) convertView = createDropDownView(parent);
		bindView(itemsList.get(position), position, convertView);
		return convertView;
	}
}