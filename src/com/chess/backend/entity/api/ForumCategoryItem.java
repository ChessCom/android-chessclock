package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.07.13
 * Time: 20:44
 */
public class ForumCategoryItem extends BaseResponseItem<List<ForumCategoryItem.Data>> {
/*
      "category_id": 10,
      "create_date": 1166293176,
      "last_date": 1166293176,
      "category": "General Chess Discussion",
      "display_order": 20,
      "description": "Have something to discuss that doesn't really fit elsewhere? Say it here!",
      "topic_count": 34,
      "post_count": 129,
      "minimum_membership_level": 10
*/

	public class Data {
		private String category;
		private int category_id;
		private long create_date;
		private long last_date;
		private int display_order;
		private String description;
		private int topic_count;
		private int post_count;
		private int minimum_membership_level;

		public int getId() {
			return category_id;
		}

		public long getCreateDate() {
			return create_date;
		}

		public long getLastDate() {
			return last_date;
		}

		public String getCategory() {
			return category;
		}

		public int getDisplayOrder() {
			return display_order;
		}

		public String getDescription() {
			return description;
		}

		public int getTopicCount() {
			return topic_count;
		}

		public int getPostCount() {
			return post_count;
		}

		public int getMinimumMembershipLevel() {
			return minimum_membership_level;
		}
	}
}
