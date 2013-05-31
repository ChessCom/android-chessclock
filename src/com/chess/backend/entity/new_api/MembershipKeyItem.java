package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.05.13
 * Time: 8:53
 */
public class MembershipKeyItem extends BaseResponseItem<MembershipKeyItem.Data> {

/*
"data": {
        "public_key": "public-key-example"
    },
*/

	public static class Data {

		private String public_key;

		public String getPublicKey() {
			return public_key;
		}

		public void setPublicKey(String public_key) {
			this.public_key = public_key;
		}
	}


}
