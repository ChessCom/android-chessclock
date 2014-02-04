//
//  Board.java
//  ChessApp
//
//  Created by Peter Hunter on Sun Dec 30 2001.
//  Java version copyright (c) 2001 Peter Hunter. All rights reserved.
//  This code is heavily based on Tom Kerrigan's tscp, for which he
//  owns the copyright, and is used with his permission. All rights are
//  reserved by the owners of the respective copyrights.
package com.chess.ui.engine;

import android.text.TextUtils;
import android.util.Log;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.chess.statics.AppConstants.*;

public class ChessBoard implements BoardFace {

	public static final int WHITE_SIDE = 0;
	public static final int BLACK_SIDE = 1;

	public static final int NO_SIDE = -1;

	// piecesBitmap codes on boardBitmap
	public static final int PAWN = 0;
	public static final int KNIGHT = 1;
	public static final int BISHOP = 2;
	public static final int ROOK = 3;
	public static final int QUEEN = 4;
	public static final int KING = 5;
	public static final int EMPTY = 6;

	public static final String DOUBLE_SPACE = "  ";
	public static final String SYMBOL_SPACE = Symbol.SPACE;
	public static final String SYMBOL_SLASH = "[/]";
	public static final String NUMBERS_PATTERS = "[0-9]";

	public static final String NUMBER_REGEXP_MATCHER = ".*\\d.*";

	/* White Promotion algebraic notations */
	public static final String PROMOTION_W_KNIGHT = "=N";
	public static final String PROMOTION_W_BISHOP = "=B";
	public static final String PROMOTION_W_ROOK = "=R";
	public static final String PROMOTION_W_QUEEN = "=Q";
	/* Black Promotion algebraic notations */
	public static final String PROMOTION_B_KNIGHT = "=n";
	public static final String PROMOTION_B_BISHOP = "=b";
	public static final String PROMOTION_B_ROOK = "=r";
	public static final String PROMOTION_B_QUEEN = "=q";

	public static final char BLACK_PAWN_CHAR = 'p';
	public static final char BLACK_KNIGHT_CHAR = 'n';
	public static final char BLACK_BISHOP_CHAR = 'b';
	public static final char BLACK_ROOK_CHAR = 'r';
	public static final char BLACK_QUEEN_CHAR = 'q';
	public static final char BLACK_KING_CHAR = 'k';

	public static final char WHITE_ROOK_CHAR = 'R';
	public static final char WHITE_KING_CHAR = 'K';
	public static final String TAG = "ChessBoard";
	public static final int SQUARES_CNT = 64;
	private static final int INVALID_POSITION = -1;
	// FEN fields positions
	private static final int ACTIVE_COLOR = 1;
	private static final int CASTLING_AVAILABILITY = 2;
	private static final int EN_PASSANT = 3;
	private static final int HALF_MOVE_CLOCK = 4;
	private static final int FULL_MOVE_NUMBER = 5;

	protected final MovesParser movesParser;


	static final int CAPTURE_PIECE_SCORE = 1000000;

	public final static int HIST_STACK = 2000;

	public static final String MOVE_NUMBER_DOT_SEPARATOR = ". ";
	public static final String MOVE_TAG = "move:";

	protected Long gameId;
	protected boolean justInitialized;

	private boolean chess960;
	private boolean reside;
	private boolean submit;

	private boolean analysis;
	private int side = WHITE_SIDE; // which side is current turn
	private int oppositeSide = BLACK_SIDE; // opponent's side

	int enPassant = NOT_SET;
	private int enPassantPrev = NOT_SET;
	int fifty;
	private int movesCount;
	/**
	 * Ply refers to one turn taken by one of the players. The word is used to clarify what is meant when one might otherwise say "turn".
	 * ply in chess is a half-move
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/Ply_(game_theory)">wiki/Ply_(game_theory)</a>
	 */
	protected int ply;
	private int[][] history = new int[SQUARES_CNT][SQUARES_CNT];
	protected HistoryData[] histDat = new HistoryData[HIST_STACK];
	private HistoryData hintHistoryData;

	public static final String[] whitePieceImageCodes = new String[]{"wp", "wn", "wb", "wr", "wq", "wk"};
	public static final String[] blackPieceImageCodes = new String[]{"bp", "bn", "bb", "br", "bq", "bk"};

	public static enum Board {
		A8, B8, C8, D8, E8, F8, G8, H8,
		A7, B7, C7, D7, E7, F7, G7, H7,
		A6, B6, C6, D6, E6, F6, G6, H6,
		A5, B5, C5, D5, E5, F5, G5, H5,
		A4, B4, C4, D4, E4, F4, G4, H4,
		A3, B3, C3, D3, E3, F3, G3, H3,
		A2, B2, C2, D2, E2, F2, G2, H2,
		A1, B1, C1, D1, E1, F1, G1, H1
	}

	final static int boardColor[] = {
			0, 1, 0, 1, 0, 1, 0, 1,
			1, 0, 1, 0, 1, 0, 1, 0,
			0, 1, 0, 1, 0, 1, 0, 1,
			1, 0, 1, 0, 1, 0, 1, 0,
			0, 1, 0, 1, 0, 1, 0, 1,
			1, 0, 1, 0, 1, 0, 1, 0,
			0, 1, 0, 1, 0, 1, 0, 1,
			1, 0, 1, 0, 1, 0, 1, 0
	};

	// 1 means - white, 0 -> black
	int colors[] = {
			1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
	};

	int[] pieces = {
			3, 1, 2, 4, 5, 2, 1, 3,
			0, 0, 0, 0, 0, 0, 0, 0,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			0, 0, 0, 0, 0, 0, 0, 0,
			3, 1, 2, 4, 5, 2, 1, 3
	};

	final char pieceChars[] = {'P', 'N', 'B', 'R', 'Q', 'K'};
	// PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
	private boolean possibleToSlide[] = {false, false, true, true, true, false};
	// PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
	private int piecesMovesWays[] = {0, 8, 4, 4, 8, 8}; // 8 means all around(8 ways to move), 4 is only four ways to move

	private int piecesOffsets[][] = {
			{0, 0, 0, 0, 0, 0, 0, 0}, // PAWN
			{-21, -19, -12, -8, 8, 12, 19, 21}, // KNIGHT
			{-11, -9, 9, 11, 0, 0, 0, 0}, // BISHOP
			{-10, -1, 1, 10, 0, 0, 0, 0}, // ROOK
			{-11, -10, -9, -1, 1, 9, 10, 11}, // QUEEN
			{-11, -10, -9, -1, 1, 9, 10, 11}  // KING
	};

	int extendedBoard[] = {
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 0  // 10
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 1  // 20
			-1, 0, 1, 2, 3, 4, 5, 6, 7, -1,         // 2  // 30
			-1, 8, 9, 10, 11, 12, 13, 14, 15, -1,   // 3  // 40
			-1, 16, 17, 18, 19, 20, 21, 22, 23, -1, // 4  // 50
			-1, 24, 25, 26, 27, 28, 29, 30, 31, -1, // 5  // 60
			-1, 32, 33, 34, 35, 36, 37, 38, 39, -1, // 6  // 70
			-1, 40, 41, 42, 43, 44, 45, 46, 47, -1, // 7  // 80
			-1, 48, 49, 50, 51, 52, 53, 54, 55, -1, // 8  // 90
			-1, 56, 57, 58, 59, 60, 61, 62, 63, -1, // 9  // 100
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 10 // 110
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1  // 11 // 120
	};

	int positionsForExtendedBoard[] = {
			21, 22, 23, 24, 25, 26, 27, 28,
			31, 32, 33, 34, 35, 36, 37, 38,
			41, 42, 43, 44, 45, 46, 47, 48,
			51, 52, 53, 54, 55, 56, 57, 58,
			61, 62, 63, 64, 65, 66, 67, 68,
			71, 72, 73, 74, 75, 76, 77, 78,
			81, 82, 83, 84, 85, 86, 87, 88,
			91, 92, 93, 94, 95, 96, 97, 98
	};

	public static final int NOT_SET = -1;
	public static final int BLACK_KINGSIDE_CASTLE = 0;
	public static final int BLACK_QUEENSIDE_CASTLE = 1;
	public static final int WHITE_KINGSIDE_CASTLE = 2;
	public static final int WHITE_QUEENSIDE_CASTLE = 3;

	public static final int BLACK_KINGSIDE_KING_DEST = Board.G8.ordinal();
	public static final int BLACK_KINGSIDE_KING_DEST_H8 = Board.H8.ordinal();
	public static final int BLACK_QUEENSIDE_KING_DEST = Board.C8.ordinal();
	public static final int BLACK_QUEENSIDE_KING_DEST_B8 = Board.B8.ordinal();
	public static final int WHITE_KINGSIDE_KING_DEST = Board.G1.ordinal();
	public static final int WHITE_KINGSIDE_KING_DEST_H1 = Board.H1.ordinal();
	public static final int WHITE_QUEENSIDE_KING_DEST = Board.C1.ordinal();
	public static final int WHITE_QUEENSIDE_KING_DEST_B1 = Board.B1.ordinal();

	/**
	 * Array of performed castling
	 * You pass a position and it tells if castling was made for this position
	 */
	boolean[] castlingWasMadeForPosition = {false, false, false, false};
	boolean whiteCanCastle = true;
	boolean blackCanCastle = true;


	int mode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE;

	int BLACK_ROOK_1_INITIAL_POS = Board.A8.ordinal();
	int BLACK_ROOK_2_INITIAL_POS = Board.H8.ordinal();
	int WHITE_ROOK_1_INITIAL_POS = Board.A1.ordinal();
	int WHITE_ROOK_2_INITIAL_POS = Board.H1.ordinal();

	int blackRook1 = BLACK_ROOK_1_INITIAL_POS;
	int blackKing = Board.E8.ordinal(); // initial position for black king
	int blackRook2 = BLACK_ROOK_2_INITIAL_POS;

	int whiteRook1 = WHITE_ROOK_1_INITIAL_POS;
	int whiteKing = Board.E1.ordinal(); // initial position for white king
	int whiteRook2 = WHITE_ROOK_2_INITIAL_POS;

	int[] blackKingMoveOO = new int[]{BLACK_KINGSIDE_KING_DEST};
	int[] blackKingMoveOOO = new int[]{BLACK_QUEENSIDE_KING_DEST};

	int[] whiteKingMoveOO = new int[]{WHITE_KINGSIDE_KING_DEST};
	int[] whiteKingMoveOOO = new int[]{WHITE_QUEENSIDE_KING_DEST};

	protected GameFace gameFace;
	private SoundPlayer soundPlayer;
	private boolean finished;
	final FenHelper fenHelper;

	public ChessBoard(GameFace gameFace) {
		this.gameFace = gameFace;
		if (gameFace != null && gameFace.isAlive()) { // TODO improve logic to create a new ChessBoard only for parsing movesList to FEN
			soundPlayer = gameFace.getSoundPlayer();
		}

		movesParser = new MovesParser();
		fenHelper = new FenHelper();
	}

	@Override
	public void setupCastlingPositions(String fen) {
		// Example rnbqk2r/pppp1ppp/5n2/4P3/1bB2p2/2N5/PPPP2PP/R1BQK1NR w KQkq - 0 2
		String[] tmp = fen.split(SYMBOL_SPACE);

		// set castle masks
		if (tmp.length > 2) { // if we have info about castling like KQkq
			String castling = tmp[2].trim();
			if (!castling.contains(MovesParser.WHITE_KING)) {
				castlingWasMadeForPosition[WHITE_KINGSIDE_CASTLE] = true;
			}
			if (!castling.contains(MovesParser.WHITE_QUEEN)) {
				castlingWasMadeForPosition[WHITE_QUEENSIDE_CASTLE] = true;
			}

			if (!castling.contains(MovesParser.WHITE_KING) && !castling.contains(MovesParser.WHITE_QUEEN)) {
				whiteCanCastle = false;
			}

			if (!castling.contains(MovesParser.BLACK_KING)) {
				castlingWasMadeForPosition[BLACK_KINGSIDE_CASTLE] = true;
			}
			if (!castling.contains(MovesParser.BLACK_QUEEN)) {
				castlingWasMadeForPosition[BLACK_QUEENSIDE_CASTLE] = true;
			}

			if (!castling.contains(MovesParser.BLACK_KING) && !castling.contains(MovesParser.BLACK_QUEEN)) {
				blackCanCastle = false;
			}
		}

		String[] boardLines = tmp[0].split(SYMBOL_SLASH);


		{// parse first black line
			int offset = 0;
			boolean found = false;
			String firstBlackLine = boardLines[0];
			for (int i = 0; i < firstBlackLine.length(); i++) {
				if (firstBlackLine.charAt(i) == BLACK_ROOK_CHAR) {
					if (!found) {
						blackRook1 = i + offset;
						BLACK_ROOK_1_INITIAL_POS = i + offset;
					} else {
						blackRook2 = i + offset;
						BLACK_ROOK_2_INITIAL_POS = i + offset;
					}
					found = true;
				}

				if (firstBlackLine.charAt(i) == BLACK_KING_CHAR) {
					blackKing = i + offset;
					found = true;
				}

				String symbol = firstBlackLine.substring(i, i + 1);
				if (symbol.matches(NUMBERS_PATTERS)) {
					offset += (Integer.parseInt(symbol) - 1);
				}
			}
		}

		{// parse last white rank
			int offset = 56;
			boolean found = false;
			String lastWhiteLine = boardLines[7];
			for (int i = 0; i < lastWhiteLine.length(); i++) {
				if (lastWhiteLine.charAt(i) == WHITE_ROOK_CHAR) {
					if (!found) {
						whiteRook1 = i + offset;
						WHITE_ROOK_1_INITIAL_POS = i + offset;
					} else {
						whiteRook2 = i + offset;
						WHITE_ROOK_2_INITIAL_POS = i + offset;
					}
					found = true;
				}
				if (lastWhiteLine.charAt(i) == WHITE_KING_CHAR) {
					whiteKing = i + offset;
					found = true;
				}
				if (lastWhiteLine.substring(i, i + 1).matches(NUMBERS_PATTERS)) {
					offset += (Integer.parseInt(lastWhiteLine.substring(i, i + 1)) - 1);
				}
			}
		}

		//black  KingSide castling O-O
		if (blackKing < 5) { // if black king was not castled to kingside
			//blackKingMoveOO = new int[]{6,7};
			blackKingMoveOO = new int[8 - (blackKing + 2)];
			for (int i = 0; i < blackKingMoveOO.length; i++) {
				blackKingMoveOO[i] = blackKing + 2 + i;
			}
		} else {
			if (blackKing == 5) { // if king has move to right of his position
				if (blackRook2 == 6) { // if black right rook was moved to left
					blackKingMoveOO = new int[]{6, 7};
				} else {
					blackKingMoveOO = new int[]{7};
				}
			} else {
				blackKingMoveOO = new int[]{7};
			}
		}

		// black  QueenSide castling O-O-O
		if (blackKing > 3) { // if black king was moved to left of black queen
			blackKingMoveOOO = new int[]{0, 1, 2};
		} else {
			if (blackKing == 3) { // if king on queen's position
				if (blackRook1 == 2) { // if rook was moved
					blackKingMoveOOO = new int[]{0, 1, 2};
				} else {
					blackKingMoveOOO = new int[]{0, 1};
				}
			} else if (blackKing == 2) {
				if (blackRook1 == 1) {
					blackKingMoveOOO = new int[]{0, 1};
				} else {
					blackKingMoveOOO = new int[]{0};
				}
			} else if (blackKing == 1) {
				blackKingMoveOOO = new int[]{0};
			}
		}

		// white KingSide castling O-O
		if (whiteKing < 61) {
			//whiteKingMoveOO = new int[]{62,63};
			whiteKingMoveOO = new int[64 - (whiteKing + 2)];
			for (int i = 0; i < 64 - (whiteKing + 2); i++) {
				whiteKingMoveOO[i] = whiteKing + 2 + i;
			}
		} else {
			if (whiteKing == 61) {
				if (whiteRook2 == 62) {
					whiteKingMoveOO = new int[]{62, 63};
				} else {
					whiteKingMoveOO = new int[]{63};
				}
			} else if (whiteKing == 62) {
				whiteKingMoveOO = new int[]{63};
			}
		}
		// white QueenSide castling O-O-O
		if (whiteKing > 59) {       // TODO remove all this hardcode and invent a easy logic to parse possible moves for castling
			whiteKingMoveOOO = new int[]{56, 57, 58};
		} else {
			if (whiteKing == 59) {
				if (whiteRook1 == 58) {
					whiteKingMoveOOO = new int[]{56, 57, 58};
				} else {
					whiteKingMoveOOO = new int[]{56, 57};
				}
			} else if (whiteKing == 58) {
				if (whiteRook1 == 57) {
					whiteKingMoveOOO = new int[]{56, 57};
				} else {
					whiteKingMoveOOO = new int[]{56};
				}
			} else {
				whiteKingMoveOOO = new int[]{56, 58}; // this should be legal, because we parse O-O-O and king can be moved to 58(C1)
			}
		}
	}

	@Override
	public int getColor(int i, int j) {
		return colors[(i << 3) + j];
	}

	@Override
	public boolean isWhiteToMove() {
		//return ply % 2 == 0;
		return side == WHITE_SIDE;
	}

	@Override
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	/**
	 * This call test if user playing under {@code playerSide} is possible to attack opponents king.
	 * We pass side if we want to confirm that we made move that perform check for opponent's king.
	 * If we pass {@code opponentsSide} as an argument then we test if our king is under attack by opponent's piece.
	 *
	 * @param playerSide to check for attack
	 * @return true if playerSide is in check and false otherwise.
	 */
	@Override
	public boolean isPerformCheck(int playerSide) {
		for (int pos = 0; pos < SQUARES_CNT; ++pos) {
			// find king of playerSide color
			if (pieces[pos] == KING && colors[pos] == playerSide) {
				return isUnderAttack(pos, playerSide ^ 1);
			}
		}
		return false;  /* shouldn't get here */  // we get here in Chess Mentor mode, when there can be no king at all
	}

	/**
	 * @param pieceSquare  attacked square
	 * @param attackerSide side of attack
	 * @return returns true if piece on the square {@code pieceSquare} is being attacked by attackerSide and false otherwise.
	 */
	boolean isUnderAttack(int pieceSquare, int attackerSide) {
		int j, n;

		for (int pos = 0; pos < SQUARES_CNT; ++pos) {
			if (colors[pos] == attackerSide) {
				int piece = pieces[pos];
				if (piece == PAWN) {
					if (attackerSide == WHITE_SIDE) {
						if (getFile(pos) != 0 && pos - 9 == pieceSquare)
							return true;
						if (getFile(pos) != 7 && pos - 7 == pieceSquare)
							return true;
					} else {
						if (getFile(pos) != 0 && pos + 7 == pieceSquare)
							return true;
						if (getFile(pos) != 7 && pos + 9 == pieceSquare)
							return true;
					}
				} else if (piece < piecesMovesWays.length) {
					for (j = 0; j < piecesMovesWays[piece]; ++j) {
						for (n = pos; ; ) {
							n = extendedBoard[positionsForExtendedBoard[n] + piecesOffsets[piece][j]];  // wtf is this???
							if (n == -1)
								break;
							if (n == pieceSquare)
								return true;
							if (colors[n] != EMPTY)
								break;
							if (!possibleToSlide[piece])
								break;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Generates pseudo-legal moves for the current position.
	 * It scans the boardBitmap to find friendly piecesBitmap and then determines
	 * what squares they attack. When it finds a piecesBitmap/square
	 * combination, it calls addMoveToStack to put the move on the "move stack."
	 *
	 * @return {@code TreeSet} collection of pseudo-legal moves
	 */
	@Override
	public List<Move> generateLegalMoves() {
		List<Move> movesSet = new ArrayList<Move>();

//		Log.d("TEST"," generateLegalMoves for side " + side);
		for (int pos = 0; pos < SQUARES_CNT; ++pos) {
			addSimpleValidMoves(movesSet, pos, side);
		}

		/* generate castle moves */
		int i;
		if (side == WHITE_SIDE) {
			if (!castlingWasMadeForPosition[2] && whiteCanCastle) {
				for (i = 0; i < whiteKingMoveOO.length; i++)
					addMoveToStack(movesSet, whiteKing, whiteKingMoveOO[i], Move.CASTLING_MASK);
			}
			if (!castlingWasMadeForPosition[3] && whiteCanCastle) {
				for (i = 0; i < whiteKingMoveOOO.length; i++)
					addMoveToStack(movesSet, whiteKing, whiteKingMoveOOO[i], Move.CASTLING_MASK);
			}
		} else {
			if (!castlingWasMadeForPosition[0] && blackCanCastle) {
				for (i = 0; i < blackKingMoveOO.length; i++)
					addMoveToStack(movesSet, blackKing, blackKingMoveOO[i], Move.CASTLING_MASK);
			}
			if (!castlingWasMadeForPosition[1] && blackCanCastle) {
				for (i = 0; i < blackKingMoveOOO.length; i++)
					addMoveToStack(movesSet, blackKing, blackKingMoveOOO[i], Move.CASTLING_MASK);
			}
		}

		/* generate en passant moves */
		addEnPassantMoveToStack(movesSet);
		return movesSet;
	}

	private void addSimpleValidMoves(List<Move> movesSet, int posFrom, int pieceSide) {
		if (colors[posFrom] != pieceSide) {
			return;
		}
		int piece = pieces[posFrom];
		if (piece == PAWN) {
			if (pieceSide == WHITE_SIDE) {
				if (getFile(posFrom) != 0 && colors[posFrom - 9] == BLACK_SIDE) {
					addMoveToStack(movesSet, posFrom, posFrom - 9, 17);
				}
				if (getFile(posFrom) != 7 && colors[posFrom - 7] == BLACK_SIDE) {
					addMoveToStack(movesSet, posFrom, posFrom - 7, 17);
				}
				if (colors[posFrom - 8] == EMPTY) {
					addMoveToStack(movesSet, posFrom, posFrom - 8, 16);
					if (posFrom >= 48 && colors[posFrom - 16] == EMPTY) {
						addMoveToStack(movesSet, posFrom, posFrom - 16, 24);
					}
				}
			} else {
				if (getFile(posFrom) != 0 && colors[posFrom + 7] == WHITE_SIDE) {
					addMoveToStack(movesSet, posFrom, posFrom + 7, 17);
				}
				if (getFile(posFrom) != 7 && colors[posFrom + 9] == WHITE_SIDE) {
					addMoveToStack(movesSet, posFrom, posFrom + 9, 17);
				}
				if (colors[posFrom + 8] == EMPTY) {
					addMoveToStack(movesSet, posFrom, posFrom + 8, 16);
					if (posFrom <= 15 && colors[posFrom + 16] == EMPTY) {
						addMoveToStack(movesSet, posFrom, posFrom + 16, 24);
					}
				}
			}
		} else if (piece < piecesMovesWays.length) {
			// for all other pieces except pawns we calculate possible moves based on directions
			int pieceWaysCnt = piecesMovesWays[piece];
			for (int j = 0; j < pieceWaysCnt; j++) {

				// start infinite loop
				for (int posTo = posFrom; ; ) { // iterate through all possible positions

					int positionInExtendedBoard = positionsForExtendedBoard[posTo];
					int pieceOffset = piecesOffsets[piece][j];
					int index = positionInExtendedBoard + pieceOffset;
//					Log.d("TEST","piece = " + piece + " posFrom = " + posFrom + " posTo = " + posTo
//							+ " positionInExtendedBoard = " + positionInExtendedBoard + " pieceOffset = " + pieceOffset
//							+ " index = " + index);
					posTo = extendedBoard[index];
					if (posTo == INVALID_POSITION) {
						break;
					}

					int pieceColor = colors[posTo];
					if (pieceColor != EMPTY) { // if not empty and can capture, then add
						if (pieceColor == oppositeSide) {
							addMoveToStack(movesSet, posFrom, posTo, 1);
						}
						break;
					}
					addMoveToStack(movesSet, posFrom, posTo, 0);
					if (!possibleToSlide[piece]) {
						break;
					}
				}
			}
		}
	}

//	/** // Not used anywhere now
//	 * Basically it's a copy of generateLegalMoves() that's modified to only generate capture and promote moves.
//	 * It's used by the quiescence search.
//	 *
//	 * @return
//	 */
//	@Override
//	public List<Move> generateCapturesAndPromotes() {
//		List<Move> moves = new ArrayList<Move>();
//
//		for (int i = 0; i < 64; ++i)
//			if (colors[i] == side) {
//				if (pieces[i] == PAWN) {
//					if (side == WHITE_SIDE) {
//						if (getFile(i) != 0 && colors[i - 9] == BLACK_SIDE)
//							addMoveToStack(moves, i, i - 9, 17);
//						if (getFile(i) != 7 && colors[i - 7] == BLACK_SIDE)
//							addMoveToStack(moves, i, i - 7, 17);
//						if (i <= 15 && colors[i - 8] == EMPTY)
//							addMoveToStack(moves, i, i - 8, 16);
//					}
//					if (side == BLACK_SIDE) {
//						if (getFile(i) != 0 && colors[i + 7] == WHITE_SIDE)
//							addMoveToStack(moves, i, i + 7, 17);
//						if (getFile(i) != 7 && colors[i + 9] == WHITE_SIDE)
//							addMoveToStack(moves, i, i + 9, 17);
//						if (i >= 48 && colors[i + 8] == EMPTY)
//							addMoveToStack(moves, i, i + 8, 16);
//					}
//				} else if (pieces[i] < piecesMovesWays.length)
//					for (int j = 0; j < piecesMovesWays[pieces[i]]; ++j)
//						for (int n = i; ; ) {
//							n = extendedBoard[positionsForExtendedBoard[n] + piecesOffsets[pieces[i]][j]];
//							if (n == -1)
//								break;
//							if (colors[n] != EMPTY) {
//								if (colors[n] == oppositeSide)
//									addMoveToStack(moves, i, n, 1);
//								break;
//							}
//							if (!possibleToSlide[pieces[i]])
//								break;
//						}
//			}
//		addEnPassantMoveToStack(moves);
//		return moves;
//	}

	private void addEnPassantMoveToStack(List<Move> movesSet) {
		if (enPassant != NOT_SET) {
			if (side == WHITE_SIDE) {
				if (getFile(enPassant) != 0 && colors[enPassant + 7] == WHITE_SIDE && pieces[enPassant + 7] == PAWN) {
					addMoveToStack(movesSet, enPassant + 7, enPassant, 21);
				}
				if (getFile(enPassant) != 7 && colors[enPassant + 9] == WHITE_SIDE && pieces[enPassant + 9] == PAWN) {
					addMoveToStack(movesSet, enPassant + 9, enPassant, 21);
				}
			} else {
				if (getFile(enPassant) != 0 && colors[enPassant - 9] == BLACK_SIDE && pieces[enPassant - 9] == PAWN) {
					addMoveToStack(movesSet, enPassant - 9, enPassant, 21);
				}
				if (getFile(enPassant) != 7 && colors[enPassant - 7] == BLACK_SIDE && pieces[enPassant - 7] == PAWN) {
					addMoveToStack(movesSet, enPassant - 7, enPassant, 21);
				}
			}
		}
	}

	/**
	 * Puts a move on the move stack, unless it's a
	 * pawn promotion that needs to be handled by addPromotionMove().
	 * It also assigns a score to the move for alpha-beta move
	 * ordering. If the move is a capture, it uses MVV/LVA
	 * (Most Valuable Victim/Least Valuable Attacker). Otherwise,
	 * it uses the move's history heuristic value. Note that
	 * 1,000,000 is added to a capture move's score, so it
	 * always gets ordered above a "normal" move.
	 *
	 * @param moves {@code TreeSet} of moves
	 * @param from  which square move was made
	 * @param to    which square move is targeted
	 * @param bits  move bits
	 */
	void addMoveToStack(List<Move> moves, int from, int to, int bits) {

		// add promotion if it wasn't promoted yet
		if ((bits & 16) != 0) {
			if (side == WHITE_SIDE) {
				if (to <= Board.H8.ordinal()) {
					addPromotionMove(moves, from, to, bits);
					return;
				}
			} else {
				if (to >= Board.A1.ordinal()) {
					addPromotionMove(moves, from, to, bits);
					return;
				}
			}
		}

		Move newMove = new Move(from, to, 0, bits);

		if (colors[to] != EMPTY) { // TODO investigate it... for castling we can't add score
			newMove.setScore(CAPTURE_PIECE_SCORE + (pieces[to] * 10) - pieces[from]);
		} else {
			newMove.setScore(history[from][to]);
		}
		moves.add(newMove);
	}


	/**
	 * Is just like addMoveToStack(), only it puts 4 moves
	 * on the move stack, one for each possible promotion piecesBitmap
	 */
	void addPromotionMove(List<Move> moves, int from, int to, int bits) {
		for (char i = KNIGHT; i <= QUEEN; ++i) {
			Move move = new Move(from, to, i, (bits | 32));
			move.setScore(CAPTURE_PIECE_SCORE + (i * 10));
			moves.add(move);
		}
	}

	/**
	 * Sometimes we have FEN that already contains made move,
	 * and that move can not be parsed, so we return null here
	 *
	 * @param move to make
	 * @return either move that was made, or null if move wasn't recognized
	 */
	@Override
	public Move convertMoveAlgebraic(String move) {
		int[] moveFT = movesParser.parse(this, move);
		if (moveFT != null) {
			return convertMove(moveFT);
		} else {
			return null;
		}
	}

	@Override
	public Move convertMoveCoordinate(String move) {
		int[] moveFT = movesParser.parseCoordinate(this, move);
		return convertMove(moveFT);
	}

	@Override
	public Move convertMove(int[] moveFT) {
		Move move;
		if (moveFT.length == 4) {
			if (moveFT[3] == Move.CASTLING_MASK) {
				move = new Move(moveFT[0], moveFT[1], 0, Move.CASTLING_MASK);
			} else {
				move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
			}
		} else {
			move = new Move(moveFT[0], moveFT[1], 0, 0);
		}
		return move;
	}

	@Override
	public boolean makeMove(String newMove, boolean playSound) {
		final Move move = convertMoveAlgebraic(newMove);
		return move != null && makeMove(move, playSound);
	}

	/**
	 * Makes a move. If the move is illegal, it undoes whatever it did and returns false. Otherwise, it returns true.
	 *
	 * @param move to be made
	 * @return {@code true} if move was made
	 */
	@Override
	public boolean makeMove(Move move) {
		return makeMove(move, true);
	}

	@Override
	public boolean makeHintMove(Move move) {
		hintHistoryData = histDat[ply];
		return makeMove(move);
	}

	/**
	 * Perform move on the board
	 *
	 * @param move      to be parsed
	 * @param playSound tells to play sound or not during the move
	 * @return {@code true} if move was made
	 */
	@Override
	public boolean makeMove(Move move, boolean playSound) {

		/* test to see if a castle move is legal and move the rook
			   (the king is moved with the usual move code later) */
		int castleMaskPosition = NOT_SET;
		if (move.isCastling()) {
			return makeCastling(move, playSound, castleMaskPosition);
		}

		/* back up information so we can take the move back later. */
		backupHistory(move, castleMaskPosition);

		histDat[ply].notation = getMoveSAN();
		ply++;

		// update the castle,
		updateCastling(move, castleMaskPosition);

		checkCastlingForRooks(move);

		// attacked rook
		if (move.to == BLACK_ROOK_1_INITIAL_POS && !castlingWasMadeForPosition[BLACK_QUEENSIDE_CASTLE]) { // q (fen castle)
			castlingWasMadeForPosition[BLACK_QUEENSIDE_CASTLE] = true;
			if (castlingWasMadeForPosition[BLACK_KINGSIDE_CASTLE]) {
				blackCanCastle = false;
			}
		}
		if (move.to == BLACK_ROOK_2_INITIAL_POS && !castlingWasMadeForPosition[BLACK_KINGSIDE_CASTLE]) {// k (fen castle)
			castlingWasMadeForPosition[BLACK_KINGSIDE_CASTLE] = true;
			if (castlingWasMadeForPosition[BLACK_QUEENSIDE_CASTLE]) {
				blackCanCastle = false;
			}
		}
		if (move.to == WHITE_ROOK_1_INITIAL_POS && !castlingWasMadeForPosition[WHITE_QUEENSIDE_CASTLE]) {// Q (fen castle)
			castlingWasMadeForPosition[WHITE_QUEENSIDE_CASTLE] = true;
			if (castlingWasMadeForPosition[WHITE_KINGSIDE_CASTLE]) {
				whiteCanCastle = false;
			}
		}
		if (move.to == WHITE_ROOK_2_INITIAL_POS && !castlingWasMadeForPosition[WHITE_KINGSIDE_CASTLE]) {// K (fen castle)
			castlingWasMadeForPosition[WHITE_KINGSIDE_CASTLE] = true;
			if (castlingWasMadeForPosition[WHITE_QUEENSIDE_CASTLE]) {
				whiteCanCastle = false;
			}
		}

		// update en passant
		if ((move.bits & 8) != 0) {
			if (side == WHITE_SIDE) {
				updateEnPassant(move.to + 8); // enPassant target square will be 8 squares below
			} else {
				updateEnPassant(move.to - 8); // enPassant target square will be 8 squares above
			}
		} else {
			updateEnPassant(NOT_SET);
		}

		// update fifty-move-draw variables
		increaseHalfMoveClock(move);

		// move the piece
		int colorFrom = colors[move.from];
		int pieceTo = pieces[move.to];

		colors[move.to] = side;

		if ((move.bits & 32) != 0) {
			pieces[move.to] = move.promote;
		} else {
			pieces[move.to] = pieces[move.from];
		}

		// mark moved square as empty
		colors[move.from] = EMPTY;
		pieces[move.from] = EMPTY;

		// erase the pawn if this is an en passant move
		if ((move.bits & 4) != 0) {
			if (side == WHITE_SIDE) {
				colors[move.to + 8] = EMPTY;
				pieces[move.to + 8] = EMPTY;
			} else {
				colors[move.to - 8] = EMPTY;
				pieces[move.to - 8] = EMPTY;
			}
		}

		// switch sides and test for legality (if we can capture the other guy's king, it's an illegal position
		// and we need to take the move back)
		switchSides();


		if (playSound && gameFace != null) {

			if (gameFace.isObservingMode()) {
				if (isPerformCheck(side)) {
					soundPlayer.playMoveOpponentCheck();
				} else if (pieceTo != EMPTY) {
					soundPlayer.playCapture();
				} else {
					soundPlayer.playMoveOpponent();
				}

			} else if (gameFace.isUserAbleToMove(side)) {
				boolean userColorWhite = gameFace.isUserColorWhite();
				if ((userColorWhite && colorFrom == 1) || (!userColorWhite && colorFrom == 0)) {
					if (isPerformCheck(side)) {
						soundPlayer.playMoveOpponentCheck();
					} else if (pieceTo != EMPTY) {
						soundPlayer.playCapture();
					} else {
						soundPlayer.playMoveOpponent();
					}
				} else if ((userColorWhite && colorFrom == 0) || (!userColorWhite && colorFrom == 1)) {
					if (isPerformCheck(side)) {
						soundPlayer.playMoveSelfCheck();
					} else if (pieceTo != EMPTY) {
						soundPlayer.playCapture();
					} else {
						soundPlayer.playMoveSelf();
					}
				}
			}
		}

		if (isPerformCheck(oppositeSide)) {
			takeBack();
			return false;
		}
		return true;
	}

	/**
	 * Perform castling if possible
	 *
	 * @param move               to be made as castling
	 * @param playSound          after move
	 * @param castleMaskPosition to apply mask
	 * @return {@code true}  if castling was performed
	 */
	private boolean makeCastling(Move move, boolean playSound, int castleMaskPosition) {
		int from = NOT_SET;
		int to = NOT_SET;

		int[] piece_tmp = pieces.clone();

		if (isPerformCheck(side)) {
			return false;
		}

		int kingToRookDistance = Math.abs(move.from - move.to);
		int minMove = move.to;
		if (move.from < move.to) {
			minMove = move.from;
		}

		for (int kingMove : blackKingMoveOO) { // check black King moves
			if (kingMove == move.to) {
				castleMaskPosition = BLACK_KINGSIDE_CASTLE;
				kingToRookDistance = Math.abs(move.from - blackRook2);
				minMove = blackRook2;
				if (move.from < blackRook2) {
					minMove = move.from;
				}
				break;
			}
		}

		for (int kingMove : blackKingMoveOOO) {
			if (kingMove == move.to) {
				castleMaskPosition = BLACK_QUEENSIDE_CASTLE;
				kingToRookDistance = Math.abs(move.from - blackRook1);
				minMove = blackRook1;
				if (move.from < blackRook1) {
					minMove = move.from;
				}
				break;
			}
		}

		for (int kingMove : whiteKingMoveOO) {
			if (kingMove == move.to) {
				castleMaskPosition = WHITE_KINGSIDE_CASTLE;
				kingToRookDistance = Math.abs(move.from - whiteRook2);
				minMove = whiteRook2;
				if (move.from < whiteRook2) {
					minMove = move.from;
				}
				break;
			}
		}

		for (int kingMove : whiteKingMoveOOO) {
			if (kingMove == move.to) {
				castleMaskPosition = WHITE_QUEENSIDE_CASTLE;
				kingToRookDistance = Math.abs(move.from - whiteRook1);
				minMove = whiteRook1;
				if (move.from < whiteRook1) {
					minMove = move.from;
				}
				break;
			}
		}

		if (castleMaskPosition != NOT_SET && castlingWasMadeForPosition[castleMaskPosition]) {
			return false;
		}

		/*
			Requirements for castling :
			1. The king and the chosen rook are on the player's first rank.[3]
			2. Neither the king nor the chosen rook have previously moved.
			3. There are no pieces between the king and the chosen rook.
			4. The king is not currently in check.
			5. The king does not pass through a square that is attacked by an enemy piece.[4]
			6. The king does not end up in check. (True of any legal move.)
		 */

		// this are final positions that pieces should have after castling:  g1f1, c1d1, g8f8, c8d8
		int kingTo;
		int rookTo;

		int kingDistance;
		int minimalSquare;
		if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
			kingTo = Board.G1.ordinal();
			rookTo = Board.F1.ordinal();

			kingDistance = Math.abs(whiteKing - kingTo);
			minimalSquare = Math.min(whiteKing, kingTo);
			if (checkIfGoesThroughAttackedSquare(kingDistance, minimalSquare)) {
				return false;
			}

			if (colors[rookTo] != EMPTY && pieces[rookTo] != KING && pieces[rookTo] != ROOK) {
				return false;
			}
			if (colors[kingTo] != EMPTY && pieces[kingTo] != KING && pieces[kingTo] != ROOK) {
				return false;
			}
			if (pieces[rookTo] == ROOK && rookTo != whiteRook2) {
				return false;
			}
			if (pieces[kingTo] == ROOK && kingTo != whiteRook2) {
				return false;
			}

			if (kingToRookDistance > 1) {
				while (kingToRookDistance != 0) {
					minMove++;
					if (minMove != whiteRook2 && pieces[minMove] != KING && colors[minMove] != EMPTY) {
						return false;
					}
					kingToRookDistance--;
				}
			}

			from = whiteRook2;
			to = rookTo;
		} else if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
			kingTo = Board.C1.ordinal();
			rookTo = Board.D1.ordinal();

			kingDistance = Math.abs(whiteKing - kingTo);
			minimalSquare = Math.min(whiteKing, kingTo);
			if (checkIfGoesThroughAttackedSquare(kingDistance, minimalSquare)) {
				return false;
			}

			if (colors[kingTo] != EMPTY && pieces[kingTo] != KING && pieces[kingTo] != ROOK) {
				return false;
			}
			if (colors[rookTo] != EMPTY && pieces[rookTo] != KING && pieces[rookTo] != ROOK) {
				return false;
			}
			if (pieces[kingTo] == ROOK && kingTo != whiteRook1) {
				return false;
			}
			if (pieces[rookTo] == ROOK && rookTo != whiteRook1) {
				return false;
			}

			if (kingToRookDistance > 1) {
				while (kingToRookDistance != 0) {
					minMove++;
					if (minMove != whiteRook1 && pieces[minMove] != KING && colors[minMove] != EMPTY) {
						return false;
					}
					kingToRookDistance--;
				}
			}

			from = whiteRook1;
			to = rookTo;
		} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
			kingTo = Board.C8.ordinal();
			rookTo = Board.D8.ordinal();

			kingDistance = Math.abs(blackKing - kingTo);
			minimalSquare = Math.min(blackKing, kingTo);
			if (checkIfGoesThroughAttackedSquare(kingDistance, minimalSquare)) {
				return false;
			}

			if (colors[kingTo] != EMPTY && pieces[kingTo] != KING && pieces[kingTo] != ROOK) {
				return false;
			}
			if (colors[rookTo] != EMPTY && pieces[rookTo] != KING && pieces[rookTo] != ROOK) {
				return false;
			}
			if (pieces[kingTo] == ROOK && kingTo != blackRook1) {
				return false;
			}
			if (pieces[rookTo] == ROOK && rookTo != blackRook1) {
				return false;
			}

			if (kingToRookDistance > 1) {
				while (kingToRookDistance != 0) {
					minMove++;
					if (minMove != blackRook1 && pieces[minMove] != KING && colors[minMove] != EMPTY) {
						return false;
					}
					kingToRookDistance--;
				}
			}

			from = blackRook1;
			to = rookTo;
		} else if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
			kingTo = Board.G8.ordinal();
			rookTo = Board.F8.ordinal();

			kingDistance = Math.abs(blackKing - kingTo);
			minimalSquare = Math.min(blackKing, kingTo);
			if (checkIfGoesThroughAttackedSquare(kingDistance, minimalSquare)) {
				return false;
			}

			if (colors[rookTo] != EMPTY && pieces[rookTo] != KING && pieces[rookTo] != ROOK) {
				return false;
			}
			if (colors[kingTo] != EMPTY && pieces[kingTo] != KING && pieces[kingTo] != ROOK) {
				return false;
			}
			if (pieces[rookTo] == ROOK && blackRook2 != rookTo) {
				return false;
			}
			if (pieces[kingTo] == ROOK && blackRook2 != kingTo) {
				return false;
			}

			if (kingToRookDistance > 1) {
				while (kingToRookDistance != 0) {
					minMove++;
					if (minMove != blackRook2 && pieces[minMove] != KING && colors[minMove] != EMPTY) {
						return false;
					}
					kingToRookDistance--;
				}
			}

			from = blackRook2;
			to = rookTo;
		}

		if (to == NOT_SET || from == NOT_SET) { // TODO investigate real problem
			return false;
		}

		colors[to] = colors[from];
		pieces[to] = pieces[from];
		if (to != from) {
			colors[from] = EMPTY;
			pieces[from] = EMPTY;
		}

		// back up information so we can take the move back later.
		backupHistory(move, castleMaskPosition);
		if (castleMaskPosition == BLACK_KINGSIDE_CASTLE || castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
			histDat[ply].notation = MovesParser.KINGSIDE_CASTLING;
		} else {
			histDat[ply].notation = MovesParser.QUEENSIDE_CASTLING;
		}
		++ply;

		// update the castle, en passant, and fifty-move-draw variables
		updateCastling(move, castleMaskPosition);

		checkCastlingForRooks(move);

		if ((move.bits & 8) != 0) {
			if (side == WHITE_SIDE) {
				updateEnPassant(move.to + 8);
			} else {
				updateEnPassant(move.to - 8);
			}
		} else {
			updateEnPassant(NOT_SET);
		}

		increaseHalfMoveClock(move);

		/* move the piecesBitmap */
		int tmp_to = -1;
		if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
			colors[WHITE_QUEENSIDE_KING_DEST] = side;
			pieces[WHITE_QUEENSIDE_KING_DEST] = piece_tmp[move.from];
			tmp_to = WHITE_QUEENSIDE_KING_DEST;
		} else if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
			colors[WHITE_KINGSIDE_KING_DEST] = side;
			pieces[WHITE_KINGSIDE_KING_DEST] = piece_tmp[move.from];
			tmp_to = WHITE_KINGSIDE_KING_DEST;
		} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
			colors[BLACK_QUEENSIDE_KING_DEST] = side;
			pieces[BLACK_QUEENSIDE_KING_DEST] = piece_tmp[move.from];
			tmp_to = BLACK_QUEENSIDE_KING_DEST;
		} else if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
			colors[BLACK_KINGSIDE_KING_DEST] = side;
			pieces[BLACK_KINGSIDE_KING_DEST] = piece_tmp[move.from];
			tmp_to = BLACK_KINGSIDE_KING_DEST;
		}
		if (pieces[move.from] != ROOK && tmp_to != move.from) {
			colors[move.from] = EMPTY;
			pieces[move.from] = EMPTY;
		}

		// switch sides and test for legality (if we can capture the other guy's king, it's an illegal position
		// and we need to take the move back)
		switchSides();

		if (isPerformCheck(oppositeSide)) {
			takeBack();
			return false;
		}

		if (playSound) {
			soundPlayer.playCastle();
		}

		return true;
	}

	private boolean checkIfGoesThroughAttackedSquare(int kingDistance, int minimalSquare) {
		for (int j = 0; j <= kingDistance; j++) {
			if (isUnderAttack(minimalSquare + j, oppositeSide)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The Halfmove Clock inside an chess position object takes care of enforcing the fifty-move rule.
	 * This counter is reset after captures or pawn moves, and incremented otherwise. Also moves which lose the
	 * castling rights, that is rook- and king moves from their initial squares, including castling itself,
	 * increment the Halfmove Clock. However, those moves are irreversible in the sense to reverse the same
	 * rights - since once a castling right is lost, it is lost forever, as considered in detecting repetitions.
	 *
	 * @param move to be made
	 * @see <a href="http://chessprogramming.wikispaces.com/Halfmove+Clock">http://chessprogramming.wikispaces.com/Halfmove+Clock</a>
	 */
	private void increaseHalfMoveClock(Move move) {
		if ((move.bits & 17) != 0) {
			fifty = 0;
		} else {
			++fifty;
		}
	}

	private void backupHistory(Move move, int castleMaskPosition) {
		histDat[ply] = new HistoryData();
		histDat[ply].move = move;
		histDat[ply].capture = pieces[move.to];
		histDat[ply].enPassant = enPassant;
		histDat[ply].enPassantPrev = enPassantPrev;
		histDat[ply].fifty = fifty;
		histDat[ply].castleMask = castlingWasMadeForPosition.clone();
		histDat[ply].whiteCanCastle = whiteCanCastle;
		histDat[ply].blackCanCastle = blackCanCastle;
		histDat[ply].castleMaskPosition = castleMaskPosition;
	}

	private void updateCastling(Move move, int castleMaskPosition) {
		if (castleMaskPosition != NOT_SET) {
			castlingWasMadeForPosition[castleMaskPosition] = true;
			if (castleMaskPosition == BLACK_KINGSIDE_CASTLE || castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
				blackCanCastle = false;
			} else if (castleMaskPosition == WHITE_KINGSIDE_CASTLE || castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
				whiteCanCastle = false;
			}
		}
		if (pieces[move.from] == KING) {
			if (side == BLACK_SIDE) {
				castlingWasMadeForPosition[BLACK_KINGSIDE_CASTLE] = true;
				castlingWasMadeForPosition[BLACK_QUEENSIDE_CASTLE] = true;
				blackCanCastle = false;
			} else {
				castlingWasMadeForPosition[WHITE_KINGSIDE_CASTLE] = true;
				castlingWasMadeForPosition[WHITE_QUEENSIDE_CASTLE] = true;
				whiteCanCastle = false;
			}
		}
	}

	private void checkCastlingForRooks(Move move) {
		if (pieces[move.from] == ROOK) {
			if (side == BLACK_SIDE) {
				if (move.from == blackRook2) {
					castlingWasMadeForPosition[BLACK_KINGSIDE_CASTLE] = true;
					if (castlingWasMadeForPosition[BLACK_QUEENSIDE_CASTLE]) {
						blackCanCastle = false;
					}
				}
				if (move.from == blackRook1) {
					castlingWasMadeForPosition[BLACK_QUEENSIDE_CASTLE] = true;
					if (castlingWasMadeForPosition[BLACK_KINGSIDE_CASTLE]) {
						blackCanCastle = false;
					}
				}
			} else {
				if (move.from == whiteRook2) {
					castlingWasMadeForPosition[WHITE_KINGSIDE_CASTLE] = true;
					if (castlingWasMadeForPosition[WHITE_QUEENSIDE_CASTLE]) {
						whiteCanCastle = false;
					}
				}
				if (move.from == whiteRook1) {
					castlingWasMadeForPosition[WHITE_QUEENSIDE_CASTLE] = true;
					if (castlingWasMadeForPosition[WHITE_KINGSIDE_CASTLE]) {
						whiteCanCastle = false;
					}
				}
			}
		}
	}


	@Override
	public void restoreBoardAfterHint() {
		histDat[ply] = hintHistoryData;
	}

	/**
	 * takeBack() is very similar to makeMove(), only backwards :)
	 *
	 * @return {@code true} if move was made
	 */
	@Override
	public boolean takeBack() {
		if (ply - 1 < 0) {
			return false;
		}

		switchSides();
		--ply;
		Move move = histDat[ply].move;
		enPassant = histDat[ply].enPassant;
		enPassantPrev = histDat[ply].enPassantPrev;
		fifty = histDat[ply].fifty;
		castlingWasMadeForPosition = histDat[ply].castleMask.clone();
		whiteCanCastle = histDat[ply].whiteCanCastle;
		blackCanCastle = histDat[ply].blackCanCastle;

		if (move.isCastling()) {

			int[] piece_tmp = pieces.clone();

			int castleMaskPosition = NOT_SET;
			for (int aBlackKingMoveOO : blackKingMoveOO) {
				if (aBlackKingMoveOO == move.to) {
					castleMaskPosition = BLACK_KINGSIDE_CASTLE;
				}
			}
			for (int aBlackKingMoveOOO : blackKingMoveOOO) {
				if (aBlackKingMoveOOO == move.to) {
					castleMaskPosition = BLACK_QUEENSIDE_CASTLE;
				}
			}
			for (int aWhiteKingMoveOO : whiteKingMoveOO) {
				if (aWhiteKingMoveOO == move.to) {
					castleMaskPosition = WHITE_KINGSIDE_CASTLE;
				}
			}
			for (int aWhiteKingMoveOOO : whiteKingMoveOOO) {
				if (aWhiteKingMoveOOO == move.to) {
					castleMaskPosition = WHITE_QUEENSIDE_CASTLE;
				}
			}
			int moveTo = move.to;
			int pieceTo = pieces[moveTo];
			if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
				pieceTo = pieces[WHITE_QUEENSIDE_KING_DEST];
				moveTo = WHITE_QUEENSIDE_KING_DEST;
			} else if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
				pieceTo = pieces[WHITE_KINGSIDE_KING_DEST];
				moveTo = WHITE_KINGSIDE_KING_DEST;
			} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
				pieceTo = pieces[BLACK_QUEENSIDE_KING_DEST];
				moveTo = BLACK_QUEENSIDE_KING_DEST;
			} else if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
				pieceTo = pieces[BLACK_KINGSIDE_KING_DEST];
				moveTo = BLACK_KINGSIDE_KING_DEST;
			}
			/* move the piecesBitmap */
			colors[move.from] = side;
			pieces[move.from] = pieceTo;
			if (move.from != moveTo) {
				colors[moveTo] = EMPTY;
				pieces[moveTo] = EMPTY;
			}

			int from = -1;
			if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
				from = whiteRook2;
				moveTo = Board.F1.ordinal();
			} else if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
				from = whiteRook1;
				moveTo = Board.D1.ordinal();
			} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
				from = blackRook1;
				moveTo = Board.D8.ordinal();
			} else if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
				from = blackRook2;
				moveTo = Board.F8.ordinal();
			}
			colors[from] = side;
			pieces[from] = piece_tmp[moveTo];
			if (moveTo != from && pieces[moveTo] != KING) {
				colors[moveTo] = EMPTY;
				pieces[moveTo] = EMPTY;
			}

			return true;
		}

		colors[move.from] = side;
		if ((move.bits & 32) != 0) {
			pieces[move.from] = PAWN;
		} else {
			pieces[move.from] = pieces[move.to];
		}
		if (histDat[ply].capture == EMPTY) {
			colors[move.to] = EMPTY;
			pieces[move.to] = EMPTY;
		} else {
			colors[move.to] = oppositeSide;
			pieces[move.to] = histDat[ply].capture;
		}
		if ((move.bits & 4) != 0) {
			if (side == WHITE_SIDE) {
				colors[move.to + 8] = oppositeSide;
				pieces[move.to + 8] = PAWN;
			} else {
				colors[move.to - 8] = oppositeSide;
				pieces[move.to - 8] = PAWN;
			}
		}

		return true;
	}

	@Override
	public boolean takeNext() {
		if (isCurrentPositionLatest()) {
			if (histDat[ply] == null) {
//				Log.e(TAG, " histDat[ply] == null, ply = " + ply + " histDat.length = " + histDat.length);
				// TODO find real problem
				return false;
			}
			return makeMove(histDat[ply].move);
		}
		return false;
	}

	@Override
	public boolean takeNext(boolean playSound) {
		return isCurrentPositionLatest() && histDat[ply] != null && makeMove(histDat[ply].move, playSound);
	}

	@Override
	public boolean isCurrentPositionLatest() {
		return ply == 0 && movesCount == 0 || ply < movesCount;
	}

/*
	public String getMoveList() {
		String output = Symbol.EMPTY;
		int i;
		for (i = 0; i < ply; i++) {
			Move m = histDat[i].move;
			if (i % 2 == 0) {
				output += Symbol.NEW_STR + (i / 2 + 1) + MOVE_NUMBER_DOT_SEPARATOR;
			}
			output += movesParser.positionToString(m.from);
			output += movesParser.positionToString(m.to);
			output += Symbol.SPACE;
		}
		return output;
	}
*/

	@Override
	public String getMoveListSAN() {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < ply; i++) {
			if (i % 2 == 0) {
				// add move number
				output.append(i / 2 + 1).append(MOVE_NUMBER_DOT_SEPARATOR);
			}
			output.append(histDat[i].notation);
			output.append(Symbol.SPACE);
		}
		return output.toString();
	}

	@Override
	public String[] getFullNotationsArray() {
		String[] movesArray = new String[movesCount];
		for (int i = 0; i < movesCount; i++) {
			movesArray[i] = histDat[i].notation;
		}

		return movesArray;
	}

	@Override
	public String[] getNotationArray() {
		String[] output = new String[ply];
		for (int i = 0; i < ply; i++) {
			output[i] = histDat[i].notation;
		}
		return output;
	}

	public String getMoveSAN() {
		Move move = histDat[ply].move;
		int fromPosition = move.from;
		int targetPos = move.to;
		int pieceCode = pieces[fromPosition];
		String piece = Symbol.EMPTY;
		String capture = Symbol.EMPTY;
		String promotion = Symbol.EMPTY;

		if (pieceCode == KNIGHT) {
			piece = MovesParser.WHITE_KNIGHT;
			// check ambiguous moves. When few pieces can target the same "to" square"
			int[] positions = new int[]{
					targetPos - 17, targetPos - 15, targetPos - 10, targetPos - 6,
					targetPos + 17, targetPos + 15, targetPos + 10, targetPos + 6
			};
			for (int pos : positions) {
				if (pos < 0 || pos > 63 || pos == fromPosition) { // if outside of board or move to itself
					continue;
				}
				if (pieces[pos] == KNIGHT && colors[pos] == side) {
					// add disambiguating notation mark
					if (getFile(pos) == getFile(fromPosition)) {
						piece += movesParser.IntPositionToRank(getRank(fromPosition));
					} else {
						piece += movesParser.IntPositionToFile(getFile(fromPosition));
					}
					break;
				}
			}
		}
		if (pieceCode == BISHOP) {
			piece = MovesParser.WHITE_BISHOP;
		}
		if (pieceCode == ROOK) {
			piece = MovesParser.WHITE_ROOK;

			// check ambiguous moves. When few pieces can target the same "to" square"
			int[] positions = new int[]{
					targetPos - 8, targetPos - 16, targetPos - 24, targetPos - 32, targetPos - 40, targetPos - 48, targetPos - 56,
					targetPos + 8, targetPos + 16, targetPos + 24, targetPos + 32, targetPos + 40, targetPos + 48, targetPos + 56,
					targetPos - 1, targetPos - 2, targetPos - 3, targetPos - 4, targetPos - 5, targetPos - 6, targetPos - 7,
					targetPos + 1, targetPos + 2, targetPos + 3, targetPos + 4, targetPos + 5, targetPos + 6, targetPos + 7
			};

			piece = addDisambiguationNotation(fromPosition, targetPos, piece, positions, ROOK);
		}

		if (pieceCode == QUEEN) {
			piece = MovesParser.WHITE_QUEEN;
		}

		if (pieceCode == KING) {
			piece = MovesParser.WHITE_KING;
		}

		// add capture notation mark
		if (histDat[ply].capture != EMPTY) {
			// Pawns are always referred to by the file they are on when capturing, and nothing when they are just moving.
			// There is never an instance where two pawns can equally capture the same pawn which requires disambiguation.
			if (pieceCode == PAWN) {
				piece = movesParser.IntPositionToFile(getFile(fromPosition)) + MovesParser.CAPTURE_MARK;
			} else {
				capture = MovesParser.CAPTURE_MARK;
			}
		}

		if (move.promote > PAWN) {
			// add promotion notation mark
			int promote = move.promote;
			if (promote == KNIGHT) {
				promotion = PROMOTION_W_KNIGHT;
			}
			if (promote == BISHOP) {
				promotion = PROMOTION_W_BISHOP;
			}
			if (promote == ROOK) {
				promotion = PROMOTION_W_ROOK;
			}
			if (promote == QUEEN) {
				promotion = PROMOTION_W_QUEEN;
			}
		}

		return piece + capture + movesParser.positionToString(targetPos) + promotion;
	}

	/**
	 * We need to check if another piece of the same kind can reach the same {@code targetSquare}.
	 *
	 * @return edited SAN notation for move
	 * @see <a href="http://en.wikipedia.org/wiki/Algebraic_notation_(chess)#Disambiguating_moves>Disambiuating moves</a>
	 */
	private String addDisambiguationNotation(int fromPosition, int targetSquare, String piece, int[] positions, int pieceToCompare) {
		for (int pos : positions) {
			// if outside of board or move to itself, or piece that not need disambiguation note - then skip
			if (pos < 0 || pos > 63 || pos == fromPosition || pieces[pos] != pieceToCompare) {
				continue;
			}

			ArrayList<Move> validMovesForRook = new ArrayList<Move>();
			// i haven't check this logic for all pieces, but i need it now only for rook
			// i think because only rook can move so free on the board, but can't jump other the pieces like Knight
			addSimpleValidMoves(validMovesForRook, pos, side);
			for (Move move : validMovesForRook) {
				// now we can verify this move
				if (move.to == targetSquare) {
					// add disambiguating notation mark - from which file (or rank, or file and rank) it was originally moved
					if (getFile(pos) == getFile(fromPosition)) {
						piece += movesParser.IntPositionToRank(getRank(fromPosition));
					} else {
						piece += movesParser.IntPositionToFile(getFile(fromPosition));
					}
					return piece;
				}
			}
		}
		return piece;
	}

	private String convertMove() {
		Move move = histDat[ply - 1].move;

		String output = Symbol.EMPTY;
		String to = movesParser.positionToString(move.to);
		if (move.isCastling()) {

			int castleMaskPosition = histDat[ply - 1].castleMaskPosition;

			if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
//				output = MovesParser.KINGSIDE_CASTLING;    // TODO uncomment when server fix IllegalMove even for O-O
				if (chess960) {
					to = movesParser.positionToString(blackRook2);
				} else {
					to = Board.G8.name();
				}
			} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
//				output = MovesParser.QUEENSIDE_CASTLING;
				if (chess960) {
					to = movesParser.positionToString(blackRook1);
				} else {
					to = Board.C8.name();
				}
			} else if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
//				output = MovesParser.KINGSIDE_CASTLING;
				if (chess960) {
					to = movesParser.positionToString(whiteRook2);
				} else {
					to = Board.G1.name();
				}
			} else if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
//				output = MovesParser.QUEENSIDE_CASTLING;
				if (chess960) {
					to = movesParser.positionToString(whiteRook1);
				} else {
					to = Board.C1.name();
				}
			}
			output = movesParser.positionToString(move.from) + to;
		} else {
			output = movesParser.positionToString(move.from) + to;
		}

		Log.d(MOVE_TAG, output);
		return output;
	}

	@Override
	public String getLastMoveForDaily() {
		String output = convertMove();
		final Move move = histDat[ply - 1].move;
		switch (move.promote) {
			case ChessBoard.KNIGHT:
				output += (colors[move.from] == WHITE_SIDE ? PROMOTION_W_KNIGHT : PROMOTION_B_KNIGHT);
				break;
			case ChessBoard.BISHOP:
				output += (colors[move.from] == WHITE_SIDE ? PROMOTION_W_BISHOP : PROMOTION_B_BISHOP);
				break;
			case ChessBoard.ROOK:
				output += (colors[move.from] == WHITE_SIDE ? PROMOTION_W_ROOK : PROMOTION_B_ROOK);
				break;
			case ChessBoard.QUEEN:
				output += (colors[move.from] == WHITE_SIDE ? PROMOTION_W_QUEEN : PROMOTION_B_QUEEN);
				break;
			default:
				break;
		}

//		if (move.isCastling()) { // O-O and O-O-O  // TODO uncomment when server fix IllegalMove even for O-O
//			return output;
//		} else {
		return output.toLowerCase(); // need to be lowercase
//		}
	}

	@Override
	public String convertMoveLive() {
		String output = convertMove();
		final Move move = histDat[ply - 1].move;
		switch (move.promote) {
			case ChessBoard.KNIGHT:
				output += BLACK_KNIGHT_CHAR;
				break;
			case ChessBoard.BISHOP:
				output += BLACK_BISHOP_CHAR;
				break;
			case ChessBoard.ROOK:
				output += BLACK_ROOK_CHAR;
				break;
			case ChessBoard.QUEEN:
				output += BLACK_QUEEN_CHAR;
				break;
			default:
				break;
		}
		return output;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("\n8 ");
		for (int pos = 0; pos < SQUARES_CNT; ++pos) {
			int pieceCode = pieces[pos];
//			Log.e("TEST", "pieceCode = " + pieceCode);
			switch (colors[pos]) {
				case EMPTY:
					sb.append(" .");
					break;
				case WHITE_SIDE:
					char pieceChar = pieceChars[pieceCode];
					sb.append(Symbol.SPACE);
					sb.append(pieceChar);
					break;
				case BLACK_SIDE:
					sb.append(Symbol.SPACE);
					pieceChar = pieceChars[pieceCode];
					sb.append((char) (pieceChar + ('a' - 'A')));
					break;
				default:
					throw new IllegalStateException("Square NOT EMPTY, WHITE_SIDE or BLACK_SIDE: " + pos);
			}
			if ((pos + 1) % 8 == 0 && pos != 63) {
				sb.append("\n");
				sb.append(Integer.toString(7 - getRank(pos)));
				sb.append(Symbol.SPACE);
			}
		}
		sb.append("\n\n___a b c d e f g h\n\n");
		return sb.toString();
	}

	/**
	 * Returns the number of times that the current position has been repeated.
	 * Thanks to John Stanback for this clever algorithm.
	 *
	 * @return
	 */
	@Override
	public int getRepetitions() {
		int tempBoard[] = new int[SQUARES_CNT];
		int differentSquaresCnt = 0;  /* count of squares that are different from the current position */
		int repetitionsNumber = 0;  /* number of repetitions */

		/* is a repetition impossible? */
		if (fifty <= 3)
			return 0;

		/* loop through the reversible moves */
		for (int i = ply - 1; i >= ply - fifty - 1; --i) {
			if (i < 0 || i >= histDat.length) {
				return repetitionsNumber;
			}

			if (++tempBoard[histDat[i].move.from] == 0) {
				--differentSquaresCnt;
			} else {
				++differentSquaresCnt;
			}
			if (--tempBoard[histDat[i].move.to] == 0) {
				--differentSquaresCnt;
			} else {
				++differentSquaresCnt;
			}
			if (differentSquaresCnt == 0)
				++repetitionsNumber;
		}

		return repetitionsNumber;
	}

	public static int getFile(int x) {
		return (x & 7);
	}

	public static int getRank(int x) {
		return (x >> 3);
	}

	public static int getPosition(int c, int r) {
		return 8 * r + c;
	}

	/**
	 * Get horizontal coordinate on the board for the given index of column
	 */
	public static int getColumn(int x, boolean reside) {
		if (reside) {
			x = 63 - x;
		}
		return (x & 7);
	}

	/**
	 * Get vertical coordinate on the board for the given index of row
	 */
	public static int getRow(int y, boolean reside) {
		if (reside) {
			y = 63 - y;
		}
		return (y >> 3);  // the same as /8
	}

	public static int getPositionIndex(int col, int row, boolean reside) {
		if (reside) {
			return 63 - (8 * row + col);
		} else {
			return (8 * row + col);
		}
	}

	@Override
	public void setReside(boolean reside) {
		this.reside = reside;
	}

	@Override
	public int getSide() {
		return side;
	}

	@Override
	public void setSide(int side) {
		this.side = side;
	}

	@Override
	public boolean isReside() {
		return reside;
	}

	@Override
	public int getPiece(int position) {
		return pieces[position];
	}

	@Override
	public int getColor(int position) {
		return colors[position];
	}

	@Override
	public int getPly() {
		return ply;
	}

	@Override
	public String generateBaseFen() {
		return fenHelper.generateBaseFen(this);
	}

	@Override
	public String generateFullFen() {
		return fenHelper.generateFullFen(this);
	}

	/**
	 * @param moveList to parse
	 * @return true if {@code moveList} wasn't empty & moves was applied
	 */
	@Override
	public boolean checkAndParseMovesList(String moveList) {
		if (!TextUtils.isEmpty(moveList.trim())) {
			moveList = movesParser.replaceSpecialSymbols(moveList);
			String[] moves = moveList.replaceAll(MovesParser.MOVE_NUMBERS_PATTERN, Symbol.EMPTY)
					.replaceAll("[ ]{2,}", Symbol.SPACE)
					.trim().split(Symbol.SPACE);

			int madeMovesCnt = 0;
			for (String move : moves) {
//				Log.d("TEST", " before move = " + move + " board = " + this.toString());

				boolean moveMade = makeMove(move, false);
				if (!moveMade) {
					setMovesCount(madeMovesCnt);
					return false;
				} else {
					madeMovesCnt++;
				}
//				Log.d("TEST", " after move " + move + " board = " + this.toString());
			}
			setMovesCount(madeMovesCnt);

			return true;
		} else {
			setMovesCount(0);
			return false;
		}
	}

	/**
	 * @return {@code true} if piece is pawn and moved to first or last rank
	 */
	@Override
	public boolean isPromote(int from, int to) {
		return pieces[from] == PAWN
				&& (to < 8 && side == WHITE_SIDE || to > 55 && side == BLACK_SIDE);
	}

	@Override
	public int getMovesCount() {
		return movesCount;
	}

	@Override
	public void setMovesCount(int movesCount) {
		this.movesCount = movesCount;
	}

	@Override
	public void decreaseMovesCount() {
		movesCount--;
	}

	@Override
	public boolean isSubmit() {
		return submit;
	}

	@Override
	public void setSubmit(boolean submit) {
		this.submit = submit;
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public boolean toggleAnalysis() {
		return analysis = !analysis;
	}

	@Override
	public boolean isAnalysis() {
		return analysis;
	}

	@Override
	public void setAnalysis(boolean analysis) {
		this.analysis = analysis;
	}

	@Override
	public HistoryData[] getHistDat() {
		return histDat;
	}

	@Override
	public int[] getBoardColor() {
		return boardColor;
	}

	@Override
	public void setChess960(boolean chess960) {
		this.chess960 = chess960;
	}

	@Override
	public boolean isChess960() {
		return chess960;
	}

	@Override
	public void setOppositeSide(int oppositeSide) {
		this.oppositeSide = oppositeSide;
	}

	@Override
	public boolean isPossibleToMakeMoves() {
		boolean found = false;
		List<Move> validMoves = generateLegalMoves();
		for (Move validMove : validMoves) {   // compute available moves
			if (makeMove(validMove, false)) {  // TODO replace with generatePossibleMove method
				takeBack();
				found = true;
				break;
			}
		}
		return found;
	}

	@Override
	public void setupBoard(String fen) {
		if (!TextUtils.isEmpty(fen)) {
			setupCastlingPositions(fen);

			fenHelper.parseFen(fen, this);

			String[] fields = fen.split(Symbol.SPACE);
			// setup additional fields if exist
			if (fields.length > 1) {
				// Active color. "w" means white moves next, "b" means black. // http://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
				if (!fields[ACTIVE_COLOR].trim().equals(FenHelper.WHITE_TO_MOVE)) {
					setReside(true);
				}

				// set enPassant move
				String enPassant = fields[EN_PASSANT].toUpperCase();
				if (!enPassant.equals(Symbol.MINUS)) {
					updateEnPassant(Board.valueOf(enPassant).ordinal());
				}
			}
		}
	}

	public Long getGameId() {
		return gameId;
	}

	@Override
	public boolean isJustInitialized() {
		return justInitialized;
	}

	@Override
	public void setJustInitialized(boolean justInitialized) {
		this.justInitialized = justInitialized;
	}

	@Override
	public String getLastMoveSAN() {
		return ply == 0 ? null : histDat[ply - 1].notation;
	}

	@Override
	public Move getLastMove() {
		return ply == 0 ? null : histDat[ply - 1].move;
	}

	@Override
	public Move getNextMove() {
		boolean isHistoryPresent = isCurrentPositionLatest() && histDat[ply] != null;
		return isHistoryPresent ? histDat[ply].move : null;
	}

	@Override
	public void switchSides() {
		side ^= 1;
		oppositeSide ^= 1;
	}

	@Override
	public void switchEnPassant() {
		int enPassantTemp = enPassant;
		enPassant = enPassantPrev;
		enPassantPrev = enPassantTemp;
	}

	@Override
	public CopyOnWriteArrayList<Move> generateValidMoves(boolean forceSwitchSide) {

		int[] piecesBackup = null;
		int[] colorsBackup = null;

		if (forceSwitchSide) {
			piecesBackup = pieces.clone();
			colorsBackup = colors.clone();
			switchSides();
			switchEnPassant();
		}
		List<Move> moves = generateLegalMoves();
		CopyOnWriteArrayList<Move> validMoves = new CopyOnWriteArrayList<Move>();

		//String movesStr = new String();
		for (Move move : moves) {
			if (makeMove(move, false)) {
				//movesStr += Symbol.SPACE + move;
				takeBack();
				validMoves.add(move);
			}
		}
		//Log.d("validmoves", "generateLegalMoves and test " + movesStr);

		if (forceSwitchSide) {
			switchSides();
			switchEnPassant();
			pieces = piecesBackup;
			colors = colorsBackup;
		}

//		Log.d("validmoves", "generated validMoves.size() " + validMoves.size());

		return validMoves;
	}

	@Override
	public int[] parseCoordinate(String actualMove) {
		return movesParser.parseCoordinate(this, actualMove);
	}

	private void updateEnPassant(int enPassant) {
		this.enPassantPrev = this.enPassant;
		this.enPassant = enPassant;
	}

	@Override
	public HashMap<String, String> getCommentsFromMovesList(String movesList) {
		return movesParser.getCommentsFromMovesList(movesList);
	}

	@Override
	public String removeCommentsAndAlternatesFromMovesList(String movesList) {
		return movesParser.removeCommentsAndAlternatesFromMovesList(movesList);
	}

	/* Game modes */
	public static boolean isFinishedEchessGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_VIEW_FINISHED_ECHESS;
	}

	public static boolean isComputerVsComputerGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_COMPUTER;
	}

	public static boolean isComputerVsHumanGameMode(BoardFace boardFace) {
		final int mode = boardFace.getMode();
		return mode == GAME_MODE_COMPUTER_VS_PLAYER_WHITE
				|| mode == GAME_MODE_COMPUTER_VS_PLAYER_BLACK;
	}

	public static boolean isHumanVsHumanGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_2_PLAYERS;
	}

	public static boolean isComputerVsHumanWhiteGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_PLAYER_WHITE;
	}

	public static boolean isComputerVsHumanBlackGameMode(BoardFace boardFace) {
		return boardFace.getMode() == GAME_MODE_COMPUTER_VS_PLAYER_BLACK;
	}
}
