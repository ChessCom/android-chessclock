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
		"name": "Glass",
		"background_preview_url": "http://d1xrj4tlyewhek.cloudfront.net/backgrounds/previews/glass.png",
		"font_color": "#FFFFFF",
		"original_handset": "http://d1xrj4tlyewhek.cloudfront.net/backgrounds/originals/Android_720x1280/glass.png",
		"original_iphone": "http://d1xrj4tlyewhek.cloudfront.net/backgrounds/originals/iPhone_5_2x/glass.png",
		"original_ipad": "http://d1xrj4tlyewhek.cloudfront.net/backgrounds/originals/iPad_Landscape_2x/glass.png",
		"original_ipad_port": "http://d1xrj4tlyewhek.cloudfront.net/backgrounds/originals/iPad_Portrait_2x/glass.png",
		"original_tablet": "http://d1xrj4tlyewhek.cloudfront.net/backgrounds/originals/Web_1440x900/glass.png",
		"resized_image": "http://chess-redesign.s3.amazonaws.com/backgrounds/originals/Android_720x1280/6_Glass1080x1920.png"
  	},
*/

	public class Data {
		private int user_theme_background_id;
		private String name;
		private String background_preview_url;
		private String font_color;
		private String original_handset;
		private String original_tablet;
		private String resized_image;

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
	}

}
