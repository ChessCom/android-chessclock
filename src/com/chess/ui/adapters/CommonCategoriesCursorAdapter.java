package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.db.DbScheme;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.07.13
 * Time: 20:18
 */
public class CommonCategoriesCursorAdapter extends ItemsCursorAdapter {

	public CommonCategoriesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = inflater.inflate(R.layout.new_video_header, parent, false);
		ViewHolder holder = new ViewHolder();

		holder.text = (TextView) convertView.findViewById(R.id.headerTitleTxt);
		holder.icon = (TextView) convertView.findViewById(R.id.headerIconTxt);
		convertView.setTag(holder);

		return convertView;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.text.setText(Html.fromHtml(getString(cursor, DbScheme.V_NAME)));
		holder.icon.setText(Symbol.EMPTY);
	}

	private class ViewHolder {
		TextView text;
		TextView icon;
	}
}
