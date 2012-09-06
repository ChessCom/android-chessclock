package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;

import java.util.List;

/**
 * ChessSpinnerAdapter class
 *
 * @author alien_roger
 * @created at: 24.02.12 5:22
 */
public class ChessSpinnerAdapter extends ItemsAdapter<String> {

	public ChessSpinnerAdapter(Context context, List<String> items) {
		super(context, items);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.textTxt = (TextView) view.findViewById(android.R.id.text1);
		holder.textTxt.setTextColor(context.getResources().getColor(R.color.hint_text));

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(String item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.textTxt.setText(item);
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
		holder.textTxt.setText(itemsList.get(position));

		return convertView;
	}

	private static class ViewHolder {
		TextView textTxt;
	}

	private static class DropViewHolder {
		TextView textTxt;
	}

}

