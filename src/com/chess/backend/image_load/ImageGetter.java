package com.chess.backend.image_load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;
import com.chess.utilities.AppUtils;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.10.13
 * Time: 8:56
 */
public class ImageGetter implements Html.ImageGetter {

	private HashMap<String, TextImage> mImageCache;
	private Context context;
	private TextView textView;
	private String sourceText;
	private int imageWidth;

	public ImageGetter(Context context, HashMap<String, TextImage> mImageCache, TextView textView, String sourceText, int imageSize) {
		this.context = context;
		this.textView = textView;
		this.sourceText = sourceText;
		// TODO rework properly
		int sidePadding = (int) (15 * context.getResources().getDisplayMetrics().density);
		this.imageWidth = imageSize - sidePadding * 2;

		this.mImageCache = mImageCache;
	}

	@Override
	public Drawable getDrawable(final String source) {
		if (source.contains("gif")) {
			return null;
		}

		TextImage textImage = mImageCache.get(source);

		if (textImage != null && textImage.drawable != null) {
			return textImage.drawable;
		} else {

			mImageCache.put(source, new TextImage(textView, sourceText));

			new ImageDownloaderToListener(context).download(source, new ImageReadyListenerLight() {
				@Override
				public void onImageReady(Bitmap bitmap) {
					if (bitmap != null) {

						float originalWidth = bitmap.getWidth();
						float originalHeight = bitmap.getHeight();
						float scale = originalWidth / originalHeight;

						int newHeight = (int) (imageWidth / scale);

//						try {
							bitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, newHeight, true);
//						} catch (OutOfMemoryError ignore) {
//							return;
//						}

						AppUtils.logMemData();

						BitmapDrawable loadedDrawable = new BitmapDrawable(context.getResources(), bitmap);
						loadedDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());

						TextImage targetTextImage = mImageCache.get(source);
						targetTextImage.drawable = loadedDrawable;
						mImageCache.put(source, targetTextImage);

						if (textView != null) {
							textView.setText(Html.fromHtml(targetTextImage.sourceStr, ImageGetter.this, null));
						}
					}
				}
			}, imageWidth);

			return null;
		}
	}

	public static class TextImage {
		public Drawable drawable;
		public TextView textView;
		public String sourceStr;

		public TextImage(TextView textView, String sourceStr) {
			this.textView = textView;
			this.sourceStr = sourceStr;
		}
	}
}