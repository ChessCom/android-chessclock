package com.chess.backend.entity.new_api;

import android.os.Parcel;
import android.os.Parcelable;

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

	public static class Data implements Parcelable{
		private int theme_id;
		private String background_url;
		private String board_background_url;
		private String background_preview_url;
		private String board_preview_url;
		private String theme_name;
		private String font_color;
		/* Local addition */
		private boolean isSelected;

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

		public void setThemeId(int theme_id) {
			this.theme_id = theme_id;
		}

		public void setBackgroundUrl(String background_url) {
			this.background_url = background_url;
		}

		public void setBoardBackgroundUrl(String board_background_url) {
			this.board_background_url = board_background_url;
		}

		public void setBackgroundPreviewUrl(String background_preview_url) {
			this.background_preview_url = background_preview_url;
		}

		public void setBoardPreviewUrl(String board_preview_url) {
			this.board_preview_url = board_preview_url;
		}

		public void setThemeName(String theme_name) {
			this.theme_name = theme_name;
		}

		public void setFontColor(String font_color) {
			this.font_color = font_color;
		}

		public String getFontColor() {
			return getSafeValue(font_color, "FFFFFF");
		}

		protected Data(Parcel in) {
			theme_id = in.readInt();
			background_url = in.readString();
			board_background_url = in.readString();
			background_preview_url = in.readString();
			board_preview_url = in.readString();
			theme_name = in.readString();
			font_color = in.readString();
			isSelected = in.readByte() != 0x00;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(theme_id);
			dest.writeString(background_url);
			dest.writeString(board_background_url);
			dest.writeString(background_preview_url);
			dest.writeString(board_preview_url);
			dest.writeString(theme_name);
			dest.writeString(font_color);
			dest.writeByte((byte) (isSelected ? 0x01 : 0x00));
		}

		public static final Parcelable.Creator<Data> CREATOR = new Parcelable.Creator<Data>() {
			@Override
			public Data createFromParcel(Parcel in) {
				return new Data(in);
			}

			@Override
			public Data[] newArray(int size) {
				return new Data[size];
			}
		};
	}
}
