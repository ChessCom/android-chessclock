package com.chess.backend;

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
}
