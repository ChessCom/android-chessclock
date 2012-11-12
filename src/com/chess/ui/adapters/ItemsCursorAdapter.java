package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;

public abstract class ItemsCursorAdapter extends CursorAdapter {

	protected Context context;
	protected final LayoutInflater inflater;

	public ItemsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, 0);

		this.context = context;
		inflater = LayoutInflater.from(context);
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
