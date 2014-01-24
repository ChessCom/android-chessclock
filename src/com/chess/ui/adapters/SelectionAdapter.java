package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import com.chess.R;
import com.chess.model.SelectionItem;

import java.util.List;

public class SelectionAdapter extends ItemsAdapter<SelectionItem> {

	public SelectionAdapter(Context context, List<SelectionItem> itemList) {
		super(context, itemList);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.selection_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.image = (ImageView) view.findViewById(R.id.image);
		holder.text = (CheckedTextView) view.findViewById(R.id.text);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(SelectionItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.image.setImageDrawable(item.getImage());
		holder.text.setText(item.getText());
		holder.text.setChecked(item.isChecked());
	}

	private class ViewHolder {
		public CheckedTextView text;
		public ImageView image;
	}
}
