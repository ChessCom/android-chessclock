package com.chess.ui.views.drawables.smart_button;

import android.graphics.*;
import android.graphics.drawable.shapes.RectShape;

/**
 * Used to fix NPE on PRE-ICS version if passing outerRadii as null
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.05.13
 * Time: 15:16
 */
public class RoundRectShapeFixed extends RectShape {
	private float[] mOuterRadii;
	private RectF mInset;
	private float[] mInnerRadii;

	private RectF mInnerRect;
	private Path mPath;    // this is what we actually draw

	/**
	 * RoundRectShapeFixed constructor.
	 * Specifies an outer (round)rect and an optional inner (round)rect.
	 *
	 * @param outerRadii An array of 8 radius values, for the outer roundrect.
	 *                   The first two floats are for the
	 *                   top-left corner (remaining pairs correspond clockwise).
	 *                   For no rounded corners on the outer rectangle,
	 *                   pass null.
	 * @param inset      A RectF that specifies the distance from the inner
	 *                   rect to each side of the outer rect.
	 *                   For no inner, pass null.
	 * @param innerRadii An array of 8 radius values, for the inner roundrect.
	 *                   The first two floats are for the
	 *                   top-left corner (remaining pairs correspond clockwise).
	 *                   For no rounded corners on the inner rectangle,
	 *                   pass null.
	 *                   If inset parameter is null, this parameter is ignored.
	 */
	public RoundRectShapeFixed(float[] outerRadii, RectF inset,
							   float[] innerRadii) {
		if (outerRadii != null && outerRadii.length < 8) {
			throw new ArrayIndexOutOfBoundsException("outer radii must have >= 8 values");
		}
		if (innerRadii != null && innerRadii.length < 8) {
			throw new ArrayIndexOutOfBoundsException("inner radii must have >= 8 values");
		}
		mOuterRadii = outerRadii;
		mInset = inset;
		mInnerRadii = innerRadii;

		if (inset != null) {
			mInnerRect = new RectF();
		}
		mPath = new Path();
	}

	@Override
	public void draw(Canvas canvas, Paint paint) {
		canvas.drawPath(mPath, paint);
	}

	@Override
	protected void onResize(float w, float h) {
		super.onResize(w, h);

		RectF r = rect();
		mPath.reset();

		if (mOuterRadii != null) {
			mPath.addRoundRect(r, mOuterRadii, Path.Direction.CW);
		} else {
			mPath.addRect(r, Path.Direction.CW);
		}
		if (mInnerRect != null) {
			mInnerRect.set(r.left + mInset.left, r.top + mInset.top,
					r.right - mInset.right, r.bottom - mInset.bottom);
			if (mInnerRect.width() < w && mInnerRect.height() < h) {
				if (mInnerRadii != null) {
					mPath.addRoundRect(mInnerRect, mInnerRadii, Path.Direction.CCW);
				} else {
					mPath.addRect(mInnerRect, Path.Direction.CCW);
				}
			}
		}
	}

	@Override
	public RoundRectShapeFixed clone() throws CloneNotSupportedException {
		RoundRectShapeFixed shape = (RoundRectShapeFixed) super.clone();
		shape.mOuterRadii = mOuterRadii != null ? mOuterRadii.clone() : null;
		shape.mInnerRadii = mInnerRadii != null ? mInnerRadii.clone() : null;
		shape.mInset = new RectF(mInset);
		shape.mInnerRect = new RectF(mInnerRect);
		shape.mPath = new Path(mPath);
		return shape;
	}
}
