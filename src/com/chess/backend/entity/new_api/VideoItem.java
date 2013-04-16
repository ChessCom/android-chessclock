package com.chess.backend.entity.new_api;

import com.chess.backend.statics.StaticData;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.12.12
 * Time: 15:18
 */
public class VideoItem extends BaseResponseItem<List<VideoItem.Data>> {
/*
{
  "status": "success",
  "count": 20,
  "data": [
    {
      "name": "Alphatest",
      "description": "Letters maybe?",
      "category": "Tactics",
      "skill_level": "Advanced",
      "username": "deepgreene",
      "first_name": "\u0412\u043e\u0432\u043a",
      "last_name": "\u0410\u043d\u0434\u0440\u0456\u0439",
      "minutes": 3,
      "timestamp": 1298620800,
      "url": "https:\/\/s3.amazonaws.com\/chess-7\/",
      "key_fen": "8\/4kp2\/2b3pp\/4p3\/1pB1P3\/1N3PP1\/1b2KP2\/8 w - - 0 1"
    },

 */

	/*   Old Request
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
                "timestamp": 1333263600,
                "url": "http://www.c.com/index_api_dev.php/video/player_mobile/the-empire-goes-on-strike/playme.mp4?loginToken=demo",
                "key_fen": "1Rr2kn1/Q1B4r/8/5n2/3P2pq/2P5/P4PPP/5RK1 b - - 0 1"
            }....
        ]
    }
	 */

	public static class Data {
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
			"timestamp": 1333263600,
			"url": "http://www.c.com/index_api_dev.php/video/player_mobile/the-empire-goes-on-strike/playme.mp4?loginToken=demo",
			"key_fen": "1Rr2kn1/Q1B4r/8/5n2/3P2pq/2P5/P4PPP/5RK1 b - - 0 1"
		*/
		private String name;
		private String description;
		private String category;
		private String skill_level;
		private String eco_name;
		private String username;
		private String first_name;
		private String chess_title;
		private String last_name;
		private int minutes;
		private long timestamp;
		private String url;
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
			return eco_name == null ? StaticData.SYMBOL_EMPTY : eco_name;
		}

		public String getUsername() {
			return username;
		}

		public String getFirstName() {
			return first_name;
		}

		public String getChessTitle() {
			return chess_title == null ? StaticData.SYMBOL_EMPTY : chess_title;
		}

		public String getLastName() {
			return last_name;
		}

		public int getMinutes() {
			return minutes;
		}

		public long getCreateDate() {
			return timestamp;
		}

		public String getUrl() {
			return url;
		}

		public String getKeyFen() {
			return key_fen == null ? StaticData.SYMBOL_EMPTY : key_fen; // could be null from server
		}
	}
}
