package com.chess.ui.views.drawables.smart_button;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import com.chess.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.05.13
 * Time: 22:15
 */
public class RectButtonDrawable extends ButtonDrawable {

	static final int TOP_LEFT = 0;
	static final int TOP_MIDDLE = 1;
	static final int TOP_RIGHT = 2;

	static final int TAB_LEFT = 3;
	static final int TAB_MIDDLE = 4;
	static final int TAB_RIGHT = 5;

	static final int BOTTOM_LEFT = 6;
	static final int BOTTOM_MIDDLE = 7;
	static final int BOTTOM_RIGHT = 8;

	int rectPosition;

	private static final int edgeOffset = 10;

	/* state & other values */
	private boolean initialized;

	/**
	 * Use for init ButtonDrawableBuilder
	 */
	RectButtonDrawable() {
		setDefaults();
	}

	public RectButtonDrawable(Context context, AttributeSet attrs) {
		Resources resources = context.getResources();

		setDefaults();

		parseAttributes(context, attrs);

		init(resources);
	}

	private void setDefaults() {
		// defaults
		bevelLvl = 1;
		isSolid = true;
		radius = DEFAULT_RADIUS;

		disabledAlpha = 100;
		enabledAlpha = 0xFF;
	}

	@Override
	void init(Resources resources) {
		float density = resources.getDisplayMetrics().density;
		outerRect = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
		bevelSize = resources.getDimensionPixelSize(R.dimen.default_bevel_size);
		bevelRect = new RectF(bevelSize, bevelSize, bevelSize, bevelSize);

		PRESSED_OVERLAY = resources.getColor(R.color.rect_button_overlay_p);
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.OVERLAY);
		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.XOR);

		List<LayerInfo> enabledLayers = new ArrayList<LayerInfo>();
		List<LayerInfo> pressedLayers = new ArrayList<LayerInfo>();

		insetOne = new InsetInfo();
		int in1 = (int) (1 * density);
		bevelInset = 0;
		insetOne.top = new int[]{bevelInset + 0, bevelInset + 0, bevelInset + 0, bevelInset + in1};
		insetOne.left = new int[]{bevelInset + 0, bevelInset + in1, bevelInset + 0, bevelInset + in1};
		insetOne.right = new int[]{bevelInset + in1, bevelInset + in1, bevelInset + 0, bevelInset + in1};
		insetOne.bottom = new int[]{bevelInset /*+ in1*/, bevelInset + in1, bevelInset + in1, bevelInset + 0};
		insetOne.button = new int[]{bevelInset + in1, bevelInset + in1, bevelInset + in1, bevelInset + in1};

		insetTwo = new InsetInfo();
		int in2 = (int) (2 * density);
		insetTwo.top = new int[]{insetOne.top[0] + 0, insetOne.top[1] + 0, insetOne.top[2] + 0, insetOne.top[3] + in2};
		insetTwo.left = new int[]{insetOne.left[0] + 0, insetOne.left[1] + in2, insetOne.left[2] + 0, insetOne.left[3] + in2};
		insetTwo.right = new int[]{insetOne.right[0] + in2, insetOne.right[1] + in2, insetOne.right[2] + 0, insetOne.right[3] + in2};
		insetTwo.bottom = new int[]{insetOne.bottom[0] + in2, insetOne.bottom[1] + in2, insetOne.bottom[2] + in2, insetOne.bottom[3] + 0};
		insetTwo.button = new int[]{insetOne.button[0] + in2, insetOne.button[1] + in2, insetOne.button[2] + in2, insetOne.button[3] + in2};


		if (useBorder) { // outer border
			int strokeSize = resources.getDimensionPixelSize(R.dimen.default_stroke_width);

			RectF stroke = new RectF(strokeSize, strokeSize, strokeSize, strokeSize);

			RoundRectShape rectShape = new RoundRectShape(outerRect, stroke, outerRect);
			ShapeDrawable shapeDrawable = new ShapeDrawable(rectShape);
			shapeDrawable.getPaint().setColor(colorOuterBorder);
//			shapeDrawable.getPaint().setColor(0xFFFF0000);

			enabledLayers.add(new LayerInfo(shapeDrawable, 0, 0, 0, 0));
			if (usePressedLayer) {
				pressedLayers.add(new LayerInfo(shapeDrawable, 0, 0, 0, 0));
			}
		}

		createDefaultState(enabledLayers);

		if (usePressedLayer) { // by default we apply color filter and alpha for different states
			createPressedState(pressedLayers);
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if (!initialized) {
			iniLayers(canvas);
		}

		switch (rectPosition) {
			case TOP_LEFT:
				enabledDrawable.setBounds(-edgeOffset, -edgeOffset, canvas.getWidth() + edgeOffset, canvas.getHeight());
				break;
			case TOP_MIDDLE:
				enabledDrawable.setBounds(0, -edgeOffset, canvas.getWidth() + edgeOffset, canvas.getHeight());
				break;
			case TOP_RIGHT:
				enabledDrawable.setBounds(0, -edgeOffset, canvas.getWidth() + edgeOffset, canvas.getHeight());
				break;

			case TAB_LEFT:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth(), canvas.getHeight());
				break;
			case TAB_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth(), canvas.getHeight());
				break;
			case TAB_RIGHT:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth() + edgeOffset, canvas.getHeight());
				break;

			case BOTTOM_LEFT:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth(), canvas.getHeight() + edgeOffset);
				break;
			case BOTTOM_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth(), canvas.getHeight() + edgeOffset);
				break;
			case BOTTOM_RIGHT:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth() + edgeOffset, canvas.getHeight() + edgeOffset);
				break;

		}
		super.draw(canvas);
	}

	@Override
	protected void createDefaultState(List<LayerInfo> enabledLayers) {   // TODO it can be improved!
		createLayer(colorTop, insetOne.top, enabledLayers);
		createLayer(colorBottom, insetOne.bottom, enabledLayers); // order means
		createLayer(colorLeft, insetOne.left, enabledLayers);
		createLayer(colorRight, insetOne.right, enabledLayers);

		if (bevelLvl == 2) {
			createLayer(colorTop2, insetTwo.top, enabledLayers);
			createLayer(colorBottom2, insetTwo.bottom, enabledLayers);
			createLayer(colorLeft2, insetTwo.left, enabledLayers);
			createLayer(colorRight2, insetTwo.right, enabledLayers);
		}

		int[] button = bevelLvl == 1 ? insetOne.button : insetTwo.button;
		int color = isSolid ? colorSolid : TRANSPARENT;
		createLayer(color, button, enabledLayers, true);

		int levelCnt = enabledLayers.size();
		buttonIndex = levelCnt - 1;

		Drawable[] enabledDrawables = new Drawable[levelCnt];  // TODO improve that mess
		for (int i = 0; i < levelCnt; i++) {
			LayerInfo layerInfo = enabledLayers.get(i);
			enabledDrawables[i] = layerInfo.shapeDrawable;
		}
		enabledDrawable = new LayerDrawable(enabledDrawables);
		for (int i = 0; i < levelCnt; i++) { // start from 2nd level, first is shadow
			LayerInfo layer = enabledLayers.get(i);
			enabledDrawable.setLayerInset(i, layer.leftInSet, layer.topInSet, layer.rightInSet, layer.bottomInSet);
		}

		addState(new int[]{}, enabledDrawable);
	}

	@Override
	protected void createPressedState(List<LayerInfo> pressedLayers) {
		createLayer(colorTopP, insetOne.top, pressedLayers);
		createLayer(colorBottomP, insetOne.bottom, pressedLayers);
		createLayer(colorLeftP, insetOne.left, pressedLayers);
		createLayer(colorRightP, insetOne.right, pressedLayers);

		if (bevelLvl == 2) {
			createLayer(colorTop2P, insetTwo.top, pressedLayers);
			createLayer(colorBottom2P, insetTwo.bottom, pressedLayers);
			createLayer(colorLeft2P, insetTwo.left, pressedLayers);
			createLayer(colorRight2P, insetTwo.right, pressedLayers);
		}

		int[] button = bevelLvl == 1 ? insetOne.button : insetTwo.button;
		int color = isSolid ? colorSolidP : TRANSPARENT;
		createLayer(color, button, pressedLayers, true);

		int levelCnt = pressedLayers.size();
		Drawable[] pressedDrawables = new Drawable[levelCnt]; // TODO improve that mess
		for (int i = 0; i < levelCnt; i++) {
			LayerInfo layerInfo = pressedLayers.get(i);
			pressedDrawables[i] = layerInfo.shapeDrawable;
		}
		pressedDrawable = new LayerDrawable(pressedDrawables);
		for (int i = 0; i < levelCnt; i++) { // start from 2nd level, first is shadow
			LayerInfo layer = pressedLayers.get(i);
			pressedDrawable.setLayerInset(i, layer.leftInSet, layer.topInSet, layer.rightInSet, layer.bottomInSet);
		}

		addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
	}

	private void createLayer(int color, int[] inSet, List<LayerInfo> layers) {
		createLayer(color, inSet, layers, false);
	}

	private void createLayer(int color, int[] inSet, List<LayerInfo> layers, boolean isButton) {
		ShapeDrawable drawable;
		if (isButton) {
			drawable = new ShapeDrawable(new RoundRectShape(outerRect, null, null));
		} else {
			drawable = new ShapeDrawable(new RoundRectShape(outerRect, bevelRect, outerRect));
		}

		if (color != TRANSPARENT) {  // TODO adjust proper logic for transparent solid use
			drawable.getPaint().setColor(color);
		}
		layers.add(new LayerInfo(drawable, inSet[0], inSet[1], inSet[2], inSet[3]));
	}


	@Override
	protected void parseAttributes(Context context, AttributeSet attrs) {
		super.parseAttributes(context, attrs);
		// get style
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoboButton);
		if (array == null) {
			return;
		}

		try { // values
			rectPosition = array.getInt(R.styleable.RoboButton_btn_rect_pos, BOTTOM_MIDDLE);
		/*	isSolid = array.getBoolean(R.styleable.RoboButton_btn_is_solid, true);
			useBorder = array.getBoolean(R.styleable.RoboButton_btn_use_border, true);
			usePressedLayer = array.getBoolean(R.styleable.RoboButton_btn_use_pressed_layer, false);
			gradientAngle = array.getInt(R.styleable.RoboButton_btn_gradient_angle, DEFAULT_ANGLE);
//			padding = array.getDimensionPixelSize(R.styleable.RoboButton_btn_padding, DEFAULT_PADDING);
			bevelLvl = array.getInt(R.styleable.RoboButton_btn_bevel_lvl, 1);
			bevelInset = array.getDimensionPixelSize(R.styleable.RoboButton_btn_bevel_inset, DEFAULT_BEVEL_INSET);

			// Colors for bevel
			colorOuterBorder = array.getInt(R.styleable.RoboButton_btn_outer_border, TRANSPARENT);
			colorTop = array.getInt(R.styleable.RoboButton_btn_top, TRANSPARENT);
			colorLeft = array.getInt(R.styleable.RoboButton_btn_left, TRANSPARENT);
			colorRight = array.getInt(R.styleable.RoboButton_btn_right, TRANSPARENT);
			colorBottom = array.getInt(R.styleable.RoboButton_btn_bottom, TRANSPARENT);

			// Level 2 for bevel
			colorTop2 = array.getInt(R.styleable.RoboButton_btn_top_2, TRANSPARENT);
			colorLeft2 = array.getInt(R.styleable.RoboButton_btn_left_2, TRANSPARENT);
			colorRight2 = array.getInt(R.styleable.RoboButton_btn_right_2, TRANSPARENT);
			colorBottom2 = array.getInt(R.styleable.RoboButton_btn_bottom_2, TRANSPARENT);

			// Button colors
			colorSolid = array.getInt(R.styleable.RoboButton_btn_solid, TRANSPARENT);
			colorGradientStart = array.getInt(R.styleable.RoboButton_btn_gradient_start, TRANSPARENT);
			colorGradientCenter = array.getInt(R.styleable.RoboButton_btn_gradient_center, TRANSPARENT);
			colorGradientEnd = array.getInt(R.styleable.RoboButton_btn_gradient_end, TRANSPARENT);

			*//* ---------------------- Pressed states colors -------------------------------------------*//*
			colorTopP = array.getInt(R.styleable.RoboButton_btn_top_p, TRANSPARENT);
			colorLeftP = array.getInt(R.styleable.RoboButton_btn_left_p, TRANSPARENT);
			colorRightP = array.getInt(R.styleable.RoboButton_btn_right_p, TRANSPARENT);
			colorBottomP = array.getInt(R.styleable.RoboButton_btn_bottom_p, TRANSPARENT);

			// Level 2 Pressed
			colorTop2P = array.getInt(R.styleable.RoboButton_btn_top_2_p, TRANSPARENT);
			colorLeft2P = array.getInt(R.styleable.RoboButton_btn_left_2_p, TRANSPARENT);
			colorRight2P = array.getInt(R.styleable.RoboButton_btn_right_2_p, TRANSPARENT);
			colorBottom2P = array.getInt(R.styleable.RoboButton_btn_bottom_2_p, TRANSPARENT);

			// Button colors Pressed
			colorSolidP = array.getInt(R.styleable.RoboButton_btn_solid_p, TRANSPARENT);
			colorGradientStartP = array.getInt(R.styleable.RoboButton_btn_gradient_start_p, TRANSPARENT);
			colorGradientCenterP = array.getInt(R.styleable.RoboButton_btn_gradient_center_p, TRANSPARENT);
			colorGradientEndP = array.getInt(R.styleable.RoboButton_btn_gradient_end_p, TRANSPARENT);
*/

		} finally {
			array.recycle();
		}
	}


}
