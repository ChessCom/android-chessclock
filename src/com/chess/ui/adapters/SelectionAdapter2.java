package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.SelectionItem;

import java.util.List;

public class SelectionAdapter2 extends ItemsAdapter<SelectionItem> {

	public SelectionAdapter2(Context context, List<SelectionItem> itemList) {
		super(context, itemList);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.selection_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.image = (ImageView) view.findViewById(R.id.image);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(SelectionItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.image.setImageDrawable(item.image);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.selection_item_dropdown, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.text = (TextView) convertView.findViewById(R.id.text);

			convertView.setTag(holder);
		}
		bindDropDownView(itemsList.get(position), position, convertView);

		return convertView;
	}

	private void bindDropDownView(SelectionItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.image.setImageDrawable(item.image);
		holder.text.setText(item.text);
	}

	private class ViewHolder {
		public TextView text;
		public ImageView image;
	}
}
