package com.chess.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.VideoItem;

import java.util.List;

public class VideosAdapter extends ItemsAdapter<VideoItem> {

	private static final String MMMM_DD_YYYY = "MMMM' 'dd,' 'yyyy";
	private PlayClickListener playClickListener;
	private FullDescClickListener fullDescClickListener;

	public VideosAdapter(Context context, List<VideoItem> items) {
		super(context, items);
		playClickListener = new PlayClickListener();
		fullDescClickListener = new FullDescClickListener();
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.video_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.title);
		holder.timesTxt = (TextView) view.findViewById(R.id.times);
		holder.descTxt = (TextView) view.findViewById(R.id.desc);
		holder.addInfoTxt = (TextView) view.findViewById(R.id.addinfo);
		holder.fullDescBtn = (Button) view.findViewById(R.id.fulldesc);
		holder.playBtn = (Button) view.findViewById(R.id.play);

		holder.fullDescBtn.setOnClickListener(fullDescClickListener);
		holder.playBtn.setOnClickListener(playClickListener);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(VideoItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.fullDescBtn.setTag(itemListId, pos);
		holder.playBtn.setTag(itemListId, pos);

		CharSequence date = DateFormat.format(MMMM_DD_YYYY, 1000* item.getPublishTimestamp());

		holder.titleTxt.setText(item.getTitle());
		holder.timesTxt.setText(item.getMinutes() + " min " + " | " + date);

		holder.descTxt.setText(item.getDescription());
		holder.addInfoTxt.setText(item.getAuthorFirstGame() + StaticData.SYMBOL_SPACE
				+ item.getAuthorLastName());

	}

	private class FullDescClickListener implements OnClickListener{
		@Override
		public void onClick(View view) {
			int pos = (Integer) view.getTag(itemListId);
			VideoItem videoItem = itemsList.get(pos);

			new AlertDialog.Builder(context)
					.setTitle(videoItem.getTitle())
					.setMessage(videoItem.getDescription())
					.setPositiveButton(context.getString(R.string.ok), null)
					.create().show();
		}
	}

	private class PlayClickListener implements OnClickListener{
		@Override
		public void onClick(View view) {
			int pos = (Integer) view.getTag(itemListId);
			VideoItem videoItem = itemsList.get(pos);

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setDataAndType(Uri.parse(videoItem.getViewUrl().trim()), "video/*");
			context.startActivity(i);
		}
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView descTxt;
		public TextView timesTxt;
		public TextView addInfoTxt;
		public Button fullDescBtn;
		public Button playBtn;
	}
}
