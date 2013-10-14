package com.chess.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.ChatItem;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class ChatMessagesAdapter extends ItemsAdapter<ChatItem> {

	private final int imageSize;

	public ChatMessagesAdapter(Context context, List<ChatItem> items, SmartImageFetcher imageFetcher) {
		super(context, items, imageFetcher);
		Resources resources = context.getResources();
		imageSize = (int) (resources.getDimension(R.dimen.chat_icon_size) / resources.getDisplayMetrics().density);
	}

	@Override
	protected View createView(ViewGroup parent) {
		ViewHolder holder = new ViewHolder();

		View view = inflater.inflate(R.layout.new_chat_list_item, null, false);
		holder.text = (TextView) view.findViewById(R.id.messageTxt);
		holder.myImg = (AvatarView) view.findViewById(R.id.myAvatarImg);
		holder.opponentImg = (AvatarView) view.findViewById(R.id.opponentAvatarImg);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(ChatItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		try {
			holder.text.setText(URLDecoder.decode(item.getContent(), HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			holder.text.setText(item.getContent());
		}

		if (item.isMine()) {
			holder.text.setBackgroundResource(R.drawable.img_chat_buble_grey);

			imageFetcher.loadImage(new SmartImageFetcher.Data(item.getAvatar(), imageSize), holder.myImg.getImageView());

			holder.myImg.setVisibility(View.VISIBLE);
			holder.opponentImg.setVisibility(View.GONE);
		} else {
			holder.text.setBackgroundResource(R.drawable.img_chat_buble_white);

			imageFetcher.loadImage(new SmartImageFetcher.Data(item.getAvatar(), imageSize), holder.opponentImg.getImageView());
			holder.myImg.setVisibility(View.GONE);
			holder.opponentImg.setVisibility(View.VISIBLE);
		}
	}

	private static class ViewHolder{
		TextView text;
		AvatarView myImg;
		AvatarView opponentImg;
	}
}
