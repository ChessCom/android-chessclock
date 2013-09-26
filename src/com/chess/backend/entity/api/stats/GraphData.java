package com.chess.backend.entity.api.stats;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.07.13
 * Time: 7:38
 */
public class GraphData {
/*
	"graph_data": {
	"min_y": 945,
	"max_x": 1606,
	"series": [
		[
			1260259200000,
			1045
		],
		[
			1302246000000,
			1218
		],
		[
			1305270000000,
			1307
		],
		[
			1306220400000,
			1318
		],
		[
			1311318000000,
			1471
		],
		[
			1359619200000,
			1506
		],
		[
			1367478000000,
			1319
		],
		[
			1369983600000,
			1201
		],
		[
			1370674800000,
			1073
		]
	]
	 */

	private int min_y;
	private int max_x;
	private List<long[]> series;

	public int getMinY() {
		return min_y;
	}

	public int getMaxX() {
		return max_x;
	}

	public List<long[]> getSeries() {
		return series;
	}

	public static class SingleItem {
		private long timestamp;
		private int minY;
		private int maxX;
		private int rating;
		private String gameType;
		private String username;

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public int getMinY() {
			return minY;
		}

		public void setMinY(int minY) {
			this.minY = minY;
		}

		public int getMaxX() {
			return maxX;
		}

		public void setMaxX(int maxX) {
			this.maxX = maxX;
		}

		public int getRating() {
			return rating;
		}

		public void setRating(int rating) {
			this.rating = rating;
		}

		public String getGameType() {
			return gameType;
		}

		public void setGameType(String gameType) {
			this.gameType = gameType;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}

}
