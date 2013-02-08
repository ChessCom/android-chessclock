package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.02.13
 * Time: 8:38
 */
public class CommonFeedCategoryItem extends BaseResponseItem<List<CommonFeedCategoryItem.Data>> {
/*
{
	"id": 7,
	"name": "Rules &amp; Basics",
	"code":"rules-basics",
	"display_order": 10
},
*/

	public class Data {
		private int id;
		private String name;
		private int display_order;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getDisplayOrder() {
			return display_order;
		}
	}

}
