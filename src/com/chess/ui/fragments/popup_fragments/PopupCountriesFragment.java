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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.drawables.ActionBarBackgroundDrawable;
import com.chess.utilities.CountryItem;
import com.chess.utilities.LoadCountryFlagsTask;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.02.13
 * Time: 19:26
 */
public class PopupCountriesFragment extends DialogFragment implements AdapterView.OnItemClickListener {

	private PopupListSelectionFace listener;
	private ListView listView;
	private ViewGroup loadingView;
	private CountriesAdapter countriesAdapter;

	public static PopupCountriesFragment createInstance(PopupListSelectionFace listener) {
		PopupCountriesFragment frag = new PopupCountriesFragment();
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

		((TextView)view.findViewById(R.id.popupTitleTxt)).setText(R.string.select_country);

		getDialog().setCancelable(false);

		loadingView = (ViewGroup) view.findViewById(R.id.loadingView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		countriesAdapter = new CountriesAdapter(getActivity(), null);
		listView.setAdapter(countriesAdapter);

		new LoadCountryFlagsTask(new CountriesLoadListener()).executeTask();
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

	private class CountriesLoadListener extends AbstractUpdateListener<CountryItem> {

		public CountriesLoadListener() {
			super(getActivity(), PopupCountriesFragment.this);
			useList = true;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateListData(List<CountryItem> itemsList) {
			super.updateListData(itemsList);

			listView.setAdapter(new CountriesAdapter(getActivity(), itemsList));
		}
	}

	private class CountriesAdapter extends ItemsAdapter<CountryItem> {

		public CountriesAdapter(Context context, List<CountryItem> itemList) {
			super(context, itemList);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_country_list_item, parent, false);
			ViewHolder holder = new ViewHolder();

			holder.name = (TextView) view.findViewById(R.id.nameTxt);
			holder.desc = (TextView) view.findViewById(R.id.codeTxt);
			holder.icon = (ImageView) view.findViewById(R.id.countryImg);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(CountryItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			setBackground(convertView, pos);

			holder.name.setText(item.getName());
			holder.desc.setText(item.getCode());
			holder.icon.setImageDrawable(item.getIcon());
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
			TextView desc;
			ImageView icon;
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			if (countriesAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}
}
