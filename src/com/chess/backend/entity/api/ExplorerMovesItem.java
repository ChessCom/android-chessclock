package com.chess.backend.entity.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 05.09.13
 * Time: 21:09
 */
public class ExplorerMovesItem extends BaseResponseItem<ExplorerMovesItem.Data> {

	/*
	"data": {
        "moves": [
            {
                "move": "1.d4",
                "num_games": 335,
                "white_won_percent": 50,
                "black_won_percent": 21,
                "draw_percent": 29
            }...
        ],
        "variations": [
            "Alekhine Defense: Balogh Variation",
            "Alekhine Defense",
            "Alekhine Defense: Modern Variation",
            "Alekhine Defense: Normal Variation"
        ]
    }
    */

	public class Data {
		//private String fen;
		private List<Move> moves;
		private List<String> variations;

		public List<Move> getMoves() {
			return moves;
		}

		public void setMoves(List<Move> moves) {
			this.moves = moves;
		}

		public List<String> getVariations() {
			return variations;
		}

		public void setVariations(List<String> variations) {
			this.variations = variations;
		}
	}

	public static class Move {
		//private String fen;
		private String move;
		private int num_games; // long?
		private int white_won_percent;
		private int black_won_percent;
		private int draw_percent;

		public int getWhiteWonPercent() {
			return white_won_percent;
		}

		public void setWhiteWonPercent(int whiteWonPercent) {
			this.white_won_percent = whiteWonPercent;
		}

		public int getNumGames() {
			return num_games;
		}

		public void setNumGames(int numGames) {
			this.num_games = numGames;
		}

		public String getMove() {
			return move;
		}

		public void setMove(String move) {
			this.move = move;
		}

		public int getDrawPercent() {
			return draw_percent;
		}

		public void setDrawPercent(int drawPercent) {
			this.draw_percent = drawPercent;
		}

		public int getBlackWonPercent() {
			return black_won_percent;
		}

		public void setBlackWonPercent(int blackWonPercent) {
			this.black_won_percent = blackWonPercent;
		}

		/*public String getFen() {
			return fen;
		}

		public void setFen(String fen) {
			this.fen = fen;
		}*/
	}
}
