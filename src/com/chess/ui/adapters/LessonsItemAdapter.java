package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.LessonListItem;
import com.chess.backend.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.08.13
 * Time: 10:04
 */
public class LessonsItemAdapter extends ItemsAdapter<LessonListItem> {

	private final int watchedTextColor;
	private final int unWatchedTextColor;
	private final int watchedIconColor;

	public LessonsItemAdapter(Context context, List<LessonListItem> cursor) {
		super(context, cursor);
		watchedTextColor = resources.getColor(R.color.new_light_grey_3);
		unWatchedTextColor = resources.getColor(R.color.new_text_blue);
		watchedIconColor = resources.getColor(R.color.new_light_grey_2);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_completed_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView) view.findViewById(R.id.titleTxt);
		holder.icon = (TextView) view.findViewById(R.id.completedIconTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(LessonListItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.text.setText(item.getName());
		if (item.isCompleted()) {
			holder.text.setTextColor(watchedTextColor);
			holder.icon.setTextColor(watchedIconColor);
			holder.icon.setText(R.string.ic_check);
		} else {
			holder.text.setTextColor(unWatchedTextColor);
			holder.icon.setText(StaticData.SYMBOL_EMPTY);
		}
	}

	public void updateCompletedLessonAtPosition(int position) {
		itemsList.get(position).setCompleted(true);
	}

	public boolean isAllLessonsCompleted() {
		for (LessonListItem item : itemsList) {
			if(!item.isCompleted()) {
				return false;
			}
		}

		return true;
	}

	private static class ViewHolder {
		TextView text;
		TextView icon;
	}
}
