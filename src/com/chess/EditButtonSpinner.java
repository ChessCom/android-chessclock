package com.chess;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;


/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.01.13
 * Time: 13:53
 */
public class EditButtonSpinner extends RoboAutoCompleteTextView {

	private int PADDING_SIDE;
	private int PADDING;

	private OnClickListener clickListener;
	private Drawable closeBtn;
	private float density;
	private int minButtonHeight;
	private int editTextColor;
	private int defaultMinWidth;
	private Handler handler;

	public EditButtonSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate(context);
	}

	public EditButtonSpinner(Context context) {
		super(context);
		onCreate(context);
	}

	public EditButtonSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(context);
	}



	private void onCreate(Context context) {
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;

		PADDING = (int) (10 * density);
		PADDING_SIDE = (int) (4 * density);

		handler = new Handler();

		minButtonHeight = (int) resources.getDimension(R.dimen.small_button_height);
		editTextColor = resources.getColor(R.color.new_edit_button_text);
		setHint(""); // disable hint
		closeBtn = resources.getDrawable(R.drawable.ic_clear_text);
		setSingleLine();
		setFont(RoboTextView.BOLD_FONT);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line, COUNTRIES);
		setAdapter(adapter);

		defaultMinWidth = (int) resources.getDimension(R.dimen.button_min_width);
	}

	private static final String[] COUNTRIES = new String[] {
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"Belgium",
			"France", "Italy", "Germany", "Spain"
	};

	public void addOnClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// get width of editText
		int width = getWidth();
		// get width of our closeBoard icon
		int iconWidth = closeBtn.getIntrinsicWidth();
		// add right padding
		int paddingRight = getPaddingRight();

		int rightOffset = width - iconWidth - paddingRight;
		// if user touched up at closeBoard button

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				showEditField(true);
				if (clickListener != null) {
					clickListener.onClick(this);
				}


				break;
			case MotionEvent.ACTION_UP:
				if (event.getX() > rightOffset) {

					showEditField(false);

				}
				break;
		}
		return super.onTouchEvent(event);
	}

	private void showEditField(boolean show) {
		if (show) {
			// change background
			setBackgroundResource(R.drawable.edit_button_white_one);
			setPadding(PADDING, PADDING, PADDING_SIDE, PADDING);
			setHeight(minButtonHeight);
			setTextColor(editTextColor);

			setShadowLayer(0, 0, 0, Color.TRANSPARENT);
			setCompoundDrawablesWithIntrinsicBounds(getCompoundDrawables()[0], getCompoundDrawables()[1], closeBtn, getCompoundDrawables()[3]);
			setCursorVisible(true);

			setMinWidth(300); // TODO adjust properly

			AppUtils.showKeyBoard(getContext(), this);

		} else {
			setCompoundDrawablesWithIntrinsicBounds(getCompoundDrawables()[0], getCompoundDrawables()[1], null, getCompoundDrawables()[3]);
			ButtonDrawableBuilder.setBackgroundToView(this, R.style.Button_Grey_Solid);
//			setBackgroundResource(R.drawable.button_grey_solid_selector);
			setTextColor(Color.WHITE);
			float shadowRadius = 0.5f;
			float shadowDx = 0 ;
			float shadowDy = -1 ;
			setShadowLayer(shadowRadius, shadowDx, shadowDy, Color.BLACK);

			clearFocus();
			setCursorVisible(false);

			setMinWidth(defaultMinWidth);


			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					setCursorVisible(false);
					AppUtils.hideKeyBoard(getContext(), getRootView());
				}
			}, 50);

		}

	}

}

