package com.chess.ui.views.drawables.smart_button;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
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
public class ButtonGlassyDrawable extends ButtonDrawable {

	public static final int[] STATE_SELECTED = new int[]{android.R.attr.state_enabled, android.R.attr.state_selected};
	public static final int[] STATE_PRESSED = new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed};
	public static final int[] STATE_ENABLED = new int[]{};

	LayerDrawable selectedDrawable;
	int colorSolidS;
	private boolean initialized;
	private static final int glassyBorderIndex = 0;
	int glassyBevelSize;

	/**
	 * Use for init ButtonDrawableBuilder
	 */
	ButtonGlassyDrawable() {
		setDefaults();
	}

	public ButtonGlassyDrawable(Context context, AttributeSet attrs) {
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
		if (radius > 0) {
			outerRect = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
		} else {
			outerRect = null; // solve optimization problem because it uses mPath.addRect instead of mPath.addRoundRect
		}

		bevelSize = resources.getDimensionPixelSize(R.dimen.default_bevel_size);
		bevelRect = new RectF(bevelSize, bevelSize, bevelSize, bevelSize);

		if (isGlassy) {
			glassyBevelSize = resources.getDimensionPixelSize(R.dimen.default_bevel_glassy_size);
			glassyRect = new RectF(glassyBevelSize, glassyBevelSize, glassyBevelSize, glassyBevelSize);

			int pressedOverlay = resources.getColor(R.color.glassy_button_overlay_p);
			int selectedOverlay = resources.getColor(R.color.glassy_button_overlay_s);
			pressedFilter = new PorterDuffColorFilter(pressedOverlay, PorterDuff.Mode.DARKEN);
			selectedFilter = new PorterDuffColorFilter(selectedOverlay, PorterDuff.Mode.DARKEN);
		} else {
			int pressedOverlay = resources.getColor(R.color.default_button_overlay_p);
			int selectedOverlay = resources.getColor(R.color.default_button_overlay_s);
			pressedFilter = new PorterDuffColorFilter(pressedOverlay, PorterDuff.Mode.DARKEN); // lighter color will overlay main
			selectedFilter = new PorterDuffColorFilter(selectedOverlay, PorterDuff.Mode.DARKEN);
		}
		int checkedOverlay = resources.getColor(R.color.default_button_overlay_c);
		checkedFilter = new PorterDuffColorFilter(checkedOverlay, PorterDuff.Mode.MULTIPLY);

//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.SCREEN); // bad edges
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.SRC_IN); //  make transparent  - dark - bad
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.SRC_OUT); // bad edges
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.DST_IN); // make light transparent
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.DARKEN);  // bad edges
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.MULTIPLY); // make transparent  - dark - bad
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.OVERLAY); // bad edges
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.XOR);  // bad edges

		List <LayerInfo> enabledLayers = new ArrayList<LayerInfo>();
		List<LayerInfo> pressedLayers = new ArrayList<LayerInfo>();

		if (useBorder) { // outer border
			int strokeSize = resources.getDimensionPixelSize(R.dimen.default_stroke_width);

			RectF stroke = new RectF(strokeSize, strokeSize, strokeSize, strokeSize);

			RectShape rectShape = new RoundRectShapeFixed(outerRect, stroke, outerRect);
			ShapeDrawable shapeDrawable = new ShapeDrawable(rectShape);
			shapeDrawable.getPaint().setColor(colorOuterBorder);

			enabledLayers.add(new LayerInfo(shapeDrawable, 0, 0, 0, 0));
			if (usePressedLayer) {
				pressedLayers.add(new LayerInfo(shapeDrawable, 0, 0, 0, 0));
			}
		}

		createDefaultState(enabledLayers);

		if (usePressedLayer) { // by default we apply color filter and alpha for different states
			createPressedState(pressedLayers);
			createSelectedState(new ArrayList<LayerInfo>());
		}
	}

	@Override
	public void draw(Canvas canvas) {
//		if (!initialized) {
			iniLayers(canvas);
//		}
		super.draw(canvas);
	}

	@Override
	void iniLayers(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		if (!isSolid) {
			((ShapeDrawable) enabledDrawable.getDrawable(buttonIndex)).getPaint().setShader(
					makeLinear(width, height, colorGradientStart, colorGradientCenter, colorGradientEnd));
			if (usePressedLayer) {
				((ShapeDrawable) pressedDrawable.getDrawable(buttonIndex)).getPaint().setShader(
						makeLinear(width, height, colorGradientStartP, colorGradientCenterP, colorGradientEndP));
			}
		}

		if (isGlassy) {
			((ShapeDrawable) enabledDrawable.getDrawable(glassyBorderIndex)).getPaint().setShader(
					makeLinear(width, height, colorGradientStart, colorGradientCenter, colorGradientEnd));
			if (usePressedLayer) {
				((ShapeDrawable) pressedDrawable.getDrawable(glassyBorderIndex)).getPaint().setShader(
						makeLinear(width, height, colorGradientStart, colorGradientCenter, colorGradientEnd));
				((ShapeDrawable) selectedDrawable.getDrawable(glassyBorderIndex)).getPaint().setShader(
						makeLinear(width, height, colorGradientStart, colorGradientCenter, colorGradientEnd));
			}
		}

		initialized = true;
	}


	void createSelectedState(List<LayerInfo> selectedLayers) {
		// add borders
		if (isGlassy) { // create thin gradient border to mimic bevel // TODO make every layer gradient? to reduce total number of layers...
			ShapeDrawable drawable = new ShapeDrawable(new RoundRectShape(outerRect, glassyRect, outerRect));
			selectedLayers.add(new LayerInfo(drawable, bevelInset + insetOne.top[0], bevelInset + insetOne.top[1],
					bevelInset + insetOne.top[2], bevelInset + insetOne.top[3]));
		} else {
			createLayer(colorTopP, insetOne.top, selectedLayers);
			createLayer(colorBottomP, insetOne.bottom, selectedLayers);
			createLayer(colorLeftP, insetOne.left, selectedLayers);
			createLayer(colorRightP, insetOne.right, selectedLayers);

			if (bevelLvl == 2) {
				createLayer(colorTop2P, insetTwo.top, selectedLayers);
				createLayer(colorBottom2P, insetTwo.bottom, selectedLayers);
				createLayer(colorLeft2P, insetTwo.left, selectedLayers);
				createLayer(colorRight2P, insetTwo.right, selectedLayers);
			}
		}

		// add button
		int[] button = bevelLvl == 1 ? insetOne.button : insetTwo.button;
		int	color = isSolid ? colorSolidS : TRANSPARENT;
		createLayer(color, button, selectedLayers, true);

		int levelCnt = selectedLayers.size();
		Drawable[] pressedDrawables = new Drawable[levelCnt]; // TODO improve that mess
		for (int i = 0; i < levelCnt; i++) {
			LayerInfo layerInfo = selectedLayers.get(i);
			pressedDrawables[i] = layerInfo.shapeDrawable;
		}
		selectedDrawable = new LayerDrawable(pressedDrawables);
		for (int i = 0; i < levelCnt; i++) { // start from 2nd level, first is shadow
			LayerInfo layer = selectedLayers.get(i);
			selectedDrawable.setLayerInset(i, layer.leftInSet, layer.topInSet, layer.rightInSet, layer.bottomInSet);
		}

		addState(STATE_SELECTED, selectedDrawable);
	}


	/**
	 * Set padding to internal cover only shape of LayerDrawables
	 * @param drawable to which we set padding must be ShapeDrawable
	 */
	@Override
	void setPaddingToShape(ShapeDrawable drawable) {
		int leftPad = padding;
		int topPad = padding;
		int rightPad = padding;
		int bottomPad = padding;

		if (leftPadding != DEF_VALUE) {
			leftPad = leftPadding;
		}

		if (rightPadding != DEF_VALUE) {
			rightPad = rightPadding;
		}

		if (topPadding != DEF_VALUE) {
			topPad = topPadding;
		}

		if (bottomPadding != DEF_VALUE) {
			bottomPad = bottomPadding;
		}

		drawable.setPadding(leftPad, topPad, rightPad, bottomPad);
	}

	@Override
	protected boolean onStateChange(int[] states) {
		boolean enabled = false;
		boolean pressed = false;
		boolean selected = false;
		boolean checked = false;

		for (int state : states) {
			if (state == android.R.attr.state_enabled)
				enabled = true;
			else if (state == android.R.attr.state_pressed)
				pressed = true;
			else if (state == android.R.attr.state_selected)
				selected = true;
			else if (state == android.R.attr.state_checked)
				checked = true;
		}

		Drawable drawable = mutate();
		if (enabled && pressed) {
			drawable.mutate().setAlpha(enabledAlpha);
			drawable.mutate().setState(STATE_PRESSED);
		} else if (enabled && selected) {
			drawable.mutate().setAlpha(enabledAlpha);
			drawable.mutate().setState(STATE_SELECTED);
		} else if (enabled && checked) {
			drawable.mutate().setAlpha(enabledAlpha);
			drawable.mutate().setState(STATE_SELECTED);
		} else if (!enabled) {
			drawable.mutate().setAlpha(disabledAlpha);
		} else {
			drawable.mutate().setColorFilter(enabledFilter);
			drawable.mutate().setAlpha(enabledAlpha);
			drawable.mutate().setState(STATE_ENABLED);
		}

		invalidateSelf();// need to update for pre-HC

		return callSuperOnStateChange(states);
	}

	@Override
	protected void parseAttributes(Context context, AttributeSet attrs) {
		super.parseAttributes(context, attrs);
		// get style
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoboButton);
		if (array == null) {
			return;
		}

		try {
			colorSolidS = array.getInt(R.styleable.RoboButton_btn_solid_s, TRANSPARENT);
		} finally {
			array.recycle();
		}
	}
}
