package com.chess.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import com.chess.R;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;

public abstract class ItemsCursorAdapter extends CursorAdapter {

	protected Resources resources;
	protected int itemListId;
	protected Context context;
	protected SmartImageFetcher imageFetcher;
	protected LayoutInflater inflater;

	public ItemsCursorAdapter(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, 0);
		this.imageFetcher = imageFetcher;
		init(context);
	}

	public ItemsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, 0);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		resources = context.getResources();
		inflater = LayoutInflater.from(context);
		itemListId = R.id.list_item_id;
	}

	protected static String getString(Cursor cursor, String column) {
		return cursor.getString(cursor.getColumnIndex(column));
	}

	protected static int getInt(Cursor cursor, String column) {
		return cursor.getInt(cursor.getColumnIndex(column));
	}

	protected static long getLong(Cursor cursor, String column) {
		return cursor.getLong(cursor.getColumnIndex(column));
	}

}
