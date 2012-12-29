package com.chess.backend.entity.new_api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.12.12
 * Time: 15:18
 */
public class VideoItem extends BaseResponseItem<VideoItem.Data>{
	/*
	"status": "success",
    "data": {
        "total_videos_count": "3",
        "videos": [
            {
                "name": "The Empire Goes On Strike",
                "description": "Believe it or not, they have a powerful union.",
                "category": "Tactics",
                "skill_level": "Intermediate-Advanced",
                "eco_name": null,
                "first_name": "Michael",
                "chess_title": "GM",
                "last_name": "Greene",
                "minutes": 5,
                "live_date": 1333263600,
                "mobile_view_url": "http://www.c.com/index_api_dev.php/video/player_mobile/the-empire-goes-on-strike/playme.mp4?loginToken=demo",
                "key_fen": "1Rr2kn1/Q1B4r/8/5n2/3P2pq/2P5/P4PPP/5RK1 b - - 0 1"
            }....
        ]
    }
	 */

	public static class Data {
		private String total_videos_count;
		private List<VideoDataItem> videos;

		public String getTotal_videos_count() {
			return total_videos_count;
		}

		public List<VideoDataItem> getVideos() {
			return videos;
		}
	}

	public static class VideoDataItem {
	/*
		"name": "The Empire Goes On Strike",
		"description": "Believe it or not, they have a powerful union.",
		"category": "Tactics",
		"skill_level": "Intermediate-Advanced",
		"eco_name": null,
		"first_name": "Michael",
		"chess_title": "GM",
		"last_name": "Greene",
		"minutes": 5,
		"live_date": 1333263600,
		"mobile_view_url": "http://www.c.com/index_api_dev.php/video/player_mobile/the-empire-goes-on-strike/playme.mp4?loginToken=demo",
		"key_fen": "1Rr2kn1/Q1B4r/8/5n2/3P2pq/2P5/P4PPP/5RK1 b - - 0 1"
	*/
		private String name;
		private String description;
		private String category;
		private String skill_level;
		private String eco_name;
		private String first_name;
		private String chess_title;
		private String last_name;
		private int minutes;
		private long live_date;
		private String mobile_view_url;
		private String key_fen;

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getCategory() {
			return category;
		}

		public String getSkill_level() {
			return skill_level;
		}

		public String getEco_name() {
			return eco_name;
		}

		public String getFirst_name() {
			return first_name;
		}

		public String getChess_title() {
			return chess_title;
		}

		public String getLast_name() {
			return last_name;
		}

		public int getMinutes() {
			return minutes;
		}

		public long getLive_date() {
			return live_date;
		}

		public String getMobile_view_url() {
			return mobile_view_url;
		}

		public String getKey_fen() {
			return key_fen;
		}
	}
}
