package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.06.13
 * Time: 6:56
 */
public class RequestItem extends BaseResponseItem<RequestItem.Data> {
/*
	"status": "success",
    "data": {
        "request_id": 2077
    }
*/

	public class Data {
		private int request_id;

		public int getRequestId() {
			return request_id;
		}
	}
}
