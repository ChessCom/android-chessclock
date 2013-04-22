package com.chess.backend.image_load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.chess.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created with IntelliJ IDEA.
 * ProgressImageView.java use for show progress bar while image is downloading
 *
 * User: roger sent2roger@gmail.com
 * @version 2.0.0
 * @created 27.07.2011
 * @modified 28.02.13
 */
public class ProgressImageView extends FrameLayout implements View.OnTouchListener {

	public Bitmap placeholder;
	private ImageView imageView;
	private Bitmap bitmap;
	public ProgressBar progress;
	public Bitmap noImage;

	private int size;

	public ProgressImageView(Context context, int size) {
		super(context);
		this.size = size;
		onCreate();
	}

	private void onCreate() {
		float density = getResources().getDisplayMetrics().density;

		size *= density;
		LayoutParams params = new LayoutParams(size, size);
		setLayoutParams(params);

		placeholder = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();
		noImage = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();

		{// image
			imageView = new ImageView(getContext());
			LayoutParams photoParams = new LayoutParams(size, size);
			photoParams.gravity = Gravity.CENTER;

			imageView.setAdjustViewBounds(true);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);

			addView(imageView, photoParams);
		}

		{// progress
			progress = new ProgressBar(getContext());
			LayoutParams progressParams = new LayoutParams(size / 2, size / 2);
			progressParams.gravity = Gravity.CENTER;
			progress.setVisibility(GONE);

			addView(progress, progressParams);
		}

		imageView.setOnTouchListener(this);

		initClickAnimation();
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

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
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
