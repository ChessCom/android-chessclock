package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.11.13
 * Time: 12:16
 */
public class ServersStatsItem extends BaseResponseItem<ServersStatsItem.Data> {

/*
	"data": {
        "totals": {
            "members": 129439,
            "games": 46,
            "online": 2,
            "tourneys": 13,
            "teams": 620,
            "live": 0
        }
    }
*/

	public class Data {
		private Totals totals;

		public Totals getTotals() {
			return totals;
		}
	}

	public class Totals {
		private long members;
		private long games;
		private long online;
		private int tourneys;
		private int teams;
		private long live;

		public long getMembers() {
			return members;
		}

		public long getGames() {
			return games;
		}

		public long getOnline() {
			return online;
		}

		public int getTourneys() {
			return tourneys;
		}

		public int getTeams() {
			return teams;
		}

		public long getLive() {
			return live;
		}
	}
}
