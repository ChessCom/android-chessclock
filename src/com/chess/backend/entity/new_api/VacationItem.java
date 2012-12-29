package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 13:34
 */
public class VacationItem extends BaseResponseItem<VacationItem.Data> {
	/*
	"status": "success",
		"data": {
			"is_on_vacation": false
		}
	*/
	public class Data {
		private boolean is_on_vacation;

		public boolean isOnVacation() {
			return is_on_vacation;
		}
	}
}
