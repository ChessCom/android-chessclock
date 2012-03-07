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

import java.util.ArrayList;
import java.util.List;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class GamePanelView extends LinearLayout {

	private LinearLayout whiteCapturedPieces;
	private LinearLayout blackCapturedPieces;
	private ListView movesListView;
	private LinearLayout controlsLayout;
	private static final int DEFAULT_HEIGHT = 50;
	private static final int DEFAULT_WIDTH = 50;
	private int mMaxChildWidth;
	private int mMaxChildHeight;

	public GamePanelView(Context context) {
		super(context);
		onCreate();
	}

	public GamePanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}


	private void addControlButton(int imageId,int backId){
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

		controlsLayout = new LinearLayout(getContext());
		controlsLayout.setPadding(10,10,10,10);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		controlsLayout.setLayoutParams(params);

		addControlButton(R.drawable.ic_fastforward, R.drawable.button_emboss_left_selector);
		addControlButton(R.drawable.ic_options,R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_flip,R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_analysis,R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_chat,R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_back,R.drawable.button_emboss_mid_selector);
		addControlButton(R.drawable.ic_forward,R.drawable.button_emboss_right_selector);
		addView(controlsLayout);

		whiteCapturedPieces = new LinearLayout(getContext());
		blackCapturedPieces = new LinearLayout(getContext());
		movesListView = new ListView(getContext());


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
		piecesLayout.setPadding(7,0,0,0);

		piecesLayout.addView(whiteCapturedPieces);
		piecesLayout.addView(blackCapturedPieces);

		infoLayout.addView(piecesLayout);
		// add captured pieces layout
		LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		listParams.weight = 7;
		movesListView.setLayoutParams(listParams);

		movesListView.setCacheColorHint(Color.TRANSPARENT);
		movesListView.setBackgroundColor(Color.TRANSPARENT);
		movesListView.setDividerHeight(0);
//		movesListView.setDivider(getResources().g);
		infoLayout.addView(movesListView);

		addView(infoLayout);

		List<String> itemList = new ArrayList<String>();

		itemList.add("42. Bf3  Ke7");
		itemList.add("43. Nd5+  Nxd5");
		itemList.add("44. exd5  Ke7");
		itemList.add("44. exd5  Ke7");
		itemList.add("44. exd5  Ke7");
		itemList.add("44. exd5  Ke7");
		itemList.add("44. exd5  Ke7");
		itemList.add("44. exd5  Ke7");
		itemList.add("44. exd5  Ke7");

		movesListView.setAdapter(new MovesAdapter(getContext(), itemList));

		addItems(whiteCapturedPieces, R.drawable.captured_bq, 1, 1.0f);
		addItems(whiteCapturedPieces, R.drawable.captured_br, 2, 1.0f);
		addItems(whiteCapturedPieces, R.drawable.captured_bb, 2, 1.0f);
		addItems(whiteCapturedPieces, R.drawable.captured_bn, 2, 1.0f);
		addItems(whiteCapturedPieces, R.drawable.captured_bp, 8, 1.0f);
		addItems(whiteCapturedPieces, R.drawable.captured_bk, 1, 1.0f);

		addItems(blackCapturedPieces, R.drawable.captured_wq, 1, 1.0f);
		addItems(blackCapturedPieces, R.drawable.captured_wr, 2, 1.0f);
		addItems(blackCapturedPieces, R.drawable.captured_wb, 2, 1.0f);
		addItems(blackCapturedPieces, R.drawable.captured_wn, 2, 1.0f);
		addItems(blackCapturedPieces, R.drawable.captured_wp, 8, 1.0f);
		addItems(blackCapturedPieces, R.drawable.captured_wk, 1, 1.0f);

		movesListView.setSelection(movesListView.getAdapter().getCount() - 1);

	}


	private void addItems(LinearLayout viewGroup, int pieceId, int layersCnt, float itemWeight) {

		Drawable[] layers = new Drawable[layersCnt];

		for (int j = 0; j < layersCnt; j++) {
			layers[j] = getResources().getDrawable(pieceId);
		}

		LayerDrawable pieceDrawable = new LayerDrawable(layers);

		for (int i = 0; i < layersCnt; i++) {
			shiftLayer(pieceDrawable, i);
		}

		ImageView imageView = new ImageView(getContext());
		imageView.setAdjustViewBounds(false);
		imageView.setScaleType(ImageView.ScaleType.CENTER);


		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		params.weight = itemWeight;
		params.gravity = Gravity.LEFT;

		imageView.setImageDrawable(pieceDrawable);
		imageView.setLayoutParams(imageParams);
		viewGroup.setWeightSum(16f);

		// put iamge inside frame to get left gravity
		FrameLayout frame = new FrameLayout(getContext());
		frame.addView(imageView);
		frame.setLayoutParams(params);

		viewGroup.addView(frame);
		viewGroup.setGravity(Gravity.LEFT);
	}

	private int shiftSize = 6;

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

//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		mMaxChildWidth = 0;
//		mMaxChildHeight = 0;
//
//		// Measure once to find the maximum child size.
//
//		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec),
//				MeasureSpec.AT_MOST);
//		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec),
//				MeasureSpec.AT_MOST);
//
//		final int count = getChildCount();
//		for (int i = 0; i < count; i++) {
//			final View child = getChildAt(i);
//			if (child.getVisibility() == GONE) {
//				continue;
//			}
//
//			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//
//			mMaxChildWidth = Math.max(mMaxChildWidth, child.getMeasuredWidth());
//			mMaxChildHeight = Math.max(mMaxChildHeight, child.getMeasuredHeight());
//		}
//
//		// Measure again for each child to be exactly the same size.
//
//		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildWidth, MeasureSpec.AT_MOST);
//		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildHeight, MeasureSpec.AT_MOST);
//
//		for (int i = 0; i < count; i++) {
//			final View child = getChildAt(i);
//			if (child.getVisibility() == GONE) {
//				continue;
//			}
//
////			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//		}
//
//		setMeasuredDimension(resolveSize(mMaxChildWidth, widthMeasureSpec),
//				resolveSize(mMaxChildHeight, heightMeasureSpec));
//	}


	private int measureParam(int valueMeasureSpec, int value) {
		switch (View.MeasureSpec.getMode(valueMeasureSpec)) {
			case MeasureSpec.EXACTLY:
				return MeasureSpec.getSize(valueMeasureSpec);
			case MeasureSpec.AT_MOST:
				return Math.min(value, MeasureSpec.getSize(valueMeasureSpec));
			default:
			case MeasureSpec.UNSPECIFIED:
				return value;
		}
	}
}