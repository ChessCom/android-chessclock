package com.chess.ui.views.drawables.smart_button;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import com.chess.R;
import com.chess.utilities.AppUtils;

import static com.chess.ui.views.drawables.smart_button.ButtonDrawable.DEFAULT_BEVEL_INSET;
import static com.chess.ui.views.drawables.smart_button.RectButtonDrawable.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.05.13
 * Time: 17:17
 */
public class ButtonDrawableBuilder {

	private static ButtonDrawable setDefaults(Context context) {
		Resources resources = context.getResources();
		ButtonDrawable buttonDrawable = new ButtonDrawable();
		buttonDrawable.isSolid = true;
		buttonDrawable.useBorder = true;
		buttonDrawable.usePressedLayer = false;
		buttonDrawable.gradientAngle = TL_BR;
		buttonDrawable.bevelLvl = 1;
		buttonDrawable.bevelInset = DEFAULT_BEVEL_INSET;

		buttonDrawable.radius = resources.getDimensionPixelSize(R.dimen.rounded_button_radius);
		buttonDrawable.colorOuterBorder = resources.getColor(R.color.semi_transparent_border);

		return buttonDrawable;
	}

	private static RectButtonDrawable setRectDefaults(Context context) {
		Resources resources = context.getResources();
		RectButtonDrawable buttonDrawable = new RectButtonDrawable();
		buttonDrawable.isSolid = true;
		buttonDrawable.useBorder = true;
		buttonDrawable.usePressedLayer = false;
		buttonDrawable.gradientAngle = TL_BR;
		buttonDrawable.bevelLvl = 1;
		buttonDrawable.bevelInset = DEFAULT_BEVEL_INSET;

		buttonDrawable.radius = resources.getDimensionPixelSize(R.dimen.rounded_button_radius);
		buttonDrawable.colorOuterBorder = resources.getColor(R.color.semi_transparent_border);

		return buttonDrawable;
	}

	public static void setBackgroundToView(View view, int styleId) {
		ButtonDrawable buttonDrawable = createDrawable(view.getContext(), styleId);
		if (AppUtils.HONEYCOMB_PLUS_API) {
			view.setBackground(buttonDrawable);
		} else {
			view.setBackgroundDrawable(buttonDrawable);
		}
	}

	public static ButtonDrawable createDrawable(Context context, int styleId) {
		ButtonDrawable buttonDrawable = setDefaults(context);

		Resources resources = context.getResources();
		switch (styleId) {
			case R.style.Button_Red:
				createRed(buttonDrawable, resources);

				return buttonDrawable;
			case R.style.Button_Green_Light:
				createGreenLight(buttonDrawable, resources);

				return buttonDrawable;
			case R.style.Button_OrangeNoBorder:
				createOrangeNoBorder(buttonDrawable, resources);

				return buttonDrawable;
			case R.style.Button_Brown:
				createBrown(buttonDrawable, resources);

				return buttonDrawable;
			case R.style.Button_Grey2Solid_NoBorder:
				createGrey2SolidNoBorder(buttonDrawable, resources);

				return buttonDrawable;
			case R.style.Button_Grey2Solid_NoBorder_Light:
				createGrey2SolidNoBorder(buttonDrawable, resources); // should set text color to white

				return buttonDrawable;
			case R.style.Button_White:
				createWhite(buttonDrawable, resources);

				return buttonDrawable;
			case R.style.Button_Glassy:
				createGlassy(buttonDrawable, resources);

				return buttonDrawable;
			case R.style.Rect_TopLeft:
				RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = TOP_LEFT;
				return rectButtonDrawable;
			case R.style.Rect_TopMiddle:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = TOP_MIDDLE;
				return rectButtonDrawable;
			case R.style.Rect_TopRight:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = TOP_RIGHT;
				return rectButtonDrawable;
			case R.style.Rect_TabLeft:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = TAB_LEFT;
				return rectButtonDrawable;
			case R.style.Rect_TabMiddle:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = TAB_MIDDLE;
				return rectButtonDrawable;
			case R.style.Rect_TabRight:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = TAB_RIGHT;
				return rectButtonDrawable;
			case R.style.Rect_Bottom_Left:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = BOTTOM_LEFT;
				return rectButtonDrawable;
			case R.style.Rect_Bottom_Middle:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = BOTTOM_MIDDLE;
				return rectButtonDrawable;
			case R.style.Rect_Bottom_Right:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = BOTTOM_RIGHT;
				return rectButtonDrawable;
			case R.style.ListItem:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources);

				rectButtonDrawable.rectPosition = LIST_ITEM;
				return rectButtonDrawable;
			case R.style.ListItem_Header:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources, R.color.glassy_button);

				rectButtonDrawable.rectPosition = LIST_ITEM;
				return rectButtonDrawable;
			case R.style.Rect_Bottom_Right_Orange:  // TODO group
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources, R.color.orange_button_flat);

				rectButtonDrawable.rectPosition = BOTTOM_RIGHT;
				return rectButtonDrawable;
			case R.style.Rect_Bottom_Right_Red:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources, R.color.red_button);

				rectButtonDrawable.rectPosition = BOTTOM_RIGHT;
				return rectButtonDrawable;
			case R.style.Rect_Bottom_Right_Green:
				rectButtonDrawable = setRectDefaults(context);
				createRect(rectButtonDrawable, resources, R.color.light_green_button);

				rectButtonDrawable.rectPosition = BOTTOM_RIGHT;
				return rectButtonDrawable;
			default /*R.style.Button_Orange2*/:
				createOrange2(buttonDrawable, resources);

				return buttonDrawable;
		}
	}

	private static void createGrey2SolidNoBorder(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.bevelLvl = 2;
		buttonDrawable.useBorder = false;
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.upgrade_plan_platinum_top_1);
		buttonDrawable.colorLeft = resources.getColor(R.color.upgrade_plan_platinum_left_1);
		buttonDrawable.colorRight = resources.getColor(R.color.upgrade_plan_platinum_right_1);
		buttonDrawable.colorBottom = resources.getColor(R.color.upgrade_plan_platinum_bottom_1);
		// Level 2 for bevel
		buttonDrawable.colorTop2 = resources.getColor(R.color.upgrade_plan_platinum_top_2);
		buttonDrawable.colorLeft2 = resources.getColor(R.color.upgrade_plan_platinum_left_2);
		buttonDrawable.colorRight2 = resources.getColor(R.color.upgrade_plan_platinum_right_2);
		buttonDrawable.colorBottom2 = resources.getColor(R.color.upgrade_plan_platinum_bottom_2);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.upgrade_plan_platinum);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createBrown(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.bevelLvl = 2;
		buttonDrawable.useBorder = false;
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.upgrade_plan_gold_top_1);
		buttonDrawable.colorLeft = resources.getColor(R.color.upgrade_plan_gold_left_1);
		buttonDrawable.colorRight = resources.getColor(R.color.upgrade_plan_gold_right_1);
		buttonDrawable.colorBottom = resources.getColor(R.color.upgrade_plan_gold_bottom_1);
		// Level 2 for bevel
		buttonDrawable.colorTop2 = resources.getColor(R.color.upgrade_plan_gold_top_2);
		buttonDrawable.colorLeft2 = resources.getColor(R.color.upgrade_plan_gold_left_2);
		buttonDrawable.colorRight2 = resources.getColor(R.color.upgrade_plan_gold_right_2);
		buttonDrawable.colorBottom2 = resources.getColor(R.color.upgrade_plan_gold_bottom_2);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.upgrade_plan_gold);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createOrangeNoBorder(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.useBorder = false;
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.orange_emboss_top_1);
		buttonDrawable.colorLeft = resources.getColor(R.color.orange_emboss_left_1);
		buttonDrawable.colorRight = resources.getColor(R.color.orange_emboss_right_1);
		buttonDrawable.colorBottom = resources.getColor(R.color.orange_emboss_bottom_1);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.orange_button);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createGreenLight(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.useBorder = true;
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.light_green_emboss_top_1);
		buttonDrawable.colorLeft = resources.getColor(R.color.light_green_emboss_top_1);
		buttonDrawable.colorRight = resources.getColor(R.color.light_green_emboss_bot_2);
		buttonDrawable.colorBottom = resources.getColor(R.color.light_green_emboss_bot_1);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.light_green_button);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createRed(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.useBorder = false;
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.red_emboss_top_1);
		buttonDrawable.colorLeft = resources.getColor(R.color.red_emboss_top_2);
		buttonDrawable.colorRight = resources.getColor(R.color.red_emboss_bottom_2);
		buttonDrawable.colorBottom = resources.getColor(R.color.red_emboss_bottom_1);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.red_button);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createWhite(ButtonDrawable buttonDrawable, Resources resources) { // TODO check transparency for white
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.white_button_solid);
		buttonDrawable.colorLeft = resources.getColor(R.color.white_button_solid);
		buttonDrawable.colorRight = resources.getColor(R.color.white_button_solid);
		buttonDrawable.colorBottom = resources.getColor(R.color.white_button_solid);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.white_button_solid);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createOrange2(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.bevelLvl = 2;
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.orange_emboss_top_1);
		buttonDrawable.colorLeft = resources.getColor(R.color.orange_emboss_left_1);
		buttonDrawable.colorRight = resources.getColor(R.color.orange_emboss_right_1);
		buttonDrawable.colorBottom = resources.getColor(R.color.orange_emboss_bottom_1);
		// Level 2 for bevel
		buttonDrawable.colorTop2 = resources.getColor(R.color.orange_emboss_top_2);
		buttonDrawable.colorLeft2 = resources.getColor(R.color.orange_emboss_left_2);
		buttonDrawable.colorRight2 = resources.getColor(R.color.orange_emboss_right_2);
		buttonDrawable.colorBottom2 = resources.getColor(R.color.orange_emboss_bottom_2);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.orange_button);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createGlassy(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.isGlassy = true;
		buttonDrawable.gradientAngle = LEFT_RIGHT;

		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.transparent_button_border_top);
		buttonDrawable.colorLeft = resources.getColor(R.color.transparent_button_border_left);
		buttonDrawable.colorRight = resources.getColor(R.color.transparent_button_border_right);
		buttonDrawable.colorBottom = resources.getColor(R.color.transparent_button_border_bottom);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.glassy_button);

		buttonDrawable.colorGradientStart = resources.getColor(R.color.transparent_button_border_left);
		buttonDrawable.colorGradientEnd = resources.getColor(R.color.transparent_button_border_top);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createRect(RectButtonDrawable buttonDrawable, Resources resources) {
		createRect(buttonDrawable, resources, R.color.transparent_button);
	}

	private static void createRect(RectButtonDrawable buttonDrawable, Resources resources, int buttonColor) {
		// no radius
		buttonDrawable.radius = 0;
		buttonDrawable.useBorder = false;
		buttonDrawable.bevelLvl = 1;

		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.transparent_button_border_top);
		buttonDrawable.colorLeft = resources.getColor(R.color.transparent_button_border_left);
		buttonDrawable.colorRight = resources.getColor(R.color.transparent_button_border_right);
		buttonDrawable.colorBottom = resources.getColor(R.color.transparent_button_border_bottom);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(buttonColor);
		// init layers
		buttonDrawable.init(resources);
	}

}
