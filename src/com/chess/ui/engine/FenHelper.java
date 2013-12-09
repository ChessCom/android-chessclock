package com.chess.ui.engine;

import android.util.Log;
import com.chess.statics.Symbol;

import java.util.HashMap;

import static com.chess.ui.engine.ChessBoard.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.09.13
 * Time: 11:33
 */
public class FenHelper {

	// white pieces
//	public static final String WHITE_QUEEN = "Q";
//	public static final String WHITE_ROOK = "R";
//	public static final String WHITE_BISHOP = "B";
//	public static final String WHITE_KNIGHT = "N";
//	public static final String WHITE_KING = "K";
//	public static final String WHITE_PAWN = "P";
	// black pieces
//	public static final String BLACK_QUEEN = "q";
//	public static final String BLACK_ROOK = "r";
//	public static final String BLACK_BISHOP = "b";
//	public static final String BLACK_KNIGHT = "n";
//	public static final String BLACK_KING = "k";
//	public static final String BLACK_PAWN = "p";

	public static final int NO_CASTLING = 0;
	public static final int KINGSIDE_CASTLING = 1;
	public static final int QUEENSIDE_CASTLING = 2;
	public static final int BOTH_CASTLING = 3;
	private static final String EMPTY_SQUARE = "1";

	public static final String WHITE_TO_MOVE = "w";


	public static final String POSITION_DIVIDER = "|";
	private static final String FEN_DIVIDER = "[/]";
	private static final int ROWS_CNT = 8;
	private final HashMap<String, PieceData> pieceDataMap;
	public static final String whitePiecesChars[] = new String[]{"P", "N", "B", "R", "Q", "K"};
	public static final String blackPiecesChars[] = new String[]{"p", "n", "b", "r", "q", "k"};
	public static final int piecesCodes[] = new int[]{0, 1, 2, 3, 4, 5};

	public FenHelper() {
		pieceDataMap = new HashMap<String, PieceData>();
		// fill white side
		for (int i = 0; i < whitePiecesChars.length; i++) {
			String piecesStr = whitePiecesChars[i];
			int piecesCode = piecesCodes[i];

			pieceDataMap.put(piecesStr, new PieceData(piecesCode, WHITE_SIDE));
		}
		// fill black side
		for (int i = 0; i < blackPiecesChars.length; i++) {
			String piecesStr = blackPiecesChars[i];
			int piecesCode = piecesCodes[i];

			pieceDataMap.put(piecesStr, new PieceData(piecesCode, BLACK_SIDE));
		}

	}

	/**
	 * 1. Piece placement (from white's perspective). Each rank is described, starting with rank 8 and ending with rank 1;
	 * within each rank, the contents of each square are described from file "a" through file "h". Following the
	 * Standard Algebraic Notation (SAN), each piece is identified by a single letter taken from the standard English
	 * names (pawn = "P", knight = "N", bishop = "B", rook = "R", queen = "Q" and king = "K").[1] White pieces are
	 * designated using upper-case letters ("PNBRQK") while black pieces use lowercase ("pnbrqk"). Blank squares are
	 * noted using digits 1 through 8 (the number of blank squares), and "/" separates ranks.
	 * 2. Active color. "w" means white moves next, "b" means black.
	 * 3. Castling availability. If neither side can castle, this is "-". Otherwise, this has one or more letters: "K"
	 * (White can castle kingside), "Q" (White can castle queenside), "k" (Black can castle kingside), and/or "q"
	 * (Black can castle queenside).
	 * 4. En passant target square in algebraic notation. If there's no en passant target square, this is "-". If a pawn
	 * has just made a two-square move, this is the position "behind" the pawn. This is recorded regardless of whether
	 * there is a pawn in position to make an en passant capture.[2]
	 * 5. Halfmove clock: This is the number of halfmoves since the last pawn advance or capture. This is used to
	 * determine if a draw can be claimed under the fifty-move rule. The fifth field is a nonnegative integer
	 * representing the halfmove clock. This number is the count of halfmoves (or ply) since the last pawn advance
	 * or capturing move. This value is used for the fifty move draw rule.
	 * 6. Fullmove number: The number of the full move. It starts at 1, and is incremented after Black's move.
	 * The sixth and last field is a positive integer that gives the fullmove number. This will have the value "1"
	 * for the first move of a game for both White and Black. It is incremented by one immediately after each move by Black.
	 * <p/>
	 * Example : rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	 * Example : rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1
	 * Example : 6k1/ppr1pp1p/2p3p1/4P1Pn/P2rp1B1/1Pq5/7P/R2Q1RK1 w - -
	 *
	 * @return generated FEN for current board configuration
	 * @see <a href="http://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation#cite_note-2">wiki/FEN</a>
	 */

	public String generateFullFen(ChessBoard board) {
		String fen = generateBaseFen(board);
		StringBuilder sb = new StringBuilder(fen);

		// add  En passant target square
		String enPassantStr = "-";
		if (board.enPassant != NOT_SET) {
			enPassantStr = getEnpassantMoveStr(board);
		}

		sb.append(" ").append(enPassantStr);

		// add halfmove or ply
		sb.append(" ").append(board.fifty);

		// add fullmove
		sb.append(" ").append(board.ply);

		Log.d("TEST", "FULL FEN = " + sb.toString());
		return sb.toString();
	}


	public String generateBaseFen(ChessBoard board) {
		StringBuilder sb = new StringBuilder();
		String[] line = new String[ROWS_CNT];

		for (int i = 0; i < ChessBoard.SQUARES_CNT; i++) {
			if (i > 0 && i % ROWS_CNT == 0) { // if end of board line
				fillTheFenLine(sb, line, false);
			}
			int pieceCode = board.pieces[i];
			Log.e("TEST", "pieceCode = " + pieceCode);
			switch (board.colors[i]) {
				case EMPTY:
					line[i % ROWS_CNT] = EMPTY_SQUARE;
					break;
				case WHITE_SIDE:
					char pieceChar = board.pieceChar[pieceCode];
					Log.e("TEST", "pieceChar = " + pieceChar);
					line[i % ROWS_CNT] = String.valueOf(pieceChar);
					break;
				case BLACK_SIDE:
					pieceChar = board.pieceChar[pieceCode];
					Log.e("TEST", "pieceChar = " + pieceChar);
					line[i % ROWS_CNT] = String.valueOf((char) (pieceChar + ('a' - 'A')));
					break;
				default:
					throw new IllegalStateException("Square not EMPTY, WHITE_SIDE or BLACK_SIDE: " + i);
			}
		}

		// filling last line
		fillTheFenLine(sb, line, true);

		// add active color
		if (board.isWhiteToMove()) {
			sb.append(" w");
		} else {
			sb.append(" b");
		}

		// add castling availability
		int whiteCastling = castlingAvailabilityForWhite(board);

		switch (whiteCastling) {
			case NO_CASTLING:
				sb.append(" -");
				break;
			case KINGSIDE_CASTLING:
				sb.append(" K");
				break;
			case QUEENSIDE_CASTLING:
				sb.append(" Q");
				break;
			case BOTH_CASTLING:
				sb.append(" KQ");
				break;
		}
		int blackCastling = castlingAvailabilityForBlack(board);

		switch (blackCastling) {
			case NO_CASTLING:
				sb.append("-");
				break;
			case KINGSIDE_CASTLING:
				sb.append("k");
				break;
			case QUEENSIDE_CASTLING:
				sb.append("q");
				break;
			case BOTH_CASTLING:
				sb.append("kq");
				break;
		}

		Log.d("TEST", "FEN = " + sb.toString());
		return sb.toString();
	}

	private String getEnpassantMoveStr(ChessBoard board) {
		if (board.enPassant != ChessBoard.NOT_SET) {
			for (ChessBoard.Board boardEnum : ChessBoard.Board.values()) {
				if (board.enPassant == boardEnum.ordinal()) {
					return boardEnum.toString().toLowerCase();
				}
			}
			throw new IllegalStateException("En Passant move should match one of file/square");
		} else {
			return null;
		}
	}

	/**
	 * Castling is permissible if and only if all of the following conditions hold (Schiller 2001:19):
	 * 1. The king has not previously moved.
	 * 2. The chosen rook has not previously moved.
	 * 3. There are no pieces between the king and the chosen rook.
	 * 4. The king is not currently in check.
	 * 5. The king does not pass through a square that is under attack by an enemy piece.[2]
	 * 6. The king does not end up in check (true of any legal move).
	 * 7. The king and the chosen rook are on the first rank of the player (rank 1 for White, rank 8 for Black,
	 * in algebraic notation).[3]
	 * Conditions 4 through 6 may be summarized with the more memorable phrase "One cannot castle out of, through,
	 * or into check."
	 * It is a common mistake[4] to think that the requirements for castling are even more stringent than the above.
	 * To clarify:
	 * The chosen rook may be under attack.
	 * The square next to the chosen rook may be under attack when castling queenside, but not when castling kingside.
	 * (Castling kingside would be illegal then, since with only two squares between king and king rook, the king would
	 * end up in check on the attacked square.)
	 *
	 * @return {@code BOTH_CASTLING} if both queenside and kingside, {@code KINGSIDE_CASTLING} if only kingside available,
	 * {@code QUEENSIDE_CASTLING} if only queenside available, {@code NO_CASTLING} if neither is available
	 */
	private int castlingAvailabilityForWhite(ChessBoard board) {
		if (board.whiteCanCastle) {
			if (!board.castlingHistory[WHITE_KINGSIDE_CASTLE] && !board.castlingHistory[WHITE_QUEENSIDE_CASTLE]) { // if non of castling was made
				return BOTH_CASTLING;
			} else if (!board.castlingHistory[WHITE_KINGSIDE_CASTLE]) { // if kingside castling wasn't performed
				return KINGSIDE_CASTLING;
			} else {
				return QUEENSIDE_CASTLING;
			}
		} else {
			return NO_CASTLING;
		}
	}

	private int castlingAvailabilityForBlack(ChessBoard board) {
		if (board.blackCanCastle) {
			if (!board.castlingHistory[BLACK_KINGSIDE_CASTLE] && !board.castlingHistory[BLACK_QUEENSIDE_CASTLE]) { // if non of castling was made
				return BOTH_CASTLING;
			} else if (!board.castlingHistory[BLACK_KINGSIDE_CASTLE]) { // if kingside castling wasn't performed
				return KINGSIDE_CASTLING;
			} else {
				return QUEENSIDE_CASTLING;
			}
		} else {
			return NO_CASTLING;
		}
	}

	private void fillTheFenLine(StringBuilder sb, String[] line, boolean lastLine) {
		String previousPiece = EMPTY_SQUARE;
		String lineResult =Symbol.EMPTY;
		int replacedCnt = 1;
		int stringAddedCnt = 0;
		for (int i = 0; i < line.length; i++) {
			String piece = line[i];
			// after char we need to drop counter to 1
			if (!piece.matches(NUMBER_REGEXP_MATCHER)) {
				replacedCnt = 1;
			}

			if (isInteger(previousPiece) && isInteger(piece)) {
				if (i > 0 && isInteger(line[i - 1])) { // replace 2 chars with increment
					lineResult = lineResult.replace(String.valueOf(replacedCnt), Symbol.EMPTY);
					replacedCnt++;
				}
				if (replacedCnt == 0) {
					int pieceInt = Integer.parseInt(previousPiece);
					lineResult += String.valueOf(pieceInt);
				} else {
					lineResult += String.valueOf(replacedCnt);
				}
				previousPiece = piece;
			} else {
				lineResult += piece;
				sb.append(lineResult);
				lineResult = Symbol.EMPTY;
				stringAddedCnt++;
			}
		}

		if (stringAddedCnt < 8) {// 8 number of ranks(rows)
			sb.append(lineResult);
		}

		if (!lastLine) {
			sb.append("/");
		}
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	void parseFen(String fen, ChessBoard board) {
		String[] boardLines = fen.split(FEN_DIVIDER);

		int i, j, pos = 0;
		for (i = 0; i < 8; i++) {
			String line = boardLines[i];
			if (i == 7) { // if last line
				String[] tmp2 = line.split(Symbol.SPACE);
				line = tmp2[0];
				if (tmp2[1].contains(WHITE_TO_MOVE)) {
					board.setSide(WHITE_SIDE);
					board.setOppositeSide(BLACK_SIDE);
				} else {
					board.setSide(BLACK_SIDE);
					board.setOppositeSide(WHITE_SIDE);
				}
			}

			String[] piecesArray = line.trim().split(POSITION_DIVIDER);
			for (j = 1; j < piecesArray.length; j++) {
				String piece = piecesArray[j];
				if (piece.matches(MovesParser.REGEXP_NUMBERS)) {
					int cnt = Integer.parseInt(piece);
					while (cnt > 0) {
						// Log.d("TEST","pos = " + pos + " piece = " + piece + " line = " + line);
						board.pieces[pos] = ChessBoard.EMPTY;
						board.colors[pos] = ChessBoard.EMPTY;
						cnt--;
						pos++;
					}
				} else {
					//Log.d("TEST","pos = " + pos + " piece = " + piece + " code = " + pieceDataMap.get(piece).pieceCode
					//		+ " color = " + pieceDataMap.get(piece).pieceColor + " line = " + line);
					board.pieces[pos] = pieceDataMap.get(piece).pieceCode;
					board.colors[pos] = pieceDataMap.get(piece).pieceColor;
					pos++;
				}

			}
		}
//		Log.d("TEST", "board = " + board.toString());
	}

	/**
	 * Helper class to simplify mapping for pieces
	 */
	private class PieceData {
		int pieceCode;
		int pieceColor;

		public PieceData(int pieceCode, int pieceColor) {
			this.pieceCode = pieceCode;
			this.pieceColor = pieceColor;
		}
	}

}
