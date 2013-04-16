package com.chess.ui.adapters;

import android.content.Context;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.AppUtils;

import java.util.List;


public class NewVideosAdapter extends ItemsAdapter<VideoItem.Data> {

	public static final String GREY_COLOR_DIVIDER = "##";

	private CharacterStyle foregroundSpan;

	public NewVideosAdapter(Context context, List<VideoItem.Data> items) {
		super(context, items);

		int lightGrey = context.getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_common_description_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(VideoItem.Data item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();


		String firstName = item.getFirstName();
		CharSequence chessTitle = item.getChessTitle();
		String lastName =  item.getLastName();
		CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + StaticData.SYMBOL_SPACE
				+ firstName + StaticData.SYMBOL_SPACE + lastName;
		authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		holder.authorTxt.setText(authorStr);

		holder.titleTxt.setText(item.getName());

	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
	}
}
