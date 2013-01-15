package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.01.13
 * Time: 12:23
 */
public class NewGameLiveView extends NewGameDefaultView implements ItemClickListenerFace {

	public NewGameLiveView(Context context) {
		super(context);
	}

	public NewGameLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NewGameLiveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void toggleOptions() {
		super.toggleOptions();

		int expandVisibility = optionsVisible? VISIBLE: GONE;

		optionsView.setVisibility(expandVisibility);

		if(optionsVisible) {
			compactRelLay.setBackgroundResource(R.drawable.game_option_back_1);
		} else {
			compactRelLay.setBackgroundResource(R.drawable.nav_menu_item_selected);
		}
		compactRelLay.setPadding(COMPACT_PADDING, 0, COMPACT_PADDING, COMPACT_PADDING);
	}

	@Override
	public void addOptionsView() {
		optionsView = LayoutInflater.from(getContext()).inflate(R.layout.new_game_option_live_view, null, false);
		optionsView.setVisibility(GONE);
		addView(optionsView);

		String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
		List<NewLiveGameButtonItem> newGameButtonItems = new ArrayList<NewLiveGameButtonItem>();
		for (String label : newGameButtonsArray) {
			newGameButtonItems.add(NewLiveGameButtonItem.createNewButtonFromLabel(label, getContext()));
		}

		GridView gridView = (GridView) optionsView.findViewById(R.id.liveModeGrid);
		gridView.setAdapter(new NewLiveGamesButtonsAdapter(this, newGameButtonItems));

	}

	@Override
	public Context getMeContext() {
		return getContext();
	}

	public static class NewLiveGameButtonItem {

		public int min;
		public int sec;
		public String label;
		private static final String PAIR_DIVIDER = " | ";

		public static NewLiveGameButtonItem createNewButtonFromLabel(String label, Context context){
			NewLiveGameButtonItem  buttonItem = new NewLiveGameButtonItem();
			if(label.contains(PAIR_DIVIDER)){
				// "5 | 2"),
				String[] params = label.split(PAIR_DIVIDER);
				buttonItem.min = Integer.valueOf(params[0]);
				buttonItem.sec = Integer.valueOf(params[2]);
				buttonItem.label = label;

			} else {
				// "10 min"),
				buttonItem.min = Integer.valueOf(label);
				buttonItem.label = context.getString(R.string.min_, label);

			}
			return buttonItem;
		}
	}

	public class NewLiveGamesButtonsAdapter extends ItemsAdapter<NewLiveGameButtonItem> {

		private ItemClickListenerFace clickListenerFace;

		public NewLiveGamesButtonsAdapter(ItemClickListenerFace clickListenerFace, List<NewLiveGameButtonItem> itemList) {
			super(clickListenerFace.getMeContext(), itemList);
			this.clickListenerFace = clickListenerFace;
		}

		@Override
		protected View createView(ViewGroup parent) {
			ViewHolder holder = new ViewHolder();
			RoboButton view = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
			holder.label = view;
			view.setTag(holder);
			view.setOnClickListener(clickListenerFace);
			return view;
		}

		@Override
		protected void bindView(NewLiveGameButtonItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.label.setText(item.label);

			convertView.setTag(itemListId, pos);
		}

		private class ViewHolder{
			RoboButton label;
		}
	}


}
