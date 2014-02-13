package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.db.DbScheme;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 7:16
 */
public class LessonsCursorAdapter extends ItemsCursorAdapter {

	protected final int completedTextColor;
	protected final int incompleteTextColor;
	protected final int completedIconColor;
	protected final int incompleteIconColor;

	public LessonsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		completedTextColor = resources.getColor(R.color.new_light_grey_3);
		incompleteTextColor = resources.getColor(R.color.new_text_blue);
		completedIconColor = resources.getColor(R.color.new_light_grey_2);
		incompleteIconColor = resources.getColor(R.color.orange_button);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.completed_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.completedIconTxt = (TextView) view.findViewById(R.id.completedIconTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.titleTxt.setText(getString(cursor, DbScheme.V_NAME));
		if (getInt(cursor, DbScheme.V_LESSON_COMPLETED) > 0) {
			holder.titleTxt.setTextColor(completedTextColor);
			holder.completedIconTxt.setTextColor(completedIconColor);
			holder.completedIconTxt.setText(R.string.ic_check);
		} else {
			holder.titleTxt.setTextColor(incompleteTextColor);
			holder.completedIconTxt.setText(Symbol.EMPTY);
		}
	}

	protected static class ViewHolder {
		TextView titleTxt;
		TextView completedIconTxt;
	}
}
