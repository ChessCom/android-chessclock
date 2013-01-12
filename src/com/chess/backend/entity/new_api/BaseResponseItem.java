package com.chess.backend.entity.new_api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.12.12
 * Time: 6:38
 */
public class BaseResponseItem<ItemType> {
/*
"status": "success",
    "count": 3,
    "data":
 */

/*
	{
	  "status": "error",
	  "message": "Invalid password or username.",
	  "code": 5,
	  "more_info": "http:\/\/www.chess-7.com\/index_api_test.php\/codes#5"
	}
*/
	private String status;
	private String message;
	private int count;
	private int code;
	private ItemType data;
	private String more_info;

	public ItemType getData() {
		return data;
	}

	public String getStatus() {
		return status;
	}

	public int getCount() {
		return count;
	}

	public String getMessage() {
		return message;
	}

	/**
	 *
	 * @return error code received from server. See more_info field for details
	 */
	public int getCode() {
		return code;
	}

	public String getMore_info() {
		return more_info;
	}
}
