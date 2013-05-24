package com.chess.ui.fragments.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 11:23
 */
public class SettingsThemeFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private ListView listView;
	private List<ThemeItem> menuItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		menuItems = new ArrayList<ThemeItem>();

		menuItems.add(new ThemeItem(R.string.theme_game_room, R.drawable.img_theme_green_felt, R.drawable.img_theme_green_felt_sample));
		menuItems.add(new ThemeItem(R.string.theme_dueling_tigers, R.drawable.img_theme_dueling_tigers, R.drawable.img_theme_dueling_tigers_sample));
//		menuItems.add(new ThemeItem(R.string.theme_blackwood, R.drawable.img_theme_blackwood, R.drawable.img_theme_blackwood_sample));
//		menuItems.add(new ThemeItem(R.string.theme_blackstone, R.drawable.img_theme_blackstone, R.drawable.img_theme_blackstone_sample));
//		menuItems.add(new ThemeItem(R.string.theme_charcoal, R.drawable.img_theme_charcoal, R.drawable.img_theme_charcoal_sample));
//		menuItems.add(new ThemeItem(R.string.theme_agua, R.drawable.img_theme_agua, R.drawable.img_theme_agua_sample));
//		menuItems.add(new ThemeItem(R.string.theme_grey_felt, R.drawable.img_theme_grey_felt, R.drawable.img_theme_grey_felt_sample));
//		menuItems.add(new ThemeItem(R.string.theme_grass, R.drawable.img_theme_grass, R.drawable.img_theme_grass_sample));

		int[] themeBackIds = AppUtils.getValidThemeBackIds();
		if (themeBackIds.length != menuItems.size()) {
			throw new IllegalStateException("Theme background ids are messed");
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.select_theme);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ThemesAdapter adapter = new ThemesAdapter(getActivity(), menuItems);

		listView.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		for (ThemeItem menuItem : menuItems) {
			menuItem.selected = false;
		}

		menuItems.get(position).selected = true;
		ThemeItem menuItem = (ThemeItem) listView.getItemAtPosition(position);
		menuItem.selected = true;
		((BaseAdapter)parent.getAdapter()).notifyDataSetChanged();

		getActivityFace().setMainBackground(menuItem.backId);
	}

	private class ThemeItem {
		public int nameId;
		public int iconRes;
		public int backId;
		public boolean selected;

		public ThemeItem(int nameId, int backId, int iconRes) {
			this.nameId = nameId;
			this.backId = backId;
			this.iconRes = iconRes;
		}
	}

	private class ThemesAdapter extends ItemsAdapter<ThemeItem> {

		public ThemesAdapter(Context context, List<ThemeItem> menuItems) {
			super(context, menuItems);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_settings_theme_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.check = (TextView) view.findViewById(R.id.iconTxt);
			holder.title = (TextView) view.findViewById(R.id.rowTitleTxt);
			holder.backImg = (ImageView) view.findViewById(R.id.backImg);
			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(ThemeItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			if (item.selected) {
				holder.check.setText(R.string.glyph_check);
			} else {
				holder.check.setText(StaticData.SYMBOL_EMPTY);
			}

			holder.title.setText(item.nameId);
			holder.backImg.setImageResource(item.iconRes);
		}

		public Context getContext() {
			return context;
		}

		public class ViewHolder {
			ImageView backImg;
			TextView check;
			TextView title;
		}
	}
}
