package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.db.DbScheme;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 7:16
 */
public class LessonsCursorAdapter extends ItemsCursorAdapter {

	private final int watchedTextColor;
	private final int unWatchedTextColor;
	private final int watchedIconColor;

	public LessonsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		watchedTextColor = resources.getColor(R.color.new_light_grey_3);
		unWatchedTextColor = resources.getColor(R.color.new_text_blue);
		watchedIconColor = resources.getColor(R.color.new_light_grey_2);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_completed_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView) view.findViewById(R.id.titleTxt);
		holder.icon = (TextView) view.findViewById(R.id.completedIconTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.text.setText(getString(cursor, DbScheme.V_NAME));
		if (getInt(cursor, DbScheme.V_LESSON_COMPLETED) > 0) {
			holder.text.setTextColor(watchedTextColor);
			holder.icon.setTextColor(watchedIconColor);
			holder.icon.setText(R.string.ic_check);
		} else {
			holder.text.setTextColor(unWatchedTextColor);
			holder.icon.setText(StaticData.SYMBOL_EMPTY);
		}
	}

	private static class ViewHolder {
		TextView text;
		TextView icon;
	}
}
