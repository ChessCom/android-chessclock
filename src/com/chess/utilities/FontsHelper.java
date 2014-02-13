package com.chess.utilities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import com.chess.statics.AppData;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.05.13
 * Time: 13:44
 */
public class FontsHelper {

	public static final String MAIN_PATH = "fonts/custom-"; // Default font is Trebuchet MS
	public static final String DEFAULT_FONT = "Regular";
	public static final String BOLD_FONT = "Bold";
	public static final String ICON_FONT = "Icon"; // Chess.com Glyph
	public static final String ITALIC_FONT = "Italic";
	public static final String THIN_FONT = "Thin";
	public static final String TTF = ".ttf";

	private static FontsHelper ourInstance = new FontsHelper();
	private HashMap<String, Typeface> fontsMap;
	private AppData appData;
	private ColorStateList themeColorStateList;

	public static FontsHelper getInstance() {
		return ourInstance;
	}

	private FontsHelper() {
		fontsMap = new HashMap<String, Typeface>();
	}

	public Typeface getTypeFace(Context context, String ttfName) {
		return getTypeFace(context.getResources(), ttfName);
	}

	public Typeface getTypeFace(Resources resources, String ttfName) {
		ttfName = ttfName == null ? DEFAULT_FONT : ttfName;

		if (fontsMap.containsKey(ttfName)) {
			return fontsMap.get(ttfName);
		} else {
			Typeface font = Typeface.createFromAsset(resources.getAssets(), MAIN_PATH + ttfName + TTF);
			fontsMap.put(ttfName, font);
			return font;
		}
	}

	public ColorStateList getThemeColorStateList(Context context, boolean forceUpdate) {
		if (appData == null) {
			appData = new AppData(context);
		}

		if (themeColorStateList == null || forceUpdate) {
			// change alpha from last 2 letters to first (FFFFFFBF -> BFFFFFFF
			String themeFontColor = appData.getThemeFontColor();
			String alpha = themeFontColor.substring(6);
			themeFontColor = themeFontColor.substring(0, 6);
			int defaultFontColor = Color.parseColor("#" + alpha + themeFontColor); // add 75% opacity
			int pressedFontColor = Color.parseColor("#" + alpha + themeFontColor);

			themeColorStateList = new ColorStateList(
					new int[][]{
							new int[]{android.R.attr.state_enabled},
							new int[]{android.R.attr.state_pressed},
							new int[]{android.R.attr.state_selected},
							new int[]{android.R.attr.state_enabled, android.R.attr.state_checked},// selected
							new int[]{-android.R.attr.state_enabled},
					},
					new int[]{
							defaultFontColor,
							pressedFontColor,
							pressedFontColor,
							Color.GREEN,
							Color.RED}
			);
		}
		return themeColorStateList;

	}

}
