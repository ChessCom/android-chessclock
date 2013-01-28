package com.chess.ui.adapters;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.interfaces.ItemClickListenerFace;

public class CustomSectionedAdapter extends SectionedListAdapter {

	private ItemClickListenerFace clickListenerFace;
	private int layoutResource;

	public CustomSectionedAdapter(ItemClickListenerFace itemClickListenerFace, int layoutResource) {
		super(itemClickListenerFace.getMeContext());
		clickListenerFace = itemClickListenerFace;
		this.layoutResource = layoutResource;
	}

	@Override
	protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(layoutResource, parent, false);
			createViewHolder(convertView);
		}
		bindView(convertView, caption);
		return convertView;
	}

	private void createViewHolder(View convertView) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.text = (TextView) convertView.findViewById(R.id.headerTitle);
		convertView.setOnClickListener(clickListenerFace);
		convertView.setTag(viewHolder);
	}

	private void bindView(View convertView, String text) {
		ViewHolder view = (ViewHolder) convertView.getTag();
		view.text.setText(text);
	}

	private class ViewHolder {
		TextView text;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		for (Section s : sections) {
			if (s.adapter instanceof BaseAdapter) {
				try {
					s.adapter.registerDataSetObserver(observer);
				} catch (Exception e) {
					// we have the same observable objects
					// so, do nothing
					Log.e("section adapter", "error" + e.getMessage());
				}
			}
		}
	}
}