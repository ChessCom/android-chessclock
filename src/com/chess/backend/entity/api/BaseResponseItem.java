package com.chess.backend.entity.api;

import com.chess.statics.Symbol;

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
    "debug": [
        "Access denied.",
        "found method: 'GET'",
        "found requestPath: '/v1/tactics/?isInstall=0&loginToken=de3d73999ffdfcc44c9b5240c170b823'",
        "found data: ''",
        "expected signature: 'c2d049b873acea19d600b28acd57ef08fe55b454'"
    ]
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

	/**
	 * TODO remove, for test purpose only
 	 */
	@Deprecated
	public void setData(ItemType data) {
		this.data = data;
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

	protected static String getSafeValue(String value) {
		return value == null? Symbol.EMPTY : value;
	}

	protected static String getSafeValue(String value, String defaultValue) {
		return value == null? defaultValue : value;
	}
}
