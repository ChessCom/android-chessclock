package com.chess.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.RatingListItem;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.02.13
 * Time: 10:16
 */
public class RatingsAdapter extends ItemsAdapter<RatingListItem> {

	public RatingsAdapter(Context context, List<RatingListItem> itemList) {
		super(context, itemList);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_rating_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.labelTxt = (TextView) view.findViewById(R.id.ratingLabelTxt);
		holder.valueTxt = (TextView) view.findViewById(R.id.ratingValueTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	protected void bindView(RatingListItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.labelTxt.setText(item.getText());
		holder.valueTxt.setText(String.valueOf(item.getValue()));
		Drawable drawable = item.getImage();
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		holder.labelTxt.setCompoundDrawables(drawable, null, null, null);
	}

	private static class ViewHolder {
		TextView labelTxt;
		TextView valueTxt;
	}
}
