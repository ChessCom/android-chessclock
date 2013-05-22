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

		int pressedOverlay = resources.getColor(R.color.rect_button_overlay_p);
		pressedFilter = new PorterDuffColorFilter(pressedOverlay, PorterDuff.Mode.XOR);

		edgeOffset = resources.getDimensionPixelSize(R.dimen.rect_edge_offset); // 4px/ 2px / 2px

		List <LayerInfo> enabledLayers = new ArrayList<LayerInfo>();
		List<LayerInfo> pressedLayers = new ArrayList<LayerInfo>();

		insetOne = new InsetInfo();
		int in1 = (int) (1 * density);
		bevelInset = 0;
		insetOne.top = new int[]{bevelInset, bevelInset, bevelInset, bevelInset + in1};
		insetOne.left = new int[]{bevelInset, bevelInset + in1, bevelInset, bevelInset + in1};
		insetOne.right = new int[]{bevelInset + in1, bevelInset + in1, bevelInset, bevelInset + in1};
		insetOne.bottom = new int[]{bevelInset, bevelInset + in1, bevelInset + in1, bevelInset};
		insetOne.button = new int[]{bevelInset + in1*2, bevelInset + in1*2, bevelInset + in1*2, bevelInset + in1*2};

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

	@Override
	public void draw(Canvas canvas) {
		if (!boundsInit) {
			initBounds(canvas);
		}

		super.draw(canvas);
	}

	private void initBounds(Canvas canvas){
		int width = canvas.getWidth();
		int height = canvas.getHeight();
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
				break;
			case BOTTOM_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset/2, 0, width + edgeOffset/2, height + edgeOffset);
				break;
			case BOTTOM_RIGHT:
				enabledDrawable.setBounds(-edgeOffset/2, 0, width + edgeOffset, height + edgeOffset);
				break;
			case LIST_ITEM:
				enabledDrawable.setBounds(-edgeOffset, -edgeOffset/2, width + edgeOffset, height + edgeOffset/2);
				break;

		}
		boundsInit = true;
	}

	/**
	 * Use override because we are using own inset* object
	 * @param enabledLayers layers to fill with
	 */
	@Override
	protected void createDefaultState(List<LayerInfo> enabledLayers) {
		createLayer(colorTop, insetOne.top, enabledLayers);
		createLayer(colorBottom, insetOne.bottom, enabledLayers); // order is important
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
