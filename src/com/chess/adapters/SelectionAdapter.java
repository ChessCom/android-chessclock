package com.chess.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.Selection;

import java.util.ArrayList;

public class SelectionAdapter extends ArrayAdapter<Selection> {

	public ArrayList<Selection> items;
	private LayoutInflater vi;
	private int resource;

	public SelectionAdapter(Context context, int textViewResourceId, ArrayList<Selection> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.vi = LayoutInflater.from(context);
		this.resource = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = vi.inflate(resource, null);
		}
		Selection el = items.get(position);
		if (el != null) {
			TextView text = (TextView) convertView.findViewById(R.id.text);
			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			if (text != null) {
				text.setText(el.text);
				image.setImageDrawable(el.image);
			}
		}
		return convertView;
	}
}
