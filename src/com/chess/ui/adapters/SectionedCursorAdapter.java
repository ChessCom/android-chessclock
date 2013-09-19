package com.chess.ui.adapters;

/***
 Copyright 2011 Gonçalo Ferreira

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */



import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.Symbol;

import java.util.LinkedHashMap;
// Do not remove until release!
public abstract class SectionedCursorAdapter extends ItemsCursorAdapter {

	private static final String TAG = "SectionCursorAdapter";
	private static final boolean LOG_FLAG = false;

	private static final int TYPE_NORMAL = 1;
	private static final int TYPE_HEADER = 0;
	private static final int TYPE_COUNT = 2;

	private final int mHeaderRes;
	private final String mGroupColumn;
	private final LayoutInflater mLayoutInflater;

	private LinkedHashMap<Integer, String> sectionsIndexer;

	public static class ViewHolder {
		public TextView headerTitleTxt;
	}

	public SectionedCursorAdapter(Context context, Cursor cursor, int headerLayout, String groupColumn) {
		super(context, cursor);

		sectionsIndexer = new LinkedHashMap<Integer, String>();

		mHeaderRes = headerLayout;
		mGroupColumn = groupColumn;
		mLayoutInflater = LayoutInflater.from(context);

		if (cursor != null) {
			calculateSectionHeaders();
			cursor.registerDataSetObserver(mDataSetObserver);
		}
	}

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			calculateSectionHeaders();
		}

		@Override
		public void onInvalidated() {
			sectionsIndexer.clear();
		}
	};

	private void calculateSectionHeaders() {
		int i = 0;

		String previous = Symbol.EMPTY;
		int count = 0;

		final Cursor cursor = getCursor();

		sectionsIndexer.clear();

		do {
			final String group = cursor.getString(cursor.getColumnIndex(mGroupColumn));

			if (!group.equals(previous)) {
				sectionsIndexer.put(i + count, group);
				previous = group;

				count++;
			}
			i++;

		} while (cursor.moveToNext());
	}

	public String getGroupCustomFormat(Object obj) {
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);

		if (viewType == TYPE_NORMAL) {
			Cursor cursor = (Cursor) getItem(position);

			if (cursor == null) {
				ViewHolder holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(mHeaderRes, null, false);
				holder.headerTitleTxt = (TextView) convertView.findViewById(R.id.headerTitle);

				convertView.setTag(holder);

				return convertView;
			}

			final int mapCursorPos = getSectionForPosition(position);
			cursor.moveToPosition(mapCursorPos);

			return super.getView(mapCursorPos, convertView, parent);
		} else {

			ViewHolder holder;

			if (convertView == null) {

				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(mHeaderRes, null, false);
				holder.headerTitleTxt = (TextView) convertView.findViewById(R.id.headerTitle);

				convertView.setTag(holder);
			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			TextView headerTxt = holder.headerTitleTxt;

			final String group = sectionsIndexer.get(position);
			final String customFormat = getGroupCustomFormat(group);

			headerTxt.setText(customFormat == null ? group : customFormat);

			return convertView;
		}
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	@Override
	public int getCount() {
		return super.getCount() + sectionsIndexer.size();
	}

	@Override
	public boolean isEnabled(int position) {
//		return getItemViewType(position) == TYPE_NORMAL;  // restore if need to disable header selection
		return true;                 // use if need to disable header selection
	}

	public boolean isHeader(int position) {
		return getItemViewType(position) == TYPE_HEADER;
	}

	public int getPositionForSection(int section) {
		if (sectionsIndexer.containsKey(section)) {
			return section + 1;
		}
		return section;
	}

	public int getSectionForPosition(int position) {
		int offset = 0;
		for (Integer key : sectionsIndexer.keySet()) {
			if (position > key) {
				offset++;
			} else {
				break;
			}
		}

		return position - offset;
	}

	@Override
	public Object getItem(int position) {
//		if (getItemViewType(position) == TYPE_NORMAL) {  // use if need to disable header selection
//			return super.getItem(getRelativePosition(position));
//		}
		return super.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		if (getItemViewType(position) == TYPE_NORMAL) {
			return super.getItemId(getSectionForPosition(position));
		}
		return super.getItemId(position);
	}

	@Override
	public int getItemViewType(int position) {
		if (position == getPositionForSection(position)) {
			return TYPE_NORMAL;
		}
		return TYPE_HEADER;
	}

	public String getSectionName(int position) {
		return sectionsIndexer.get(position);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		final Cursor old = swapCursor(cursor);

		if (old != null) {
			old.close();
		}
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		if (getCursor() != null) {
			getCursor().unregisterDataSetObserver(mDataSetObserver);
		}

		final Cursor oldCursor = super.swapCursor(newCursor);

		if (newCursor != null) {
			calculateSectionHeaders();
			newCursor.registerDataSetObserver(mDataSetObserver);
		}

		return oldCursor;
	}
}
