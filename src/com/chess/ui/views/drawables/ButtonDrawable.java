package com.chess.ui.views.drawables;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import com.chess.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.05.13
 * Time: 18:40
 */
public class ButtonDrawable extends StateListDrawable {

	private static final int DEFAULT_RADIUS = 4;
	private static final int DEFAULT_ANGLE = 270;
	private static final int DEFAULT_PADDING = 2;
	private static final int DEFAULT_BEVEL_INSET = 1;
	private final LayerDrawable enabledDrawable;
	private final LayerDrawable pressedDrawable;
	private final Resources resources;

	private final int levelCnt;
	private final float[] outerR;

	private int colorOuterBorder;
	private int colorBottom;
	private int colorRight;
	private int colorLeft;
	private int colorTop;
	private int colorBottom2;
	private int colorRight2;
	private int colorLeft2;
	private int colorTop2;
	private int colorSolid;
	private int colorGradientStart;
	private int colorGradientCenter;
	private int colorGradientEnd;

	private int colorBottomP;
	private int colorRightP;
	private int colorLeftP;
	private int colorTopP;
	private int colorBottom2P;
	private int colorRight2P;
	private int colorLeft2P;
	private int colorTop2P;
	private int colorSolidP;
	private int colorGradientStartP;
	private int colorGradientCenterP;
	private int colorGradientEndP;

	private int padding;
	private int gradientAngle;
	private boolean isSolid;
	private int bevelLvl;
	private int bevelInset;
	private int radius;

	private boolean initialized;
	private boolean pressed;
	private boolean enabled;

	public ButtonDrawable(Context context, AttributeSet attrs) {
		resources = context.getResources();

		// defaults
		bevelLvl = 1;
		isSolid = true;
		padding = DEFAULT_PADDING;
		radius = DEFAULT_RADIUS;

		parseAttributes(context, attrs);

		outerR = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

		List<LayerInfo> enabledLayers = new ArrayList<LayerInfo>();
		List<LayerInfo> pressedLayers = new ArrayList<LayerInfo>();

		int[] topInset = new int[]{0, 0, 0, 1};  // TODO improve
		int[] bottomInset = new int[]{1, 1, 0, 0};
		int[] leftInset = new int[]{1, 0, 0, 1};
		int[] rightInset = new int[]{1, 1, 0, 1};
		int[] buttonInset = new int[]{1, 1, 1, 1};


		{ // outer border
			int strokeSize = resources.getDimensionPixelSize(R.dimen.default_stroke_width);
			RectF stroke = new RectF(strokeSize, strokeSize, strokeSize, strokeSize);

			RoundRectShape rectShape = new RoundRectShape(outerR, stroke, outerR);
			ShapeDrawable shapeDrawable = new ShapeDrawable(rectShape);
			shapeDrawable.getPaint().setColor(colorOuterBorder);

			enabledLayers.add(new LayerInfo(shapeDrawable, 0, 0, 0, 0));
			pressedLayers.add(new LayerInfo(shapeDrawable, 0, 0, 0, 0));  // TODO check
		}

		if (bevelLvl == 1) {
			createLayer(colorTop, topInset, enabledLayers);
			createLayer(colorBottom, bottomInset, enabledLayers);
			createLayer(colorLeft, leftInset, enabledLayers);
			createLayer(colorRight, rightInset, enabledLayers);
			// Pressed
			createLayer(colorTopP, topInset, pressedLayers);
			createLayer(colorBottomP, bottomInset, pressedLayers);
			createLayer(colorLeftP, leftInset, pressedLayers);
			createLayer(colorRightP, rightInset, pressedLayers);
		}

		if (isSolid) {
			createLayer(colorSolid, buttonInset, enabledLayers);
			createLayer(colorSolidP, buttonInset, pressedLayers);
		}

		levelCnt = enabledLayers.size();
		Drawable[] enabledDrawables = new Drawable[levelCnt];  // TODO improve that mess
		for (int i = 0; i < levelCnt; i++) {
			LayerInfo layerInfo = enabledLayers.get(i);
			enabledDrawables[i] = layerInfo.shapeDrawable;
		}
		enabledDrawable = new LayerDrawable(enabledDrawables);
		for (int i = 1; i < levelCnt; i++) { // start from 2nd level, first is shadow
			LayerInfo layer = enabledLayers.get(i);
			enabledDrawable.setLayerInset(i, layer.leftInSet, layer.topInSet, layer.rightInSet, layer.bottomInSet);
		}

		Drawable[] pressedDrawables = new Drawable[levelCnt]; // TODO improve that mess
		for (int i = 0; i < levelCnt; i++) {
			LayerInfo layerInfo = pressedLayers.get(i);
			pressedDrawables[i] = layerInfo.shapeDrawable;
		}
		pressedDrawable = new LayerDrawable(pressedDrawables);
		for (int i = 1; i < levelCnt; i++) { // start from 2nd level, first is shadow
			LayerInfo layer = pressedLayers.get(i);
			pressedDrawable.setLayerInset(i, layer.leftInSet, layer.topInSet, layer.rightInSet, layer.bottomInSet);
		}


		addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
		addState(new int[]{android.R.attr.state_enabled}, enabledDrawable);
	}

	private void createLayer(int color, int[] inSet, List<LayerInfo> layers) {
		ShapeDrawable drawable = new ShapeDrawable(new RoundRectShape(outerR, null, null));
		drawable.getPaint().setColor(color);
		layers.add(new LayerInfo(drawable, bevelInset + inSet[0], bevelInset + inSet[1],
										   bevelInset + inSet[2], bevelInset + inSet[3]));
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		if (!initialized) {
			iniLayers(width, height);
		}
	}

	private void iniLayers(int width, int height) {
		if (!isSolid) {
//			((ShapeDrawable) layerDrawable.getDrawable(levelCnt)).getPaint().setShader(makeLinear(width, height,
//					colorGradientStart, colorGradientCenter, colorGradientEnd));
		}

		initialized = true;
	}

	private static Shader makeLinear(int width, int height, int startColor, int centerColor, int endColor) {
		return new LinearGradient(0, 0, width, height,
				new int[]{startColor, centerColor, endColor},
				null, Shader.TileMode.REPEAT);
	}

	@Override
	public void setAlpha(int alpha) {
		enabledDrawable.setAlpha(alpha);
		pressedDrawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		enabledDrawable.setColorFilter(cf);
		pressedDrawable.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	private void parseAttributes(Context context, AttributeSet attrs) {
		// get style
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoboButton);

		try { // values
			if (array.hasValue(R.styleable.RoboButton_btn_radius)) {
				radius = array.getDimensionPixelSize(R.styleable.RoboButton_btn_radius, DEFAULT_RADIUS);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_is_solid)) {
				isSolid = array.getBoolean(R.styleable.RoboButton_btn_is_solid, true);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_gradient_angle)) {
				gradientAngle = array.getInt(R.styleable.RoboButton_btn_gradient_angle, DEFAULT_ANGLE);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_padding)) {
				padding = array.getDimensionPixelSize(R.styleable.RoboButton_btn_padding, DEFAULT_PADDING);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_bevel_lvl)) {
				bevelLvl = array.getInt(R.styleable.RoboButton_btn_bevel_lvl, 1);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_bevel_inset)) {
				bevelInset = array.getDimensionPixelSize(R.styleable.RoboButton_btn_bevel_inset, DEFAULT_BEVEL_INSET);
			}

			// Colors for bevel
			if (array.hasValue(R.styleable.RoboButton_btn_outer_border)) {
				colorOuterBorder = array.getInt(R.styleable.RoboButton_btn_outer_border, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_top)) {
				colorTop = array.getInt(R.styleable.RoboButton_btn_top, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_left)) {
				colorLeft = array.getInt(R.styleable.RoboButton_btn_left, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_right)) {
				colorRight = array.getInt(R.styleable.RoboButton_btn_right, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_bottom)) {
				colorBottom = array.getInt(R.styleable.RoboButton_btn_bottom, 0xFFFFFFFF);
			}

			// Level 2 for bevel
			if (array.hasValue(R.styleable.RoboButton_btn_top_2)) {
				colorTop2 = array.getInt(R.styleable.RoboButton_btn_top_2, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_left_2)) {
				colorLeft2 = array.getInt(R.styleable.RoboButton_btn_left_2, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_right_2)) {
				colorRight2 = array.getInt(R.styleable.RoboButton_btn_right_2, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_bottom_2)) {
				colorBottom2 = array.getInt(R.styleable.RoboButton_btn_bottom_2, 0xFFFFFFFF);
			}

			// Button colors
			if (array.hasValue(R.styleable.RoboButton_btn_solid)) {
				colorSolid = array.getInt(R.styleable.RoboButton_btn_solid, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_gradient_start)) {
				colorGradientStart = array.getInt(R.styleable.RoboButton_btn_gradient_start, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_gradient_center)) {
				colorGradientCenter = array.getInt(R.styleable.RoboButton_btn_gradient_center, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_gradient_end)) {
				colorGradientEnd = array.getInt(R.styleable.RoboButton_btn_gradient_end, 0xFFFFFFFF);
			}

			/* ---------------------- Pressed states colors -------------------------------------------*/
			if (array.hasValue(R.styleable.RoboButton_btn_top_p)) {
				colorTopP = array.getInt(R.styleable.RoboButton_btn_top_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_left_p)) {
				colorLeftP = array.getInt(R.styleable.RoboButton_btn_left_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_right_p)) {
				colorRightP = array.getInt(R.styleable.RoboButton_btn_right_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_bottom_p)) {
				colorBottomP = array.getInt(R.styleable.RoboButton_btn_bottom_p, 0xFFFFFFFF);
			}

			// Level 2 Pressed
			if (array.hasValue(R.styleable.RoboButton_btn_top_2_p)) {
				colorTop2P = array.getInt(R.styleable.RoboButton_btn_top_2_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_left_2_p)) {
				colorLeft2P = array.getInt(R.styleable.RoboButton_btn_left_2_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_right_2_p)) {
				colorRight2P = array.getInt(R.styleable.RoboButton_btn_right_2_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_bottom_2_p)) {
				colorBottom2P = array.getInt(R.styleable.RoboButton_btn_bottom_2_p, 0xFFFFFFFF);
			}

			// Button colors Pressed
			if (array.hasValue(R.styleable.RoboButton_btn_solid_p)) {
				colorSolidP = array.getInt(R.styleable.RoboButton_btn_solid_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_gradient_start_p)) {
				colorGradientStartP = array.getInt(R.styleable.RoboButton_btn_gradient_start_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_gradient_center_p)) {
				colorGradientCenterP = array.getInt(R.styleable.RoboButton_btn_gradient_center_p, 0xFFFFFFFF);
			}
			if (array.hasValue(R.styleable.RoboButton_btn_gradient_end_p)) {
				colorGradientEndP = array.getInt(R.styleable.RoboButton_btn_gradient_end_p, 0xFFFFFFFF);
			}


		} finally {
			array.recycle();
		}
	}

	private class LayerInfo {
		int leftInSet;
		int topInSet;
		int rightInSet;
		int bottomInSet;
		ShapeDrawable shapeDrawable;

		public LayerInfo(ShapeDrawable shapeDrawable, int leftInSet, int topInSet, int rightInSet, int bottomInSet) {
			this.shapeDrawable = shapeDrawable;
			this.leftInSet = leftInSet;
			this.topInSet = topInSet;
			this.rightInSet = rightInSet;
			this.bottomInSet = bottomInSet;
		}
	}


/*


    <!--Emboss Top Level 1-->
        android:top="0px"
        android:left="0px"
        android:right="0px"
        android:bottom="1px"

    <!--Emboss Bottom Level 2-->
        android:top="1px"
        android:left="1px"
        android:right="0px"
        android:bottom="0px"

    <!--Emboss Left Level 1-->
        android:top="1px"
        android:left="0px"
        android:right="0px"
        android:bottom="1px"

    <!--Emboss Right Level 2-->
        android:top="1px"
        android:left="1px"
        android:right="0px"
        android:bottom="1px"

    <!-- Grey config -->

        android:top="2px"
        android:left="2px"
        android:right="2px"
        android:bottom="3px"

		android:top="3px"
        android:left="2px"
        android:right="2px"
        android:bottom="2px"

        android:top="3px"
        android:left="2px"
        android:right="2px"
        android:bottom="3px"

        android:top="3px"
        android:left="3px"
        android:right="2px"
        android:bottom="3px"

*/
}
