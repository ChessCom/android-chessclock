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
		...
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

}
