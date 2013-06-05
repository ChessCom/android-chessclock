package com.chess.ui.fragments.popup_fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.interfaces.PopupListSelectionFace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.06.13
 * Time: 13:35
 */
public class PopupOptionsMenuFragment extends DialogFragment implements View.OnClickListener {

	private PopupListSelectionFace listener;
	private List<String> itemsList;

	public static PopupOptionsMenuFragment newInstance(PopupListSelectionFace listener, List<String> itemsList) {
		PopupOptionsMenuFragment frag = new PopupOptionsMenuFragment();
		frag.listener = listener;
		frag.itemsList = itemsList;
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
		return inflater.inflate(R.layout.new_popup_option_select_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(new StringAdapter(getActivity(), itemsList));
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (listener != null) {
			listener.dialogCanceled();
		}
	}

	@Override
	public void onClick(View v) {
		listener.valueSelected((Integer) v.getTag(R.id.list_item_id));
	}

	private class StringAdapter extends ItemsAdapter<String> {

		public StringAdapter(Context context, List<String> itemList) {
			super(context, itemList);
		}

		@Override
		protected View createView(ViewGroup parent) {
			RoboButton button = new RoboButton(context, null, R.attr.greyButton);
			button.setDrawableStyle(R.style.Button_Glassy);
			button.setTextColor(Color.WHITE);
			float shadowRadius = 0.5f;
			float shadowDx = 0 ;
			float shadowDy = -1 ;
			button.setShadowLayer(shadowRadius, shadowDx, shadowDy, Color.BLACK);

			button.setOnClickListener(PopupOptionsMenuFragment.this);
			ViewHolder holder = new ViewHolder();

			holder.name = button;

			button.setTag(holder);
			return button;
		}

		@Override
		protected void bindView(String item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			holder.name.setTag(R.id.list_item_id, pos);
			holder.name.setText(item);
		}

		private class ViewHolder {
			RoboButton name;
		}
	}
}
