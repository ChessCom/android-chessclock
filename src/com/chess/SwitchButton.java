package com.chess;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.*;
import android.widget.Button;

import android.widget.RelativeLayout;
import com.chess.utilities.AppUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.01.13
 * Time: 7:02
 */
public class SwitchButton extends RelativeLayout implements View.OnClickListener {

	public static final int TEXT_SIZE = 11;

	public float HANDLE_SHIFT = -40f;
	public float TEXT_RIGHT_SHIFT = 40f;
	public static int BUTTON_ID = 0x00009999;
	public static int TEXT_ID = 0x00008888;


	private Button handleButton;
	private RoboTextView textView;
	private boolean switchEnabled;
	private String yesStr;
	private String noStr;
	private int TEXT_LEFT_PADDING = 13;

	private ObjectAnimator animateHandleLeftShift;
	private ObjectAnimator animateHandleRightShift;
	private int HANDLE_BUTTON_HEIGHT = 22;
	private int HANDLE_BUTTON_WIDTH = 42;
	private ObjectAnimator animateTextLeftShift;
	private ObjectAnimator animateTextRightShift;


	public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate(context);
	}

	public SwitchButton(Context context) {
		super(context);
		onCreate(context);
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(context);
	}

	private void onCreate(Context context) {

		float density = context.getResources().getDisplayMetrics().density;

		TEXT_LEFT_PADDING *= density;

		HANDLE_BUTTON_HEIGHT *= density;
		HANDLE_BUTTON_WIDTH *= density;

		HANDLE_SHIFT *= density;
		TEXT_RIGHT_SHIFT *= density;

		yesStr = getContext().getString(R.string.yes).toUpperCase();
		noStr = getContext().getString(R.string.no).toUpperCase();

		{// Button
			handleButton = new Button(getContext());
			RelativeLayout.LayoutParams buttonParams = new LayoutParams(HANDLE_BUTTON_WIDTH, HANDLE_BUTTON_HEIGHT);
			buttonParams.addRule(RelativeLayout.CENTER_VERTICAL);
			buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			handleButton.setBackgroundResource(R.drawable.button_switch_handle_selector);
			handleButton.setId(BUTTON_ID);

			addView(handleButton, buttonParams);
		}


		{// Text
			textView = new RoboTextView(getContext());
			LayoutParams textParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			textParams.addRule(RelativeLayout.CENTER_VERTICAL);

			textView.setText(yesStr);
			textView.setTextColor(getContext().getResources().getColor(R.color.new_normal_grey));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
			textView.setPadding(TEXT_LEFT_PADDING, 0, 0, 0);
			textView.setFont(RoboTextView.HELV_NEUE_FONT);
			textView.setId(TEXT_ID);
			float shadowRadius = 0.5f ;
			float shadowDx = 0;
			float shadowDy = 1;
			textView.setShadowLayer(shadowRadius, shadowDx, shadowDy, Color.BLACK);

			addView(textView, textParams);
		}
		initFlipAnimation();

		setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == SwitchButton.BUTTON_ID || view.getId() == SwitchButton.TEXT_ID){
			toggle(view);
		}
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		handleButton.setOnClickListener(l);
		textView.setOnClickListener(l);
	}

	public void toggle(View view){
		if (AppUtils.HONEYCOMB_PLUS_API && view.getId() == TEXT_ID) { // ignore text clicks
			return;
		}

		switchEnabled = !switchEnabled;

		if (switchEnabled) {
			// animate handle to the left
			animateHandleLeftShift.start();
			animateTextLeftShift.start();

			textView.setText(noStr);
		} else {
			animateHandleRightShift.start();
			animateTextRightShift.start();

			textView.setText(yesStr);
		}
	}

	public boolean isSwitchEnabled() {
		return switchEnabled;
	}

	private android.view.animation.Interpolator accelerator = new LinearInterpolator();
	private static final int DURATION = 70;

	private void initFlipAnimation() {


		animateHandleLeftShift = ObjectAnimator.ofFloat(handleButton, "translationX", 0f, HANDLE_SHIFT);
		animateHandleLeftShift.setDuration(DURATION);
		animateHandleLeftShift.setInterpolator(accelerator);

		animateHandleLeftShift.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				// inform listener
			}
		});

		animateHandleRightShift = ObjectAnimator.ofFloat(handleButton, "translationX", HANDLE_SHIFT, 0f);
		animateHandleRightShift.setDuration(DURATION);
		animateHandleRightShift.setInterpolator(accelerator);

		animateHandleRightShift.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				// inform listener
			}
		});


		animateTextLeftShift = ObjectAnimator.ofFloat(textView, "translationX", 0f, TEXT_RIGHT_SHIFT);
		animateTextLeftShift.setDuration(DURATION);
		animateTextLeftShift.setInterpolator(accelerator);

		animateTextRightShift = ObjectAnimator.ofFloat(textView, "translationX", TEXT_RIGHT_SHIFT, 0f);
		animateTextRightShift.setDuration(DURATION);
		animateTextRightShift.setInterpolator(accelerator);
	}

}
