package com.chess.ui.fragments.popup_fragments;

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
public class PopupSelectPhotoFragment extends DialogFragment implements AdapterView.OnItemClickListener {

	private PopupListSelectionFace listener;

	public static PopupSelectPhotoFragment createInstance(PopupListSelectionFace listener) {
		PopupSelectPhotoFragment frag = new PopupSelectPhotoFragment();
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

		((TextView) view.findViewById(R.id.popupTitleTxt)).setText(R.string.profile_picture);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		List<String> skillItems = new ArrayList<String>();
		skillItems.add(getString(R.string.choose_photo));
		skillItems.add(getString(R.string.take_photo));

		listView.setAdapter(new StringAdapter(getActivity(), skillItems));
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (listener != null) {
			listener.onDialogCanceled();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		listener.onValueSelected(position);
	}

	private class StringAdapter extends ItemsAdapter<String> {

		public StringAdapter(Context context, List<String> itemList) {
			super(context, itemList);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_select_list_item, parent, false);
			ViewHolder holder = new ViewHolder();

			holder.name = (TextView) view.findViewById(R.id.nameTxt);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(String item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			setBackground(convertView, pos);

			holder.name.setText(item);
		}

		private void setBackground(View view, int pos) {
			if (pos == getCount() - 1) {
				view.setBackgroundResource(R.drawable.round_list_item_bottom_selector);
			} else {
				view.setBackgroundResource(R.drawable.round_list_item_middle_selector);
			}
		}

		private class ViewHolder {
			TextView name;
		}
	}

}
