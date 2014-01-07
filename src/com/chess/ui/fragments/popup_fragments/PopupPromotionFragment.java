package com.chess.ui.fragments.popup_fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.chess.R;
import com.chess.statics.AppData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.utilities.AppUtils;

import java.io.File;
import java.util.WeakHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.01.14
 * Time: 16:45
 */
public class PopupPromotionFragment extends SimplePopupDialogFragment implements View.OnClickListener {

	private PopupListSelectionFace listener;
	private BitmapFactory.Options bitmapOptions;
	private AppData appData;
	private WeakHashMap<Integer, Bitmap> whitePiecesMap;
	private WeakHashMap<Integer, Bitmap> blackPiecesMap;
	private int side;

	public static PopupPromotionFragment createInstance(PopupListSelectionFace listener, int side) {
		PopupPromotionFragment fragment = new PopupPromotionFragment();
		fragment.listener = listener;
		fragment.side = side;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		appData = new AppData(getActivity());
		loadPieces();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_promotion_popup, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (listener == null) { // we were restored from killed state
			dismiss();
			return;
		}

		ImageButton queenBtn = (ImageButton) view.findViewById(R.id.queenBtn);
		ImageButton rookBtn = (ImageButton) view.findViewById(R.id.rookBtn);
		ImageButton bishopBtn = (ImageButton) view.findViewById(R.id.bishopBtn);
		ImageButton knightBtn = (ImageButton) view.findViewById(R.id.knightBtn);

		queenBtn.setOnClickListener(this);
		rookBtn.setOnClickListener(this);
		bishopBtn.setOnClickListener(this);
		knightBtn.setOnClickListener(this);

		queenBtn.setImageBitmap(getScaledBitmapForPiece(ChessBoard.QUEEN));
		rookBtn.setImageBitmap(getScaledBitmapForPiece(ChessBoard.ROOK));
		bishopBtn.setImageBitmap(getScaledBitmapForPiece(ChessBoard.BISHOP));
		knightBtn.setImageBitmap(getScaledBitmapForPiece(ChessBoard.KNIGHT));
	}

	private Bitmap getScaledBitmapForPiece(int piece) {
		Bitmap bitmap;
		if (side == ChessBoard.WHITE_SIDE) {
			bitmap = whitePiecesMap.get(piece);
		} else {
			bitmap = blackPiecesMap.get(piece);
		}
		int size = (int) (48 * getResources().getDisplayMetrics().density);

		return Bitmap.createScaledBitmap(bitmap, size, size, true);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.queenBtn) {
			listener.onValueSelected(ChessBoard.QUEEN);
		} else if (view.getId() == R.id.rookBtn) {
			listener.onValueSelected(ChessBoard.ROOK);
		} else if (view.getId() == R.id.bishopBtn) {
			listener.onValueSelected(ChessBoard.BISHOP);
		} else if (view.getId() == R.id.knightBtn) {
			listener.onValueSelected(ChessBoard.KNIGHT);
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (listener != null) {
			listener.onDialogCanceled();
		}
	}

	protected void loadPieces() {
		Context context = getActivity();
		whitePiecesMap = new WeakHashMap<Integer, Bitmap>();
		blackPiecesMap = new WeakHashMap<Integer, Bitmap>();

		if (getAppData().isUseThemePieces()) {
			String piecesThemePath = appData.getThemePiecesPath();

			File dirForPieces = AppUtils.getLocalDirForPieces(context, piecesThemePath);

			bitmapOptions = new BitmapFactory.Options();
			// get bitmapOptions size. It's always the same for same sized drawables
			String testPath = dirForPieces.getAbsolutePath() + "/wq.png";
			bitmapOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(testPath, bitmapOptions);

			// create white pieces
			String[] whitePieceImageCodes = ChessBoard.whitePieceImageCodes;
			for (int i = 0; i < whitePieceImageCodes.length; i++) {
				String pieceImageCode = whitePieceImageCodes[i];
				String filePath = dirForPieces.getAbsolutePath() + "/" + pieceImageCode + ".png";
				Bitmap pieceBitmap = createBitmapForPiece(filePath);

				whitePiecesMap.put(i, pieceBitmap);
			}

			// create black pieces
			String[] blackPieceImageCodes = ChessBoard.blackPieceImageCodes;
			for (int i = 0; i < blackPieceImageCodes.length; i++) {
				String pieceImageCode = blackPieceImageCodes[i];
				String filePath = dirForPieces.getAbsolutePath() + "/" + pieceImageCode + ".png";
				Bitmap pieceBitmap = createBitmapForPiece(filePath);

				blackPiecesMap.put(i, pieceBitmap);
			}
		} else {
			String themePiecesName = appData.getThemePiecesName();
			setDefaultPiecesByName(context, themePiecesName);
		}
	}

	/**
	 * Create bitmap to be re-used, based on the size of one of the bitmaps
	 * pass bitmapOptions to get info
	 */
	private Bitmap createBitmapForPiece(int drawableId) {
		// Decode bitmap with inSampleSize set
		bitmapOptions.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(getResources(), drawableId, bitmapOptions);
	}

	private Bitmap createBitmapForPiece(String filePath) {
		bitmapOptions.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, bitmapOptions);
	}

	private void setDefaultPiecesByName(Context context, String themePiecesName) {
		if (themePiecesName.equals(context.getString(R.string.pieces_game))) {
			setPieceBitmapFromArray(ChessBoardBaseView.gamePiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_alpha))) {
			setPieceBitmapFromArray(ChessBoardBaseView.alphaPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_book))) {
			setPieceBitmapFromArray(ChessBoardBaseView.bookPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_cases))) {
			setPieceBitmapFromArray(ChessBoardBaseView.casesPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_classic))) {
			setPieceBitmapFromArray(ChessBoardBaseView.classicPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_club))) {
			setPieceBitmapFromArray(ChessBoardBaseView.clubPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_condal))) {
			setPieceBitmapFromArray(ChessBoardBaseView.condalPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_maya))) {
			setPieceBitmapFromArray(ChessBoardBaseView.mayaPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_modern))) {
			setPieceBitmapFromArray(ChessBoardBaseView.modernPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_vintage))) {
			setPieceBitmapFromArray(ChessBoardBaseView.vintagePiecesDrawableIds);
		} else { // if pieces wasn't selected yet, use default
			setPieceBitmapFromArray(ChessBoardBaseView.gamePiecesDrawableIds);
		}
	}

	private void setPieceBitmapFromArray(int[] drawableArray) {

		bitmapOptions = new BitmapFactory.Options();
		// get bitmapOptions size. It's always the same for same sized drawables
		int drawableId = drawableArray[0];
		bitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), drawableId, bitmapOptions);

		for (int j = 0; j < 6; j++) {
			// create white piece
			drawableId = drawableArray[j];
			Bitmap pieceBitmap = createBitmapForPiece(drawableId);
			whitePiecesMap.put(j, pieceBitmap);

			// create black piece
			drawableId = drawableArray[6 + j];
			pieceBitmap = createBitmapForPiece(drawableId);
			blackPiecesMap.put(j, pieceBitmap);
		}
	}


	public AppData getAppData() {
		return appData;
	}

	public void setAppData(AppData appData) {
		this.appData = appData;
	}
}
