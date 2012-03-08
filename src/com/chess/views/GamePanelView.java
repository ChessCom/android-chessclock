package com.chess.views;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.adapters.ItemsAdapter;
import com.chess.engine.PieceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class GamePanelView extends LinearLayout {

	private LinearLayout controlsLayout;
	private int[] pieceIds;
	private int[] whitePieceDrawableIds;
	private int[] blackPieceDrawableIds;

	//	Ids for pieces
	public static final int PAWN_ID = 0;
	public static final int KNIGHT_ID = 1;
	public static final int BISHOP_ID = 2;
	public static final int ROOK_ID = 3;
	public static final int QUEEN_ID = 4;
	public static final int KING_ID = 5;

//	if (piece == 0)
//	w_pawns--;
//	if (piece == 1)
//	w_knights--;
//	if (piece == 2)
//	w_bishops--;
//	if (piece == 3)
//	w_rooks--;
//	if (piece == 4)
//	w_queen = 0;


	//	prefixes
	public static final int FRAME_PREFIX = 0x00001000;


	public GamePanelView(Context context) {
		super(context);
		onCreate();
	}

	public GamePanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}


	private void addControlButton(int imageId, int backId) {
		ImageButton imageButton = new ImageButton(getContext());
		imageButton.setImageResource(imageId);
		imageButton.setBackgroundResource(backId);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		params.weight = 1;
		imageButton.setLayoutParams(params);
		controlsLayout.addView(imageButton);
	}

	public void onCreate() {
		setOrientation(VERTICAL);

		pieceIds = getResources().getIntArray(R.array.pieces_ids);

		controlsLayout = new LinearLayout(getContext());
		controlsLayout.setPadding(10, 10, 10, 10);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		controlsLayout.setLayoutParams(params);

		addControlButton(R.drawable.ic_fastforward, R.drawable.button_emboss_left_selector);
		addControlButton(R.drawable.ic_options, R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_flip, R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_analysis, R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_chat, R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_back, R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_forward, R.drawable.button_emboss_right_selector);
		addView(controlsLayout);


		LinearLayout infoLayout = new LinearLayout(getContext());
		infoLayout.setLayoutParams(params);

		// add captured pieces layout
		LinearLayout.LayoutParams pieceParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LinearLayout piecesLayout = new LinearLayout(getContext());
		// Set weights to moves list and captured pieces
		pieceParams.weight = 3;
		piecesLayout.setLayoutParams(pieceParams);
		piecesLayout.setOrientation(VERTICAL);
		piecesLayout.setPadding(7, 1, 0, 1);
		piecesLayout.setGravity(Gravity.CENTER);

		LinearLayout whiteCapturedPieces = new LinearLayout(getContext());
		whiteCapturedPieces.setPadding(1, 1, 1, 1);
		whiteCapturedPieces.setLayoutParams(params);
		whiteCapturedPieces.setGravity(Gravity.LEFT);

		piecesLayout.addView(whiteCapturedPieces);

		LinearLayout blackCapturedPieces = new LinearLayout(getContext());
		blackCapturedPieces.setPadding(1, 1, 1, 1);
		blackCapturedPieces.setLayoutParams(params);
		blackCapturedPieces.setGravity(Gravity.LEFT);

		piecesLayout.addView(blackCapturedPieces);

		infoLayout.addView(piecesLayout);

		// add moves list view
		LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		listParams.weight = 7;
		listParams.gravity = Gravity.RIGHT;


		ListView movesListView = new ListView(getContext());
		movesListView.setPadding(1, 1, 0, 1);
		movesListView.setLayoutParams(listParams);

		movesListView.setCacheColorHint(Color.TRANSPARENT);
		movesListView.setBackgroundColor(Color.TRANSPARENT);
		movesListView.setDividerHeight(0);
		movesListView.setDivider(getResources().getDrawable(android.R.color.transparent));

		infoLayout.addView(movesListView);

		addView(infoLayout);

		List<String> itemList = new ArrayList<String>();
		itemList.add("42. Bf3  Ke7");
		itemList.add("43. Nd5+  Nxd5");
		itemList.add("44. exd5  Ke7");

		movesListView.setAdapter(new MovesAdapter(getContext(), itemList));

		whitePieceDrawableIds = new int[]{
				R.drawable.captured_wp,
				R.drawable.captured_wn,
				R.drawable.captured_wb,
				R.drawable.captured_wr,
				R.drawable.captured_wq,
				R.drawable.captured_wk
		};

		blackPieceDrawableIds = new int[]{   // TODO reuse to set other drawable sets
				R.drawable.captured_bp,
				R.drawable.captured_bn,
				R.drawable.captured_bb,
				R.drawable.captured_br,
				R.drawable.captured_bq,
				R.drawable.captured_bk
		};

		addItems(whiteCapturedPieces, true, 1, 1.0f, QUEEN_ID );
		addItems(whiteCapturedPieces, true, 2, 1.0f, ROOK_ID  );
		addItems(whiteCapturedPieces, true, 2, 1.0f, BISHOP_ID);
		addItems(whiteCapturedPieces, true, 2, 1.0f, KNIGHT_ID);
		addItems(whiteCapturedPieces, true, 8, 1.0f, PAWN_ID  );
		addItems(whiteCapturedPieces, true, 1, 1.0f, KING_ID  );

		addItems(blackCapturedPieces, false, 1, 1.0f, QUEEN_ID );
		addItems(blackCapturedPieces, false, 2, 1.0f, ROOK_ID  );
		addItems(blackCapturedPieces, false, 2, 1.0f, BISHOP_ID);
		addItems(blackCapturedPieces, false, 2, 1.0f, KNIGHT_ID);
		addItems(blackCapturedPieces, false, 8, 1.0f, PAWN_ID  );
		addItems(blackCapturedPieces, false, 1, 1.0f, KING_ID  );

		movesListView.setSelection(movesListView.getAdapter().getCount() - 1);
	}

	private int getFrameIdByCode(PieceItem pieceItem){
		int pieceId = 0;
/*
		String strCode = pieceItem.getStringCode();
		pieceItem.setWhite(strCode.substring(0, 1).equals("w"));

		String pieceCode = strCode.substring(1, 2);
		int pieceId = 0;
		if(pieceCode.equals("q")){
			pieceId = QUEEN_ID;
		}else if (pieceCode.equals("r")){
			pieceId = ROOK_ID;
		}else if (pieceCode.equals("b")){
			pieceId = BISHOP_ID;
		}else if (pieceCode.equals("n")){
			pieceId = KNIGHT_ID;
		}else if (pieceCode.equals("p")){
			pieceId = PAWN_ID;
		}else if (pieceCode.equals("k")){
			pieceId = KING_ID;
		}
*/


		switch(pieceItem.getCode()){
			case PieceItem.Q:
				pieceId = QUEEN_ID;
				break;
			case PieceItem.R:
				pieceId = ROOK_ID;
				break;
			case PieceItem.B:
				pieceId = BISHOP_ID;
				break;
			case PieceItem.N:
				pieceId = KNIGHT_ID;
				break;
			case PieceItem.P:
				pieceId = PAWN_ID;
				break;
			case PieceItem.K:
				pieceId = KING_ID;
				break;
		}

		return FRAME_PREFIX + pieceIds[pieceId];
	}

	public void capturePiece(PieceItem pieceItem) {
		if(pieceItem.isCaptured())
			return;
		showPiece(true, pieceItem);
	}

	public void restorePiece(PieceItem pieceItem) {
		showPiece(false, pieceItem);
	}

	private void showPiece(boolean show, PieceItem pieceItem) {
		int frameId = getFrameIdByCode(pieceItem);

		FrameLayout capturedFrame = (FrameLayout) findViewById(frameId);

		PieceItem storedPieceItem = (PieceItem) capturedFrame.getTag();
		// change image
		ImageView imageView = (ImageView) capturedFrame.findViewById(R.id.imagePieceView);

		int maxLevel = storedPieceItem.getLayersCnt();
		int currentLevel = storedPieceItem.getCurrentLevel();

		if(show){
			if(currentLevel < maxLevel){
				currentLevel++;
			}
		}else{
			if(currentLevel > 0){
				currentLevel--;
			}
		}
		storedPieceItem.setCurrentLevel(currentLevel);
		LayerDrawable pieceDrawable;
		if(storedPieceItem.isWhite()){
			pieceDrawable = createImageDrawable(currentLevel, whitePieceDrawableIds[storedPieceItem.getPieceId()]);
		}else{
			pieceDrawable = createImageDrawable(currentLevel, blackPieceDrawableIds[storedPieceItem.getPieceId()]);
		}
		imageView.setImageDrawable(pieceDrawable);

		capturedFrame.setTag(storedPieceItem);
		invalidate();
	}



	private LayerDrawable createImageDrawable(int layersCnt, int pieceDrawableId){
		Drawable[] layers = new Drawable[layersCnt];

		for (int j = 0; j < layersCnt; j++) {
			layers[j] = getResources().getDrawable(pieceDrawableId);
		}

		LayerDrawable pieceDrawable = new LayerDrawable(layers);

		for (int i = 0; i < layersCnt; i++) {
			shiftLayer(pieceDrawable, i);
		}
		return pieceDrawable;
	}

	private void addItems(LinearLayout viewGroup, boolean isWhite, int layersCnt, float itemWeight, int pieceId) {
		// Add background image to get correct weights
		ImageView imageView = new ImageView(getContext());
		imageView.setAdjustViewBounds(false);
		imageView.setScaleType(ImageView.ScaleType.CENTER);

		FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LayerDrawable pieceDrawable;
		if(isWhite){
			pieceDrawable = createImageDrawable(layersCnt, whitePieceDrawableIds[pieceId]);
		}else{
			pieceDrawable = createImageDrawable(layersCnt, blackPieceDrawableIds[pieceId]);
		}

		imageView.setImageDrawable(pieceDrawable);
		imageView.setLayoutParams(imageParams);

		// crate pieceItem
		PieceItem pieceItem = new PieceItem();
		pieceItem.setCode(PieceItem.P);
		pieceItem.setWhite(true);
		pieceItem.setPieceId(pieceId);
		pieceItem.setLayersCnt(layersCnt);
		pieceItem.setPieceFrameId(FRAME_PREFIX +  pieceIds[pieceId]);


		imageView.setVisibility(INVISIBLE);
		viewGroup.setWeightSum(16f);

		// put image inside frame to get left gravity
		FrameLayout frame = new FrameLayout(getContext());

		frame.addView(imageView);
		frame.setId(pieceItem.getPieceFrameId());
		frame.setTag(pieceItem);

		// Add empty image view to show captured pieces
		FrameLayout.LayoutParams pieceImageParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		pieceImageParams.gravity = Gravity.LEFT;
		ImageView pieceView = new ImageView(getContext());
		pieceView.setAdjustViewBounds(false);
		pieceView.setScaleType(ImageView.ScaleType.CENTER);
		pieceView.setId(R.id.imagePieceView);
		pieceView.setLayoutParams(pieceImageParams);
		frame.addView(pieceView);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		params.weight = itemWeight;
		frame.setLayoutParams(params);

		viewGroup.addView(frame);
	}

	private int shiftSize = 7;

	private void shiftLayer(LayerDrawable pieceDrawable, int level) {

		int l = level * shiftSize;
		int r = 0;
		int t = 0;
		int b = 0;
		pieceDrawable.setLayerInset(level, l, t, r, b);
		((BitmapDrawable) pieceDrawable.getDrawable(level)).setGravity(Gravity.LEFT | Gravity.TOP);
	}


	private class MovesAdapter extends ItemsAdapter<String> {
		public MovesAdapter(Context context, List<String> itemList) {
			super(context, itemList);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.game_panel_list_item, parent, false);
			return view;
		}

		@Override
		protected void bindView(String item, int pos, View convertView) {
			((TextView) convertView).setText(item);
		}
	}

}