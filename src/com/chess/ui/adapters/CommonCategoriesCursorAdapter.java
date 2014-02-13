package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.07.13
 * Time: 20:18
 */
public class CommonCategoriesCursorAdapter extends ItemsCursorAdapter {

	private int layoutId;

	public CommonCategoriesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		layoutId = R.layout.common_titled_list_item;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = inflater.inflate(layoutId, parent, false);
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

	public void setLayoutId(int layoutId) {
		this.layoutId = layoutId;
	}

	private class ViewHolder {
		TextView text;
		TextView icon;
	}
}
