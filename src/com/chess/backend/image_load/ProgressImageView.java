package com.chess.backend.image_load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
	public ImageView imageView;
	public Bitmap bitmap;
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
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
		setLayoutParams(params);

		placeholder = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();
		noImage = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_picture_stub)).getBitmap();

		{// image
			imageView = new ImageView(getContext());
			FrameLayout.LayoutParams photoParams = new FrameLayout.LayoutParams(size, size);
			photoParams.gravity = Gravity.CENTER;

			imageView.setAdjustViewBounds(true);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);

			addView(imageView, photoParams);
		}
		{// progress
			progress = new ProgressBar(getContext());
			FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(size / 2, size / 2);
			progressParams.gravity = Gravity.CENTER;
			progress.setVisibility(GONE);

			addView(progress, progressParams);
		}
	}

	public void showProgress() {

	}
/*
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/progressImage"
    android:layout_width="50dip"
    android:layout_height="50dip"
    >

    <ImageView
        android:id="@+id/photoImg"
        android:layout_width="50dip"
        android:layout_height="50dip"
        android:layout_gravity="center"
        android:adjustViewBounds="false"
        android:scaleType="fitXY"
        android:contentDescription="@string/image"
        />

    <ProgressBar
        android:id="@+id/imageProgressBar"
        style="@android:style/Widget.ProgressBar.Inverse"
        android:layout_width="20dip"
        android:layout_height="20dip"
        android:layout_gravity="center"
        android:visibility="gone"
        />

</FrameLayout>
 */

}
