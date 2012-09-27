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
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.BoardToGameActivityFace;
import org.apache.http.protocol.HTTP;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoard implements BoardFace {
	final public static int LIGHT = 0;
	final public static int DARK = 1;

	// piecesBitmap codes on boardBitmap
	public static final int PAWN = 0;
	public static final int KNIGHT = 1;
	public static final int BISHOP = 2;
	public static final int ROOK = 3;
	public static final int QUEEN = 4;
	public static final int KING = 5;
	public static final int EMPTY = 6;

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
	public static final String EQUALS_N = "=N";
	public static final String EQUALS_B = "=B";
	public static final String EQUALS_R = "=R";
	public static final String EQUALS_Q = "=Q";

	private static ChessBoard instanceLive;
	private static ChessBoard instanceOnline;
	private static ChessBoard instanceComputer;
	private static ChessBoard instanceTactics;

	private Long gameId;
	private Boolean justInitialized;

	private boolean init;
	private boolean chess960;
	private boolean reside;
	private boolean submit;


	private boolean analysis;
	private boolean retry;
	private boolean tacticCanceled;
	private int side = LIGHT;
	private int secondsPassed = 0;
	private int secondsLeft = 0;
	private int tacticsCorrectMoves = 0;
	private String[] tacticMoves;
	private int xside = DARK;
	private int rotated = 0;
	private int ep = -1;  // en passant move
	private int fifty = 0;
	private int movesCount = 0;
	private int hply = 0;
	private int history[][] = new int[64][64];
	private HistoryData[] histDat = new HistoryData[HIST_STACK];
	private int pawnRank[][] = new int[2][10];
	private int pieceMat[] = new int[2];
	private int pawnMat[] = new int[2];


	private int boardcolor[] = {
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


	private int pieces[] = {
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

	private boolean castleMask[] = {false, false, false, false};
	private boolean whiteCanCastle = true;
	private boolean blackCanCastle = true;

	/* the values of the piecesBitmap */
	int pieceValue[] = {
			100, 300, 300, 500, 900, 0
	};

	/* The "pcsq" arrays are piecesBitmap/square tables. They're values
		added to the material value of the piecesBitmap based on the
		location of the piecesBitmap. */

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

	/* The flip array is used to calculate the piecesBitmap/square
		values for DARK piecesBitmap. The piecesBitmap/square value of a
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

	private int mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;

	private int BLACK_ROOK_1_INITIAL_POS = 0;
	private int BLACK_ROOK_2_INITIAL_POS = 7;
	private int WHITE_ROOK_1_INITIAL_POS = 56;
	private int WHITE_ROOK_2_INITIAL_POS = 63;

	private int bRook1 = BLACK_ROOK_1_INITIAL_POS;
	private int bKing = 4;
	private int bRook2 = BLACK_ROOK_2_INITIAL_POS;

	private int wRook1 = WHITE_ROOK_1_INITIAL_POS;
	private int wKing = 60;
	private int wRook2 = WHITE_ROOK_2_INITIAL_POS;

	private int[] bKingMoveOO = new int[]{6};
	private int[] bKingMoveOOO = new int[]{2};

	private int[] wKingMoveOO = new int[]{62};
	private int[] wKingMoveOOO = new int[]{58};

	//private boolean userColorWhite;
	private BoardToGameActivityFace gameActivityFace;
	private SoundPlayer soundPlayer;

	// todo: should be changed to private after refactoring for another boards: Echess, Tactics
	public ChessBoard(BoardToGameActivityFace gameActivityFace) {
		this.gameActivityFace = gameActivityFace;
		soundPlayer = gameActivityFace.getSoundPlayer();
	}

	private static ChessBoard getInstance(BoardToGameActivityFace gameActivityFace, ChessBoard instance) {
		final Long gameId = gameActivityFace.getGameId();
		if (instance == null || !instance.gameId.equals(gameId)) {
			instance = new ChessBoard(gameActivityFace);
			instance.gameId = gameId;
			instance.setInit(true);
			instance.genCastlePos(AppConstants.DEFAULT_GAMEBOARD_CASTLE);
		}
		return instance;
	}

	public static ChessBoard getInstanceLive(BoardToGameActivityFace gameActivityFace) {
		return getInstance(gameActivityFace, instanceLive);
	}

	public static ChessBoard getInstanceOnline(BoardToGameActivityFace gameActivityFace) {
		return getInstance(gameActivityFace, instanceOnline);
	}

	public static ChessBoard getInstanceComputer(BoardToGameActivityFace gameActivityFace) {
		return getInstance(gameActivityFace, instanceLive);
	}

	public static ChessBoard getInstanceTactics(BoardToGameActivityFace gameActivityFace) {
		final Long gameId = gameActivityFace.getGameId();
		if (instanceTactics == null || instanceTactics.gameId == null || !instanceTactics.gameId.equals(gameId)) {
			instanceTactics = new ChessBoard(gameActivityFace);
			instanceTactics.gameId = gameId;
			instanceTactics.justInitialized = true;
		} else {
			instanceTactics.justInitialized = false;
		}
		return instanceTactics;
	}

	public void resetCastlePos() {
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

	@Override
	public int[] genCastlePos(String fen) {
		//rnbqk2r/pppp1ppp/5n2/4P3/1bB2p2/2N5/PPPP2PP/R1BQK1NR
		String[] tmp = fen.split(" ");

		// set castle masks
		if (tmp.length > 2) { //0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
			String castling = tmp[2].trim();
			if (!castling.contains(MoveParser.WHITE_KING)) {
				castleMask[2] = true;
			}
			if (!castling.contains(MoveParser.WHITE_QUEEN)) {
				castleMask[3] = true;
			}

			if (!castling.contains(MoveParser.WHITE_KING) && !castling.contains(MoveParser.WHITE_QUEEN)) {
				whiteCanCastle = false;
			}

			if (!castling.contains(MoveParser.BLACK_KING)) {
				castleMask[0] = true;
			}
			if (!castling.contains(MoveParser.BLACK_QUEEN)) {
				castleMask[1] = true;
			}

			if (!castling.contains(MoveParser.BLACK_KING) && !castling.contains(MoveParser.BLACK_QUEEN)) {
				blackCanCastle = false;
			}

			Log.d(fen, StaticData.SYMBOL_EMPTY + castleMask[2] + castleMask[3] + castleMask[0] + castleMask[1]);
		}

		String[] FEN = tmp[0].split("[/]");
		int offset = 0, i;
		boolean found = false;
		for (i = 0; i < FEN[0].length(); i++) {
			if (FEN[0].charAt(i) == 'r') {
				if (!found) {
					bRook1 = i + offset;
					BLACK_ROOK_1_INITIAL_POS = i + offset;
				} else {
					bRook2 = i + offset;
					BLACK_ROOK_2_INITIAL_POS = i + offset;
				}
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
				if (!found) {
					wRook1 = i + offset;
					WHITE_ROOK_1_INITIAL_POS = i + offset;
				} else {
					wRook2 = i + offset;
					WHITE_ROOK_2_INITIAL_POS = i + offset;
				}
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

		//white KingSide castling O-O
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
		//white QueenSide castling O-O-O
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


	@Override
	public int getColor(int i, int j) {
		return color[(i << 3) + j];
	}

//	@Override
//	public int getPiece(int i, int j) {
//		return pieces[(i << 3) + j];
//	}

	public boolean isWhiteToMove() {
		return (side == LIGHT);
	}

	/* inCheck() returns true if side s is in check and false
		otherwise. It just scans the boardBitmap to find side s's king
		and calls attack() to see if it's being attacked. */

	@Override
	public boolean inCheck(int s) {
		int i;

		for (i = 0; i < 64; ++i)
			if (pieces[i] == KING && color[i] == s)
				return attack(i, s ^ 1);
		return true;  /* shouldn't get here */
	}


	/* attack() returns true if square sq is being attacked by side
		s and false otherwise. */

	boolean attack(int sq, int s) {
		int i, j, n;

		for (i = 0; i < 64; ++i)
			if (color[i] == s) {
				int p = pieces[i];
				if (p == PAWN) {
					if (s == LIGHT) {
						if (getColumn(i) != 0 && i - 9 == sq)
							return true;
						if (getColumn(i) != 7 && i - 7 == sq)
							return true;
					} else {
						if (getColumn(i) != 0 && i + 7 == sq)
							return true;
						if (getColumn(i) != 7 && i + 9 == sq)
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
		It scans the boardBitmap to find friendly piecesBitmap and then determines
		what squares they attack. When it finds a piecesBitmap/square
		combination, it calls genPush to put the move on the "move
		stack." */

	@Override
	public TreeSet<Move> gen() {
		TreeSet<Move> ret = new TreeSet<Move>();

		for (int i = 0; i < 64; ++i){
			if (color[i] == side) {
				if (pieces[i] == PAWN) {
					if (side == LIGHT) {
						if (getColumn(i) != 0 && color[i - 9] == DARK)
							genPush(ret, i, i - 9, 17);
						if (getColumn(i) != 7 && color[i - 7] == DARK)
							genPush(ret, i, i - 7, 17);
						if (color[i - 8] == EMPTY) {
							genPush(ret, i, i - 8, 16);
							if (i >= 48 && color[i - 16] == EMPTY)
								genPush(ret, i, i - 16, 24);
						}
					} else {
						if (getColumn(i) != 0 && color[i + 7] == LIGHT)
							genPush(ret, i, i + 7, 17);
						if (getColumn(i) != 7 && color[i + 9] == LIGHT)
							genPush(ret, i, i + 9, 17);
						if (color[i + 8] == EMPTY) {
							genPush(ret, i, i + 8, 16);
							if (i <= 15 && color[i + 16] == EMPTY)
								genPush(ret, i, i + 16, 24);
						}
					}
				} else if (pieces[i] < offsets.length){
					for (int j = 0; j < offsets[pieces[i]]; ++j){
						for (int n = i; ; ) {
							n = mailbox[mailbox64[n] + offset[pieces[i]][j]];
							if (n == -1)
								break;
							if (color[n] != EMPTY) {
								if (color[n] == xside)
									genPush(ret, i, n, 1);
								break;
							}
							genPush(ret, i, n, 0);
							if (!slide[pieces[i]])
								break;
						}
                    }
                }
			}
        }

		/* generate castle moves */
		int i;
		//0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
		if (side == LIGHT) {
			if (!castleMask[2] && whiteCanCastle) {
				for (i = 0; i < wKingMoveOO.length; i++)
					genPush(ret, wKing, wKingMoveOO[i], 2);
			}
			if (!castleMask[3] && whiteCanCastle) {
				for (i = 0; i < wKingMoveOOO.length; i++)
					genPush(ret, wKing, wKingMoveOOO[i], 2);
			}
		} else {
			if (!castleMask[0] && blackCanCastle) {
				for (i = 0; i < bKingMoveOO.length; i++)
					genPush(ret, bKing, bKingMoveOO[i], 2);
			}
			if (!castleMask[1] && blackCanCastle) {
				for (i = 0; i < bKingMoveOOO.length; i++)
					genPush(ret, bKing, bKingMoveOOO[i], 2);
			}
		}

		/* generate en passant moves */
		if (ep != -1) {
			if (side == LIGHT) {
				if (getColumn(ep) != 0 && color[ep + 7] == LIGHT && pieces[ep + 7] == PAWN){
					genPush(ret, ep + 7, ep, 21);
                }
				if (getColumn(ep) != 7 && color[ep + 9] == LIGHT && pieces[ep + 9] == PAWN){
					genPush(ret, ep + 9, ep, 21);
                }
			} else {
				if (getColumn(ep) != 0 && color[ep - 9] == DARK && pieces[ep - 9] == PAWN){
					genPush(ret, ep - 9, ep, 21);
                }
				if (getColumn(ep) != 7 && color[ep - 7] == DARK && pieces[ep - 7] == PAWN) {
                    genPush(ret, ep - 7, ep, 21);
                }
			}
		}
		return ret;
	}


/* genCaps() is basically a copy of gen() that's modified to
   only generate capture and promote moves. It's used by the
   quiescence search. */

	@Override
	public TreeSet<Move> genCaps() {
		TreeSet<Move> ret = new TreeSet<Move>();

		for (int i = 0; i < 64; ++i)
			if (color[i] == side) {
				if (pieces[i] == PAWN) {
					if (side == LIGHT) {
						if (getColumn(i) != 0 && color[i - 9] == DARK)
							genPush(ret, i, i - 9, 17);
						if (getColumn(i) != 7 && color[i - 7] == DARK)
							genPush(ret, i, i - 7, 17);
						if (i <= 15 && color[i - 8] == EMPTY)
							genPush(ret, i, i - 8, 16);
					}
					if (side == DARK) {
						if (getColumn(i) != 0 && color[i + 7] == LIGHT)
							genPush(ret, i, i + 7, 17);
						if (getColumn(i) != 7 && color[i + 9] == LIGHT)
							genPush(ret, i, i + 9, 17);
						if (i >= 48 && color[i + 8] == EMPTY)
							genPush(ret, i, i + 8, 16);
					}
				} else if (pieces[i] < offsets.length)
					for (int j = 0; j < offsets[pieces[i]]; ++j)
						for (int n = i; ; ) {
							n = mailbox[mailbox64[n] + offset[pieces[i]][j]];
							if (n == -1)
								break;
							if (color[n] != EMPTY) {
								if (color[n] == xside)
									genPush(ret, i, n, 1);
								break;
							}
							if (!slide[pieces[i]])
								break;
						}
			}
		if (ep != -1) {
			if (side == LIGHT) {
				if (getColumn(ep) != 0 && color[ep + 7] == LIGHT && pieces[ep + 7] == PAWN)
					genPush(ret, ep + 7, ep, 21);
				if (getColumn(ep) != 7 && color[ep + 9] == LIGHT && pieces[ep + 9] == PAWN)
					genPush(ret, ep + 9, ep, 21);
			} else {
				if (getColumn(ep) != 0 && color[ep - 9] == DARK && pieces[ep - 9] == PAWN)
					genPush(ret, ep - 9, ep, 21);
				if (getColumn(ep) != 7 && color[ep - 7] == DARK && pieces[ep - 7] == PAWN)
					genPush(ret, ep - 7, ep, 21);
			}
		}
		return ret;
	}


	/**
	 *  Puts a move on the move stack, unless it's a
	 * pawn promotion that needs to be handled by genPromote().
	 * It also assigns a score to the move for alpha-beta move
	 * ordering. If the move is a capture, it uses MVV/LVA
	 * (Most Valuable Victim/Least Valuable Attacker). Otherwise,
	 * it uses the move's history heuristic value. Note that
	 * 1,000,000 is added to a capture move's score, so it
	 * always gets ordered above a "normal" move.
	 * @param ret
	 * @param from
	 * @param to
	 * @param bits
	 */
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
			g.setScore(1000000 + (pieces[to] * 10) - pieces[from]);
		else
			g.setScore(history[from][to]);
		ret.add(g);
	}


	/* genPromote() is just like genPush(), only it puts 4 moves
		on the move stack, one for each possible promotion piecesBitmap */

	void genPromote(TreeSet<Move> ret, int from, int to, int bits) {
		for (char i = KNIGHT; i <= QUEEN; ++i) {
			Move g = new Move(from, to, i, (bits | 32));
			g.setScore(1000000 + (i * 10));
			ret.add(g);
		}
	}

	@Override
	public void updateMoves(String newMove, boolean playSound) {
		int[] moveFT = MoveParser.parse(this, newMove);
		if (moveFT.length == 4) {
			Move move;
			if (moveFT[3] == 2)
				move = new Move(moveFT[0], moveFT[1], 0, 2);
			else
				move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);

			makeMove(move, playSound);
		} else {
			Move move = new Move(moveFT[0], moveFT[1], 0, 0);
			makeMove(move, playSound);
		}
	}


	/* makemove() makes a move. If the move is illegal, it
			undoes whatever it did and returns false. Otherwise, it
			returns true. */

	@Override
	public boolean makeMove(Move m) {
		return makeMove(m, true);
	}

	@Override
	public boolean makeMove(Move move, boolean playSound) {

		/* test to see if a castle move is legal and move the rook
			   (the king is moved with the usual move code later) */
		int what = -1; //0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
		int kingToRookDistance;
		int kingDistance;
		if ((move.bits & 2) != 0) {
			int from = -1, to = -1;

			int[] piece_tmp = pieces.clone();

			if (inCheck(side))
				return false;

			kingToRookDistance = Math.abs(move.from - move.to);
			int minMove = move.to;
			if (move.from < move.to) minMove = move.from;

			int i;
			i = 0;
			while (i < bKingMoveOO.length) {
				if (bKingMoveOO[i] == move.to) {
					what = 0;
					kingToRookDistance = Math.abs(move.from - bRook2);
					minMove = bRook2;
					if (move.from < bRook2) minMove = move.from;
					break;
				}
				i++;
			}
			i = 0;
			while (i < bKingMoveOOO.length) {
				if (bKingMoveOOO[i] == move.to) {
					what = 1;
					kingToRookDistance = Math.abs(move.from - bRook1);
					minMove = bRook1;
					if (move.from < bRook1) minMove = move.from;
					break;
				}
				i++;
			}
			i = 0;
			while (i < wKingMoveOO.length) {
				if (wKingMoveOO[i] == move.to) {
					what = 2;
					kingToRookDistance = Math.abs(move.from - wRook2);
					minMove = wRook2;
					if (move.from < wRook2)
						minMove = move.from;
					break;
				}
				i++;
			}
			i = 0;
			while (i < wKingMoveOOO.length) {
				if (wKingMoveOOO[i] == move.to) {
					what = 3;
					kingToRookDistance = Math.abs(move.from - wRook1);
					minMove = wRook1;
					if (move.from < wRook1)
						minMove = move.from;
					break;
				}
				i++;
			}

			if (castleMask[what])
				return false;

			if (what == 2) {

				kingDistance = Math.abs(wKing - G1);
				int minimalSquare = Math.min(wKing, G1);
				for (int j = 0; j <= kingDistance; j++) {
					if (attack(minimalSquare + j, xside)) {
						return false;
					}
				}

				if (color[F1] != EMPTY && pieces[F1] != KING && pieces[F1] != ROOK)
					return false;
				if (color[G1] != EMPTY && pieces[G1] != KING && pieces[G1] != ROOK)
					return false;
				if (pieces[F1] == ROOK && F1 != wRook2)
					return false;
				if (pieces[G1] == ROOK && G1 != wRook2)
					return false;


				if (kingToRookDistance > 1) {
					while (kingToRookDistance != 0) {
						minMove++;
						if (minMove != wRook2 && pieces[minMove] != KING && color[minMove] != EMPTY) {
							return false;
						}
						kingToRookDistance--;
					}
				}

				from = wRook2;
				to = F1;
			} else if (what == 3) {

				kingDistance = Math.abs(wKing - C1);
				int minimalSquare = Math.min(wKing, C1);
				for (int j = 0; j <= kingDistance; j++) {
					if (attack(minimalSquare + j, xside)) {
						return false;
					}
				}

				if (color[C1] != EMPTY && pieces[C1] != KING && pieces[C1] != ROOK)
					return false;
				if (color[D1] != EMPTY && pieces[D1] != KING && pieces[D1] != ROOK)
					return false;
				if (pieces[C1] == ROOK && C1 != wRook1)
					return false;
				if (pieces[D1] == ROOK && D1 != wRook1)
					return false;

				if (kingToRookDistance > 1) {
					while (kingToRookDistance != 0) {
						minMove++;
						if (minMove != wRook1 && pieces[minMove] != KING && color[minMove] != EMPTY) {
							return false;
						}
						kingToRookDistance--;
					}
				}

				from = wRook1;
				to = D1;
			} else if (what == 1) {

				kingDistance = Math.abs(bKing - C8);
				int minimalSquare = Math.min(bKing, C8);
				for (int j = 0; j <= kingDistance; j++) {
					if (attack(minimalSquare + j, xside)) {
						return false;
					}
				}

				if (color[C8] != EMPTY && pieces[C8] != KING && pieces[C8] != ROOK)
					return false;
				if (color[D8] != EMPTY && pieces[D8] != KING && pieces[D8] != ROOK)
					return false;
				if (pieces[C8] == ROOK && C8 != bRook1)
					return false;
				if (pieces[D8] == ROOK && D8 != bRook1)
					return false;

				if (kingToRookDistance > 1) {
					while (kingToRookDistance != 0) {
						minMove++;
						if (minMove != bRook1 && pieces[minMove] != KING && color[minMove] != EMPTY) {
							return false;
						}
						kingToRookDistance--;
					}
				}

				from = bRook1;
				to = D8;
			} else if (what == 0) {

				kingDistance = Math.abs(bKing - G8);
				int minimalSquare = Math.min(bKing, G8);
				for (int j = 0; j <= kingDistance; j++) {
					if (attack(minimalSquare + j, xside)) {
						return false;
					}
				}

				if (color[F8] != EMPTY && pieces[F8] != KING && pieces[F8] != ROOK)
					return false;
				if (color[G8] != EMPTY && pieces[G8] != KING && pieces[G8] != ROOK)
					return false;

				if (pieces[F8] == ROOK && bRook2 != F8)
					return false;
				if (pieces[G8] == ROOK && bRook2 != G8)
					return false;

				if (kingToRookDistance > 1) {
					while (kingToRookDistance != 0) {
						minMove++;
						if (minMove != bRook2 && pieces[minMove] != KING && color[minMove] != EMPTY) {
							return false;
						}
						kingToRookDistance--;
					}
				}

				from = bRook2;
				to = F8;
			}

			color[to] = color[from];
			pieces[to] = pieces[from];
			if (to != from) {
				color[from] = EMPTY;
				pieces[from] = EMPTY;
			}

			/* back up information so we can take the move back later. */
			histDat[hply] = new HistoryData();
			histDat[hply].move = move;
			histDat[hply].capture = pieces[move.to];
			histDat[hply].ep = ep;
			histDat[hply].fifty = fifty;
			histDat[hply].castleMask = castleMask.clone();
			histDat[hply].whiteCanCastle = whiteCanCastle;
			histDat[hply].blackCanCastle = blackCanCastle;
			histDat[hply].what = what;
			if (what == 0 || what == 2)
				histDat[hply].notation = MoveParser.KINGSIDE_CASTLING;
			else
				histDat[hply].notation = MoveParser.QUEENSIDE_CASTLING;
			++hply;

			/* update the castle, en passant, and
					   fifty-move-draw variables */
			if (what != -1) {
				castleMask[what] = true;
				if (what == 0 || what == 1) {
					blackCanCastle = false;
				} else if (what == 2 || what == 3) {
					whiteCanCastle = false;
				}
			}
			if (pieces[move.from] == KING) {
				if (side == DARK) {
					castleMask[0] = true;
					castleMask[1] = true;
					blackCanCastle = false;
				} else {
					castleMask[2] = true;
					castleMask[3] = true;
					whiteCanCastle = false;
				}
			}
			//0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
			if (pieces[move.from] == ROOK) {
				if (side == DARK) {
					if (move.from == bRook2) {
						castleMask[0] = true;
						if (castleMask[1]) {
							blackCanCastle = false;
						}
					}
					if (move.from == bRook1) {
						castleMask[1] = true;
						if (castleMask[0]) {
							blackCanCastle = false;
						}
					}
				} else {
					if (move.from == wRook2) {
						castleMask[2] = true;
						if (castleMask[3]) {
							whiteCanCastle = false;
						}
					}
					if (move.from == wRook1) {
						castleMask[3] = true;
						if (castleMask[2]) {
							whiteCanCastle = false;
						}
					}
				}
			}

			if ((move.bits & 8) != 0) {
				if (side == LIGHT)
					ep = move.to + 8;
				else
					ep = move.to - 8;
			} else
				ep = -1;
			if ((move.bits & 17) != 0)
				fifty = 0;
			else
				++fifty;

			/* move the piecesBitmap */
			int tmp_to = -1;
			if (what == 3) {
				color[58] = side;
				pieces[58] = piece_tmp[ move.from];
				tmp_to = 58;
			} else if (what == 2) {
				color[62] = side;
				pieces[62] = piece_tmp[move.from];
				tmp_to = 62;
			} else if (what == 1) {
				color[2] = side;
				pieces[2] = piece_tmp[ move.from];
				tmp_to = 2;
			} else if (what == 0) {
				color[6] = side;
				pieces[6] = piece_tmp[move.from];
				tmp_to = 6;
			}
			if (pieces[move.from] != ROOK && tmp_to != move.from) {
				color[move.from] = EMPTY;
				pieces[move.from] = EMPTY;
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
				soundPlayer.playCastle();
			}

			return true;
		}

		/* back up information so we can take the move back later. */
		histDat[hply] = new HistoryData();
//		Log.d("TEST_MOVE", " __________ makeMove___________________");
//		Log.d("TEST_MOVE", "new HistoryData = " + histDat[hply] + ", hply = " + hply + ", move = "+ move);
		histDat[hply].move = move;
		histDat[hply].capture = pieces[move.to];
		histDat[hply].ep = ep;
		histDat[hply].fifty = fifty;
		histDat[hply].castleMask = castleMask.clone();
		histDat[hply].whiteCanCastle = whiteCanCastle;
		histDat[hply].blackCanCastle = blackCanCastle;
		histDat[hply].what = what;
		histDat[hply].notation = getMoveSAN();
		++hply;

		/* update the castle, en passant, and
			   fifty-move-draw variables */
		if (what != -1) {
			castleMask[what] = true;
			if (what == 0 || what == 1) {
				blackCanCastle = false;
			} else if (what == 2 || what == 3) {
				whiteCanCastle = false;
			}
		}
		if (pieces[move.from] == KING) {
			if (side == DARK) {
				castleMask[0] = true;
				castleMask[1] = true;
				blackCanCastle = false;
			} else {
				castleMask[2] = true;
				castleMask[3] = true;
				whiteCanCastle = false;
			}
		}
		//0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
		if (pieces[move.from] == ROOK) {
			if (side == DARK) {
				if (move.from == bRook2) {
					castleMask[0] = true;
					if (castleMask[1]) {
						blackCanCastle = false;
					}
				}
				if (move.from == bRook1) {
					castleMask[1] = true;
					if (castleMask[0]) {
						blackCanCastle = false;
					}
				}
			} else {
				if (move.from == wRook2) {
					castleMask[2] = true;
					if (castleMask[3]) {
						whiteCanCastle = false;
					}
				}
				if (move.from == wRook1) {
					castleMask[3] = true;
					if (castleMask[2]) {
						whiteCanCastle = false;
					}
				}
			}
		}

		// attacked rook
		if (move.to == BLACK_ROOK_1_INITIAL_POS && !castleMask[1]) // q (fen castle)
		{
			castleMask[1] = true;
			if (castleMask[0]) {
				blackCanCastle = false;
			}
		}
		if (move.to == BLACK_ROOK_2_INITIAL_POS && !castleMask[0]) // k (fen castle)
		{
			castleMask[0] = true;
			if (castleMask[1]) {
				blackCanCastle = false;
			}
		}
		if (move.to == WHITE_ROOK_1_INITIAL_POS && !castleMask[3]) // Q (fen castle)
		{
			castleMask[3] = true;
			if (castleMask[2]) {
				whiteCanCastle = false;
			}
		}
		if (move.to == WHITE_ROOK_2_INITIAL_POS && !castleMask[2]) // K (fen castle)
		{
			castleMask[2] = true;
			if (castleMask[3]) {
				whiteCanCastle = false;
			}
		}

		if ((move.bits & 8) != 0) {
			if (side == LIGHT)
				ep = move.to + 8;
			else
				ep = move.to - 8;
		} else
			ep = -1;
		if ((move.bits & 17) != 0)
			fifty = 0;
		else
			++fifty;

		/* move the piece */
		int colorFrom = color[move.from];
		int pieceTo = pieces[move.to];

		color[move.to] = side;

		if ((move.bits & 32) != 0) {
			pieces[move.to] = move.promote;
			//System.out.println("!!!!!!!! PROMOTION");
		} else {
			pieces[move.to] = pieces[move.from];
		}

		color[move.from] = EMPTY;
		pieces[move.from] = EMPTY;

		/* erase the pawn if this is an en passant move */
		if ((move.bits & 4) != 0) {
			if (side == LIGHT) {
				color[move.to + 8] = EMPTY;
				pieces[move.to + 8] = EMPTY;
			} else {
				color[move.to - 8] = EMPTY;
				pieces[move.to - 8] = EMPTY;
			}
		}

		/* switch sides and test for legality (if we can capture
			   the other guy's king, it's an illegal position and
			   we need to take the move back) */
		side ^= 1;
		xside ^= 1;

		Boolean userColorWhite = gameActivityFace.isUserColorWhite();
		if (playSound && userColorWhite != null) {
			if ((userColorWhite && colorFrom == 1) || (!userColorWhite && colorFrom == 0)) {
				if (inCheck(side)) {
					soundPlayer.playMoveOpponentCheck();
				} else if (pieceTo != 6) {
					soundPlayer.playCapture();
				} else {
					soundPlayer.playMoveOpponent();
				}
			} else if ((userColorWhite && colorFrom == 0) || (!userColorWhite && colorFrom == 1)) {
				if (inCheck(side)) {
					soundPlayer.playMoveSelfCheck();
				} else if (pieceTo != 6) {
					soundPlayer.playCapture();
				} else {
					soundPlayer.playMoveSelf();
				}
			}
		}

		if (inCheck(xside)) {
			takeBack();
			return false;
		}
		return true;
	}

	/**
	 * takeBack() is very similar to makeMove(), only backwards :)
	 */
	@Override
	public void takeBack() {
        if (hply - 1 < 0)
            return;

		side ^= 1;
		xside ^= 1;
		--hply;
//		Log.d("TEST_MOVE", " _________________________________________");
//		Log.d("TEST_MOVE", " taking BACK move = " + histDat[hply].move);
		Move move = histDat[hply].move;
		ep = histDat[hply].ep;
		fifty = histDat[hply].fifty;
		castleMask = histDat[hply].castleMask.clone();
		whiteCanCastle = histDat[hply].whiteCanCastle;
		blackCanCastle = histDat[hply].blackCanCastle;

		if ((move.bits & 2) != 0) {

			int[] piece_tmp = pieces.clone();

			int i;
			int what = -1; //0 - b O-O; 1 - b O-O-O; 2 - w O-O; 3 - w O-O-O;
			for (i = 0; i < bKingMoveOO.length; i++) {
				if (bKingMoveOO[i] == move.to)
					what = 0;
			}
			for (i = 0; i < bKingMoveOOO.length; i++) {
				if (bKingMoveOOO[i] == move.to)
					what = 1;
			}
			for (i = 0; i < wKingMoveOO.length; i++) {
				if (wKingMoveOO[i] == move.to)
					what = 2;
			}
			for (i = 0; i < wKingMoveOOO.length; i++) {
				if (wKingMoveOOO[i] == move.to)
					what = 3;
			}
			int to = move.to;
			int pt = pieces[to];
			if (what == 3) {
				pt = pieces[58];
				to = 58;
			} else if (what == 2) {
				pt = pieces[62];
				to = 62;
			} else if (what == 1) {
				pt = pieces[2];
				to = 2;
			} else if (what == 0) {
				pt = pieces[6];
				to = 6;
			}
			/* move the piecesBitmap */
			color[move.from] = side;
			pieces[move.from] = pt;
			if (move.from != to) {
				color[to] = EMPTY;
				pieces[to] = EMPTY;
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
			pieces[from] = piece_tmp[to];
			if (to != from && pieces[to] != KING) {
				color[to] = EMPTY;
				pieces[to] = EMPTY;
			}

			return;
		}


		color[move.from] = side;
		if ((move.bits & 32) != 0) {
            pieces[move.from] = PAWN;
        } else {
            pieces[move.from] = pieces[move.to];
        }
		if (histDat[hply].capture == EMPTY) {
			color[move.to] = EMPTY;
			pieces[move.to] = EMPTY;
		} else {
			color[move.to] = xside;
			pieces[move.to] = histDat[hply].capture;
		}
		if ((move.bits & 4) != 0) {
			if (side == LIGHT) {
				color[move.to + 8] = xside;
				pieces[move.to + 8] = PAWN;
			} else {
				color[move.to - 8] = xside;
				pieces[move.to - 8] = PAWN;
			}
		}
	}

	@Override
	public void takeNext() {
//		Log.d("TEST_MOVE", " takeNext hply + 1 = " + (hply + 1)
//				+ " <= movesCount = " + (hply + 1 <= movesCount) + " movesCount = " + movesCount);
		if (hply + 1 <= movesCount) {
//			Log.d("TEST_MOVE", " histDat[hply] = " + histDat[hply]);
			if(histDat[hply] == null) // TODO find real problem
				return;

//			Log.d("TEST_MOVE", " taking NEXT move = " + histDat[hply].move);
			makeMove(histDat[hply].move);
		}
	}

	@Override
	public boolean isLatestMoveMadeUser() {
		return hply > 0 && hply %2 == 0;
	}

	public String getMoveList() {
		String output = StaticData.SYMBOL_EMPTY;
		int i;
		for (i = 0; i < hply; i++) {
			Move m = histDat[i].move;
			if (i % 2 == 0)
				output += "\n" + (i / 2 + 1) + ". ";
			output += MoveParser.positionToString(m.from);
			output += MoveParser.positionToString(m.to);
			output += " ";
		}
		return output;
	}

	@Override
	public String getMoveListSAN() {
		String output = StaticData.SYMBOL_EMPTY;
		int i;
		for (i = 0; i < hply; i++) {
			if (i % 2 == 0)
				output += "\n " + (i / 2 + 1) + ". ";
			output += histDat[i].notation;
			output += StaticData.SYMBOL_SPACE;
		}
		return output;
	}

	public String getMoveSAN() {
		Move move = histDat[hply].move;
		int piece = pieces[move.from];
		String f = StaticData.SYMBOL_EMPTY;
		String capture = StaticData.SYMBOL_EMPTY;
		String promotion = StaticData.SYMBOL_EMPTY;
		if (piece == 1) {
			f = MoveParser.WHITE_KNIGHT;
			//ambigues
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
			//ambigues
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

		if (histDat[hply].capture != 6) {
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
		Move move = new Move(0,0,0,0);
		try {
			move = histDat[hply - 1].move;
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


		String output = StaticData.SYMBOL_EMPTY;
		try {
			String to = MoveParser.positionToString(move.to);
			if ((move.bits & 2) != 0) {
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
			output = URLEncoder.encode(MoveParser.positionToString(move.from) + to, HTTP.UTF_8);
		} catch (Exception ignored) {
		}
		Log.d("move:", output);
		return output;
	}

	@Override
	public String convertMoveEchess() {
		String output = convertMove();
		final Move m = histDat[hply - 1].move;
		switch (m.promote) {
			case ChessBoard.KNIGHT:
				output += (color[m.from] == 0 ? EQUALS_N : "=n");
				break;
			case ChessBoard.BISHOP:
				output += (color[m.from] == 0 ? EQUALS_B : "=b");
				break;
			case ChessBoard.ROOK:
				output += (color[m.from] == 0 ? EQUALS_R : "=r");
				break;
			case ChessBoard.QUEEN:
				output += (color[m.from] == 0 ? EQUALS_Q : "=q");
				break;
			default:
				break;
		}
		return output;
	}

	@Override
	public String convertMoveLive() {
		String output = convertMove();
		final Move m = histDat[hply - 1].move;
		switch (m.promote) {
			case ChessBoard.KNIGHT:
				output += 'n';
				break;
			case ChessBoard.BISHOP:
				output += 'b';
				break;
			case ChessBoard.ROOK:
				output += 'r';
				break;
			case ChessBoard.QUEEN:
				output += 'q';
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
				case LIGHT:
					sb.append(" ");
					sb.append(pieceChar[pieces[i]]);
					break;
				case DARK:
					sb.append(" ");
					sb.append((char) (pieceChar[pieces[i]] + ('a' - 'A')));
					break;
				default:
					throw new IllegalStateException("Square not EMPTY, LIGHT or DARK: " + i);
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

	/* reps() returns the number of times that the current
		position has been repeated. Thanks to John Stanback
		for this clever algorithm. */

	@Override
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

			if (++b[histDat[i].move.from] == 0)
				--c;
			else
				++c;
			if (--b[histDat[i].move.to] == 0)
				--c;
			else
				++c;
			if (c == 0)
				++r;
		}

		return r;
	}

	@Override
	public int eval() {
		int score[] = new int[2];  /* each side's score */

		/* this is the first pass: set up pawnRank, pieceMat, and pawnMat. */
		int i;
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
			if (pieces[i] == PAWN) {
				pawnMat[color[i]] += pieceValue[PAWN];
				int f = getColumn(i) + 1;  /* add 1 because of the extra file in the array */
				if (color[i] == LIGHT) {
					if (pawnRank[LIGHT][f] < getRow(i))
						pawnRank[LIGHT][f] = getRow(i);
				} else {
					if (pawnRank[DARK][f] > getRow(i))
						pawnRank[DARK][f] = getRow(i);
				}
			} else {
				try {
					pieceMat[color[i]] += pieceValue[pieces[i]];
				} catch (Exception e) {
					Log.d("I!!!!!!!!:", StaticData.SYMBOL_EMPTY + i);
				}
			}
		}

		/* this is the second pass: evaluate each piecesBitmap */
		score[LIGHT] = pieceMat[LIGHT] + pawnMat[LIGHT];
		score[DARK] = pieceMat[DARK] + pawnMat[DARK];
		for (i = 0; i < 64; ++i) {
			if (color[i] == EMPTY)
				continue;
			if (color[i] == LIGHT) {
				switch (pieces[i]) {
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
						if (pawnRank[LIGHT][getColumn(i) + 1] == 0) {
							if (pawnRank[DARK][getColumn(i) + 1] == 7)
								score[LIGHT] += ROOK_OPEN_FILE_BONUS;
							else
								score[LIGHT] += ROOK_SEMI_OPEN_FILE_BONUS;
						}
						if (getRow(i) == 1)
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
				switch (pieces[i]) {
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
						if (pawnRank[DARK][getColumn(i) + 1] == 7) {
							if (pawnRank[LIGHT][getColumn(i) + 1] == 0)
								score[DARK] += ROOK_OPEN_FILE_BONUS;
							else
								score[DARK] += ROOK_SEMI_OPEN_FILE_BONUS;
						}
						if (getRow(i) == 6)
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
		int f = getColumn(sq) + 1; /* pawn's file */

		r += pawnPcsq[sq];

		/* if there's a pawn behind this one, it's doubled */
		if (pawnRank[LIGHT][f] > getRow(sq))
			r -= DOUBLED_PAWN_PENALTY;

		/* if there aren't any friendly pawns on either side of
			   this one, it's isolated */
		if ((pawnRank[LIGHT][f - 1] == 0) &&
				(pawnRank[LIGHT][f + 1] == 0))
			r -= ISOLATED_PAWN_PENALTY;

			/* if it's not isolated, it might be backwards */
		else if ((pawnRank[LIGHT][f - 1] < getRow(sq)) &&
				(pawnRank[LIGHT][f + 1] < getRow(sq)))
			r -= BACKWARDS_PAWN_PENALTY;

		/* add a bonus if the pawn is passed */
		if ((pawnRank[DARK][f - 1] >= getRow(sq)) &&
				(pawnRank[DARK][f] >= getRow(sq)) &&
				(pawnRank[DARK][f + 1] >= getRow(sq)))
			r += (7 - getRow(sq)) * PASSED_PAWN_BONUS;

		return r;
	}

	int evalDarkPawn(int sq) {
		int r = 0;  /* the value to return */
		int f = getColumn(sq) + 1;  /* the pawn's file */

		r += pawnPcsq[flip[sq]];

		/* if there's a pawn behind this one, it's doubled */
		if (pawnRank[DARK][f] < getRow(sq))
			r -= DOUBLED_PAWN_PENALTY;

		/* if there aren't any friendly pawns on either side of
			   this one, it's isolated */
		if ((pawnRank[DARK][f - 1] == 7) &&
				(pawnRank[DARK][f + 1] == 7))
			r -= ISOLATED_PAWN_PENALTY;

			/* if it's not isolated, it might be backwards */
		else if ((pawnRank[DARK][f - 1] > getRow(sq)) &&
				(pawnRank[DARK][f + 1] > getRow(sq)))
			r -= BACKWARDS_PAWN_PENALTY;

		/* add a bonus if the pawn is passed */
		if ((pawnRank[LIGHT][f - 1] <= getRow(sq)) &&
				(pawnRank[LIGHT][f] <= getRow(sq)) &&
				(pawnRank[LIGHT][f + 1] <= getRow(sq)))
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
		}

		/* otherwise, just assess a penalty if there are open files near
			   the king */
		else {
			for (int i = getColumn(sq); i <= getColumn(sq) + 2; ++i)
				if ((pawnRank[LIGHT][i] == 0) &&
						(pawnRank[DARK][i] == 7))
					r -= 10;
		}

		/* scale the king safety value according to the opponent's material;
			   the premise is that your king safety can only be bad if the
			   opponent has enough piecesBitmap to attack you */
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
		if (getColumn(sq) < 3) {
			r += evalDkp(1);
			r += evalDkp(2);
			r += evalDkp(3) / 2;
		} else if (getColumn(sq) > 4) {
			r += evalDkp(8);
			r += evalDkp(7);
			r += evalDkp(6) / 2;
		} else {
			for (i = getColumn(sq); i <= getColumn(sq) + 2; ++i)
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

	public static int getColumn(int x) {
		return (x & 7);
	}

	public static int getRow(int x) {
		return (x >> 3);
	}

	public static int getPosition(int c, int r) {
		return 8 * r + c;
	}

	public static int getColumn(int x, boolean reside) {
		if (reside) x = 63 - x;
		return (x & 7);
	}

	public static int getRow(int x, boolean reside) {
		if (reside) x = 63 - x;
		return (x >> 3);
	}

	@Override
	public int[] getColor() {
		return color;
	}

	public void setColor(int[] color) {
		this.color = color;
	}

	public static int getPositionIndex(int c, int r, boolean reside) {
		if (reside)
			return 63 - (8 * r + c);
		else
			return (8 * r + c);
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

	public void setPieces(int[] pieces) {
		this.pieces = pieces;
	}

	@Override
	public int getHply() {
		return hply;
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
	public boolean isRetry() {
		return retry;
	}

	@Override
	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	@Override
	public boolean isInit() {
		return init;
	}

	@Override
	public void setInit(boolean init) {
		this.init = init;
	}

	@Override
	public int getSecondsPassed() {
		return secondsPassed;
	}

	@Override
	public void setSecondsPassed(int secondsPassed) {
		this.secondsPassed = secondsPassed;
	}

	@Override
	public int getSecondsLeft() {
		return secondsLeft;
	}

	@Override
	public void increaseSecondsPassed() {
		secondsPassed++;

		if(secondsLeft > 0)
			secondsLeft--;
	}

	@Override
	public void setSecondsLeft(int secondsLeft) {
		this.secondsLeft = secondsLeft;
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
	public boolean isTacticCanceled() {
		return tacticCanceled;
	}

	@Override
	public void setTacticCanceled(boolean tacticCanceled) {
		this.tacticCanceled = tacticCanceled;
	}

	@Override
	public void setTacticMoves(String tacticMoves) {
		this.tacticMoves = tacticMoves.replaceAll("[0-9]{1,4}[.]", StaticData.SYMBOL_EMPTY)
				.replaceAll("[.]", StaticData.SYMBOL_EMPTY)
				.replaceAll("  ", StaticData.SYMBOL_SPACE)
				.substring(1).split(StaticData.SYMBOL_SPACE);
	}

	@Override
	public String[] getTacticMoves() {
		return tacticMoves;
	}

	@Override
	public boolean toggleAnalysis() {
		return analysis = !analysis;
	}

	@Override
	public boolean lastTacticMoveIsCorrect() {
		int lastIndex = hply - 1;
		Move move = histDat[lastIndex].move; // get last move
		String piece = StaticData.SYMBOL_EMPTY;
		int pieceCode = pieces[move.to];
		if (pieceCode == 1) { // set piece name
			piece = MoveParser.WHITE_KNIGHT;
		} else if (pieceCode == 2) {
			piece = MoveParser.WHITE_BISHOP;
		} else if (pieceCode == 3) {
			piece = MoveParser.WHITE_ROOK;
		} else if (pieceCode == 4) {
			piece = MoveParser.WHITE_QUEEN;
		} else if (pieceCode == 5) {
			piece = MoveParser.WHITE_KING;
		}
		String moveTo = MoveParser.positionToString(move.to);
//		Log.d("TEST_MOVE", "piece " + piece + " | move to " + moveTo + " : tactic last move = " + tacticMoves[lastIndex]);

		return tacticMoves[lastIndex].contains(piece) && tacticMoves[lastIndex].contains(moveTo);
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

	public boolean[] getSlide() {
		return slide;
	}

	public void setSlide(boolean[] slide) {
		this.slide = slide;
	}

	public int[] getOffsets() {
		return offsets;
	}

	public void setOffsets(int[] offsets) {
		this.offsets = offsets;
	}

	public int[][] getOffset() {
		return offset;
	}

	public void setOffset(int[][] offset) {
		this.offset = offset;
	}

	public int getbRook1() {
		return bRook1;
	}

	public void setbRook1(int bRook1) {
		this.bRook1 = bRook1;
	}

	@Override
	public int getbKing() {
		return bKing;
	}

	public void setbKing(int bKing) {
		this.bKing = bKing;
	}

	public int getbRook2() {
		return bRook2;
	}

	public void setbRook2(int bRook2) {
		this.bRook2 = bRook2;
	}

	public int getwRook1() {
		return wRook1;
	}

	public void setwRook1(int wRook1) {
		this.wRook1 = wRook1;
	}

	@Override
	public int getwKing() {
		return wKing;
	}

	public void setwKing(int wKing) {
		this.wKing = wKing;
	}

	public int getwRook2() {
		return wRook2;
	}

	public void setwRook2(int wRook2) {
		this.wRook2 = wRook2;
	}

	@Override
	public int[] getbKingMoveOO() {
		return bKingMoveOO;
	}

	public void setbKingMoveOO(int[] bKingMoveOO) {
		this.bKingMoveOO = bKingMoveOO;
	}

	@Override
	public int[] getbKingMoveOOO() {
		return bKingMoveOOO;
	}

	public void setbKingMoveOOO(int[] bKingMoveOOO) {
		this.bKingMoveOOO = bKingMoveOOO;
	}

	@Override
	public int[] getwKingMoveOO() {
		return wKingMoveOO;
	}

	public void setwKingMoveOO(int[] wKingMoveOO) {
		this.wKingMoveOO = wKingMoveOO;
	}

	@Override
	public int[] getwKingMoveOOO() {
		return wKingMoveOOO;
	}

	public void setwKingMoveOOO(int[] wKingMoveOOO) {
		this.wKingMoveOOO = wKingMoveOOO;
	}

	@Override
	public int[] getBoardColor() {
		return boardcolor;
	}

	public void setBoardcolor(int[] boardcolor) {
		this.boardcolor = boardcolor;
	}

	public boolean[] getCastleMask() {
		return castleMask;
	}

	public void setCastleMask(boolean[] castleMask) {
		this.castleMask = castleMask;
	}

	public boolean isChess960() {
		return chess960;
	}

	@Override
	public void setChess960(boolean chess960) {
		this.chess960 = chess960;
	}

	@Override
	public int[][] getHistory() {
		return history;
	}

	public void setHistory(int[][] history) {
		this.history = history;
	}

	public int[] getPawnMat() {
		return pawnMat;
	}

	public void setPawnMat(int[] pawnMat) {
		this.pawnMat = pawnMat;
	}

	public int[][] getPawnRank() {
		return pawnRank;
	}

	public void setPawnRank(int[][] pawnRank) {
		this.pawnRank = pawnRank;
	}

	public int[] getPieceMat() {
		return pieceMat;
	}

	public void setPieceMat(int[] pieceMat) {
		this.pieceMat = pieceMat;
	}

	public int getRotated() {
		return rotated;
	}

	public void setRotated(int rotated) {
		this.rotated = rotated;
	}

	@Override
	public int getTacticsCorrectMoves() {
		return tacticsCorrectMoves;
	}

	@Override
	public void increaseTacticsCorrectMoves() {
		tacticsCorrectMoves++;
	}


	public void setTacticsCorrectMoves(int tacticsCorrectMoves) {
		this.tacticsCorrectMoves = tacticsCorrectMoves;
	}

	public int getXside() {
		return xside;
	}

	@Override
	public void setXside(int xside) {
		this.xside = xside;
	}

	@Override
	public int getFifty() {
		return fifty;
	}

	public void setFifty(int fifty) {
		this.fifty = fifty;
	}

	public int getEp() {
		return ep;
	}

	public void setEp(int ep) {
		this.ep = ep;
	}

	public boolean isPossibleToMakeMoves() {
		TreeSet<Move> validMoves = gen();

		Iterator<Move> i = validMoves.iterator();
		boolean found = false;
		while (i.hasNext()) {   // compute available moves
			if (makeMove(i.next(), false)) {
				takeBack();
				found = true;
				break;
			}
		}
		return found;
	}

	@Override
	public void setupBoard(String FEN) {
		if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
			genCastlePos(FEN);
			MoveParser.fenParse(FEN, this);
			String[] tmp = FEN.split(StaticData.SYMBOL_SPACE);
			if (tmp.length > 1) {
				if (tmp[1].trim().equals(MoveParser.W_SMALL)) {
					setReside(true);
				}
			}
		}
	}

	public Long getGameId() {
		return gameId;
	}

	private static void resetInstance(ChessBoard instance) {
		instance = null;
	}

	public static void resetInstanceLive() {
		resetInstance(instanceLive);
	}

	public static void resetInstanceOnline() {
		resetInstance(instanceOnline);
	}

	public static void resetInstanceComputer() {
		resetInstance(instanceComputer);
	}

	public static void resetInstanceTactics() {
		resetInstance(instanceTactics);
	}

	public boolean isJustInitialized() {
		return justInitialized;
	}
}
