package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.new_api.ChatItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.model.MessageItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatMessagesAdapter extends ItemsAdapter<ChatItem> {

	private int ownerColor;
    private int opponentColor;

	private String userName;
	private String opponentName;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

	public ChatMessagesAdapter(Context context, List<ChatItem> items) {
		super(context, items);
        ownerColor = context.getResources().getColor(R.color.green_button);
        opponentColor = context.getResources().getColor(R.color.orange_button);

		userName =  AppData.getUserName(context);
		opponentName =  AppData.getOpponentName(context);
	}

	@Override
	protected View createView(ViewGroup parent) {
		ViewHolder holder = new ViewHolder();

		View view = inflater.inflate(R.layout.chat_item, null, false);
		holder.playerLabel = (TextView) view.findViewById(R.id.playerLabelTxt);
		holder.text = (TextView) view.findViewById(R.id.messageTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(ChatItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.text.setText(item.getContent());
		String time = dateFormat.format(new Date(item.getTimestamp()));
		if (item.isMine()) {
			holder.playerLabel.setTextColor(ownerColor);
			holder.playerLabel.setText(time + StaticData.SYMBOL_SPACE + userName);
		} else {
			holder.playerLabel.setTextColor(opponentColor);
			holder.playerLabel.setText(time + StaticData.SYMBOL_SPACE + opponentName);
		}
	}


	private static class ViewHolder{
		TextView playerLabel;
		TextView text;
	}
}
