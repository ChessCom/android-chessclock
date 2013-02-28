package com.chess.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.chess.R;
import com.chess.backend.image_load.EnhancedImageDownloader;

import java.util.List;

public abstract class ItemsAdapter<T> extends BaseAdapter {

	protected final EnhancedImageDownloader imageLoader;
	protected List<T> itemsList;
	protected Context context;
	protected final LayoutInflater inflater;
	protected final int itemListId;

	public ItemsAdapter(Context context, List<T> itemList) {
		itemsList = itemList;
		this.context = context;
		inflater = LayoutInflater.from(context);
		itemListId = R.id.list_item_id;
		imageLoader = new EnhancedImageDownloader(context);
	}

	public void setItemsList(List<T> list) {
		itemsList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return itemsList == null ? 0 : itemsList.size();
	}

	@Override
	public T getItem(int position) {
		return itemsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void remove(T item) {
		if (itemsList.remove(item))
			notifyDataSetChanged();
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = createView(parent);
		}
		bindView(itemsList.get(pos), pos, convertView);
		return convertView;
	}

	protected abstract View createView(ViewGroup parent);

	protected abstract void bindView(T item, int pos, View convertView);

}
