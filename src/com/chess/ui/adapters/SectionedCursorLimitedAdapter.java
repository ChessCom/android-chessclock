package com.chess.ui.adapters;

/***
 Copyright 2011 Gonçalo Ferreira
 Copyright 2013 Alien Roger

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
import com.chess.backend.statics.StaticData;
import com.chess.db.DbDataManager;

import java.util.LinkedHashMap;

public abstract class SectionedCursorLimitedAdapter extends ItemsCursorAdapter {

	private static final String TAG = "SectionCursorAdapter";
	private static final boolean LOG_FLAG = false;

	private static final int TYPE_NORMAL = 1;
	private static final int TYPE_HEADER = 0;
	private static final int TYPE_COUNT = 2;

	private final int mHeaderRes;
	private final String mGroupColumn;
	private final LayoutInflater mLayoutInflater;

	private LinkedHashMap<Integer, String> sectionsIndexer;
	private LinkedHashMap<Integer, Integer> sectionsCounter;
	private int itemsPerSectionCnt;

	public static class ViewHolder {
		public TextView headerTitleTxt;
	}

	public SectionedCursorLimitedAdapter(Context context, Cursor cursor, int headerLayout, String groupColumn,
										 int itemsPerSectionCnt) {
		super(context, cursor);
		this.itemsPerSectionCnt = itemsPerSectionCnt;

		sectionsIndexer = new LinkedHashMap<Integer, String>();
		sectionsCounter = new LinkedHashMap<Integer, Integer>();

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

		String previous = StaticData.SYMBOL_EMPTY;
		int sectionNumber = 0;

		final Cursor cursor = getCursor();

		sectionsIndexer.clear();

		int itemsInSection = 0;
		int lastSectionPosition = 0;
		do {
			final String sectionName = DbDataManager.getString(cursor, mGroupColumn);

			if (!sectionName.equals(previous)) {

				if (sectionsIndexer.size() > 0) {
					lastSectionPosition += itemsInSection + 1;
				}
				sectionsIndexer.put(lastSectionPosition, sectionName);
				previous = sectionName;
				sectionNumber++;
				itemsInSection = 0;
			}

			if (itemsInSection < itemsPerSectionCnt) {
				itemsInSection++;
				sectionsCounter.put(sectionNumber, itemsInSection);
			}

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

			final int mapCursorPos = getRelativePosition(position);
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
		int sectionsCnt = sectionsIndexer.size();
		int totalCnt = 0;
		for (Integer sectionNumber : sectionsCounter.keySet()) {
			totalCnt += sectionsCounter.get(sectionNumber);
		}

		return totalCnt + sectionsCnt;
	}

	@Override
	public boolean isEnabled(int position) {
//		return getItemViewType(position) == TYPE_NORMAL;  // restore if need to disable header selection
		return true;                 // use if need to disable header selection
	}

//	/**
//	 * Checks if {@code position} intersects with header of section
//	 * and return next value after header if matches.
//	 *
//	 * @param position
//	 * @return position of specified item
//	 */
//	public int getPositionForItem(int position) {
//		if (isSectionHeader(position)) {
//			return position + 1;
//		}
//		return position;
//	}

	/**
	 * @param position to compare
	 * @return true if specified absolute position intersects with header position
	 */
	public boolean isSectionHeader(int position) {
		return sectionsIndexer.containsKey(position);
	}

	/**
	 * Gives a number of section for required position
	 *
	 * @param position to compare
	 * @return position relatively headers/offsets
	 */
	public int getRelativePosition(int position) {
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
		return super.getItem(position);
	}

//	private int getPositionInSection(int position) {
//		int currentSectionNumber = getCurrentSectionNumber(position);
//		if (currentSectionNumber > 1) { // if in 2+ section
//			int offset = 1;
//			for (int i = 1; i < currentSectionNumber; i++) {
//				offset += sectionsCounter.get(i);
//				offset++;
//			}
//
//			int positionInSection = position - offset;
//			return positionInSection > ITEMS_PER_SECTION_CNT ? 1 : positionInSection;
//		}
//
//		return position > ITEMS_PER_SECTION_CNT ? 1 : position;
//	}

	public int getPositionForSection(int section) {
		if (sectionsIndexer.containsKey(section)) {
			return section + 1;
		}
		return section;
	}

//	private int getCurrentSectionNumber(int position) {
//		int currentSectionNumber = 1;
//		int absolutePosition = position;
//		while (absolutePosition > 0) {
//			if (isSectionHeader(absolutePosition)) {
//				currentSectionNumber++;
//			}
//
//			absolutePosition--;
//		}
//
//		return currentSectionNumber;
//	}


	@Override
	public long getItemId(int position) {
		if (getItemViewType(position) == TYPE_NORMAL) {
			return super.getItemId(getRelativePosition(position));
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
