//
//  Move.java
//
//  Created by Peter Hunter on Mon Dec 31 2001.
//  Copyright (c) 2001 Peter Hunter. All rights reserved.
//
package com.chess.ui.engine;

public final class Move implements Comparable<Object> {

	public final static int CASTLING_MASK = 2;

	public int from;
	public int to;
	public int promote;
	public int bits;
	int score;

	public Move(int from, int to, int promote, int bits) {
		this.from = from;
		this.to = to;
		this.promote = promote;
		this.bits = bits;
	}

	int getScore() {
		return score;
	}

	void setScore(int score) {
		this.score = score;
	}

	public int hashCode() {
		return from + (to << 8) + (promote << 16);
	}

	@Override
	public boolean equals(Object o) {
		Move move = (Move) o;
		return (move.from == from && move.to == to && move.promote == promote);
	}

	@Override
	public int compareTo(Object o) {
		Move move = (Move) o;
		int mScore = move.getScore();
		if (score < mScore) return 1;
		if (score > mScore) return -1;
		int mHashCode = move.hashCode();
		int hash = hashCode();
		if (hash > mHashCode) return 1;
		if (hash < mHashCode) return -1;
		return 0;
	}

	public String toString() {
		char piece;
		StringBuilder sb = new StringBuilder();

		if ((bits & 32) != 0) {
			switch (promote) {
				case ChessBoard.KNIGHT:
					piece = 'n';
					break;
				case ChessBoard.BISHOP:
					piece = 'b';
					break;
				case ChessBoard.ROOK:
					piece = 'r';
					break;
				default:
					piece = 'q';
					break;
			}
			sb.append((char) (ChessBoard.getColumn(from) + 'a'));
			sb.append(8 - ChessBoard.getRow(from));
			sb.append((char) (ChessBoard.getColumn(to) + 'a'));
			sb.append(8 - ChessBoard.getRow(to));
			sb.append(piece);
		} else {
			sb.append((char) (ChessBoard.getColumn(from) + 'a'));
			sb.append(8 - ChessBoard.getRow(from));
			sb.append((char) (ChessBoard.getColumn(to) + 'a'));
			sb.append(8 - ChessBoard.getRow(to));
		}
		return sb.toString();
	}

	public boolean isCastling() {
		return (bits & CASTLING_MASK) != 0;
	}
}