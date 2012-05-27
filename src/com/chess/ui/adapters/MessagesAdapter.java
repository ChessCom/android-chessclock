package com.chess.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.model.MessageItem;

import java.util.ArrayList;

public class MessagesAdapter extends ArrayAdapter<MessageItem> {

	public ArrayList<MessageItem> items;
	private LayoutInflater vi;
	private int resource;
	private int ownerColor;
    private int opponentColor;

	private String userName;
	private String opponentName;

	public MessagesAdapter(Context context, int textViewResourceId, ArrayList<MessageItem> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.vi = LayoutInflater.from(context);
		this.resource = textViewResourceId;
        ownerColor = getContext().getResources().getColor(R.color.green_button);
        opponentColor = getContext().getResources().getColor(R.color.orange_button);

		userName =  AppData.getUserName(getContext());
		opponentName =  AppData.getOpponentName(getContext());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = vi.inflate(resource, null);
		}
		MessageItem el = items.get(position);
		if (el != null) {
			TextView owner = (TextView) convertView.findViewById(R.id.owner);
			TextView text = (TextView) convertView.findViewById(R.id.text);
			if (text != null) text.setText(el.message);
			if (owner != null) {
				if (el.owner.equals("0")) {
					owner.setTextColor(ownerColor);
					owner.setText(userName);
				} else {
					owner.setTextColor(opponentColor);
					owner.setText(opponentName);
				}
			}
		}
		return convertView;
	}
}
