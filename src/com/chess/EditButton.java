package com.chess;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.chess.utilities.AppUtils;


/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.01.13
 * Time: 13:53
 */
public class EditButton extends RoboEditText implements View.OnClickListener {

	private int PADDING_SIDE;
	private int PADDING;

	private OnClickListener clickListener;
	private Drawable closeBtn;
	private float density;
	private int minButtonHeight;
	private int editTextColor;

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
		density = context.getResources().getDisplayMetrics().density;

		PADDING = (int) (10 * density);
		PADDING_SIDE = (int) (4 * density);

		minButtonHeight = (int) getContext().getResources().getDimension(R.dimen.small_button_height);
		editTextColor = getContext().getResources().getColor(R.color.new_edit_button_text);
		setHint(""); // disable hint
		closeBtn = getContext().getResources().getDrawable(R.drawable.ic_clear_text);
		setSingleLine();
		setFont(RoboTextView.BOLD_FONT);

	}

	@Override
	public void onClick(View v) {
		Toast.makeText(getContext(), " image clicked", Toast.LENGTH_SHORT).show();
	}


	public void addOnClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// get width of editText
		int width = getWidth();
		// get width of our close icon
		int iconWidth = closeBtn.getIntrinsicWidth();
		// add right padding
		int paddingRight = getPaddingRight();

		int rightOffset = width - iconWidth - paddingRight;
		// if user touched up at close button

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

			AppUtils.showKeyBoard(getContext(), this);

		} else {
			setCompoundDrawablesWithIntrinsicBounds(getCompoundDrawables()[0], getCompoundDrawables()[1], null, getCompoundDrawables()[3]);
			setBackgroundResource(R.drawable.button_grey_solid_selector);
			setTextColor(0xFFFFFFFF);
			float shadowRadius = 0.5f;
			float shadowDx = 0 ;
			float shadowDy = -1 ;
			setShadowLayer(shadowRadius, shadowDx, shadowDy, Color.BLACK);
			clearFocus();
			setCursorVisible(false);

			Handler handler = new Handler();
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

