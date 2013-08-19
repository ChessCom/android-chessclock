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
            "min_y": 1100,
            "min_x": 1449,
            "series": [
                [
                    1370070000000,
                    1200
                ],
            ]
        }
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
