package com.chess.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;

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
	public static final String TTF = ".ttf";

	private static FontsHelper ourInstance = new FontsHelper();
	private HashMap<String, Typeface> fontsMap;

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
		ttfName = ttfName == null? DEFAULT_FONT: ttfName;

		if (fontsMap.containsKey(ttfName)) {
		    return fontsMap.get(ttfName);
		} else {
			Typeface font = Typeface.createFromAsset(resources.getAssets(), MAIN_PATH + ttfName + TTF);
			fontsMap.put(ttfName, font);
			return font;
		}
	}
}
