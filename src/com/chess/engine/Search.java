//
//  Search.java
//  ChessApp
//
//  This search code is heavily based on Tom Kerrigan's tscp for which he
//  owns the copyright - (c) 1997 Tom Kerrigan -  and is used with his permission.
//  All rights are reserved by the owners of the respective copyrights.
//  Java version created by Peter Hunter on Sat Jan 05 2002.
//  Copyright (c) 2002 Peter Hunter. All rights reserved.
//
package com.chess.engine;

import android.util.Log;
import com.chess.core.interfaces.BoardFace;

import java.util.Iterator;
import java.util.TreeSet;

public class Search {
	final static int MAX_PLY = 32;

	private BoardFace boardFace;
	private Move pv[][] = new Move[MAX_PLY][MAX_PLY];
	private int pvLength[] = new int[MAX_PLY];
	private boolean followPV;
	private int ply = 0;
	private int nodes = 0;
	private long startTime;
	private long stopTime;

	public Search(BoardFace boardFace1) {
		boardFace = boardFace1;
	}

	public Move getBest() {
		return pv[0][0];
	}

	public void think(int output, int maxTime, int maxDepth) {
		/* some code that lets us get back here and return
			   from think() when our time is up */
		try {
			startTime = System.currentTimeMillis();
			stopTime = startTime + maxTime;

			ply = 0;
			nodes = 0;
			for (int i = 0; i < MAX_PLY; i++)
				for (int j = 0; j < MAX_PLY; j++)
					pv[i][j] = new Move((char) 0, (char) 0, (char) 0, (char) 0);
			for (int i = 0; i < 64; i++)
				for (int j = 0; j < 64; j++)
					boardFace.getHistory()[i][j] = 0;
			if (output == 1)
				Log.d("SEARCH", "ply      nodes  score  pv");
			for (int i = 1; i <= maxDepth; ++i) {
				followPV = true;
				int x = search(-10000, 10000, i);
				if (output > 0) {
					Log.d("SEARCH", /*"%3d  %9d  %5d "*/ i + " " + nodes + " " + x);
					for (int j = 0; j < pvLength[0]; ++j)
						Log.d("SEARCH", " " + pv[0][j].toString());
				}
				if (x > 9000 || x < -9000)
					break;
			}
		} catch (StopSearchingException e) {
			/* make sure to take back the line we were searching */
			while (ply != 0) {
				boardFace.takeBack();
				--ply;
			}
		}
		Log.d("SEARCH", "Nodes searched: " + nodes);
		return;
	}

	/* search() does just that, in negamax fashion */

	int search(int alpha, int beta, int depth) throws StopSearchingException {
		/* we're as deep as we want to be; call quiesce() to get
			   a reasonable score and return it. */
		if (depth == 0)
			return quiesce(alpha, beta);
		++nodes;

		/* do some housekeeping every 1024 nodes */
		if ((nodes & 1023) == 0)
			checkup();

		pvLength[ply] = ply;

		/* if this isn't the root of the search tree (where we have
			   to pick a move and can't simply return 0) then check to
			   see if the position is a repeat. if so, we can assume that
			   this line is a draw and return 0. */
		if ((ply > 0) && (boardFace.reps() > 0))
			return 0;

		/* are we too deep? */
		if (ply >= MAX_PLY - 1)
			return boardFace.eval();
/*	if (hply >= HIST_STACK - 1)
            return newBoardView.eval();
FIXME!!! We could in principle overflow the move history stack.
*/
		/* are we in check? if so, we want to search deeper */
		boolean check = boardFace.inCheck(boardFace.getSide());
		if (check)
			++depth;
		TreeSet<Move> validMoves = boardFace.gen();
		if (followPV)  /* are we following the PV? */
			sortPV(validMoves);

		/* loop through the moves */
		boolean foundMove = false;
		Iterator<Move> i = validMoves.iterator();
		while (i.hasNext()) {
			Move m = (Move) i.next();
			if (!boardFace.makeMove(m, false))
				continue;
			foundMove = true;
			ply++;
			int x = -search(-beta, -alpha, depth - 1);
			boardFace.takeBack();
			ply--;
			if (x > alpha) {
				/* this move caused a cutoff, so increase the history
									value so it gets ordered high next time we can
									search it */
				boardFace.getHistory()[m.from][m.to] += depth;
				if (x >= beta)
					return beta;
				alpha = x;

				/* update the PV */
				pv[ply][ply] = m;
				for (int j = ply + 1; j < pvLength[ply + 1]; ++j)
					pv[ply][j] = pv[ply + 1][j];
				pvLength[ply] = pvLength[ply + 1];
			}
		}

		/* no legal moves? then we're in checkmate or stalemate */
		if (!foundMove) {
			if (check)
				return -10000 + ply;
			else
				return 0;
		}

		/* fifty move draw rule */
		if (boardFace.getFifty() >= 100)
			return 0;
		return alpha;
	}


	/* quiesce() is a recursive minimax search function with
		alpha-beta cutoffs. In other words, negamax. It basically
		only searches capture sequences and allows the evaluation
		function to cut the search off (and set alpha). The idea
		is to find a position where there isn't a lot going on
		so the static evaluation function will work. */

	int quiesce(int alpha, int beta) throws StopSearchingException {
		++nodes;

		/* do some housekeeping every 1024 nodes */
		if ((nodes & 1023) == 0)
			checkup();

		pvLength[ply] = ply;

		/* are we too deep? */
		if (ply >= MAX_PLY - 1)
			return boardFace.eval();
/*	if (hply >= HIST_STACK - 1)
            return newBoardView.eval();
FIXME!! see above */
		/* check with the evaluation function */
		int x = boardFace.eval();
		if (x >= beta)
			return beta;
		if (x > alpha)
			alpha = x;

		TreeSet<Move> validCaptures = boardFace.genCaps();
		if (followPV)  /* are we following the PV? */
			sortPV(validCaptures);

		/* loop through the moves */
		Iterator<Move> i = validCaptures.iterator();
		while (i.hasNext()) {
			Move m = (Move) i.next();
			if (!boardFace.makeMove(m, false))
				continue;
			ply++;
			x = -quiesce(-beta, -alpha);
			boardFace.takeBack();
			ply--;
			if (x > alpha) {
				if (x >= beta)
					return beta;
				alpha = x;

				/* update the PV */
				pv[ply][ply] = m;
				for (int j = ply + 1; j < pvLength[ply + 1]; ++j)
					pv[ply][j] = pv[ply + 1][j];
				pvLength[ply] = pvLength[ply + 1];
			}
		}
		return alpha;
	}


/* sortPV() is called when the search function is following
   the PV (Principal Variation). It looks through the current
   ply's move list to see if the PV move is there. If so,
   it adds 10,000,000 to the move's score so it's played first
   by the search function. If not, followPV remains FALSE and
   search() stops calling sortPV(). */

	void sortPV(TreeSet<Move> moves) {
		followPV = false;
		Iterator<Move> i = moves.iterator();
		while (i.hasNext()) {
			Move m = (Move) i.next();
			if (m.equals(pv[0][ply])) {
				followPV = true;
				m.score += 10000000;
				i.remove();
				moves.add(m);
				return;
			}
		}
	}

	/* checkup() is called once in a while during the search. */

	void checkup() throws StopSearchingException {
		/* is the engine's time up? if so, longjmp back to the
			   beginning of think() */
		if (System.currentTimeMillis() >= stopTime) {
			throw new StopSearchingException();
		}
	}


}
