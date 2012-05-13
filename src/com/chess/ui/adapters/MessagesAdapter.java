package com.chess.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.model.MessageItem;
import com.chess.ui.core.CoreActivityActionBar;

import java.util.ArrayList;

public class MessagesAdapter extends ArrayAdapter<MessageItem> {

	public ArrayList<MessageItem> items;
	private LayoutInflater vi;
	private int resource;
	private CoreActivityActionBar activity;
    private int ownerColor;
    private int opponentColor;

	public MessagesAdapter(Context context, int textViewResourceId, ArrayList<MessageItem> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.vi = LayoutInflater.from(context);
		this.resource = textViewResourceId;
		this.activity = (CoreActivityActionBar) context;
        ownerColor = getContext().getResources().getColor(R.color.green_button);
        opponentColor = getContext().getResources().getColor(R.color.orange_button);
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
					owner.setTextColor(ownerColor/* Color.GREEN*/);
					owner.setText(activity.getMainApp().getSharedData().getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY));
				} else {
					owner.setTextColor(opponentColor/*Color.RED*/);
					owner.setText(activity.getMainApp().getSharedData().getString(AppConstants.OPPONENT, StaticData.SYMBOL_EMPTY));
				}
			}
		}
		return convertView;
	}
}
