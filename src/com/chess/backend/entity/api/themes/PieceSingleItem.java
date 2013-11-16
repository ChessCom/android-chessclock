package com.chess.backend.entity.api.themes;

import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.ui.fragments.ThemeManagerFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.10.13
 * Time: 19:09
 */
public class PieceSingleItem extends BaseResponseItem<PieceSingleItem.Data> {

	public static final String PATH = ThemeManagerFragment.CLOUD_FRONT + ThemeManagerFragment.PIECES;

/*
	"data": [
		{
		"user_theme_pieces_id": 1,
		"name": "Lolz",
		"piece_preview_url": "http://d1xrj4tlyewhek.cloudfront.net/pieces/_previews_/line/lolz.png",
		"theme_id": 17,
		"theme_dir": "lolz"
		},
*/

	public static class Data {
		private int user_theme_pieces_id;
		private String name;
		private String piece_preview_url;
		private int theme_id;
		private String theme_dir;

		public int getThemePieceId() {
			return user_theme_pieces_id;
		}

		public void setThemePieceId(int user_theme_pieces_id) {
			this.user_theme_pieces_id = user_theme_pieces_id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPreviewUrl() {
			return piece_preview_url;
		}

		public void setPreviewUrl(String piece_preview_url) {
			this.piece_preview_url = piece_preview_url;
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
