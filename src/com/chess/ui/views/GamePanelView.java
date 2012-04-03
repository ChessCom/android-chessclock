package com.chess.ui.views;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.ui.engine.PieceItem;
import com.chess.ui.interfaces.BoardViewFace;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class GamePanelView extends LinearLayout implements View.OnClickListener {

	private LinearLayout controlsLayout;
	private int[] pieceIds;
	private int[] whitePieceDrawableIds;
	private int[] blackPieceDrawableIds;

	//	Ids for piecesBitmap
	public static final int PAWN_ID = 0;
	public static final int KNIGHT_ID = 1;
	public static final int BISHOP_ID = 2;
	public static final int ROOK_ID = 3;
	public static final int QUEEN_ID = 4;
	public static final int KING_ID = 5;
	public static final int EMPTY_ID = 6;


	//	PieceItem Count on boardBitmap
	private int pieceItemCounts[] = new int[]{
			8,
			2,
			2,
			2,
			1,
			1
	};
	private int whiteSavedPiecesCount[] = pieceItemCounts.clone();
	private int blackSavedPiecesCount[] = pieceItemCounts.clone();

	private int[] buttonsDrawableIds = new int[]{
			R.drawable.ic_next_game,
			R.drawable.ic_options,
			R.drawable.ic_flip,
			R.drawable.ic_analysis,
			R.drawable.ic_chat,
			R.drawable.ic_back,
			R.drawable.ic_forward,
			R.drawable.ic_hint
	};

	public static final int B_NEW_GAME_ID = 0;
	public static final int B_OPTIONS_ID = 1;
	public static final int B_FLIP_ID = 2;
	public static final int B_ANALYSIS_ID = 3;
	public static final int B_CHAT_ID = 4;
	public static final int B_BACK_ID = 5;
	public static final int B_FORWARD_ID = 6;
	public static final int B_HINT_ID = 7;

	public static final int T_WHITE_TIMER_ID = 8;
	public static final int T_BLACK_TIMER_ID = 9;

	private int whiteAlivePiecesCount[] = new int[6];
	private int blackAlivePiecesCount[] = new int[6];

	//	prefixes
	public static final int BUTTON_PREFIX = 0x00002000;
	public static final int WHITE_FRAME_PREFIX = 0x00001000;
	public static final int BLACK_FRAME_PREFIX = 0x00004000;
	//	private List<String> itemList;
	private TextView movesTextView;
	private BoardViewFace boardViewFace;
    private float density;
	private RelativeLayout timerRelLay;
	private RoboTextView whiteTimer;
	private RoboTextView blackTimer;
	private Resources resources;

	public GamePanelView(Context context) {
		super(context);
		onCreate();
	}

	public GamePanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}


	public void onCreate() {
		setOrientation(VERTICAL);
        density = getContext().getResources().getDisplayMetrics().density;
		resources = getContext().getResources();
        pieceIds = getResources().getIntArray(R.array.pieces_ids);

		controlsLayout = new LinearLayout(getContext());
		int paddingLeft = (int) getResources().getDimension(R.dimen.game_control_padding_left);
		int paddingTop = (int) getResources().getDimension(R.dimen.game_control_padding_top);
		int paddingRight = (int) getResources().getDimension(R.dimen.game_control_padding_right);
		int paddingBottom = (int) getResources().getDimension(R.dimen.game_control_padding_bottom);

		controlsLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		controlsLayout.setLayoutParams(params);

		addControlButton(B_NEW_GAME_ID, R.drawable.button_emboss_left_selector);
		addControlButton(B_OPTIONS_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FLIP_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_ANALYSIS_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_CHAT_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_BACK_ID, R.drawable.button_emboss_mid_selector);
		addControlButton(B_FORWARD_ID, R.drawable.button_emboss_right_selector);
		addView(controlsLayout);

		// create textViews for timers
		whiteTimer = new RoboTextView(getContext(), null,R.attr.playerLabelStyle);
		whiteTimer.setCompoundDrawablesWithIntrinsicBounds(R.drawable.player_indicator_white,0,0,0);
		whiteTimer.setId(BUTTON_PREFIX + T_WHITE_TIMER_ID);
		int timerPaddingLeft = (int) (8*density + 0.5f);
		int timerPaddingRight = (int) (2*density + 0.5f);
		whiteTimer.setPadding(timerPaddingLeft, 0, timerPaddingRight, 0);

		// set whiteTimer params
		RelativeLayout.LayoutParams whiteTimerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		whiteTimer.setLayoutParams(whiteTimerParams);

		blackTimer = new RoboTextView(getContext(), null,R.attr.playerLabelStyle);
		blackTimer.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.player_indicator_black, 0);
		blackTimer.setId(BUTTON_PREFIX + T_BLACK_TIMER_ID);

		int timerPaddingLeft1 = (int) (2*density + 0.5f);
		int timerPaddingRight1 = (int) (8* density + 0.5f);
		blackTimer.setPadding(timerPaddingLeft1, 0, timerPaddingRight1, 0);

		// create layout for text views
		timerRelLay = new RelativeLayout(getContext());
		RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		timerRelLay.setLayoutParams(relParams);

		// set blackTimer params
		RelativeLayout.LayoutParams blackTimerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		blackTimerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		blackTimer.setLayoutParams(blackTimerParams);

		// add to layout
		timerRelLay.addView(whiteTimer);
		timerRelLay.addView(blackTimer);

		timerRelLay.setVisibility(View.INVISIBLE);
		addView(timerRelLay);

		LinearLayout infoLayout = new LinearLayout(getContext());
		infoLayout.setLayoutParams(params);

		// add captured piecesBitmap layout
		LinearLayout.LayoutParams pieceParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LinearLayout piecesLayout = new LinearLayout(getContext());
		// Set weights to moves list and captured piecesBitmap
		pieceParams.weight = 3;
		piecesLayout.setLayoutParams(pieceParams);
		piecesLayout.setOrientation(VERTICAL);
		int pieceLayoutPaddingLeft = (int) getResources().getDimension(R.dimen.piece_layout_padding_left);
		int pieceLayoutPaddingRight = (int) getResources().getDimension(R.dimen.piece_layout_padding_right);
		int pieceLayoutPaddingTop = (int) getResources().getDimension(R.dimen.piece_layout_padding_top);
		int pieceLayoutPaddingBottom = (int) getResources().getDimension(R.dimen.piece_layout_padding_bottom);

		piecesLayout.setPadding(pieceLayoutPaddingLeft, pieceLayoutPaddingTop, pieceLayoutPaddingRight, pieceLayoutPaddingBottom);
		piecesLayout.setGravity(Gravity.CENTER);
//		piecesLayout.setBackgroundColor(resources.getColor(R.color.blue));

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
//        android:gravity="right"


		// change to TextView
//		ListView movesListView = new ListView(getContext());
//		movesListView.setPadding(1, 1, 0, 1);
//		movesListView.setLayoutParams(listParams);
//
//		movesListView.setCacheColorHint(Color.TRANSPARENT);
//		movesListView.setBackgroundColor(Color.TRANSPARENT);
//		movesListView.setDividerHeight(0);
//		movesListView.setDivider(getResources().getDrawable(android.R.color.transparent));
//        infoLayout.addView(movesListView);

		movesTextView = new TextView(getContext());
//        android:inputType="textImeMultiLine"
//        android:ellipsize="start"
//        movesTextView.setEllipsize(TextUtils.TruncateAt.START);
//        movesTextView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		movesTextView.setLayoutParams(listParams);
		movesTextView.setGravity(Gravity.BOTTOM);
		movesTextView.setTextSize(13);
		infoLayout.addView(movesTextView);

		addView(infoLayout);

//		itemList = new ArrayList<String>();

//		movesListView.setAdapter(new MovesAdapter(getContext(), itemList));

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

		addPieceItems(whiteCapturedPieces, true, 1.0f, QUEEN_ID);
		addPieceItems(whiteCapturedPieces, true, 1.0f, ROOK_ID);
		addPieceItems(whiteCapturedPieces, true, 1.0f, BISHOP_ID);
		addPieceItems(whiteCapturedPieces, true, 1.0f, KNIGHT_ID);
		addPieceItems(whiteCapturedPieces, true, 1.0f, PAWN_ID);
		addPieceItems(whiteCapturedPieces, true, 1.0f, KING_ID);

		addPieceItems(blackCapturedPieces, false, 1.0f, QUEEN_ID);
		addPieceItems(blackCapturedPieces, false, 1.0f, ROOK_ID);
		addPieceItems(blackCapturedPieces, false, 1.0f, BISHOP_ID);
		addPieceItems(blackCapturedPieces, false, 1.0f, KNIGHT_ID);
		addPieceItems(blackCapturedPieces, false, 1.0f, PAWN_ID);
		addPieceItems(blackCapturedPieces, false, 1.0f, KING_ID);

//		movesListView.setSelection(movesListView.getAdapter().getCount() - 1);
	}

	private void addControlButton(int buttonId, int backId) {
		controlsLayout.addView(createControlButton(buttonId, backId));
	}

	private View createControlButton(int buttonId, int backId) {
		ImageButton imageButton = new ImageButton(getContext());
		imageButton.setImageResource(buttonsDrawableIds[buttonId]);
		imageButton.setBackgroundResource(backId);
		imageButton.setOnClickListener(this);
		imageButton.setId(BUTTON_PREFIX + buttonId);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		params.weight = 1;
		imageButton.setLayoutParams(params);
		return imageButton;
	}

	public void addControlButton(int position, int buttonId, int backId) {
		controlsLayout.addView(createControlButton(buttonId, backId), position);
	}

	public void toggleControlButton(int buttonId, boolean checked) {
		if (checked) {
			findViewById(BUTTON_PREFIX + buttonId).setBackgroundResource(R.drawable.button_emboss_mid_checked);
		} else {
			findViewById(BUTTON_PREFIX + buttonId).setBackgroundResource(R.drawable.button_emboss_mid_selector);
		}
	}

	public void hideGameButton(int buttonId) {
		findViewById(BUTTON_PREFIX + buttonId).setVisibility(View.GONE);
	}


	public void enableGameButton(int buttonId, boolean enable) {
		findViewById(BUTTON_PREFIX + buttonId).setEnabled(enable);
	}

	public void changeGameButton(int buttonId, int resId) {
		((ImageButton) findViewById(BUTTON_PREFIX + buttonId)).setImageResource(resId);
	}

	public void addMoveLog(CharSequence move) {
//		itemList.clear();
//		itemList.add(move.toString());
		movesTextView.setText(move);
	}

	private int getFramePrefix(boolean isWhite) {
		return isWhite ? WHITE_FRAME_PREFIX : BLACK_FRAME_PREFIX;
	}

	public void capturePiece(boolean isWhite, int pieceId) {
		showPiece(true, isWhite, pieceId);
	}

	public void restorePiece(boolean isWhite, int pieceId) {
		showPiece(false, isWhite, pieceId);
	}
	                     // TODO incapsulate
	private void showPiece(boolean show, boolean isWhite, int pieceId) {
		int frameId = getFramePrefix(isWhite) + pieceIds[pieceId];

		FrameLayout capturedFrame = (FrameLayout) findViewById(frameId);

		PieceItem storedPieceItem = (PieceItem) capturedFrame.getTag();
		// change image
		ImageView imageView = (ImageView) capturedFrame.findViewById(R.id.imagePieceView);

		int maxLevel = storedPieceItem.getLayersCnt();
		int currentLevel = storedPieceItem.getCurrentLevel();

		if (show) {
			if (currentLevel < maxLevel) {
				currentLevel++;
			}
		} else {
			if (currentLevel > 0) {
				currentLevel--;
			}
		}
		storedPieceItem.setCurrentLevel(currentLevel);
		LayerDrawable pieceDrawable;
		if (storedPieceItem.isWhite()) {
			pieceDrawable = createImageDrawable(currentLevel, whitePieceDrawableIds[storedPieceItem.getPieceId()]);
		} else {
			pieceDrawable = createImageDrawable(currentLevel, blackPieceDrawableIds[storedPieceItem.getPieceId()]);
		}
		imageView.setImageDrawable(pieceDrawable);

		capturedFrame.setTag(storedPieceItem);
		invalidate();
	}


	private LayerDrawable createImageDrawable(int layersCnt, int pieceDrawableId) {
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

	private void addPieceItems(LinearLayout viewGroup, boolean isWhite, float itemWeight, int pieceId) {
		int layersCnt = pieceItemCounts[pieceId];
		// Add background image to get correct weights
		ImageView imageView = new ImageView(getContext());
		imageView.setAdjustViewBounds(false);
		imageView.setScaleType(ImageView.ScaleType.CENTER);

		FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LayerDrawable pieceDrawable;
		if (isWhite) {
			pieceDrawable = createImageDrawable(layersCnt, whitePieceDrawableIds[pieceId]);
		} else {
			pieceDrawable = createImageDrawable(layersCnt, blackPieceDrawableIds[pieceId]);
		}

		imageView.setImageDrawable(pieceDrawable);
		imageView.setLayoutParams(imageParams);

		// crate pieceItem
		PieceItem pieceItem = new PieceItem();
		pieceItem.setCode(pieceId);
		pieceItem.setWhite(isWhite);
		pieceItem.setPieceId(pieceId);
		pieceItem.setLayersCnt(layersCnt);
		pieceItem.setPieceFrameId(getFramePrefix(isWhite) + pieceIds[pieceId]);


		imageView.setVisibility(INVISIBLE); // togle preview for test here
		viewGroup.setWeightSum(16f);

		// put image inside frame to get left gravity
		FrameLayout frame = new FrameLayout(getContext());

		frame.addView(imageView);
		frame.setId(pieceItem.getPieceFrameId());
		frame.setTag(pieceItem);

		// Add empty image view to show captured piecesBitmap
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

	public int[] getPieceItemCounts() {
		return pieceItemCounts;
	}

	public int getPieceItemCount(int id) {
		return pieceItemCounts[id];
	}

	private int shiftSize = 4;

	private void shiftLayer(LayerDrawable pieceDrawable, int level) {

		int l = (int) (level * shiftSize*density);
		int r = 0;
		int t = 0;
		int b = 0;
		pieceDrawable.setLayerInset(level, l, t, r, b);
		((BitmapDrawable) pieceDrawable.getDrawable(level)).setGravity(Gravity.LEFT | Gravity.TOP);
	}

	public void dropAlivePieces() {
		for (int i = 0, cnt = whiteAlivePiecesCount.length; i < cnt; i++) {
			whiteAlivePiecesCount[i] = 0;
		}

		for (int i = 0, cnt = blackAlivePiecesCount.length; i < cnt; i++) {
			blackAlivePiecesCount[i] = 0;
		}
	}

	public void addAlivePiece(boolean isWhite, int pieceId) {
		if (pieceId == EMPTY_ID)
			return;
		if (isWhite) {
			whiteAlivePiecesCount[pieceId]++;
		} else {
			blackAlivePiecesCount[pieceId]++;
		}
	}

	public void updateCapturedPieces() {
		// iterate through alive arrays
		manageAlivePiecesCnts(whiteAlivePiecesCount, whiteSavedPiecesCount, true);
		manageAlivePiecesCnts(blackAlivePiecesCount, blackSavedPiecesCount, false);
	}

	private void manageAlivePiecesCnts(int[] alivePiecesCounts, int[] storedPiecesCounts, boolean isWhite) {
		for (int i = 0, cnt = alivePiecesCounts.length; i < cnt; i++) {
			int alivePieceCnt = alivePiecesCounts[i];
			int savedPieceCnt = storedPiecesCounts[i];

			if (alivePieceCnt > savedPieceCnt) {
				int diff = alivePieceCnt - savedPieceCnt;
				for (int j = 0; j < diff; j++) {
					restorePiece(isWhite, i);
					storedPiecesCounts[i]++;
				}
			} else {
				int diff = savedPieceCnt - alivePieceCnt;
				for (int j = 0; j < diff; j++) {
					capturePiece(isWhite, i);
					storedPiecesCounts[i]--;
				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		/*if(view.getId() == BUTTON_PREFIX + B_NEXT_GAME_ID){
			boardViewFace.nextGame();
		}else */
		if (view.getId() == BUTTON_PREFIX + B_NEW_GAME_ID) {
			boardViewFace.newGame();
		} else if (view.getId() == BUTTON_PREFIX + B_OPTIONS_ID) {
			boardViewFace.showOptions();
		} else if (view.getId() == BUTTON_PREFIX + B_HINT_ID) {
			boardViewFace.showHint();
		} else if (view.getId() == BUTTON_PREFIX + B_FLIP_ID) {
			boardViewFace.flipBoard();
		} else if (view.getId() == BUTTON_PREFIX + B_ANALYSIS_ID) {
			boardViewFace.switchAnalysis();
		} else if (view.getId() == BUTTON_PREFIX + B_CHAT_ID) {
			boardViewFace.switchChat();
		} else if (view.getId() == BUTTON_PREFIX + B_BACK_ID) {
			boardViewFace.moveBack();
		} else if (view.getId() == BUTTON_PREFIX + B_FORWARD_ID) {
			boardViewFace.moveForward();
		}
	}

	public void setBoardViewFace(BoardViewFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void haveNewMessage(boolean newMessage) {
		int imgId = newMessage? R.drawable.ic_chat_nm1 :R.drawable.ic_chat;

		((ImageButton) findViewById(BUTTON_PREFIX + B_CHAT_ID)).setImageResource(imgId);
	}

	public void activatePlayerTimer(boolean isWhite, boolean active) {
		timerRelLay.setVisibility(View.VISIBLE);
		TextView textView = isWhite? whiteTimer: blackTimer;
		if(active)
			textView.setTextColor(resources.getColor(R.color.white));
		else
			textView.setTextColor(resources.getColor(R.color.hint_text));
	}

	public void setBlackTimer(String timeString) {
		blackTimer.setText(timeString);
	}

	public void setWhiteTimer(String timeString) {
		whiteTimer.setText(timeString);
	}

//	private class MovesAdapter extends ItemsAdapter<String> {
//		public MovesAdapter(Context context, List<String> itemList) {
//			super(context, itemList);
//		}
//
//		@Override
//		protected View createView(ViewGroup parent) {
//			View view = inflater.inflate(R.layout.game_panel_list_item, parent, false);
//			return view;
//		}
//
//		@Override
//		protected void bindView(String item, int pos, View convertView) {
//			((TextView) convertView).setText(item);
//		}
//	}


}