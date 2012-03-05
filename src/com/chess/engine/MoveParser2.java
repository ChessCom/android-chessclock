package com.chess.engine;

import com.chess.core.interfaces.BoardFace;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class MoveParser2 {
	String[] Pices = new String[]{"K", "Q", "R", "B", "N", "O"};
	String[] PromotionPices = new String[]{"N", "B", "R", "Q"};

	public MoveParser2() {
	}

	public static int[] parseCoordinate(BoardFace board, String move) {
		TreeSet<Move> validMoves = board.gen();

		int promotion = 0;

		String[] MoveTo = new String[2];
		String currentmove = move.trim();

		if (currentmove.equals("O-O") || currentmove.equals("O-O+")) {
			if (board.getSide() == 0) {
				return new int[]{board.getwKing(), board.getwKingMoveOO()[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.getbKing(), board.getbKingMoveOO()[0], 0, 2};
			}
		}
		if (currentmove.equals("O-O-O") || currentmove.equals("O-O-O+")) {
			if (board.getSide() == 0) {
				return new int[]{board.getwKing(), board.getwKingMoveOOO()[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.getbKing(), board.getbKingMoveOOO()[0], 0, 2};
			}
		}

		int end = currentmove.length() - 1;
		while (end != 0) {
			if (Pattern.matches("[0-9]", currentmove.substring(end, end + 1))) {
				MoveTo[0] = currentmove.substring(end - 1, end);
				MoveTo[1] = currentmove.substring(end, end + 1);
				break;
			}
			end--;
		}

		int i = LetterToBN(MoveTo[0]);
		int j = NumToBN(MoveTo[1]);
		int to = j * 8 - i;
		int from = NumToBN("" + currentmove.charAt(1)) * 8 - LetterToBN("" + currentmove.charAt(0));

		Iterator<Move> itr = validMoves.iterator();
		Move M;
		while (itr.hasNext()) {
			M = itr.next();
			if (M.from == from && M.to == to) {
				char lastChar = currentmove.charAt(currentmove.length() - 1);
				if (lastChar == 'q') {
					promotion = 4;
				} else if (lastChar == 'r') {
					promotion = 3;
				} else if (lastChar == 'b') {
					promotion = 2;
				} else if (lastChar == 'n') {
					promotion = 1;
				}
				return new int[]{from, to, promotion, M.bits};
			}
		}

		return new int[]{from, to, promotion};
	}

	public static int[] Parse(BoardFace board, String move) {
		TreeSet<Move> validMoves = board.gen();

		int promotion = 0;

		String[] MoveTo = new String[2];
		String currentmove = move.trim();

		if (currentmove.equals("O-O") || currentmove.equals("O-O+")) {
			if (board.getSide() == 0) {
				return new int[]{board.getwKing(), board.getwKingMoveOO()[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.getbKing(), board.getbKingMoveOO()[0], 0, 2};
			}
		}
		if (currentmove.equals("O-O-O") || currentmove.equals("O-O-O+")) {
			if (board.getSide() == 0) {
				return new int[]{board.getwKing(), board.getwKingMoveOOO()[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.getbKing(), board.getbKingMoveOOO()[0], 0, 2};
			}
		}

		int end = currentmove.length() - 1;
		while (end != 0) {
			if (Pattern.matches("[0-9]", currentmove.substring(end, end + 1))) {
				MoveTo[0] = currentmove.substring(end - 1, end);
				MoveTo[1] = currentmove.substring(end, end + 1);
				break;
			}
			end--;
		}

		int i = LetterToBN(MoveTo[0]);
		int j = NumToBN(MoveTo[1]);
		int from = 0;
		int to = j * 8 - i;

		int pieceType = 0;
		if (currentmove.substring(0, 1).contains("N")) pieceType = 1;
		if (currentmove.substring(0, 1).contains("B")) pieceType = 2;
		if (currentmove.substring(0, 1).contains("R")) pieceType = 3;
		if (currentmove.substring(0, 1).contains("Q")) pieceType = 4;
		if (currentmove.substring(0, 1).contains("K")) pieceType = 5;
		int k;
		if ((pieceType >= 1 && pieceType <= 4)/*(pieceType == 3 || pieceType == 1)*/ && !currentmove.substring(1, 2).contains("x") && !currentmove.substring(2, 3).matches("[0-9]")) {//Rooks and Knights which?
			for (k = 0; k < 64; k++) {
				int l1 = (Board.ROW(k) + 1) * 8 - LetterToBN(currentmove.substring(1, 2));
				int l2 = NumToBN(currentmove.substring(1, 2)) * 8 - (Board.COL(k) + 1);

				if (currentmove.substring(1, 2).matches("[abcdefgh]")) {
					if (board.getPiece()[l1] == pieceType && board.getColor()[l1] == board.getSide()) {
						return new int[]{l1, to, promotion};
					}
				}
				if (currentmove.substring(1, 2).matches("[0-9]")) {
					if (board.getPiece()[l2] == pieceType && board.getColor()[l2] == board.getSide())
						return new int[]{l2, to, promotion};
				}
			}
		}

		for (k = 0; k < 64; k++) {
			if (board.getPiece()[k] == pieceType && board.getColor()[k] == board.getSide()) {
				Iterator<Move> itr = validMoves.iterator();
				Move M = null;
				while (itr.hasNext()) {
					M = (Move) itr.next();
					if (M.from == k && M.to == to) {
						if (pieceType == 2) {
							if (board.getBoardColor()[k] == board.getBoardColor()[to])
								return new int[]{k, to, promotion};
						} else if (pieceType == 0) {
							if (currentmove.contains("x") && 9 - LetterToBN(currentmove.substring(0, 1)) != Board.COL(k) + 1) {
								break;
							}

							if (currentmove.contains("Q"))
								promotion = 4;
							if (currentmove.contains("R"))
								promotion = 3;
							if (currentmove.contains("B"))
								promotion = 2;
							if (currentmove.contains("N"))
								promotion = 1;

							return new int[]{k, to, promotion, M.bits};
						} else return new int[]{k, to, promotion};
					}
				}
			}
		}

		return new int[]{from, to, promotion};
	}

	public static int LetterToBN(String l) {
		int i = 0;
		if (l.toLowerCase().contains("a")) i = 8;
		if (l.toLowerCase().contains("b")) i = 7;
		if (l.toLowerCase().contains("c")) i = 6;
		if (l.toLowerCase().contains("d")) i = 5;
		if (l.toLowerCase().contains("e")) i = 4;
		if (l.toLowerCase().contains("f")) i = 3;
		if (l.toLowerCase().contains("g")) i = 2;
		if (l.toLowerCase().contains("h")) i = 1;

		return i;
	}

	public static int NumToBN(String l) {
		int j = 0;
		if (l.contains("1")) j = 8;
		if (l.contains("2")) j = 7;
		if (l.contains("3")) j = 6;
		if (l.contains("4")) j = 5;
		if (l.contains("5")) j = 4;
		if (l.contains("6")) j = 3;
		if (l.contains("7")) j = 2;
		if (l.contains("8")) j = 1;
		return j;
	}

	public static String BNToLetter(int i) {
		String l = "";
		if (i == 7) l = "h";
		if (i == 6) l = "g";
		if (i == 5) l = "f";
		if (i == 4) l = "e";
		if (i == 3) l = "d";
		if (i == 2) l = "c";
		if (i == 1) l = "b";
		if (i == 0) l = "a";

		return l;
	}

	public static String BNToNum(int j) {
		String l = "";
		if (j == 7) l = "1";
		if (j == 6) l = "2";
		if (j == 5) l = "3";
		if (j == 4) l = "4";
		if (j == 3) l = "5";
		if (j == 2) l = "6";
		if (j == 1) l = "7";
		if (j == 0) l = "8";

		return l;
	}

	public static String positionToString(int pos) {
		return BNToLetter(Board.COL(pos)) + BNToNum(Board.ROW(pos));
	}

//    public static void FenParse(String fen, Board2 b) {
    public static void FenParse(String fen, BoardFace b) {
		String[] FEN = fen.split("[/]");
		int i, j, p = 0;
		for (i = 0; i < 8; i++) {
			String pos = FEN[i];
			if (i == 7) {
				String[] tmp2 = FEN[i].split(" ");
				pos = tmp2[0];
				if (tmp2[1].contains("w")) {
					b.setSide(0);
					b.setXside(1);
				} else {
					b.setSide(1);
					b.setXside(0);
				}
			}
			String[] f = pos.trim().split("|");
			for (j = 1; j < f.length; j++) {
				if (f[j].matches("[0-9]")) {
					int cnt = new Integer(f[j]);
					while (cnt > 0) {
						b.getPiece()[p] = 6;
						b.getColor()[p++] = 6;
						cnt--;
					}
				}
				if (f[j].contains("P")) {
					b.getPiece()[p] = 0;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains("N")) {
					b.getPiece()[p] = 1;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains("B")) {
					b.getPiece()[p] = 2;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains("R")) {
					b.getPiece()[p] = 3;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains("Q")) {
					b.getPiece()[p] = 4;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains("K")) {
					b.getPiece()[p] = 5;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains("p")) {
					b.getPiece()[p] = 0;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains("n")) {
					b.getPiece()[p] = 1;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains("b")) {
					b.getPiece()[p] = 2;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains("r")) {
					b.getPiece()[p] = 3;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains("q")) {
					b.getPiece()[p] = 4;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains("k")) {
					b.getPiece()[p] = 5;
					b.getColor()[p++] = 1;
				}
			}
		}
	}
}
