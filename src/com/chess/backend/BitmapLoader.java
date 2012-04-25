package com.chess.backend;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public abstract class BitmapLoader {
	public static final BitmapLoader instance;

	static {
		instance = new New();
	}

	public static Bitmap loadFromResource(Resources resources, int resId/*, BitmapFactory.Options options*/) {
		return instance.load(resources, resId/*, options*/);
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
