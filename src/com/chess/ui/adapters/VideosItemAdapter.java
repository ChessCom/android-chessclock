package com.chess.ui.adapters;

import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.VideoItem;
import com.chess.backend.statics.Symbol;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.09.13
 * Time: 20:41
 */
public class VideosItemAdapter extends ItemsAdapter<VideoItem.Data> {

	public static final String GREY_COLOR_DIVIDER = "##";
	public static final String SLASH_DIVIDER = " | ";

	private final ItemClickListenerFace clickFace;
	private final int watchedTextColor;
	private final int unWatchedTextColor;
	private final int watchedIconColor;
	private final int unWatchedIconColor;

	private CharacterStyle foregroundSpan;
	private SparseBooleanArray viewedMap;

	public VideosItemAdapter(ItemClickListenerFace clickFace, List<VideoItem.Data> itemList) {
		super(clickFace.getMeContext(), itemList);
		int lightGrey = context.getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		watchedTextColor = resources.getColor(R.color.new_light_grey_3);
		unWatchedTextColor = resources.getColor(R.color.new_text_blue);
		watchedIconColor = resources.getColor(R.color.new_light_grey_2);
		unWatchedIconColor = resources.getColor(R.color.orange_button);
		this.clickFace = clickFace;

	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_video_lib_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.durationTxt = (TextView) view.findViewById(R.id.durationTxt);
		holder.completedIconTxt = (TextView) view.findViewById(R.id.completedIconTxt);

		holder.completedIconTxt.setOnClickListener(clickFace);
		view.setTag(holder);
		return view;

	}

	@Override
	protected void bindView(VideoItem.Data item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.completedIconTxt.setTag(R.id.list_item_id, pos);

		String firstName = item.getFirstName();
		CharSequence chessTitle = item.getChessTitle();
		String lastName =  item.getLastName();
		CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + Symbol.SPACE
				+ firstName + Symbol.SPACE + lastName;
		authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		holder.authorTxt.setText(authorStr);
		String durationStr = SLASH_DIVIDER + context.getString(R.string.min_arg, item.getMinutes());
		String viewsCntStr = SLASH_DIVIDER + context.getString(R.string.views_arg, item.getViewCount());
		holder.durationTxt.setText(durationStr + viewsCntStr);
		holder.titleTxt.setText(item.getTitle());

		if (viewedMap.get((int) item.getVideoId(), false)) {
			holder.titleTxt.setTextColor(watchedTextColor);
			holder.completedIconTxt.setTextColor(watchedIconColor);
			holder.completedIconTxt.setText(R.string.ic_check);
		} else {
			holder.titleTxt.setTextColor(unWatchedTextColor);
			holder.completedIconTxt.setTextColor(unWatchedIconColor);
			holder.completedIconTxt.setText(R.string.ic_play);
		}
	}

	public void addViewedMap(SparseBooleanArray viewedMap) {
		this.viewedMap = viewedMap;
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView durationTxt;
		public TextView completedIconTxt;
	}
}
