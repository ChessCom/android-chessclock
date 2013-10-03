package com.chess.backend.entity.api.themes;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.10.13
 * Time: 6:22
 */
public class BackgroundItem extends BaseResponseItem<BackgroundItem.Data> {
/*
	"data": {
		"name": "Glass",
		"background_preview_url": "https://chess-redesign.s3.amazonaws.com/backgrounds/previews/glass.png",
		"font_color": "#FFFFFF",
		"original_handset": "https://chess-redesign.s3.amazonaws.com/backgrounds/originals/Android_720x1280/glass.png",
		"original_iphone": "https://chess-redesign.s3.amazonaws.com/backgrounds/originals/iPhone_5_2x/glass.png",
		"original_ipad": "https://chess-redesign.s3.amazonaws.com/backgrounds/originals/iPad_Landscape_2x/glass.png",
		"original_ipad_port": "https://chess-redesign.s3.amazonaws.com/backgrounds/originals/iPad_Portrait_2x/glass.png",
		"original_tablet": "https://chess-redesign.s3.amazonaws.com/backgrounds/originals/Web_1440x900/glass.png",
		"resized_image": "http://chess-redesign.s3.amazonaws.com/backgrounds/originals/Android_720x1280/6_Glass1080x1920.png"
  	},
*/

	public class Data {
		private String name;
		private String background_preview_url;
		private String font_color;
		private String original_handset;
		private String original_tablet;
		private String resized_image;

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
