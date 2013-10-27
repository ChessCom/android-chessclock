package com.chess.backend.entity.api.themes;

import android.text.TextUtils;
import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.10.13
 * Time: 16:34
 */
public class BoardSingleItem extends BaseResponseItem<BoardSingleItem.Data>{
	public static final String PATH = "https://chess-redesign.s3.amazonaws.com/Boards/";
	public static final String COLOR_DIVIDER = "#";

/*
	"user_theme_board_id": 5,
	"name": "Cloud",
	"board_preview_url": "www.some_board_url.com/slika.jpg",
	"line_board_preview": "www.some_board_url.com/linijadruga.jpg"
	"coordinate_color_light": "FFFFFF",
	"coordinate_color_dark": "000000",
	"highlight_color": "fffccc",
	"theme_id": 42,
	"theme_dir": "metal"
*/

	public static class Data {
		private static final int LIGHT_COLOR = 1;
		private static final int DARK_COLOR = 2;
		private static final int HIGHLIGHT_COLOR = 3;
		private static final String ALPHA = "#80";
		private int user_theme_board_id;
		private String name;
		private String board_preview_url;
		private String line_board_preview;
		private String coordinate_color_light;
		private String coordinate_color_dark;
		private String highlight_color;
		private int theme_id;
		private String theme_dir;

		public int getThemeBoardId() {
			return user_theme_board_id;
		}

		public void setThemeBoardId(int user_theme_board_id) {
			this.user_theme_board_id = user_theme_board_id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPreviewUrl() {
			return board_preview_url;
		}

		public void setPreviewUrl(String board_preview_url) {
			this.board_preview_url = board_preview_url;
		}

		public String getLineBoardPreviewUrl() {
			return line_board_preview;
		}

		public void setLineBoardPreviewUrl(String line_board_preview) {
			this.line_board_preview = line_board_preview;
		}

		public String getCoordinateColorLight() {
			return COLOR_DIVIDER + getSafeColor(coordinate_color_light);
		}

		public void setCoordinateColorLight(String coordinate_color_light) {
			this.coordinate_color_light = coordinate_color_light;
		}

		public String getCoordinateColorDark() {
			return COLOR_DIVIDER + getSafeColor(coordinate_color_dark);
		}

		public void setCoordinateColorDark(String coordinate_color_dark) {
			this.coordinate_color_dark = coordinate_color_dark;
		}

		public String getHighlightColor() {
			return ALPHA + getSafeColor(highlight_color);
		}

		public void setHighlightColor(String highlight_color) {
			this.highlight_color = highlight_color;
		}

		public int getThemeId() {
			return theme_id;
		}

		public void setThemeId(int theme_id) {
			this.theme_id = theme_id;
		}

		public String getThemeDir() {
			return theme_dir;
		}

		public void setThemeDir(String theme_dir) {
			this.theme_dir = theme_dir;
		}

		private String getSafeColor(String color) {
			if (TextUtils.isEmpty(color)) {
				return "000000"; // return black color by default, so we could see if that is wrong
			} else {
				return color.trim();
			}
		}
	}
}
