/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chess.backend.image_load.bitmapfun;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.chess.BuildConfig;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.model.GameDiagramItem;
import com.chess.ui.engine.ChessBoardDiagram;
import com.chess.ui.engine.FenHelper;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.GameFaceHelper;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameFace;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.ui.views.chess_boards.ChessBoardDiagramView;
import com.chess.utilities.AppUtils;

import java.io.FileDescriptor;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width
 * and height. Useful for when the input images might be too large to simply load directly into
 * memory.
 */
public class DiagramImageProcessor extends ImageResizer {
	private static final String TAG = "ImageResizer";
	public static final int DEFAULT = 0;
	public static final int NEXUS_7 = 1;
	protected int mImageWidth;
	protected int mImageHeight;
	private int deviceCode;

	/**
	 * Initialize providing a single target image size (used for both width and height);
	 *
	 */
	public DiagramImageProcessor(Context context, int deviceCode) {
		super(context, 0);
		this.deviceCode = deviceCode;
		setImageSize(0);
	}

	/**
	 * Set the target image width and height.
	 *
	 * @param width
	 * @param height
	 */
	@Override
	public void setImageSize(int width, int height) {
		mImageWidth = width;
		mImageHeight = height;
	}

	/**
	 * Set the target image size (width and height will be the same).
	 *
	 * @param size
	 */
	@Override
	public void setImageSize(int size) {
		setImageSize(size, size);
	}

	/**
	 * The main processing method. This happens in a background task. In this case we are just
	 * sampling down the bitmap and returning it from a resource.
	 *
	 * @param data that contain identifier and View to be processed
	 * @return
	 */
	protected Bitmap processBitmap(Data data) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "processBitmap - " + data.getId());
		}
		Bitmap bitmapFromView;
//		if (deviceCode == NEXUS_7) {
//			// use inset to correct shadow shift
//			bitmapFromView = getBitmapFromView(data.sourceView, mImageWidth, mImageHeight);
//		} else {
			// get bitmap from fragmentView
			bitmapFromView = AppUtils.getBitmapFromView(data.sourceView, mImageWidth, mImageHeight);
//		}

		if (data.sourceView instanceof ChessBoardBaseView) {
			((ChessBoardBaseView)data.sourceView).releaseBitmaps();
		}

		return bitmapFromView;
	}

	@Override
	protected Bitmap processBitmap(Object data) {
		return processBitmap((Data)data);
	}

	private Bitmap getBitmapFromView(View view, int width, int height) {
		// we add inset because of shadow background which have 15px margin
		try {
			int inset = 15;
			Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(returnedBitmap);
			canvas.drawColor(Color.WHITE);
			view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
					View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
			view.layout(0, 0, width, height);

			canvas.save();
			canvas.translate(inset, 0);
			view.draw(canvas);
			canvas.restore();

			return returnedBitmap;
		} catch (OutOfMemoryError ex) {
			return null;
		}
	}

	/**
	 * Decode and sample down a bitmap from resources to the requested width and height.
	 *
	 * @param res The resources object containing the image data
	 * @param resId The resource id of the image data
	 * @param reqWidth The requested width of the resulting bitmap
	 * @param reqHeight The requested height of the resulting bitmap
	 * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
	 * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
	 *         that are equal to or greater than the requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
														 int reqWidth, int reqHeight, ImageCache cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Decode and sample down a bitmap from a file to the requested width and height.
	 *
	 * @param filename The full path of the file to decode
	 * @param reqWidth The requested width of the resulting bitmap
	 * @param reqHeight The requested height of the resulting bitmap
	 * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
	 * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
	 *         that are equal to or greater than the requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromFile(String filename,
													 int reqWidth, int reqHeight, ImageCache cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);
	}

	/**
	 * Decode and sample down a bitmap from a file input stream to the requested width and height.
	 *
	 * @param fileDescriptor The file descriptor to read from
	 * @param reqWidth The requested width of the resulting bitmap
	 * @param reqHeight The requested height of the resulting bitmap
	 * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
	 * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
	 *         that are equal to or greater than the requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromDescriptor(
			FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
		// inBitmap only works with mutable bitmaps so force the decoder to
		// return mutable bitmaps.
		options.inMutable = true;

		if (cache != null) {
			// Try and find a bitmap to use for inBitmap
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

			if (inBitmap != null) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "Found bitmap to use for inBitmap");
				}
				options.inBitmap = inBitmap;
			}
		}
	}

	/**
	 * Calculate an inSampleSize for use in a {@link android.graphics.BitmapFactory.Options} object when decoding
	 * bitmaps using the decode* methods from {@link android.graphics.BitmapFactory}. This implementation calculates
	 * the closest inSampleSize that will result in the final decoded bitmap having a width and
	 * height equal to or larger than the requested width and height. This implementation does not
	 * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
	 * results in a larger bitmap which isn't as useful for caching purposes.
	 *
	 * @param options An options object with out* params already populated (run through a decode*
	 *            method with inJustDecodeBounds==true
	 * @param reqWidth The requested width of the resulting bitmap
	 * @param reqHeight The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
											int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee a final image
			// with both dimensions larger than or equal to the requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger inSampleSize).

			final float totalPixels = width * height;

			// Anything more than 2x the requested pixels we'll sample down further
			final float totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}
		return inSampleSize;
	}

	public static class Data {
		private int id;
		private View sourceView;

		public Data(int id, View sourceView) {
			this.id = id;
			this.sourceView = sourceView;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public View getSourceView() {
			return sourceView;
		}

		public void setSourceView(View sourceView) {
			this.sourceView = sourceView;
		}

		@Override
		public String toString() {
			return "diagram_" + String.valueOf(id);

		}
	}

	public static View createBoardView(GameDiagramItem diagramItem, Context context) {

		Resources resources = context.getResources();
		GameFace gameFaceHelper = new GameFaceHelper(context);
		ChessBoardDiagramView boardView = new ChessBoardDiagramView(context);

		boardView.setGameFace(gameFaceHelper);
		boardView.setCustomPiecesName(context.getString(R.string.pieces_alpha));
		boardView.setCustomBoard(R.drawable.board_green);
		int highlightColor = resources.getColor(R.color.highlight_green_default);

		boardView.setCustomHighlight(highlightColor);

		int coordinateColorLight = resources.getColor(R.color.coordinate_green_default_light);
		int coordinateColorDark = resources.getColor(R.color.coordinate_green_default_dark);
		boardView.setCustomCoordinatesColors(new int[]{coordinateColorLight, coordinateColorDark});

		ChessBoardDiagram.resetInstance();
		BoardFace boardFace = gameFaceHelper.getBoardFace();

		if (diagramItem.getGameType() == RestHelper.V_GAME_CHESS_960) {
			boardFace.setChess960(true);
		} else {
			boardFace.setChess960(false);
		}

		String fen = diagramItem.getFen();
		boardFace.setupBoard(fen);

		// revert reside back, because for diagrams white is always at bottom
		if (!TextUtils.isEmpty(fen) && !fen.contains(FenHelper.WHITE_TO_MOVE)) {
			boardFace.setReside(!boardFace.isReside());
		}

		boardFace.setReside(diagramItem.isFlip());

		// remove comments from movesList
		String movesList = diagramItem.getMovesList();
		if (movesList != null) {
			movesList = boardFace.removeCommentsAndAlternatesFromMovesList(movesList);
			boardFace.checkAndParseMovesList(movesList);
			while(boardFace.takeBack()) {

			}
			if (diagramItem.getFocusMove() != 0) {
				for (int i = 0; i < diagramItem.getFocusMove(); i++) {
					Move move = boardFace.getNextMove();
					if (move != null) {
						boardFace.makeMove(move, false);
					}
				}
			}
		}

		return boardView;
	}
}
