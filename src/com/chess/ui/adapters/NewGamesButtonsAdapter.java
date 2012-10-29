package com.chess.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.model.NewGameButtonItem;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

/**
 * NewGamesButtonsAdapter class
 *
 * @author alien_roger
 * @created at: 22.07.12 7:58
 */
public class NewGamesButtonsAdapter extends ItemsAdapter<NewGameButtonItem>{

	private ItemClickListenerFace clickListenerFace;

	public NewGamesButtonsAdapter(ItemClickListenerFace clickListenerFace, List<NewGameButtonItem> itemList) {
		super(clickListenerFace.getMeContext(), itemList);
		this.clickListenerFace = clickListenerFace;
	}

	@Override
	protected View createView(ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
		RoboButton view = (RoboButton) inflater.inflate(R.layout.default_button, null, false);
		holder.label = view;
		view.setTag(holder);
		view.setOnClickListener(clickListenerFace);
		return view;
	}

	@Override
	protected void bindView(NewGameButtonItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.label.setText(item.getLabel());

		convertView.setTag(itemListId, pos);
	}
	
	private class ViewHolder{
		RoboButton label;
	}
}
