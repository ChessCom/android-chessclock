package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.LessonSingleItem;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.11.13
 * Time: 7:42
 */
public class LessonsItemsAdapterTablet extends LessonsItemsAdapter {

	public LessonsItemsAdapterTablet(Context context, List<LessonSingleItem> itemList) {
		super(context, itemList);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View convertView = inflater.inflate(R.layout.new_lessons_thumb_list_item, parent, false);
		ViewHolder holder = new ViewHolder();

		holder.text = (TextView) convertView.findViewById(R.id.titleTxt);
		holder.completedIconTxt = (TextView) convertView.findViewById(R.id.completedIconTxt);

		holder.scoreLabelTxt = (TextView) convertView.findViewById(R.id.scoreLabelTxt);
		holder.attemptsLabelTxt = (TextView) convertView.findViewById(R.id.attemptsLabelTxt);
		holder.ratingLabelTxt = (TextView) convertView.findViewById(R.id.ratingLabelTxt);
		holder.scoreValueTxt = (TextView) convertView.findViewById(R.id.scoreValueTxt);
		holder.attemptsValueTxt = (TextView) convertView.findViewById(R.id.attemptsValueTxt);
		holder.ratingValueTxt = (TextView) convertView.findViewById(R.id.ratingValueTxt);

		convertView.setTag(holder);
		return convertView;
	}

	@Override
	protected void bindView(LessonSingleItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.text.setText(item.getName());

		boolean completed = item.isCompleted();
		if (completed) {
			holder.text.setTextColor(completedTextColor);
			holder.completedIconTxt.setTextColor(completedIconColor);
			holder.completedIconTxt.setText(R.string.ic_check);
			holder.scoreValueTxt.setText(item.getLastScoreStr());
			holder.attemptsValueTxt.setText(item.getLastScoreStr());
			holder.ratingValueTxt.setText(item.getLastScoreStr());
		} else {
			holder.text.setTextColor(incompleteTextColor);
			holder.completedIconTxt.setText(R.string.ic_lessons);
			holder.completedIconTxt.setTextColor(incompleteIconColor);
		}

		holder.scoreLabelTxt.setVisibility(completed ? View.VISIBLE : View.GONE);
		holder.attemptsLabelTxt.setVisibility(completed ? View.VISIBLE : View.GONE);
		holder.ratingLabelTxt.setVisibility(completed ? View.VISIBLE : View.GONE);
		holder.scoreValueTxt.setVisibility(completed ? View.VISIBLE : View.GONE);
		holder.attemptsValueTxt.setVisibility(completed ? View.VISIBLE : View.GONE);
		holder.ratingValueTxt.setVisibility(completed ? View.VISIBLE : View.GONE);
	}

	private class ViewHolder {
		TextView text;
		TextView completedIconTxt;
		TextView scoreLabelTxt;
		TextView attemptsLabelTxt;
		TextView ratingLabelTxt;
		TextView scoreValueTxt;
		TextView attemptsValueTxt;
		TextView ratingValueTxt;
	}
}
