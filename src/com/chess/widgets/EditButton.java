package com.chess.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.R;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;


/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.01.13
 * Time: 13:53
 */
public class EditButton extends RoboEditText {

	private int PADDING_SIDE;
	private int PADDING;

	private OnClickListener clickListener;
	private Drawable closeBtn;
	private float density;
	private int minButtonHeight;
	private int editTextColor;
	private int defaultTextColor;
	private int safePadding;
	private int defaultMinWidth;
	private Handler handler;

	public EditButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate(context);
	}

	public EditButton(Context context) {
		super(context);
		onCreate(context);
	}

	public EditButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(context);
	}

	private void onCreate(Context context) {
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;

		PADDING = (int) (10 * density);
		PADDING_SIDE = (int) (8 * density);
		safePadding = (int) (10 * density);

		handler = new Handler();

		minButtonHeight = (int) resources.getDimension(R.dimen.small_button_height);
		editTextColor = resources.getColor(R.color.new_edit_button_text);
		defaultTextColor = resources.getColor(R.color.new_light_grey);

		closeBtn = new IconDrawable(getContext(), R.string.ic_close);

		setSingleLine();

		defaultMinWidth = (int) resources.getDimension(R.dimen.button_min_width);
		showEditField(false);
	}

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

		int rightOffset = width - iconWidth - paddingRight - safePadding;
		// if user touched up at closeBoard button

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (event.getX() > rightOffset) {
					showEditField(false);
				} else {
					showEditField(true);
					if (clickListener != null) {
						clickListener.onClick(this);
					}
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
		Drawable[] compoundDrawables = getCompoundDrawables();
		if (show) {
			// change background
			ButtonDrawableBuilder.setBackgroundToView(this, R.style.Button_White);
			setPadding(PADDING, PADDING, PADDING_SIDE, PADDING);
			setHeight(minButtonHeight);
			setTextColor(editTextColor);

			setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1], closeBtn, compoundDrawables[3]);
			setCompoundDrawablePadding(PADDING_SIDE);
			setCursorVisible(true);

			AppUtils.showKeyBoard(getContext(), this);

		} else {
			setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1], null, compoundDrawables[3]);
			ButtonDrawableBuilder.setBackgroundToView(this, R.style.Button_Glassy);
			setTextColor(defaultTextColor);

			clearFocus();
			setCursorVisible(false);

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

