package com.chess.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.Search;
import com.chess.ui.interfaces.GameCompActivityFace;

import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardCompView extends ChessBoardBaseView {

    private boolean hint;
    private boolean compmoving;
    private boolean stopThinking;


    private int compStrength;
	private int[] compStrengthArray;
	private GameCompActivityFace gameCompActivityFace;


	public ChessBoardCompView(Context context, AttributeSet attrs) {
        super(context, attrs);

		compStrengthArray = resources.getIntArray(R.array.comp_strength);
    }



    public void setGameActivityFace(GameCompActivityFace gameActivityFace) {
		super.setGameActivityFace(gameActivityFace);

        gameCompActivityFace = gameActivityFace;

		compStrength = compStrengthArray[AppData.getCompStrength(getContext())];
    }

    public void afterMove() {
        boardFace.setMovesCount(boardFace.getHply());
//        gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);    //movelist
		gameActivityFace.invalidateGameScreen();

        if (isGameOver())
            return;

        if (!boardFace.isAnalysis()) {
			computerMove(compStrength);
        }
    }


    boolean isGameOver() {
        //saving game for comp game mode if human is playing
        if ((AppData.isComputerVsHumanGameMode(boardFace) || AppData.isHumanVsHumanGameMode(boardFace))
                && !boardFace.isAnalysis()) {

            String saving = StaticData.SYMBOL_EMPTY + boardFace.getMode();

            int i;
            for (i = 0; i < boardFace.getMovesCount(); i++) {
                Move m = boardFace.getHistDat()[i].move;
                saving += "|" + m.from + ":" + m.to + ":" + m.promote + ":" + m.bits;
            }

			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(AppConstants.SAVED_COMPUTER_GAME, saving);
			editor.commit();
        }

        // Check available moves
        TreeSet<Move> validMoves = boardFace.gen();

        Iterator<Move> i = validMoves.iterator();
        boolean found = false;
        while (i.hasNext()) {   // compute available moves
            if (boardFace.makeMove(i.next(), false)) {
                boardFace.takeBack();
                found = true;
                break;
            }
        }
        String message = null;
        if (!found) {
            if (boardFace.inCheck(boardFace.getSide())) {
                boardFace.getHistDat()[boardFace.getHply() - 1].notation += "#";
				gameActivityFace.invalidateGameScreen();

                if (boardFace.getSide() == ChessBoard.LIGHT)
                    message = getResources().getString(R.string.black_wins);
                else
                    message = getResources().getString(R.string.white_wins);
            } else
                message = getResources().getString(R.string.draw_by_stalemate);
        } else if (boardFace.reps() == 3 )
            message = getResources().getString(R.string.draw_by_3fold_repetition);

        if (message != null) {
            finished = true;

			gameActivityFace.onGameOver(message, false);

            return true;
        }

        if (boardFace.inCheck(boardFace.getSide())) {
            boardFace.getHistDat()[boardFace.getHply() - 1].notation += "+";
			gameActivityFace.invalidateGameScreen();

			gameActivityFace.onCheck();
        }
        return false;
    }

    public void computerMove(final int time) {
        if (boardFace.isAnalysis())
            return;

        if (AppData.isComputerVsComputerGameMode(boardFace) && stopThinking) {
            stopThinking = false;
            return;
        }

        compmoving = true;
//        gameActivityFace.update(GameBaseActivity.CALLBACK_COMP_MOVE);
		gameCompActivityFace.onCompMove();
        new Thread(new Runnable() {
            @Override
            public void run() {
                pieces_tmp = boardFace.getPieces().clone();
                colors_tmp = boardFace.getColor().clone();
                Search searcher = new Search(boardFace);
                searcher.think(0, time, 32);
                Move best = searcher.getBest();
                boardFace.makeMove(best);
                compmoving = false;
                boardFace.setMovesCount(boardFace.getHply());
                update.sendEmptyMessage(0);
            }

            private Handler update = new Handler() {
                @Override
                public void dispatchMessage(Message msg) {
                    super.dispatchMessage(msg);
//                    gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);    //movelist
					gameActivityFace.invalidateGameScreen();
//                    gameActivityFace.update(GameBaseActivity.CALLBACK_PLAYER_MOVE);
					gameCompActivityFace.onPlayerMove();
                    invalidate();
                    if (isGameOver())
                        return;
                    if (AppData.isComputerVsComputerGameMode(boardFace)
                            || (hint && !AppData.isHumanVsHumanGameMode(boardFace))) {
                        if (hint)
                            hint = false;
                        computerMove(time);
                    }
                }
            };
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG));
        super.onDraw(canvas);
        W = viewWidth;
        H = viewHeight;

        if (H < W) {
            square = viewHeight / 8;
            H -= viewHeight % 8;
        } else {
            square = viewWidth / 8;
            W -= viewWidth % 8;
        }
        side = square * 2;

        int i, j;
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++) {
				rect.set(i * side, j * side, i * side + side, j * side + side);
				canvas.drawBitmap(boardBitmap, null, rect, null);
            }
        }

        if (!compmoving) {
            for (i = 0; i < 64; i++) {
                if (drag && i == from)
                    continue;
                int c = boardFace.getColor()[i];
                int p = boardFace.getPieces()[i];
                int x = ChessBoard.getColumn(i, boardFace.isReside());
                int y = ChessBoard.getRow(i, boardFace.isReside());
                if (c != 6 && p != 6) {    // here is the simple replace/redraw of piece
                    rect.set(x * square, y * square, x * square + square, y * square + square);
                    canvas.drawBitmap(piecesBitmaps[c][p], null, rect, null);
                }
            }
        } else {
            for (i = 0; i < 64; i++) {
                if (drag && i == from)
                    continue;
                int c = colors_tmp[i];
                int p = pieces_tmp[i];
                int x = ChessBoard.getColumn(i, boardFace.isReside());
                int y = ChessBoard.getRow(i, boardFace.isReside());
                if (c != 6 && p != 6) {     // here is the simple replace/redraw of piece
                    rect.set(x * square, y * square, x * square + square, y * square + square);
                    canvas.drawBitmap(piecesBitmaps[c][p], null, rect, null);
                }
            }
        }

        if (showCoordinates) {
            for (i = 0; i < 8; i++) {
                if (boardFace.isReside()) {
                    canvas.drawText(nums[i], 2, i * square + 12, blackPaint);
                    canvas.drawText(signs[7 - i], i * square + 2, 8 * square - 2, blackPaint);
                } else {
                    canvas.drawText(nums[7 - i], 2, i * square + 12, blackPaint);
                    canvas.drawText(signs[i], i * square + 2, 8 * square - 2, blackPaint);
                }
            }
        }

        if (isHighlightEnabled && boardFace.getHply() > 0 && !compmoving) {
            Move m = boardFace.getHistDat()[boardFace.getHply() - 1].move;
            int x1 = ChessBoard.getColumn(m.from, boardFace.isReside());
            int y1 = ChessBoard.getRow(m.from, boardFace.isReside());
            canvas.drawRect(x1 * square, y1 * square, x1 * square + square, y1 * square + square, redPaint);
            int x2 = ChessBoard.getColumn(m.to, boardFace.isReside());
            int y2 = ChessBoard.getRow(m.to, boardFace.isReside());
            canvas.drawRect(x2 * square, y2 * square, x2 * square + square, y2 * square + square, redPaint);
        }

        if (sel) {
            int x = ChessBoard.getColumn(from, boardFace.isReside());
            int y = ChessBoard.getRow(from, boardFace.isReside());
            canvas.drawRect(x * square, y * square, x * square + square, y * square + square, whitePaint);
        }
        if (drag) {
            int c = boardFace.getColor()[from];
            int p = boardFace.getPieces()[from];
            int x = dragX - square / 2;
            int y = dragY - square / 2;
            int col = (dragX - dragX % square) / square;
            int row = ((dragY + square) - (dragY + square) % square) / square;
            if (c != 6 && p != 6) {
                rect.set(x - square / 2, y - square / 2, x + square + square / 2, y + square + square / 2);
                canvas.drawBitmap(piecesBitmaps[c][p], null, rect, null);
                canvas.drawRect(col * square - square / 2, row * square - square / 2,
                        col * square + square + square / 2, row * square + square + square / 2, whitePaint);
            }
        }
        if (track) {
            int x = (trackX - trackX % square) / square;
            int y = (trackY - trackY % square) / square;
            canvas.drawRect(x * square, y * square, x * square + square, y * square + square, greenPaint);
        }

        // Count captured piecesBitmap
		if(!compmoving){
			gamePanelView.dropAlivePieces();
			for (i = 0; i < 64; i++) {
				int pieceId = boardFace.getPiece(i);
				if (boardFace.getColor()[i] == ChessBoard.LIGHT) {
					gamePanelView.addAlivePiece(true, pieceId);
				} else {
					gamePanelView.addAlivePiece(false, pieceId);
				}
			}
		}

        gamePanelView.updateCapturedPieces();
    }


    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (useTouchTimer) { // start count before next touch
            handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
            userActive = true;
        }

        float sens = 0.3f;
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            track = true;
            if (event.getX() > sens)
                trackX += square;
            else if (event.getX() < -sens)
                trackX -= square;
            if (event.getY() > sens)
                trackY += square;
            else if (event.getY() < -sens)
                trackY -= square;
            if (trackX < 0)
                trackX = 0;
            if (trackY < 0)
                trackY = 0;
            if (trackX > 7 * square)
                trackX = 7 * square;
            if (trackY > 7 * square)
                trackY = 7 * square;
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int col = (trackX - trackX % square) / square;
            int row = (trackY - trackY % square) / square;

            if (firstclick) {
                from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                if (boardFace.getPieces()[from] != 6 && boardFace.getSide() == boardFace.getColor()[from]) {
                    sel = true;
                    firstclick = false;
                    invalidate();
                }
            } else {
                to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                sel = false;
                firstclick = true;
                boolean found = false;

                TreeSet<Move> moves = boardFace.gen();
                Iterator<Move> moveIterator = moves.iterator();

                Move move = null;
                while (moveIterator.hasNext()) {
                    move = moveIterator.next();
                    if (move.from == from && move.to == to) {
                        found = true;
                        break;
                    }
                }
                if ((((to < 8) && (boardFace.getSide() == ChessBoard.LIGHT)) ||
						((to > 55) && (boardFace.getSide() == ChessBoard.DARK))) &&
                        (boardFace.getPieces()[from] == ChessBoard.PAWN) && found) {

                    gameActivityFace.showChoosePieceDialog(col, row);
                    return true;
                }
                if (found && boardFace.makeMove(move)) {
                    invalidate();
                    afterMove();
                } else if (boardFace.getPieces()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
                    sel = true;
                    firstclick = false;
                    from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    invalidate();
                } else {
                    invalidate();
                }
            }
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (useTouchTimer) { // start count before next touch
            handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
            userActive = true;
        }

        if (square == 0) {
            return super.onTouchEvent(event);
        }

        track = false;
        if (!boardFace.isAnalysis()) {
            if (compmoving || finished
					|| boardFace.isSubmit())
                return true;

//            if ((AppData.isComputerVsHumanWhiteGameMode(boardFace) && boardFace.getHply() % 2 != 0)
//                    || (AppData.isComputerVsHumanBlackGameMode(boardFace) && boardFace.getHply() % 2 == 0)) {
//                return true;
//            }

            if ((AppData.isComputerVsHumanWhiteGameMode(boardFace)
					&& boardFace.getHply() % 2 != 0 && !boardFace.isReside())
					||(AppData.isComputerVsHumanWhiteGameMode(boardFace)
					&& boardFace.getHply() % 2 == 0 && boardFace.isReside())
					|| (AppData.isComputerVsHumanBlackGameMode(boardFace)
					&& boardFace.getHply() % 2 == 0 && !boardFace.isReside())
					|| (AppData.isComputerVsHumanBlackGameMode(boardFace)
					&& boardFace.getHply() % 2 != 0 && boardFace.isReside())
					) {
                return true;
            }

        }

        int col, row;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                col = (int) (event.getX() - event.getX() % square) / square;
                row = (int) (event.getY() - event.getY() % square) / square;
                if (col > 7 || col < 0 || row > 7 || row < 0) {
                    invalidate();
                    return false;
                }
                if (firstclick) {
                    from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    if (boardFace.getPieces()[from] != 6 && boardFace.getSide() == boardFace.getColor()[from]) {
                        sel = true;
                        firstclick = false;
                        invalidate();
                    }
                } else {
                    int fromPosIndex = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    if (boardFace.getPieces()[fromPosIndex] != 6 && boardFace.getSide() == boardFace.getColor()[fromPosIndex]) {
                        from = fromPosIndex;
                        sel = true;
                        firstclick = false;
                        invalidate();
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                dragX = (int) event.getX();
                dragY = (int) event.getY() - square;
                col = (dragX - dragX % square) / square;
                row = (dragY - dragY % square) / square;
                if (col > 7 || col < 0 || row > 7 || row < 0) {
                    invalidate();
                    return false;
                }
                if (!drag && !sel)
                    from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                if (!firstclick && boardFace.getSide() == boardFace.getColor()[from]) {
                    drag = true;
                    to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                col = (int) (event.getX() - event.getX() % square) / square;
                row = (int) (event.getY() - event.getY() % square) / square;

                drag = false;
                // if outside of the boardBitmap - return
                if (col > 7 || col < 0 || row > 7 || row < 0) { // if touched out of board
                    invalidate();
                    return false;
                }
                if (firstclick) {
                    from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    if (boardFace.getPieces()[from] != 6 && boardFace.getSide() == boardFace.getColor()[from]) {
                        sel = true;
                        firstclick = false;
                        invalidate();
                    }
                } else {
                    to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    sel = false;
                    firstclick = true;
                    boolean found = false;
                    TreeSet<Move> moves = boardFace.gen();
                    Iterator<Move> moveIterator = moves.iterator();

                    Move move = null;
                    while (moveIterator.hasNext()) {
                        move = moveIterator.next();     // search for move that was made
                        if (move.from == from && move.to == to) {
                            found = true;
                            break;
                        }
                    }

                    if ((((to < 8) && (boardFace.getSide() == ChessBoard.LIGHT)) ||
							((to > 55) && (boardFace.getSide() == ChessBoard.DARK))) &&
                            (boardFace.getPieces()[from] == ChessBoard.PAWN) && found) {

                        gameActivityFace.showChoosePieceDialog(col, row);
                        return true;
                    }

                    if (found && move != null && boardFace.makeMove(move)) {
                        afterMove();
                    } else if (boardFace.getPieces()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
                        sel = true;
                        firstclick = false;
                        from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    }
                    invalidate();
                }
                return true;
            }
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    public void promote(int promote, int col, int row) {
        boolean found = false;
        TreeSet<Move> moves = boardFace.gen();
        Iterator<Move> iterator = moves.iterator();

        Move move = null;
        while (iterator.hasNext()) {
            move = iterator.next();
            if (move.from == from && move.to == to && move.promote == promote) {
                found = true;
                break;
            }
        }
        if (found && boardFace.makeMove(move)) {
            invalidate();
            afterMove();
        } else if (boardFace.getPieces()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
            sel = true;
            firstclick = false;
            from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
            invalidate();
        } else {
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        viewWidth = (xNew == 0 ? viewWidth : xNew);
        viewHeight = (yNew == 0 ? viewHeight : yNew);
    }

    @Override
    public void flipBoard() {
//        stopThinking = true;
        if (!compmoving) {
            getBoardFace().setReside(!getBoardFace().isReside());
            if (AppData.isComputerVsHumanGameMode(getBoardFace())) {
                if (AppData.isComputerVsHumanWhiteGameMode(getBoardFace())) {
                    getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);

                } else if (AppData.isComputerVsHumanBlackGameMode(getBoardFace())) {
                    getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);

                }
                computerMove(compStrength);
            }
            invalidate();
			gameActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void switchAnalysis() {
        boolean isAnalysis = getBoardFace().toggleAnalysis();

        gamePanelView.toggleControlButton(GamePanelView.B_ANALYSIS_ID, isAnalysis);
        gameActivityFace.switch2Analysis(isAnalysis); //  update(GameBaseActivity.CALLBACK_REPAINT_UI);
    }

    @Override
    public void switchChat() {
    }

	@Override
    public void moveBack() {
        stopThinking = true;
        if (!compmoving) {
            finished = false;
            sel = false;
            getBoardFace().takeBack();
            invalidate();
//            gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);
			gameActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void moveForward() {
        stopThinking = true;
        if (!compmoving) {
            sel = false;
            getBoardFace().takeNext();
            invalidate();
//            gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);
			gameActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void showHint() {
        stopThinking = true;
        if (!compmoving) {
            hint = true;
            computerMove(compStrength);
        }
    }

	public void stopThinking() {
		stopThinking = true;
	}

	public void think(){
		stopThinking = false;
	}

	public boolean isThinking() {
		return stopThinking;
	}
}
