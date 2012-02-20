//
//  Board.java
//  ChessApp
//
//  Created by Peter Hunter on Sun Dec 30 2001.
//  Java version copyright (c) 2001 Peter Hunter. All rights reserved.
//  This code is heavily based on Tom Kerrigan's tscp, for which he
//  owns the copyright, and is used with his permission. All rights are
//  reserved by the owners of the respective copyrights.
package com.chess.engine;

import android.util.Log;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivity;
import com.chess.utilities.SoundPlayer;

import java.net.URLEncoder;
import java.util.TreeSet;

public class Board {
	final public static int LIGHT = 0;
	final public static int DARK = 1;
	final public static int PAWN = 0;
	final static int KNIGHT = 1;
	final static int BISHOP = 2;
	final static int ROOK = 3;
	final static int QUEEN = 4;
	final static int KING = 5;
	final static int EMPTY = 6;

	final static char A1 = 56;
	final static char B1 = 57;
	final static char C1 = 58;
	final static char D1 = 59;
	final static char E1 = 60;
	final static char F1 = 61;
	final static char G1 = 62;
	final static char H1 = 63;
	final static char A8 = 0;
	final static char B8 = 1;
	final static char C8 = 2;
	final static char D8 = 3;
	final static char E8 = 4;
	final static char F8 = 5;
	final static char G8 = 6;
	final static char H8 = 7;

	final static int DOUBLED_PAWN_PENALTY = 10;
	final static int ISOLATED_PAWN_PENALTY = 20;
	final static int BACKWARDS_PAWN_PENALTY = 8;
	final static int PASSED_PAWN_BONUS = 20;
	final static int ROOK_SEMI_OPEN_FILE_BONUS = 10;
	final static int ROOK_OPEN_FILE_BONUS = 15;
	final static int ROOK_ON_SEVENTH_BONUS = 20;

	public final static int HIST_STACK = 1000;

	public boolean init = false, chess960 = false, reside = false, submit = false, analysis = false, retry = false;
	private boolean tacticCanceled;
	public int side = LIGHT;
	public int sec = 0, left = 0;
	public int TacticsCorrectMoves = 0;
	private String[] tacticMoves;
	int xside = DARK;
	int rotated = 0;
	int ep = -1;
	int fifty = 0;
	public int movesCount = 0;
	public int hply = 0;
	int history[][] = new int[64][64];
	public HistoryData histDat[] = new HistoryData[HIST_STACK];
	int pawnRank[][] = new int[2][10];
	int pieceMat[] = new int[2];
	int pawnMat[] = new int[2];

	int boardcolor[] = {
			0, 1, 0, 1, 0, 1, 0, 1,
			1, 0, 1, 0, 1, 0, 1, 0,
			0, 1, 0, 1, 0, 1, 0, 1,
			1, 0, 1, 0, 1, 0, 1, 0,
			0, 1, 0, 1, 0, 1, 0, 1,
			1, 0, 1, 0, 1, 0, 1, 0,
			0, 1, 0, 1, 0, 1, 0, 1,
			1, 0, 1, 0, 1, 0, 1, 0
	};

	public int color[] = {
			1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6,
			0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
	};

	public int piece[] = {
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

	public boolean castleMask[] = {false, false, false, false};

	/* the values of the pieces */
	int pieceValue[] = {
			100, 300, 300, 500, 900, 0
	};

	/* The "pcsq" arrays are piece/square tables. They're values
		added to the material value of the piece based on the
		location of the piece. */

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

	/* The flip array is used to calculate the piece/square
		values for DARK pieces. The piece/square value of a
		LIGHT pawn is pawnPcsq[sq] and the value of a DARK
		pawn is pawnPcsq[flip[sq]] */
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

	public int mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;

	private final int BLACK_ROOK_1_INITIAL_POS = 0;
	private final int BLACK_ROOK_2_INITIAL_POS = 7;
	private final int WHITE_ROOK_1_INITIAL_POS = 56;
	private final int WHITE_ROOK_2_INITIAL_POS = 63;

	public int bRook1 = BLACK_ROOK_1_INITIAL_POS;
	public int bKing = 4;
	public int bRook2 = BLACK_ROOK_2_INITIAL_POS;

	public int wRook1 = WHITE_ROOK_1_INITIAL_POS;
	public int wKing = 60;
	public int wRook2 = WHITE_ROOK_2_INITIAL_POS;

	public int[] bKingMoveOO = new int[]{6};
	public int[] bKingMoveOOO = new int[]{2};

	public int[] wKingMoveOO = new int[]{62};
	public int[] wKingMoveOOO = new int[]{58};

	//private boolean userColorWhite;
	private CoreActivity coreActivity;

	public Board(CoreActivity coreActivity) {
		this.coreActivity = coreActivity;
	}

	public void ResetCastlePos() {
		bRook1 = 0;
		bKing = 4;
		bRook2 = 7;
		wRook1 = 56;
		wKing = 60;
		wRook2 = 63;
		bKingMoveOO = new int[]{6};
		bKingMoveOOO = new int[]{2};
		wKingMoveOO = new int[]{62};
		wKingMoveOOO = new int[]{58};
	}

	public int[] GenCastlePos(String fen) {
		//rnbqk2r/pppp1ppp/5n2/4P3/1bB2p2/2N5/PPPP2PP/R1BQK1NR
		String[] tmp = fen.split(" ");

		if (tmp.length > 2) { //0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
			String castling = tmp[2].trim();
			if (!castling.contains("K")) {
				castleMask[2] = true;
			}
			if (!castling.contains("Q")) {
				castleMask[3] = true;
			}
			if (!castling.contains("k")) {
				castleMask[0] = true;
			}
			if (!castling.contains("q")) {
				castleMask[1] = true;
			}
			Log.d(fen, "" + castleMask[2] + castleMask[3] + castleMask[0] + castleMask[1]);
		}

		String[] FEN = tmp[0].split("[/]");
		int offset = 0, i = 0;
		boolean found = false;
		for (i = 0; i < FEN[0].length(); i++) {
			if (FEN[0].charAt(i) == 'r') {
				if (!found)
					bRook1 = i + offset;
				else
					bRook2 = i + offset;
				found = true;
			}
			if (FEN[0].charAt(i) == 'k') {
				bKing = i + offset;
				found = true;
			}
			if (FEN[0].substring(i, i + 1).matches("[0-9]")) {
				offset += (Integer.parseInt(FEN[0].substring(i, i + 1)) - 1);
			}
		}
		offset = 56;
		found = false;
		for (i = 0; i < FEN[7].length(); i++) {
			if (FEN[7].charAt(i) == 'R') {
				if (!found)
					wRook1 = i + offset;
				else
					wRook2 = i + offset;
				found = true;
			}
			if (FEN[7].charAt(i) == 'K') {
				wKing = i + offset;
				found = true;
			}
			if (FEN[7].substring(i, i + 1).matches("[0-9]")) {
				offset += (Integer.parseInt(FEN[7].substring(i, i + 1)) - 1);
			}
		}

		//black O-O
		if (bKing < 5) {
			//bKingMoveOO = new int[]{6,7};
			bKingMoveOO = new int[8 - (bKing + 2)];
			for (i = 0; i < bKingMoveOO.length; i++)
				bKingMoveOO[i] = bKing + 2 + i;
		} else {
			if (bKing == 5) {
				if (bRook2 == 6) {
					bKingMoveOO = new int[]{6, 7};
				} else {
					bKingMoveOO = new int[]{7};
				}
			} else {
				bKingMoveOO = new int[]{7};
			}
		}
		//black O-O-O
		if (bKing > 3) {
			bKingMoveOOO = new int[]{0, 1, 2};
		} else {
			if (bKing == 3) {
				if (bRook1 == 2) {
					bKingMoveOOO = new int[]{0, 1, 2};
				} else {
					bKingMoveOOO = new int[]{0, 1};
				}
			} else if (bKing == 2) {
				if (bRook1 == 1) {
					bKingMoveOOO = new int[]{0, 1};
				} else {
					bKingMoveOOO = new int[]{0};
				}
			} else if (bKing == 1) {
				bKingMoveOOO = new int[]{0};
			}
		}

		//white O-O
		if (wKing < 61) {
			//wKingMoveOO = new int[]{62,63};
			wKingMoveOO = new int[64 - (wKing + 2)];
			for (i = 0; i < 64 - (wKing + 2); i++)
				wKingMoveOO[i] = wKing + 2 + i;
		} else {
			if (wKing == 61) {
				if (wRook2 == 62) {
					wKingMoveOO = new int[]{62, 63};
				} else {
					wKingMoveOO = new int[]{63};
				}
			} else if (wKing == 62) {
				wKingMoveOO = new int[]{63};
			}
		}
		//white O-O-O
		if (wKing > 59) {
			wKingMoveOOO = new int[]{56, 57, 58};
		} else {
			if (wKing == 59) {
				if (wRook1 == 58) {
					wKingMoveOOO = new int[]{56, 57, 58};
				} else {
					wKingMoveOOO = new int[]{56, 57};
				}
			} else if (wKing == 58) {
				if (wRook1 == 57) {
					wKingMoveOOO = new int[]{56, 57};
				} else {
					wKingMoveOOO = new int[]{56};
				}
			} else {
				wKingMoveOOO = new int[]{56};
			}
		}

		Log.d(fen, bRook1 + " " + bKing + " " + bRook2 + " " + wRook1 + " " + wKing + " " + wRook2);
		return new int[]{bRook1, bKing, bRook2, wRook1, wKing, wRook2};
	}


	public int getColor(int i, int j) {
		return color[(i << 3) + j];
	}

	public int getPiece(int i, int j) {
		return piece[(i << 3) + j];
	}

	public boolean isWhiteToMove() {
		return (side == LIGHT);
	}

	/* inCheck() returns true if side s is in check and false
		otherwise. It just scans the board to find side s's king
		and calls attack() to see if it's being attacked. */

	public boolean inCheck(int s) {
		int i;

		for (i = 0; i < 64; ++i)
			if (piece[i] == KING && color[i] == s)
				return attack(i, s ^ 1);
		return true;  /* shouldn't get here */
	}


	/* attack() returns true if square sq is being attacked by side
		s and false otherwise. */

	boolean attack(int sq, int s) {
		int i, j, n;

		for (i = 0; i < 64; ++i)
			if (color[i] == s) {
				int p = piece[i];
				if (p == PAWN) {
					if (s == LIGHT) {
						if (COL(i) != 0 && i - 9 == sq)
							return true;
						if (COL(i) != 7 && i - 7 == sq)
							return true;
					} else {
						if (COL(i) != 0 && i + 7 == sq)
							return true;
						if (COL(i) != 7 && i + 9 == sq)
							return true;
					}
				} else if (p < offsets.length) {
					for (j = 0; j < offsets[p]; ++j)
						for (n = i; ; ) {
							n = mailbox[mailbox64[n] + offset[p][j]];
							if (n == -1)
								break;
							if (n == sq)
								return true;
							if (color[n] != EMPTY)
								break;
							if (!slide[p])
								break;
						}
				}
			}
		return false;
	}


	/* gen() generates pseudo-legal moves for the current position.
		It scans the board to find friendly pieces and then determines
		what squares they attack. When it finds a piece/square
		combination, it calls genPush to put the move on the "move
		stack." */

	public TreeSet<Move> gen() {
		TreeSet<Move> ret = new TreeSet<Move>();

		for (int i = 0; i < 64; ++i)
			if (color[i] == side) {
				if (piece[i] == PAWN) {
					if (side == LIGHT) {
						if (COL(i) != 0 && color[i - 9] == DARK)
							genPush(ret, i, i - 9, 17);
						if (COL(i) != 7 && color[i - 7] == DARK)
							genPush(ret, i, i - 7, 17);
						if (color[i - 8] == EMPTY) {
							genPush(ret, i, i - 8, 16);
							if (i >= 48 && color[i - 16] == EMPTY)
								genPush(ret, i, i - 16, 24);
						}
					} else {
						if (COL(i) != 0 && color[i + 7] == LIGHT)
							genPush(ret, i, i + 7, 17);
						if (COL(i) != 7 && color[i + 9] == LIGHT)
							genPush(ret, i, i + 9, 17);
						if (color[i + 8] == EMPTY) {
							genPush(ret, i, i + 8, 16);
							if (i <= 15 && color[i + 16] == EMPTY)
								genPush(ret, i, i + 16, 24);
						}
					}
				} else if (piece[i] < offsets.length)
					for (int j = 0; j < offsets[piece[i]]; ++j)
						for (int n = i; ; ) {
							n = mailbox[mailbox64[n] + offset[piece[i]][j]];
							if (n == -1)
								break;
							if (color[n] != EMPTY) {
								if (color[n] == xside)
									genPush(ret, i, n, 1);
								break;
							}
							genPush(ret, i, n, 0);
							if (!slide[piece[i]])
								break;
						}
			}

		/* generate castle moves */
		int i;
		//0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
		if (side == LIGHT) {
			if (!castleMask[2]) {
				for (i = 0; i < wKingMoveOO.length; i++)
					genPush(ret, wKing, wKingMoveOO[i], 2);
			}
			if (!castleMask[3]) {
				for (i = 0; i < wKingMoveOOO.length; i++)
					genPush(ret, wKing, wKingMoveOOO[i], 2);
			}
		} else {
			if (!castleMask[0]) {
				for (i = 0; i < bKingMoveOO.length; i++)
					genPush(ret, bKing, bKingMoveOO[i], 2);
			}
			if (!castleMask[1]) {
				for (i = 0; i < bKingMoveOOO.length; i++)
					genPush(ret, bKing, bKingMoveOOO[i], 2);
			}
		}

		/* generate en passant moves */
		if (ep != -1) {
			if (side == LIGHT) {
				if (COL(ep) != 0 && color[ep + 7] == LIGHT && piece[ep + 7] == PAWN)
					genPush(ret, ep + 7, ep, 21);
				if (COL(ep) != 7 && color[ep + 9] == LIGHT && piece[ep + 9] == PAWN)
					genPush(ret, ep + 9, ep, 21);
			} else {
				if (COL(ep) != 0 && color[ep - 9] == DARK && piece[ep - 9] == PAWN)
					genPush(ret, ep - 9, ep, 21);
				if (COL(ep) != 7 && color[ep - 7] == DARK && piece[ep - 7] == PAWN)
					genPush(ret, ep - 7, ep, 21);
			}
		}
		return ret;
	}


/* genCaps() is basically a copy of gen() that's modified to
   only generate capture and promote moves. It's used by the
   quiescence search. */

	TreeSet<Move> genCaps() {
		TreeSet<Move> ret = new TreeSet<Move>();

		for (int i = 0; i < 64; ++i)
			if (color[i] == side) {
				if (piece[i] == PAWN) {
					if (side == LIGHT) {
						if (COL(i) != 0 && color[i - 9] == DARK)
							genPush(ret, i, i - 9, 17);
						if (COL(i) != 7 && color[i - 7] == DARK)
							genPush(ret, i, i - 7, 17);
						if (i <= 15 && color[i - 8] == EMPTY)
							genPush(ret, i, i - 8, 16);
					}
					if (side == DARK) {
						if (COL(i) != 0 && color[i + 7] == LIGHT)
							genPush(ret, i, i + 7, 17);
						if (COL(i) != 7 && color[i + 9] == LIGHT)
							genPush(ret, i, i + 9, 17);
						if (i >= 48 && color[i + 8] == EMPTY)
							genPush(ret, i, i + 8, 16);
					}
				} else if (piece[i] < offsets.length)
					for (int j = 0; j < offsets[piece[i]]; ++j)
						for (int n = i; ; ) {
							n = mailbox[mailbox64[n] + offset[piece[i]][j]];
							if (n == -1)
								break;
							if (color[n] != EMPTY) {
								if (color[n] == xside)
									genPush(ret, i, n, 1);
								break;
							}
							if (!slide[piece[i]])
								break;
						}
			}
		if (ep != -1) {
			if (side == LIGHT) {
				if (COL(ep) != 0 && color[ep + 7] == LIGHT && piece[ep + 7] == PAWN)
					genPush(ret, ep + 7, ep, 21);
				if (COL(ep) != 7 && color[ep + 9] == LIGHT && piece[ep + 9] == PAWN)
					genPush(ret, ep + 9, ep, 21);
			} else {
				if (COL(ep) != 0 && color[ep - 9] == DARK && piece[ep - 9] == PAWN)
					genPush(ret, ep - 9, ep, 21);
				if (COL(ep) != 7 && color[ep - 7] == DARK && piece[ep - 7] == PAWN)
					genPush(ret, ep - 7, ep, 21);
			}
		}
		return ret;
	}

	/* genPush() puts a move on the move stack, unless it's a
		pawn promotion that needs to be handled by genPromote().
		It also assigns a score to the move for alpha-beta move
		ordering. If the move is a capture, it uses MVV/LVA
		(Most Valuable Victim/Least Valuable Attacker). Otherwise,
		it uses the move's history heuristic value. Note that
		1,000,000 is added to a capture move's score, so it
		always gets ordered above a "normal" move. */

	void genPush(TreeSet<Move> ret, int from, int to, int bits) {
		if ((bits & 16) != 0) {
			if (side == LIGHT) {
				if (to <= H8) {
					genPromote(ret, from, to, bits);
					return;
				}
			} else {
				if (to >= A1) {
					genPromote(ret, from, to, bits);
					return;
				}
			}
		}

		Move g = new Move(from, to, 0, bits);

		if (color[to] != EMPTY)
			g.setScore(1000000 + (piece[to] * 10) - piece[from]);
		else
			g.setScore(history[from][to]);
		ret.add(g);
	}


	/* genPromote() is just like genPush(), only it puts 4 moves
		on the move stack, one for each possible promotion piece */

	void genPromote(TreeSet<Move> ret, int from, int to, int bits) {
		for (char i = KNIGHT; i <= QUEEN; ++i) {
			Move g = new Move(from, to, i, (bits | 32));
			g.setScore(1000000 + (i * 10));
			ret.add(g);
		}
	}

	/* makemove() makes a move. If the move is illegal, it
		undoes whatever it did and returns false. Otherwise, it
		returns true. */

	public boolean makeMove(Move m) {
		return makeMove(m, true);
	}

	public boolean makeMove(Move m, boolean playSound) {

		/* test to see if a castle move is legal and move the rook
			   (the king is moved with the usual move code later) */
		int what = -1; //0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
		if ((m.bits & 2) != 0) {
			int from = -1, to = -1;

			int[] piece_tmp = piece.clone();

			if (inCheck(side))
				return false;

			int d = Math.abs(m.from - m.to);
			int min = m.to;
			if (m.from < m.to) min = m.from;

			int i;
			for (i = 0; i < bKingMoveOO.length; i++) {
				if (bKingMoveOO[i] == m.to) {
					what = 0;
					d = Math.abs(m.from - bRook2);
					min = bRook2;
					if (m.from < bRook2) min = m.from;
					break;
				}
			}
			for (i = 0; i < bKingMoveOOO.length; i++) {
				if (bKingMoveOOO[i] == m.to) {
					what = 1;
					d = Math.abs(m.from - bRook1);
					min = bRook1;
					if (m.from < bRook1) min = m.from;
					break;
				}
			}
			for (i = 0; i < wKingMoveOO.length; i++) {
				if (wKingMoveOO[i] == m.to) {
					what = 2;
					d = Math.abs(m.from - wRook2);
					min = wRook2;
					if (m.from < wRook2) min = m.from;
					break;
				}
			}
			for (i = 0; i < wKingMoveOOO.length; i++) {
				if (wKingMoveOOO[i] == m.to) {
					what = 3;
					d = Math.abs(m.from - wRook1);
					min = wRook1;
					if (m.from < wRook1) min = m.from;
					break;
				}
			}

			if (castleMask[what]) return false;

			if (what == 2) {
				if (attack(F1, xside) || attack(G1, xside))
					return false;
				if (color[F1] != EMPTY && piece[F1] != KING && piece[F1] != ROOK)
					return false;
				if (color[G1] != EMPTY && piece[G1] != KING && piece[G1] != ROOK)
					return false;
				if (piece[F1] == ROOK && F1 != wRook2)
					return false;
				if (piece[G1] == ROOK && G1 != wRook2)
					return false;


				if (d > 1) {
					while (d != 0) {
						if (piece[++min] != ROOK && piece[min] != KING && color[min] != EMPTY) {
							return false;
						}
						d--;
					}
				}

				from = wRook2;
				to = F1;
			} else if (what == 3) {
				if (attack(C1, xside) || attack(D1, xside))
					return false;
				if (color[C1] != EMPTY && piece[C1] != KING && piece[C1] != ROOK)
					return false;
				if (color[D1] != EMPTY && piece[D1] != KING && piece[D1] != ROOK)
					return false;
				if (piece[C1] == ROOK && C1 != wRook1)
					return false;
				if (piece[D1] == ROOK && D1 != wRook1)
					return false;

				if (d > 1) {
					while (d != 0) {
						if (piece[++min] != ROOK && piece[min] != KING && color[min] != EMPTY) {
							return false;
						}
						d--;
					}
				}

				from = wRook1;
				to = D1;
			} else if (what == 1) {
				if (attack(C8, xside) || attack(D8, xside))
					return false;
				if (color[C8] != EMPTY && piece[C8] != KING && piece[C8] != ROOK)
					return false;
				if (color[D8] != EMPTY && piece[D8] != KING && piece[D8] != ROOK)
					return false;
				if (piece[C8] == ROOK && C8 != bRook1)
					return false;
				if (piece[D8] == ROOK && D8 != bRook1)
					return false;

				if (d > 1) {
					while (d != 0) {
						if (piece[++min] != ROOK && piece[min] != KING && color[min] != EMPTY) {
							return false;
						}
						d--;
					}
				}

				from = bRook1;
				to = D8;
			} else if (what == 0) {
				if (attack(F8, xside) || attack(G8, xside))
					return false;
				if (color[F8] != EMPTY && piece[F8] != KING && piece[F8] != ROOK)
					return false;
				if (color[G8] != EMPTY && piece[G8] != KING && piece[G8] != ROOK)
					return false;

				if (piece[F8] == ROOK && bRook2 != F8)
					return false;
				if (piece[G8] == ROOK && bRook2 != G8)
					return false;

				if (d > 1) {
					while (d != 0) {
						if (piece[++min] != ROOK && piece[min] != KING && color[min] != EMPTY) {
							return false;
						}
						d--;
					}
				}

				from = bRook2;
				to = F8;
			}

			color[to] = color[from];
			piece[to] = piece[from];
			if (to != from) {
				color[from] = EMPTY;
				piece[from] = EMPTY;
			}

			/* back up information so we can take the move back later. */
			histDat[hply] = new HistoryData();
			histDat[hply].m = m;
			histDat[hply].capture = piece[m.to];
			histDat[hply].ep = ep;
			histDat[hply].fifty = fifty;
			histDat[hply].castleMask = castleMask.clone();
			histDat[hply].what = what;
			if (what == 0 || what == 2)
				histDat[hply].notation = "O-O";
			else
				histDat[hply].notation = "O-O-O";
			++hply;

			/* update the castle, en passant, and
					   fifty-move-draw variables */
			if (what != -1) castleMask[what] = true;
			if (piece[m.from] == KING) {
				if (side == DARK) {
					castleMask[0] = true;
					castleMask[1] = true;
				} else {
					castleMask[2] = true;
					castleMask[3] = true;
				}
			}
			//0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
			if (piece[m.from] == ROOK) {
				if (side == DARK) {
					if (m.from == bRook2)
						castleMask[0] = true;
					if (m.from == bRook1)
						castleMask[1] = true;
				} else {
					if (m.from == wRook2)
						castleMask[2] = true;
					if (m.from == wRook1)
						castleMask[3] = true;
				}
			}

			if ((m.bits & 8) != 0) {
				if (side == LIGHT)
					ep = m.to + 8;
				else
					ep = m.to - 8;
			} else
				ep = -1;
			if ((m.bits & 17) != 0)
				fifty = 0;
			else
				++fifty;

			/* move the piece */
			int tmp_to = -1;
			if (what == 3) {
				color[58] = side;
				piece[58] = piece_tmp[(int) m.from];
				tmp_to = 58;
			} else if (what == 2) {
				color[62] = side;
				piece[62] = piece_tmp[(int) m.from];
				tmp_to = 62;
			} else if (what == 1) {
				color[2] = side;
				piece[2] = piece_tmp[(int) m.from];
				tmp_to = 2;
			} else if (what == 0) {
				color[6] = side;
				piece[6] = piece_tmp[(int) m.from];
				tmp_to = 6;
			}
			if (piece[m.from] != ROOK && tmp_to != m.from) {
				color[m.from] = EMPTY;
				piece[m.from] = EMPTY;
			}

			/* switch sides and test for legality (if we can capture
					   the other guy's king, it's an illegal position and
					   we need to take the move back) */
			side ^= 1;
			xside ^= 1;
			if (inCheck(xside)) {
				takeBack();
				return false;
			}

			if (playSound) {
				getSoundPlayer().playCastle();
			}

			return true;
		}

		/* back up information so we can take the move back later. */
		histDat[hply] = new HistoryData();
		histDat[hply].m = m;
		histDat[hply].capture = piece[(int) m.to];
		histDat[hply].ep = ep;
		histDat[hply].fifty = fifty;
		histDat[hply].castleMask = castleMask.clone();
		histDat[hply].what = what;
		histDat[hply].notation = GetMoveSAN();
		++hply;

		/* update the castle, en passant, and
			   fifty-move-draw variables */
		if (what != -1) castleMask[what] = true;
		if (piece[m.from] == KING) {
			if (side == DARK) {
				castleMask[0] = true;
				castleMask[1] = true;
			} else {
				castleMask[2] = true;
				castleMask[3] = true;
			}
		}
		//0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
		if (piece[m.from] == ROOK) {
			if (side == DARK) {
				if (m.from == bRook2)
					castleMask[0] = true;
				if (m.from == bRook1)
					castleMask[1] = true;
			} else {
				if (m.from == wRook2)
					castleMask[2] = true;
				if (m.from == wRook1)
					castleMask[3] = true;
			}
		}

		if (m.to == BLACK_ROOK_1_INITIAL_POS && !castleMask[1]) // q (fen castle)
		{
			castleMask[1] = true;
		}
		if (m.to == BLACK_ROOK_2_INITIAL_POS && !castleMask[0]) // k (fen castle)
		{
			castleMask[0] = true;
		}
		if (m.to == WHITE_ROOK_1_INITIAL_POS && !castleMask[3]) // Q (fen castle)
		{
			castleMask[3] = true;
		}
		if (m.to == WHITE_ROOK_2_INITIAL_POS && !castleMask[2]) // K (fen castle)
		{
			castleMask[2] = true;
		}

		if ((m.bits & 8) != 0) {
			if (side == LIGHT)
				ep = m.to + 8;
			else
				ep = m.to - 8;
		} else
			ep = -1;
		if ((m.bits & 17) != 0)
			fifty = 0;
		else
			++fifty;

		/* move the piece */

		int colorFrom = color[m.from];
		int pieceTo = piece[m.to];

		color[m.to] = side;

		if ((m.bits & 32) != 0) {
			piece[m.to] = m.promote;
			//System.out.println("!!!!!!!! PROMOTION");
		} else {
			piece[m.to] = piece[m.from];
		}

		color[m.from] = EMPTY;
		piece[m.from] = EMPTY;

		/* erase the pawn if this is an en passant move */
		if ((m.bits & 4) != 0) {
			if (side == LIGHT) {
				color[m.to + 8] = EMPTY;
				piece[m.to + 8] = EMPTY;
			} else {
				color[m.to - 8] = EMPTY;
				piece[m.to - 8] = EMPTY;
			}
		}

		/* switch sides and test for legality (if we can capture
			   the other guy's king, it's an illegal position and
			   we need to take the move back) */
		side ^= 1;
		xside ^= 1;

		Boolean userColorWhite = coreActivity.isUserColorWhite();
		if (playSound && userColorWhite != null) {
			if ((userColorWhite && colorFrom == 1) || (!userColorWhite && colorFrom == 0)) {
				if (inCheck(side)) {
					getSoundPlayer().playMoveOpponentCheck();
				} else if (pieceTo != 6) {
					getSoundPlayer().playCapture();
				} else {
					getSoundPlayer().playMoveOpponent();
				}
			} else if ((userColorWhite && colorFrom == 0) || (!userColorWhite && colorFrom == 1)) {
				if (inCheck(side)) {
					getSoundPlayer().playMoveSelfCheck();
				} else if (pieceTo != 6) {
					getSoundPlayer().playCapture();
				} else {
					getSoundPlayer().playMoveSelf();
				}
			}
		}

		if (inCheck(xside)) {
			takeBack();
			return false;
		}
		return true;
	}


/* takeBack() is very similar to makeMove(), only backwards :)  */

	public void takeBack() {
		if (hply - 1 < 0) return;
		side ^= 1;
		xside ^= 1;
		--hply;
		Move m = histDat[hply].m;
		ep = histDat[hply].ep;
		fifty = histDat[hply].fifty;
		castleMask = histDat[hply].castleMask.clone();

		if ((m.bits & 2) != 0) {

			int[] piece_tmp = piece.clone();

			int i;
			int what = -1; //0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
			for (i = 0; i < bKingMoveOO.length; i++) {
				if (bKingMoveOO[i] == m.to)
					what = 0;
			}
			for (i = 0; i < bKingMoveOOO.length; i++) {
				if (bKingMoveOOO[i] == m.to)
					what = 1;
			}
			for (i = 0; i < wKingMoveOO.length; i++) {
				if (wKingMoveOO[i] == m.to)
					what = 2;
			}
			for (i = 0; i < wKingMoveOOO.length; i++) {
				if (wKingMoveOOO[i] == m.to)
					what = 3;
			}
			int to = m.to;
			int pt = piece[to];
			if (what == 3) {
				pt = piece[58];
				to = 58;
			} else if (what == 2) {
				pt = piece[62];
				to = 62;
			} else if (what == 1) {
				pt = piece[2];
				to = 2;
			} else if (what == 0) {
				pt = piece[6];
				to = 6;
			}
			/* move the piece */
			color[m.from] = side;
			piece[m.from] = pt;
			if (m.from != to) {
				color[to] = EMPTY;
				piece[to] = EMPTY;
			}

			int from = -1;
			if (what == 2) {
				from = wRook2;
				to = F1;
			} else if (what == 3) {
				from = wRook1;
				to = D1;
			} else if (what == 1) {
				from = bRook1;
				to = D8;
			} else if (what == 0) {
				from = bRook2;
				to = F8;
			}
			color[from] = side;
			piece[from] = piece_tmp[to];
			if (to != from && piece[to] != KING) {
				color[to] = EMPTY;
				piece[to] = EMPTY;
			}

			return;
		}


		color[(int) m.from] = side;
		if ((m.bits & 32) != 0)
			piece[(int) m.from] = PAWN;
		else
			piece[(int) m.from] = piece[(int) m.to];
		if (histDat[hply].capture == EMPTY) {
			color[(int) m.to] = EMPTY;
			piece[(int) m.to] = EMPTY;
		} else {
			color[(int) m.to] = xside;
			piece[(int) m.to] = histDat[hply].capture;
		}
		if ((m.bits & 4) != 0) {
			if (side == LIGHT) {
				color[m.to + 8] = xside;
				piece[m.to + 8] = PAWN;
			} else {
				color[m.to - 8] = xside;
				piece[m.to - 8] = PAWN;
			}
		}
	}

	public void takeNext() {
		if (hply + 1 <= movesCount) {
			Move m = histDat[hply].m;
			makeMove(m);
		}
	}

	public String MoveList() {
		String output = "";
		int i = 0;
		for (i = 0; i < hply; i++) {
			Move m = histDat[i].m;
			if (i % 2 == 0)
				output += "\n" + (i / 2 + 1) + ". ";
			output += MoveParser.positionToString(m.from);
			output += MoveParser.positionToString(m.to);
			output += " ";
		}
		return output;
	}

	public String MoveListSAN() {
		String output = "";
		int i = 0;
		for (i = 0; i < hply; i++) {
			if (i % 2 == 0)
				output += "\n " + (i / 2 + 1) + ". ";
			output += histDat[i].notation;
			output += " ";
		}
		return output;
	}

	public String GetMoveSAN() {
		Move m = histDat[hply].m;
		int p = piece[m.from];
		String f = "", capture = "", promotion = "";
		if (p == 1) {
			f = "N";
			//ambigues
			int[] positions = new int[]{
					m.to - 17, m.to - 15, m.to - 10, m.to - 6,
					m.to + 17, m.to + 15, m.to + 10, m.to + 6
			};
			int i;
			for (i = 0; i < 8; i++) {
				int pos = positions[i];
				if (pos < 0 || pos > 63 || pos == m.from)
					continue;
				if (piece[pos] == 1 && color[pos] == side) {
					if (COL(pos) == COL(m.from))
						f += MoveParser.BNToNum(ROW(m.from));
					else
						f += MoveParser.BNToLetter(COL(m.from));
					break;
				}
			}
		}
		if (p == 2)
			f = "B";
		if (p == 3) {
			f = "R";
			//ambigues
			int[] positions = new int[]{
					m.to - 8, m.to - 16, m.to - 24, m.to - 32, m.to - 40, m.to - 48, m.to - 56,
					m.to + 8, m.to + 16, m.to + 24, m.to + 32, m.to + 40, m.to + 48, m.to + 56,
					m.to - 1, m.to - 2, m.to - 3, m.to - 4, m.to - 5, m.to - 6, m.to - 7,
					m.to + 1, m.to + 2, m.to + 3, m.to + 4, m.to + 5, m.to + 6, m.to + 7
			};
			int i;
			for (i = 0; i < 28; i++) {
				int pos = positions[i];
				if (pos < 0 || pos > 63 || pos == m.from)
					continue;
				if (piece[pos] == 3 && color[pos] == side) {
					if (COL(pos) == COL(m.from))
						f += MoveParser.BNToNum(ROW(m.from));
					else
						f += MoveParser.BNToLetter(COL(m.from));
					break;
				}
			}
		}
		if (p == 4)
			f = "Q";
		if (p == 5)
			f = "K";

		if (histDat[hply].capture != 6) {
			if (p == 0) {
				f = MoveParser.BNToLetter(COL(m.from));
			}
			capture = "x";
		}

		if (m.promote > 0) {
			int pr = m.promote;
			if (pr == 1)
				promotion = "=N";
			if (pr == 2)
				promotion = "=B";
			if (pr == 3)
				promotion = "=R";
			if (pr == 4)
				promotion = "=Q";
		}

		return f + capture + MoveParser.positionToString(m.to) + promotion;
	}

	private String convertMove() {
		Move m = histDat[hply - 1].m;
		String output = "";
		try {
			String to = MoveParser.positionToString(m.to);
			if ((m.bits & 2) != 0) {
				//0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
				int what = histDat[hply - 1].what;

				if (what == 0) {
					if (chess960)
						to = MoveParser.positionToString(bRook2);
					else
						to = "g8";
				} else if (what == 1) {
					if (chess960)
						to = MoveParser.positionToString(bRook1);
					else
						to = "c8";
				} else if (what == 2) {
					if (chess960)
						to = MoveParser.positionToString(wRook2);
					else
						to = "g1";
				} else if (what == 3) {
					if (chess960)
						to = MoveParser.positionToString(wRook1);
					else
						to = "c1";
				}
			}
			output = URLEncoder.encode(MoveParser.positionToString(m.from) + to, "UTF-8");
		} catch (Exception e) {
		}
		Log.d("move:", output);
		return output;
	}

	public String convertMoveEchess() {
		String output = convertMove();
		final Move m = histDat[hply - 1].m;
		switch (m.promote) {
			case Board.KNIGHT:
				output += (color[m.from] == 0 ? "=N" : "=n");
				break;
			case Board.BISHOP:
				output += (color[m.from] == 0 ? "=B" : "=b");
				break;
			case Board.ROOK:
				output += (color[m.from] == 0 ? "=R" : "=r");
				break;
			case Board.QUEEN:
				output += (color[m.from] == 0 ? "=Q" : "=q");
				break;
			default:
				break;
		}
		return output;
	}

	public String convertMoveLive() {
		String output = convertMove();
		final Move m = histDat[hply - 1].m;
		switch (m.promote) {
			case Board.KNIGHT:
				output += 'n';
				break;
			case Board.BISHOP:
				output += 'b';
				break;
			case Board.ROOK:
				output += 'r';
				break;
			case Board.QUEEN:
				output += 'q';
				break;
			default:
				break;
		}
		return output;
	}

	public String toString() {
		int i;

		StringBuffer sb = new StringBuffer("\n8 ");
		for (i = 0; i < 64; ++i) {
			switch (color[i]) {
				case EMPTY:
					sb.append(" .");
					break;
				case LIGHT:
					sb.append(" ");
					sb.append(pieceChar[piece[i]]);
					break;
				case DARK:
					sb.append(" ");
					sb.append((char) (pieceChar[piece[i]] + ('a' - 'A')));
					break;
				default:
					throw new IllegalStateException("Square not EMPTY, LIGHT or DARK: " + i);
			}
			if ((i + 1) % 8 == 0 && i != 63) {
				sb.append("\n");
				sb.append(Integer.toString(7 - ROW(i)));
				sb.append(" ");
			}
		}
		sb.append("\n\n   a b c d e f g h\n\n");
		return sb.toString();
	}

	/* reps() returns the number of times that the current
		position has been repeated. Thanks to John Stanback
		for this clever algorithm. */

	public int reps() {
		int b[] = new int[64];
		int c = 0;  /* count of squares that are different from
				   the current position */
		int r = 0;  /* number of repetitions */

		/* is a repetition impossible? */
		if (fifty <= 3)
			return 0;

		/* loop through the reversible moves */
		for (int i = hply - 1; i >= hply - fifty - 1; --i) {

			if (i < 0 || i >= histDat.length) return r;

			if (++b[histDat[i].m.from] == 0)
				--c;
			else
				++c;
			if (--b[histDat[i].m.to] == 0)
				--c;
			else
				++c;
			if (c == 0)
				++r;
		}

		return r;
	}

	int eval() {
		int score[] = new int[2];  /* each side's score */

		/* this is the first pass: set up pawnRank, pieceMat, and pawnMat. */
		int i = 0;
		for (i = 0; i < 10; ++i) {
			pawnRank[LIGHT][i] = 0;
			pawnRank[DARK][i] = 7;
		}
		pieceMat[LIGHT] = 0;
		pieceMat[DARK] = 0;
		pawnMat[LIGHT] = 0;
		pawnMat[DARK] = 0;

		for (i = 0; i < 64; i++) {
			if (color[i] == EMPTY)
				continue;
			if (piece[i] == PAWN) {
				pawnMat[color[i]] += pieceValue[PAWN];
				int f = COL(i) + 1;  /* add 1 because of the extra file in the array */
				if (color[i] == LIGHT) {
					if (pawnRank[LIGHT][f] < ROW(i))
						pawnRank[LIGHT][f] = ROW(i);
				} else {
					if (pawnRank[DARK][f] > ROW(i))
						pawnRank[DARK][f] = ROW(i);
				}
			} else {
				try {
					pieceMat[color[i]] += pieceValue[piece[i]];
				} catch (Exception e) {
					Log.d("I!!!!!!!!:", "" + i);
				}
			}
		}

		/* this is the second pass: evaluate each piece */
		score[LIGHT] = pieceMat[LIGHT] + pawnMat[LIGHT];
		score[DARK] = pieceMat[DARK] + pawnMat[DARK];
		for (i = 0; i < 64; ++i) {
			if (color[i] == EMPTY)
				continue;
			if (color[i] == LIGHT) {
				switch (piece[i]) {
					case PAWN:
						score[LIGHT] += evalLightPawn(i);
						break;
					case KNIGHT:
						score[LIGHT] += knightPcsq[i];
						break;
					case BISHOP:
						score[LIGHT] += bishopPcsq[i];
						break;
					case ROOK:
						if (pawnRank[LIGHT][COL(i) + 1] == 0) {
							if (pawnRank[DARK][COL(i) + 1] == 7)
								score[LIGHT] += ROOK_OPEN_FILE_BONUS;
							else
								score[LIGHT] += ROOK_SEMI_OPEN_FILE_BONUS;
						}
						if (ROW(i) == 1)
							score[LIGHT] += ROOK_ON_SEVENTH_BONUS;
						break;
					case KING:
						if (pieceMat[DARK] <= 1200)
							score[LIGHT] += kingEndgamePcsq[i];
						else
							score[LIGHT] += evalLightKing(i);
						break;
				}
			} else {
				switch (piece[i]) {
					case PAWN:
						score[DARK] += evalDarkPawn(i);
						break;
					case KNIGHT:
						score[DARK] += knightPcsq[flip[i]];
						break;
					case BISHOP:
						score[DARK] += bishopPcsq[flip[i]];
						break;
					case ROOK:
						if (pawnRank[DARK][COL(i) + 1] == 7) {
							if (pawnRank[LIGHT][COL(i) + 1] == 0)
								score[DARK] += ROOK_OPEN_FILE_BONUS;
							else
								score[DARK] += ROOK_SEMI_OPEN_FILE_BONUS;
						}
						if (ROW(i) == 6)
							score[DARK] += ROOK_ON_SEVENTH_BONUS;
						break;
					case KING:
						if (pieceMat[LIGHT] <= 1200)
							score[DARK] += kingEndgamePcsq[flip[i]];
						else
							score[DARK] += evalDarkKing(i);
						break;
				}
			}
		}

		/* the score[] array is set, now return the score relative
			   to the side to move */
		if (side == LIGHT)
			return score[LIGHT] - score[DARK];
		return score[DARK] - score[LIGHT];
	}

	int evalLightPawn(int sq) {
		int r = 0; /* return value */
		int f = COL(sq) + 1; /* pawn's file */

		r += pawnPcsq[sq];

		/* if there's a pawn behind this one, it's doubled */
		if (pawnRank[LIGHT][f] > ROW(sq))
			r -= DOUBLED_PAWN_PENALTY;

		/* if there aren't any friendly pawns on either side of
			   this one, it's isolated */
		if ((pawnRank[LIGHT][f - 1] == 0) &&
				(pawnRank[LIGHT][f + 1] == 0))
			r -= ISOLATED_PAWN_PENALTY;

			/* if it's not isolated, it might be backwards */
		else if ((pawnRank[LIGHT][f - 1] < ROW(sq)) &&
				(pawnRank[LIGHT][f + 1] < ROW(sq)))
			r -= BACKWARDS_PAWN_PENALTY;

		/* add a bonus if the pawn is passed */
		if ((pawnRank[DARK][f - 1] >= ROW(sq)) &&
				(pawnRank[DARK][f] >= ROW(sq)) &&
				(pawnRank[DARK][f + 1] >= ROW(sq)))
			r += (7 - ROW(sq)) * PASSED_PAWN_BONUS;

		return r;
	}

	int evalDarkPawn(int sq) {
		int r = 0;  /* the value to return */
		int f = COL(sq) + 1;  /* the pawn's file */

		r += pawnPcsq[flip[sq]];

		/* if there's a pawn behind this one, it's doubled */
		if (pawnRank[DARK][f] < ROW(sq))
			r -= DOUBLED_PAWN_PENALTY;

		/* if there aren't any friendly pawns on either side of
			   this one, it's isolated */
		if ((pawnRank[DARK][f - 1] == 7) &&
				(pawnRank[DARK][f + 1] == 7))
			r -= ISOLATED_PAWN_PENALTY;

			/* if it's not isolated, it might be backwards */
		else if ((pawnRank[DARK][f - 1] > ROW(sq)) &&
				(pawnRank[DARK][f + 1] > ROW(sq)))
			r -= BACKWARDS_PAWN_PENALTY;

		/* add a bonus if the pawn is passed */
		if ((pawnRank[LIGHT][f - 1] <= ROW(sq)) &&
				(pawnRank[LIGHT][f] <= ROW(sq)) &&
				(pawnRank[LIGHT][f + 1] <= ROW(sq)))
			r += ROW(sq) * PASSED_PAWN_BONUS;

		return r;
	}

	int evalLightKing(int sq) {
		int r = kingPcsq[sq]; /* return value */

		/* if the king is castled, use a special function to evaluate the
			   pawns on the appropriate side */
		if (COL(sq) < 3) {
			r += evalLkp(1);
			r += evalLkp(2);
			r += evalLkp(3) / 2;  /* problems with pawns on the c & f files
                                                                are not as severe */
		} else if (COL(sq) > 4) {
			r += evalLkp(8);
			r += evalLkp(7);
			r += evalLkp(6) / 2;
		}

		/* otherwise, just assess a penalty if there are open files near
			   the king */
		else {
			for (int i = COL(sq); i <= COL(sq) + 2; ++i)
				if ((pawnRank[LIGHT][i] == 0) &&
						(pawnRank[DARK][i] == 7))
					r -= 10;
		}

		/* scale the king safety value according to the opponent's material;
			   the premise is that your king safety can only be bad if the
			   opponent has enough pieces to attack you */
		r *= pieceMat[DARK];
		r /= 3100;

		return r;
	}

	/* evalLkp(f) evaluates the Light King Pawn on file f */

	int evalLkp(int f) {
		int r = 0;

		if (pawnRank[LIGHT][f] == 6) ;  /* pawn hasn't moved */
		else if (pawnRank[LIGHT][f] == 5)
			r -= 10;  /* pawn moved one square */
		else if (pawnRank[LIGHT][f] != 0)
			r -= 20;  /* pawn moved more than one square */
		else
			r -= 25;  /* no pawn on this file */

		if (pawnRank[DARK][f] == 7)
			r -= 15;  /* no enemy pawn */
		else if (pawnRank[DARK][f] == 5)
			r -= 10;  /* enemy pawn on the 3rd rank */
		else if (pawnRank[DARK][f] == 4)
			r -= 5;   /* enemy pawn on the 4th rank */

		return r;
	}

	int evalDarkKing(int sq) {
		int r;
		int i;

		r = kingPcsq[flip[sq]];
		if (COL(sq) < 3) {
			r += evalDkp(1);
			r += evalDkp(2);
			r += evalDkp(3) / 2;
		} else if (COL(sq) > 4) {
			r += evalDkp(8);
			r += evalDkp(7);
			r += evalDkp(6) / 2;
		} else {
			for (i = COL(sq); i <= COL(sq) + 2; ++i)
				if ((pawnRank[LIGHT][i] == 0) &&
						(pawnRank[DARK][i] == 7))
					r -= 10;
		}
		r *= pieceMat[LIGHT];
		r /= 3100;
		return r;
	}

	int evalDkp(int f) {
		int r = 0;

		if (pawnRank[DARK][f] == 1) ;
		else if (pawnRank[DARK][f] == 2)
			r -= 10;
		else if (pawnRank[DARK][f] != 7)
			r -= 20;
		else
			r -= 25;

		if (pawnRank[LIGHT][f] == 0)
			r -= 15;
		else if (pawnRank[LIGHT][f] == 2)
			r -= 10;
		else if (pawnRank[LIGHT][f] == 3)
			r -= 5;

		return r;
	}

	public static int COL(int x) {
		return (x & 7);
	}

	public static int ROW(int x) {
		return (x >> 3);
	}

	public static int POS(int c, int r) {
		return 8 * r + c;
	}

	public static int COL(int x, boolean reside) {
		if (reside) x = 63 - x;
		return (x & 7);
	}

	public static int ROW(int x, boolean reside) {
		if (reside) x = 63 - x;
		return (x >> 3);
	}

	public static int POS(int c, int r, boolean reside) {
		if (reside)
			return 63 - (8 * r + c);
		else
			return (8 * r + c);
	}

	public void setReside(boolean reside) {
		/*System.out.println("!!!!!!!! reside current = " + this.reside);
			  System.out.println("!!!!!!!! reside new = " + reside);
			  try
			  {
				throw new Exception();
			  }
			  catch(Exception e)
			  {
				e.printStackTrace();
			  }*/
		this.reside = reside;
	}

	private SoundPlayer getSoundPlayer() {
		return coreActivity.getSoundPlayer();
	}

	public boolean isTacticCanceled() {
		return tacticCanceled;
	}

	public void setTacticCanceled(boolean tacticCanceled) {
		this.tacticCanceled = tacticCanceled;
	}

	public void setTacticMoves(String[] tacticMoves) {
		this.tacticMoves = tacticMoves;
	}

	public String[] getTacticMoves() {
		return tacticMoves;
	}
}
