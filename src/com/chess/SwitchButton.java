package com.chess;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import com.chess.utilities.AppUtils;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.01.13
 * Time: 7:02
 */
public class SwitchButton extends RelativeLayout implements View.OnClickListener, Checkable {

	public static int BUTTON_ID = 0x00009999;
	public static int TEXT_ID = 0x00008888;

	private static final int TEXT_SIZE = 11;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final String TEXT_FONT = "Regular";
	private static final float HANDLE_SHIFT = -40;
	private static final float TEXT_SHIFT = 40f;
	private static final int TEXT_LEFT_PADDING = 13;
	private static final int HANDLE_BUTTON_WIDTH = 42;
	private static final int HANDLE_BUTTON_HEIGHT = 22;
	private static final float HANDLE_BUTTON_MARGIN = 2.5f;

	private Button handleButton;
	private RoboTextView textView;
	private boolean switchEnabled = true;
	private String textOnStr;
	private String textOffStr;

	private ObjectAnimator animateHandleLeftShift;
	private ObjectAnimator animateHandleRightShift;

	private ObjectAnimator animateTextLeftShift;
	private ObjectAnimator animateTextRightShift;
	private float handleShift;
	private float textShift;
	private float textSize;
	private int textColorOn;
	private int textColorOff;
	private String textFont;
	private int textLeftPadding;
	private boolean textUseShadow;
	private Drawable backOnDrawable;
	private Drawable backOffDrawable;

	private SwitchChangeListener switchChangeListener;


	public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate(context, attrs);
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(context, attrs);
	}

	private void onCreate(Context context, AttributeSet attrs) {
		Resources resources = getResources();

		float density = resources.getDisplayMetrics().density;
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton);
		Drawable handleDrawable = resources.getDrawable(R.drawable.button_switch_dark_handle_selector);
		backOnDrawable = resources.getDrawable(R.drawable.button_switch_dark_back);
		backOffDrawable = resources.getDrawable(R.drawable.button_switch_dark_back);
		int handleWidth = HANDLE_BUTTON_WIDTH;
		int handleHeight = HANDLE_BUTTON_HEIGHT;
		float handleMargin;

		try {
			backOnDrawable = array.getDrawable(R.styleable.SwitchButton_switchBackOn);
			backOffDrawable = array.getDrawable(R.styleable.SwitchButton_switchBackOff);
			handleDrawable = array.getDrawable(R.styleable.SwitchButton_switchHandle);
			handleWidth = array.getInteger(R.styleable.SwitchButton_switchHandleWidth, HANDLE_BUTTON_WIDTH);
			handleHeight = array.getInteger(R.styleable.SwitchButton_switchHandleHeight, HANDLE_BUTTON_HEIGHT);
			handleMargin = array.getFloat(R.styleable.SwitchButton_switchHandleMargin, HANDLE_BUTTON_MARGIN);
			handleShift = array.getFloat(R.styleable.SwitchButton_switchHandleShift, HANDLE_SHIFT);
			textShift = array.getFloat(R.styleable.SwitchButton_switchTextShift, TEXT_SHIFT);
			textLeftPadding = array.getInteger(R.styleable.SwitchButton_switchTextLeftPadding, TEXT_LEFT_PADDING);

			textSize = array.getDimension(R.styleable.SwitchButton_switchTextSize, TEXT_SIZE)/ density;
			textColorOn = array.getInteger(R.styleable.SwitchButton_switchTextColorOn, TEXT_COLOR);
			textColorOff = array.getInteger(R.styleable.SwitchButton_switchTextColorOff, TEXT_COLOR);
			textOnStr = array.getString(R.styleable.SwitchButton_switchTextOn);
			textOffStr = array.getString(R.styleable.SwitchButton_switchTextOff);
			textFont = array.getString(R.styleable.SwitchButton_switchTextFont);
			textUseShadow = array.getBoolean(R.styleable.SwitchButton_switchTextUseShadow, true);

		} finally {
			array.recycle();
		}

		textLeftPadding *= density;

		handleHeight *= density;
		handleWidth *= density;
		handleMargin *= density;

		handleShift *= density;
		textShift *= density;

		textOnStr = textOnStr.toUpperCase();
		textOffStr = textOffStr.toUpperCase();

		{// Button
			handleButton = new Button(context);
			RelativeLayout.LayoutParams buttonParams = new LayoutParams(handleWidth, handleHeight);
			buttonParams.addRule(RelativeLayout.CENTER_VERTICAL);
			buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			buttonParams.setMargins((int) handleMargin, 0 ,(int) handleMargin, 0);

			if (AppUtils.JELLYBEAN_PLUS_API) {
				handleButton.setBackground(handleDrawable);
			} else {
				handleButton.setBackgroundDrawable(handleDrawable);
			}

			handleButton.setId(BUTTON_ID);

			addView(handleButton, buttonParams);
		}

		{// Text
			textView = new RoboTextView(context);
			LayoutParams textParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			textParams.addRule(RelativeLayout.CENTER_VERTICAL);

			textView.setText(textOnStr);
			textView.setTextColor(textColorOn);
			textView.setTextSize(textSize);
			textView.setPadding(textLeftPadding, 0, 0, 0);
			textView.setFont(textFont);
			textView.setId(TEXT_ID);
			addView(textView, textParams);
		}
		initFlipAnimation();

		setOnClickListener(this);
		setBackOn(switchEnabled);
	}

	@Override
	public void onClick(View view) {
		toggle();
//		toggle(view);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		handleButton.setOnClickListener(l);
		textView.setOnClickListener(l);
	}

	private android.view.animation.Interpolator accelerator = new LinearInterpolator();
	private static final int DURATION = 70;

	private void initFlipAnimation() {
		animateHandleLeftShift = ObjectAnimator.ofFloat(handleButton, "translationX", 0f, handleShift);
		animateHandleLeftShift.setDuration(DURATION);
		animateHandleLeftShift.setInterpolator(accelerator);

		animateHandleRightShift = ObjectAnimator.ofFloat(handleButton, "translationX", handleShift, 0f);
		animateHandleRightShift.setDuration(DURATION);
		animateHandleRightShift.setInterpolator(accelerator);

		animateTextLeftShift = ObjectAnimator.ofFloat(textView, "translationX", 0f, textShift);
		animateTextLeftShift.setDuration(DURATION);
		animateTextLeftShift.setInterpolator(accelerator);

		animateTextRightShift = ObjectAnimator.ofFloat(textView, "translationX", textShift, 0f);
		animateTextRightShift.setDuration(DURATION);
		animateTextRightShift.setInterpolator(accelerator);
	}

	public void setSwitchChangeListener(SwitchChangeListener switchChangeListener) {
		this.switchChangeListener = switchChangeListener;
	}

	@Override
	public void setChecked(boolean checked) {
		switchEnabled = checked;
		invalidate();
		invalidateView();
	}

	@Override
	public boolean isChecked() {
		return switchEnabled;
	}

	@Override
	public void toggle() {
		switchEnabled = !switchEnabled;

		invalidateView();
	}

	private void invalidateView() {
		if (switchEnabled) {
			animateHandleRightShift.start();
			animateTextRightShift.start();

			textView.setText(textOnStr);
			textView.setTextColor(textColorOn);
		} else {
			// animate handle to the left
			animateHandleLeftShift.start();
			animateTextLeftShift.start();

			textView.setText(textOffStr);
			textView.setTextColor(textColorOff);
		}

		setBackOn(switchEnabled);

		if (switchChangeListener != null) {
			switchChangeListener.onSwitchChanged(this, switchEnabled);
		}
	}

	private void setBackOn(boolean on) {
		if (on) {
			if (AppUtils.JELLYBEAN_PLUS_API) {
				setBackground(backOnDrawable);
			} else {
				setBackgroundDrawable(backOnDrawable);
			}
		} else {
			if (AppUtils.JELLYBEAN_PLUS_API) {
				setBackground(backOffDrawable);
			} else {
				setBackgroundDrawable(backOffDrawable);
			}
		}

		if (textUseShadow) {
			float shadowRadius = 0.5f ;
			float shadowDx = 0;
			if (on) {
				float shadowDy = -1;
				textView.setShadowLayer(shadowRadius, shadowDx, shadowDy, 0x7F000000);
			} else {
				float shadowDy = 1;
				textView.setShadowLayer(shadowRadius, shadowDx, shadowDy, 0xFFFFFFFF);
			}
		}
	}

	public interface SwitchChangeListener {
		void onSwitchChanged(SwitchButton switchButton, boolean checked);
	}

}
