package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class SectionedListAdapter extends BaseAdapter {
	abstract protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent);

	protected List<Section> sections = new ArrayList<Section>();
	private static int TYPE_SECTION_HEADER = 0;

	private Context context;

	public Context getContext() {
		return context;
	}

	public SectionedListAdapter(Context context) {
		this.context = context;
	}

	public void addSection(String caption, Adapter adapter) {
		sections.add(new Section(caption, adapter));
	}

	public void removeSection(int index) {
		sections.remove(index);
	}

	public int getSectionsCnt() {
		return sections.size();
	}

	@Override
	public int getViewTypeCount() {
		int total = 1; // one for the header, plus those from sections
		for (Section section : sections) {
			total += section.adapter.getViewTypeCount();
		}
		return (total);
	}

	@Override
	public int getItemViewType(int position) {
		int typeOffset = TYPE_SECTION_HEADER + 1; // start counting from here
		for (Section section : sections) {
			int count = section.adapter.getCount();
			if (count != 0) {
				if (position == 0) {
					return TYPE_SECTION_HEADER;
				}
				int size = count + 1;
				if (position < size) {
					return typeOffset + section.adapter.getItemViewType(position - 1);
				}
				position -= size;
			}
			typeOffset += section.adapter.getViewTypeCount();
		}
		return (-1);
	}

//	public boolean areAllItemsSelectable() {
//		return false;
//	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) != TYPE_SECTION_HEADER;
	}

	@Override
	public long getItemId(int position) {
		return (position);
	}

	@Override
	public int getCount() {
		int total = 0;
		for (Section section : sections) {
			if (section.adapter != null && section.adapter.getCount() != 0)
				total += section.adapter.getCount() + 1; // add one for header
		}
		return (total);
	}

	/**
	 * Section means adapter with it's own items
	 * @param pos selected position
	 * @return section(adapter) number which is responsible for providing data for selected position
	 */
	public int getCurrentSection(int pos) {
		final int sectionsCnt = sections.size();
		int section;
		int headersCnt = 0;
		int passedItems = 0;
		int lastPassedItems = 0;

		for (section=0; section < sectionsCnt; section++) {
			passedItems += getSection(section).adapter.getCount(); // items in section. If there are 0 items, don't display/count header

			if (passedItems > lastPassedItems) { // means we switched to another section, adding header
				lastPassedItems = passedItems;
				headersCnt++;
			}
			if (pos < headersCnt + passedItems) { // if we found needed section, quit
				break;
			}
		}
		return section;
	}

	@Override
	public Object getItem(int position) {
		for (Section section : sections) {
			int count = section.adapter.getCount();
			if (count == 0)
				continue;
			if (position == 0) {
				return (section);
			}
			int size = section.adapter.getCount() + 1;

			if (position < size) {
				return (section.adapter.getItem(position - 1));
			}
			position -= size;
		}
		return (null);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionIndex = 0;

		for (Section section : sections) {
			int count = section.adapter.getCount();
			if (count != 0) {
				if (position == 0) {
					return (getHeaderView(section.caption, sectionIndex, convertView, parent));
				}
				int size = count + 1;

				if (position < size) {
					return (section.adapter.getView(position - 1, convertView, parent));
				}
				position -= size;
			}
			sectionIndex++;
		}
		return (null);
	}

	public Adapter getSectionAdapter(int index) {
		return sections.get(index).adapter;
	}

	public Section getSection(int index) {
		return sections.get(index);
	}

	public class Section {
		public String caption;
		public Adapter adapter;

		Section(String caption, Adapter adapter) {
			this.caption = caption;
			this.adapter = adapter;
		}

	}
}
