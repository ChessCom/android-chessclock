package com.chess.backend.image_load;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.chess.R;
import com.chess.widgets.ProfileImageView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created with IntelliJ IDEA.
 * ProgressImageView.java use for show progress bar while image is downloading
 * <p/>
 * User: roger sent2roger@gmail.com
 *
 * @version 2.0.0
 * @created 27.07.2011
 * @modified 28.02.13
 */
public class ProgressImageView extends FrameLayout implements View.OnTouchListener {

	public static final int DEFAULT_IMG_SIZE = 80;
	public Bitmap placeholder;
	private ProfileImageView imageView;
	private Bitmap bitmap;
	public View progress;
	public Bitmap noImage;

	protected int size;
	protected float density;

	public ProgressImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	public ProgressImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		onCreate(attrs);
	}

	public ProgressImageView(Context context, int size) {
		super(context);
		this.size = size;
		onCreate(null);
	}

	protected void onCreate(AttributeSet attrs) {
		Resources resources = getResources();
		density = resources.getDisplayMetrics().density;
		int widthPixels = resources.getDisplayMetrics().widthPixels;

		if (attrs != null) {

			TypedArray array = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.layout_width});
			try {
				if (array.getLayoutDimension(0, DEFAULT_IMG_SIZE) == ViewGroup.LayoutParams.MATCH_PARENT) {
					size = widthPixels;
				} else if (array.getLayoutDimension(0, DEFAULT_IMG_SIZE) == ViewGroup.LayoutParams.WRAP_CONTENT) {
					size = DEFAULT_IMG_SIZE;  // TODO adjust
				} else {
					size = array.getDimensionPixelSize(0, DEFAULT_IMG_SIZE); // 0 for android.R.attr.layout_width
				}

			} finally {
				array.recycle();
			}
		} else {
			size *= density;
		}

		LayoutParams params = new LayoutParams(size, size);
		setLayoutParams(params);

		placeholder = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();
		noImage = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();

		{// image
			imageView = new ProfileImageView(getContext());
			LayoutParams photoParams = new LayoutParams(size, size);
			photoParams.gravity = Gravity.CENTER;

			imageView.setAdjustViewBounds(true);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);

			addView(imageView, photoParams);
		}

		{// progress
			progress = new ImageView(getContext());
			LayoutParams progressParams = new LayoutParams(size, size);
			progressParams.gravity = Gravity.CENTER;
			ColorDrawable colorDrawable = new ColorDrawable(0x80FFFFFF);
			((ImageView) progress).setImageDrawable(colorDrawable);

			progress.setVisibility(GONE);

			addView(progress, progressParams);
		}

		imageView.setOnTouchListener(this);

		initClickAnimation();
	}

	public View getProgressBar() {
		return progress;
	}


	@Override
	public void setId(int id) {
		super.setId(id);
		imageView.setId(id);
	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		super.setOnClickListener(listener);
		imageView.setOnClickListener(listener);
	}

	@Override
	public void setTag(int key, Object tag) {
		super.setTag(key, tag);
		imageView.setTag(key, tag);
	}

	public void setImageDrawable(Drawable drawable) {
		imageView.setImageDrawable(drawable);
	}

	public void setImageBitmap(Bitmap bitmap) {
		imageView.setImageBitmap(bitmap);
	}

	public void updateImageBitmap() {
		imageView.setImageBitmap(bitmap);
	}

	public ProfileImageView getImageView() {
		return imageView;
	}

	public void setImageView(ProfileImageView imageView) {
		this.imageView = imageView;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	/**
	 * Implement this method to handle touch screen motion events.
	 *
	 * @param event The motion event.
	 * @return True if the event was handled, false otherwise.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setPressed(true);

				animateClick();
				break;
			case MotionEvent.ACTION_UP:
				setPressed(false);

				break;
			case MotionEvent.ACTION_MOVE:
				setPressed(true);

				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setPressed(true);

				animateClick();

				break;
			case MotionEvent.ACTION_UP:
				setPressed(false);
				break;
		}
		return false;
	}

	private static final int DURATION = 500;
	private ObjectAnimator flipFirstHalf;
	private static final float alphaPressed = 0.2f;

	private void initClickAnimation() {
		final View animationView = imageView;

		flipFirstHalf = ObjectAnimator.ofFloat(animationView, "alpha", 1, alphaPressed, 1);
		flipFirstHalf.setDuration(DURATION);

		flipFirstHalf.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				performClick();
				imageView.performClick();
			}
		});
	}

	private void animateClick() {
		flipFirstHalf.start();
	}
}
