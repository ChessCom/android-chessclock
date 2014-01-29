package com.chess.ui.engine;

import android.text.TextUtils;
import com.chess.statics.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class MovesParser {
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


	public static final char fileLetters[] = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
	public static final String CAPTURE_MARK = "x";

	public static final String REGEXP_CHARS = "[a,b,c,d,e,f,g,h]";
	public static final String REGEXP_NUMBERS = "[0-9]";


	public static final String KINGSIDE_CASTLING = "O-O";
	public static final String KINGSIDE_CASTLING_AND_CHECK = "O-O+";
	public static final String QUEENSIDE_CASTLING = "O-O-O";
	public static final String QUEENSIDE_CASTLING_AND_CHECK = "O-O-O+";
	public static final String MOVE_NUMBERS_PATTERN = "[0-9]{1,4}[.]";

	private static final int PAWN = 0;
	private static final int KNIGHT = 1;
	private static final int BISHOP = 2;
	private static final int ROOK = 3;
	private static final int QUEEN = 4;
	private static final int KING = 5;
	private static final CharSequence COMMENTS_SYMBOL_START = "{";
	private static final CharSequence ALTERNATE_MOVES_SYMBOL_START = Symbol.LEFT_PAR;
	public static final String SPECIAL_SYMBOLS_PATTERN = "[+,!,?,#,x,=,‼,⁇,⁉,⁈,□,∞,⩲,⩱,±,∓,−]";
	public static final String TAG = "MovesParser";

	private final HashMap<String, String> annotationsMapping;

	public MovesParser() {
		annotationsMapping = new HashMap<String, String>();
		fillMapping();
	}

	public static String removeNumbers(String moves) {
		return moves.replaceAll(MOVE_NUMBERS_PATTERN, Symbol.EMPTY)
				.replaceAll("[.]", Symbol.EMPTY);
	}

	int[] parseCoordinate(ChessBoard board, String move) {
		List<Move> validMoves = board.generateLegalMoves();

		int promotion = 0;

		String[] moveTo = new String[2];
		String currentMove = move.trim();

		if (currentMove.equals(KINGSIDE_CASTLING) || currentMove.equals(KINGSIDE_CASTLING_AND_CHECK)) {
			if (board.getSide() == 0) {
				return new int[]{board.whiteKing, board.whiteKingMoveOO[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.blackKing, board.blackKingMoveOO[0], 0, 2};
			}
		}
		if (currentMove.equals(QUEENSIDE_CASTLING) || currentMove.equals(QUEENSIDE_CASTLING_AND_CHECK)) {
			if (board.getSide() == 0) {
				return new int[]{board.whiteKing, board.whiteKingMoveOOO[0], 0, 2};
			} else if (board.getSide() == 1) {
				return new int[]{board.blackKing, board.blackKingMoveOOO[0], 0, 2};
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

		int i = fileToIntPosition(moveTo[0]);
		int j = rankToIntPosition(moveTo[1]);
		int to = j * 8 - i;
		int from = rankToIntPosition(Symbol.EMPTY + currentMove.charAt(1)) * 8 - fileToIntPosition(Symbol.EMPTY + currentMove.charAt(0));

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

	/**
	 * Method will parse {@code String} move and return array of {@code int}
	 * <SAN move descriptor piece moves>   ::= <Piece symbol>[<from file>|<from rank>|<from square>]['x']<to square>
	 * <SAN move descriptor pawn captures> ::= <from file>[<from rank>] 'x' <to square>[<promoted to>]
	 * <SAN move descriptor pawn push>     ::= <to square>[<promoted to>]
	 * <p></p>
	 * {@code currentMove.substring(1, 2)} is a selection to identify Disambiguating move
	 * When two (or more) identical pieces can move to the same square, the moving piece is uniquely identified
	 * by specifying the piece's letter, followed by (in descending order of preference):
	 * the file of departure (if they differ); or
	 * the rank of departure (if the files are the same but the ranks differ); or
	 * both the file and rank (if neither alone is sufficient to identify the piece—which occurs only in rare cases
	 * where one or more pawns have promoted, resulting in a player having three or more identical pieces able to
	 * reach the same square).
	 * For example, with knights on g1 and d2, either of which might move to f3, the move is specified as Ngf3 or Ndf3,
	 * as appropriate. With knights on g5 and g1, the moves are N5f3 or N1f3. As above, an "x" can be inserted
	 * to indicate a capture, for example: N5xf3. Another example: two rooks on d3 and h5, either one of which
	 * may move to d5. If the rook on d3 moves to d5, it is possible to disambiguate with either Rdd5 or R3d5,
	 * but the file takes precedence over the rank, so Rdd5 is correct.
	 * (And likewise if the move is a capture, Rdxd5 is correct.)
	 *
	 * @param board to be used as resource to identify move from positions
	 * @param move  String move representation like <Piece symbol>[<from file>|<from rank>|<from square>]['x']<to square>
	 * @return the {@code int} array of move(s)
	 * @see <a href="http://en.wikipedia.org/wiki/Algebraic_notation_(chess)>en.wikipedia.org/wiki/Algebraic_notation_(chess)</a>
	 */
	int[] parse(ChessBoard board, String move) {
		// possible values ,e4, e4xd5, Rfg1, Rdxd5
//		Log.e(TAG, " move to parse = " + move);
		move = removeNumbers(move);
		List<Move> validMoves = board.generateLegalMoves();

		int promotion = 0;

		String[] moveTo = new String[2];
		String currentMove = move.trim();

		if (currentMove.contains(QUEENSIDE_CASTLING)) {
			if (board.getSide() == ChessBoard.WHITE_SIDE) {
				if (board.whiteKing == ChessBoard.WHITE_QUEENSIDE_KING_DEST) { // for chess960 king can be already on this position
					return new int[]{board.whiteKing, ChessBoard.WHITE_QUEENSIDE_KING_DEST_B1, 0, Move.CASTLING_MASK};
				} else {
					return new int[]{board.whiteKing, ChessBoard.WHITE_QUEENSIDE_KING_DEST, 0, Move.CASTLING_MASK};
				}
			} else if (board.getSide() == ChessBoard.BLACK_SIDE) {
				if (board.blackKing == ChessBoard.BLACK_QUEENSIDE_KING_DEST) { // for chess960 king can be already on this position
					return new int[]{board.blackKing, ChessBoard.BLACK_QUEENSIDE_KING_DEST_B8, 0, Move.CASTLING_MASK};
				} else {
					return new int[]{board.blackKing, ChessBoard.BLACK_QUEENSIDE_KING_DEST, 0, Move.CASTLING_MASK};
				}
			}
		}
		if (currentMove.contains(KINGSIDE_CASTLING)) {
			if (board.getSide() == ChessBoard.WHITE_SIDE) {
				if (board.whiteKing == ChessBoard.WHITE_KINGSIDE_KING_DEST) { // for chess960 king can be already on this position
					return new int[]{board.whiteKing, ChessBoard.WHITE_KINGSIDE_KING_DEST_H1, 0, Move.CASTLING_MASK};
				} else {
					return new int[]{board.whiteKing, ChessBoard.WHITE_KINGSIDE_KING_DEST, 0, Move.CASTLING_MASK};
				}
			} else if (board.getSide() == ChessBoard.BLACK_SIDE) {
				if (board.blackKing == ChessBoard.BLACK_KINGSIDE_KING_DEST) { // for chess960 king can be already on this position
					return new int[]{board.blackKing, ChessBoard.BLACK_KINGSIDE_KING_DEST_H8, 0, Move.CASTLING_MASK};
				} else {
					return new int[]{board.blackKing, ChessBoard.BLACK_KINGSIDE_KING_DEST, 0, Move.CASTLING_MASK};
				}
			}
		}

		// detecting destination square(file and rank)
		int end = currentMove.length() - 1;
		while (end != 0) {
			if (Pattern.matches(REGEXP_NUMBERS, currentMove.substring(end, end + 1))) { // if last symbol in string match number of square
				moveTo[0] = currentMove.substring(end - 1, end); // get letter coordinate
				moveTo[1] = currentMove.substring(end, end + 1); // get number coordinate
				break;
			}
			end--;
		}

		int i = fileToIntPosition(moveTo[0]);
		int j = rankToIntPosition(moveTo[1]);
//		int from = 0;
		int squareTo = j * 8 - i;

		int pieceType = PAWN;
		// we should cut non capital letters
		String movePieceStr = currentMove.replaceAll("[^R,N,B,Q,K,=Q,=R,=B,=N]", Symbol.EMPTY);  // leave only piece letter, except promotions
		movePieceStr = movePieceStr.replaceAll("=Q|=R|=B|=N", Symbol.EMPTY); // remove promotion marks
		if (movePieceStr.contains(WHITE_KNIGHT)) {
			pieceType = KNIGHT;
		} else if (movePieceStr.contains(WHITE_BISHOP)) {
			pieceType = BISHOP;
		} else if (movePieceStr.contains(WHITE_ROOK)) {
			pieceType = ROOK;
		} else if (movePieceStr.contains(WHITE_QUEEN)) {
			pieceType = QUEEN;
		} else if (movePieceStr.contains(WHITE_KING)) {
			pieceType = KING;
		}

		// Rfg1 for example
		// check if we have Disambiguating move
		String fromSquare = Symbol.EMPTY; // can be any file or rank, or even square , e4xd5, Rfg1, Rdd5, Rdxd5, but the file takes precedence over the rank, so Rdd5 is correct.
		String fromFile = Symbol.EMPTY;
		String fromRank = Symbol.EMPTY;
		if (currentMove.length() > 3) { // i.e. 4  // if we have Piece, or capture mark, or from file/rank/square

			fromSquare = currentMove.replaceAll(SPECIAL_SYMBOLS_PATTERN, Symbol.EMPTY); // remove special symbols
			fromSquare = fromSquare.replaceAll("[N,R,K,Q,B]", Symbol.EMPTY); // remove Piece
			String destinationSquare = moveTo[0] + moveTo[1];
			fromSquare = fromSquare.replace(destinationSquare, Symbol.EMPTY); // remove destination square

			if (fromSquare.length() < 2 && fromSquare.length() > 0) {
				if (fromSquare.matches(REGEXP_CHARS)) {
					fromFile = fromSquare;
				} else {
					fromRank = fromSquare;
				}
			}
		}

		// use Disambiguating move, and fill from data
		// if KNIGHT, BISHOP, ROOK OR QUEEN
		if ((pieceType >= KNIGHT && pieceType <= QUEEN)
				&& !fromSquare.contains(CAPTURE_MARK) // looks like always will be true
				&& !currentMove.substring(2, 3).matches(REGEXP_NUMBERS) // it check  ??? TODO investigate what is it for
				) {
			for (int k = 0; k < 64; k++) {
				if (!TextUtils.isEmpty(fromFile)) {
					int fromFileInt = (ChessBoard.getRank(k) + 1) * 8 - fileToIntPosition(fromFile);

					if (board.pieces[fromFileInt] == pieceType && board.colors[fromFileInt] == board.getSide()) { // if we have found that piece and color on board
						return new int[]{fromFileInt, squareTo, promotion};
					}
				}
				if (!TextUtils.isEmpty(fromRank)) {
					int fromRankInt = rankToIntPosition(fromRank) * 8 - (ChessBoard.getFile(k) + 1);  // TODO add logic for promotion parsing
					if (board.pieces[fromRankInt] == pieceType && board.colors[fromRankInt] == board.getSide()) { // if we have found that piece and color on board
						return new int[]{fromRankInt, squareTo, promotion};
					}
				}
			}
		}

		// detect piece that was moved
		for (int squareFrom = 0; squareFrom < ChessBoard.SQUARES_CNT; squareFrom++) {
			// if it's not our piece then skip
			if (board.pieces[squareFrom] != pieceType || board.colors[squareFrom] != board.getSide()) {
				continue;
			}

			for (Move validMove : validMoves) {
//				Log.d("TEST", " pieceType = " + pieceType + " valid move from " + validMove.from
// 															+ " validMove.to = " + validMove.to);
				// if valid move doesn't include our move then skip
				if (validMove.from != squareFrom || validMove.to != squareTo) {
					continue;
				}

				if (pieceType == BISHOP) { // TODO investigate why we have only BISHOP check here???
					if (board.getBoardColor()[squareFrom] == board.getBoardColor()[squareTo]) {
						return new int[]{squareFrom, squareTo, promotion};
					}
				} else if (pieceType == PAWN) {
					// what do we check here???
					if (!fromFile.equals(Symbol.EMPTY)) {
						if (currentMove.contains(CAPTURE_MARK)
								&& 8 - fileToIntPosition(fromFile) != ChessBoard.getFile(squareFrom)) {
							break;
						}
					}

					if (currentMove.contains(WHITE_QUEEN)) {
						promotion = QUEEN;
					} else if (currentMove.contains(WHITE_ROOK)) {
						promotion = ROOK;
					} else if (currentMove.contains(WHITE_BISHOP)) {
						promotion = BISHOP;
					} else if (currentMove.contains(WHITE_KNIGHT)) {
						promotion = KNIGHT;
					}

					return new int[]{squareFrom, squareTo, promotion, validMove.bits};
				} else {
					return new int[]{squareFrom, squareTo, promotion};
				}
			}
		}

//		return new int[]{from, squareTo, promotion};  // means that real move was not found!
		return null;
	}

	int fileToIntPosition(String letter) {
		for (int i = 0; i < fileLetters.length; i++) {
			char symbol = fileLetters[i];
			if (letter.equalsIgnoreCase(String.valueOf(symbol))) {
				return 8 - i;
			}
		}
		throw new IllegalStateException(" fileToIntPosition haven't found a needed int for symbol " + letter);
	}

	int rankToIntPosition(String symbol) {
		return 9 - Integer.parseInt(symbol);
	}

	/**
	 * Use {@link com.chess.ui.engine.ChessBoard#getFile(int x)} to pass correct argument
	 *
	 * @param fileIntNumber to be parsed
	 * @return String value of file number
	 */
	String IntPositionToFile(int fileIntNumber) {
		return String.valueOf(fileLetters[fileIntNumber]);
	}

	String IntPositionToRank(int number) {
		return String.valueOf(8 - number);
	}

	String positionToString(int pos) {
		return ChessBoard.Board.values()[pos].toString().toLowerCase();
	}

	public HashMap<String, String> getCommentsFromMovesList(String movesList) {
		HashMap<String, String> commentsMap = new HashMap<String, String>();

		while (movesList.contains(COMMENTS_SYMBOL_START)) {
			int firstIndex = movesList.indexOf("{");
			int lastIndex = movesList.indexOf("}") + 1;
			String comment;
			if (firstIndex == 0) {
				comment = movesList.substring(firstIndex, lastIndex);
				movesList = movesList.replace(comment, Symbol.EMPTY);

				comment = comment.replace("{", Symbol.EMPTY).replace("}", Symbol.EMPTY);
				commentsMap.put("", comment); // TODO add logic to show comment before first move

			} else {
				String movesBeforeComment = movesList.substring(0, firstIndex - 1);
				int indexOfMoveBeforeComment = movesBeforeComment.lastIndexOf(Symbol.SPACE);
				String moveBeforeComment;
				if (indexOfMoveBeforeComment > 0) {
					moveBeforeComment = movesBeforeComment.substring(indexOfMoveBeforeComment);
				} else {
					moveBeforeComment = movesBeforeComment;
				}

				comment = movesList.substring(firstIndex, lastIndex);

				String keyForSpecialSymbol = " \\" + moveBeforeComment.trim();
				if (annotationsMapping.containsKey(keyForSpecialSymbol)) {
					// if it's a special symbol we need to copy actual move before that symbol and not just last special
					// symbol which separated with space
					movesBeforeComment = movesList.substring(0, firstIndex - 1);
					indexOfMoveBeforeComment = movesBeforeComment.lastIndexOf(Symbol.SPACE);
					String actualMoveBeforeComment = movesBeforeComment.substring(0, indexOfMoveBeforeComment);
					int moveIndex = actualMoveBeforeComment.lastIndexOf(Symbol.SPACE);

					if (moveIndex > 0) {
						actualMoveBeforeComment = actualMoveBeforeComment.substring(moveIndex);
					}
					moveBeforeComment = actualMoveBeforeComment
							.replace(moveBeforeComment, annotationsMapping.get(keyForSpecialSymbol))
							.trim();
				}
				if (moveBeforeComment.contains(Symbol.LEFT_PAR)) {

					movesBeforeComment = movesList.substring(0, firstIndex - 1);
					indexOfMoveBeforeComment = movesBeforeComment.lastIndexOf(Symbol.SPACE);
					String actualMoveBeforeComment = movesBeforeComment.substring(0, indexOfMoveBeforeComment);
					int moveIndex = actualMoveBeforeComment.lastIndexOf(Symbol.SPACE);

					if (moveIndex > 0) {
						actualMoveBeforeComment = actualMoveBeforeComment.substring(moveIndex);
					}
					moveBeforeComment = actualMoveBeforeComment;
				}

				movesList = movesList.replace(comment, Symbol.EMPTY);

				comment = comment.replace("{", Symbol.EMPTY).replace("}", Symbol.EMPTY);
				commentsMap.put(moveBeforeComment, comment);
			}
		}
		return commentsMap;
	}

	public String removeCommentsAndAlternatesFromMovesList(String movesList) {
		while (movesList.contains(COMMENTS_SYMBOL_START)) {
			int firstIndex = movesList.indexOf("{");
			int lastIndex = movesList.indexOf("}") + 1;

			String result = movesList.substring(firstIndex, lastIndex);
			movesList = movesList.replace(result, Symbol.EMPTY);
		}

		if (!movesList.contains(ALTERNATE_MOVES_SYMBOL_START)) {
			return movesList.trim();
		}
		movesList = removeAlternateMoves(movesList);



		return movesList.trim();
	}

	public String removeAlternateMoves(String movesList) {
		int start = movesList.indexOf(Symbol.LEFT_PAR);
		int end = movesList.indexOf(Symbol.RIGHT_PAR);

		// no need to parse anything
		if (end == -1) {
			return movesList;
		}
//		// we need to remove all alternate moves in case ( asdasd ( asdas ( asdas ) asdas ) asdas )
//		// also there are cases like ( asdas (asdasd ) asdas (asdfasd) asdasdas)
////		int firstStartMark = movesList.indexOf("(");
////		int firstEndMark = movesList.indexOf(")");
////		String listToCountStartMarks = movesList.substring(start, end + 1);
//		// count how many startMarks we have here
//		int startCnt = 0;
//		int endCnt = 0;
//		int closePoint = end;
//		for(int k=0; k< movesList.length(); k++){
//			char symbol = movesList.charAt(k);
//			if (symbol == '(') {
//				startCnt++;
//			}
//			if (symbol == ')') {
//				endCnt++;
//				closePoint = k;
//			}
//		}
//
//		String tempList = movesList;
//		while (endCnt < startCnt) {
//			tempList = tempList.substring(end + 1);
//			end = tempList.indexOf(")");
//			String tempStringForStart = tempList.substring(0, end);
//			if (tempStringForStart.contains("(")) { // if there is another open string for alternative moves
//				startCnt++;
//			}
//			endCnt++;
//		}

		String substring;
		String clearMoves = Symbol.EMPTY;
		// if closing parenthesis is closer then opening
		if (end < start || (end > 0 && start == -1)) {
			substring = movesList.substring(end + 1);
		} else {
			clearMoves = movesList.substring(0, start); // remove alternate moves and parse further
			substring = movesList.substring(end + 1);
		}

		String moves = removeAlternateMoves(substring);
		return clearMoves + moves;
	}

	public String replaceSpecialSymbols(String movesList) {
		for (String key : annotationsMapping.keySet()) {
			movesList = movesList.replaceAll(key, annotationsMapping.get(key));
		}
		return movesList;
	}

	private void fillMapping() {
		annotationsMapping.put(" \\$1", "!");
		annotationsMapping.put(" \\$2", "?");
		annotationsMapping.put(" \\$3", "‼");
		annotationsMapping.put(" \\$4", "⁇");
		annotationsMapping.put(" \\$5", "⁉");
		annotationsMapping.put(" \\$6", "⁈");
		annotationsMapping.put(" \\$7", "□");
		annotationsMapping.put(" \\$10", "=");
		annotationsMapping.put(" \\$13", "∞");
		annotationsMapping.put(" \\$14", "⩲");
		annotationsMapping.put(" \\$15", "⩱");
		annotationsMapping.put(" \\$16", "±");
		annotationsMapping.put(" \\$17", "∓");
		annotationsMapping.put(" \\$18", "+−");
		annotationsMapping.put(" \\$19", "−+");
	}


}
