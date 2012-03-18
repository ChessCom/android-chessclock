package com.chess.utilities;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;

public abstract class BitmapLoader {
	public static final BitmapLoader instance;

	static {
		instance = Integer.parseInt(Build.VERSION.SDK) < 4 ? new Old() : new New();
	}

	public static Bitmap loadFromResource(Resources resources, int resId/*, BitmapFactory.Options options*/) {
		return instance.load(resources, resId/*, options*/);
	}

	private static class Old extends BitmapLoader {
		@Override
		Bitmap load(Resources resources, int resId/*, Options options*/) {
			return BitmapFactory.decodeResource(resources, resId/*, options*/);
		}
	}

	private static class New extends BitmapLoader {
		@Override
		Bitmap load(Resources resources, int resId/*, Options options*/) {
			Options options = new BitmapFactory.Options();
			//options.inScaled = false;
			options.inPurgeable = true;
			return BitmapFactory.decodeResource(resources, resId, options);
		}
	}

	abstract Bitmap load(Resources resources, int resId/*, BitmapFactory.Options options*/);
}
