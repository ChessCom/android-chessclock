package com.chess.backend.entity.api.themes;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.10.13
 * Time: 6:22
 */
public class BackgroundSingleItem extends BaseResponseItem<BackgroundSingleItem.Data> {
/*
	"data": {
		"user_theme_background_id": 4,
		"name": "Wave",
		"background_preview_url": "www.some_zip_url.com/image.zip",
		"font_color": "FFFFFF",
		"original_handset": "http://chess-redesign.s3.amazonaws.com/backgrounds/originals/iPad_Landscape_2x/1_Graffity480x360.png",
		"original_iphone": "http://chess-redesign.s3.amazonaws.com/backgrounds/originals/iPad_Landscape_2x/1_Graffity480x360.png",
		"original_ipad": "http://chess-redesign.s3.amazonaws.com/backgrounds/originals/iPad_Landscape_2x/1_Graffity480x360.png",
		"original_ipad_port": "http://chess-redesign.s3.amazonaws.com/backgrounds/originals/iPad_Landscape_2x/1_Graffity480x360.png",
		"original_tablet": "http://chess-redesign.s3.amazonaws.com/backgrounds/originals/iPad_Landscape_2x/1_Graffity480x360.png",
		"theme_id": 43
	},
*/

	public static class Data {
		private int user_theme_background_id;
		private String name;
		private String background_preview_url;
		private String font_color;
		private String original_handset;
		private String original_tablet;
		private String resized_image;
		private int theme_id;
		/* Local addition */
		private String localPathLand;
		private String localPathPort;

		public int getBackgroundId() {
			return user_theme_background_id;
		}

		public String getName() {
			return name;
		}

		public String getBackgroundPreviewUrl() {
			return background_preview_url;
		}

		public String getFontColor() {
			return font_color;
		}

		public String getOriginalHandset() {
			return original_handset;
		}

		public String getOriginalTablet() {
			return original_tablet;
		}

		public String getResizedImage() {
			return resized_image;
		}

		public int getThemeId() {
			return theme_id;
		}

		public void setBackgroundId(int user_theme_background_id) {
			this.user_theme_background_id = user_theme_background_id;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setBackgroundPreviewUrl(String background_preview_url) {
			this.background_preview_url = background_preview_url;
		}

		public void setFontColor(String font_color) {
			this.font_color = font_color;
		}

		public void setOriginalHandset(String original_handset) {
			this.original_handset = original_handset;
		}

		public void setOriginalTablet(String original_tablet) {
			this.original_tablet = original_tablet;
		}

		public void setResizedImage(String resized_image) {
			this.resized_image = resized_image;
		}

		public void setThemeId(int theme_id) {
			this.theme_id = theme_id;
		}

		public void setLocalPathLand(String localPath) {
			this.localPathLand = localPath;
		}
		public void setLocalPathPort(String localPath) {
			this.localPathPort = localPath;
		}

		public String getLocalPathLand() {
			return getSafeValue(localPathLand);
		}

		public String getLocalPathPort() {
			return getSafeValue(localPathPort);
		}
	}

}
