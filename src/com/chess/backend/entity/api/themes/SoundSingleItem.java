package com.chess.backend.entity.api.themes;

import com.chess.backend.entity.api.BaseResponseItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.10.13
 * Time: 9:11
 */
public class SoundSingleItem extends  BaseResponseItem<SoundSingleItem.Data> {
/*
	 "data": {
		"user_theme_sound_id": 3,
		"name": "Marble",
		"sound_pack_zip": "http://d1xrj4tlyewhek.cloudfront.net/sounds/marble.zip",
		"theme_id": 0
	  },
*/

	public static class Data {
		private int user_theme_sound_id;
		private String name;
		private String sound_pack_zip;
		private int theme_id;

		public int getThemeSoundId() {
			return user_theme_sound_id;
		}

		public void setUserThemeSoundId(int user_theme_sound_id) {
			this.user_theme_sound_id = user_theme_sound_id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSoundPackZipUrl() {
			return sound_pack_zip;
		}

		public void setSoundPackZip(String sound_pack_zip) {
			this.sound_pack_zip = sound_pack_zip;
		}

		public int getThemeId() {
			return theme_id;
		}

		public void setThemeId(int theme_id) {
			this.theme_id = theme_id;
		}
	}
}