package com.chess.ui.fragments.messages;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.LeftNavigationFragment;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.11.13
 * Time: 17:38
 */
public class MessagesFragmentTablet extends CommonLogicFragment implements FragmentParentFace, AdapterView.OnItemClickListener {

	private boolean noCategoriesFragmentsAdded;
	private List<SelectionItem> menuItems;
	private OptionsAdapter optionsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_tablet_content_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(optionsAdapter);
		listView.setOnItemClickListener(this);
	}


	@Override
	public void changeFragment(BasePopupsFragment fragment) {
		openInternalFragment(fragment);
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment);
		transaction.commitAllowingStateLoss();
	}

	private void openInternalFragment(Fragment fragment) {
		String simpleName = fragment.getClass().getSimpleName();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment, simpleName);
		transaction.addToBackStack(simpleName);
		transaction.commitAllowingStateLoss();
	}

	@Override
	public boolean showPreviousFragment() {
		if (getActivity() == null) {
			return false;
		}
		int entryCount = getChildFragmentManager().getBackStackEntryCount();
		if (entryCount > 0) {
			int last = entryCount - 1;
			FragmentManager.BackStackEntry stackEntry = getChildFragmentManager().getBackStackEntryAt(last);
			if (stackEntry != null && stackEntry.getName().equals(MessagesInboxFragmentTablet.class.getSimpleName())) {
				noCategoriesFragmentsAdded = true;    // TODO adjust
			}

			return getChildFragmentManager().popBackStackImmediate();
		} else {
			return super.showPreviousFragment();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		for (SelectionItem menuItem : menuItems) { // uncheck
			menuItem.setChecked(false);
		}

		SelectionItem selectionItem = (SelectionItem) parent.getItemAtPosition(position);
		selectionItem.setChecked(true);
		optionsAdapter.notifyDataSetChanged();

		if (position == 0) {
			Fragment fragmentByTag = getChildFragmentManager().findFragmentByTag(MessagesInboxFragmentTablet.class.getSimpleName());
			if (fragmentByTag == null) {
				fragmentByTag = MessagesInboxFragmentTablet.createInstance(this);
			}
			changeInternalFragment(fragmentByTag);
		} else {
			Fragment fragmentByTag = getChildFragmentManager().findFragmentByTag(NewMessageFragment.class.getSimpleName());
			if (fragmentByTag == null) {
				fragmentByTag = new NewMessageFragment();
			}
			changeInternalFragment(fragmentByTag);
		}
	}

	protected void init() {

		menuItems = new ArrayList<SelectionItem>();
		{ // inbox
			SelectionItem selectionItem = new SelectionItem(null, getString(R.string.inbox));
			selectionItem.setCode(getString(R.string.ic_inbox));
			selectionItem.setChecked(true);
			menuItems.add(selectionItem);
		}
		{ // new
			SelectionItem selectionItem = new SelectionItem(null, getString(R.string.new_message));
			selectionItem.setCode(getString(R.string.ic_edit));
			menuItems.add(selectionItem);
		}
		optionsAdapter = new OptionsAdapter(getActivity(), menuItems);

		changeInternalFragment(MessagesInboxFragmentTablet.createInstance(this));

		noCategoriesFragmentsAdded = true;
	}

	private class OptionsAdapter extends ItemsAdapter<SelectionItem> {

		private final int sidePadding;
		private final int whiteColor;

		public OptionsAdapter(Context context, List<SelectionItem> itemList) {
			super(context, itemList);
			sidePadding = (int) (8 * density);
			whiteColor = resources.getColor(R.color.semitransparent_white_75);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.dark_spinner_item, parent, false);

			ButtonDrawableBuilder.setBackgroundToView(view, R.style.ListItem_Tablet);
			view.setPadding(sidePadding, 0, sidePadding, 0);

			ViewHolder holder = new ViewHolder();
			holder.nameTxt = (TextView) view.findViewById(R.id.categoryNameTxt);
			holder.iconTxt = (TextView) view.findViewById(R.id.iconTxt);
			holder.spinnerIcon = (TextView) view.findViewById(R.id.spinnerIcon);
			holder.spinnerIcon.setVisibility(View.GONE);

			holder.nameTxt.setPadding(sidePadding, 0, 0, 0);
			holder.nameTxt.setTextColor(whiteColor);
			holder.iconTxt.setVisibility(View.VISIBLE);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(SelectionItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			holder.nameTxt.setText(item.getText());
			holder.iconTxt.setText(item.getCode());

			Drawable background = convertView.getBackground();
			if (item.isChecked()) {
				background.mutate().setState(LeftNavigationFragment.SELECTED_STATE);
			} else {
				background.mutate().setState(LeftNavigationFragment.ENABLED_STATE);
			}
		}

		private class ViewHolder {
			TextView iconTxt;
			TextView nameTxt;
			TextView spinnerIcon;
		}
	}
}
