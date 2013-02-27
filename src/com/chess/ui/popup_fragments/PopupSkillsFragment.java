package com.chess.ui.popup_fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.drawables.ActionBarBackgroundDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.02.13
 * Time: 19:26
 */
public class PopupSkillsFragment extends DialogFragment implements AdapterView.OnItemClickListener {

	private PopupListSelectionFace listener;

	public static PopupSkillsFragment newInstance(PopupListSelectionFace listener) {
		PopupSkillsFragment frag = new PopupSkillsFragment();
		frag.listener = listener;
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
		return inflater.inflate(R.layout.new_popup_list_selection_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View popupTitleLay = view.findViewById(R.id.popupTitleLay);
		popupTitleLay.setBackgroundDrawable(new ActionBarBackgroundDrawable(getActivity()));

		((TextView)view.findViewById(R.id.popupTitleTxt)).setText(R.string.choose_level);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		String[] names = getResources().getStringArray(R.array.skills_name);
		String[] descriptions = getResources().getStringArray(R.array.skills_desc);

		List<SkillItem> skillItems = new ArrayList<SkillItem>();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			String description = descriptions[i];

			skillItems.add(new SkillItem(name, description));
		}

		listView.setAdapter(new SkillsAdapter(getActivity(), skillItems));
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		listener.dialogCanceled();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		listener.valueSelected(position);
	}



	private class SkillItem {
		String name;
		String desc;

		private SkillItem(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}
	}

	private class SkillsAdapter extends ItemsAdapter<SkillItem> {

		public SkillsAdapter(Context context, List<SkillItem> itemList) {
			super(context, itemList);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_skill_list_item, parent, false);
			ViewHolder holder = new ViewHolder();

			holder.name = (TextView) view.findViewById(R.id.nameTxt);
			holder.desc = (TextView) view.findViewById(R.id.descTxt);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(SkillItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			setBackground(convertView, pos);

			holder.name.setText(item.name);
			holder.desc.setText(item.desc);
		}

		private void setBackground(View view, int pos) {
			/*if (pos == 0) {
				view.setBackgroundResource(R.drawable.edit_white_top_selector);
			} else*/ if (pos == getCount() - 1) {
				view.setBackgroundResource(R.drawable.edit_white_bottom_selector);
			} else {
				view.setBackgroundResource(R.drawable.edit_white_middle_selector);
			}
		}

		private class ViewHolder {
			TextView name;
			TextView desc;
		}
	}
}
