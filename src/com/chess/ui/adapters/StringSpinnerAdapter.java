package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.07.13
 * Time: 20:26
 */
public class StringSpinnerAdapter extends ItemsAdapter<String> {

	public StringSpinnerAdapter(Context context, List<String> items) {
		super(context, items);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.dark_spinner_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.categoryNameTxt = (TextView) view.findViewById(R.id.categoryNameTxt);
		view.findViewById(R.id.spinnerIcon).setVisibility(View.GONE);
		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(String item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.categoryNameTxt.setText(item);
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
		TextView categoryNameTxt;
	}

	private static class DropViewHolder {
		TextView textTxt;
	}

}