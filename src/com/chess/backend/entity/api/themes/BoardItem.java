package com.chess.backend.entity.api.themes;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.10.13
 * Time: 19:20
 */
public class BoardItem extends BaseResponseItem<BoardItem.Data> {

	public static final String PATH = "https://chess-redesign.s3.amazonaws.com/Boards/";

/*
	"user_theme_board_id": 18,
	"name": "Graffiti",
	"board_preview_url": "https://chess-redesign.s3.amazonaws.com/Boards/_previews_/line/graffiti.png",
	"line_board_preview": "https://chess-redesign.s3.amazonaws.com/Boards/_previews_/line/graffiti.png",
	"coordinate_color": "#FF2f3c22",
	"theme_id": 19,
	"theme_dir": "graffiti"

*/

	public class Data {
		private int user_theme_board_id;
		private String name;
		private String board_preview_url;
		private String line_board_preview;
		private String coordinate_color;
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

		public String getBoardPreviewUrl() {
			return board_preview_url;
		}

		public void setBoardPreviewUrl(String board_preview_url) {
			this.board_preview_url = board_preview_url;
		}

		public String getLineBoardPreview() {
			return line_board_preview;
		}

		public void setLineBoardPreview(String line_board_preview) {
			this.line_board_preview = line_board_preview;
		}

		public String getCoordinateColor() {
			return coordinate_color;
		}

		public void setCoordinateColor(String coordinate_color) {
			this.coordinate_color = coordinate_color;
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
	}
}
