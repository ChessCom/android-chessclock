package com.chess.backend.entity;

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
public class LoadItem {
	private String loadPath;
	private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

	public LoadItem() {
		nameValuePairs = new ArrayList<NameValuePair>();
	}

	public void addRequestParams(String key, String value) {
		nameValuePairs.add(new BasicNameValuePair(key, value));
	}

	public void addRequestParams(NameValuePair pair) {
		nameValuePairs.add(pair);
	}

	public void replaceRequestParams(String key, String value) {
		NameValuePair pairNew = new BasicNameValuePair(key, value);
		ArrayList<NameValuePair> removedItems = new ArrayList<NameValuePair>();

		for (NameValuePair nameValuePair : nameValuePairs) {
			if(nameValuePair.getName().equals(key))
				removedItems.add(nameValuePair);

		}

		nameValuePairs.removeAll(removedItems);
		nameValuePairs.add(pairNew);
	}

	public List<NameValuePair> getRequestParams(){
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
}
