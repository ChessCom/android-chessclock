package com.chess.backend;

import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * LoadItem class
 *
 * @author alien_roger
 * @created at: 15.04.12 6:54
 */
public class LoadItem { // TODO refactor with builder

	public static final String CODE = "";
	private String loadPath;
	private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	private String requestMethod;
	private String filePath;
	private String fileMark;
	private int fileSize;

	public LoadItem() {
		nameValuePairs = new ArrayList<NameValuePair>();
		requestMethod = RestHelper.GET;
	}

	public void addRequestParams(String key, String value) {
		nameValuePairs.add(new BasicNameValuePair(key, value));
	}

	public void addRequestParams(String key, int value) {
		nameValuePairs.add(new BasicNameValuePair(key, String.valueOf(value)));
	}

	public void addRequestParams(String key, long value) {
		nameValuePairs.add(new BasicNameValuePair(key, String.valueOf(value)));
	}

	public void addRequestParams(NameValuePair pair) {
		nameValuePairs.add(pair);
	}

	public void replaceRequestParams(String key, String value) {
		NameValuePair pairNew = new BasicNameValuePair(key, value);
		ArrayList<NameValuePair> removedItems = new ArrayList<NameValuePair>();

		for (NameValuePair nameValuePair : nameValuePairs) {
			if (nameValuePair.getName().equals(key)) {
				removedItems.add(nameValuePair);
			}
		}

		nameValuePairs.removeAll(removedItems);
		nameValuePairs.add(pairNew);
	}

	public List<NameValuePair> getRequestParams() {
		return nameValuePairs;
	}

	public void setLoadPath(String loadPath) {
		this.loadPath = loadPath;
	}

	public String getLoadPath() {
		return loadPath;
	}

	public void clearParams() {
		nameValuePairs.clear();
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileMark() {
		return fileMark;
	}

	public void setFileMark(String fileMark) {
		this.fileMark = fileMark;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public String getCode() {
		return CODE;
	}


/*
 {
        "method": "GET",
        "url": "/v1/friends/requests?loginToken=0a5e997ed6fa26213d5db9c4fafe1072",
        "requestId": 0
    },
    {
        "method": "PUT",
        "url": "/v1/games/35000574/actions",
        "body": {
            "command": "CHAT",
            "timestamp": 1355687586,
            "message": "Hellooooo",
            "loginToken": "0a5e997ed6fa26213d5db9c4fafe1072"
        },
        "requestId": 1
    },
    {
        "method": "POST",
        "url": "/v1/games/35000574/notes",
        "body": {
            "content": "This works!"
        },
        "requestId": 2
    },
    {
        "method": "GET",
        "url": "/v1/games/35000579?loginToken=0a5e997ed6fa26213d5db9c4fafe1072",
        "requestId": 3
    }
*/

	public String getJsonBody() {
//		String url = loadPath + "?" + RestHelper.formPostData(this);
		String url = RestHelper.getInstance().createSignature(this, AppUtils.getAppId());

		String loadBody = Symbol.NEW_STR + "    {" + Symbol.NEW_STR
			+ "        \"method\": \"" + requestMethod + "\"" + Symbol.COMMA + Symbol.NEW_STR
			+ "        \"url\": \"" + url.replace(RestHelper.getInstance().BASE_URL, "") + "\"" + Symbol.COMMA + Symbol.NEW_STR;
		if (requestMethod.equals(RestHelper.PUT)) {
			loadBody += "        \"body\": {";
			for (NameValuePair pair : nameValuePairs) {
				String name = pair.getName();
				String value = pair.getValue();
				loadBody += "\"" + name + "\": " + "\"" + value + "\"" + Symbol.COMMA + Symbol.NEW_STR;
			}
			loadBody += "    }" + Symbol.COMMA + Symbol.NEW_STR;


/*
 		"body": {
            "command": "CHAT",
            "timestamp": 1355687586,
            "message": "Hellooooo",
            "loginToken": "0a5e997ed6fa26213d5db9c4fafe1072"
        },
*/
		} else if (requestMethod.equals(RestHelper.POST)) {
			loadBody += "        \"body\": {";
			String postBody = RestHelper.formPostData(this);
				loadBody += "            \"" + "content" + "\": " + "\"" + postBody + "\"" + Symbol.COMMA + Symbol.NEW_STR;
			loadBody += "        }" + Symbol.COMMA;
		}
		loadBody += "        \"requestId\": " + 0;

		loadBody += Symbol.NEW_STR + "    }";

		return loadBody;
	}
}
