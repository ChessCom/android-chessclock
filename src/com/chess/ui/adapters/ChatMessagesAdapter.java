package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.new_api.ChatItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;

import java.util.List;

public class ChatMessagesAdapter extends ItemsAdapter<ChatItem> {

	private int ownerColor;
    private int opponentColor;

	private String userName;
	private String opponentName;

	public ChatMessagesAdapter(Context context, List<ChatItem> items) {
		super(context, items);
        ownerColor = context.getResources().getColor(R.color.new_light_grey_0);
        opponentColor = context.getResources().getColor(R.color.new_light_grey_2);

		userName =  AppData.getUserName(context);
		opponentName =  AppData.getOpponentName(context);
	}

	@Override
	protected View createView(ViewGroup parent) {
		ViewHolder holder = new ViewHolder();

		View view = inflater.inflate(R.layout.chat_list_item, null, false);
		holder.text = (TextView) view.findViewById(R.id.messageTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(ChatItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		if (item.isMine()) {
			holder.text.setTextColor(ownerColor);
			holder.text.setText(userName + StaticData.SYMBOL_COLON + StaticData.SYMBOL_SPACE + item.getContent());
		} else {
			holder.text.setTextColor(opponentColor);
			holder.text.setText(opponentName + StaticData.SYMBOL_COLON + StaticData.SYMBOL_SPACE + item.getContent());
		}
	}


	private static class ViewHolder{
		TextView text;
	}
}
