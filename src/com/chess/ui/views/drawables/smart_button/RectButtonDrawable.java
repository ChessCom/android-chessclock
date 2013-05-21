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

	int rectPosition;

	private int edgeOffset;

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
		pressedFilter = new PorterDuffColorFilter(PRESSED_OVERLAY, PorterDuff.Mode.XOR);

		edgeOffset = resources.getDimensionPixelSize(R.dimen.rect_edge_offset);

				List <LayerInfo> enabledLayers = new ArrayList<LayerInfo>();
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
				enabledDrawable.setBounds(-edgeOffset, -edgeOffset, canvas.getWidth() + edgeOffset/2, canvas.getHeight());
				break;
			case TOP_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset/2, -edgeOffset, canvas.getWidth() + edgeOffset/2, canvas.getHeight());
				break;
			case TOP_RIGHT:
				enabledDrawable.setBounds(-edgeOffset/2, -edgeOffset, canvas.getWidth() + edgeOffset, canvas.getHeight());
				break;

			case TAB_LEFT:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth() + edgeOffset/2, canvas.getHeight());
				break;
			case TAB_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset/2, 0, canvas.getWidth() + edgeOffset/2, canvas.getHeight());
				break;
			case TAB_RIGHT:
				enabledDrawable.setBounds(-edgeOffset/2, 0, canvas.getWidth() + edgeOffset, canvas.getHeight());
				break;

			case BOTTOM_LEFT:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth() + edgeOffset/2, canvas.getHeight() + edgeOffset);
				break;
			case BOTTOM_MIDDLE:
				enabledDrawable.setBounds(-edgeOffset/2, 0, canvas.getWidth() + edgeOffset/2, canvas.getHeight() + edgeOffset);
				break;
			case BOTTOM_RIGHT:
				enabledDrawable.setBounds(-edgeOffset/2, 0, canvas.getWidth() + edgeOffset, canvas.getHeight() + edgeOffset);
				break;
			case LIST_ITEM:
				enabledDrawable.setBounds(-edgeOffset, 0, canvas.getWidth() + edgeOffset, canvas.getHeight() + edgeOffset);
				break;

		}
		super.draw(canvas);
	}

	@Override
	protected void createDefaultState(List<LayerInfo> enabledLayers) {   // TODO it can be improved!
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
