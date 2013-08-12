package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.01.13
 * Time: 7:55
 */
public class GcmItem extends BaseResponseItem<GcmItem.Data> {
	/*
	{
		"status": "success",
		"data": {
			"registration_id": "APA91bGdgBoogb-2fuaEXrk39Y3LDi-kQHZtT1iTLb_P-zG-y7DaJleiH-CpEfmZIt3IPFznGkmnggRMuJOb3rRL8LZ7eg3DLwst9vpbrXvDBQvw6HTyX77ZQv-DLvOax0zJhI3WrZv3"
		}
	}
*/
	public class Data {
		private String registration_id;

		public String getRegistration_id() {
			return registration_id;
		}

		public void setRegistration_id(String registration_id) {
			this.registration_id = registration_id;
		}
	}
}
