package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.statics.Symbol;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.08.13
 * Time: 10:04
 */
public class LessonsItemsAdapter extends ItemsAdapter<LessonSingleItem> {

	protected final int completedTextColor;
	protected final int incompleteTextColor;
	protected final int completedIconColor;
	protected final int incompleteIconColor;

	public LessonsItemsAdapter(Context context, List<LessonSingleItem> cursor) {
		super(context, cursor);
		completedTextColor = resources.getColor(R.color.new_light_grey_3);
		incompleteTextColor = resources.getColor(R.color.new_text_blue);
		completedIconColor = resources.getColor(R.color.new_light_grey_2);
		incompleteIconColor = resources.getColor(R.color.orange_button);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.completed_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView) view.findViewById(R.id.titleTxt);
		holder.icon = (TextView) view.findViewById(R.id.completedIconTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(LessonSingleItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.text.setText(item.getName());
		if (item.isCompleted()) {
			holder.text.setTextColor(completedTextColor);
			holder.icon.setTextColor(completedIconColor);
			holder.icon.setText(R.string.ic_check);
		} else {
			holder.text.setTextColor(incompleteTextColor);
			holder.icon.setText(Symbol.EMPTY);
		}
	}

	public boolean isAllLessonsCompleted() {
		for (LessonSingleItem item : itemsList) {
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
