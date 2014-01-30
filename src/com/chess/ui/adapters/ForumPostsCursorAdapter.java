package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.entity.api.ArticleDetailsItem;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.DiagramImageProcessor;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.model.GameDiagramItem;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.articles.ArticleDetailsFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboTextView;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 20:21
 */
public class ForumPostsCursorAdapter extends ItemsCursorAdapter {

	public static final String DIAGRAM_TAG = "<!-- \n&-diagramtype:";
	public static final String END_TAG = "-->";
	private static final int NON_INIT = -1;
	private static final int CONTAINER_ID = 0x00009843;
	public static final String WHITE_WON = "1-0";
	public static final String BLACK_WON = "0-1";
	public static final String DRAW = "1/2-1/2";
	public static final String ONGOING = "*";
	public static final String PAIR_START_TAG = "[";
	public static final String PAIR_END_TAG = "\"]";

	private final int imageSize;
	private final SparseArray<String> countryMap;
	private final SparseArray<Drawable> countryDrawables;
	private final ItemClickListenerFace clickFace;
	private DiagramImageProcessor diagramImageProcessor;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;
	private final int textColor;
	private final int textSize;
	private final int infoTextSize;
	private final int paddingSide;
	private final int iconOverlaySize;
	private final int iconOverlayColor;
	public static float IMAGE_WIDTH_PERCENT = 0.80f;

	private static final int DIAGRAM = 0;
	private static final int TEXT = 1;

	public ForumPostsCursorAdapter(ItemClickListenerFace clickFace, Cursor cursor,
								   SmartImageFetcher imageFetcher, DiagramImageProcessor diagramImageProcessor) {
		super(clickFace.getMeContext(), cursor, imageFetcher);
		this.clickFace = clickFace;
		this.diagramImageProcessor = diagramImageProcessor;
		imageSize = resources.getDimensionPixelSize(R.dimen.chat_icon_size);

		String[] countryNames = resources.getStringArray(R.array.new_countries);
		int[] countryCodes = resources.getIntArray(R.array.new_country_ids);
		countryMap = new SparseArray<String>();
		for (int i = 0; i < countryNames.length; i++) {
			countryMap.put(countryCodes[i], countryNames[i]);
		}
		countryDrawables = new SparseArray<Drawable>();

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();

		// diagram init
		if (AppUtils.inLandscape(context)) {
			screenWidth -= resources.getDimensionPixelSize(R.dimen.tablet_side_menu_width) * 2;
		} else {
			screenWidth = resources.getDisplayMetrics().widthPixels;
		}

		textColor = resources.getColor(R.color.new_subtitle_dark_grey);
		textSize = (int) (resources.getDimensionPixelSize(R.dimen.content_text_size) / density);
		infoTextSize = (int) (resources.getDimensionPixelSize(R.dimen.content_info_text_size) / density);
		paddingSide = resources.getDimensionPixelSize(R.dimen.default_scr_side_padding);
		iconOverlaySize = (int) (resources.getDimension(R.dimen.diagram_icon_overlay_size) / density);
		iconOverlayColor = resources.getColor(R.color.semitransparent_white_75);

		// for tablets make diagram wider
		if (isTablet) {
			IMAGE_WIDTH_PERCENT = 0.85f;
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (getString(mCursor, DbScheme.V_DESCRIPTION).contains(DIAGRAM_TAG)) {
			return DIAGRAM;
		} else {
			return TEXT;
		}
	}

	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount() + 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (!mDataValid) {
			throw new IllegalStateException("this should only be called when the cursor is valid");
		}
		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}
		View view;
		boolean isForDiagram = getItemViewType(position) == DIAGRAM;
		if (convertView == null) {
			if (isForDiagram) {
				view = createDiagramView(parent);
			} else {
				view = newView(mContext, mCursor, parent);
			}
		} else {
			view = convertView;
			if (isForDiagram && !(view.getTag() instanceof DiagramViewHolder)) {
				view = createDiagramView(parent);
			} else if (!isForDiagram && !(view.getTag() instanceof ViewHolder)) {
				view = newView(mContext, mCursor, parent);
			}
		}
		if (isForDiagram) {
			bindDiagramView(view, mContext, mCursor);
		} else {
			bindView(view, mContext, mCursor);
		}

		return view;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_forum_post_list_item, parent, false);
		ViewHolder holder = new ViewHolder();

		holder.photoImg = (ProgressImageView) view.findViewById(R.id.photoImg);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.countryImg = (ImageView) view.findViewById(R.id.countryImg);
		holder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		holder.quoteTxt = (TextView) view.findViewById(R.id.quoteTxt);
		holder.bodyTxt = (TextView) view.findViewById(R.id.bodyTxt);
		holder.commentNumberTxt = (TextView) view.findViewById(R.id.commentNumberTxt);

		view.setTag(holder);

		holder.quoteTxt.setOnClickListener(clickFace);
		holder.bodyTxt.setMovementMethod(LinkMovementMethod.getInstance());

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		bindPostData(context, cursor, holder);
	}

	private void bindPostData(Context context, Cursor cursor, ViewHolder holder) {
		holder.quoteTxt.setTag(R.id.list_item_id, cursor.getPosition());

		holder.authorTxt.setText(getString(cursor, DbScheme.V_USERNAME));
		holder.commentNumberTxt.setText("# " + getInt(cursor, DbScheme.V_NUMBER));
		String contentStr = correctLinks(getString(cursor, DbScheme.V_DESCRIPTION));

		loadTextWithImage(holder.bodyTxt, contentStr);

		long timestamp = getLong(cursor, DbScheme.V_CREATE_DATE);
		String lastCommentAgoStr = AppUtils.getMomentsAgoFromSeconds(timestamp, context);
		holder.dateTxt.setText(lastCommentAgoStr + Symbol.BULLET);

		// set premium icon
		int status = getInt(cursor, DbScheme.V_PREMIUM_STATUS);
		holder.premiumImg.setImageResource(AppUtils.getPremiumIcon(status));

		// set country flag
		int countryId = getInt(cursor, DbScheme.V_COUNTRY_ID);
		Drawable drawable;
		if (countryDrawables.get(countryId) == null) {
			drawable = AppUtils.getCountryFlagScaled(context, countryMap.get(countryId));
			countryDrawables.put(countryId, drawable);
		} else {
			drawable = countryDrawables.get(countryId);
		}

		holder.countryImg.setImageDrawable(drawable);

		String avatarUrl = getString(cursor, DbScheme.V_PHOTO_URL);
		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.photoImg.getImageView());
	}

	private String correctLinks(String content) {
		return content.replaceAll("href=\"/", "href=\"http://www.chess.com/");
	}

	private void bindDiagramView(View convertView, Context context, Cursor cursor) {
		DiagramViewHolder holder = (DiagramViewHolder) convertView.getTag();
		int pos = cursor.getPosition();

		String bodyStr = getString(cursor, DbScheme.V_DESCRIPTION);
		// use diagram data to create board image
		ArticleDetailsItem.Diagram diagramToShow = getDiagramItem(bodyStr);
		final GameDiagramItem diagramItem = new GameDiagramItem();
		diagramItem.setShowAnimation(false);
		if (diagramToShow.getType() == ArticleDetailsItem.Diagram.PUZZLE) {
			diagramItem.setMovesList(diagramToShow.getMoveList());
			diagramItem.setDiagramType(ArticleDetailsItem.CHESS_PROBLEM);
		} else if (diagramToShow.getType() == ArticleDetailsItem.Diagram.CHESS_GAME) {
			diagramItem.setMovesList(diagramToShow.getMoveList());
			diagramItem.setDiagramType(ArticleDetailsItem.CHESS_GAME);
		} else {
			diagramItem.setDiagramType(ArticleDetailsItem.SIMPLE_DIAGRAM);
		}

		String fen = diagramToShow.getFen();
		diagramItem.setFen(fen);
		diagramItem.setFlip(diagramToShow.getFlip());
		diagramItem.setFocusMove(diagramToShow.getFocusNode());

		// set diagramItem as tag to get it in onClick
		holder.contentTxt.setTag(itemListId, diagramItem);
		holder.imageView.setTag(itemListId, diagramItem);

		// create board with pieces based on diagram
		View boardView = DiagramImageProcessor.createBoardView(diagramItem, context);

		// get bitmap from fragmentView
		int bitmapWidth = (int) (screenWidth * IMAGE_WIDTH_PERCENT);
		int bitmapHeight = (int) (screenWidth * IMAGE_WIDTH_PERCENT);
		// fill data for load image
		int diagramId = fen.hashCode();
		DiagramImageProcessor.Data data = new DiagramImageProcessor.Data(diagramId, boardView);

		// load image out off UI thread
		diagramImageProcessor.setImageSize(bitmapWidth, bitmapHeight);
		diagramImageProcessor.loadImage(data, holder.imageView);


		if (diagramToShow.getType() == ArticleDetailsItem.Diagram.SIMPLE) {
			holder.iconView.setVisibility(View.GONE);
			holder.userToMoveTxt.setVisibility(View.VISIBLE);
			holder.userToMoveTxt.setText(diagramToShow.getUserToMove());
		} else {
			holder.iconView.setVisibility(View.VISIBLE);
			holder.iconView.setTag(itemListId, pos);
			holder.userToMoveTxt.setVisibility(View.GONE);
		}

		// set players and game info
		String players = diagramToShow.getPlayers();
		if (TextUtils.isEmpty(players)) {
			holder.playersTxt.setVisibility(View.GONE);
		} else {
			holder.playersTxt.setVisibility(View.VISIBLE);
			holder.playersTxt.setText(players);
		}

		String gameInfo = diagramToShow.getGameInfo();
		if (TextUtils.isEmpty(gameInfo)) {
			holder.infoTxt.setVisibility(View.GONE);
		} else {
			holder.infoTxt.setVisibility(View.VISIBLE);
			holder.infoTxt.setText(gameInfo);
		}

		holder.contentTxt.setVisibility(View.GONE);

		bindPostData(context, cursor, holder.postHolder);
	}

	private View createDiagramView(ViewGroup parent) {
		RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.new_forum_post_list_item, parent, false);

		DiagramViewHolder holder = new DiagramViewHolder();

		// create container for
		LinearLayout container = new LinearLayout(context);
		RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		textLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

		container.setOrientation(LinearLayout.VERTICAL);
		container.setLayoutParams(containerParams);
		container.setId(CONTAINER_ID);

		{// add text info above diagram "Serper vs. Dorfman"
			RoboTextView textView = new RoboTextView(context);
			textView.setTextSize(textSize);
			textView.setTextColor(textColor);
			textView.setPadding(paddingSide, 0, paddingSide, 0);
			textView.setFont(FontsHelper.BOLD_FONT);

			container.addView(textView, textLayoutParams);
			holder.playersTxt = textView;
		}

		{// add second line text info above diagram "Clock simul | Tashkent | 1983 | ECO: B63 | 1-0"
			RoboTextView textView = new RoboTextView(context);
			textView.setTextSize(infoTextSize);
			textView.setTextColor(textColor);
			textView.setPadding(paddingSide, 0, paddingSide, 0);

			container.addView(textView, textLayoutParams);
			holder.infoTxt = textView;
		}

		// add FrameLayout for imageView and fragment container
		FrameLayout frameLayout = new FrameLayout(context);
		int frameWidth = screenWidth;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(frameWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;

		frameLayout.setLayoutParams(params);

		{// add imageView with diagram bitmap
			// take 80% of screen width
			int imageSize = (int) (screenWidth * IMAGE_WIDTH_PERCENT);

			FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(imageSize, imageSize);
			imageParams.gravity = Gravity.CENTER;

			final ImageView imageView = new ImageView(context);
			imageView.setLayoutParams(imageParams);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setId(ArticleDetailsFragment.IMAGE_PREFIX);

			// set click handler and then get this tag from view to show diagram
			imageView.setOnClickListener(clickFace);

			holder.imageView = imageView;

			frameLayout.addView(imageView);

			imageView.setBackgroundResource(R.drawable.shadow_back_square);
		}

		container.addView(frameLayout);
		holder.fragmentContainer = frameLayout;

		// add icon for pop-out
		{
			FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			iconParams.gravity = Gravity.CENTER;

			final RoboTextView iconView = new RoboTextView(context);
			iconView.setFont(FontsHelper.ICON_FONT);
			iconView.setText(R.string.ic_expand);
			iconView.setTextSize(iconOverlaySize);
			iconView.setTextColor(iconOverlayColor);
			iconView.setId(ArticleDetailsFragment.ICON_PREFIX);

			float shadowRadius = 1 * density + 0.5f;
			float shadowDx = 1 * density;
			float shadowDy = 1 * density;
			iconView.setShadowLayer(shadowRadius, shadowDx, shadowDy, 0x88000000);

			// set click handler and then get this tag from view to show diagram
			iconView.setOnClickListener(clickFace);

			frameLayout.addView(iconView, iconParams);
			holder.iconView = iconView;
		}

		{// add "White to move"
			RoboTextView textView = new RoboTextView(context);
			textView.setTextSize(textSize);
			textView.setTextColor(textColor);
			textView.setPadding(paddingSide, 0, paddingSide, 0);
			textView.setFont(FontsHelper.BOLD_FONT);

			container.addView(textView, textLayoutParams);
			holder.userToMoveTxt = textView;
		}

		{// add text content
			RoboTextView textView = new RoboTextView(context, null, R.attr.contentStyle);
			textView.setTextSize(textSize);
			textView.setTextColor(textColor);
			textView.setPadding(paddingSide, paddingSide, paddingSide, 0);
			textView.setId(ArticleDetailsFragment.TEXT_PREFIX);
			textView.setOnClickListener(clickFace);
			textView.setMovementMethod(LinkMovementMethod.getInstance());

			container.addView(textView);
			holder.contentTxt = textView;
		}

		{ // posts view
			holder.postHolder = new ViewHolder();
			holder.postHolder.photoImg = (ProgressImageView) view.findViewById(R.id.photoImg);
			holder.postHolder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
			holder.postHolder.countryImg = (ImageView) view.findViewById(R.id.countryImg);
			holder.postHolder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
			holder.postHolder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
			holder.postHolder.quoteTxt = (TextView) view.findViewById(R.id.quoteTxt);
			holder.postHolder.bodyTxt = (TextView) view.findViewById(R.id.bodyTxt);
			holder.postHolder.commentNumberTxt = (TextView) view.findViewById(R.id.commentNumberTxt);

			holder.postHolder.quoteTxt.setOnClickListener(clickFace);
			holder.postHolder.bodyTxt.setMovementMethod(LinkMovementMethod.getInstance());

			// change layout params
			((RelativeLayout.LayoutParams) holder.postHolder.bodyTxt.getLayoutParams())
					.addRule(RelativeLayout.BELOW, CONTAINER_ID);
		}

		containerParams.setMargins(0, (int) (5 * density), 0, 0);
		containerParams.addRule(RelativeLayout.BELOW, R.id.dateTxt);

		view.addView(container);
		view.setTag(holder);
		return view;
	}

	protected class ViewHolder {
		public ProgressImageView photoImg;
		public TextView authorTxt;
		public ImageView countryImg;
		public ImageView premiumImg;
		public TextView dateTxt;
		public TextView quoteTxt;
		public TextView bodyTxt;
		public TextView commentNumberTxt;
	}

	private class DiagramViewHolder {
		View fragmentContainer;
		ImageView imageView;
		TextView iconView;
		TextView contentTxt;
		TextView playersTxt;
		TextView infoTxt;
		TextView userToMoveTxt;

		ViewHolder postHolder;
	}

	private ArticleDetailsItem.Diagram getDiagramItem(String bodyStr) {
		ArticleDetailsItem.Diagram diagram = new ArticleDetailsItem.Diagram();

		int start = bodyStr.indexOf(DIAGRAM_TAG);
		String diagramCode = bodyStr.substring(start);
		diagramCode = diagramCode.substring(0, diagramCode.indexOf(END_TAG));

		diagram.setDiagramCode(diagramCode);
		{ // extract moveList
			// get [Event ] end
			int startIndex = diagramCode.lastIndexOf(PAIR_START_TAG);

			// truncate text to more close state, because "\r\n\r\n doesn't work for some diagrams
			// we count from last [ ]
			String movesPart = diagramCode.substring(startIndex + PAIR_START_TAG.length());
			startIndex = movesPart.indexOf(PAIR_END_TAG);
			movesPart = movesPart.substring(startIndex + PAIR_END_TAG.length());

			// Result: the result of the game. This can only have four possible values: "1-0" (White won), "0-1" (Black won), "1/2-1/2" (Draw), or "*"
			int endIndex = movesPart.lastIndexOf(WHITE_WON);
			if (endIndex == NON_INIT) {
				endIndex = movesPart.lastIndexOf(BLACK_WON);
			}
			if (endIndex == NON_INIT) {
				endIndex = movesPart.lastIndexOf(DRAW);
			}
			if (endIndex == NON_INIT) {
				endIndex = movesPart.lastIndexOf(ONGOING);
			}

			String moveList = movesPart.substring(0, endIndex).replaceAll("\n", Symbol.EMPTY).replaceAll("\r", Symbol.EMPTY);
			diagram.setMoveList(moveList);
		}
		return diagram;
	}
}
