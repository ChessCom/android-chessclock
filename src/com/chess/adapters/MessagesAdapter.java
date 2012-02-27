package com.chess.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivity;
import com.chess.model.Message;

import java.util.ArrayList;

public class MessagesAdapter extends ArrayAdapter<Message> {

	public ArrayList<Message> items;
	private LayoutInflater vi;
	private int resource;
	private CoreActivity activity;

	public MessagesAdapter(Context context, int textViewResourceId, ArrayList<Message> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.vi = LayoutInflater.from(context);
		this.resource = textViewResourceId;
		this.activity = (CoreActivity) context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = vi.inflate(resource, null);
		}
		Message el = items.get(position);
		if (el != null) {
			TextView owner = (TextView) convertView.findViewById(R.id.owner);
			TextView text = (TextView) convertView.findViewById(R.id.text);
			if (text != null) text.setText(el.message);
			if (owner != null) {
				if (el.owner.equals("0")) {
					owner.setTextColor(Color.GREEN);
					owner.setText(activity.getMainApp().getSharedData().getString(AppConstants.USERNAME, ""));
				} else {
					owner.setTextColor(Color.RED);
					owner.setText(activity.getMainApp().getSharedData().getString("opponent", ""));
				}
			}
		}
		return convertView;
	}
}
