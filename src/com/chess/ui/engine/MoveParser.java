package com.chess.ui.engine;

import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.boards.BoardFace;

import java.util.TreeSet;
import java.util.regex.Pattern;

public class MoveParser {
	/*
	Special Symbols
	x: captures
	0-0: kingside castle
	0-0-0: queenside castle
	+: check
	#: checkmate
	!: good move
	?: poor move
	more !s and ?s can be added for emphasis.
	*/

	// white pieces
	public static final String WHITE_QUEEN = "Q";
	public static final String WHITE_ROOK = "R";
	public static final String WHITE_BISHOP = "B";
	public static final String WHITE_KNIGHT = "N";
	public static final String WHITE_KING = "K";
	public static final String WHITE_PAWN = "P";
	// black pieces
	public static final String BLACK_QUEEN = "q";
	public static final String BLACK_ROOK = "r";
	public static final String BLACK_BISHOP = "b";
	public static final String BLACK_KNIGHT = "n";
	public static final String BLACK_KING = "k";
	public static final String BLACK_PAWN = "p";

	public static final String W_SMALL = "w";
	public static final String A_SMALL = "a";
	public static final String B_SMALL = "b";
	public static final String C_SMALL = "c";
	public static final String D_SMALL = "d";
	public static final String E_SMALL = "e";
	public static final String F_SMALL = "f";
	public static final String G_SMALL = "g";
	public static final String H_SMALL = "h";
	public static final String CAPTURE_MARK = "x";

	public static final String NUMB_1 = "1";
	public static final String NUMB_2 = "2";
	public static final String NUMB_3 = "3";
	public static final String NUMB_4 = "4";
	public static final String NUMB_5 = "5";
	public static final String NUMB_6 = "6";
	public static final String NUMB_7 = "7";
	public static final String NUMB_8 = "8";
	public static final String POSITION_DIVIDER = "|";
	private static final String FEN_DIVIDER = "[/]";
	private static final String REGEXP_NUMBERS = "[0-9]";

	public static final String W_KINGSIDE_MOVE_CASTLING = "Kg1";
	public static final String W_QUEENSIDE_MOVE_CASTLING = "Kc1";
	public static final String B_KINGSIDE_MOVE_CASTLING = "kg8";
	public static final String B_QUEENSIDE_MOVE_CASTLING = "kc8";
	public static final String KINGSIDE_CASTLING = "O-O";
	public static final String KINGSIDE_CASTLING_AND_CHECK = "O-O+";
	public static final String QUEENSIDE_CASTLING = "O-O-O";
	public static final String QUEENSIDE_CASTLING_AND_CHECK = "O-O-O+";

	private static final int ROOK = 3;
	private static final int KNIGHT = 1;
	private static final int BISHOP = 2;
	private static final int QUEEN = 4;
	private static final int KING = 5;

	//	String[] pices = new String[]{"K", "Q", "R", "B", "N", "O"};
//	String[] promotionPices = new String[]{"N", "B", "R", "Q"};

	public MoveParser() {
	}

	public static String removeNumbers(String moves) {
		return moves.replaceAll(AppConstants.MOVE_NUMBERS_PATTERN, Symbol.EMPTY)
				.replaceAll("[.]", Symbol.EMPTY);
	}

	public static int[] parseCoordinate(BoardFace board, String move) {
		TreeSet<Move> validMoves = board.generateLegalMoves();

		int promotion = 0;

		String[] MoveTo = new String[2];
		String currentMove = move.trim();

		if (currentMove.equals(KINGSIDE_CASTLING) || currentMove.equals(KINGSIDE_CASTLING_AND_CHECK)) {
			if (board.getSide() == 0) {
				return new int[]{board.getWhiteKing(), board.getWhiteKingMoveOO()[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.getBlackKing(), board.getBlackKingMoveOO()[0], 0, 2};
			}
		}
		if (currentMove.equals(QUEENSIDE_CASTLING) || currentMove.equals(QUEENSIDE_CASTLING_AND_CHECK)) {
			if (board.getSide() == 0) {
				return new int[]{board.getWhiteKing(), board.getWhiteKingMoveOOO()[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.getBlackKing(), board.getBlackKingMoveOOO()[0], 0, 2};
			}
		}

		int end = currentMove.length() - 1;
		while (end != 0) {
			if (Pattern.matches(REGEXP_NUMBERS, currentMove.substring(end, end + 1))) {
				MoveTo[0] = currentMove.substring(end - 1, end);
				MoveTo[1] = currentMove.substring(end, end + 1);
				break;
			}
			end--;
		}

		int i = letterToBN(MoveTo[0]);
		int j = numToBN(MoveTo[1]);
		int to = j * 8 - i;
		int from = numToBN(Symbol.EMPTY + currentMove.charAt(1)) * 8 - letterToBN(Symbol.EMPTY + currentMove.charAt(0));

		for (Move validMove : validMoves) {
			if (validMove.from == from && validMove.to == to) {
				char lastChar = currentMove.charAt(currentMove.length() - 1);
				if (lastChar == 'q') {
					promotion = 4;
				} else if (lastChar == 'r') {
					promotion = 3;
				} else if (lastChar == 'b') {
					promotion = 2;
				} else if (lastChar == 'n') {
					promotion = 1;
				}
				return new int[]{from, to, promotion, validMove.bits};
			}
		}

		return new int[]{from, to, promotion};
	}

	public static int[] parse(BoardFace board, String move) {
		move = removeNumbers(move);
		TreeSet<Move> validMoves = board.generateLegalMoves();

		int promotion = 0;

		String[] moveTo = new String[2];
		String currentMove = move.trim();

		if (currentMove.equals(KINGSIDE_CASTLING) || currentMove.equals(KINGSIDE_CASTLING_AND_CHECK)) {
			if (board.getSide() == ChessBoard.WHITE_SIDE) {
				return new int[]{board.getWhiteKing(), board.getWhiteKingMoveOO()[ChessBoard.BLACK_KINGSIDE_CASTLE], 0, 2};
			} else if (board.getSide() == ChessBoard.BLACK_SIDE) {
				return new int[]{board.getBlackKing(), board.getBlackKingMoveOO()[ChessBoard.BLACK_KINGSIDE_CASTLE], 0, 2};
			}
		}
		if (currentMove.equals(QUEENSIDE_CASTLING) || currentMove.equals(QUEENSIDE_CASTLING_AND_CHECK)) {
			if (board.getSide() == ChessBoard.WHITE_SIDE) {
				return new int[]{board.getWhiteKing(), board.getWhiteKingMoveOOO()[ChessBoard.BLACK_KINGSIDE_CASTLE], 0, 2};
			} else if (board.getSide() == ChessBoard.BLACK_SIDE) {
				return new int[]{board.getBlackKing(), board.getBlackKingMoveOOO()[ChessBoard.BLACK_KINGSIDE_CASTLE], 0, 2};
			}
		}

		int end = currentMove.length() - 1;
		while (end != 0) {
			if (Pattern.matches(REGEXP_NUMBERS, currentMove.substring(end, end + 1))) {
				moveTo[0] = currentMove.substring(end - 1, end);
				moveTo[1] = currentMove.substring(end, end + 1);
				break;
			}
			end--;
		}

		int i = letterToBN(moveTo[0]);
		int j = numToBN(moveTo[1]);
		int from = 0;
		int to = j * 8 - i;

		int pieceType = 0;
		if (currentMove.substring(0, 1).contains(WHITE_KNIGHT)) pieceType = KNIGHT;
		if (currentMove.substring(0, 1).contains(WHITE_BISHOP)) pieceType = BISHOP;
		if (currentMove.substring(0, 1).contains(WHITE_ROOK)) pieceType = ROOK;
		if (currentMove.substring(0, 1).contains(WHITE_QUEEN)) pieceType = QUEEN;
		if (currentMove.substring(0, 1).contains(WHITE_KING)) pieceType = KING;
		int k;

		if ((pieceType >= KNIGHT && pieceType <= QUEEN)/*(pieceType == 3 || pieceType == 1)*/
				&& !currentMove.substring(1, 2).contains(CAPTURE_MARK) && !currentMove.substring(2, 3).matches(REGEXP_NUMBERS)) {//Rooks and Knights which?
			for (k = 0; k < 64; k++) {
				int l1 = (ChessBoard.getRow(k) + 1) * 8 - letterToBN(currentMove.substring(1, 2));
				int l2 = numToBN(currentMove.substring(1, 2)) * 8 - (ChessBoard.getColumn(k) + 1);

				if (currentMove.substring(1, 2).matches("[abcdefgh]")) {
					if (board.getPieces()[l1] == pieceType && board.getColor()[l1] == board.getSide()) {
						return new int[]{l1, to, promotion};
					}
				}
				if (currentMove.substring(1, 2).matches(REGEXP_NUMBERS)) {
					if (board.getPieces()[l2] == pieceType && board.getColor()[l2] == board.getSide()) {
						return new int[]{l2, to, promotion};
					}
				}
			}
		}

		for (k = 0; k < 64; k++) {
			if (board.getPieces()[k] == pieceType && board.getColor()[k] == board.getSide()) {
				for (Move validMove : validMoves) {
					if (validMove.from == k && validMove.to == to) {
						if (pieceType == 2) {
							if (board.getBoardColor()[k] == board.getBoardColor()[to]) {
								return new int[]{k, to, promotion};
							}
						} else if (pieceType == 0) {
							if (currentMove.contains(CAPTURE_MARK)
									&& 9 - letterToBN(currentMove.substring(0, 1)) != ChessBoard.getColumn(k) + 1) {
								break;
							}

							if (currentMove.contains(WHITE_QUEEN)) {
								promotion = 4;
							}
							if (currentMove.contains(WHITE_ROOK)) {
								promotion = 3;
							}
							if (currentMove.contains(WHITE_BISHOP)) {
								promotion = 2;
							}
							if (currentMove.contains(WHITE_KNIGHT)) {
								promotion = 1;
							}

							return new int[]{k, to, promotion, validMove.bits};
						} else {
							return new int[]{k, to, promotion};
						}
					}
				}
			}
		}

		return new int[]{from, to, promotion};
	}

	public static int letterToBN(String l) {
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

	public static int numToBN(String l) {
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
		String l = Symbol.EMPTY;
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
		String l = Symbol.EMPTY;
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
		return BNToLetter(ChessBoard.getColumn(pos)) + BNToNum(ChessBoard.getRow(pos));
	}

	public static void fenParse(String fen, BoardFace boardFace) {
		String[] FEN = fen.split(FEN_DIVIDER);
		int i, j, p = 0;
		for (i = 0; i < 8; i++) {
			String pos = FEN[i];
			if (i == 7) {
				String[] tmp2 = FEN[i].split(Symbol.SPACE);
				pos = tmp2[0];
				if (tmp2[1].contains(W_SMALL)) {
					boardFace.setSide(0);
					boardFace.setOppositeSide(1);
				} else {
					boardFace.setSide(1);
					boardFace.setOppositeSide(0);
				}
			}
			String[] piecesArray = pos.trim().split(POSITION_DIVIDER);
			for (j = 1; j < piecesArray.length; j++) {
				if (piecesArray[j].matches(REGEXP_NUMBERS)) {
					int cnt = Integer.parseInt(piecesArray[j]);
					while (cnt > 0) {
						boardFace.getPieces()[p] = 6;
						boardFace.getColor()[p++] = 6;
						cnt--;
					}
				}
				if (piecesArray[j].contains(WHITE_PAWN)) {
					boardFace.getPieces()[p] = 0;
					boardFace.getColor()[p++] = 0;
				}
				if (piecesArray[j].contains(WHITE_KNIGHT)) {
					boardFace.getPieces()[p] = 1;
					boardFace.getColor()[p++] = 0;
				}
				if (piecesArray[j].contains(WHITE_BISHOP)) {
					boardFace.getPieces()[p] = 2;
					boardFace.getColor()[p++] = 0;
				}
				if (piecesArray[j].contains(WHITE_ROOK)) {
					boardFace.getPieces()[p] = 3;
					boardFace.getColor()[p++] = 0;
				}
				if (piecesArray[j].contains(WHITE_QUEEN)) {
					boardFace.getPieces()[p] = 4;
					boardFace.getColor()[p++] = 0;
				}
				if (piecesArray[j].contains(WHITE_KING)) {
					boardFace.getPieces()[p] = 5;
					boardFace.getColor()[p++] = 0;
				}
				if (piecesArray[j].contains(BLACK_PAWN)) {
					boardFace.getPieces()[p] = 0;
					boardFace.getColor()[p++] = 1;
				}
				if (piecesArray[j].contains(BLACK_KNIGHT)) {
					boardFace.getPieces()[p] = 1;
					boardFace.getColor()[p++] = 1;
				}
				if (piecesArray[j].contains(BLACK_BISHOP)) {
					boardFace.getPieces()[p] = 2;
					boardFace.getColor()[p++] = 1;
				}
				if (piecesArray[j].contains(BLACK_ROOK)) {
					boardFace.getPieces()[p] = 3;
					boardFace.getColor()[p++] = 1;
				}
				if (piecesArray[j].contains(BLACK_QUEEN)) {
					boardFace.getPieces()[p] = 4;
					boardFace.getColor()[p++] = 1;
				}
				if (piecesArray[j].contains(BLACK_KING)) {
					boardFace.getPieces()[p] = 5;
					boardFace.getColor()[p++] = 1;
				}
			}
		}
	}
}
