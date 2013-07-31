package com.chess.ui.views.drawables.smart_button;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
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

	private static ButtonDrawable setDefaultsNoBorder(Context context) {
		ButtonDrawable buttonDrawable = setDefaults(context);
		buttonDrawable.useBorder = false;

		return buttonDrawable;
	}

	private static ButtonGlassyDrawable setGlassyDefaults(Context context) {
		Resources resources = context.getResources();
		ButtonGlassyDrawable buttonDrawable = new ButtonGlassyDrawable();
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
		RectButtonDrawable buttonDrawable = new RectButtonDrawable();// TODO improve code - remove duplicates
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
		if (AppUtils.JELLYBEAN_PLUS_API) {
			view.setBackground(buttonDrawable);
		} else {
			view.setBackgroundDrawable(buttonDrawable);
		}
	}

	public static void setBackgroundToView(View view, AttributeSet attrs) {
		Context context = view.getContext();
		if (context != null && attrs != null) {                            // TODO hide to Builder
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoboButton);
			if (array == null) {
				return;
			}
			boolean isRect = false;
			boolean isGlassy = false;
			try {
				if (!array.hasValue(R.styleable.RoboButton_btn_is_solid)) {
					return;
				}
				isRect = array.getBoolean(R.styleable.RoboButton_btn_is_rect, false);
				isGlassy = array.getBoolean(R.styleable.RoboButton_btn_is_glassy, false);
			} finally {
				array.recycle();
			}

			if (isRect) {
				RectButtonDrawable background = new RectButtonDrawable(context, attrs);
				if (AppUtils.JELLYBEAN_PLUS_API) {
					view.setBackground(background);
				} else {
					view.setBackgroundDrawable(background);
				}
			} else if (isGlassy){
				ButtonGlassyDrawable background = new ButtonGlassyDrawable(context, attrs);
				if (AppUtils.JELLYBEAN_PLUS_API) {
					view.setBackground(background);
				} else {
					view.setBackgroundDrawable(background);
				}
			} else {
				ButtonDrawable background = new ButtonDrawable(context, attrs);
				if (AppUtils.JELLYBEAN_PLUS_API) {
					view.setBackground(background);
				} else {
					view.setBackgroundDrawable(background);
				}
			}
		}
	}

	public static ButtonDrawable createDrawable(Context context, int styleId) {
		Resources resources = context.getResources();
		if (styleId == R.style.Button_Red) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createRed(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Green_Light) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createGreenLight(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_OrangeNoBorder) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createOrangeNoBorder(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Brown) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createBrown(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Blue) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createBlue(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Grey2Solid_NoBorder) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createGrey2SolidNoBorder(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Grey2Solid_NoBorder_Light) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createGrey2SolidNoBorder(buttonDrawable, resources); // should set text color to white

			return buttonDrawable;
		} else if (styleId == R.style.Button_White) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createWhite(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_White_30) {
			ButtonDrawable buttonDrawable = setDefaultsNoBorder(context);
			createWhite30(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_White_75) {
			ButtonDrawable buttonDrawable = setDefaultsNoBorder(context);
			createWhite75(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Black_30) {
			ButtonDrawable buttonDrawable = setDefaultsNoBorder(context);
			createBlack30(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Black_65) {
			ButtonDrawable buttonDrawable = setDefaultsNoBorder(context);
			createBlack65(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Glassy) {
			ButtonGlassyDrawable buttonDrawable = setGlassyDefaults(context);
			createGlassy(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Page) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			createPage(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Button_Page_Selected) {
			ButtonDrawable buttonDrawable = setDefaults(context);
			buttonDrawable.colorSolid = resources.getColor(R.color.white);
			createPage(buttonDrawable, resources);

			return buttonDrawable;
		} else if (styleId == R.style.Rect_Top_Left) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = TOP_LEFT;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Top_Middle) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = TOP_MIDDLE;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Top_Right) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = TOP_RIGHT;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Tab_Left) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = TAB_LEFT;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Tab_Middle) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = TAB_MIDDLE;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Tab_Right) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = TAB_RIGHT;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Bottom_Left) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = BOTTOM_LEFT;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Bottom_Middle) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = BOTTOM_MIDDLE;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Bottom_Right) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = BOTTOM_RIGHT;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.ListItem) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = LIST_ITEM;
			createRect(rectButtonDrawable, resources);

			return rectButtonDrawable;
		} else if (styleId == R.style.ListItem_Header) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = LIST_ITEM_HEADER;
			createRect(rectButtonDrawable, resources, R.color.glassy_header);

			return rectButtonDrawable;
		} else if (styleId == R.style.ListItem_Header_2) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = LIST_ITEM_HEADER;
			createRect(rectButtonDrawable, resources, R.color.glassy_header);
			rectButtonDrawable.colorLeft = resources.getColor(R.color.transparent_button_border_left);

			return rectButtonDrawable;
		} else if (styleId == R.style.ListItem_Header_2_Light) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = LIST_ITEM_HEADER;
			rectButtonDrawable.colorLeft = resources.getColor(R.color.transparent_button_border_left); // doesn't affect
			createRect(rectButtonDrawable, resources, R.color.header_light);

			return rectButtonDrawable;
		} else if (styleId == R.style.ListItem_Header_Dark) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = LIST_ITEM_HEADER;
			createRect(rectButtonDrawable, resources, R.color.header_dark);

			return rectButtonDrawable;
		} else if (styleId == R.style.ListItem_Header_2_Dark) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = LIST_ITEM_HEADER_2_DARK;
			rectButtonDrawable.colorTop = resources.getColor(R.color.transparent_button_border_top);
			rectButtonDrawable.colorLeft = resources.getColor(R.color.transparent_button_border_left);
			createRect(rectButtonDrawable, resources, R.color.header_dark);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Bottom_Right_Orange) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = BOTTOM_RIGHT;
			createRect(rectButtonDrawable, resources, R.color.orange_button_flat);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Bottom_Right_Red) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = BOTTOM_RIGHT;
			createRect(rectButtonDrawable, resources, R.color.red_button);

			return rectButtonDrawable;
		} else if (styleId == R.style.Rect_Bottom_Right_Green) {
			RectButtonDrawable rectButtonDrawable = setRectDefaults(context);
			rectButtonDrawable.rectPosition = BOTTOM_RIGHT;
			createRect(rectButtonDrawable, resources, R.color.light_green_button);

			return rectButtonDrawable;
		} else {
			ButtonDrawable buttonDrawable = setDefaults(context);
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
	
	private static void createBlue(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.useBorder = false;
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.blue_emboss_top);
		buttonDrawable.colorLeft = resources.getColor(R.color.blue_emboss_left);
		buttonDrawable.colorRight = resources.getColor(R.color.blue_emboss_right);
		buttonDrawable.colorBottom = resources.getColor(R.color.blue_emboss_bottom);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.blue_button);
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

	private static void createWhite30(ButtonDrawable buttonDrawable, Resources resources) {
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.semitransparent_white_30);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createWhite75(ButtonDrawable buttonDrawable, Resources resources) {
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.semitransparent_white_75);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createBlack30(ButtonDrawable buttonDrawable, Resources resources) {
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.semitransparent_black_30);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createBlack65(ButtonDrawable buttonDrawable, Resources resources) {
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.semitransparent_black_65);
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

	private static void createPage(ButtonDrawable buttonDrawable, Resources resources) {
		buttonDrawable.bevelLvl = 2;
		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.new_soft_grey3);
		buttonDrawable.colorLeft = resources.getColor(R.color.new_soft_grey3);
		buttonDrawable.colorRight = resources.getColor(R.color.new_soft_grey3);
		buttonDrawable.colorBottom = resources.getColor(R.color.new_soft_grey3);
		// Level 2 for bevel
		buttonDrawable.colorTop2 = resources.getColor(R.color.white);
		buttonDrawable.colorLeft2 = resources.getColor(R.color.white);
		buttonDrawable.colorRight2 = resources.getColor(R.color.white);
		buttonDrawable.colorBottom2 = resources.getColor(R.color.white);
		// Button colors
		if (buttonDrawable.colorSolid == 0)
			buttonDrawable.colorSolid = resources.getColor(R.color.new_square_button);
		// init layers
		buttonDrawable.init(resources);
	}

	private static void createGlassy(ButtonGlassyDrawable buttonDrawable, Resources resources) {
		buttonDrawable.isGlassy = true;
		buttonDrawable.useBorder = false;
		buttonDrawable.usePressedLayer = true;
		buttonDrawable.gradientAngle = LEFT_RIGHT;

		// Colors for bevel
		buttonDrawable.colorTop = resources.getColor(R.color.transparent_button_border_top);
		buttonDrawable.colorLeft = resources.getColor(R.color.transparent_button_border_left);
		buttonDrawable.colorRight = resources.getColor(R.color.transparent_button_border_right);
		buttonDrawable.colorBottom = resources.getColor(R.color.transparent_button_border_bottom);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(R.color.glassy_button_1);
		buttonDrawable.colorSolidP = resources.getColor(R.color.glassy_button_p);
		buttonDrawable.colorSolidS = resources.getColor(R.color.glassy_button_s);

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
		if (buttonDrawable.colorTop == 0) {
			buttonDrawable.colorTop = resources.getColor(R.color.transparent_button_border_left);
		}
//		buttonDrawable.colorLeft = resources.getColor(R.color.transparent_button_border_left);
//		buttonDrawable.colorRight = resources.getColor(R.color.transparent_button_border_right);
		buttonDrawable.colorBottom = resources.getColor(R.color.transparent_button_border_bottom);
		// Button colors
		buttonDrawable.colorSolid = resources.getColor(buttonColor);
		// init layers
		buttonDrawable.init(resources);
	}

}
