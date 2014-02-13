package com.chess.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.RatingListItem;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.02.13
 * Time: 10:16
 */
public class RatingsAdapter extends ItemsAdapter<RatingListItem> {

	private final boolean useLtr;

	public RatingsAdapter(Context context, List<RatingListItem> itemList) {
		super(context, itemList);

		useLtr = AppUtils.useLtr(context);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.rating_list_item, parent, false);
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
		if (item.getValue() == 0) {
			holder.valueTxt.setText(R.string.not_available);
		} else {
			holder.valueTxt.setText(String.valueOf(item.getValue()));
		}

		Drawable drawable = item.getImage();
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		if (useLtr) {
			holder.labelTxt.setCompoundDrawables(drawable, null, null, null);
		} else {
			holder.labelTxt.setCompoundDrawables(null, null, drawable, null);
		}
	}

	private static class ViewHolder {
		TextView labelTxt;
		TextView valueTxt;
	}
}
