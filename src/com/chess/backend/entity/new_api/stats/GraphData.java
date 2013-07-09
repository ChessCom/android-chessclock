package com.chess.backend.entity.new_api.stats;

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
            "minY": 1100,
            "maxX": 1449,
            "series": [
                [
                    1370070000000,
                    1200
                ],
            ]
        }
	 */

	private int minY;
	private int maxX;
	private List<long[]> series;

}
