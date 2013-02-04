package com.chess.ui.adapters;

import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.statics.StaticData;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

//public class VideosAdapter extends ItemsAdapter<VideoItemOld> {
public class VideosAdapter extends ItemsAdapter<VideoItem.VideoDataItem> {

	private static final String MMMM_DD_YYYY = "MMMM' 'dd,' 'yyyy";
	private ItemClickListenerFace clickListenerFace;

//	public VideosAdapter(ItemClickListenerFace clickListenerFace, List<VideoItemOld> items) {
	public VideosAdapter(ItemClickListenerFace clickListenerFace, List<VideoItem.VideoDataItem> items) {
		super(clickListenerFace.getMeContext(), items);
		this.clickListenerFace = clickListenerFace;
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.video_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.title);
		holder.timesTxt = (TextView) view.findViewById(R.id.times);
		holder.descTxt = (TextView) view.findViewById(R.id.desc);
		holder.addInfoTxt = (TextView) view.findViewById(R.id.addinfo);
		holder.fullDescBtn = (Button) view.findViewById(R.id.fullDescBtn);
		holder.playBtn = (Button) view.findViewById(R.id.playVideoBtn);

		holder.fullDescBtn.setOnClickListener(clickListenerFace);
		holder.playBtn.setOnClickListener(clickListenerFace);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(VideoItem.VideoDataItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.fullDescBtn.setTag(itemListId, pos);
		holder.playBtn.setTag(itemListId, pos);

		CharSequence date = DateFormat.format(MMMM_DD_YYYY, 1000* item.getCreateDate()/*getPublishTimestamp()*/);

		holder.titleTxt.setText(item.getName()/*getTitle()*/);
		holder.timesTxt.setText(item.getMinutes() + " min " + " | " + date);

		holder.descTxt.setText(item.getDescription());
		holder.addInfoTxt.setText(item.getFirstName()/*getAuthorFirstGame()*/ + StaticData.SYMBOL_SPACE
				+ item.getLastName()/*getAuthorLastName()*/);
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
