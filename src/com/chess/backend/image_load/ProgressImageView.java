package com.chess.backend.image_load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * ProgressImageView.java use for show progress bar while image is downloading
 *
 * User: roger sent2roger@gmail.com
 * @version 2.0.0
 * @created 27.07.2011
 * @modified 28.02.13
 */
public class ProgressImageView extends FrameLayout {

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
	}

	@Override
	public void setId(int id) {
		super.setId(id);
		imageView.setId(id);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(l);
		imageView.setOnClickListener(l);
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
}
