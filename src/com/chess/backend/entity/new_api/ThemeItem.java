package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.07.13
 * Time: 22:54
 */
public class ThemeItem extends BaseResponseItem<List<ThemeItem.Data>> {
/*
"data": [
    {
      "theme_id": 1,
      "background_url": "http://static3.depositphotos.com/1004423/181/i/950/depositphotos_1812868-Abstract-Rock-Background.jpg",
      "board_background_url": "http://t1.gstatic.com/images?q=tbn:ANd9GcQ31TI8XI4GM5QiXYLnP5kVWM18QzdqRG2d2f34UEwYahJgpl0b",
      "background_preview_url": "http://static3.depositphotos.com/1004423/181/i/950/depositphotos_1812868-Abstract-Rock-Background.jpg",
      "board_preview_url": "http://t1.gstatic.com/images?q=tbn:ANd9GcQ31TI8XI4GM5QiXYLnP5kVWM18QzdqRG2d2f34UEwYahJgpl0b",
      "theme_name": "Rock"
    }
  ]
*/

	public static class Data {
		private int theme_id;
		private String background_url;
		private String board_background_url;
		private String background_preview_url;
		private String board_preview_url;
		private String theme_name;
		/* Local addition */
		private boolean isSelected;
		private boolean isLocal;

		public int getThemeId() {
			return theme_id;
		}

		public String getBackgroundUrl() {
			return background_url;
		}

		public String getBoardBackgroundUrl() {
			return board_background_url;
		}

		public String getBackgroundPreviewUrl() {
			return background_preview_url;
		}

		public String getBoardPreviewUrl() {
			return board_preview_url;
		}

		public String getThemeName() {
			return theme_name;
		}

		public boolean isSelected() {
			return isSelected;
		}

		public void setSelected(boolean selected) {
			isSelected = selected;
		}

		public boolean isLocal() {
			return isLocal;
		}

		public void setLocal(boolean local) {
			isLocal = local;
		}
	}
}
