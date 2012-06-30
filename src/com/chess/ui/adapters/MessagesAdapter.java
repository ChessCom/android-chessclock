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

	public ArrayList<MessageItem> itemsList;
	private LayoutInflater inflater;
	private int resource;
	private int ownerColor;
    private int opponentColor;

	private String userName;
	private String opponentName;

	public MessagesAdapter(Context context, int textViewResourceId, ArrayList<MessageItem> items) {
		super(context, textViewResourceId, items);
		itemsList = items;
		inflater = LayoutInflater.from(context);
		resource = textViewResourceId;
        ownerColor = getContext().getResources().getColor(R.color.green_button);
        opponentColor = getContext().getResources().getColor(R.color.orange_button);

		userName =  AppData.getUserName(getContext());
		opponentName =  AppData.getOpponentName(getContext());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(resource, null);
		}
		MessageItem messageItem = itemsList.get(position);
		if (messageItem != null) {
			TextView playerLabel = (TextView) convertView.findViewById(R.id.owner);
			TextView text = (TextView) convertView.findViewById(R.id.text);

			if (text != null) text.setText(messageItem.message);
			if (playerLabel != null) {
				if (messageItem.owner.equals("0")) {
					playerLabel.setTextColor(ownerColor);
					playerLabel.setText(userName);
				} else {
					playerLabel.setTextColor(opponentColor);
					playerLabel.setText(opponentName);
				}
			}
		}
		return convertView;
	}
}
