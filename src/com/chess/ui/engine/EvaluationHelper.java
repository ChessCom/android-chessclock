package com.chess.ui.engine;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.09.13
 * Time: 11:29
 */
public class EvaluationHelper {

//	static final int DOUBLED_PAWN_PENALTY = 10;
//	static final int ISOLATED_PAWN_PENALTY = 20;
//	static final int BACKWARDS_PAWN_PENALTY = 8;
//	static final int PASSED_PAWN_BONUS = 20;
//	static final int ROOK_SEMI_OPEN_FILE_BONUS = 10;
//	static final int ROOK_OPEN_FILE_BONUS = 15;
//	static final int ROOK_ON_SEVENTH_BONUS = 20;
//
//	private int pawnRank[][] = new int[2][10];
//	private int pieceMat[] = new int[2];
//	private int pawnMat[] = new int[2];
//
//	/* the values of the piecesBitmap */
//	int pieceValue[] = {
//			100, 300, 300, 500, 900, 0
//	};

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

//	@Override
//	public int eval() {
//		int score[] = new int[2];  /* each side's score */
//
//		/* this is the first pass: set up pawnRank, pieceMat, and pawnMat. */
//		int i;
//		for (i = 0; i < 10; ++i) {
//			pawnRank[WHITE_SIDE][i] = 0;
//			pawnRank[BLACK_SIDE][i] = 7;
//		}
//		pieceMat[WHITE_SIDE] = 0;
//		pieceMat[BLACK_SIDE] = 0;
//		pawnMat[WHITE_SIDE] = 0;
//		pawnMat[BLACK_SIDE] = 0;
//
//		for (i = 0; i < 64; i++) {
//			if (colors[i] == EMPTY)
//				continue;
//			if (pieces[i] == PAWN) {
//				pawnMat[colors[i]] += pieceValue[PAWN];
//				int f = getFile(i) + 1;  /* add 1 because of the extra file in the array */
//				if (colors[i] == WHITE_SIDE) {
//					if (pawnRank[WHITE_SIDE][f] < getRank(i))
//						pawnRank[WHITE_SIDE][f] = getRank(i);
//				} else {
//					if (pawnRank[BLACK_SIDE][f] > getRank(i))
//						pawnRank[BLACK_SIDE][f] = getRank(i);
//				}
//			} else {
//				try {
//					pieceMat[colors[i]] += pieceValue[pieces[i]];
//				} catch (Exception e) {
//					Log.e("I!!!!!!!!:", Symbol.EMPTY + i + Symbol.SPACE + e.toString());
//				}
//			}
//		}
//
//		/* this is the second pass: evaluate each piecesBitmap */
//		score[WHITE_SIDE] = pieceMat[WHITE_SIDE] + pawnMat[WHITE_SIDE];
//		score[BLACK_SIDE] = pieceMat[BLACK_SIDE] + pawnMat[BLACK_SIDE];
//		for (i = 0; i < 64; ++i) {
//			if (colors[i] == EMPTY)
//				continue;
//			if (colors[i] == WHITE_SIDE) {
//				switch (pieces[i]) {
//					case PAWN:
//						score[WHITE_SIDE] += evalLightPawn(i);
//						break;
//					case KNIGHT:
//						score[WHITE_SIDE] += knightPcsq[i];
//						break;
//					case BISHOP:
//						score[WHITE_SIDE] += bishopPcsq[i];
//						break;
//					case ROOK:
//						if (pawnRank[WHITE_SIDE][getFile(i) + 1] == 0) {
//							if (pawnRank[BLACK_SIDE][getFile(i) + 1] == 7)
//								score[WHITE_SIDE] += ROOK_OPEN_FILE_BONUS;
//							else
//								score[WHITE_SIDE] += ROOK_SEMI_OPEN_FILE_BONUS;
//						}
//						if (getRank(i) == 1)
//							score[WHITE_SIDE] += ROOK_ON_SEVENTH_BONUS;
//						break;
//					case KING:
//						if (pieceMat[BLACK_SIDE] <= 1200)
//							score[WHITE_SIDE] += kingEndgamePcsq[i];
//						else
//							score[WHITE_SIDE] += evalLightKing(i);
//						break;
//				}
//			} else {
//				switch (pieces[i]) {
//					case PAWN:
//						score[BLACK_SIDE] += evalDarkPawn(i);
//						break;
//					case KNIGHT:
//						score[BLACK_SIDE] += knightPcsq[flip[i]];
//						break;
//					case BISHOP:
//						score[BLACK_SIDE] += bishopPcsq[flip[i]];
//						break;
//					case ROOK:
//						if (pawnRank[BLACK_SIDE][getFile(i) + 1] == 7) {
//							if (pawnRank[WHITE_SIDE][getFile(i) + 1] == 0)
//								score[BLACK_SIDE] += ROOK_OPEN_FILE_BONUS;
//							else
//								score[BLACK_SIDE] += ROOK_SEMI_OPEN_FILE_BONUS;
//						}
//						if (getRank(i) == 6)
//							score[BLACK_SIDE] += ROOK_ON_SEVENTH_BONUS;
//						break;
//					case KING:
//						if (pieceMat[WHITE_SIDE] <= 1200)
//							score[BLACK_SIDE] += kingEndgamePcsq[flip[i]];
//						else
//							score[BLACK_SIDE] += evalDarkKing(i);
//						break;
//				}
//			}
//		}
//
//		/* the score[] array is set, now return the score relative
//			   to the side to move */
//		if (side == WHITE_SIDE) {
//			return score[WHITE_SIDE] - score[BLACK_SIDE];
//		}
//		return score[BLACK_SIDE] - score[WHITE_SIDE];
//	}

//	int evalLightPawn(int sq) {
//		int r = 0; /* return value */
//		int f = getFile(sq) + 1; /* pawn's file */
//
//		r += pawnPcsq[sq];
//
//		/* if there's a pawn behind this one, it's doubled */
//		if (pawnRank[WHITE_SIDE][f] > getRank(sq))
//			r -= DOUBLED_PAWN_PENALTY;
//
//		/* if there aren't any friendly pawns on either side of
//			   this one, it's isolated */
//		if ((pawnRank[WHITE_SIDE][f - 1] == 0) &&
//				(pawnRank[WHITE_SIDE][f + 1] == 0))
//			r -= ISOLATED_PAWN_PENALTY;
//
//			/* if it's not isolated, it might be backwards */
//		else if ((pawnRank[WHITE_SIDE][f - 1] < getRank(sq)) &&
//				(pawnRank[WHITE_SIDE][f + 1] < getRank(sq)))
//			r -= BACKWARDS_PAWN_PENALTY;
//
//		/* add a bonus if the pawn is passed */
//		if ((pawnRank[BLACK_SIDE][f - 1] >= getRank(sq)) &&
//				(pawnRank[BLACK_SIDE][f] >= getRank(sq)) &&
//				(pawnRank[BLACK_SIDE][f + 1] >= getRank(sq)))
//			r += (7 - getRank(sq)) * PASSED_PAWN_BONUS;
//
//		return r;
//	}

//	int evalDarkPawn(int sq) {
//		int r = 0;  /* the value to return */
//		int f = getFile(sq) + 1;  /* the pawn's file */
//
//		r += pawnPcsq[flip[sq]];
//
//		/* if there's a pawn behind this one, it's doubled */
//		if (pawnRank[BLACK_SIDE][f] < getRank(sq))
//			r -= DOUBLED_PAWN_PENALTY;
//
//		/* if there aren't any friendly pawns on either side of
//			   this one, it's isolated */
//		if ((pawnRank[BLACK_SIDE][f - 1] == 7) &&
//				(pawnRank[BLACK_SIDE][f + 1] == 7))
//			r -= ISOLATED_PAWN_PENALTY;
//
//			/* if it's not isolated, it might be backwards */
//		else if ((pawnRank[BLACK_SIDE][f - 1] > getRank(sq)) &&
//				(pawnRank[BLACK_SIDE][f + 1] > getRank(sq)))
//			r -= BACKWARDS_PAWN_PENALTY;
//
//		/* add a bonus if the pawn is passed */
//		if ((pawnRank[WHITE_SIDE][f - 1] <= getRank(sq)) &&
//				(pawnRank[WHITE_SIDE][f] <= getRank(sq)) &&
//				(pawnRank[WHITE_SIDE][f + 1] <= getRank(sq)))
//			r += getRank(sq) * PASSED_PAWN_BONUS;
//
//		return r;
//	}

//	int evalLightKing(int sq) {
//		int r = kingPcsq[sq]; /* return value */
//
//		/* if the king is castled, use a special function to evaluate the
//			   pawns on the appropriate side */
//		if (getFile(sq) < 3) {
//			r += evalLkp(1);
//			r += evalLkp(2);
//			r += evalLkp(3) / 2;  /* problems with pawns on the c & f files
//																are not as severe */
//		} else if (getFile(sq) > 4) {
//			r += evalLkp(8);
//			r += evalLkp(7);
//			r += evalLkp(6) / 2;
//		} else { // otherwise, just assess a penalty if there are open files near the king
//			for (int i = getFile(sq); i <= getFile(sq) + 2; ++i) {
//				if ((pawnRank[WHITE_SIDE][i] == 0) && (pawnRank[BLACK_SIDE][i] == 7)) {
//					r -= 10;
//				}
//			}
//		}
//
//		/* scale the king safety value according to the opponent's material;
//			   the premise is that your king safety can only be bad if the
//			   opponent has enough piecesBitmap to attack you */
//		r *= pieceMat[BLACK_SIDE];
//		r /= 3100;
//
//		return r;
//	}

//	/**
//	 * Evaluates the Light King Pawn on file f
//	 *
//	 * @param f target file
//	 * @return move rating?
//	 */
//	int evalLkp(int f) {
//		int r = 0;
//
//		if (pawnRank[WHITE_SIDE][f] == 6) ;  /* pawn hasn't moved */
//		else if (pawnRank[WHITE_SIDE][f] == 5)
//			r -= 10;  /* pawn moved one square */
//		else if (pawnRank[WHITE_SIDE][f] != 0)
//			r -= 20;  /* pawn moved more than one square */
//		else
//			r -= 25;  /* no pawn on this file */
//
//		if (pawnRank[BLACK_SIDE][f] == 7)
//			r -= 15;  /* no enemy pawn */
//		else if (pawnRank[BLACK_SIDE][f] == 5)
//			r -= 10;  /* enemy pawn on the 3rd rank */
//		else if (pawnRank[BLACK_SIDE][f] == 4)
//			r -= 5;   /* enemy pawn on the 4th rank */
//
//		return r;
//	}

//	int evalDarkKing(int sq) {
//		int r;
//		int i;
//
//		r = kingPcsq[flip[sq]];
//		if (getFile(sq) < 3) {
//			r += evalDkp(1);
//			r += evalDkp(2);
//			r += evalDkp(3) / 2;
//		} else if (getFile(sq) > 4) {
//			r += evalDkp(8);
//			r += evalDkp(7);
//			r += evalDkp(6) / 2;
//		} else {
//			for (i = getFile(sq); i <= getFile(sq) + 2; ++i) {
//				if ((pawnRank[WHITE_SIDE][i] == 0) && (pawnRank[BLACK_SIDE][i] == 7)) {
//					r -= 10;
//				}
//			}
//		}
//		r *= pieceMat[WHITE_SIDE];
//		r /= 3100;
//		return r;
//	}
//
//	int evalDkp(int f) {
//		int r = 0;
//
//		if (pawnRank[BLACK_SIDE][f] == 1) ;
//		else if (pawnRank[BLACK_SIDE][f] == 2)
//			r -= 10;
//		else if (pawnRank[BLACK_SIDE][f] != 7)
//			r -= 20;
//		else
//			r -= 25;
//
//		if (pawnRank[WHITE_SIDE][f] == 0)
//			r -= 15;
//		else if (pawnRank[WHITE_SIDE][f] == 2)
//			r -= 10;
//		else if (pawnRank[WHITE_SIDE][f] == 3)
//			r -= 5;
//
//		return r;
//	}
}
