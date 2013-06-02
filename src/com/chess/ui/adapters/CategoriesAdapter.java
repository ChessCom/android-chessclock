package com.chess.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.06.13
 * Time: 11:14
 */
public class CategoriesAdapter  extends ItemsAdapter<String> {

	private final Resources resources;
	private final float density;
	private final int padding;

	public CategoriesAdapter(Context context, List<String> itemList) {
		super(context, itemList);
		resources = context.getResources();
		density = resources.getDisplayMetrics().density;
		padding = (int) (15 * density);
	}

	@Override
	protected View createView(ViewGroup parent) {
		RoboTextView textView = new RoboTextView(context);
		ButtonDrawableBuilder.setBackgroundToView(textView, R.style.ListItem_Header_Light);
		textView.setTextColor(resources.getColor(R.color.new_main_back));
		textView.setTextSize(resources.getDimension(R.dimen.header_title_size) / density);
		textView.setPadding(padding, padding, padding, padding);
		textView.setFont(FontsHelper.BOLD_FONT);
		ViewHolder holder = new ViewHolder();
		holder.textTxt = textView;

		textView.setTag(holder);
		return textView;
	}

	@Override
	protected void bindView(String item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.textTxt.setText(item);
	}

	private class ViewHolder {
		TextView textTxt;
	}
}
