//
//  Move.java
//
//  Created by Peter Hunter on Mon Dec 31 2001.
//  Copyright (c) 2001 Peter Hunter. All rights reserved.
//
package com.chess.engine;

public final class Move implements Comparable<Object> {
	public int from;
	public int to;
	public int promote;
	public int bits;
	int score = 0;

	public Move(int from, int to, int promote, int bits) {
		this.from = from;
		this.to = to;
		this.promote = promote;
		this.bits = bits;
	}

	int getScore() {
		return score;
	}

	void setScore(int i) {
		score = i;
	}

	public int hashCode() {
		return from + (to << 8) + (promote << 16);
	}

	public boolean equals(Object o) {
		Move m = (Move) o;
		return (m.from == from && m.to == to && m.promote == promote);
	}

	public int compareTo(Object o) {
		Move m = (Move) o;
		int mScore = m.getScore();
		if (score < mScore) return 1;
		if (score > mScore) return -1;
		int mHashCode = m.hashCode();
		int hash = hashCode();
		if (hash > mHashCode) return 1;
		if (hash < mHashCode) return -1;
		return 0;
	}

	public String toString() {
		char c;
		StringBuffer sb = new StringBuffer();

		if ((bits & 32) != 0) {
			switch (promote) {
				case ChessBoard.KNIGHT:
					c = 'n';
					break;
				case ChessBoard.BISHOP:
					c = 'b';
					break;
				case ChessBoard.ROOK:
					c = 'r';
					break;
				default:
					c = 'q';
					break;
			}
			sb.append((char) (ChessBoard.COL(from) + 'a'));
			sb.append(8 - ChessBoard.ROW(from));
			sb.append((char) (ChessBoard.COL(to) + 'a'));
			sb.append(8 - ChessBoard.ROW(to));
			sb.append(c);
		} else {
			sb.append((char) (ChessBoard.COL(from) + 'a'));
			sb.append(8 - ChessBoard.ROW(from));
			sb.append((char) (ChessBoard.COL(to) + 'a'));
			sb.append(8 - ChessBoard.ROW(to));
		}
		return sb.toString();
	}
}