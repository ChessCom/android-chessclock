package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.05.13
 * Time: 22:42
 */
public class PayloadItem extends BaseResponseItem<PayloadItem.Data> {
/*
	"developer_payload": "kwN7T5G/B3jmYbbUqmeV5XLBEvF5GFe9QIm5tY0dpkkd96o9A6PQHDt0EISjnQ+JKPm6c3+pzCAoT3v8pQ/NiA==",
	"public_key": "public-key-example"
*/

	public class Data {
		private String developer_payload;
		private String public_key;

		public String getPayload() {
			return developer_payload;
		}

		public String getPublic_key() {
			return public_key;
		}
	}

}
