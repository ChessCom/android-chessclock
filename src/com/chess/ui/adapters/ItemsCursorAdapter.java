package com.chess.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ImageGetter;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.statics.StaticData;
import com.chess.utilities.AppUtils;

import java.util.HashMap;

public abstract class ItemsCursorAdapter extends CursorAdapter {

	protected Resources resources;
	protected int itemListId;
	protected Context context;
	protected SmartImageFetcher imageFetcher;
	protected LayoutInflater inflater;
	private HashMap<String, ImageGetter.TextImage> textViewsImageCache;
	private int screenWidth;
	protected boolean isTablet;
	protected float density;

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
		density = resources.getDisplayMetrics().density;
		inflater = LayoutInflater.from(context);
		itemListId = R.id.list_item_id;

		screenWidth = resources.getDisplayMetrics().widthPixels;
		textViewsImageCache = new HashMap<String, ImageGetter.TextImage>();

		if (StaticData.USE_TABLETS) {
			isTablet = AppUtils.is7InchTablet(context) || AppUtils.is10InchTablet(context);
		}
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


	protected void loadTextWithImage(TextView textView, String sourceStr) {
		textView.setText(Html.fromHtml(sourceStr, getImageGetter(textView, sourceStr), null));
	}

	protected ImageGetter getImageGetter(TextView textView, String sourceStr) {
		return new ImageGetter(context, textViewsImageCache, textView, sourceStr, screenWidth);
	}

	protected ImageGetter getImageGetter(TextView textView, String sourceText, int imageSize) {
		return new ImageGetter(context, textViewsImageCache, textView, sourceText, imageSize);
	}
}
