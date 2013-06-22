package com.chess.ui.fragments.popup_fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.ContactItem;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.interfaces.MultiTypeGetFace;
import com.chess.db.QueryParams;
import com.chess.db.tasks.GetEmailsFromContactsTask;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.drawables.ActionBarBackgroundDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.06.13
 * Time: 6:41
 */
public class PopupContactSelectFragment extends DialogFragment implements AdapterView.OnItemClickListener {

	private PopupListSelectionFace listener;
	private ContactsAdapter contactsAdapter;
	private ListView listView;
	private View loadingView;

	public PopupContactSelectFragment(){

	}

	public static PopupContactSelectFragment createInstance(PopupListSelectionFace listener) {
		PopupContactSelectFragment frag = new PopupContactSelectFragment();
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

		((TextView) view.findViewById(R.id.popupTitleTxt)).setText(R.string.select_contact);

		loadingView = view.findViewById(R.id.loadingView);
		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		getEmails();
	}

	private void getEmails() {
		QueryParams params = new QueryParams();
		params.setUri(ContactsContract.Contacts.CONTENT_URI);

		new GetEmailsFromContactsTask(new DbUpdateListener(), params, new ArrayList<ContactItem>()).executeTask();
	}

	private class DbUpdateListener extends AbstractUpdateListener<Cursor> implements MultiTypeGetFace<ContactItem, Cursor> {

		public DbUpdateListener() {
			super(getActivity(), PopupContactSelectFragment.this);
		}

		@Override
		public void showProgress(boolean show) {
			loadingView.setVisibility(show? View.VISIBLE : View.GONE);
		}

		@Override
		public void updateContacts(List<ContactItem> itemsList) {
			updateList(itemsList);
		}
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

	public ContactItem getContactByPosition(int position) {
		return contactsAdapter.getItem(position);
	}

	private void updateList(List<ContactItem> itemsList){
		if (contactsAdapter == null) {
			contactsAdapter = new ContactsAdapter(getActivity(), itemsList);
		} else {
			contactsAdapter.setItemsList(itemsList);
		}
		listView.setAdapter(contactsAdapter);
	}

	public class ContactsAdapter extends ItemsAdapter<ContactItem> {

		private final int imageSize;

		public ContactsAdapter(Context context, List<ContactItem> itemList) {
			super(context, itemList);
			imageSize = (int) (context.getResources().getDimension(R.dimen.list_item_image_size_big)
					/ context.getResources().getDisplayMetrics().density);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_recent_opponent_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.playerImg = (ProgressImageView) view.findViewById(R.id.playerImg);
			holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(ContactItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.playerTxt.setText(item.getName());
			imageLoader.download(item.getIconUrl(), holder.playerImg, imageSize);
		}

		private class ViewHolder{
			public ProgressImageView playerImg;
			public TextView playerTxt;
		}
	}


}

