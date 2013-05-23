package com.chess.ui.views.drawables.smart_button;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.utilities.AppUtils;

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

	static final int LIST_ITEM = 9;

	int rectPosition = DEF_VALUE;

	private int edgeOffset;
	InsetInfo insetOne = new InsetInfo();
	InsetInfo insetTwo = new InsetInfo();

	/* state & other values */
	private boolean boundsInit;

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
		if (radius > 0) {
			outerRect = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
		} else {
			outerRect = null;
		}

		bevelSize = resources.getDimensionPixelSize(R.dimen.default_bevel_size);
		bevelRect = new RectF(bevelSize, bevelSize, bevelSize, bevelSize);

		// set color filters for different drawable state
		int pressedOverlay = resources.getColor(R.color.rect_button_overlay_p);
		pressedFilter = new PorterDuffColorFilter(pressedOverlay, PorterDuff.Mode.XOR);
		int selectedOverlay = resources.getColor(R.color.default_button_overlay_s);
		selectedFilter = new PorterDuffColorFilter(selectedOverlay, PorterDuff.Mode.XOR);
		int checkedOverlay = resources.getColor(R.color.rect_button_overlay_c);
		checkedFilter = new PorterDuffColorFilter(checkedOverlay, PorterDuff.Mode.XOR);

		if (isCheckable()){
			if (!AppUtils.HONEYCOMB_PLUS_API ) {
				checkedFilter = new LightingColorFilter(Color.RED, 1);
			}
			enabledFilter = checkedFilter;
			checkedFilter = null;
		} else {
			selectedFilter = null;
			enabledFilter = null;
		}

//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.SCREEN); // bad edges
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.SRC_IN); //  make transparent  - dark - bad
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.SRC_OUT); // bad edges
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.DST_IN); // make light transparent
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.DARKEN);  // bad edges
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.MULTIPLY); // make transparent  - dark - bad
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.OVERLAY); // bad edges
//		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.XOR);  // bad edges


		edgeOffset = bevelSize*2; // resources.getDimensionPixelSize(R.dimen.rect_edge_offset); // 4px/ 1px / 1px

		List <LayerInfo> enabledLayers = new ArrayList<LayerInfo>();
		List<LayerInfo> pressedLayers = new ArrayList<LayerInfo>();

		insetOne = new InsetInfo();
		int in1 = bevelSize;

		bevelInset = 0;
		insetOne.top = new int[]{in1, in1, in1, 0 };
		insetOne.bottom = new int[]{0, 0, in1, 0};
		insetOne.button = new int[]{in1, in1, in1, in1};

		if (bevelLvl == 2) {
			insetTwo = new InsetInfo();
			int in2 = (int) (2 * density);
			insetTwo.top = new int[]{insetOne.top[0], insetOne.top[1], insetOne.top[2], insetOne.top[3] + in2};
			insetTwo.left = new int[]{insetOne.left[0], insetOne.left[1] + in2, insetOne.left[2], insetOne.left[3] + in2};
			insetTwo.right = new int[]{insetOne.right[0] + in2, insetOne.right[1] + in2, insetOne.right[2], insetOne.right[3] + in2};
			insetTwo.bottom = new int[]{insetOne.bottom[0] + in2, insetOne.bottom[1] + in2, insetOne.bottom[2] + in2, insetOne.bottom[3]};
			insetTwo.button = new int[]{insetOne.button[0] + in2, insetOne.button[1] + in2, insetOne.button[2] + in2, insetOne.button[3] + in2};
		}

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

	/**
	 * For tabs and top buttons we need to draw default state darker then checked
	 * @return true if drawable used for tabs or top buttons
	 */
	private boolean isCheckable() {
		return rectPosition == TAB_LEFT ||rectPosition == TAB_MIDDLE ||rectPosition == TAB_RIGHT
				|| rectPosition == TOP_LEFT ||rectPosition == TOP_MIDDLE ||rectPosition == TOP_RIGHT;
	}

	@Override
	public void draw(Canvas canvas) {
		if (!boundsInit) {
			initBounds(canvas);
		}

		super.draw(canvas);
	}

	private void initBounds(Canvas canvas){
		Rect bounds = canvas.getClipBounds();
		int width = bounds.width();
		int height = bounds.height();
		switch (rectPosition) {
			case TOP_LEFT:
				enabledDrawable.setBounds(-edgeOffset, -edgeOffset, width + edgeOffset/2, height);
				break;
			case TOP_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset/2, -edgeOffset, width + edgeOffset/2, height);
				break;
			case TOP_RIGHT:
				enabledDrawable.setBounds(-edgeOffset/2, -edgeOffset, width + edgeOffset, height);
				break;

			case TAB_LEFT:
				enabledDrawable.setBounds(-edgeOffset, 0, width + edgeOffset/2, height);
				break;
			case TAB_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset/2, 0, width + edgeOffset/2, height);
				break;
			case TAB_RIGHT:
				enabledDrawable.setBounds(-edgeOffset/2, 0, width + edgeOffset, height);
				break;

			case BOTTOM_LEFT:
				enabledDrawable.setBounds(-edgeOffset, 0, width + edgeOffset/2, height + edgeOffset);
//				enabledDrawable.setBounds(-edgeOffset*2, 0, width + edgeOffset, height + edgeOffset);
				break;
			case BOTTOM_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset/2, 0, width + edgeOffset/2, height + edgeOffset);
//				enabledDrawable.setBounds(-edgeOffset/2, 0, width + edgeOffset, height + edgeOffset);
				break;
			case BOTTOM_RIGHT:
				enabledDrawable.setBounds(-edgeOffset/2, 0, width + edgeOffset, height + edgeOffset);
//				enabledDrawable.setBounds(-edgeOffset/2, 0, width + edgeOffset*2, height + edgeOffset);
				break;
			case LIST_ITEM:
				int width1 = canvas.getWidth();  // use full screen width to make backward compatibility
				enabledDrawable.setBounds(-edgeOffset, -edgeOffset/2, width1 + edgeOffset, height);
				break;

		}
		boundsInit = true;
	}

	/**
	 * Use override because we are using own insetOne* object
	 * @param enabledLayers layers to fill with
	 */
	@Override
	protected void createDefaultState(List<LayerInfo> enabledLayers) {
		createLayer(colorTop, insetOne.top, enabledLayers);
		createLayer(colorBottom, insetOne.bottom, enabledLayers); // order is important
//		createLayer(colorLeft, insetOne.left, enabledLayers);
//		createLayer(colorRight, insetOne.right, enabledLayers);

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

		addState(ENABLED_STATE, enabledDrawable);
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

		} finally {
			array.recycle();
		}
	}

}
