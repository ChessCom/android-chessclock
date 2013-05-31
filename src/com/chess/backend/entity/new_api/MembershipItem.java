package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.05.13
 * Time: 20:52
 */
public class MembershipItem extends BaseResponseItem<MembershipItem.Data>{
/*
  	"date": {
            "expires": 1555570800,
            "last_renewed": 1524034800,
            "start": 1369258234
        },
	"is_premium": 0,
	"level": 0,
	"result": "success",
	"sku": "gold_monthly",
	"type": "basic",
	"user_id": 41
*/

	public class Data {

		private ExpirationInfo date;
		private int is_premium;
		private int level;
		private String result;
		private String sku;
		private String type;
		private int user_id;

		public ExpirationInfo getDate() {
			return date;
		}

		public int getIs_premium() {
			return is_premium;
		}

		public int getLevel() {
			return level;
		}

		public String getResult() {
			return result;
		}

		public String getSku() {
			return sku;
		}

		public String getType() {
			return type;
		}

		public int getUser_id() {
			return user_id;
		}
	}

	public static class ExpirationInfo {
		private long expires;
		private long last_renewed;
		private long start;

		public long getExpires() {
			return expires;
		}

		public long getLast_renewed() {
			return last_renewed;
		}

		public long getStart() {
			return start;
		}
	}
}
