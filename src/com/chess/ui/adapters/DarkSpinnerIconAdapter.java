package com.chess.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.SelectionItem;

import java.util.List;

/**
 * WhiteSpinnerAdapter class
 *
 * @author alien_roger
 * @created at: 30.01.13 5:22
 */
public class DarkSpinnerIconAdapter extends ItemsAdapter<SelectionItem> {

	public DarkSpinnerIconAdapter(Context context, List<SelectionItem> items) {
		super(context, items);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_dark_spinner_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.textTxt = (TextView) view.findViewById(android.R.id.text1);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(SelectionItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.textTxt.setText(item.getText());
		Drawable drawable = item.getImage();
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		holder.textTxt.setCompoundDrawables(drawable, null, null, null);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		DropViewHolder holder = new DropViewHolder();
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false);
			holder.textTxt = (TextView) convertView.findViewById(android.R.id.text1);

			convertView.setTag(holder);
		} else {
			holder = (DropViewHolder) convertView.getTag();
		}

		holder.textTxt.setTextColor(context.getResources().getColor(R.color.black));
		holder.textTxt.setText(itemsList.get(position).getText());
		Drawable drawable = itemsList.get(position).getImage();
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		holder.textTxt.setCompoundDrawables(drawable, null, null, null);

		return convertView;
	}

	private static class ViewHolder {
		TextView textTxt;
	}

	private static class DropViewHolder {
		TextView textTxt;
	}

}

