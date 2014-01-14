package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.views.chess_boards.NotationFace;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.widgets.RoboTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 19:59
 */
public class NotationsViewTablet extends GridView implements NotationFace, AdapterView.OnItemClickListener {

	public static final int NOTATION_ID = 0x00003321;

	private NotationsPagerAdapter notationsAdapter;
	private List<String> originalNotations;
	private NotationView.BoardForNotationFace selectionFace;
	private int textPadding;
	private boolean newNotations;
	public int textSize;
	public int textColor;
	public int textColorSelected;
	private int textSidePadding;
	private int textViewMinHeight;
	private boolean clickable;

	public NotationsViewTablet(Context context) {
		super(context);
	}

	public NotationsViewTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	public void onCreate(AttributeSet attrs) {
		Context context = getContext();
		if (context == null) {
			return;
		}
		Resources resources = context.getResources();
		float density = resources.getDisplayMetrics().density;

		textSize = (int) (resources.getDimension(R.dimen.notations_text_size) / density);

		textColor = resources.getColor(R.color.notations_text_color);
		textColorSelected = resources.getColor(R.color.notations_text_color_selected);
		textViewMinHeight = resources.getDimensionPixelSize(R.dimen.notations_text_view_height);
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NotationView);
		if (array == null) {
			return;
		}
		try {
			if (array.hasValue(R.styleable.NotationView_textColorBack)) {
				textColor = array.getColor(R.styleable.NotationView_textColorBack, 0xFF00FF00);
			}
			if (array.hasValue(R.styleable.NotationView_textColorSelected)) {
				textColorSelected = array.getColor(R.styleable.NotationView_textColorSelected, 0xFF00FF00);
			}
		} finally {
			array.recycle();
		}

		notationsAdapter = new NotationsPagerAdapter(getContext(), null);
		setAdapter(notationsAdapter);
		setOnItemClickListener(this);

		textPadding = resources.getDimensionPixelSize(R.dimen.notations_text_padding);
		textSidePadding = resources.getDimensionPixelSize(R.dimen.notations_text_side_padding);
		int screenPadding = (int) (10 * density);
		int screenPaddingSide = (int) (20 * density);

		LinearLayout.LayoutParams notationTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		notationTextParams.gravity = Gravity.CENTER_VERTICAL;

		if (attrs != null) {
			ButtonDrawableBuilder.setBackgroundToView(this, attrs);
		}

//		ButtonDrawableBuilder.setBackgroundToView(this, R.style.Rect_Tab_Middle);
		setPadding(screenPaddingSide, screenPadding, screenPaddingSide, 0);

		clickable = true;
	}

	@Override
	public void updateNotations(String[] notations, NotationView.BoardForNotationFace selectionFace, int ply) {
		this.selectionFace = selectionFace;

		newNotations = false;
		if (originalNotations != null) {
			// if notations array was replaced with different notations
			for (int i = 0; i < originalNotations.size(); i++) {
				String notation = originalNotations.get(i);
				if (notations.length > i) {
					// if stored notation do not equals to notation from new array
					if (!notation.equals(notations[i])) {
						newNotations = true;
					}
				}
			}
		}

		if (newNotations || originalNotations == null || originalNotations.size() < notations.length) {
			newNotations = true;
			originalNotations = new ArrayList<String>();
			Collections.addAll(originalNotations, notations);
		}

		if (notationsAdapter == null) {
			notationsAdapter = new NotationsPagerAdapter(getContext(), originalNotations);
			setAdapter(notationsAdapter);
		}

		if (newNotations) {
			notationsAdapter.setItemsList(originalNotations);
		}

		selectNotation(ply -1);
	}

	private void selectNotation(int pos) {
		notationsAdapter.selectItem(pos);
		smoothScrollToPosition(pos);
	}

	private String setNumberToNotation(String notation, int position) {
		if (position % 2 == 0) {
			int number = (position) / 2 + 1;
			notation = number + Symbol.DOT + notation;
		}
		return notation;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if (!clickable) {
			return;
		}
		notationsAdapter.selectItem(position);
		selectionFace.onClick(view);
	}

	private class NotationsPagerAdapter extends ItemsAdapter<String> {
		private int selectedPosition;

		public NotationsPagerAdapter(Context context, List<String> itemList) {
			super(context, itemList);
		}

		@Override
		protected View createView(ViewGroup parent) {
			RoboTextView textView = new RoboTextView(getContext());

			textView.setId(NOTATION_ID);
			textView.setTextSize(textSize);
			textView.setTextColor(textColor);
			textView.setMinHeight(textViewMinHeight);
			textView.setMinimumHeight(textViewMinHeight);
			textView.setGravity(Gravity.LEFT);
			textView.setPadding(textSidePadding, textPadding, textSidePadding, textPadding);

			return textView;
		}

		@Override
		protected void bindView(String item, int pos, View convertView) {
			TextView textView = (TextView) convertView;
			String notation = setNumberToNotation(item, pos);
			textView.setText(notation);

			if (pos == selectedPosition) {
				textView.setBackgroundResource(R.drawable.button_grey_flat);
				textView.setTextColor(textColorSelected);
			} else {
				textView.setTextColor(textColor);
				textView.setBackgroundDrawable(null);
			}

			textView.setTag(R.id.list_item_id, pos);
			textView.setPadding(textSidePadding, textPadding, textSidePadding, textPadding);
		}

		public void selectItem(int pos) {
			selectedPosition = pos;
			notifyDataSetChanged();
		}
	}

	public void show(boolean show) {
		setVisibility(show ? VISIBLE : GONE);
	}

	@Override
	public void moveBack(int ply) {
		selectNotation(ply - 1);
	}

	@Override
	public void moveForward(int ply) {
		selectNotation(ply - 1);
	}

	@Override
	public void rewindBack() {
		selectNotation(0);
	}

	@Override
	public void rewindForward() {
		int totalCnt = originalNotations.size() - 1;
		selectNotation(totalCnt);
	}

	@Override
	public void resetNotations() {
		originalNotations = new ArrayList<String>();
		notationsAdapter.notifyDataSetChanged();
		newNotations = true;
		notationsAdapter.selectItem(-1);
	}

	@Override
	public void setClickable(boolean clickable) {
		super.setClickable(clickable);
		// todo: rename field and do not override base method?
		this.clickable = clickable;
	}

}
