package com.chess.ui.adapters;

import android.content.Context;
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
		View view = inflater.inflate(R.layout.dark_spinner_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.categoryNameTxt = (TextView) view.findViewById(R.id.categoryNameTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(SelectionItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.categoryNameTxt.setText(item.getText());
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

		return convertView;
	}

	private static class ViewHolder {
		TextView categoryNameTxt;
	}

	private static class DropViewHolder {
		TextView textTxt;
	}

}

