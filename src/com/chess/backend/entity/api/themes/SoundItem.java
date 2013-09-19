package com.chess.backend.entity.api.themes;

import com.chess.backend.entity.api.BaseResponseItem;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.09.13
 * Time: 17:02
 */
public class SoundItem extends BaseResponseItem<List<SoundItem.Data>> {
/*
	"user_theme_sound_id": 4,
	"name": "Wave",
	"sound_pack_zip": "www.some_zip_url.com/image.zip",
*/

	public static class Data {
		private int user_theme_sound_id;
		private String name;
		private String sound_pack_zip;

		public int getUserThemeSoundId() {
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
	}

}
