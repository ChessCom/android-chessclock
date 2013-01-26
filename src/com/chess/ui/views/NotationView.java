package com.chess.ui.views;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.PieceItem;
import com.chess.ui.interfaces.BoardViewFace;

import java.util.HashMap;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class NotationView extends LinearLayout {

	public static final int NOTATION_TEXT_SIZE = 11;

    private float density;

	private boolean blocked; // TODO

	private HorizontalListView horizontalListView;

	public NotationView(Context context) {
        super(context);
        onCreate();
    }

    public NotationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate();
    }


    public void onCreate() {
        setOrientation(VERTICAL);
        density = getContext().getResources().getDisplayMetrics().density;

        LayoutParams infoLayParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout infoLayout = new LinearLayout(getContext());
        infoLayout.setLayoutParams(infoLayParams);


		horizontalListView = new HorizontalListView(getContext(), null);

        infoLayout.addView(horizontalListView);

        addView(infoLayout);
    }

    public void updateNotations(String[] notations) {
		// set Array adapter
		horizontalListView.setAdapter(new NotationsAdapter(notations)); // TODO
    }

	private class NotationsAdapter extends BaseAdapter {

		private String[] dataObjects;
		private int textColor;

		NotationsAdapter(String[] dataObjects) {

			this.dataObjects = dataObjects;
			textColor = getContext().getResources().getColor(R.color.new_light_grey);
		}

		private OnClickListener mOnButtonClicked = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO handle click - list back to the selected item
			}
		};

		@Override
		public int getCount() {
			return dataObjects.length;
		}

		@Override
		public String getItem(int position) {
			return dataObjects[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = createView(parent);
			}
			bindView(dataObjects[pos], pos, convertView);
			return convertView;
		}

		protected View createView(ViewGroup parent){
			RoboTextView textView = new RoboTextView(getContext());
			textView.setTextColor(textColor);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, NOTATION_TEXT_SIZE);
			textView.setFont(RoboTextView.HELV_NEUE_FONT);
			textView.setOnClickListener(mOnButtonClicked);

			return textView;
		}

		protected void bindView(String item, int pos, View convertView){
			convertView.setTag(R.id.list_item_id, pos);

			if (pos %2 == 0) {
				int number = pos / 2 + 1;
				((TextView)convertView).setText(String.valueOf(number) + StaticData.SYMBOL_DOT + item);
			} else {
				((TextView)convertView).setText(item);
			}

			if (pos == getCount() -1) {
				convertView.setBackgroundResource(R.drawable.button_grey_solid_default);
			} else {
				convertView.setBackgroundDrawable(null);
			}
			convertView.setPadding((int) (5 * density), (int) (5 * density), (int) (5 * density), (int) (5 * density));
		}
	}

	public void show(boolean show) {
		setVisibility(show? VISIBLE : GONE);
	}

}