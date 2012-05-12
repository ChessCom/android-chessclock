package com.chess.ui.engine;

import com.chess.backend.statics.StaticData;
import com.chess.ui.interfaces.BoardFace;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class MoveParser {
	public static final String Q_BIG = "Q";
	public static final String R_BIG = "R";
	public static final String B_BIG = "B";
	public static final String N_BIG = "N";
	public static final String A_SMALL = "a";
	public static final String B_SMALL = "b";
	public static final String C_SMALL = "c";
	public static final String D_SMALL = "d";
	public static final String E_SMALL = "e";
	public static final String F_SMALL = "f";
	public static final String G_SMALL = "g";
	public static final String H_SMALL = "h";
	public static final String NUMB_1 = "1";
	public static final String NUMB_2 = "2";
	public static final String NUMB_3 = "3";
	public static final String NUMB_4 = "4";
	public static final String NUMB_5 = "5";
	public static final String NUMB_6 = "6";
	public static final String NUMB_7 = "7";
	public static final String NUMB_8 = "8";
	public static final String K_BIG = "K";
	public static final String P_SMALL = "p";
	public static final String N_SMALL = "n";
	public static final String R_SMALL = "r";
	public static final String Q_SMALL = "q";
	public static final String K_SMALL = "k";
	public static final String W_SMALL = "w";
	public static final String P_BIG = "P";
	public static final String X_SMALL = "x";
	//	String[] pices = new String[]{"K", "Q", "R", "B", "N", "O"};
//	String[] promotionPices = new String[]{"N", "B", "R", "Q"};

	public MoveParser() {
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
		int from = NumToBN(StaticData.SYMBOL_EMPTY + currentmove.charAt(1)) * 8 - LetterToBN(StaticData.SYMBOL_EMPTY + currentmove.charAt(0));

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

	public static int[] parse(BoardFace board, String move) {
		TreeSet<Move> validMoves = board.gen();

		int promotion = 0;

		String[] moveTo = new String[2];
		String currentMove = move.trim();

		if (currentMove.equals("O-O") || currentMove.equals("O-O+")) {
			if (board.getSide() == 0) {
				return new int[]{board.getwKing(), board.getwKingMoveOO()[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.getbKing(), board.getbKingMoveOO()[0], 0, 2};
			}
		}
		if (currentMove.equals("O-O-O") || currentMove.equals("O-O-O+")) {
			if (board.getSide() == 0) {
				return new int[]{board.getwKing(), board.getwKingMoveOOO()[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.getbKing(), board.getbKingMoveOOO()[0], 0, 2};
			}
		}

		int end = currentMove.length() - 1;
		while (end != 0) {
			if (Pattern.matches("[0-9]", currentMove.substring(end, end + 1))) {
				moveTo[0] = currentMove.substring(end - 1, end);
				moveTo[1] = currentMove.substring(end, end + 1);
				break;
			}
			end--;
		}

		int i = LetterToBN(moveTo[0]);
		int j = NumToBN(moveTo[1]);
		int from = 0;
		int to = j * 8 - i;

		int pieceType = 0;
		if (currentMove.substring(0, 1).contains(N_BIG)) pieceType = 1;
		if (currentMove.substring(0, 1).contains(B_BIG)) pieceType = 2;
		if (currentMove.substring(0, 1).contains(R_BIG)) pieceType = 3;
		if (currentMove.substring(0, 1).contains(Q_BIG)) pieceType = 4;
		if (currentMove.substring(0, 1).contains(K_BIG)) pieceType = 5;
		int k;

		if ((pieceType >= 1 && pieceType <= 4)/*(pieceType == 3 || pieceType == 1)*/
				&& !currentMove.substring(1, 2).contains(X_SMALL) && !currentMove.substring(2, 3).matches("[0-9]")) {//Rooks and Knights which?
			for (k = 0; k < 64; k++) {
				int l1 = (ChessBoard.ROW(k) + 1) * 8 - LetterToBN(currentMove.substring(1, 2));
				int l2 = NumToBN(currentMove.substring(1, 2)) * 8 - (ChessBoard.COL(k) + 1);

				if (currentMove.substring(1, 2).matches("[abcdefgh]")) {
					if (board.getPieces()[l1] == pieceType && board.getColor()[l1] == board.getSide()) {
						return new int[]{l1, to, promotion};
					}
				}
				if (currentMove.substring(1, 2).matches("[0-9]")) {
					if (board.getPieces()[l2] == pieceType && board.getColor()[l2] == board.getSide())
						return new int[]{l2, to, promotion};
				}
			}
		}

		for (k = 0; k < 64; k++) {
			if (board.getPieces()[k] == pieceType && board.getColor()[k] == board.getSide()) {
				Iterator<Move> moveIterator = validMoves.iterator();
				Move move1;
				while (moveIterator.hasNext()) {
					move1 = moveIterator.next();
					if (move1.from == k && move1.to == to) {
						if (pieceType == 2) {
							if (board.getBoardColor()[k] == board.getBoardColor()[to])
								return new int[]{k, to, promotion};
						} else if (pieceType == 0) {
							if (currentMove.contains(X_SMALL)
									&& 9 - LetterToBN(currentMove.substring(0, 1)) != ChessBoard.COL(k) + 1) {
								break;
							}

							if (currentMove.contains(Q_BIG))
								promotion = 4;
							if (currentMove.contains(R_BIG))
								promotion = 3;
							if (currentMove.contains(B_BIG))
								promotion = 2;
							if (currentMove.contains(N_BIG))
								promotion = 1;

							return new int[]{k, to, promotion, move1.bits};
						} else return new int[]{k, to, promotion};
					}
				}
			}
		}

		return new int[]{from, to, promotion};
	}

	public static int LetterToBN(String l) {
		int i = 0;
		if (l.toLowerCase().contains(A_SMALL)) i = 8;
		if (l.toLowerCase().contains(B_SMALL)) i = 7;
		if (l.toLowerCase().contains(C_SMALL)) i = 6;
		if (l.toLowerCase().contains(D_SMALL)) i = 5;
		if (l.toLowerCase().contains(E_SMALL)) i = 4;
		if (l.toLowerCase().contains(F_SMALL)) i = 3;
		if (l.toLowerCase().contains(G_SMALL)) i = 2;
		if (l.toLowerCase().contains(H_SMALL)) i = 1;

		return i;
	}

	public static int NumToBN(String l) {
		int j = 0;
		if (l.contains(NUMB_1)) j = 8;
		if (l.contains(NUMB_2)) j = 7;
		if (l.contains(NUMB_3)) j = 6;
		if (l.contains(NUMB_4)) j = 5;
		if (l.contains(NUMB_5)) j = 4;
		if (l.contains(NUMB_6)) j = 3;
		if (l.contains(NUMB_7)) j = 2;
		if (l.contains(NUMB_8)) j = 1;
		return j;
	}

	public static String BNToLetter(int i) {
		String l = StaticData.SYMBOL_EMPTY;
		if (i == 7) l = H_SMALL;
		if (i == 6) l = G_SMALL;
		if (i == 5) l = F_SMALL;
		if (i == 4) l = E_SMALL;
		if (i == 3) l = D_SMALL;
		if (i == 2) l = C_SMALL;
		if (i == 1) l = B_SMALL;
		if (i == 0) l = A_SMALL;

		return l;
	}

	public static String BNToNum(int j) {
		String l = StaticData.SYMBOL_EMPTY;
		if (j == 7) l = NUMB_1;
		if (j == 6) l = NUMB_2;
		if (j == 5) l = NUMB_3;
		if (j == 4) l = NUMB_4;
		if (j == 3) l = NUMB_5;
		if (j == 2) l = NUMB_6;
		if (j == 1) l = NUMB_7;
		if (j == 0) l = NUMB_8;

		return l;
	}

	public static String positionToString(int pos) {
		return BNToLetter(ChessBoard.COL(pos)) + BNToNum(ChessBoard.ROW(pos));
	}

	//    public static void fenParse(String fen, ChessBoard b) {
	public static void fenParse(String fen, BoardFace b) {
		String[] FEN = fen.split("[/]");
		int i, j, p = 0;
		for (i = 0; i < 8; i++) {
			String pos = FEN[i];
			if (i == 7) {
				String[] tmp2 = FEN[i].split(" ");
				pos = tmp2[0];
				if (tmp2[1].contains(W_SMALL)) {
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
						b.getPieces()[p] = 6;
						b.getColor()[p++] = 6;
						cnt--;
					}
				}
				if (f[j].contains(P_BIG)) {
					b.getPieces()[p] = 0;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains(N_BIG)) {
					b.getPieces()[p] = 1;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains(B_BIG)) {
					b.getPieces()[p] = 2;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains(R_BIG)) {
					b.getPieces()[p] = 3;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains(Q_BIG)) {
					b.getPieces()[p] = 4;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains(K_BIG)) {
					b.getPieces()[p] = 5;
					b.getColor()[p++] = 0;
				}
				if (f[j].contains(P_SMALL)) {
					b.getPieces()[p] = 0;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains(N_SMALL)) {
					b.getPieces()[p] = 1;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains(B_SMALL)) {
					b.getPieces()[p] = 2;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains(R_SMALL)) {
					b.getPieces()[p] = 3;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains(Q_SMALL)) {
					b.getPieces()[p] = 4;
					b.getColor()[p++] = 1;
				}
				if (f[j].contains(K_SMALL)) {
					b.getPieces()[p] = 5;
					b.getColor()[p++] = 1;
				}
			}
		}
	}
}
