package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 11:56
 */
public class ArticleCategoryItem extends BaseResponseItem<List<ArticleCategoryItem.Data>> {
/*
 	"id": 11,
	"name": "For Beginners",
	"display_order": 10
*/

	public class Data {
		private long id;
		private String name;
		private int display_order;

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getDisplayOrder() {
			return display_order;
		}
	}
}
