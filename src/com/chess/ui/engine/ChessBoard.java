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

import android.util.Log;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;
import org.apache.http.protocol.HTTP;

import java.net.URLEncoder;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChessBoard implements BoardFace {

	public static final int WHITE_SIDE = 0;
	public static final int BLACK_SIDE = 1;
	public static final int WHITE_PIECE = 1;
	public static final int BLACK_PIECE = 0;

	public static final int NO_CASTLING = 0;
	public static final int KINGSIDE_CASTLING = 1;
	public static final int QUEENSIDE_CASTLING = 2;
	public static final int BOTH_CASTLING = 3;

	public static final int NO_SIDE = -1;

	// piecesBitmap codes on boardBitmap
	public static final int PAWN = 0;
	public static final int KNIGHT = 1;
	public static final int BISHOP = 2;
	public static final int ROOK = 3;
	public static final int QUEEN = 4;
	public static final int KING = 5;
	public static final int EMPTY = 6;

	public static final String SYMBOL_SPACE = " ";
	public static final String SYMBOL_SLASH = "[/]";
	public static final String NUMBERS_PATTERS = "[0-9]";

	static final char A1 = 56;
	static final char B1 = 57;
	static final char C1 = 58;
	static final char D1 = 59;
	static final char E1 = 60;
	static final char F1 = 61;
	static final char G1 = 62;
	static final char H1 = 63;
	static final char A8 = 0;
	static final char B8 = 1;
	static final char C8 = 2;
	static final char D8 = 3;
	static final char E8 = 4;
	static final char F8 = 5;
	static final char G8 = 6;
	static final char H8 = 7;

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

	static final int DOUBLED_PAWN_PENALTY = 10;
	static final int ISOLATED_PAWN_PENALTY = 20;
	static final int BACKWARDS_PAWN_PENALTY = 8;
	static final int PASSED_PAWN_BONUS = 20;
	static final int ROOK_SEMI_OPEN_FILE_BONUS = 10;
	static final int ROOK_OPEN_FILE_BONUS = 15;
	static final int ROOK_ON_SEVENTH_BONUS = 20;
	static final int CAPTURE_PIECE_SCORE = 1000000;

	public final static int HIST_STACK = 1000;
	public static final String EQUALS_N = "=N";
	public static final String EQUALS_B = "=B";
	public static final String EQUALS_R = "=R";
	public static final String EQUALS_Q = "=Q";
	public static final String SYMBOL_NEW_STRING = "\n ";
	public static final String MOVE_NUMBER_DOT_SEPARATOR = ". ";
	public static final String G8_STR = "g8";
	public static final String C8_STR = "c8";
	public static final String G1_STR = "g1";
	public static final String C1_STR = "c1";
	public static final String MOVE_TAG = "move:";
	public static final String EQUALS_N_SMALL = "=n";
	public static final String EQUALS_B_SMALL = "=b";
	public static final String EQUALS_R_SMALL = "=r";
	public static final String EQUALS_Q_SMALL = "=q";
	public static final char CHAR_N = 'n';
	public static final char CHAR_B = 'b';
	public static final char CHAR_R = 'r';
	public static final char CHAR_Q = 'q';

	protected Long gameId;
	protected boolean justInitialized;

	private boolean chess960;
	private boolean reside;
	private boolean submit;

	private boolean analysis;
	private int side = WHITE_SIDE; // which side is current turn
	private int oppositeSide = BLACK_SIDE; // opponent's side
	private int rotated = 0;
	private int enPassant = NOT_SET;
	private int enPassantPrev = NOT_SET;
	private int fifty;
	private int movesCount;
	/**
	 * Ply refers to one turn taken by one of the players. The word is used to clarify what is meant when one might otherwise say "turn".
	 * ply in chess is a half-move
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/Ply_(game_theory)">wiki/Ply_(game_theory)</a>
	 */
	protected int ply;
	private int history[][] = new int[64][64];
	protected HistoryData[] histDat = new HistoryData[HIST_STACK];
	private int pawnRank[][] = new int[2][10];
	private int pieceMat[] = new int[2];
	private int pawnMat[] = new int[2];


	private int boardColor[] = {
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
	private int color[] = {
			1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
	};

	protected int[] pieces = {
			3, 1, 2, 4, 5, 2, 1, 3,
			0, 0, 0, 0, 0, 0, 0, 0,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			0, 0, 0, 0, 0, 0, 0, 0,
			3, 1, 2, 4, 5, 2, 1, 3
	};

	final char pieceChar[] = {'P', 'N', 'B', 'R', 'Q', 'K'};

	private boolean slide[] = {false, false, true, true, true, false};

	private int offsets[] = {0, 8, 4, 4, 8, 8};

	private int offset[][] = {
			{0, 0, 0, 0, 0, 0, 0, 0},
			{-21, -19, -12, -8, 8, 12, 19, 21},
			{-11, -9, 9, 11, 0, 0, 0, 0},
			{-10, -1, 1, 10, 0, 0, 0, 0},
			{-11, -10, -9, -1, 1, 9, 10, 11},
			{-11, -10, -9, -1, 1, 9, 10, 11}
	};

	int mailbox[] = {
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, 0, 1, 2, 3, 4, 5, 6, 7, -1,
			-1, 8, 9, 10, 11, 12, 13, 14, 15, -1,
			-1, 16, 17, 18, 19, 20, 21, 22, 23, -1,
			-1, 24, 25, 26, 27, 28, 29, 30, 31, -1,
			-1, 32, 33, 34, 35, 36, 37, 38, 39, -1,
			-1, 40, 41, 42, 43, 44, 45, 46, 47, -1,
			-1, 48, 49, 50, 51, 52, 53, 54, 55, -1,
			-1, 56, 57, 58, 59, 60, 61, 62, 63, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1
	};

	int mailbox64[] = {
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

	/**
	 * Array of performed castling
	 * You pass a position and it tells if castling was made for this position
	 */
	private boolean castlingHistory[] = {false, false, false, false};
	private boolean whiteCanCastle = true;
	private boolean blackCanCastle = true;

	/* the values of the piecesBitmap */
	int pieceValue[] = {
			100, 300, 300, 500, 900, 0
	};

	/**
	 * The "pcsq" arrays are piecesBitmap/square tables. They're values
	 * added to the material value of the piecesBitmap based on the location of the piecesBitmap.
	 */
	int pawnPcsq[] = {
			0, 0, 0, 0, 0, 0, 0, 0,
			5, 10, 15, 20, 20, 15, 10, 5,
			4, 8, 12, 16, 16, 12, 8, 4,
			3, 6, 9, 12, 12, 9, 6, 3,
			2, 4, 6, 8, 8, 6, 4, 2,
			1, 2, 3, -10, -10, 3, 2, 1,
			0, 0, 0, -40, -40, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
	};

	int knightPcsq[] = {
			-10, -10, -10, -10, -10, -10, -10, -10,
			-10, 0, 0, 0, 0, 0, 0, -10,
			-10, 0, 5, 5, 5, 5, 0, -10,
			-10, 0, 5, 10, 10, 5, 0, -10,
			-10, 0, 5, 10, 10, 5, 0, -10,
			-10, 0, 5, 5, 5, 5, 0, -10,
			-10, 0, 0, 0, 0, 0, 0, -10,
			-10, -30, -10, -10, -10, -10, -30, -10
	};

	int bishopPcsq[] = {
			-10, -10, -10, -10, -10, -10, -10, -10,
			-10, 0, 0, 0, 0, 0, 0, -10,
			-10, 0, 5, 5, 5, 5, 0, -10,
			-10, 0, 5, 10, 10, 5, 0, -10,
			-10, 0, 5, 10, 10, 5, 0, -10,
			-10, 0, 5, 5, 5, 5, 0, -10,
			-10, 0, 0, 0, 0, 0, 0, -10,
			-10, -10, -20, -10, -10, -20, -10, -10
	};

	int kingPcsq[] = {
			-40, -40, -40, -40, -40, -40, -40, -40,
			-40, -40, -40, -40, -40, -40, -40, -40,
			-40, -40, -40, -40, -40, -40, -40, -40,
			-40, -40, -40, -40, -40, -40, -40, -40,
			-40, -40, -40, -40, -40, -40, -40, -40,
			-40, -40, -40, -40, -40, -40, -40, -40,
			-20, -20, -20, -20, -20, -20, -20, -20,
			0, 20, 40, -20, 0, -20, 40, 20
	};


	int kingEndgamePcsq[] = {
			0, 10, 20, 30, 30, 20, 10, 0,
			10, 20, 30, 40, 40, 30, 20, 10,
			20, 30, 40, 50, 50, 40, 30, 20,

			30, 40, 50, 60, 60, 50, 40, 30,
			30, 40, 50, 60, 60, 50, 40, 30,
			20, 30, 40, 50, 50, 40, 30, 20,
			10, 20, 30, 40, 40, 30, 20, 10,
			0, 10, 20, 30, 30, 20, 10, 0
	};

	/**
	 * The flip array is used to calculate the piecesBitmap/square
	 * values for BLACK_SIDE piecesBitmap. The piecesBitmap/square value of a
	 * WHITE_SIDE pawn is pawnPcsq[sq] and the value of a BLACK_SIDE
	 * pawn is pawnPcsq[flip[sq]]
	 */
	int flip[] = {
			56, 57, 58, 59, 60, 61, 62, 63,
			48, 49, 50, 51, 52, 53, 54, 55,
			40, 41, 42, 43, 44, 45, 46, 47,
			32, 33, 34, 35, 36, 37, 38, 39,
			24, 25, 26, 27, 28, 29, 30, 31,
			16, 17, 18, 19, 20, 21, 22, 23,
			8, 9, 10, 11, 12, 13, 14, 15,
			0, 1, 2, 3, 4, 5, 6, 7
	};

	private int mode = AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE;

	private int BLACK_ROOK_1_INITIAL_POS = 0;
	private int BLACK_ROOK_2_INITIAL_POS = 7;
	private int WHITE_ROOK_1_INITIAL_POS = 56;
	private int WHITE_ROOK_2_INITIAL_POS = 63;

	private int blackRook1 = BLACK_ROOK_1_INITIAL_POS;
	private int blackKing = 4;
	private int blackRook2 = BLACK_ROOK_2_INITIAL_POS;

	private int whiteRook1 = WHITE_ROOK_1_INITIAL_POS;
	private int whiteKing = 60;
	private int whiteRook2 = WHITE_ROOK_2_INITIAL_POS;

	private int[] blackKingMoveOO = new int[]{6};
	private int[] blackKingMoveOOO = new int[]{2};

	private int[] whiteKingMoveOO = new int[]{62};
	private int[] whiteKingMoveOOO = new int[]{58};

	protected GameFace gameFace;
	private SoundPlayer soundPlayer;
	private boolean finished;

	protected ChessBoard(GameFace gameFace) {
		this.gameFace = gameFace;
		soundPlayer = gameFace.getSoundPlayer();
	}

	@Override
	public int[] buildBoardFromFen(String fen) {
		//rnbqk2r/pppp1ppp/5n2/4P3/1bB2p2/2N5/PPPP2PP/R1BQK1NR
		String[] tmp = fen.split(SYMBOL_SPACE);

		// set castle masks
		if (tmp.length > 2) {
			String castling = tmp[2].trim();
			if (!castling.contains(MoveParser.WHITE_KING)) {
				castlingHistory[WHITE_KINGSIDE_CASTLE] = true;
			}
			if (!castling.contains(MoveParser.WHITE_QUEEN)) {
				castlingHistory[WHITE_QUEENSIDE_CASTLE] = true;
			}

			if (!castling.contains(MoveParser.WHITE_KING) && !castling.contains(MoveParser.WHITE_QUEEN)) {
				whiteCanCastle = false;
			}

			if (!castling.contains(MoveParser.BLACK_KING)) {
				castlingHistory[BLACK_KINGSIDE_CASTLE] = true;
			}
			if (!castling.contains(MoveParser.BLACK_QUEEN)) {
				castlingHistory[BLACK_QUEENSIDE_CASTLE] = true;
			}

			if (!castling.contains(MoveParser.BLACK_KING) && !castling.contains(MoveParser.BLACK_QUEEN)) {
				blackCanCastle = false;
			}

			Log.d(fen, Symbol.EMPTY + castlingHistory[WHITE_KINGSIDE_CASTLE] + castlingHistory[WHITE_QUEENSIDE_CASTLE]
					+ castlingHistory[BLACK_KINGSIDE_CASTLE] + castlingHistory[BLACK_QUEENSIDE_CASTLE]);
		}

		String[] FEN = tmp[0].split(SYMBOL_SLASH);
		int offset = 0, i;
		boolean found = false;
		for (i = 0; i < FEN[0].length(); i++) {
			if (FEN[0].charAt(i) == CHAR_R) {
				if (!found) {
					blackRook1 = i + offset;
					BLACK_ROOK_1_INITIAL_POS = i + offset;
				} else {
					blackRook2 = i + offset;
					BLACK_ROOK_2_INITIAL_POS = i + offset;
				}
				found = true;
			}
			if (FEN[0].charAt(i) == 'k') {
				blackKing = i + offset;
				found = true;
			}
			if (FEN[0].substring(i, i + 1).matches(NUMBERS_PATTERS)) {
				offset += (Integer.parseInt(FEN[0].substring(i, i + 1)) - 1);
			}
		}
		offset = 56;
		found = false;
		for (i = 0; i < FEN[7].length(); i++) {
			if (FEN[7].charAt(i) == 'R') {
				if (!found) {
					whiteRook1 = i + offset;
					WHITE_ROOK_1_INITIAL_POS = i + offset;
				} else {
					whiteRook2 = i + offset;
					WHITE_ROOK_2_INITIAL_POS = i + offset;
				}
				found = true;
			}
			if (FEN[7].charAt(i) == 'K') {
				whiteKing = i + offset;
				found = true;
			}
			if (FEN[7].substring(i, i + 1).matches(NUMBERS_PATTERS)) {
				offset += (Integer.parseInt(FEN[7].substring(i, i + 1)) - 1);
			}
		}

		//black  KingSide castling O-O
		if (blackKing < 5) {
			//blackKingMoveOO = new int[]{6,7};
			blackKingMoveOO = new int[8 - (blackKing + 2)];
			for (i = 0; i < blackKingMoveOO.length; i++)
				blackKingMoveOO[i] = blackKing + 2 + i;
		} else {
			if (blackKing == 5) {
				if (blackRook2 == 6) {
					blackKingMoveOO = new int[]{6, 7};
				} else {
					blackKingMoveOO = new int[]{7};
				}
			} else {
				blackKingMoveOO = new int[]{7};
			}
		}
		// black  QueenSide castling O-O-O
		if (blackKing > 3) {
			blackKingMoveOOO = new int[]{0, 1, 2};
		} else {
			if (blackKing == 3) {
				if (blackRook1 == 2) {
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
			for (i = 0; i < 64 - (whiteKing + 2); i++) {
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
		if (whiteKing > 59) {
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
				whiteKingMoveOOO = new int[]{56};
			}
		}

		Log.d(fen, blackRook1 + " " + blackKing + " " + blackRook2 + " " + whiteRook1 + " " + whiteKing + " " + whiteRook2);
		return new int[]{blackRook1, blackKing, blackRook2, whiteRook1, whiteKing, whiteRook2};
	}

	@Override
	public int getColor(int i, int j) {
		return color[(i << 3) + j];
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
	 * It just scans the boardBitmap to find sideOfKing and calls attack() to see if it's being attacked.
	 *
	 * @param sideOfKing to check for attack
	 * @return true if sideOfKing is in check and false otherwise.
	 */
	@Override
	public boolean inCheck(int sideOfKing) {
		int i;

		for (i = 0; i < 64; ++i) {
			if (pieces[i] == KING && color[i] == sideOfKing) {
				return attack(i, sideOfKing ^ 1);
			}
		}
		return false;  /* shouldn't get here */  // we get here in Chess Mentor mode, when there can be no king at all
	}

	/**
	 * @param attackedSquare attacked square
	 * @param attackerSide   side of attack
	 * @return returns true if square sq is being attacked by attackerSide and false otherwise.
	 */
	boolean attack(int attackedSquare, int attackerSide) {
		int i, j, n;

		for (i = 0; i < 64; ++i) {
			if (color[i] == attackerSide) {
				int piece = pieces[i];
				if (piece == PAWN) {
					if (attackerSide == WHITE_SIDE) {
						if (getColumn(i) != 0 && i - 9 == attackedSquare)
							return true;
						if (getColumn(i) != 7 && i - 7 == attackedSquare)
							return true;
					} else {
						if (getColumn(i) != 0 && i + 7 == attackedSquare)
							return true;
						if (getColumn(i) != 7 && i + 9 == attackedSquare)
							return true;
					}
				} else if (piece < offsets.length) {
					for (j = 0; j < offsets[piece]; ++j) {
						for (n = i; ; ) {
							n = mailbox[mailbox64[n] + offset[piece][j]];
							if (n == -1)
								break;
							if (n == attackedSquare)
								return true;
							if (color[n] != EMPTY)
								break;
							if (!slide[piece])
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
	public TreeSet<Move> generateLegalMoves() {
		TreeSet<Move> movesSet = new TreeSet<Move>();

		for (int i = 0; i < 64; ++i) {
			if (color[i] == side) {
				if (pieces[i] == PAWN) {
					if (side == WHITE_SIDE) {
						if (getColumn(i) != 0 && color[i - 9] == BLACK_SIDE) {
							addMoveToStack(movesSet, i, i - 9, 17);
						}
						if (getColumn(i) != 7 && color[i - 7] == BLACK_SIDE) {
							addMoveToStack(movesSet, i, i - 7, 17);
						}
						if (color[i - 8] == EMPTY) {
							addMoveToStack(movesSet, i, i - 8, 16);
							if (i >= 48 && color[i - 16] == EMPTY) {
								addMoveToStack(movesSet, i, i - 16, 24);
							}
						}
					} else {
						if (getColumn(i) != 0 && color[i + 7] == WHITE_SIDE) {
							addMoveToStack(movesSet, i, i + 7, 17);
						}
						if (getColumn(i) != 7 && color[i + 9] == WHITE_SIDE) {
							addMoveToStack(movesSet, i, i + 9, 17);
						}
						if (color[i + 8] == EMPTY) {
							addMoveToStack(movesSet, i, i + 8, 16);
							if (i <= 15 && color[i + 16] == EMPTY) {
								addMoveToStack(movesSet, i, i + 16, 24);
							}
						}
					}
				} else if (pieces[i] < offsets.length) {
					for (int j = 0; j < offsets[pieces[i]]; ++j) {
						for (int n = i; ; ) {
							n = mailbox[mailbox64[n] + offset[pieces[i]][j]];
							if (n == -1)
								break;
							if (color[n] != EMPTY) {
								if (color[n] == oppositeSide) {
									addMoveToStack(movesSet, i, n, 1);
								}
								break;
							}
							addMoveToStack(movesSet, i, n, 0);
							if (!slide[pieces[i]]) {
								break;
							}
						}
					}
				}
			}
		}

		/* generate castle moves */
		int i;
		if (side == WHITE_SIDE) {
			if (!castlingHistory[2] && whiteCanCastle) {
				for (i = 0; i < whiteKingMoveOO.length; i++)
					addMoveToStack(movesSet, whiteKing, whiteKingMoveOO[i], Move.CASTLING_MASK);
			}
			if (!castlingHistory[3] && whiteCanCastle) {
				for (i = 0; i < whiteKingMoveOOO.length; i++)
					addMoveToStack(movesSet, whiteKing, whiteKingMoveOOO[i], Move.CASTLING_MASK);
			}
		} else {
			if (!castlingHistory[0] && blackCanCastle) {
				for (i = 0; i < blackKingMoveOO.length; i++)
					addMoveToStack(movesSet, blackKing, blackKingMoveOO[i], Move.CASTLING_MASK);
			}
			if (!castlingHistory[1] && blackCanCastle) {
				for (i = 0; i < blackKingMoveOOO.length; i++)
					addMoveToStack(movesSet, blackKing, blackKingMoveOOO[i], Move.CASTLING_MASK);
			}
		}

		/* generate en passant moves */
		addEnPassantMoveToStack(movesSet);
		return movesSet;
	}

	/**
	 * Basically it's a copy of generateLegalMoves() that's modified to only generate capture and promote moves.
	 * It's used by the quiescence search.
	 *
	 * @return
	 */
	@Override
	public TreeSet<Move> generateCapturesAndPromotes() {
		TreeSet<Move> moves = new TreeSet<Move>();

		for (int i = 0; i < 64; ++i)
			if (color[i] == side) {
				if (pieces[i] == PAWN) {
					if (side == WHITE_SIDE) {
						if (getColumn(i) != 0 && color[i - 9] == BLACK_SIDE)
							addMoveToStack(moves, i, i - 9, 17);
						if (getColumn(i) != 7 && color[i - 7] == BLACK_SIDE)
							addMoveToStack(moves, i, i - 7, 17);
						if (i <= 15 && color[i - 8] == EMPTY)
							addMoveToStack(moves, i, i - 8, 16);
					}
					if (side == BLACK_SIDE) {
						if (getColumn(i) != 0 && color[i + 7] == WHITE_SIDE)
							addMoveToStack(moves, i, i + 7, 17);
						if (getColumn(i) != 7 && color[i + 9] == WHITE_SIDE)
							addMoveToStack(moves, i, i + 9, 17);
						if (i >= 48 && color[i + 8] == EMPTY)
							addMoveToStack(moves, i, i + 8, 16);
					}
				} else if (pieces[i] < offsets.length)
					for (int j = 0; j < offsets[pieces[i]]; ++j)
						for (int n = i; ; ) {
							n = mailbox[mailbox64[n] + offset[pieces[i]][j]];
							if (n == -1)
								break;
							if (color[n] != EMPTY) {
								if (color[n] == oppositeSide)
									addMoveToStack(moves, i, n, 1);
								break;
							}
							if (!slide[pieces[i]])
								break;
						}
			}
		addEnPassantMoveToStack(moves);
		return moves;
	}

	private void addEnPassantMoveToStack(TreeSet<Move> movesSet) {
		if (enPassant != NOT_SET) {
			if (side == WHITE_SIDE) {
				if (getColumn(enPassant) != 0 && color[enPassant + 7] == WHITE_SIDE && pieces[enPassant + 7] == PAWN) {
					addMoveToStack(movesSet, enPassant + 7, enPassant, 21);
				}
				if (getColumn(enPassant) != 7 && color[enPassant + 9] == WHITE_SIDE && pieces[enPassant + 9] == PAWN) {
					addMoveToStack(movesSet, enPassant + 9, enPassant, 21);
				}
			} else {
				if (getColumn(enPassant) != 0 && color[enPassant - 9] == BLACK_SIDE && pieces[enPassant - 9] == PAWN) {
					addMoveToStack(movesSet, enPassant - 9, enPassant, 21);
				}
				if (getColumn(enPassant) != 7 && color[enPassant - 7] == BLACK_SIDE && pieces[enPassant - 7] == PAWN) {
					addMoveToStack(movesSet, enPassant - 7, enPassant, 21);
				}
			}
		}
	}

	private String getEnpassantMoveStr() {
		if (enPassant != NOT_SET) {
			for (Board board : Board.values()) {
				if (enPassant == board.ordinal()) {
				   return board.toString().toLowerCase();
				}
			}
			throw new IllegalStateException("En Passant move should match one of file/square");
		} else {
			return null;
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
	void addMoveToStack(TreeSet<Move> moves, int from, int to, int bits) {
		if ((bits & 16) != 0) {
			if (side == WHITE_SIDE) {
				if (to <= H8) {
					addPromotionMove(moves, from, to, bits);
					return;
				}
			} else {
				if (to >= A1) {
					addPromotionMove(moves, from, to, bits);
					return;
				}
			}
		}

		Move newMove = new Move(from, to, 0, bits);

		if (color[to] != EMPTY) {
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
	void addPromotionMove(TreeSet<Move> moves, int from, int to, int bits) {
		for (char i = KNIGHT; i <= QUEEN; ++i) {
			Move move = new Move(from, to, i, (bits | 32));
			move.setScore(CAPTURE_PIECE_SCORE + (i * 10));
			moves.add(move);
		}
	}

	@Override
	public Move convertMoveAlgebraic(String move) {
		int[] moveFT = MoveParser.parse(this, move);
		Move move1 = convertMove(moveFT);
		Log.d("TEST", " new moveStr = " + move + " new move = " + move1.toString());

		return move1;
	}

	@Override
	public Move convertMoveCoordinate(String move) {
		int[] moveFT = MoveParser.parseCoordinate(this, move);
		return convertMove(moveFT);
	}

	@Override
	public Move convertMove(int[] moveFT) {
		Move move;
		if (moveFT.length == 4) {
			if (moveFT[3] == 2)
				move = new Move(moveFT[0], moveFT[1], 0, Move.CASTLING_MASK);
			else
				move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
		} else {
			move = new Move(moveFT[0], moveFT[1], 0, 0);
		}
		return move;
	}

	@Override
	public boolean makeMove(String newMove, boolean playSound) {
		final Move move = convertMoveAlgebraic(newMove);
		return makeMove(move, playSound);
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
		++ply;

		// update the castle,
		updateCastling(move, castleMaskPosition);

		checkCastlingForRooks(move);

		// attacked rook
		if (move.to == BLACK_ROOK_1_INITIAL_POS && !castlingHistory[BLACK_QUEENSIDE_CASTLE]) { // q (fen castle)
			castlingHistory[BLACK_QUEENSIDE_CASTLE] = true;
			if (castlingHistory[BLACK_KINGSIDE_CASTLE]) {
				blackCanCastle = false;
			}
		}
		if (move.to == BLACK_ROOK_2_INITIAL_POS && !castlingHistory[BLACK_KINGSIDE_CASTLE]) {// k (fen castle)
			castlingHistory[BLACK_KINGSIDE_CASTLE] = true;
			if (castlingHistory[BLACK_QUEENSIDE_CASTLE]) {
				blackCanCastle = false;
			}
		}
		if (move.to == WHITE_ROOK_1_INITIAL_POS && !castlingHistory[WHITE_QUEENSIDE_CASTLE]) {// Q (fen castle)
			castlingHistory[WHITE_QUEENSIDE_CASTLE] = true;
			if (castlingHistory[WHITE_KINGSIDE_CASTLE]) {
				whiteCanCastle = false;
			}
		}
		if (move.to == WHITE_ROOK_2_INITIAL_POS && !castlingHistory[WHITE_KINGSIDE_CASTLE]) {// K (fen castle)
			castlingHistory[WHITE_KINGSIDE_CASTLE] = true;
			if (castlingHistory[WHITE_QUEENSIDE_CASTLE]) {
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
		int colorFrom = color[move.from];
		int pieceTo = pieces[move.to];

		color[move.to] = side;

		if ((move.bits & 32) != 0) {
			pieces[move.to] = move.promote;
		} else {
			pieces[move.to] = pieces[move.from];
		}

		color[move.from] = EMPTY;
		pieces[move.from] = EMPTY;

		// erase the pawn if this is an en passant move
		if ((move.bits & 4) != 0) {
			if (side == WHITE_SIDE) {
				color[move.to + 8] = EMPTY;
				pieces[move.to + 8] = EMPTY;
			} else {
				color[move.to - 8] = EMPTY;
				pieces[move.to - 8] = EMPTY;
			}
		}

		// switch sides and test for legality (if we can capture the other guy's king, it's an illegal position
		// and we need to take the move back)
		switchSides();

		Boolean userColorWhite = gameFace.isUserColorWhite();
		if (playSound && userColorWhite != null) {
			if ((userColorWhite && colorFrom == 1) || (!userColorWhite && colorFrom == 0)) {
				if (inCheck(side)) {
					soundPlayer.playMoveOpponentCheck();
				} else if (pieceTo != EMPTY) {
					soundPlayer.playCapture();
				} else {
					soundPlayer.playMoveOpponent();
				}
			} else if ((userColorWhite && colorFrom == 0) || (!userColorWhite && colorFrom == 1)) {
				if (inCheck(side)) {
					soundPlayer.playMoveSelfCheck();
				} else if (pieceTo != EMPTY) {
					soundPlayer.playCapture();
				} else {
					soundPlayer.playMoveSelf();
				}
			}
		}

		if (inCheck(oppositeSide)) {
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

		if (inCheck(side))
			return false;

		int kingToRookDistance = Math.abs(move.from - move.to);
		int minMove = move.to;
		if (move.from < move.to) {
			minMove = move.from;
		}

		int i = 0;
		while (i < blackKingMoveOO.length) { // check black King moves
			if (blackKingMoveOO[i] == move.to) {
				castleMaskPosition = BLACK_KINGSIDE_CASTLE;
				kingToRookDistance = Math.abs(move.from - blackRook2);
				minMove = blackRook2;
				if (move.from < blackRook2) {
					minMove = move.from;
				}
				break;
			}
			i++;
		}
		i = 0;
		while (i < blackKingMoveOOO.length) {
			if (blackKingMoveOOO[i] == move.to) {
				castleMaskPosition = BLACK_QUEENSIDE_CASTLE;
				kingToRookDistance = Math.abs(move.from - blackRook1);
				minMove = blackRook1;
				if (move.from < blackRook1) {
					minMove = move.from;
				}
				break;
			}
			i++;
		}
		i = 0;
		while (i < whiteKingMoveOO.length) {
			if (whiteKingMoveOO[i] == move.to) {
				castleMaskPosition = WHITE_KINGSIDE_CASTLE;
				kingToRookDistance = Math.abs(move.from - whiteRook2);
				minMove = whiteRook2;
				if (move.from < whiteRook2)
					minMove = move.from;
				break;
			}
			i++;
		}
		i = 0;
		while (i < whiteKingMoveOOO.length) {
			if (whiteKingMoveOOO[i] == move.to) {
				castleMaskPosition = WHITE_QUEENSIDE_CASTLE;
				kingToRookDistance = Math.abs(move.from - whiteRook1);
				minMove = whiteRook1;
				if (move.from < whiteRook1)
					minMove = move.from;
				break;
			}
			i++;
		}

		if (castlingHistory[castleMaskPosition]) {
			return false;
		}

		int kingDistance;
		if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
			kingDistance = Math.abs(whiteKing - G1);
			int minimalSquare = Math.min(whiteKing, G1);
			for (int j = 0; j <= kingDistance; j++) {
				if (attack(minimalSquare + j, oppositeSide)) {
					return false;
				}
			}

			if (color[F1] != EMPTY && pieces[F1] != KING && pieces[F1] != ROOK) {
				return false;
			}
			if (color[G1] != EMPTY && pieces[G1] != KING && pieces[G1] != ROOK) {
				return false;
			}
			if (pieces[F1] == ROOK && F1 != whiteRook2) {
				return false;
			}
			if (pieces[G1] == ROOK && G1 != whiteRook2) {
				return false;
			}

			if (kingToRookDistance > 1) {
				while (kingToRookDistance != 0) {
					minMove++;
					if (minMove != whiteRook2 && pieces[minMove] != KING && color[minMove] != EMPTY) {
						return false;
					}
					kingToRookDistance--;
				}
			}

			from = whiteRook2;
			to = F1;
		} else if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {

			kingDistance = Math.abs(whiteKing - C1);
			int minimalSquare = Math.min(whiteKing, C1);
			for (int j = 0; j <= kingDistance; j++) {
				if (attack(minimalSquare + j, oppositeSide)) {
					return false;
				}
			}

			if (color[C1] != EMPTY && pieces[C1] != KING && pieces[C1] != ROOK) {
				return false;
			}
			if (color[D1] != EMPTY && pieces[D1] != KING && pieces[D1] != ROOK) {
				return false;
			}
			if (pieces[C1] == ROOK && C1 != whiteRook1) {
				return false;
			}
			if (pieces[D1] == ROOK && D1 != whiteRook1) {
				return false;
			}

			if (kingToRookDistance > 1) {
				while (kingToRookDistance != 0) {
					minMove++;
					if (minMove != whiteRook1 && pieces[minMove] != KING && color[minMove] != EMPTY) {
						return false;
					}
					kingToRookDistance--;
				}
			}

			from = whiteRook1;
			to = D1;
		} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {

			kingDistance = Math.abs(blackKing - C8);
			int minimalSquare = Math.min(blackKing, C8);
			for (int j = 0; j <= kingDistance; j++) {
				if (attack(minimalSquare + j, oppositeSide)) {
					return false;
				}
			}

			if (color[C8] != EMPTY && pieces[C8] != KING && pieces[C8] != ROOK) {
				return false;
			}
			if (color[D8] != EMPTY && pieces[D8] != KING && pieces[D8] != ROOK) {
				return false;
			}
			if (pieces[C8] == ROOK && C8 != blackRook1) {
				return false;
			}
			if (pieces[D8] == ROOK && D8 != blackRook1) {
				return false;
			}

			if (kingToRookDistance > 1) {
				while (kingToRookDistance != 0) {
					minMove++;
					if (minMove != blackRook1 && pieces[minMove] != KING && color[minMove] != EMPTY) {
						return false;
					}
					kingToRookDistance--;
				}
			}

			from = blackRook1;
			to = D8;
		} else if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {

			kingDistance = Math.abs(blackKing - G8);
			int minimalSquare = Math.min(blackKing, G8);
			for (int j = 0; j <= kingDistance; j++) {
				if (attack(minimalSquare + j, oppositeSide)) {
					return false;
				}
			}

			if (color[F8] != EMPTY && pieces[F8] != KING && pieces[F8] != ROOK) {
				return false;
			}
			if (color[G8] != EMPTY && pieces[G8] != KING && pieces[G8] != ROOK) {
				return false;
			}
			if (pieces[F8] == ROOK && blackRook2 != F8) {
				return false;
			}
			if (pieces[G8] == ROOK && blackRook2 != G8) {
				return false;
			}

			if (kingToRookDistance > 1) {
				while (kingToRookDistance != 0) {
					minMove++;
					if (minMove != blackRook2 && pieces[minMove] != KING && color[minMove] != EMPTY) {
						return false;
					}
					kingToRookDistance--;
				}
			}

			from = blackRook2;
			to = F8;
		}

		color[to] = color[from];
		pieces[to] = pieces[from];
		if (to != from) {
			color[from] = EMPTY;
			pieces[from] = EMPTY;
		}

		// back up information so we can take the move back later.
		backupHistory(move, castleMaskPosition);
		if (castleMaskPosition == BLACK_KINGSIDE_CASTLE || castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
			histDat[ply].notation = MoveParser.KINGSIDE_CASTLING;
		} else {
			histDat[ply].notation = MoveParser.QUEENSIDE_CASTLING;
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
			updateEnPassant(-1);
		}

		increaseHalfMoveClock(move);

		/* move the piecesBitmap */
		int tmp_to = -1;
		if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
			color[58] = side;
			pieces[58] = piece_tmp[move.from];
			tmp_to = 58;
		} else if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
			color[62] = side;
			pieces[62] = piece_tmp[move.from];
			tmp_to = 62;
		} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
			color[2] = side;
			pieces[2] = piece_tmp[move.from];
			tmp_to = 2;
		} else if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
			color[6] = side;
			pieces[6] = piece_tmp[move.from];
			tmp_to = 6;
		}
		if (pieces[move.from] != ROOK && tmp_to != move.from) {
			color[move.from] = EMPTY;
			pieces[move.from] = EMPTY;
		}

		// switch sides and test for legality (if we can capture the other guy's king, it's an illegal position
		// and we need to take the move back)
		switchSides();

		if (inCheck(oppositeSide)) {
			takeBack();
			return false;
		}

		if (playSound) {
			soundPlayer.playCastle();
		}

		return true;
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
		histDat[ply].castleMask = castlingHistory.clone();
		histDat[ply].whiteCanCastle = whiteCanCastle;
		histDat[ply].blackCanCastle = blackCanCastle;
		histDat[ply].castleMaskPosition = castleMaskPosition;
	}


	private void updateCastling(Move move, int castleMaskPosition) {
		if (castleMaskPosition != NOT_SET) {
			castlingHistory[castleMaskPosition] = true;
			if (castleMaskPosition == BLACK_KINGSIDE_CASTLE || castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
				blackCanCastle = false;
			} else if (castleMaskPosition == WHITE_KINGSIDE_CASTLE || castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
				whiteCanCastle = false;
			}
		}
		if (pieces[move.from] == KING) {
			if (side == BLACK_SIDE) {
				castlingHistory[BLACK_KINGSIDE_CASTLE] = true;
				castlingHistory[BLACK_QUEENSIDE_CASTLE] = true;
				blackCanCastle = false;
			} else {
				castlingHistory[WHITE_KINGSIDE_CASTLE] = true;
				castlingHistory[WHITE_QUEENSIDE_CASTLE] = true;
				whiteCanCastle = false;
			}
		}
	}

	private void checkCastlingForRooks(Move move) {
		if (pieces[move.from] == ROOK) {
			if (side == BLACK_SIDE) {
				if (move.from == blackRook2) {
					castlingHistory[BLACK_KINGSIDE_CASTLE] = true;
					if (castlingHistory[BLACK_QUEENSIDE_CASTLE]) {
						blackCanCastle = false;
					}
				}
				if (move.from == blackRook1) {
					castlingHistory[BLACK_QUEENSIDE_CASTLE] = true;
					if (castlingHistory[BLACK_KINGSIDE_CASTLE]) {
						blackCanCastle = false;
					}
				}
			} else {
				if (move.from == whiteRook2) {
					castlingHistory[WHITE_KINGSIDE_CASTLE] = true;
					if (castlingHistory[WHITE_QUEENSIDE_CASTLE]) {
						whiteCanCastle = false;
					}
				}
				if (move.from == whiteRook1) {
					castlingHistory[WHITE_QUEENSIDE_CASTLE] = true;
					if (castlingHistory[WHITE_KINGSIDE_CASTLE]) {
						whiteCanCastle = false;
					}
				}
			}
		}
	}

	/**
	 * takeBack() is very similar to makeMove(), only backwards :)
	 */
	@Override
	public void takeBack() {
		if (ply - 1 < 0)
			return;

		switchSides();
		--ply;
		Move move = histDat[ply].move;
		enPassant = histDat[ply].enPassant;
		enPassantPrev = histDat[ply].enPassantPrev;
		fifty = histDat[ply].fifty;
		castlingHistory = histDat[ply].castleMask.clone();
		whiteCanCastle = histDat[ply].whiteCanCastle;
		blackCanCastle = histDat[ply].blackCanCastle;

		if ((move.isCastling())) {

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
				pieceTo = pieces[58];
				moveTo = 58;
			} else if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
				pieceTo = pieces[62];
				moveTo = 62;
			} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
				pieceTo = pieces[2];
				moveTo = 2;
			} else if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
				pieceTo = pieces[6];
				moveTo = 6;
			}
			/* move the piecesBitmap */
			color[move.from] = side;
			pieces[move.from] = pieceTo;
			if (move.from != moveTo) {
				color[moveTo] = EMPTY;
				pieces[moveTo] = EMPTY;
			}

			int from = -1;
			if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
				from = whiteRook2;
				moveTo = F1;
			} else if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
				from = whiteRook1;
				moveTo = D1;
			} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
				from = blackRook1;
				moveTo = D8;
			} else if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
				from = blackRook2;
				moveTo = F8;
			}
			color[from] = side;
			pieces[from] = piece_tmp[moveTo];
			if (moveTo != from && pieces[moveTo] != KING) {
				color[moveTo] = EMPTY;
				pieces[moveTo] = EMPTY;
			}

			return;
		}


		color[move.from] = side;
		if ((move.bits & 32) != 0) {
			pieces[move.from] = PAWN;
		} else {
			pieces[move.from] = pieces[move.to];
		}
		if (histDat[ply].capture == EMPTY) {
			color[move.to] = EMPTY;
			pieces[move.to] = EMPTY;
		} else {
			color[move.to] = oppositeSide;
			pieces[move.to] = histDat[ply].capture;
		}
		if ((move.bits & 4) != 0) {
			if (side == WHITE_SIDE) {
				color[move.to + 8] = oppositeSide;
				pieces[move.to + 8] = PAWN;
			} else {
				color[move.to - 8] = oppositeSide;
				pieces[move.to - 8] = PAWN;
			}
		}
	}

	@Override
	public void takeNext() {
		if (ply + 1 <= movesCount) {
			if (histDat[ply] == null) // TODO find real problem
				return;

			makeMove(histDat[ply].move);
		}
	}

	/*public String getMoveList() {
		String output = StaticData.EMPTY;
		int i;
		for (i = 0; i < ply; i++) {
			Move m = histDat[i].move;
			if (i % 2 == 0)
				output += "\n" + (i / 2 + 1) + ". ";
			output += MoveParser.positionToString(m.from);
			output += MoveParser.positionToString(m.to);
			output += " ";
		}
		return output;
	}*/

	@Override
	public String getMoveListSAN() {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < ply; i++) {
			if (i % 2 == 0) { //
				output.append(SYMBOL_NEW_STRING);
				// add move number
				output.append(i / 2 + 1).append(MOVE_NUMBER_DOT_SEPARATOR);
			}
			output.append(histDat[i].notation);
			output.append(Symbol.SPACE);
		}
		return output.toString();
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
		int piece = pieces[move.from];
		String f = Symbol.EMPTY;
		String capture = Symbol.EMPTY;
		String promotion = Symbol.EMPTY;
		if (piece == 1) {
			f = MoveParser.WHITE_KNIGHT;
			//ambiguous
			int[] positions = new int[]{
					move.to - 17, move.to - 15, move.to - 10, move.to - 6,
					move.to + 17, move.to + 15, move.to + 10, move.to + 6
			};
			int i;
			for (i = 0; i < 8; i++) {
				int pos = positions[i];
				if (pos < 0 || pos > 63 || pos == move.from)
					continue;
				if (pieces[pos] == 1 && color[pos] == side) {
					if (getColumn(pos) == getColumn(move.from))
						f += MoveParser.BNToNum(getRow(move.from));
					else
						f += MoveParser.BNToLetter(getColumn(move.from));
					break;
				}
			}
		}
		if (piece == 2)
			f = MoveParser.WHITE_BISHOP;
		if (piece == 3) {
			f = MoveParser.WHITE_ROOK;
			//ambiguous
			int[] positions = new int[]{
					move.to - 8, move.to - 16, move.to - 24, move.to - 32, move.to - 40, move.to - 48, move.to - 56,
					move.to + 8, move.to + 16, move.to + 24, move.to + 32, move.to + 40, move.to + 48, move.to + 56,
					move.to - 1, move.to - 2, move.to - 3, move.to - 4, move.to - 5, move.to - 6, move.to - 7,
					move.to + 1, move.to + 2, move.to + 3, move.to + 4, move.to + 5, move.to + 6, move.to + 7
			};
			int i;
			for (i = 0; i < 28; i++) {
				int pos = positions[i];
				if (pos < 0 || pos > 63 || pos == move.from)
					continue;
				if (pieces[pos] == 3 && color[pos] == side) {
					if (getColumn(pos) == getColumn(move.from))
						f += MoveParser.BNToNum(getRow(move.from));
					else
						f += MoveParser.BNToLetter(getColumn(move.from));
					break;
				}
			}
		}
		if (piece == 4)
			f = MoveParser.WHITE_QUEEN;
		if (piece == 5)
			f = MoveParser.WHITE_KING;

		if (histDat[ply].capture != 6) {
			if (piece == 0) {
				f = MoveParser.BNToLetter(getColumn(move.from));
			}
			capture = "x";
		}

		if (move.promote > 0) {
			int pr = move.promote;
			if (pr == 1)
				promotion = EQUALS_N;
			if (pr == 2)
				promotion = EQUALS_B;
			if (pr == 3)
				promotion = EQUALS_R;
			if (pr == 4)
				promotion = EQUALS_Q;
		}

		return f + capture + MoveParser.positionToString(move.to) + promotion;
	}

	private String convertMove() {
		Move move = new Move(0, 0, 0, 0);
		try {
			move = histDat[ply - 1].move;
		} catch (ArrayIndexOutOfBoundsException e) {
//			StringBuilder result = new StringBuilder(); // never queried
//			if (histDat.length > 0) {
//				result.append(histDat[0]);
//				for (int i = 1; i < histDat.length; i++) {
//					if (histDat[i] != null) {
//						result.append(", ");
//						result.append(histDat[i].move);
//					}
//				}
//			}
		}


		String output = Symbol.EMPTY;
		try {
			String to = MoveParser.positionToString(move.to);
			if (move.isCastling()) {

				int castleMaskPosition = histDat[ply - 1].castleMaskPosition;

				if (castleMaskPosition == BLACK_KINGSIDE_CASTLE) {
					if (chess960) {
						to = MoveParser.positionToString(blackRook2);
					} else {
						to = G8_STR;
					}
				} else if (castleMaskPosition == BLACK_QUEENSIDE_CASTLE) {
					if (chess960) {
						to = MoveParser.positionToString(blackRook1);
					} else {
						to = C8_STR;
					}
				} else if (castleMaskPosition == WHITE_KINGSIDE_CASTLE) {
					if (chess960) {
						to = MoveParser.positionToString(whiteRook2);
					} else {
						to = G1_STR;
					}
				} else if (castleMaskPosition == WHITE_QUEENSIDE_CASTLE) {
					if (chess960) {
						to = MoveParser.positionToString(whiteRook1);
					} else {
						to = C1_STR;
					}
				}
			}
			output = URLEncoder.encode(MoveParser.positionToString(move.from) + to, HTTP.UTF_8);
		} catch (Exception ignored) {
		}
		Log.d(MOVE_TAG, output);
		return output;
	}

	@Override
	public String convertMoveEchess() {
		String output = convertMove();
		final Move move = histDat[ply - 1].move;
		switch (move.promote) {
			case ChessBoard.KNIGHT:
				output += (color[move.from] == 0 ? EQUALS_N : EQUALS_N_SMALL);
				break;
			case ChessBoard.BISHOP:
				output += (color[move.from] == 0 ? EQUALS_B : EQUALS_B_SMALL);
				break;
			case ChessBoard.ROOK:
				output += (color[move.from] == 0 ? EQUALS_R : EQUALS_R_SMALL);
				break;
			case ChessBoard.QUEEN:
				output += (color[move.from] == 0 ? EQUALS_Q : EQUALS_Q_SMALL);
				break;
			default:
				break;
		}
		return output;
	}

	@Override
	public String convertMoveLive() {
		String output = convertMove();
		final Move move = histDat[ply - 1].move;
		switch (move.promote) {
			case ChessBoard.KNIGHT:
				output += CHAR_N;
				break;
			case ChessBoard.BISHOP:
				output += CHAR_B;
				break;
			case ChessBoard.ROOK:
				output += CHAR_R;
				break;
			case ChessBoard.QUEEN:
				output += CHAR_Q;
				break;
			default:
				break;
		}
		return output;
	}

	public String toString() {
		int i;

		StringBuilder sb = new StringBuilder("\n8 ");
		for (i = 0; i < 64; ++i) {
			switch (color[i]) {
				case EMPTY:
					sb.append(" .");
					break;
				case WHITE_SIDE:
					sb.append(" ");
					sb.append(pieceChar[pieces[i]]);
					break;
				case BLACK_SIDE:
					sb.append(" ");
					sb.append((char) (pieceChar[pieces[i]] + ('a' - 'A')));
					break;
				default:
					throw new IllegalStateException("Square not EMPTY, WHITE_SIDE or BLACK_SIDE: " + i);
			}
			if ((i + 1) % 8 == 0 && i != 63) {
				sb.append("\n");
				sb.append(Integer.toString(7 - getRow(i)));
				sb.append(" ");
			}
		}
		sb.append("\n\n   a b c d e f g h\n\n");
		return sb.toString();
	}

	private static final String EMPTY_SQUARE = "1";

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
	@Override
	public String generateFullFen() {
		String fen = generateBaseFen();
		StringBuilder sb = new StringBuilder(fen);

		// add  En passant target square
		String enPassantStr = "-";
		if (enPassant != NOT_SET) {
			enPassantStr = getEnpassantMoveStr();
		}

		sb.append(" ").append(enPassantStr);

		// add halfmove or ply
		sb.append(" ").append(fifty);

		// add fullmove
		sb.append(" ").append(ply);

		Log.d("TEST", "FULL FEN = " + sb.toString());
		return sb.toString();
	}

	@Override
	public String generateBaseFen() {
		StringBuilder sb = new StringBuilder();
		String[] line = new String[8];

		for (int i = 0; i < 64; i++) {
			if (i > 0 && i % 8 == 0) { // if end of board line
				fillTheFenLine(sb, line, false);
			}
			switch (color[i]) {
				case EMPTY:
					line[i % 8] = EMPTY_SQUARE;
					break;
				case WHITE_SIDE:
					line[i % 8] = String.valueOf(pieceChar[pieces[i]]);
					break;
				case BLACK_SIDE:
					line[i % 8] = String.valueOf((char) (pieceChar[pieces[i]] + ('a' - 'A')));
					break;
				default:
					throw new IllegalStateException("Square not EMPTY, WHITE_SIDE or BLACK_SIDE: " + i);
			}
		}

		// filling last line
		fillTheFenLine(sb, line, true);

		// add active color
		if (isWhiteToMove()) {
			sb.append(" w");
		} else {
			sb.append(" b");
		}

		// add castling availability
		int whiteCastling = castlingAvailabilityForWhite();

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
		int blackCastling = castlingAvailabilityForBlack();

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
	private int castlingAvailabilityForWhite() {
		if (whiteCanCastle) {
			if (!castlingHistory[WHITE_KINGSIDE_CASTLE] && !castlingHistory[WHITE_QUEENSIDE_CASTLE]) { // if non of castling was made
				return BOTH_CASTLING;
			} else if (!castlingHistory[WHITE_KINGSIDE_CASTLE]) { // if kingside castling wasn't performed
				return KINGSIDE_CASTLING;
			} else {
				return QUEENSIDE_CASTLING;
			}
		} else {
			return NO_CASTLING;
		}
	}

	private int castlingAvailabilityForBlack() {
		if (blackCanCastle) {
			if (!castlingHistory[BLACK_KINGSIDE_CASTLE] && !castlingHistory[BLACK_QUEENSIDE_CASTLE]) { // if non of castling was made
				return BOTH_CASTLING;
			} else if (!castlingHistory[BLACK_KINGSIDE_CASTLE]) { // if kingside castling wasn't performed
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
		String lineResult = "";
		int replacedCnt = 1;
		int stringAddedCnt = 0;
		for (int i = 0; i < line.length; i++) {
			String piece = line[i];
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
				replacedCnt = 0;
				sb.append(lineResult);
				lineResult = Symbol.EMPTY;
				stringAddedCnt++;
			}
		}

		if (stringAddedCnt < 8) {
			sb.append(lineResult)/*.append("/")*/;
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

	/**
	 * Returns the number of times that the current position has been repeated. Thanks to John Stanback for this clever algorithm.
	 *
	 * @return
	 */
	@Override
	public int reps() {
		int tempBoard[] = new int[64];
		int differentSquaresCnt = 0;  /* count of squares that are different from
				   the current position */
		int repetitionsNumber = 0;  /* number of repetitions */

		/* is a repetition impossible? */
		if (fifty <= 3)
			return 0;

		/* loop through the reversible moves */
		for (int i = ply - 1; i >= ply - fifty - 1; --i) {

			if (i < 0 || i >= histDat.length) return repetitionsNumber;

			if (++tempBoard[histDat[i].move.from] == 0)
				--differentSquaresCnt;
			else
				++differentSquaresCnt;
			if (--tempBoard[histDat[i].move.to] == 0)
				--differentSquaresCnt;
			else
				++differentSquaresCnt;
			if (differentSquaresCnt == 0)
				++repetitionsNumber;
		}

		return repetitionsNumber;
	}

	@Override
	public int eval() {
		int score[] = new int[2];  /* each side's score */

		/* this is the first pass: set up pawnRank, pieceMat, and pawnMat. */
		int i;
		for (i = 0; i < 10; ++i) {
			pawnRank[WHITE_SIDE][i] = 0;
			pawnRank[BLACK_SIDE][i] = 7;
		}
		pieceMat[WHITE_SIDE] = 0;
		pieceMat[BLACK_SIDE] = 0;
		pawnMat[WHITE_SIDE] = 0;
		pawnMat[BLACK_SIDE] = 0;

		for (i = 0; i < 64; i++) {
			if (color[i] == EMPTY)
				continue;
			if (pieces[i] == PAWN) {
				pawnMat[color[i]] += pieceValue[PAWN];
				int f = getColumn(i) + 1;  /* add 1 because of the extra file in the array */
				if (color[i] == WHITE_SIDE) {
					if (pawnRank[WHITE_SIDE][f] < getRow(i))
						pawnRank[WHITE_SIDE][f] = getRow(i);
				} else {
					if (pawnRank[BLACK_SIDE][f] > getRow(i))
						pawnRank[BLACK_SIDE][f] = getRow(i);
				}
			} else {
				try {
					pieceMat[color[i]] += pieceValue[pieces[i]];
				} catch (Exception e) {
					Log.e("I!!!!!!!!:", Symbol.EMPTY + i + Symbol.SPACE + e.toString());
				}
			}
		}

		/* this is the second pass: evaluate each piecesBitmap */
		score[WHITE_SIDE] = pieceMat[WHITE_SIDE] + pawnMat[WHITE_SIDE];
		score[BLACK_SIDE] = pieceMat[BLACK_SIDE] + pawnMat[BLACK_SIDE];
		for (i = 0; i < 64; ++i) {
			if (color[i] == EMPTY)
				continue;
			if (color[i] == WHITE_SIDE) {
				switch (pieces[i]) {
					case PAWN:
						score[WHITE_SIDE] += evalLightPawn(i);
						break;
					case KNIGHT:
						score[WHITE_SIDE] += knightPcsq[i];
						break;
					case BISHOP:
						score[WHITE_SIDE] += bishopPcsq[i];
						break;
					case ROOK:
						if (pawnRank[WHITE_SIDE][getColumn(i) + 1] == 0) {
							if (pawnRank[BLACK_SIDE][getColumn(i) + 1] == 7)
								score[WHITE_SIDE] += ROOK_OPEN_FILE_BONUS;
							else
								score[WHITE_SIDE] += ROOK_SEMI_OPEN_FILE_BONUS;
						}
						if (getRow(i) == 1)
							score[WHITE_SIDE] += ROOK_ON_SEVENTH_BONUS;
						break;
					case KING:
						if (pieceMat[BLACK_SIDE] <= 1200)
							score[WHITE_SIDE] += kingEndgamePcsq[i];
						else
							score[WHITE_SIDE] += evalLightKing(i);
						break;
				}
			} else {
				switch (pieces[i]) {
					case PAWN:
						score[BLACK_SIDE] += evalDarkPawn(i);
						break;
					case KNIGHT:
						score[BLACK_SIDE] += knightPcsq[flip[i]];
						break;
					case BISHOP:
						score[BLACK_SIDE] += bishopPcsq[flip[i]];
						break;
					case ROOK:
						if (pawnRank[BLACK_SIDE][getColumn(i) + 1] == 7) {
							if (pawnRank[WHITE_SIDE][getColumn(i) + 1] == 0)
								score[BLACK_SIDE] += ROOK_OPEN_FILE_BONUS;
							else
								score[BLACK_SIDE] += ROOK_SEMI_OPEN_FILE_BONUS;
						}
						if (getRow(i) == 6)
							score[BLACK_SIDE] += ROOK_ON_SEVENTH_BONUS;
						break;
					case KING:
						if (pieceMat[WHITE_SIDE] <= 1200)
							score[BLACK_SIDE] += kingEndgamePcsq[flip[i]];
						else
							score[BLACK_SIDE] += evalDarkKing(i);
						break;
				}
			}
		}

		/* the score[] array is set, now return the score relative
			   to the side to move */
		if (side == WHITE_SIDE) {
			return score[WHITE_SIDE] - score[BLACK_SIDE];
		}
		return score[BLACK_SIDE] - score[WHITE_SIDE];
	}

	int evalLightPawn(int sq) {
		int r = 0; /* return value */
		int f = getColumn(sq) + 1; /* pawn's file */

		r += pawnPcsq[sq];

		/* if there's a pawn behind this one, it's doubled */
		if (pawnRank[WHITE_SIDE][f] > getRow(sq))
			r -= DOUBLED_PAWN_PENALTY;

		/* if there aren't any friendly pawns on either side of
			   this one, it's isolated */
		if ((pawnRank[WHITE_SIDE][f - 1] == 0) &&
				(pawnRank[WHITE_SIDE][f + 1] == 0))
			r -= ISOLATED_PAWN_PENALTY;

			/* if it's not isolated, it might be backwards */
		else if ((pawnRank[WHITE_SIDE][f - 1] < getRow(sq)) &&
				(pawnRank[WHITE_SIDE][f + 1] < getRow(sq)))
			r -= BACKWARDS_PAWN_PENALTY;

		/* add a bonus if the pawn is passed */
		if ((pawnRank[BLACK_SIDE][f - 1] >= getRow(sq)) &&
				(pawnRank[BLACK_SIDE][f] >= getRow(sq)) &&
				(pawnRank[BLACK_SIDE][f + 1] >= getRow(sq)))
			r += (7 - getRow(sq)) * PASSED_PAWN_BONUS;

		return r;
	}

	int evalDarkPawn(int sq) {
		int r = 0;  /* the value to return */
		int f = getColumn(sq) + 1;  /* the pawn's file */

		r += pawnPcsq[flip[sq]];

		/* if there's a pawn behind this one, it's doubled */
		if (pawnRank[BLACK_SIDE][f] < getRow(sq))
			r -= DOUBLED_PAWN_PENALTY;

		/* if there aren't any friendly pawns on either side of
			   this one, it's isolated */
		if ((pawnRank[BLACK_SIDE][f - 1] == 7) &&
				(pawnRank[BLACK_SIDE][f + 1] == 7))
			r -= ISOLATED_PAWN_PENALTY;

			/* if it's not isolated, it might be backwards */
		else if ((pawnRank[BLACK_SIDE][f - 1] > getRow(sq)) &&
				(pawnRank[BLACK_SIDE][f + 1] > getRow(sq)))
			r -= BACKWARDS_PAWN_PENALTY;

		/* add a bonus if the pawn is passed */
		if ((pawnRank[WHITE_SIDE][f - 1] <= getRow(sq)) &&
				(pawnRank[WHITE_SIDE][f] <= getRow(sq)) &&
				(pawnRank[WHITE_SIDE][f + 1] <= getRow(sq)))
			r += getRow(sq) * PASSED_PAWN_BONUS;

		return r;
	}

	int evalLightKing(int sq) {
		int r = kingPcsq[sq]; /* return value */

		/* if the king is castled, use a special function to evaluate the
			   pawns on the appropriate side */
		if (getColumn(sq) < 3) {
			r += evalLkp(1);
			r += evalLkp(2);
			r += evalLkp(3) / 2;  /* problems with pawns on the c & f files
																are not as severe */
		} else if (getColumn(sq) > 4) {
			r += evalLkp(8);
			r += evalLkp(7);
			r += evalLkp(6) / 2;
		} else { // otherwise, just assess a penalty if there are open files near the king
			for (int i = getColumn(sq); i <= getColumn(sq) + 2; ++i) {
				if ((pawnRank[WHITE_SIDE][i] == 0) && (pawnRank[BLACK_SIDE][i] == 7)) {
					r -= 10;
				}
			}
		}

		/* scale the king safety value according to the opponent's material;
			   the premise is that your king safety can only be bad if the
			   opponent has enough piecesBitmap to attack you */
		r *= pieceMat[BLACK_SIDE];
		r /= 3100;

		return r;
	}

	/**
	 * Evaluates the Light King Pawn on file f
	 *
	 * @param f target file
	 * @return move rating?
	 */
	int evalLkp(int f) {
		int r = 0;

		if (pawnRank[WHITE_SIDE][f] == 6) ;  /* pawn hasn't moved */
		else if (pawnRank[WHITE_SIDE][f] == 5)
			r -= 10;  /* pawn moved one square */
		else if (pawnRank[WHITE_SIDE][f] != 0)
			r -= 20;  /* pawn moved more than one square */
		else
			r -= 25;  /* no pawn on this file */

		if (pawnRank[BLACK_SIDE][f] == 7)
			r -= 15;  /* no enemy pawn */
		else if (pawnRank[BLACK_SIDE][f] == 5)
			r -= 10;  /* enemy pawn on the 3rd rank */
		else if (pawnRank[BLACK_SIDE][f] == 4)
			r -= 5;   /* enemy pawn on the 4th rank */

		return r;
	}

	int evalDarkKing(int sq) {
		int r;
		int i;

		r = kingPcsq[flip[sq]];
		if (getColumn(sq) < 3) {
			r += evalDkp(1);
			r += evalDkp(2);
			r += evalDkp(3) / 2;
		} else if (getColumn(sq) > 4) {
			r += evalDkp(8);
			r += evalDkp(7);
			r += evalDkp(6) / 2;
		} else {
			for (i = getColumn(sq); i <= getColumn(sq) + 2; ++i) {
				if ((pawnRank[WHITE_SIDE][i] == 0) && (pawnRank[BLACK_SIDE][i] == 7)) {
					r -= 10;
				}
			}
		}
		r *= pieceMat[WHITE_SIDE];
		r /= 3100;
		return r;
	}

	int evalDkp(int f) {
		int r = 0;

		if (pawnRank[BLACK_SIDE][f] == 1) ;
		else if (pawnRank[BLACK_SIDE][f] == 2)
			r -= 10;
		else if (pawnRank[BLACK_SIDE][f] != 7)
			r -= 20;
		else
			r -= 25;

		if (pawnRank[WHITE_SIDE][f] == 0)
			r -= 15;
		else if (pawnRank[WHITE_SIDE][f] == 2)
			r -= 10;
		else if (pawnRank[WHITE_SIDE][f] == 3)
			r -= 5;

		return r;
	}

	public static int getColumn(int x) {
		return (x & 7);
	}

	public static int getRow(int x) {
		return (x >> 3);
	}

	public static int getPosition(int c, int r) {
		return 8 * r + c;
	}

	/**
	 * Get horizontal coordinate on the board for the given index of column
	 */
	public static int getColumn(int x, boolean reside) {
		if (reside)
			x = 63 - x;
		return (x & 7);
	}

	/**
	 * Get vertical coordinate on the board for the given index of row
	 */
	public static int getRow(int y, boolean reside) {
		if (reside)
			y = 63 - y;
		return (y >> 3);  // the same as /8
	}

	@Override
	public int[] getColor() {
		return color;
	}

	public static int getPositionIndex(int col, int row, boolean reside) {
		if (reside)
			return 63 - (8 * row + col);
		else
			return (8 * row + col);
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
	public int[] getPieces() {
		return pieces;
	}

	@Override
	public int getPiece(int pieceId) {
		return pieces[pieceId];
	}

	@Override
	public int getPly() {
		return ply;
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
	public int getBlackKing() {
		return blackKing;
	}

	@Override
	public int getWhiteKing() {
		return whiteKing;
	}

	@Override
	public int[] getBlackKingMoveOO() {
		return blackKingMoveOO;
	}

	@Override
	public int[] getBlackKingMoveOOO() {
		return blackKingMoveOOO;
	}

	@Override
	public int[] getWhiteKingMoveOO() {
		return whiteKingMoveOO;
	}

	@Override
	public int[] getWhiteKingMoveOOO() {
		return whiteKingMoveOOO;
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
	public int[][] getHistory() {
		return history;
	}

	@Override
	public void setOppositeSide(int oppositeSide) {
		this.oppositeSide = oppositeSide;
	}

	@Override
	public int getFifty() {
		return fifty;
	}

	@Override
	public boolean isPossibleToMakeMoves() {
		TreeSet<Move> validMoves = generateLegalMoves();

		boolean found = false;
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
		if (!fen.equals(Symbol.EMPTY)) {
			buildBoardFromFen(fen);

			MoveParser.fenParse(fen, this);
			String[] tmp = fen.split(Symbol.SPACE);
			if (tmp.length > 1) {
				if (tmp[1].trim().equals(MoveParser.B_SMALL)) { // Active color. "w" means white moves next, "b" means black. // http://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
					setReside(true);
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
	public Move getLastMove() {
		return ply == 0 ? null : histDat[ply - 1].move;
	}

	@Override
	public Move getNextMove() {
		boolean isHistoryPresent = ply < movesCount && histDat[ply] != null;
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
			colorsBackup = color.clone();
			switchSides();
			switchEnPassant();
		}
		TreeSet<Move> moves = generateLegalMoves();
		CopyOnWriteArrayList<Move> validMoves = new CopyOnWriteArrayList<Move>();

		//String movesStr = new String();
		for (Move move : moves) {
			if (makeMove(move, false)) {
				//movesStr += " " + move;
				takeBack();
				validMoves.add(move);
			}
		}
		//Log.d("validmoves", "generateLegalMoves and test " + movesStr);

		if (forceSwitchSide) {
			switchSides();
			switchEnPassant();
			pieces = piecesBackup;
			color = colorsBackup;
		}

		Log.d("validmoves", "generated validMoves.size() " + validMoves.size());

		return validMoves;
	}

	private void updateEnPassant(int enPassant) {
		this.enPassantPrev = this.enPassant;
		this.enPassant = enPassant;
	}
}
